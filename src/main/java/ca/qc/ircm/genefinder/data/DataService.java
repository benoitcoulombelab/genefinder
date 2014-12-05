package ca.qc.ircm.genefinder.data;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.progress_bar.ProgressBar;

/**
 * Services for data file.
 */
public interface DataService {
    public File findGeneNames(Organism organism, File file, FindGenesParameters findGenesParameter,
            ProgressBar progressBar, Locale locale) throws IOException, InterruptedException;
}
