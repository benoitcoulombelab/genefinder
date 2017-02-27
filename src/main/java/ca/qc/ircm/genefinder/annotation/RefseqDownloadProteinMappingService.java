package ca.qc.ircm.genefinder.annotation;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.ftp.FtpService;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progressbar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

/**
 * Download protein mappings from RefSeq database.
 */
@Component
public class RefseqDownloadProteinMappingService implements DownloadProteinMappingService {
  private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(RefseqDownloadProteinMappingService.class);
  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private FtpService ftpService;
  @Inject
  private ProteinService proteinService;

  protected RefseqDownloadProteinMappingService() {
  }

  protected RefseqDownloadProteinMappingService(NcbiConfiguration ncbiConfiguration,
      FtpService ftpService, ProteinService proteinService) {
    this.ncbiConfiguration = ncbiConfiguration;
    this.ftpService = ftpService;
    this.proteinService = proteinService;
  }

  @Override
  public List<ProteinMapping> downloadProteinMappings(List<String> proteinIds,
      FindGenesParameters parameters, ProgressBar progressBar, Locale locale)
      throws IOException, InterruptedException {
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    int steps = 0;
    steps += isDownloadGene2Accession(parameters) ? 1 : 0;
    steps += isDownloadGeneInfo(parameters) ? 1 : 0;
    steps += isDownloaSequences(parameters) ? 1 : 0;
    double step = 1.0 / steps;
    List<ProteinMapping> mappings =
        proteinIds.stream().map(id -> new ProteinMapping(id)).collect(Collectors.toList());
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadGene2Accession(parameters)) {
      Path gene2Accession = downloadGene2Accession(progressBar.step(step / 2), locale);
      parseGene2Accession(gene2Accession, mappings, progressBar.step(step / 2), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloadGeneInfo(parameters)) {
      Path geneInfo = downloadGeneInfo(progressBar.step(step / 2), locale);
      parseGeneInfo(geneInfo, mappings, parameters, progressBar.step(step / 2), resources);
    }
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    if (isDownloaSequences(parameters)) {
      List<Path> sequences = downloadSequences(progressBar.step(step / 2), locale);
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

  private boolean isDownloadGene2Accession(FindGenesParameters parameters) {
    return parameters.isGeneId() || parameters.isGeneName() || parameters.isGeneSummary()
        || parameters.isGeneSynonyms();
  }

  private Path downloadGene2Accession(ProgressBar progressBar, Locale locale) throws IOException {
    FTPClient client = ftpService.anonymousConnect(ncbiConfiguration.ftp());
    String gene2accession = ncbiConfiguration.gene2accession();
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    Path gene2accessionFile = ftpService.localFile(gene2accession);
    progressBar.setMessage(resources.message("download", gene2accession, gene2accessionFile));
    ftpService.downloadFile(client, gene2accession, gene2accessionFile, progressBar, locale);
    progressBar.setProgress(1.0);
    return gene2accessionFile;
  }

  private void parseGene2Accession(Path gene2Accession, List<ProteinMapping> mappings,
      ProgressBar progressBar, MessageResources resources) throws IOException {
    progressBar.setMessage(resources.message("parsing", gene2Accession.getFileName()));
    Map<String, ProteinMapping> mappingsById = mappings.stream()
        .collect(Collectors.toMap(mapping -> mapping.getProteinId(), mapping -> mapping));
    try (BufferedReader reader = newBufferedReader(gene2Accession)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        String[] columns = line.split("\t", -1);
        String accession = columns[5];
        String gi = columns[6];
        if (mappingsById.containsKey(accession)) {
          addGeneInfo(mappingsById.get(accession), new GeneInfo(Long.parseLong(columns[1])));
        }
        if (mappingsById.containsKey(gi)) {
          addGeneInfo(mappingsById.get(gi), new GeneInfo(Long.parseLong(columns[1])));
        }
      }
    }
    progressBar.setProgress(1.0);
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

  private boolean isDownloaSequences(FindGenesParameters parameters) {
    return parameters.isSequence() || parameters.isProteinMolecularWeight();
  }

  private List<Path> downloadSequences(ProgressBar progressBar, Locale locale) throws IOException {
    FTPClient client = ftpService.anonymousConnect(ncbiConfiguration.ftp());
    String refseqSequences = ncbiConfiguration.refseqSequences();
    Pattern refseqSequencesFilenamePattern = ncbiConfiguration.refseqSequencesFilenamePattern();
    List<String> files = ftpService.walkTree(client, refseqSequences).stream()
        .filter(file -> refseqSequencesFilenamePattern.matcher(file).matches())
        .collect(Collectors.toList());
    List<Path> downloadedFiles = new ArrayList<>();
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    double step = 1.0 / Math.max(files.size(), 1);
    for (String file : files) {
      Path localFile = ftpService.localFile(file);
      progressBar.setMessage(resources.message("download", file, localFile));
      ftpService.downloadFile(client, file, localFile, progressBar.step(step), locale);
      downloadedFiles.add(localFile);
    }
    progressBar.setProgress(1.0);
    return downloadedFiles;
  }

  private void parseSequences(List<Path> sequences, List<ProteinMapping> mappings,
      FindGenesParameters parameters, ProgressBar progressBar, MessageResources resources)
      throws IOException {
    Function<ProteinMapping, Pattern> sequencePatternProvider = mapping -> Pattern
        .compile("^>(.*\\|)?" + (parameters.getProteinDatabase() == REFSEQ_GI ? "gi" : "ref")
            + "\\|" + Pattern.quote(mapping.getProteinId()) + "(\\|.*)?$");
    Map<Pattern, ProteinMapping> sequenceNamePatterns =
        mappings.stream().collect(Collectors.toMap(sequencePatternProvider, mapping -> mapping));
    double step = 1.0 / Math.max(sequences.size(), 1);
    int count = 0;
    for (Path file : sequences) {
      progressBar.setMessage(resources.message("parsing", file.getFileName()));
      try (BufferedReader reader = newBufferedReader(file)) {
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
      progressBar.setProgress(++count * step);
    }
    progressBar.setProgress(1.0);
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
