package ca.qc.ircm.genefinder.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExcelDataWriterTest {
  private ExcelDataWriter excelDataWriter;
  @Mock
  private FindGenesParameters parameters;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private static final NumberFormat numberFormat;

  static {
    numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    numberFormat.setGroupingUsed(false);
  }

  private static final NumberFormat doubleFormat;

  static {
    doubleFormat = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    doubleFormat.setMinimumFractionDigits(1);
    doubleFormat.setGroupingUsed(false);
  }

  @Before
  public void beforeTest() throws Throwable {
    excelDataWriter = new ExcelDataWriter();
    temporaryFolder.create();
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

  @Test
  public void writeGene() throws Throwable {
    final File input = new File(getClass().getResource("/data/data.xlsx").toURI());
    final File output = temporaryFolder.newFile("data.xlsx");
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("gi|119627830", getComputedValue(row.getCell(0)));
      assertEquals("1234", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A", getComputedValue(row.getCell(3)));
      assertEquals("This gene encodes the largest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4), doubleFormat));
      assertEquals("20.0", getComputedValue(row.getCell(5), doubleFormat));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("gi|119580583", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
    }
  }

  @Test
  public void writeGene_Many() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    mapping.setGeneId(4567L);
    mapping.setGeneName("POLR2B");
    mapping.setGeneSynonyms("RPB2|RPO2B");
    mapping.setGeneSummary("This gene encodes the smallest subunit of RNA polymerase II");
    mapping.setMolecularWeight(3.4);
    mappings.put("189054652", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("gi|119627830;gi|189054652", getComputedValue(row.getCell(0)));
      assertEquals("1234;4567", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A;POLR2B", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", getComputedValue(row.getCell(3)));
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
      assertEquals("20.0;3.4", getComputedValue(row.getCell(5), doubleFormat));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("gi|119580583", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
    }
  }

  @Test
  public void writeGene_ManyInDifferentColumns() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_manycolumns.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    mapping.setGeneId(4567L);
    mapping.setGeneName("POLR2B");
    mapping.setGeneSynonyms("RPB2|RPO2B");
    mapping.setGeneSummary("This gene encodes the smallest subunit of RNA polymerase II");
    mapping.setMolecularWeight(3.4);
    mappings.put("189054652", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("gi|119627830;gi|189054652", getComputedValue(row.getCell(0)));
      assertEquals("1234;4567", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A;POLR2B", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", getComputedValue(row.getCell(3)));
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
      assertEquals("20.0;3.4", getComputedValue(row.getCell(5)));
      assertEquals("gi|119621462", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("gi|119580583", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("gi|119572880", getComputedValue(row.getCell(6)));
    }
  }

  @Test
  public void writeGene_NoGi() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("119627830", getComputedValue(row.getCell(0)));
      assertEquals("1234", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A", getComputedValue(row.getCell(3)));
      assertEquals("This gene encodes the largest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
      assertEquals("20.0", getComputedValue(row.getCell(5), doubleFormat));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("119580583", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
    }
  }

  @Test
  public void writeGene_ManyNoGi() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi_many.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    mapping.setGeneId(4567L);
    mapping.setGeneName("POLR2B");
    mapping.setGeneSynonyms("RPB2|RPO2B");
    mapping.setGeneSummary("This gene encodes the smallest subunit of RNA polymerase II");
    mapping.setMolecularWeight(3.4);
    mappings.put("189054652", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("119627830;189054652", getComputedValue(row.getCell(0)));
      assertEquals("1234;4567", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A;POLR2B", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", getComputedValue(row.getCell(3)));
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
      assertEquals("20.0;3.4", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("119580583", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
    }
  }

  @Test
  public void writeGene_ManyNoGiInDifferentColumns() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi_manycolumns.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    mapping.setGeneId(4567L);
    mapping.setGeneName("POLR2B");
    mapping.setGeneSynonyms("RPB2|RPO2B");
    mapping.setGeneSummary("This gene encodes the smallest subunit of RNA polymerase II");
    mapping.setMolecularWeight(3.4);
    mappings.put("189054652", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("119627830;189054652", getComputedValue(row.getCell(0)));
      assertEquals("1234;4567", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A;POLR2B", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", getComputedValue(row.getCell(3)));
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
      assertEquals("20.0;3.4", getComputedValue(row.getCell(5)));
      assertEquals("119621462", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("119580583", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("119572880", getComputedValue(row.getCell(6)));
    }
  }

  @Test
  public void writeGene_Uniprot() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_uniprot.xlsx").toURI());
    final File output = temporaryFolder.newFile("data.xlsx");
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("P11171", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("sp|P11171", getComputedValue(row.getCell(0)));
      assertEquals("1234", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A", getComputedValue(row.getCell(3)));
      assertEquals("This gene encodes the largest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
      assertEquals("20.0", getComputedValue(row.getCell(5), doubleFormat));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("sp|Q08211", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
    }
  }

  @Test
  public void writeGene_Trembl() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_trembl.xlsx").toURI());
    final File output = temporaryFolder.newFile("data.xlsx");
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("P11171", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("tr|P11171", getComputedValue(row.getCell(0)));
      assertEquals("1234", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A", getComputedValue(row.getCell(3)));
      assertEquals("This gene encodes the largest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
      assertEquals("20.0", getComputedValue(row.getCell(5), doubleFormat));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("tr|Q08211", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
    }
  }

  @Test
  public void writeGene_Refseq() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_refseq.xlsx").toURI());
    final File output = temporaryFolder.newFile("data.xlsx");
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGeneId(1234L);
    mapping.setGeneName("POLR2A");
    mapping.setGeneSynonyms("RPB1|RPO2A");
    mapping.setGeneSummary("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setMolecularWeight(20.0);
    mappings.put("NP_001159477.1", mapping);

    excelDataWriter.writeGene(input, output, parameters, mappings);

    try (InputStream inputStream = new FileInputStream(output)) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      assertEquals("human", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(2);
      assertEquals("ref|NP_001159477.1", getComputedValue(row.getCell(0)));
      assertEquals("1234", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A", getComputedValue(row.getCell(3)));
      assertEquals("This gene encodes the largest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
      assertEquals("20.0", getComputedValue(row.getCell(5), doubleFormat));
      assertEquals("", getComputedValue(row.getCell(6)));
      row = sheet.getRow(3);
      assertEquals("ref|NP_001348.2", getComputedValue(row.getCell(0)));
      assertEquals("", getComputedValue(row.getCell(1)));
      assertEquals("", getComputedValue(row.getCell(2)));
      assertEquals("", getComputedValue(row.getCell(3)));
      assertEquals("", getComputedValue(row.getCell(4)));
      assertEquals("", getComputedValue(row.getCell(5)));
      assertEquals("", getComputedValue(row.getCell(6)));
    }
  }
}
