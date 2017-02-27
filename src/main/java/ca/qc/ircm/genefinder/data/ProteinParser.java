package ca.qc.ircm.genefinder.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Parses protein ids from file.
 */
public interface ProteinParser {
  public List<String> parseProteinIds(File input, FindGenesParameters parameters)
      throws IOException;
}
