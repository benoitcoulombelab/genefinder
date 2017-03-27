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
import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ExcelDataWriter extends AbstractDataWriter implements DataWriter {
  private static final NumberFormat doubleFormat;

  static {
    doubleFormat = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    doubleFormat.setMinimumFractionDigits(1);
    doubleFormat.setGroupingUsed(false);
  }

  private static final NumberFormat numberFormat;

  static {
    numberFormat = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    numberFormat.setGroupingUsed(false);
  }

  protected ExcelDataWriter() {
    super();
  }

  protected ExcelDataWriter(NcbiConfiguration ncbiConfiguration,
      UniprotConfiguration uniprotConfiguration) {
    super(ncbiConfiguration, uniprotConfiguration);
  }

  @Override
  public void writeGene(File input, File output, FindGenesParameters parameters,
      Map<String, ProteinMapping> mappings) throws IOException, InterruptedException {
    Pattern proteinIdPattern = proteinIdPattern(parameters);
    try (InputStream inputStream = new FileInputStream(input)) {
      Workbook workbook;
      if (input.getName().endsWith(".xlsx")) {
        workbook = new XSSFWorkbook(inputStream);
      } else {
        workbook = new HSSFWorkbook(inputStream);
      }
      Sheet sheet = workbook.getSheetAt(0);
      for (int i = 0; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        Cell cell = row.getCell(parameters.getProteinColumn());
        String value = getComputedValue(cell);
        List<String> proteinIds = parseProteinIds(value, proteinIdPattern);
        int addedCount = 0;
        if (parameters.isGeneId()) {
          addedCount++;
        }
        if (parameters.isGeneName()) {
          addedCount++;
        }
        if (parameters.isGeneSynonyms()) {
          addedCount++;
        }
        if (parameters.isGeneSummary()) {
          addedCount++;
        }
        if (parameters.isProteinMolecularWeight()) {
          addedCount++;
        }
        shitCells(row, parameters.getProteinColumn(), addedCount);
        int index = parameters.getProteinColumn() + 1;
        if (parameters.isGeneId()) {
          cell = row.getCell(index++);
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getGenes()).filter(genes -> genes != null)
              .flatMap(genes -> genes.stream()).map(gene -> numberFormat.format(gene.getId()))
              .distinct().collect(Collectors.joining(PROTEIN_DELIMITER));
          cell.setCellType(Cell.CELL_TYPE_STRING);
          cell.setCellValue(newValue);
          if (!newValue.isEmpty() && !newValue.contains(PROTEIN_DELIMITER)) {
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(Long.parseLong(newValue));
          }
        }
        if (parameters.isGeneName()) {
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getGenes()).filter(genes -> genes != null)
              .flatMap(genes -> genes.stream()).map(gene -> gene.getSymbol()).filter(s -> s != null)
              .distinct().collect(Collectors.joining(PROTEIN_DELIMITER));
          cell = row.getCell(index++);
          cell.setCellType(Cell.CELL_TYPE_STRING);
          cell.setCellValue(newValue);
        }
        if (parameters.isGeneSynonyms()) {
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getGenes()).filter(genes -> genes != null)
              .flatMap(genes -> genes.stream()).map(gene -> gene.getSynonyms())
              .filter(s -> s != null)
              .map(s -> s.stream().collect(Collectors.joining(LIST_DELIMITER))).distinct()
              .collect(Collectors.joining(PROTEIN_DELIMITER));
          cell = row.getCell(index++);
          cell.setCellType(Cell.CELL_TYPE_STRING);
          cell.setCellValue(newValue);
        }
        if (parameters.isGeneSummary()) {
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getGenes()).filter(genes -> genes != null)
              .flatMap(genes -> genes.stream()).map(gene -> gene.getDescription())
              .filter(s -> s != null).distinct().collect(Collectors.joining(PROTEIN_DELIMITER));
          cell = row.getCell(index++);
          cell.setCellType(Cell.CELL_TYPE_STRING);
          cell.setCellValue(newValue);
        }
        if (parameters.isProteinMolecularWeight()) {
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getMolecularWeight())
              .filter(mw -> mw != null).map(mw -> doubleFormat.format(mw))
              .collect(Collectors.joining(PROTEIN_DELIMITER));
          cell = row.getCell(index++);
          cell.setCellType(Cell.CELL_TYPE_STRING);
          cell.setCellValue(newValue);
          if (!newValue.isEmpty() && !newValue.contains(PROTEIN_DELIMITER)) {
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(Double.parseDouble(newValue));
            CellStyle style = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            style.setDataFormat(format.getFormat("0.00"));
            cell.setCellStyle(style);
          }
        }
      }
      try (OutputStream outputStream = new FileOutputStream(output)) {
        workbook.write(outputStream);
      }
    }
  }

  private void shitCells(Row row, int start, int count) {
    int end = row.getLastCellNum();
    for (int i = 0; i < end + count; i++) {
      if (row.getCell(i) == null) {
        row.createCell(i);
      }
    }
    for (int i = end - 1; i > start; i--) {
      Cell destination = row.getCell(i + count);
      Cell source = row.getCell(i);
      destination.setCellType(source.getCellType());
      destination.setCellStyle(source.getCellStyle());
      switch (source.getCellType()) {
        case Cell.CELL_TYPE_STRING:
          destination.setCellValue(source.getStringCellValue());
          break;
        case Cell.CELL_TYPE_BOOLEAN:
          destination.setCellValue(source.getBooleanCellValue());
          break;
        case Cell.CELL_TYPE_NUMERIC:
          destination.setCellValue(source.getNumericCellValue());
          break;
        case Cell.CELL_TYPE_ERROR:
          destination.setCellValue(source.getErrorCellValue());
          break;
        case Cell.CELL_TYPE_FORMULA:
          destination.setCellValue(source.getCellFormula());
          break;
        default:
          destination.setCellValue(source.getStringCellValue());
          break;
      }
    }
  }

  private String getComputedValue(Cell cell) {
    return getComputedValue(cell, numberFormat);
  }

  private String getComputedValue(Cell cell, NumberFormat numberFormat) {
    if (cell == null) {
      return "";
    }
    switch (cell.getCellType()) {
      case Cell.CELL_TYPE_STRING:
      case Cell.CELL_TYPE_BLANK:
        return cell.getStringCellValue();
      case Cell.CELL_TYPE_BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case Cell.CELL_TYPE_NUMERIC:
        return numberFormat.format(cell.getNumericCellValue());
      case Cell.CELL_TYPE_ERROR:
        return "";
      case Cell.CELL_TYPE_FORMULA:
        switch (cell.getCachedFormulaResultType()) {
          case Cell.CELL_TYPE_STRING:
            return cell.getStringCellValue();
          case Cell.CELL_TYPE_BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
          case Cell.CELL_TYPE_NUMERIC:
            return numberFormat.format(cell.getNumericCellValue());
          case Cell.CELL_TYPE_ERROR:
            return "";
          default:
            return "";
        }
      default:
        return "";
    }
  }
}
