package ca.qc.ircm.genefinder.annotation;

import ca.qc.ircm.genefinder.ApplicationProperties;
import ca.qc.ircm.genefinder.net.FtpClientFactory;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progress_bar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;

/**
 * Services for downloading {@link TargetMapping target mappings}.
 */
public class DownloadProteinMappingServiceBean implements ProteinMappingService {
  private static final Pattern UNIPROT_PROTEIN_ID_PATTERN =
      Pattern.compile("^(?:\\w{2}\\|)?([OPQ][0-9][A-Z0-9]{3}[0-9])(?:-\\d+)?(?:\\|.*)?"
          + "|^(?:\\w{2}\\|)?([A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(?:-\\d+)?(?:\\|.*)?");
  private static final String USERNAME = "anonymous";
  private static final String PASSWORD = "";
  private static final String UNIPROT_HOST = "ftp.uniprot.org";
  private static final String UNIPROT_FOLDER =
      "/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes";
  private static final String NCBI_HOST = "ftp.ncbi.nlm.nih.gov";
  private static final String NCBI_GENE_INFO = "/gene/DATA/GENE_INFO/All_Data.gene_info.gz";
  private static final String UNIPROT_FILE_PATTERN = "UP\\d+_(\\d+)[\\._].+";
  private static final String GI_MAPPING_TYPE = "GI";
  private static final String REFSEQ_MAPPING_TYPE = "RefSeq";
  private static final String ORGANISM_ID_MAPPING_TYPE = "NCBI_TaxID";
  private static final String GENE_ID_MAPPING_TYPE = "GeneID";
  private static final String MAPPINGS_FILENAME = "mappings.txt";
  private static final String INTERRUPTED_MESSAGE = "Interrupted database mapping update";
  private static final int CHUNK = 1000000;
  private static final Period FILE_UPDATE_INTERVAL = Period.ofDays(7);
  private static final Logger logger =
      LoggerFactory.getLogger(DownloadProteinMappingServiceBean.class);

  private static class OrganismFiles {
    private int organism;
    private Path geneInfoFile;
    private Path idMappingFile;
    private List<Path> sequenceFiles = new ArrayList<>();
  }

  @Inject
  private FtpClientFactory ftpClientFactory;
  @Inject
  private IdMappingParser idMappingParser;
  @Inject
  private GeneInfoParser geneInfoMappingParser;
  @Inject
  private ApplicationProperties applicationProperties;

  protected DownloadProteinMappingServiceBean() {
  }

  /**
   * Creates instance of DownloadTargetMappingServiceBean.
   *
   * @param ftpClientFactory
   *          FTP client factory
   * @param idMappingParser
   *          id mapping parser
   * @param geneInfoMappingParser
   *          gene info mapping parser
   * @param applicationProperties
   *          application properties
   */
  public DownloadProteinMappingServiceBean(FtpClientFactory ftpClientFactory,
      IdMappingParser idMappingParser, GeneInfoParser geneInfoMappingParser,
      ApplicationProperties applicationProperties) {
    this.ftpClientFactory = ftpClientFactory;
    this.idMappingParser = idMappingParser;
    this.geneInfoMappingParser = geneInfoMappingParser;
    this.applicationProperties = applicationProperties;
  }

  @Override
  public List<ProteinMapping> allProteinMappings(Organism organism, ProgressBar progressBar,
      Locale locale) throws IOException, InterruptedException {
    Set<Integer> includeOrganisms = new HashSet<>();
    includeOrganisms.add(organism.getId());
    return downloadMappings(locale, progressBar, includeOrganisms);
  }

