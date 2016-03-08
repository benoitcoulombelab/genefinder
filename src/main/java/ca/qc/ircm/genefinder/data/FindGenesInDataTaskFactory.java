package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.organism.Organism;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

/**
 * Creates instances of {@link FindGenesInDataTask}.
 */
public interface FindGenesInDataTaskFactory {
  public FindGenesInDataTask create(Organism organism, Collection<File> files,
      FindGenesParameters findGenesParameter, Locale locale);
}
