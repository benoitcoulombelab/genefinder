package ca.qc.ircm.genefinder.ncbi;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ca.qc.ircm.genefinder.ApplicationProperties;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progress_bar.ProgressBar;

public class NcbiServiceBean implements NcbiService {
    private static final String GENE_2_ACCESSION_DOWNLOAD = "ncbi.gene2accession";
    private static final String GENE_INFO_DOWNLOAD = "ncbi.gene_info";
    private static final String INTERRUPTED_MESSAGE = "Interrupted gene fetching";
    @Inject
    private ApplicationProperties applicationProperties;
    private Map<Integer, SoftReference<List<ProteinMapping>>> cache = new HashMap<Integer, SoftReference<List<ProteinMapping>>>();

    protected NcbiServiceBean() {
    }

    public NcbiServiceBean(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public List<ProteinMapping> allProteinMappings(Organism organism, ProgressBar progressBar, Locale locale)
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
        File gene2accession = null;
        File geneInfo = null;
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        URL url = new URL(applicationProperties.getProperty(GENE_2_ACCESSION_DOWNLOAD));
        gene2accession = getFile(url);
        progressBar.setMessage(MessageFormat.format(bundle.getString("download"), url, gene2accession));
        download(url);
        progressBar.setProgress(0.2);
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        url = new URL(applicationProperties.getProperty(GENE_INFO_DOWNLOAD));
        geneInfo = getFile(url);
        progressBar.setMessage(MessageFormat.format(bundle.getString("download"), url, geneInfo));
        download(url);
        progressBar.setProgress(0.4);
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        progressBar.setMessage(bundle.getString("parse.GENE_INFO_DOWNLOAD"));
        Map<Integer, String> geneNames = parseGeneInfo(geneInfo, organism);
        progressBar.setProgress(0.7);
        ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
        progressBar.setMessage(bundle.getString("parse.GENE_2_ACCESSION_DOWNLOAD"));
        List<ProteinMapping> proteinMappings = parseGene2Accession(gene2accession, organism);
        proteinMappings.forEach(mapping -> {
            mapping.setGeneName(geneNames.get(mapping.getGeneId()));
        });
        cache.put(organism.getId(), new SoftReference<List<ProteinMapping>>(proteinMappings));
        progressBar.setProgress(1.0);
        return proteinMappings;
    }

    private File download(URL url) throws IOException {
        File file = getFile(url);
        if (!wasFileModifiedToday(file)) {
            download(url, file);
        }
        return file;
    }

    private boolean wasFileModifiedToday(File file) throws IOException {
        if (file.exists()) {
            LocalDateTime today = LocalDate.now().atTime(0, 0);
            LocalDateTime fileModifiedTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()),
                    ZoneId.systemDefault());
            return !fileModifiedTime.isBefore(today);
        }
        return false;
    }

    private File getFile(URL url) throws IOException {
        File home = applicationProperties.getHome();
        if ((!home.exists() && !home.mkdirs()) || !home.isDirectory()) {
            throw new IOException("Could not create directory " + home + " where required files are stored");
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

    private List<ProteinMapping> parseGene2Accession(File file, Organism organism) throws IOException,
    InterruptedException {
        List<ProteinMapping> proteinMappings = new ArrayList<ProteinMapping>();
        Set<Integer> gis = new HashSet<Integer>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(
                file))))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
                String[] columns = line.split("\\t");
                Integer parsedTaxonomyId = Integer.valueOf(columns[0]);
                if (organism.getId().equals(parsedTaxonomyId) && !columns[6].equals("-")) {
                    Integer gi = Integer.valueOf(columns[6]);
                    ProteinMapping mapping = new ProteinMapping();
                    mapping.setTaxonomyId(organism.getId());
                    mapping.setGi(gi);
                    mapping.setGeneId(Integer.valueOf(columns[1]));
                    if (gis.add(gi)) {
                        proteinMappings.add(mapping);
                    }
                }
            }
        }
        return proteinMappings;
    }

    private Map<Integer, String> parseGeneInfo(File file, Organism organism) throws IOException, InterruptedException {
        Map<Integer, String> geneNames = new HashMap<Integer, String>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(
                file))))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                ExceptionUtils.throwIfInterrupted(INTERRUPTED_MESSAGE);
                String[] columns = line.split("\\t");
                Integer parsedTaxonomyId = Integer.valueOf(columns[0]);
                if (organism.getId().equals(parsedTaxonomyId)) {
                    geneNames.put(Integer.valueOf(columns[1]), columns[2]);
                }
            }
        }
        return geneNames;
    }
}
