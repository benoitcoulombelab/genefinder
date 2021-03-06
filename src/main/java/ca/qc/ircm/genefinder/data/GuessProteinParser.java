/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.genefinder.data;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Guesses {@link ProteinParser} instance to use based on file type.
 */
@Component
@Primary
public class GuessProteinParser implements ProteinParser {
  private static final Pattern EXCEL_FILENAME_PATTERN =
      Pattern.compile(".+\\.xls(\\w?)", Pattern.CASE_INSENSITIVE);
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
    if (EXCEL_FILENAME_PATTERN.matcher(input.getName()).matches()) {
      return excelProteinParser.parseProteinIds(input, parameters);
    } else {
      return textProteinParser.parseProteinIds(input, parameters);
    }
  }
}
