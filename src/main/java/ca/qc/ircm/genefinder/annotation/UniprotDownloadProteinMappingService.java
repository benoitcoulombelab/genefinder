package ca.qc.ircm.genefinder.annotation;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.rest.RestClientFactory;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.genefinder.xml.StackSaxHandler;
import ca.qc.ircm.progressbar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Download protein mappings from RefSeq database.
 */
@Component
public class UniprotDownloadProteinMappingService implements DownloadProteinMappingService {
  private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
  private static final int MAX_RETRIES = 5;
  private static final long RETRY_SLEEP_TIME = 2000;
  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(UniprotDownloadProteinMappingService.class);
  @Inject
  private UniprotConfiguration uniprotConfiguration;
  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private RestClientFactory restClientFactory;
  @Inject
  private ProteinService proteinService;

  protected UniprotDownloadProteinMappingService() {
  }

  protected UniprotDownloadProteinMappingService(UniprotConfiguration uniprotConfiguration,
      NcbiConfiguration ncbiConfiguration, RestClientFactory restClientFactory,
      ProteinService proteinService) {
    this.uniprotConfiguration = uniprotConfiguration;
    this.ncbiConfiguration = ncbiConfiguration;
    this.restClientFactory = restClientFactory;
    this.proteinService = proteinService;
  }

  @Override
  public List<ProteinMapping> downloadProteinMappings(List<String> proteinIds,
      FindGenesParameters parameters, ProgressBar progressBar, Locale locale)
      throws IOException, InterruptedException {
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    int steps = 0;
    steps += isDownloadMappings(parameters) ? 1 : 0;
    steps += isDownloadGeneInfo(parameters) ? 1 : 0;
    double step = 1.0 / steps;
    List<ProteinMapping> mappings = proteinIds.stream().distinct().map(id -> new ProteinMapping(id))
        .collect(Collectors.toList());
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadMappings(parameters)) {
      downloadMappings(mappings, parameters, progressBar.step(step / 2), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadGeneInfo(parameters)) {
      downloadGeneInfo(mappings, parameters, progressBar.step(step / 2), resources);
    }
    progressBar.setProgress(1.0);
    return mappings;
  }

  private <R> R retry(Callable<R> callable, int maxRetries) throws Exception {
    int tries = 0;
    while (true) {
      try {
        return callable.call();
      } catch (Exception e) {
        if (e instanceof InterruptedException) {
          throw e;
        }
        if (tries++ == MAX_RETRIES) {
          throw e;
        }
        Thread.sleep(RETRY_SLEEP_TIME);
      }
    }
  }

  private boolean isDownloadMappings(FindGenesParameters parameters) {
    return parameters.isGeneId() || parameters.isGeneName() || parameters.isGeneSummary()
        || parameters.isGeneSynonyms() || parameters.isSequence()
        || parameters.isProteinMolecularWeight();
  }

