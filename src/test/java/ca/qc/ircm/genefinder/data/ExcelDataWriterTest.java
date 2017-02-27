package ca.qc.ircm.genefinder.data;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.SWISSPROT;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.UNIPROT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.GeneInfo;
import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExcelDataWriterTest {
  private ExcelDataWriter excelDataWriter;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private NcbiConfiguration ncbiConfiguration;
  @Mock
  private UniprotConfiguration uniprotConfiguration;
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

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    excelDataWriter = new ExcelDataWriter(ncbiConfiguration, uniprotConfiguration);
    temporaryFolder.create();
    when(ncbiConfiguration.refseqProteinAccessionPattern())
        .thenReturn(Pattern.compile("^(?:ref\\|)?([ANYXZ]P_\\d+\\.\\d+)"));
    when(ncbiConfiguration.refseqProteinGiPattern())
        .thenReturn(Pattern.compile("^(?:gi\\|)?(\\d+)"));
    when(uniprotConfiguration.proteinIdPattern()).thenReturn(Pattern.compile(
        "^(?:\\w{2}\\|)?([OPQ][0-9][A-Z0-9]{3}[0-9])(?:-\\d+)?(?:\\|.*)?|^(?:\\w{2}\\|)?([A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(?:-\\d+)?(?:\\|.*)?"));
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
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
  public void writeGene_ManyGenesForProtein() throws Throwable {
    final File input = new File(getClass().getResource("/data/data.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    GeneInfo gene1 = new GeneInfo(1234L, "POLR2A");
    gene1.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene1.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    GeneInfo gene2 = new GeneInfo(4567L, "POLR2B");
    gene2.setSynonyms(Arrays.asList("RPB2", "RPO2B"));
    gene2.setDescription("This gene encodes the smallest subunit of RNA polymerase II");
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGenes(Arrays.asList(gene1, gene2));
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
      assertEquals("1234;4567", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A;POLR2B", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", getComputedValue(row.getCell(3)));
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          getComputedValue(row.getCell(4)));
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
  public void writeGene_MultipleLines() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    gene = new GeneInfo(4567L, "POLR2B");
    gene.setSynonyms(Arrays.asList("RPB2", "RPO2B"));
    gene.setDescription("This gene encodes the smallest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
  public void writeGene_MultipleLinesInDifferentColumns() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_manycolumns.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    gene = new GeneInfo(4567L, "POLR2B");
    gene.setSynonyms(Arrays.asList("RPB2", "RPO2B"));
    gene.setDescription("This gene encodes the smallest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
  public void writeGene_MultipleLinesWithManyGenesForProtein() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    GeneInfo gene1 = new GeneInfo(1234L, "POLR2A");
    gene1.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene1.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    GeneInfo gene2 = new GeneInfo(4567L, "POLR2B");
    gene2.setSynonyms(Arrays.asList("RPB2", "RPO2B"));
    gene2.setDescription("This gene encodes the smallest subunit of RNA polymerase II");
    ProteinMapping mapping = new ProteinMapping();
    mapping.setGenes(Arrays.asList(gene1, gene2));
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(4568L, "POLR2C");
    gene.setSynonyms(Arrays.asList("RPB3", "RPO2C"));
    gene.setDescription("This gene encodes the second smallest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene2, gene));
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
      assertEquals("1234;4567;4568", getComputedValue(row.getCell(1)));
      assertEquals("POLR2A;POLR2B;POLR2C", getComputedValue(row.getCell(2)));
      assertEquals("RPB1|RPO2A;RPB2|RPO2B;RPB3|RPO2C", getComputedValue(row.getCell(3)));
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II;This gene encodes the second smallest subunit of RNA polymerase II",
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
  public void writeGene_NoGi() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi.xlsx").toURI());
    final File output = temporaryFolder.newFile();
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    gene = new GeneInfo(4567L, "POLR2B");
    gene.setSynonyms(Arrays.asList("RPB2", "RPO2B"));
    gene.setDescription("This gene encodes the smallest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
    mapping.setMolecularWeight(20.0);
    mappings.put("119627830", mapping);
    mapping = new ProteinMapping();
    gene = new GeneInfo(4567L, "POLR2B");
    gene.setSynonyms(Arrays.asList("RPB2", "RPO2B"));
    gene.setDescription("This gene encodes the smallest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
    when(parameters.getProteinDatabase()).thenReturn(SWISSPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
    when(parameters.getProteinDatabase()).thenReturn(UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final Map<String, ProteinMapping> mappings = new HashMap<>();
    ProteinMapping mapping = new ProteinMapping();
    GeneInfo gene = new GeneInfo(1234L, "POLR2A");
    gene.setSynonyms(Arrays.asList("RPB1", "RPO2A"));
    gene.setDescription("This gene encodes the largest subunit of RNA polymerase II");
    mapping.setGenes(Arrays.asList(gene));
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
