package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.annotation.ProteinMapping;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Writes data file with additional information.
 */
public interface DataWriter {
  public static final Pattern PROTEIN_PATTERN = Pattern
      .compile("^(?:gi\\|)?(\\d+)" + "|^(?:sp\\|)?([OPQ][0-9][A-Z0-9]{3}[0-9])(?:-\\d+)?(?:\\|.*)?"
          + "|^(?:sp\\|)?([A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(?:-\\d+)?(?:\\|.*)?");

  public void writeGene(File input, File output, FindGenesParameters parameters,
      Map<String, ProteinMapping> mappings) throws IOException, InterruptedException;
}
