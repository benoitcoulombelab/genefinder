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
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.rest.RestClientFactory;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progressbar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

/**
 * Download protein mappings from RefSeq database.
 */
@Component
public class UniprotDownloadProteinMappingService extends AbstractDownloadProteinMappingService {
  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(UniprotDownloadProteinMappingService.class);
  @Inject
  private UniprotConfiguration uniprotConfiguration;
  @Inject
  private RestClientFactory restClientFactory;
  @Inject
  private ProteinService proteinService;

  protected UniprotDownloadProteinMappingService() {
  }

  protected UniprotDownloadProteinMappingService(UniprotConfiguration uniprotConfiguration,
      NcbiConfiguration ncbiConfiguration, RestClientFactory restClientFactory,
      ProteinService proteinService) {
    super(ncbiConfiguration, restClientFactory);
    this.uniprotConfiguration = uniprotConfiguration;
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
      downloadMappings(mappings, parameters, progressBar.step(step), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadGeneInfo(parameters)) {
      downloadGeneInfo(mappings, parameters, progressBar.step(step), resources);
    }
    progressBar.setProgress(1.0);
    return mappings;
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
    double step = 1.0 / Math.max(proteinIds.size() / maxIdsPerRequest, 1.0);
    for (int i = 0; i < proteinIds.size(); i += maxIdsPerRequest) {
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      progressBar.setMessage(resources.message("downloadMappings", i + 1,
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
        });
      } catch (Exception e) {
        ExceptionUtils.throwExceptionIfMatch(e, IOException.class);
        ExceptionUtils.throwExceptionIfMatch(e, InterruptedException.class);
        throw new IOException(e);
      }
      progressBar.setProgress(i * step);
    }
    progressBar.setProgress(1.0);
  }

  private void addGeneInfo(ProteinMapping mapping, GeneInfo geneInfo) {
    if (mapping.getGenes() == null) {
      mapping.setGenes(new ArrayList<>());
    }
    mapping.getGenes().add(geneInfo);
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
