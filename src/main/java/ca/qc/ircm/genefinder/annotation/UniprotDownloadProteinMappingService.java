package ca.qc.ircm.genefinder.annotation;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.ftp.FtpService;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.rest.RestClientFactory;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progressbar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.apache.commons.net.ftp.FTPClient;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 * Download protein mappings from RefSeq database.
 */
@Component
public class UniprotDownloadProteinMappingService implements DownloadProteinMappingService {
  private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(UniprotDownloadProteinMappingService.class);
  @Inject
  private UniprotConfiguration uniprotConfiguration;
  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private FtpService ftpService;
  @Inject
  private RestClientFactory restClientFactory;
  @Inject
  private ProteinService proteinService;

  protected UniprotDownloadProteinMappingService() {
  }

  protected UniprotDownloadProteinMappingService(UniprotConfiguration uniprotConfiguration,
      NcbiConfiguration ncbiConfiguration, FtpService ftpService,
      RestClientFactory restClientFactory, ProteinService proteinService) {
    this.uniprotConfiguration = uniprotConfiguration;
    this.ncbiConfiguration = ncbiConfiguration;
    this.ftpService = ftpService;
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
    List<ProteinMapping> mappings =
        proteinIds.stream().map(id -> new ProteinMapping(id)).collect(Collectors.toList());
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadMappings(parameters)) {
      downloadMappings(mappings, parameters, progressBar.step(step / 2), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadGeneInfo(parameters)) {
      Path geneInfo = downloadGeneInfo(progressBar.step(step / 2), locale);
      parseGeneInfo(geneInfo, mappings, parameters, progressBar.step(step / 2), resources);
    }
    progressBar.setProgress(1.0);
    return mappings;
  }

  private BufferedReader newBufferedReader(Path file) throws IOException {
    if (file.getFileName().toString().endsWith(".gz")) {
      return new BufferedReader(
          new InputStreamReader(new GZIPInputStream(Files.newInputStream(file)), UTF_8_CHARSET));
    } else {
      return Files.newBufferedReader(file, UTF_8_CHARSET);
    }
  }

  private boolean isDownloadMappings(FindGenesParameters parameters) {
    return parameters.isGeneId() || parameters.isGeneName() || parameters.isGeneSummary()
        || parameters.isGeneSynonyms() || parameters.isSequence()
        || parameters.isProteinMolecularWeight();
  }

  private void downloadMappings(List<ProteinMapping> mappings, FindGenesParameters parameters,
      ProgressBar progressBar, MessageResources resources) throws IOException {
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
      progressBar.setMessage(resources.message("downloadMappings", i,
          Math.min(i + maxIdsPerRequest, proteinIds.size()), proteinIds.size()));
      String ids =
          proteinIds.stream().skip(i).limit(maxIdsPerRequest).collect(Collectors.joining(" "));
      WebTarget targetWithIds = target.queryParam("query", ids);
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(targetWithIds.request().get(InputStream.class), UTF_8_CHARSET))) {
        String line;
        reader.readLine();
        while ((line = reader.readLine()) != null) {
          String[] columns = line.split("\t");
          String id = columns[0];
          ProteinMapping mapping = mappingsById.get(id);
          for (int j = 1; j < columns.length; j++) {
            if (columnConsumers.containsKey(j)) {
              columnConsumers.get(j).accept(mapping, columns[j]);
            }
          }
        }
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

  private Path downloadGeneInfo(ProgressBar progressBar, Locale locale) throws IOException {
    FTPClient client = ftpService.anonymousConnect(ncbiConfiguration.ftp());
    String geneInfo = ncbiConfiguration.geneInfo();
    Path geneInfoFile = ftpService.localFile(geneInfo);
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    progressBar.setMessage(resources.message("download", geneInfo, geneInfoFile));
    ftpService.downloadFile(client, geneInfo, geneInfoFile, progressBar, locale);
    progressBar.setProgress(1.0);
    return geneInfoFile;
  }

  private void parseGeneInfo(Path geneInfo, List<ProteinMapping> mappings,
      FindGenesParameters parameters, ProgressBar progressBar, MessageResources resources)
      throws IOException {
    progressBar.setMessage(resources.message("parsing", geneInfo.getFileName()));
    Map<Long, List<GeneInfo>> mappingsByGene = new HashMap<>();
    mappings.stream().map(mapping -> mapping.getGenes()).filter(genes -> genes != null)
        .flatMap(genes -> genes.stream()).forEach(gene -> {
          if (!mappingsByGene.containsKey(gene.getId())) {
            mappingsByGene.put(gene.getId(), new ArrayList<>());
          }
          mappingsByGene.get(gene.getId()).add(gene);
        });
    try (BufferedReader reader = newBufferedReader(geneInfo)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        String[] columns = line.split("\t", -1);
        Long geneId = Long.parseLong(columns[1]);
        if (mappingsByGene.containsKey(geneId)) {
          String name = columns[2];
          String synonyms = columns[4].equals("-") ? null : columns[4];
          String description = columns[8].equals("-") ? null : columns[8];
          mappingsByGene.get(geneId).forEach(mapping -> {
            if (parameters.isGeneName()) {
              mapping.setSymbol(name);
            }
            if (parameters.isGeneSynonyms() && synonyms != null) {
              mapping.setSynonyms(Arrays.asList(synonyms.split("\\|", -1)));
            }
            if (parameters.isGeneSummary() && description != null) {
              mapping.setDescription(description);
            }
          });
        }
      }
    }
    progressBar.setProgress(1.0);
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
