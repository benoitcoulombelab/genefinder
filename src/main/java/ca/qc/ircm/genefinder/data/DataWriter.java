package ca.qc.ircm.genefinder.data;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import ca.qc.ircm.genefinder.ncbi.ProteinMapping;

/**
 * Writes data file with additional information.
 */
public interface DataWriter {
    public static final Pattern GI_PATTERN = Pattern.compile("(?:gi\\|)?(\\d+)");

    public void writeGene(File input, File output, FindGenesParameters parameters, Map<Integer, ProteinMapping> mappings)
            throws IOException, InterruptedException;
}
