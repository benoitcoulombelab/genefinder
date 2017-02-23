package ca.qc.ircm.genefinder.annotation;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;

import ca.qc.ircm.genefinder.ApplicationConfiguration;
import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.ftp.FtpService;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.rest.RestClientFactory;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.genefinder.xml.StackSaxHandler;
import ca.qc.ircm.progressbar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Download protein mappings from RefSeq database.
 */
public class RefseqDownloadProteinMappingService implements DownloadProteinMappingService {
  private static class SearchOutput {
    private String webEnv;
    private String queryKey;
    private Integer count;
  }

  private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
  private static final Logger logger =
      LoggerFactory.getLogger(RefseqDownloadProteinMappingService.class);
  @Inject
  private ApplicationConfiguration applicationConfiguration;
  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private RestClientFactory restClientFactory;
  @Inject
  private FtpService ftpService;
  @Inject
  private ProteinService proteinService;

  protected RefseqDownloadProteinMappingService() {
  }

  protected RefseqDownloadProteinMappingService(ApplicationConfiguration applicationConfiguration,
      NcbiConfiguration ncbiConfiguration, RestClientFactory restClientFactory,
      FtpService ftpService, ProteinService proteinService) {
    this.applicationConfiguration = applicationConfiguration;
    this.ncbiConfiguration = ncbiConfiguration;
    this.restClientFactory = restClientFactory;
    this.ftpService = ftpService;
    this.proteinService = proteinService;
  }

  @Override
  public List<ProteinMapping> allProteinMappings(Organism organism, ProgressBar progressBar,
      Locale locale) throws IOException, InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ProteinMapping> downloadProteinMappings(FindGenesParameters parameters,
      ProgressBar progressBar, Locale locale) throws IOException, InterruptedException {
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    int steps = 1;
    steps += isDownloadGene2Accession(parameters) ? 1 : 0;
    steps += isDownloadGeneInfo(parameters) ? 1 : 0;
    steps += isDownloaSequences(parameters) ? 1 : 0;
    double step = 1.0 / steps;
    List<String> ids = getIds(parameters, progressBar.step(step), locale);
    List<ProteinMapping> mappings =
        ids.stream().map(id -> new ProteinMapping(id)).collect(Collectors.toList());
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
      Path sequences = downloadSequences(progressBar.step(step / 2), locale);
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
    SearchOutput searchOutput = search(parameters.getOrganism(), progressBar.step(0.05), resources);
    ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
    List<String> ids = downloadIds(searchOutput, parameters.getProteinDatabase(),
        progressBar.step(0.05), resources);
    return ids;
  }

  private SearchOutput search(Organism organism, ProgressBar progressBar,
      MessageResources resources) throws IOException, InterruptedException {
    progressBar.setMessage(resources.message("search"));
    Client client = restClientFactory.createClient();
    WebTarget target = client.target(ncbiConfiguration.eutils());
    target = target.path("esearch.fcgi");
    target = target.queryParam("db", "protein");
    target = target.queryParam("term", "txid" + organism.getId() + "[Organism] AND refseq[filter]");
    target = target.queryParam("usehistory", "y");
    try (InputStream searchInput = target.request().get(InputStream.class)) {
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      progressBar.setProgress(0.75);
      SearchOutput searchOutput = parseSearchOutput(searchInput);
      progressBar.setProgress(1.0);
      return searchOutput;
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException("Could not read NCBI search content", e);
    }
  }

