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

import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * Parses protein ids from Excel files.
 */
@Component
public class ExcelProteinParser extends AbstractProteinParser {
  protected ExcelProteinParser() {
  }

  protected ExcelProteinParser(NcbiConfiguration ncbiConfiguration,
      UniprotConfiguration uniprotConfiguration) {
    super(ncbiConfiguration, uniprotConfiguration);
  }

  @Override
  public List<String> parseProteinIds(File input, FindGenesParameters parameters)
      throws IOException {
    Pattern proteinIdPattern = proteinIdPattern(parameters);
    List<String> proteinIds = new ArrayList<>();
    NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    numberFormat.setGroupingUsed(false);
    try (InputStream inputStream = new FileInputStream(input)) {
      try (Workbook workbook =
          input.getName().toLowerCase().endsWith(".xls") ? new HSSFWorkbook(inputStream)
              : new XSSFWorkbook(inputStream)) {
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
          Row row = sheet.getRow(i);
          if (row == null) {
            continue;
          }
          Cell cell = row.getCell(parameters.getProteinColumn());
          String value = getComputedValue(cell, numberFormat);
          proteinIds.addAll(parseProteinIds(value, proteinIdPattern));
        }
      }
    }
    return proteinIds;
  }

  private String getComputedValue(Cell cell, NumberFormat numberFormat) {
    if (cell == null) {
      return "";
    }
    switch (cell.getCellTypeEnum()) {
      case STRING:
      case BLANK:
        return cell.getStringCellValue();
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case NUMERIC:
        return numberFormat.format(cell.getNumericCellValue());
      case ERROR:
      case _NONE:
        return "";
      case FORMULA:
        switch (cell.getCachedFormulaResultTypeEnum()) {
          case STRING:
          case BLANK:
            return cell.getStringCellValue();
          case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
          case NUMERIC:
            return numberFormat.format(cell.getNumericCellValue());
          case FORMULA:
          case ERROR:
          case _NONE:
            return "";
          default:
            return "";
        }
      default:
        return "";
    }
  }
}
