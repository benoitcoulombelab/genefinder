package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses protein ids from text files.
 */
@Component
public class TextProteinParser extends AbstractProteinParser {
  protected TextProteinParser() {
  }

  protected TextProteinParser(NcbiConfiguration ncbiConfiguration,
      UniprotConfiguration uniprotConfiguration) {
    super(ncbiConfiguration, uniprotConfiguration);
  }

  @Override
  public List<String> parseProteinIds(File input, FindGenesParameters parameters)
      throws IOException {
    Pattern proteinIdPattern = proteinIdPattern(parameters);
    List<String> proteinIds = new ArrayList<>();
    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(input)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        proteinIds
            .addAll(parseProteinIds(columns[parameters.getProteinColumn()], proteinIdPattern));
      }
    }
    return proteinIds;
  }
}
