package ca.qc.ircm.genefinder.ncbi;

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.progress_bar.ProgressBar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Services using resources from NCBI.
 */
public interface NcbiService {
  public List<ProteinMapping> allProteinMappings(Organism organism,
      ProteinMappingParameters parameters, ProgressBar progressBar, Locale locale)
          throws IOException, InterruptedException;
}
