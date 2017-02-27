package ca.qc.ircm.genefinder.data;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

/**
 * Guesses {@link ProteinParser} instance to use based on file type.
 */
@Component
@Primary
public class GuessProteinParser implements ProteinParser {
  @Inject
  private ExcelProteinParser excelProteinParser;
  @Inject
  private TextProteinParser textProteinParser;

  protected GuessProteinParser() {
  }

  protected GuessProteinParser(ExcelProteinParser excelProteinParser,
      TextProteinParser textProteinParser) {
    this.excelProteinParser = excelProteinParser;
    this.textProteinParser = textProteinParser;
  }

  @Override
  public List<String> parseProteinIds(File input, FindGenesParameters parameters)
      throws IOException {
    if (input.getName().endsWith(".xlsx") || input.getName().endsWith(".xlsm")
        || input.getName().endsWith(".xls")) {
      return excelProteinParser.parseProteinIds(input, parameters);
    } else {
      return textProteinParser.parseProteinIds(input, parameters);
    }
  }
}
