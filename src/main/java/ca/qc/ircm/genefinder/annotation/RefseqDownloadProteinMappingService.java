/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.genefinder.annotation;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
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
public class RefseqDownloadProteinMappingService extends AbstractDownloadProteinMappingService {
  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(RefseqDownloadProteinMappingService.class);
  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private RestClientFactory restClientFactory;
  @Inject
  private ProteinService proteinService;

  protected RefseqDownloadProteinMappingService() {
  }

  protected RefseqDownloadProteinMappingService(NcbiConfiguration ncbiConfiguration,
      RestClientFactory restClientFactory, ProteinService proteinService) {
    super(ncbiConfiguration, restClientFactory);
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
    steps += isDownloadGeneMappings(parameters) ? 1 : 0;
    steps += isDownloadGeneInfo(parameters) ? 1 : 0;
    steps += isDownloaSequences(parameters) ? 1 : 0;
    double step = 1.0 / steps;
    List<ProteinMapping> mappings = proteinIds.stream().distinct().map(id -> new ProteinMapping(id))
        .collect(Collectors.toList());
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadGeneMappings(parameters)) {
      downloadGeneMappings(mappings, parameters, progressBar.step(step), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadGeneInfo(parameters)) {
      downloadGeneInfo(mappings, parameters, progressBar.step(step), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloaSequences(parameters)) {
      downloadSequences(mappings, parameters, progressBar.step(step), resources);
    }
    progressBar.setProgress(1.0);
    return mappings;
  }

  private void downloadGeneMappings(List<ProteinMapping> mappings, FindGenesParameters parameters,
      ProgressBar progressBar, MessageResources resources)
      throws IOException, InterruptedException {
    Map<String, ProteinMapping> mappingsById = mappings.stream()
        .collect(Collectors.toMap(mapping -> mapping.getProteinId(), mapping -> mapping));
    Map<String, String> gis = gis(mappings, parameters, progressBar.step(0.5), resources);
    progressBar = progressBar.step(0.5);
    Client client = restClientFactory.createClient();
    client.register(LoggingFeature.class);
    client.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT,
        LoggingFeature.Verbosity.HEADERS_ONLY);
    WebTarget target = client.target(ncbiConfiguration.eutils());
    target = target.path("elink.fcgi");
    List<String> gisIds = new ArrayList<>(gis.keySet());
    int maxIdsPerRequest = ncbiConfiguration.maxIdsPerRequest();
    double step = 1.0 / Math.max(gisIds.size() / maxIdsPerRequest, 1.0);
    for (int i = 0; i < gisIds.size(); i += maxIdsPerRequest) {
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      progressBar.setMessage(resources.message("downloadGeneMappings", i + 1,
          Math.min(i + maxIdsPerRequest, gisIds.size()), gisIds.size()));
      Form form = new Form();
      form.param("db", "gene");
      form.param("dbfrom", "protein");
      gisIds.stream().skip(i).limit(maxIdsPerRequest).forEach(gi -> form.param("id", gi));
      final Invocation.Builder request = target.request();
      try {
        retry(() -> {
          try (InputStream input = new BufferedInputStream(
              request.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                  InputStream.class))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(input, new StackSaxHandler() {
              private String id;
              private List<String> geneIds = new ArrayList<>();
              private boolean saveCharacter;
              private StringBuilder builder = new StringBuilder();

              @Override
              protected void startElement(String elementName, Attributes attributes)
                  throws SAXException {
                if (current("LinkSet")) {
                  geneIds.clear();
                } else if (current("Id") && parent("IdList")) {
                  builder.delete(0, builder.length());
                  saveCharacter = true;
                } else if (current("Id") && parent("Link")) {
                  builder.delete(0, builder.length());
                  saveCharacter = true;
                }
              }

              @Override
              protected void endElement(String elementName) {
                if (current("LinkSet")) {
                  ProteinMapping mapping = mappingsById.get(gis.get(id));
                  if (mapping != null) {
                    geneIds.stream().forEach(
                        geneId -> addGeneInfo(mapping, new GeneInfo(Long.parseLong(geneId))));
                  }
                } else if (current("Id") && parent("IdList")) {
                  id = builder.toString();
                  saveCharacter = false;
                } else if (current("Id") && parent("Link")) {
                  geneIds.add(builder.toString());
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
            throw new IOException("Could not parse elink response", e);
          }
          return null;
        });
      } catch (Exception e) {
        ExceptionUtils.throwExceptionIfMatch(e, IOException.class);
        ExceptionUtils.throwExceptionIfMatch(e, InterruptedException.class);
        ExceptionUtils.throwExceptionIfMatch(e, RuntimeException.class);
        throw new IOException(e);
      }
      progressBar.setProgress(i * step);
    }
    progressBar.setProgress(1.0);
  }

  private Map<String, String> gis(List<ProteinMapping> mappings, FindGenesParameters parameters,
      ProgressBar progressBar, MessageResources resources)
      throws IOException, InterruptedException {
    if (parameters.getProteinDatabase() == REFSEQ_GI) {
      return mappings.stream().collect(
          Collectors.toMap(mapping -> mapping.getProteinId(), mapping -> mapping.getProteinId()));
    } else {
      Map<String, String> gis = new HashMap<>();
      Client client = restClientFactory.createClient();
      client.register(LoggingFeature.class);
      client.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT,
          LoggingFeature.Verbosity.HEADERS_ONLY);
      WebTarget target = client.target(ncbiConfiguration.eutils());
      target = target.path("esummary.fcgi");
      List<String> proteinIds = mappings.stream().map(mapping -> mapping.getProteinId()).distinct()
          .collect(Collectors.toList());
      int maxIdsPerRequest = ncbiConfiguration.maxIdsPerRequest();
      double step = 1.0 / Math.max(proteinIds.size() / maxIdsPerRequest, 1.0);
      for (int i = 0; i < proteinIds.size(); i += maxIdsPerRequest) {
        ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
        progressBar.setMessage(resources.message("downloadGis", i + 1,
            Math.min(i + maxIdsPerRequest, proteinIds.size()), proteinIds.size()));
        Form form = new Form();
        form.param("db", "protein");
        form.param("id",
            proteinIds.stream().skip(i).limit(maxIdsPerRequest).collect(Collectors.joining(",")));
        final Invocation.Builder request = target.request();
        try {
          retry(() -> {
            try (InputStream input = new BufferedInputStream(
                request.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    InputStream.class))) {
              SAXParserFactory factory = SAXParserFactory.newInstance();
              SAXParser parser = factory.newSAXParser();
              parser.parse(input, new StackSaxHandler() {
                private String id;
                private String accession;
                private boolean saveCharacter;
                private StringBuilder builder = new StringBuilder();

                @Override
                protected void startElement(String elementName, Attributes attributes)
                    throws SAXException {
                  if (current("DocSum")) {
                  } else if (current("Id")) {
                    builder.delete(0, builder.length());
                    saveCharacter = true;
                  } else if (current("Item") && attribute("Name", "AccessionVersion")) {
                    builder.delete(0, builder.length());
                    saveCharacter = true;
                  }
                }

                @Override
                protected void endElement(String elementName) {
                  if (current("DocSum")) {
                    gis.put(id, accession);
                  } else if (current("Id")) {
                    id = builder.toString();
                    saveCharacter = false;
                  } else if (current("Item") && attribute("Name", "AccessionVersion")) {
                    accession = builder.toString();
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
          });
        } catch (Exception e) {
          ExceptionUtils.throwExceptionIfMatch(e, IOException.class);
          ExceptionUtils.throwExceptionIfMatch(e, InterruptedException.class);
          ExceptionUtils.throwExceptionIfMatch(e, RuntimeException.class);
          throw new IOException(e);
        }
        progressBar.setProgress(i * step);
      }
      progressBar.setProgress(1.0);
      return gis;
    }
  }

  private boolean isDownloadGeneMappings(FindGenesParameters parameters) {
    return parameters.isGeneId() || parameters.isGeneName() || parameters.isGeneSummary()
        || parameters.isGeneSynonyms();
  }

  private void addGeneInfo(ProteinMapping mapping, GeneInfo geneInfo) {
    if (mapping.getGenes() == null) {
      mapping.setGenes(new ArrayList<>());
    }
    mapping.getGenes().add(geneInfo);
  }

  private boolean isDownloaSequences(FindGenesParameters parameters) {
    return parameters.isSequence() || parameters.isProteinMolecularWeight();
  }

  private void downloadSequences(List<ProteinMapping> mappings, FindGenesParameters parameters,
      ProgressBar progressBar, MessageResources resources)
      throws IOException, InterruptedException {
    Map<String, ProteinMapping> mappingsById = mappings.stream()
        .collect(Collectors.toMap(mapping -> mapping.getProteinId(), mapping -> mapping));
    Map<String, String> accessions =
        accessions(mappings, parameters, progressBar.step(0.5), resources);
    Function<String, Pattern> sequencePatternProvider =
        accession -> Pattern.compile("^>(.*\\|)?" + Pattern.quote(accession) + "([\\| ].*)?$");
    Map<Pattern, ProteinMapping> sequenceNamePatterns =
        accessions.keySet().stream().collect(Collectors.toMap(sequencePatternProvider,
            accession -> mappingsById.get(accessions.get(accession))));
    progressBar = progressBar.step(0.5);
    Client client = restClientFactory.createClient();
    client.register(LoggingFeature.class);
    client.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT,
        LoggingFeature.Verbosity.HEADERS_ONLY);
    WebTarget target = client.target(ncbiConfiguration.eutils());
    target = target.path("efetch.fcgi");
    List<String> proteinIds = accessions.keySet().stream().collect(Collectors.toList());
    int maxIdsPerRequest = ncbiConfiguration.maxIdsPerRequest();
    double step = 1.0 / Math.max(proteinIds.size() / maxIdsPerRequest, 1.0);
    for (int i = 0; i < proteinIds.size(); i += maxIdsPerRequest) {
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      progressBar.setMessage(resources.message("downloadSequences", i + 1,
          Math.min(i + maxIdsPerRequest, proteinIds.size()), proteinIds.size()));
      Form form = new Form();
      form.param("db", "protein");
      form.param("rettype", "fasta");
      List<String> currentProteinIds =
          proteinIds.stream().skip(i).limit(maxIdsPerRequest).collect(Collectors.toList());
      currentProteinIds.forEach(id -> form.param("id", id));
      final Invocation.Builder request = target.request();
      try {
        retry(() -> {
          try (BufferedReader reader = new BufferedReader(new InputStreamReader(
              request.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                  InputStream.class),
              UTF_8_CHARSET))) {
            String line;
            ProteinMapping mapping = null;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
              if (line.startsWith(">")) {
                if (mapping != null) {
                  setSequence(mapping, builder.toString(), parameters);
                }
                mapping = null;
                builder.delete(0, builder.length());
                for (Pattern pattern : sequenceNamePatterns.keySet()) {
                  if (pattern.matcher(line).matches()) {
                    mapping = sequenceNamePatterns.get(pattern);
                  }
                }
              } else if (mapping != null) {
                builder.append(line);
              }
            }
            if (mapping != null) {
              setSequence(mapping, builder.toString(), parameters);
            }
          }
          return null;
        });
      } catch (Exception e) {
        ExceptionUtils.throwExceptionIfMatch(e, IOException.class);
        ExceptionUtils.throwExceptionIfMatch(e, InterruptedException.class);
        ExceptionUtils.throwExceptionIfMatch(e, RuntimeException.class);
        throw new IOException(e);
      }
      progressBar.setProgress(i * step);
    }
    progressBar.setProgress(1.0);
  }

  private Map<String, String> accessions(List<ProteinMapping> mappings,
      FindGenesParameters parameters, ProgressBar progressBar, MessageResources resources)
      throws IOException, InterruptedException {
    if (parameters.getProteinDatabase() == REFSEQ_GI) {
      Map<String, String> accessions = new HashMap<>();
      Client client = restClientFactory.createClient();
      client.register(LoggingFeature.class);
      client.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT,
          LoggingFeature.Verbosity.HEADERS_ONLY);
      WebTarget target = client.target(ncbiConfiguration.eutils());
      target = target.path("esummary.fcgi");
      List<String> proteinIds = mappings.stream().map(mapping -> mapping.getProteinId()).distinct()
          .collect(Collectors.toList());
      int maxIdsPerRequest = ncbiConfiguration.maxIdsPerRequest();
      double step = 1.0 / Math.max(proteinIds.size() / maxIdsPerRequest, 1.0);
      for (int i = 0; i < proteinIds.size(); i += maxIdsPerRequest) {
        ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
        progressBar.setMessage(resources.message("downloadAccessions", i + 1,
            Math.min(i + maxIdsPerRequest, proteinIds.size()), proteinIds.size()));
        Form form = new Form();
        form.param("db", "protein");
        form.param("id",
            proteinIds.stream().skip(i).limit(maxIdsPerRequest).collect(Collectors.joining(",")));
        final Invocation.Builder request = target.request();
        try {
          retry(() -> {
            try (InputStream input = new BufferedInputStream(
                request.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    InputStream.class))) {
              SAXParserFactory factory = SAXParserFactory.newInstance();
              SAXParser parser = factory.newSAXParser();
              parser.parse(input, new StackSaxHandler() {
                private String id;
                private String accession;
                private boolean saveCharacter;
                private StringBuilder builder = new StringBuilder();

                @Override
                protected void startElement(String elementName, Attributes attributes)
                    throws SAXException {
                  if (current("DocSum")) {
                  } else if (current("Id")) {
                    builder.delete(0, builder.length());
                    saveCharacter = true;
                  } else if (current("Item") && attribute("Name", "AccessionVersion")) {
                    builder.delete(0, builder.length());
                    saveCharacter = true;
                  }
                }

                @Override
                protected void endElement(String elementName) {
                  if (current("DocSum")) {
                    accessions.put(accession, id);
                  } else if (current("Id")) {
                    id = builder.toString();
                    saveCharacter = false;
                  } else if (current("Item") && attribute("Name", "AccessionVersion")) {
                    accession = builder.toString();
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
          });
        } catch (Exception e) {
          ExceptionUtils.throwExceptionIfMatch(e, IOException.class);
          ExceptionUtils.throwExceptionIfMatch(e, InterruptedException.class);
          ExceptionUtils.throwExceptionIfMatch(e, RuntimeException.class);
          throw new IOException(e);
        }
        progressBar.setProgress(i * step);
      }
      progressBar.setProgress(1.0);
      return accessions;
    } else {
      return mappings.stream().collect(
          Collectors.toMap(mapping -> mapping.getProteinId(), mapping -> mapping.getProteinId()));
    }
  }

  private void setSequence(ProteinMapping mapping, String sequence,
      FindGenesParameters parameters) {
    if (parameters.isSequence()) {
      mapping.setSequence(sequence);
    }
    if (parameters.isProteinMolecularWeight()) {
      mapping.setMolecularWeight(proteinService.weight(sequence));
    }
  }
}
