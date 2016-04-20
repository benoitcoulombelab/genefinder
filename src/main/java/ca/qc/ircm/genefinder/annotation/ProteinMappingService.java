package ca.qc.ircm.genefinder.annotation;

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.progress_bar.ProgressBar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Service for {@link ProteinMapping}.
 */
public interface ProteinMappingService {
  public List<ProteinMapping> allProteinMappings(Organism organism, ProgressBar progressBar,
      Locale locale) throws IOException, InterruptedException;
}
