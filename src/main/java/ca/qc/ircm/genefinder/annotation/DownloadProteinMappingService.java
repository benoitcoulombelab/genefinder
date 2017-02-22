package ca.qc.ircm.genefinder.annotation;

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.progressbar.ProgressBar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Service for {@link ProteinMapping}.
 */
public interface DownloadProteinMappingService {
  public List<ProteinMapping> allProteinMappings(Organism organism, ProgressBar progressBar,
      Locale locale) throws IOException, InterruptedException;
}
