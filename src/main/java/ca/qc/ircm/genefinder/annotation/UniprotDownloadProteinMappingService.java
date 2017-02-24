package ca.qc.ircm.genefinder.annotation;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.SWISSPROT;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.ftp.FtpService;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.rest.RestClientFactory;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progressbar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.apache.commons.net.ftp.FTPClient;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
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
  private static final Logger logger =
      LoggerFactory.getLogger(UniprotDownloadProteinMappingService.class);
  @Inject
  private UniprotConfiguration uniprotConfiguration;
  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private RestClientFactory restClientFactory;
  @Inject
  private FtpService ftpService;
  @Inject
  private ProteinService proteinService;

  protected UniprotDownloadProteinMappingService() {
  }

  protected UniprotDownloadProteinMappingService(UniprotConfiguration uniprotConfiguration,
      NcbiConfiguration ncbiConfiguration, RestClientFactory restClientFactory,
      FtpService ftpService, ProteinService proteinService) {
    this.uniprotConfiguration = uniprotConfiguration;
    this.ncbiConfiguration = ncbiConfiguration;
    this.restClientFactory = restClientFactory;
    this.ftpService = ftpService;
    this.proteinService = proteinService;
  }

  @Override
  public List<ProteinMapping> downloadProteinMappings(FindGenesParameters parameters,
      ProgressBar progressBar, Locale locale) throws IOException, InterruptedException {
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    int steps = 1;
    steps += isDownloadIdMapping(parameters) ? 1 : 0;
    steps += isDownloadGeneInfo(parameters) ? 1 : 0;
    steps += isDownloaSequences(parameters) ? 1 : 0;
    double step = 1.0 / steps;
    List<String> ids = getIds(parameters, progressBar.step(step), locale);
    List<ProteinMapping> mappings =
        ids.stream().map(id -> new ProteinMapping(id)).collect(Collectors.toList());
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadIdMapping(parameters)) {
      Path idMapping = downloadIdMapping(progressBar.step(step / 2), locale);
      parseIdMapping(idMapping, mappings, progressBar.step(step / 2), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadGeneInfo(parameters)) {
      Path geneInfo = downloadGeneInfo(progressBar.step(step / 2), locale);
      parseGeneInfo(geneInfo, mappings, parameters, progressBar.step(step / 2), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloaSequences(parameters)) {
      List<Path> sequences = downloadSequences(parameters, progressBar.step(step / 2), locale);
      parseSequences(sequences, mappings, parameters, progressBar.step(step / 2), resources);
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

  private List<String> getIds(FindGenesParameters parameters, ProgressBar progressBar,
      Locale locale) throws IOException, InterruptedException {
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    Client client = restClientFactory.createClient();
    WebTarget target = client.target(uniprotConfiguration.search());
    target = target.queryParam("query", "organism:" + parameters.getOrganism().getId()
        + (parameters.getProteinDatabase() == SWISSPROT ? "+AND+reviewed:yes" : ""));
    target = target.queryParam("format", "list");
    Set<String> ids = new LinkedHashSet<>();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(target.request().get(InputStream.class), UTF_8_CHARSET))) {
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      progressBar.setProgress(0.5);
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isEmpty() && !ids.add(line)) {
          logger.warn("id {} already parsed before", line);
        }
      }
      progressBar.setProgress(1.0);
    }
    return new ArrayList<>(ids);
  }

  private boolean isDownloadIdMapping(FindGenesParameters parameters) {
    return parameters.isGeneId() || parameters.isGeneName() || parameters.isGeneSummary()
        || parameters.isGeneSynonyms();
  }

  private Path downloadIdMapping(ProgressBar progressBar, Locale locale) throws IOException {
    FTPClient client = ftpService.anonymousConnect(uniprotConfiguration.ftp());
    String idmapping = uniprotConfiguration.idmapping();
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    Path idmappingFile = ftpService.localFile(idmapping);
    progressBar.setMessage(resources.message("download", idmapping, idmappingFile));
    ftpService.downloadFile(client, idmapping, idmappingFile, progressBar, locale);
    progressBar.setProgress(1.0);
    return idmappingFile;
  }

  private void parseIdMapping(Path idmapping, List<ProteinMapping> mappings,
      ProgressBar progressBar, MessageResources resources) throws IOException {
    progressBar.setMessage(resources.message("parsing", idmapping.getFileName()));
    Map<String, ProteinMapping> mappingsById = mappings.stream()
        .collect(Collectors.toMap(mapping -> mapping.getProteinId(), mapping -> mapping));
    String geneMapping = uniprotConfiguration.geneMapping();
    try (BufferedReader reader = newBufferedReader(idmapping)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        String proteinId = columns[0];
        String mappingType = columns[1];
        if (mappingType.equals(geneMapping) && mappingsById.containsKey(proteinId)) {
          ProteinMapping mapping = mappingsById.get(proteinId);
          if (mapping.getGeneId() != null) {
            ProteinMapping copy = new ProteinMapping(proteinId);
            copy.setGeneId(Long.parseLong(columns[2]));
            mappings.add(copy);
          } else {
            mapping.setGeneId(Long.parseLong(columns[2]));
          }
        }
      }
    }
    progressBar.setProgress(1.0);
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
    Map<Long, List<ProteinMapping>> mappingsByGene = new HashMap<>();
    mappings.forEach(mapping -> {
      if (!mappingsByGene.containsKey(mapping.getGeneId())) {
        mappingsByGene.put(mapping.getGeneId(), new ArrayList<>());
      }
      mappingsByGene.get(mapping.getGeneId()).add(mapping);
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
          String summary = columns[8].equals("-") ? null : columns[8];
          mappingsByGene.get(geneId).forEach(mapping -> {
            if (parameters.isGeneName()) {
              mapping.setGeneName(name);
            }
            if (parameters.isGeneSynonyms()) {
              mapping.setGeneSynonyms(synonyms);
            }
            if (parameters.isGeneSummary()) {
              mapping.setGeneSummary(summary);
            }
          });
        }
      }
    }
    progressBar.setProgress(1.0);
  }

  private boolean isDownloaSequences(FindGenesParameters parameters) {
    return parameters.isSequence() || parameters.isProteinMolecularWeight();
  }

  private List<Path> downloadSequences(FindGenesParameters parameters, ProgressBar progressBar,
      Locale locale) throws IOException {
    FTPClient client = ftpService.anonymousConnect(uniprotConfiguration.ftp());
    List<Path> files = new ArrayList<>();
    String swissprotFasta = uniprotConfiguration.swissprotFasta();
    Path swissprotFastaFile = ftpService.localFile(swissprotFasta);
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    progressBar.setMessage(resources.message("download", swissprotFasta, swissprotFastaFile));
    ftpService.downloadFile(client, swissprotFasta, swissprotFastaFile, progressBar.step(0.5),
        locale);
    files.add(swissprotFastaFile);
    if (parameters.getProteinDatabase() != ProteinDatabase.SWISSPROT) {
      String tremblFasta = uniprotConfiguration.tremblFasta();
      Path tremblFastaFile = ftpService.localFile(tremblFasta);
      progressBar.setMessage(resources.message("download", tremblFasta, tremblFastaFile));
      ftpService.downloadFile(client, tremblFasta, tremblFastaFile, progressBar.step(0.5), locale);
      files.add(tremblFastaFile);
    }
    progressBar.setProgress(1.0);
    return files;
  }

  private void parseSequences(List<Path> sequences, List<ProteinMapping> mappings,
      FindGenesParameters parameters, ProgressBar progressBar, MessageResources resources)
      throws IOException {
    Function<ProteinMapping, Pattern> sequencePatternProvider = mapping -> Pattern
        .compile("^>(.*\\|)?(sp|tr)\\|" + Pattern.quote(mapping.getProteinId()) + "(\\|.*)?$");
    Map<Pattern, ProteinMapping> sequenceNamePatterns =
        mappings.stream().collect(Collectors.toMap(sequencePatternProvider, mapping -> mapping));
    double step = 1.0 / Math.max(sequences.size(), 1);
    int count = 0;
    for (Path file : sequences) {
      progressBar.setMessage(resources.message("parsing", file.getFileName()));
      try (BufferedReader reader = newBufferedReader(file)) {
        String line;
        List<ProteinMapping> mapping = null;
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
                if (mapping == null) {
                  mapping = new ArrayList<>();
                }
                mapping.add(sequenceNamePatterns.get(pattern));
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
      progressBar.setProgress(++count * step);
    }
    progressBar.setProgress(1.0);
  }

  private void setSequence(List<ProteinMapping> mappings, String sequence,
      FindGenesParameters parameters) {
    if (parameters.isSequence()) {
      mappings.stream().forEach(mapping -> mapping.setSequence(sequence));
    }
    if (parameters.isProteinMolecularWeight()) {
      double weight = proteinService.weight(sequence);
      mappings.stream().forEach(mapping -> mapping.setMolecularWeight(weight));
    }
  }
}
