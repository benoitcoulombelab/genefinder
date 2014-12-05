package ca.qc.ircm.genefinder.maxquant;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import ca.qc.ircm.genefinder.organism.Organism;

/**
 * Creates instances of {@link FindGenesInMaxQuantTask}.
 */
public interface FindGenesInMaxQuantTaskFactory {
    public FindGenesInMaxQuantTask create(Organism organism, Collection<File> proteinGroupsFiles, Locale locale);
}
