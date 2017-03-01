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

import ca.qc.ircm.genefinder.data.FindGenesParameters;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
 * Common function of DownloadProteinMappingService implementations.
 */
@Component
public abstract class AbstractDownloadProteinMappingService
    implements DownloadProteinMappingService {
  protected static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
  protected static final int MAX_RETRIES = 5;
  protected static final long RETRY_SLEEP_TIME = 2000;
  private static final Logger logger =
      LoggerFactory.getLogger(AbstractDownloadProteinMappingService.class);

  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private RestClientFactory restClientFactory;

  protected AbstractDownloadProteinMappingService() {
  }

  protected AbstractDownloadProteinMappingService(NcbiConfiguration ncbiConfiguration,
      RestClientFactory restClientFactory) {
    this.ncbiConfiguration = ncbiConfiguration;
    this.restClientFactory = restClientFactory;
  }

  protected <R> R retry(Callable<R> callable) throws Exception {
    return retry(callable, MAX_RETRIES, RETRY_SLEEP_TIME);
  }

  protected <R> R retry(Callable<R> callable, int maxRetries, long retrySleepTime)
      throws Exception {
    int tries = 0;
    Exception firstException = null;
    while (true) {
      try {
        return callable.call();
      } catch (Exception e) {
        if (e instanceof InterruptedException) {
          throw e;
        }
        logger.trace("Caught {} exception {}, retry in {} millis", e.getClass(), e.getMessage(),
            retrySleepTime);
        if (firstException == null) {
          firstException = e;
        }
        if (tries++ == maxRetries) {
          throw firstException;
        }
        Thread.sleep(retrySleepTime);
      }
    }
  }

  protected boolean isDownloadGeneInfo(FindGenesParameters parameters) {
    return parameters.isGeneName() || parameters.isGeneSummary() || parameters.isGeneSynonyms();
  }

  protected void downloadGeneInfo(List<ProteinMapping> mappings, FindGenesParameters parameters,
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
    Client client = restClientFactory.createClient();
    client.register(LoggingFeature.class);
    client.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT,
        LoggingFeature.Verbosity.HEADERS_ONLY);
    WebTarget target = client.target(ncbiConfiguration.eutils());
    target = target.path("esummary.fcgi");
    List<Long> geneIds = new ArrayList<>(genesById.keySet());
    int maxIdsPerRequest = ncbiConfiguration.maxIdsPerRequest();
    double step = 1.0 / Math.max(geneIds.size() / maxIdsPerRequest, 1.0);
    for (int i = 0; i < geneIds.size(); i += maxIdsPerRequest) {
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      progressBar.setMessage(resources.message("downloadGenes", i + 1,
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
                if (genesById.containsKey(id)) {
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
  }
}
