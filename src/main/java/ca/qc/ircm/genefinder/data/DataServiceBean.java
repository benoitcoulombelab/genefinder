package ca.qc.ircm.genefinder.data;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;

import ca.qc.ircm.genefinder.ncbi.NcbiService;
import ca.qc.ircm.genefinder.ncbi.ProteinMapping;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progress_bar.ProgressBar;

public class DataServiceBean implements DataService {
    @Inject
    private NcbiService ncbiService;
    @Inject
    private DataWriter dataWriter;

    protected DataServiceBean() {
    }

    public DataServiceBean(NcbiService ncbiService, DataWriter dataWriter) {
        this.ncbiService = ncbiService;
        this.dataWriter = dataWriter;
    }

    @Override
    public File findGeneNames(Organism organism, File file, FindGenesParameters parameters, ProgressBar progressBar,
            Locale locale) throws IOException, InterruptedException {
        ResourceBundle bundle = ResourceBundle.getBundle(DataService.class.getName(), locale);
        progressBar.setMessage(MessageFormat.format(bundle.getString("mappings"), organism.getName()));
        ExceptionUtils.throwIfInterrupted("Interrupted gene finding");
        List<ProteinMapping> rawMappings = ncbiService.allProteinMappings(organism, progressBar.step(0.5), locale);
        Map<Integer, ProteinMapping> mappings = rawMappings.stream().collect(
                Collectors.toMap(ProteinMapping::getGi, Function.<ProteinMapping> identity()));
        progressBar.setProgress(0.5);
        progressBar.setMessage(MessageFormat.format(bundle.getString("finding"), file.getName()));
        String extension = FilenameUtils.getExtension(file.getName());
        File temp = File.createTempFile("withGenes", "." + extension);
        dataWriter.writeGene(file, temp, parameters, mappings);
        progressBar.setProgress(1.0);
        return temp;
    }
}
