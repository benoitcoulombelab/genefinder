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

import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GuessDataWriter implements DataWriter {
  private static final Pattern EXCEL_FILENAME_PATTERN =
      Pattern.compile(".+\\.xls(\\w?)", Pattern.CASE_INSENSITIVE);

  @Inject
  private ExcelDataWriter excelDataWriter;
  @Inject
  private TextDataWriter textDataWriter;

  protected GuessDataWriter() {
  }

  public GuessDataWriter(ExcelDataWriter excelDataWriter, TextDataWriter textDataWriter) {
    this.excelDataWriter = excelDataWriter;
    this.textDataWriter = textDataWriter;
  }

  @Override
  public void writeGene(File input, File output, FindGenesParameters parameters,
      Map<String, ProteinMapping> mappings) throws IOException, InterruptedException {
    if (EXCEL_FILENAME_PATTERN.matcher(input.getName()).matches()) {
      excelDataWriter.writeGene(input, output, parameters, mappings);
    } else {
      textDataWriter.writeGene(input, output, parameters, mappings);
    }
  }
}