  private List<ProteinMapping> downloadMappings(Locale locale, ProgressBar progressBar,
      Set<Integer> includeOrganisms) throws InterruptedException, IOException {
    MessageResources messageResource =
        new MessageResources(DownloadProteinMappingServiceBean.class, locale);
    progressBar.setMessage(messageResource.message("geneInfo.download"));
    final Path geneInfoFile = downloadGeneInfo(applicationProperties.getAnnotationsFolder());
    Map<Integer, Path> geneInfoFiles = splitGeneInfo(geneInfoFile);
    progressBar.setProgress(0.1);
    progressBar.setMessage(messageResource.message("idMapping.download"));
    final List<Path> idMappingsFiles = downloadIdMappings(
        applicationProperties.getAnnotationsFolder(), progressBar.step(0.1), includeOrganisms);
    progressBar.setProgress(0.2);
    final List<Path> sequencesFiles = downloadSequences(
        applicationProperties.getAnnotationsFolder(), progressBar.step(0.1), includeOrganisms);
    progressBar.setProgress(0.3);

    progressBar.setMessage(messageResource.message("database"));
    Map<Integer, OrganismFiles> organismFilesMap = new HashMap<>();
    for (Path idMappingsFile : idMappingsFiles) {
      Integer organism = parseOrganismFromFilename(idMappingsFile);
      if (organism != null) {
        OrganismFiles organismFiles = new OrganismFiles();
        organismFiles.organism = organism;
        organismFiles.geneInfoFile = geneInfoFiles.get(organism);
        organismFiles.idMappingFile = idMappingsFile;
        if (organismFiles.geneInfoFile == null) {
          logger.debug("Could not find gene info for organism {}", organism);
        }
        organismFilesMap.put(organism, organismFiles);
      } else {
        logger.warn("Could not find organism for file {}", idMappingsFile.getFileName());
      }
    }
    for (Path sequencesFile : sequencesFiles) {
      Integer organism = parseOrganismFromFilename(sequencesFile);
      if (organism != null) {
        OrganismFiles organismFiles = organismFilesMap.get(organism);
        if (organismFiles == null) {
          logger.warn("Could not find id mapping file for sequence file {}",
              sequencesFile.getFileName());
        } else {
          organismFiles.sequenceFiles.add(sequencesFile);
        }
      } else {
        logger.warn("Could not find organism for file {}", sequencesFile.getFileName());
      }
    }

    List<OrganismFiles> organismFilesList = new ArrayList<>(organismFilesMap.values().size());
    for (OrganismFiles organismFiles : organismFilesMap.values()) {
      if (organismFiles.geneInfoFile != null) {
        // Skip organisms with no genes.
        organismFilesList.add(organismFiles);
      }
    }
    Path mappingsPath = applicationProperties.getAnnotationsFolder().resolve(MAPPINGS_FILENAME);
    double step = 0.5 / Math.max(organismFilesList.size(), 1);
    List<ProteinMapping> returnedMappings = new ArrayList<>();
    try (Writer writer = Files.newBufferedWriter(mappingsPath, Charset.forName("UTF-8"))) {
      for (OrganismFiles organismFiles : organismFilesList) {
        List<ProteinMapping> mappings = organismMappings(organismFiles);
        returnedMappings.addAll(mappings);
        progressBar.step(step).setProgress(1.0);
      }
    }
    progressBar.step(step).setProgress(1.0);
    return returnedMappings;
  }

  private Integer parseOrganismFromFilename(Path file) {
    Pattern uniProtFilePattern =
        Pattern.compile(DownloadProteinMappingServiceBean.UNIPROT_FILE_PATTERN);
    Matcher matcher = uniProtFilePattern.matcher(file.getFileName().toString());
    if (matcher.matches()) {
      return Integer.valueOf(matcher.group(1));
    } else {
      return null;
    }
  }