  private SearchOutput parseSearchOutput(InputStream input)
      throws IOException, ParserConfigurationException, SAXException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    SearchOutput seachOutput = new SearchOutput();
    parser.parse(input, new StackSaxHandler() {
      private StringBuilder builder = new StringBuilder();

      @Override
      protected void startElement(String elementName, Attributes attributes) throws SAXException {
        super.startElement(elementName, attributes);

        if (current("Count") && parent("eSearchResult")) {
          builder.setLength(0);
        } else if (current("QueryKey")) {
          builder.setLength(0);
        } else if (current("WebEnv")) {
          builder.setLength(0);
        }
      }

      @Override
      protected void endElement(String elementName) {
        super.endElement(elementName);

        if (current("Count") && parent("eSearchResult")) {
          seachOutput.count = Integer.parseInt(builder.toString());
        } else if (current("QueryKey")) {
          seachOutput.queryKey = builder.toString();
        } else if (current("WebEnv")) {
          seachOutput.webEnv = builder.toString();
        }
      }

      @Override
      public void characters(char[] ch, int start, int length) throws SAXException {
        if (builder != null) {
          builder.append(ch, start, length);
        }
      }
    });
    return seachOutput;
  }

  private List<String> downloadIds(SearchOutput searchOutput, ProteinDatabase proteinDatabase,
      ProgressBar progressBar, MessageResources resources)
      throws IOException, InterruptedException {
    final int maxIdsPerRequest = ncbiConfiguration.maxIdsPerRequest();
    Client client = restClientFactory.createClient();
    WebTarget target = client.target(ncbiConfiguration.eutils());
    target = target.path("efetch.fcgi");
    target = target.queryParam("db", "protein");
    target = target.queryParam("WebEnv", searchOutput.webEnv);
    target = target.queryParam("query_key", searchOutput.queryKey);
    if (proteinDatabase == REFSEQ_GI) {
      target = target.queryParam("rettype", "gi");
    } else {
      target = target.queryParam("rettype", "acc");
    }
    target = target.queryParam("retmax", maxIdsPerRequest);
    Set<String> ids = new LinkedHashSet<>();
    for (int i = 0; i < searchOutput.count; i += maxIdsPerRequest) {
      progressBar.setMessage(resources.message("downloadIds", i + 1,
          Math.min(i + maxIdsPerRequest, searchOutput.count), searchOutput.count));
      ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
      WebTarget stepTarget = target.queryParam("retstart", i);
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(stepTarget.request().get(InputStream.class), UTF_8_CHARSET))) {
        ExceptionUtils.throwIfInterrupted(resources.message("interrupted"));
        progressBar.setProgress(0.75);
        String line;
        while ((line = reader.readLine()) != null) {
          if (!line.isEmpty() && !ids.add(line)) {
            logger.warn("id {} already parsed before", line);
          }
        }
        progressBar.setProgress(1.0);
      }
    }
    return new ArrayList<>(ids);
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
          mappingsById.get(accession).setGeneId(Long.parseLong(columns[1]));
        }
        if (mappingsById.containsKey(gi)) {
          mappingsById.get(gi).setGeneId(Long.parseLong(columns[1]));
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

  private Path downloadSequences(ProgressBar progressBar, Locale locale) throws IOException {
    FTPClient client = ftpService.anonymousConnect(ncbiConfiguration.ftp());
    String refseqSequences = ncbiConfiguration.refseqSequences();
    Pattern refseqSequencesFilenamePattern = ncbiConfiguration.refseqSequencesFilenamePattern();
    List<String> files = ftpService.walkTree(client, refseqSequences).stream()
        .filter(file -> refseqSequencesFilenamePattern.matcher(file).matches())
        .collect(Collectors.toList());
    Path downloadHome = applicationConfiguration.download();
    MessageResources resources = new MessageResources(DownloadProteinMappingService.class, locale);
    double step = 1.0 / Math.max(files.size(), 1);
    for (String file : files) {
      Path localFile = ftpService.localFile(file);
      progressBar.setMessage(resources.message("download", file, localFile));
      ftpService.downloadFile(client, file, localFile, progressBar.step(step), locale);
    }
    progressBar.setProgress(1.0);
    return downloadHome.resolve(refseqSequences.replaceFirst("^/", ""));
  }

  private void parseSequences(Path sequences, List<ProteinMapping> mappings,
      FindGenesParameters parameters, ProgressBar progressBar, MessageResources resources)
      throws IOException {
    Pattern refseqSequencesFilenamePattern = ncbiConfiguration.refseqSequencesFilenamePattern();
    Function<ProteinMapping, Pattern> sequencePatternProvider = mapping -> Pattern
        .compile("^>(.*\\|)?" + (parameters.getProteinDatabase() == REFSEQ_GI ? "gi" : "ref")
            + "\\|" + Pattern.quote(mapping.getProteinId()) + "(\\|.*)?$");
    Map<Pattern, ProteinMapping> sequenceNamePatterns =
        mappings.stream().collect(Collectors.toMap(sequencePatternProvider, mapping -> mapping));
    List<Path> files = Files.list(sequences)
        .filter(
            file -> refseqSequencesFilenamePattern.matcher(file.getFileName().toString()).matches())
        .collect(Collectors.toList());
    double step = 1.0 / Math.max(files.size(), 1);
    int count = 0;
    for (Path file : files) {
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
