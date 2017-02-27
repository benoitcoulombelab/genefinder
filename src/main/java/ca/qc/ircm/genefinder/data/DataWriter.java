package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.annotation.ProteinMapping;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Writes data file with additional information.
 */
public interface DataWriter {
  public void writeGene(File input, File output, FindGenesParameters parameters,
      Map<String, ProteinMapping> mappings) throws IOException, InterruptedException;
}
