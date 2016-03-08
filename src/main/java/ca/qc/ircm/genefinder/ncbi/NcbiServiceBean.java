package ca.qc.ircm.genefinder.ncbi;

import ca.qc.ircm.genefinder.ApplicationProperties;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progress_bar.ProgressBar;
import ca.qc.ircm.protein.ProteinService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

public class NcbiServiceBean implements NcbiService {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(NcbiServiceBean.class);
  private static final String GENE_2_ACCESSION_DOWNLOAD = "ncbi.gene2accession";
  private static final String GENE_INFO_DOWNLOAD = "ncbi.gene_info";
  private static final String GI_TAX_DOWNLOAD = "ncbi.gi_taxid";
  private static final String NR_DOWNLOAD = "ncbi.nr";
  private static final String INTERRUPTED_MESSAGE = "Interrupted gene fetching";
  private static final String SEQUENCE_NAME_START_PATTERN = "gi|";
  private static final char SEQUENCE_NAME_END_PATTERN = '|';
  @Inject
  private ProteinService proteinService;
  @Inject
  private ApplicationProperties applicationProperties;
  private Map<Integer, SoftReference<List<ProteinMapping>>> cache =
      new HashMap<Integer, SoftReference<List<ProteinMapping>>>();

  protected NcbiServiceBean() {
  }

  public NcbiServiceBean(ProteinService proteinService,
      ApplicationProperties applicationProperties) {
    this.proteinService = proteinService;
    this.applicationProperties = applicationProperties;
  }