  private void downloadMappings(List<ProteinMapping> mappings, FindGenesParameters parameters,
      ProgressBar progressBar, MessageResources resources)
      throws IOException, InterruptedException {
    final Map<String, ProteinMapping> mappingsById = mappings.stream()
        .collect(Collectors.toMap(mapping -> mapping.getProteinId(), mapping -> mapping));
    Map<Integer, BiConsumer<ProteinMapping, String>> columnConsumers = new HashMap<>();
    StringBuilder columnsBuilder = new StringBuilder("id");
    int index = 1;
    if (parameters.isGeneId() || parameters.isGeneName() || parameters.isGeneSummary()
        || parameters.isGeneSynonyms()) {
      columnConsumers.put(index++, (mapping, value) -> {
        String[] geneIds = value.split(";");
        for (String geneId : geneIds) {
          if (!geneId.isEmpty()) {
            addGeneInfo(mapping, new GeneInfo(Long.parseLong(geneId)));
          }
        }
      });
      columnsBuilder.append(",database(GeneID)");
    }
    if (parameters.isSequence() || parameters.isProteinMolecularWeight()) {
      columnConsumers.put(index++, (mapping, value) -> {
        setSequence(mapping, value, parameters);
      });
      columnsBuilder.append(",sequence");
    }
    Client client = restClientFactory.createClient();
    client.register(LoggingFeature.class);
    client.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT,
        LoggingFeature.Verbosity.HEADERS_ONLY);
    WebTarget target = client.target(uniprotConfiguration.mapping());
    target = target.queryParam("from", "ACC,ID");
    target = target.queryParam("to", "ACC");
    target = target.queryParam("format", "tab");
    target = target.queryParam("columns", columnsBuilder.toString());
    List<String> proteinIds = new ArrayList<>(mappingsById.keySet());
    int maxIdsPerRequest = uniprotConfiguration.maxIdsPerRequest();
    for (int i = 0; i < proteinIds.size(); i += maxIdsPerRequest) {
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      progressBar.setMessage(resources.message("downloadMappings", i,
          Math.min(i + maxIdsPerRequest, proteinIds.size()), proteinIds.size()));
      String ids =
          proteinIds.stream().skip(i).limit(maxIdsPerRequest).collect(Collectors.joining(" "));
      final Invocation.Builder request = target.queryParam("query", ids).request();
      try {
        retry(() -> {
          try (BufferedReader reader = new BufferedReader(
              new InputStreamReader(request.get(InputStream.class), UTF_8_CHARSET))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
              String[] columns = line.split("\t");
              String id = columns[0];
              ProteinMapping mapping = mappingsById.get(id);
              if (mapping != null) {
                for (int j = 1; j < columns.length; j++) {
                  if (columnConsumers.containsKey(j)) {
                    columnConsumers.get(j).accept(mapping, columns[j]);
                  }
                }
              }
            }
          }
          return null;
        }, MAX_RETRIES);
      } catch (Exception e) {
        ExceptionUtils.throwExceptionIfMatch(e, IOException.class);
        ExceptionUtils.throwExceptionIfMatch(e, InterruptedException.class);
        throw new IOException(e);
      }
    }
  }

  private void addGeneInfo(ProteinMapping mapping, GeneInfo geneInfo) {
    if (mapping.getGenes() == null) {
      mapping.setGenes(new ArrayList<>());
    }
    mapping.getGenes().add(geneInfo);
  }

  private boolean isDownloadGeneInfo(FindGenesParameters parameters) {
    return parameters.isGeneName() || parameters.isGeneSummary() || parameters.isGeneSynonyms();
  }

  private void downloadGeneInfo(List<ProteinMapping> mappings, FindGenesParameters parameters,
      ProgressBar progressBar, MessageResources resources)
      throws IOException, InterruptedException {
    final Map<Long, List<GeneInfo>> genesById = new HashMap<>();
    mappings.stream().map(mapping -> mapping.getGenes()).filter(genes -> genes != null)
        .flatMap(genes -> genes.stream()).forEach(gene -> {
          if (!genesById.containsKey(gene.getId())) {
            genesById.put(gene.getId(), new ArrayList<>());
          }
          genesById.get(gene.getId()).add(gene);
        });
    ;
    Client client = restClientFactory.createClient();
    client.register(LoggingFeature.class);
    client.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT,
        LoggingFeature.Verbosity.PAYLOAD_ANY);
    WebTarget target = client.target(ncbiConfiguration.eutils());
    target = target.path("esummary.fcgi");
    List<Long> geneIds = new ArrayList<>(genesById.keySet());
    int maxIdsPerRequest = ncbiConfiguration.maxIdsPerRequest();
    for (int i = 0; i < geneIds.size(); i += maxIdsPerRequest) {
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      progressBar.setMessage(resources.message("downloadGenes", i,
          Math.min(i + maxIdsPerRequest, geneIds.size()), geneIds.size()));
      Form form = new Form();
      form.param("db", "gene");
      form.param("id", geneIds.stream().skip(i).limit(maxIdsPerRequest)
          .map(id -> String.valueOf(id)).collect(Collectors.joining(",")));
      final Invocation.Builder request = target.request();
      try {
        retry(() -> {
          try (InputStream input = new BufferedInputStream(
              request.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                  InputStream.class))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(input, new StackSaxHandler() {
              private Long id;
              private boolean saveCharacter;
              private StringBuilder builder = new StringBuilder();

              @Override
              protected void startElement(String elementName, Attributes attributes)
                  throws SAXException {
                if (current("DocumentSummary") && hasAttribute("uid")) {
                  id = Long.valueOf(attribute("uid"));
                } else if (current("Name")) {
                  builder.delete(0, builder.length());
                  saveCharacter = true;
                } else if (current("Description")) {
                  builder.delete(0, builder.length());
                  saveCharacter = true;
                } else if (current("OtherAliases")) {
                  builder.delete(0, builder.length());
                  saveCharacter = true;
                }
              }

              @Override
              protected void endElement(String elementName) {
                if (parameters.isGeneName() && current("Name")) {
                  genesById.get(id).stream().forEach(gene -> gene.setSymbol(builder.toString()));
                  saveCharacter = false;
                } else if (parameters.isGeneSummary() && current("Description")) {
                  genesById.get(id).stream()
                      .forEach(gene -> gene.setDescription(builder.toString()));
                  saveCharacter = false;
                } else if (parameters.isGeneSynonyms() && current("OtherAliases")) {
                  if (!builder.toString().isEmpty()) {
                    genesById.get(id).stream().forEach(
                        gene -> gene.setSynonyms(Arrays.asList(builder.toString().split(", "))));
                  }
                  saveCharacter = false;
                }
              }

              @Override
              public void characters(char[] ch, int start, int length) throws SAXException {
                if (saveCharacter) {
                  builder.append(ch, start, length);
                }
              }
            });
          } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Could not parse esummary response", e);
          }
          return null;
        }, MAX_RETRIES);
      } catch (Exception e) {
        ExceptionUtils.throwExceptionIfMatch(e, IOException.class);
        ExceptionUtils.throwExceptionIfMatch(e, InterruptedException.class);
        throw new IOException(e);
      }
    }
  }

  private void setSequence(ProteinMapping mapping, String sequence,
      FindGenesParameters parameters) {
    if (parameters.isSequence()) {
      mapping.setSequence(sequence);
    }
    if (parameters.isProteinMolecularWeight()) {
      double weight = proteinService.weight(sequence);
      mapping.setMolecularWeight(weight);
    }
  }
}