  private FTPClient connect(String host) throws IOException {
    FTPClient client = ftpClientFactory.create();
    client.connect(host);
    if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
      throw new IOException("Could not connect to server " + UNIPROT_HOST);
    }
    client.enterLocalPassiveMode();
    client.setControlKeepAliveTimeout(300);
    return client;
  }

  private void login(FTPClient client) throws IOException {
    if (!client.login(USERNAME, PASSWORD)) {
      throw new IOException("Could not login on server " + client.getRemoteAddress());
    }
  }

  private Path downloadGeneInfo(Path folder) throws IOException, InterruptedException {
    Path destination = folder.resolve(Paths.get(NCBI_GENE_INFO).getFileName());
    if (Files.exists(destination) && wasFileModifiedRecently(destination)) {
      // File already downloaded.
      return destination;
    }

    FTPClient client = connect(NCBI_HOST);
    try {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      login(client);
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      logger.trace("Download {} to {}", NCBI_GENE_INFO, folder);
      try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(destination))) {
        download(client, NCBI_GENE_INFO, output);
      }
      return destination;
    } catch (IOException | InterruptedException e) {
      Files.deleteIfExists(destination);
      throw e;
    } finally {
      if (client.isConnected()) {
        client.disconnect();
      }
    }
  }

  private Map<Integer, Path> splitGeneInfo(final Path path)
      throws IOException, InterruptedException {
    final Set<Integer> organisms = new HashSet<>();
    class OrganismConsumer implements Consumer<GeneInfo> {
      @Override
      public void accept(GeneInfo geneInfo) {
        try {
          ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        organisms.add(geneInfo.getOrganismId());
      }
    }

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(path)),
            Charset.forName("UTF-8")))) {
      try {
        geneInfoMappingParser.parse(reader, new OrganismConsumer());
      } catch (RuntimeException e) {
        ExceptionUtils.throwExceptionIfMatch(e.getCause(), IOException.class);
        ExceptionUtils.throwExceptionIfMatch(e.getCause(), InterruptedException.class);
        throw e;
      }
    }

    final Map<Integer, Path> geneInfoFiles = new HashMap<>();
    for (Integer organism : organisms) {
      Path organismPath = path.getParent().resolve(organism + ".gene_info.gz");
      try (OutputStream output = Files.newOutputStream(organismPath)) {
        // Empty existing files.
      }
      geneInfoFiles.put(organism, organismPath);
    }

    class GeneInfoConsumer implements BiConsumer<GeneInfo, String> {
      Map<Integer, List<String>> geneInfos = new HashMap<>();
      private int count = 0;

      @Override
      public void accept(GeneInfo geneInfo, String line) {
        try {
          ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        if (!geneInfos.containsKey(geneInfo.getOrganismId())) {
          geneInfos.put(geneInfo.getOrganismId(), new ArrayList<String>());
        }
        geneInfos.get(geneInfo.getOrganismId()).add(line);
        count++;
        if (count >= CHUNK) {
          writeChunk();
        }
      }

      private void writeChunk() {
        for (Map.Entry<Integer, List<String>> entry : geneInfos.entrySet()) {
          int organism = entry.getKey();
          try (Writer writer = new BufferedWriter(new OutputStreamWriter(
              new GZIPOutputStream(Files.newOutputStream(geneInfoFiles.get(organism),
                  StandardOpenOption.WRITE, StandardOpenOption.APPEND)),
              "UTF-8"))) {
            for (String line : entry.getValue()) {
              writer.write(line);
              writer.write("\n");
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        geneInfos.clear();
        count = 0;
      }
    }

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(path)),
            Charset.forName("UTF-8")))) {
      GeneInfoConsumer consumer = new GeneInfoConsumer();
      try {
        geneInfoMappingParser.parse(reader, consumer);
        consumer.writeChunk();
      } catch (RuntimeException e) {
        ExceptionUtils.throwExceptionIfMatch(e.getCause(), IOException.class);
        ExceptionUtils.throwExceptionIfMatch(e.getCause(), InterruptedException.class);
        throw e;
      }
    }
    return geneInfoFiles;
  }

  private List<Path> downloadIdMappings(Path folder, ProgressBar progressBar,
      Set<Integer> includeOrganisms) throws IOException, InterruptedException {
    FTPClient client = connect(UNIPROT_HOST);
    try {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      login(client);
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      List<String> filenames = listIdMappingFilenames(client);
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      List<Path> paths = new ArrayList<>();
      double step = 1.0 / Math.max(filenames.size(), 1);
      int count = 0;
      for (String filename : filenames) {
        Integer organism = parseOrganismFromFilename(Paths.get(filename));
        if (includeOrganisms != null && !includeOrganisms.contains(organism)) {
          continue;
        }
        Path destination = folder.resolve(Paths.get(filename).getFileName());
        logger.trace("Download {} id mapping file to {}", filename, destination);
        paths.add(destination);
        if (Files.exists(destination) && wasFileModifiedRecently(destination)) {
          // File already downloaded.
          continue;
        }
        try (OutputStream mergedOutput =
            new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(destination)))) {
          ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
          ByteArrayOutputStream output = new ByteArrayOutputStream();
          download(client, filename, output);
          ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
          try (InputStream input =
              new GZIPInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            IOUtils.copyLarge(input, mergedOutput);
          }
          progressBar.setProgress(++count * step);
        } catch (IOException | InterruptedException e) {
          Files.deleteIfExists(destination);
          throw e;
        }
      }
      return paths;
    } finally {
      if (client.isConnected()) {
        client.disconnect();
      }
    }
  }

  private List<Path> downloadSequences(Path folder, ProgressBar progressBar,
      Set<Integer> includeOrganisms) throws IOException, InterruptedException {
    FTPClient client = connect(UNIPROT_HOST);
    try {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      login(client);
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      List<String> filenames = listFastaFilenames(client);
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      List<Path> paths = new ArrayList<>();
      double step = 1.0 / Math.max(filenames.size(), 1);
      int count = 0;
      for (String filename : filenames) {
        Integer organism = parseOrganismFromFilename(Paths.get(filename));
        if (includeOrganisms != null && !includeOrganisms.contains(organism)) {
          continue;
        }
        Path destination = folder.resolve(Paths.get(filename).getFileName());
        logger.trace("Download {} sequence file to {}", filename, destination);
        paths.add(destination);
        if (Files.exists(destination) && wasFileModifiedRecently(destination)) {
          // File already downloaded.
          continue;
        }
        try (OutputStream mergedOutput =
            new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(destination)))) {
          ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
          ByteArrayOutputStream output = new ByteArrayOutputStream();
          download(client, filename, output);
          ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
          try (InputStream input =
              new GZIPInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            IOUtils.copyLarge(input, mergedOutput);
          }
          progressBar.setProgress(++count * step);
        } catch (IOException | InterruptedException e) {
          Files.deleteIfExists(destination);
          throw e;
        }
      }
      return paths;
    } finally {
      if (client.isConnected()) {
        client.disconnect();
      }
    }
  }

  private boolean wasFileModifiedRecently(Path path) throws IOException {
    if (Files.exists(path)) {
      Instant today = Instant.now();
      Instant fileModifiedTime = Files.getLastModifiedTime(path).toInstant();
      return !fileModifiedTime.plus(FILE_UPDATE_INTERVAL).isBefore(today);
    }
    return false;
  }

  private List<ProteinMapping> organismMappings(OrganismFiles organismFiles)
      throws IOException, InterruptedException {
    final Integer organism = organismFiles.organism;
    Map<Long, GeneInfo> geneInfos = geneInfos(organismFiles.geneInfoFile);
    Map<Integer, Map<String, List<ProteinMapping>>> mappedMappings =
        mappings(organismFiles.idMappingFile);
    Set<String> proteinIds = new HashSet<>();
    for (Map<String, List<ProteinMapping>> organismsMappings : mappedMappings.values()) {
      proteinIds.addAll(organismsMappings.keySet());
    }
    Map<String, String> sequences = new HashMap<>();
    for (Path sequencesFile : organismFiles.sequenceFiles) {
      sequences.putAll(sequences(proteinIds, sequencesFile));
    }
    for (Map<String, List<ProteinMapping>> organismsMappings : mappedMappings.values()) {
      for (Map.Entry<String, List<ProteinMapping>> entry : organismsMappings.entrySet()) {
        String proteinId = entry.getKey();
        for (ProteinMapping mapping : entry.getValue()) {
          mapping.setSequence(sequences.get(proteinId));
        }
      }
    }
    final List<ProteinMapping> mappings = new ArrayList<>();
    Set<String> otherIds = new HashSet<>();
    for (List<ProteinMapping> value : mappedMappings.get(organism).values()) {
      for (ProteinMapping targetMapping : value) {
        String proteinId = targetMapping.getProteinId();
        if (otherIds.add(proteinId)) {
          mappings.add(targetMapping);
        }
      }
    }
    for (ProteinMapping mapping : mappings) {
      if (mapping.getGeneId() != null && geneInfos.get(mapping.getGeneId()) != null) {
        GeneInfo geneInfo = geneInfos.get(mapping.getGeneId());
        mapping.setGeneName(geneInfo.getSymbol());
        mapping.setGeneSummary(geneInfo.getDescription());
        mapping.setGeneSynonyms(String.join("|", geneInfo.getSynonyms()));
      }
    }
    return mappings;
  }

  private Map<Long, GeneInfo> geneInfos(Path path) throws InterruptedException, IOException {
    if (path == null) {
      return new HashMap<>();
    }

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(path)),
            Charset.forName("UTF-8")))) {
      try {
        List<GeneInfo> rawGeneInfos = geneInfoMappingParser.parse(reader);
        Map<Long, GeneInfo> geneInfos = new HashMap<>();
        for (GeneInfo geneInfo : rawGeneInfos) {
          geneInfos.put(geneInfo.getId(), geneInfo);
        }
        return geneInfos;
      } catch (RuntimeException e) {
        ExceptionUtils.throwExceptionIfMatch(e.getCause(), InterruptedException.class);
        throw e;
      }
    }
  }

  private Map<Integer, Map<String, List<ProteinMapping>>> mappings(Path path)
      throws InterruptedException, IOException {
    class IdMappingConsumer implements Consumer<IdMapping> {
      private Map<Integer, Map<String, List<ProteinMapping>>> mappings = new HashMap<>();

      @Override
      public void accept(IdMapping idMapping) {
        try {
          ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        Integer organism;
        if (idMapping.getMappings().get(ORGANISM_ID_MAPPING_TYPE) != null
            && !idMapping.getMappings().get(ORGANISM_ID_MAPPING_TYPE).isEmpty()) {
          organism = Integer.valueOf(idMapping.getMappings().get(ORGANISM_ID_MAPPING_TYPE).get(0));
        } else {
          // Skip, no organism.
          return;
        }
        if (!mappings.containsKey(organism)) {
          mappings.put(organism, new HashMap<String, List<ProteinMapping>>());
        }
        Long geneId = null;
        if (idMapping.getMappings().get(GENE_ID_MAPPING_TYPE) != null
            && !idMapping.getMappings().get(GENE_ID_MAPPING_TYPE).isEmpty()) {
          geneId = minLong(idMapping.getMappings().get(GENE_ID_MAPPING_TYPE));
        }
        List<String> otherProteinIds = new ArrayList<>();
        List<String> gis = idMapping.getMappings().get(GI_MAPPING_TYPE);
        if (gis != null) {
          otherProteinIds.addAll(gis);
        }
        List<String> refseqs = idMapping.getMappings().get(REFSEQ_MAPPING_TYPE);
        if (refseqs != null) {
          otherProteinIds.addAll(refseqs);
        }
        String proteinId = idMapping.getProtein();
        mappings.get(organism).put(proteinId, new ArrayList<ProteinMapping>());
        ProteinMapping mapping = new ProteinMapping();
        mapping.setTaxonomyId(organism);
        mapping.setProteinId(proteinId);
        mapping.setGeneId(geneId);
        mappings.get(organism).get(proteinId).add(mapping);
        for (String otherProteinId : otherProteinIds) {
          mapping = new ProteinMapping();
          mapping.setTaxonomyId(organism);
          mapping.setProteinId(otherProteinId);
          mapping.setGeneId(geneId);
          mappings.get(organism).get(proteinId).add(mapping);
        }
      }
    }

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(path)),
            Charset.forName("UTF-8")))) {
      IdMappingConsumer consumer = new IdMappingConsumer();
      try {
        idMappingParser.parse(reader, consumer);
      } catch (RuntimeException e) {
        ExceptionUtils.throwExceptionIfMatch(e.getCause(), InterruptedException.class);
        throw e;
      }
      return consumer.mappings;
    }
  }

  private Map<String, String> sequences(Set<String> proteinIds, Path path)
      throws InterruptedException, IOException {
    Map<String, String> sequences = new HashMap<>();
    try (FastaReader reader = new FastaReader(new BufferedReader(new InputStreamReader(
        new GZIPInputStream(Files.newInputStream(path)), Charset.forName("UTF-8"))))) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      Sequence sequence;
      while ((sequence = reader.nextSequence()) != null) {
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        String proteinId = proteinId(sequence.getName());
        if (proteinIds.contains(proteinId)) {
          sequences.put(proteinId, sequence.getSequence());
        }
      }
    }
    return sequences;
  }

  private List<String> listIdMappingFilenames(FTPClient client)
      throws InterruptedException, IOException {
    List<String> files = listAllFilenames(client, UNIPROT_FOLDER);
    List<String> fastas = new ArrayList<>();
    for (String file : files) {
      if (file.endsWith(".idmapping.gz")) {
        fastas.add(file);
      }
    }
    return fastas;
  }

  private List<String> listFastaFilenames(FTPClient client)
      throws InterruptedException, IOException {
    List<String> files = listAllFilenames(client, UNIPROT_FOLDER);
    List<String> fastas = new ArrayList<>();
    for (String file : files) {
      if (file.endsWith(".fasta.gz") && !file.endsWith("_DNA.fasta.gz")) {
        fastas.add(file);
      }
    }
    return fastas;
  }

  private List<String> listAllFilenames(FTPClient client, String directory)
      throws InterruptedException, IOException {
    List<String> files = new ArrayList<>();
    if (directory != null) {
      if (!client.changeWorkingDirectory(directory)) {
        return new ArrayList<>();
      }
    }
    for (FTPFile file : client.listFiles()) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      String filename = directory + "/" + file.getName();
      if (file.isDirectory()) {
        files.addAll(listAllFilenames(client, filename));
      } else {
        files.add(filename);
      }
    }
    return files;
  }

  private void download(FTPClient client, String path, OutputStream output)
      throws InterruptedException, IOException {
    Thread watchdog = abortOnInterrupt(client);
    try {
      client.setFileType(FTP.BINARY_FILE_TYPE);
      client.retrieveFile(path, output);
    } catch (IOException e) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      throw e;
    } finally {
      watchdog.interrupt();
    }
  }

  private Thread abortOnInterrupt(final FTPClient client) {
    final Thread currentThread = Thread.currentThread();
    Thread watchdog = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            if (currentThread.isInterrupted()) {
              logger.debug("Download target mapping thread interrupted, abording transfer");
              client.abor();
              client.disconnect();
            }
            Thread.sleep(100);
          } catch (InterruptedException | IOException e) {
            return;
          }
        }
      }
    });
    watchdog.start();
    return watchdog;
  }

  private long minLong(List<String> longsAsString) {
    long min = Long.parseLong(longsAsString.get(0));
    for (String longAsString : longsAsString) {
      min = Math.min(min, Long.parseLong(longAsString));
    }
    return min;
  }

  private String proteinId(String sequenceName) {
    Matcher matcher = UNIPROT_PROTEIN_ID_PATTERN.matcher(sequenceName);
    if (matcher.find()) {
      String proteinId = matcher.group(1);
      if (proteinId == null) {
        proteinId = matcher.group(2);
      }
      return proteinId;
    }
    throw new IllegalStateException(
        sequenceName + " does not match pattern " + UNIPROT_PROTEIN_ID_PATTERN);
  }
}
