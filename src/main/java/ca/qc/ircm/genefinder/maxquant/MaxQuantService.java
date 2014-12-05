package ca.qc.ircm.genefinder.maxquant;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.progress_bar.ProgressBar;

/**
 * Services for MaxQuant.
 */
public interface MaxQuantService {
    public File findGeneNames(Organism organism, File proteinGroups, ProgressBar progressBar, Locale locale)
            throws IOException, InterruptedException;
}