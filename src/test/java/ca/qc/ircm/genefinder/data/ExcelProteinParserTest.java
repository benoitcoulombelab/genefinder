package ca.qc.ircm.genefinder.data;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.UNIPROT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExcelProteinParserTest {
  private ExcelProteinParser excelProteinParser;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private NcbiConfiguration ncbiConfiguration;
  @Mock
  private UniprotConfiguration uniprotConfiguration;

  /**
   * Before test.
   */
  @Before
  @SuppressWarnings("checkstyle:linelength")
  public void beforeTest() {
    excelProteinParser = new ExcelProteinParser(ncbiConfiguration, uniprotConfiguration);
    when(ncbiConfiguration.refseqProteinAccessionPattern())
        .thenReturn(Pattern.compile("^(?:ref\\|)?([ANYXZ]P_\\d+\\.\\d+)"));
    when(ncbiConfiguration.refseqProteinGiPattern())
        .thenReturn(Pattern.compile("^(?:gi\\|)?(\\d+)"));
    when(uniprotConfiguration.proteinIdPattern()).thenReturn(Pattern.compile(
        "^(?:\\w{2}\\|)?([OPQ][0-9][A-Z0-9]{3}[0-9])(?:-\\d+)?(?:\\|.*)?|^(?:\\w{2}\\|)?([A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(?:-\\d+)?(?:\\|.*)?"));
  }

  @Test
  public void parseProteinIds() throws Throwable {
    final File input = new File(getClass().getResource("/data/data.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(6, ids.size());
    assertTrue(ids.contains("119627830"));
    assertTrue(ids.contains("119580583"));
    assertTrue(ids.contains("108250308"));
    assertTrue(ids.contains("100913206"));
    assertTrue(ids.contains("269849686"));
    assertTrue(ids.contains("119580714"));
  }

  @Test
  public void parseProteinIds_MultipleLines() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(9, ids.size());
    assertTrue(ids.contains("119627830"));
    assertTrue(ids.contains("189054652"));
    assertTrue(ids.contains("119580583"));
    assertTrue(ids.contains("108250308"));
    assertTrue(ids.contains("119605998"));
    assertTrue(ids.contains("100913206"));
    assertTrue(ids.contains("119589484"));
    assertTrue(ids.contains("269849686"));
    assertTrue(ids.contains("119580714"));
  }

  @Test
  public void parseProteinIds_MultipleLinesInDifferentColumns() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_manycolumns.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(9, ids.size());
    assertTrue(ids.contains("119627830"));
    assertTrue(ids.contains("189054652"));
    assertTrue(ids.contains("119580583"));
    assertTrue(ids.contains("108250308"));
    assertTrue(ids.contains("119605998"));
    assertTrue(ids.contains("100913206"));
    assertTrue(ids.contains("119589484"));
    assertTrue(ids.contains("269849686"));
    assertTrue(ids.contains("119580714"));
  }

  @Test
  public void parseProteinIds_NoGi() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(6, ids.size());
    assertTrue(ids.contains("119627830"));
    assertTrue(ids.contains("119580583"));
    assertTrue(ids.contains("108250308"));
    assertTrue(ids.contains("100913206"));
    assertTrue(ids.contains("269849686"));
    assertTrue(ids.contains("119580714"));
  }

  @Test
  public void parseProteinIds_ManyNoGi() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi_many.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(9, ids.size());
    assertTrue(ids.contains("119627830"));
    assertTrue(ids.contains("189054652"));
    assertTrue(ids.contains("119580583"));
    assertTrue(ids.contains("108250308"));
    assertTrue(ids.contains("119605998"));
    assertTrue(ids.contains("100913206"));
    assertTrue(ids.contains("119589484"));
    assertTrue(ids.contains("269849686"));
    assertTrue(ids.contains("119580714"));
  }

  @Test
  public void parseProteinIds_ManyNoGiInDifferentColumns() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi_manycolumns.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(9, ids.size());
    assertTrue(ids.contains("119627830"));
    assertTrue(ids.contains("189054652"));
    assertTrue(ids.contains("119580583"));
    assertTrue(ids.contains("108250308"));
    assertTrue(ids.contains("119605998"));
    assertTrue(ids.contains("100913206"));
    assertTrue(ids.contains("119589484"));
    assertTrue(ids.contains("269849686"));
    assertTrue(ids.contains("119580714"));
  }

  @Test
  public void parseProteinIds_Uniprot() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_uniprot.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(2, ids.size());
    assertTrue(ids.contains("P11171"));
    assertTrue(ids.contains("Q08211"));
  }

  @Test
  public void parseProteinIds_Refseq() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_refseq.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(2, ids.size());
    assertTrue(ids.contains("NP_001159477.1"));
    assertTrue(ids.contains("NP_001348.2"));
  }

  @Test
  public void parseProteinIds_RefseqWrongDatabase() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_refseq.xlsx").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = excelProteinParser.parseProteinIds(input, parameters);

    assertEquals(0, ids.size());
  }
}
