package ca.qc.ircm.genefinder.maxquant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ca.qc.ircm.genefinder.ncbi.NcbiService;
import ca.qc.ircm.genefinder.ncbi.ProteinMapping;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progress_bar.ProgressBar;

public class MaxQuantServiceBean implements MaxQuantService {
    private static final Pattern GI_PATTERN = Pattern.compile("gi\\|(\\d+)");
    @Inject
    private NcbiService ncbiService;

    protected MaxQuantServiceBean() {
    }

    public MaxQuantServiceBean(NcbiService ncbiService) {
        this.ncbiService = ncbiService;
    }

    @Override
    public File findGeneNames(Organism organism, File proteinGroups, ProgressBar progressBar, Locale locale)
            throws IOException, InterruptedException {
        ResourceBundle bundle = ResourceBundle.getBundle(MaxQuantService.class.getName(), locale);
        progressBar.setMessage(MessageFormat.format(bundle.getString("mappings"), organism.getName()));
        ExceptionUtils.throwIfInterrupted("Interrupted gene finding");
        List<ProteinMapping> rawMappings = ncbiService.allProteinMappings(organism, progressBar.step(0.5), locale);
        Map<Integer, ProteinMapping> mappings = rawMappings.stream().collect(
                Collectors.toMap(ProteinMapping::getGi, Function.<ProteinMapping> identity()));
        progressBar.setProgress(0.5);
        progressBar.setMessage(MessageFormat.format(bundle.getString("finding"), proteinGroups.getName()));
        File temp = File.createTempFile("proteinGroups", ".txt");
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(proteinGroups)));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp)))) {
            String line;
            // Add gene column.
            line = reader.readLine();
            writer.write("Gene\t");
            writer.write(line);
            writer.write(System.getProperty("line.separator"));
            // Finds gene.
            while ((line = reader.readLine()) != null) {
                ExceptionUtils.throwIfInterrupted("Interrupted gene finding");
                Set<String> geneNames = new LinkedHashSet<String>();
                String gis = line.split("\\t")[0];
                Matcher matcher = GI_PATTERN.matcher(gis);
                int start = 0;
                while (matcher.find(start)) {
                    Integer gi = Integer.valueOf(matcher.group(1));
                    ProteinMapping proteinMapping = mappings.get(gi);
                    if (proteinMapping != null) {
                        geneNames.add(proteinMapping.getGeneName());
                    }
                    start = matcher.end();
                }
                StringBuilder geneNamesToWrite = new StringBuilder();
                if (!geneNames.isEmpty()) {
                    geneNames.forEach(name -> {
                        geneNamesToWrite.append(name);
                        geneNamesToWrite.append(";");
                    });
                    geneNamesToWrite.deleteCharAt(geneNamesToWrite.length() - 1);
                }
                writer.write(geneNamesToWrite.toString());
                writer.write("\t");
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            }
        }
        progressBar.setProgress(1.0);
        return temp;
    }
}