  @Override
  public List<ProteinMapping> allProteinMappings(Organism organism,
      ProteinMappingParameters parameters, ProgressBar progressBar, Locale locale)
          throws IOException, InterruptedException {
    ResourceBundle bundle = ResourceBundle.getBundle(NcbiService.class.getName(), locale);
    if (cache.containsKey(organism.getId())) {
      List<ProteinMapping> mappings = cache.get(organism.getId()).get();
      if (mappings != null) {
        progressBar.setMessage(MessageFormat.format(bundle.getString("cache"), organism.getName()));
        progressBar.setProgress(1.0);
        return mappings;
      }
    }
    File giTaxonomy = null;
    File nr = null;
    File gene2accession = null;
    File geneInfo = null;
    ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
    URL url = new URL(applicationProperties.getProperty(GI_TAX_DOWNLOAD));
    giTaxonomy = getFile(url);
    progressBar.setMessage(MessageFormat.format(bundle.getString("download"), url, giTaxonomy));
    download(url);
    progressBar.setProgress(0.05);
    if (parameters.isGeneId() || parameters.isGeneDetails()) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      url = new URL(applicationProperties.getProperty(GENE_2_ACCESSION_DOWNLOAD));
      gene2accession = getFile(url);
      progressBar
          .setMessage(MessageFormat.format(bundle.getString("download"), url, gene2accession));
      download(url);
    }
    progressBar.setProgress(0.1);
    if (parameters.isSequence() || parameters.isMolecularWeight()) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      url = new URL(applicationProperties.getProperty(NR_DOWNLOAD));
      nr = getFile(url);
      progressBar.setMessage(MessageFormat.format(bundle.getString("download"), url, nr));
      download(url);
    }
    progressBar.setProgress(0.2);
    if (parameters.isGeneDetails()) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      url = new URL(applicationProperties.getProperty(GENE_INFO_DOWNLOAD));
      geneInfo = getFile(url);
      progressBar.setMessage(MessageFormat.format(bundle.getString("download"), url, geneInfo));
      download(url);
    }
    progressBar.setProgress(0.25);
    ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
    progressBar.setMessage(bundle.getString("parse.GI_TAX_DOWNLOAD"));
    List<Integer> gis = parseGiTax(giTaxonomy, organism);
    Map<Integer, ProteinMapping> mappings =
        gis.stream().collect(Collectors.toMap(Function.<Integer>identity(), gi -> {
          ProteinMapping mapping = new ProteinMapping();
          mapping.setGi(gi);
          mapping.setTaxonomyId(organism.getId());
          return mapping;
        }));
    progressBar.setProgress(0.4);
    if (parameters.isGeneId() || parameters.isGeneDetails()) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      progressBar.setMessage(bundle.getString("parse.GENE_2_ACCESSION_DOWNLOAD"));
      parseGene2Accession(gene2accession, mappings, organism);
    }
    progressBar.setProgress(0.55);
    if (parameters.isSequence() || parameters.isMolecularWeight()) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      progressBar.setMessage(bundle.getString("parse.NR_DOWNLOAD"));
      parseNr(nr, mappings, organism);
      if (parameters.isMolecularWeight()) {
        mappings.values().stream().filter(m -> m.getSequence() != null)
            .forEach(m -> m.setMolecularWeight(proteinService.weight(m.getSequence())));
      }
    }
    progressBar.setProgress(0.85);
    if (parameters.isGeneDetails()) {
      ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
      progressBar.setMessage(bundle.getString("parse.GENE_INFO_DOWNLOAD"));
      parseGeneInfo(geneInfo, mappings.values(), organism);
    }
    List<ProteinMapping> mappingsAsList = new ArrayList<>(mappings.values());
    cache.put(organism.getId(), new SoftReference<List<ProteinMapping>>(mappingsAsList));
    progressBar.setProgress(1.0);
    return mappingsAsList;
  }

  private File download(URL url) throws IOException {
    File file = getFile(url);
    if (!wasFileModifiedRecently(file)) {
      download(url, file);
    }
    return file;
  }

  private boolean wasFileModifiedRecently(File file) throws IOException {
    if (file.exists()) {
      LocalDateTime today = LocalDate.now().atTime(0, 0).minusWeeks(1);
      LocalDateTime fileModifiedTime = LocalDateTime
          .ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
      return !fileModifiedTime.isBefore(today);
    }
    return false;
  }

  private File getFile(URL url) throws IOException {
    File home = applicationProperties.getHome();
    if ((!home.exists() && !home.mkdirs()) || !home.isDirectory()) {
      throw new IOException(
          "Could not create directory " + home + " where required files are stored");
    }
    String path = url.getPath();
    return new File(home, FilenameUtils.getName(path));
  }

  private void download(URL url, File outputFile) throws IOException {
    try (InputStream input = new BufferedInputStream(url.openConnection().getInputStream());
        OutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile))) {
      IOUtils.copyLarge(input, output);
    }
  }

  private void parseGene2Accession(File file, Map<Integer, ProteinMapping> mappings,
      Organism organism) throws IOException, InterruptedException {
    Set<Integer> gis = new HashSet<Integer>();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
      String line;
      reader.readLine(); // Skip header
      while ((line = reader.readLine()) != null) {
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        String[] columns = line.split("\t", -1);
        Integer parsedTaxonomyId = Integer.valueOf(columns[0]);
        if (organism.getId().equals(parsedTaxonomyId) && !columns[6].equals("-")) {
          Integer gi = Integer.valueOf(columns[6]);
          if (gis.add(gi)) {
            if (!mappings.containsKey(gi)) {
              ProteinMapping mapping = new ProteinMapping();
              mapping.setTaxonomyId(organism.getId());
              mapping.setGi(gi);
              mappings.put(gi, mapping);
            }
            ProteinMapping mapping = mappings.get(gi);
            mapping.setGeneId(Integer.valueOf(columns[1]));
          }
        }
      }
    }
  }

  private void parseGeneInfo(File file, Collection<ProteinMapping> mappings, Organism organism)
      throws IOException, InterruptedException {
    Map<Integer, Collection<ProteinMapping>> mappingsByGeneId = new HashMap<>();
    for (ProteinMapping m : mappings) {
      if (m.getGeneId() != null) {
        Integer geneId = m.getGeneId();
        if (!mappingsByGeneId.containsKey(geneId)) {
          mappingsByGeneId.put(geneId, new ArrayList<>());
        }
        mappingsByGeneId.get(geneId).add(m);
      }
    }
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
      String line;
      reader.readLine(); // Skip header
      while ((line = reader.readLine()) != null) {
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        String[] columns = line.split("\t", -1);
        Integer parsedTaxonomyId = Integer.valueOf(columns[0]);
        if (organism.getId().equals(parsedTaxonomyId)) {
          Integer id = Integer.valueOf(columns[1]);
          String name = columns[2];
          String synonyms = columns[4];
          String summary = columns[8];
          if (mappingsByGeneId.containsKey(id)) {
            mappingsByGeneId.get(id).forEach(m -> {
              m.setGeneName(name);
              m.setGeneSynonyms(synonyms);
              m.setGeneSummary(summary);
            });
          }
        }
      }
    }
  }

  private List<Integer> parseGiTax(File file, Organism organism)
      throws IOException, InterruptedException {
    final List<Integer> gisForOrganism = new ArrayList<Integer>(100000);
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
      String line;
      while ((line = reader.readLine()) != null) {
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        String[] columns = line.split("\t", -1);
        Integer gi = Integer.valueOf(columns[0]);
        Integer taxonomyId = Integer.valueOf(columns[1]);
        if (organism.getId().equals(taxonomyId)) {
          gisForOrganism.add(gi);
        }
      }
    }
    return gisForOrganism;
  }

  private void parseNr(File file, Map<Integer, ProteinMapping> mappings, Organism organism)
      throws IOException, InterruptedException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8"))) {
      String line;
      List<Integer> gis = new ArrayList<>();
      StringBuilder sequence = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        if (line.startsWith(">")) {
          gis.stream().filter(gi -> mappings.containsKey(gi))
              .forEach(gi -> mappings.get(gi).setSequence(sequence.toString()));
          try {
            parseGis(line, gis);
          } catch (NumberFormatException e) {
            System.out.println(line);
            throw e;
          }
          sequence.setLength(0);
        } else {
          sequence.append(line);
        }
      }
      gis.stream().filter(gi -> mappings.containsKey(gi))
          .forEach(gi -> mappings.get(gi).setSequence(sequence.toString()));
    }
  }

  private void parseGis(String sequenceName, List<Integer> gis) {
    gis.clear();
    int start = 0, end = 0;
    char zero = '0', nine = '9';
    char charAt;
    while (sequenceName.indexOf(SEQUENCE_NAME_START_PATTERN, start) > -1) {
      start = sequenceName.indexOf(SEQUENCE_NAME_START_PATTERN, start)
          + SEQUENCE_NAME_START_PATTERN.length();
      end = start;
      while (end < sequenceName.length() && (charAt = sequenceName.charAt(end)) >= zero
          && charAt <= nine) {
        end++;
      }
      // End must be a delimiter or the end of the string to contain a valid GI. Otherwise, the GI is inside of a comment.
      if (end == sequenceName.length() || sequenceName.charAt(end) == SEQUENCE_NAME_END_PATTERN) {
        gis.add(Integer.valueOf(sequenceName.substring(start, end)));
      }
    }
  }
}
