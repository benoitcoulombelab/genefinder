package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.genefinder.test.config.Rules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GeneInfoParserTest {
  private GeneInfoParser geneInfoParser;
  @Rule
  public RuleChain rules = Rules.defaultRules(this);
  @Mock
  private Consumer<GeneInfo> handler;
  @Mock
  private BiConsumer<GeneInfo, String> biHandler;
  @Captor
  private ArgumentCaptor<GeneInfo> geneInfoCaptor;
  @Captor
  private ArgumentCaptor<String> lineCaptor;

  @Before
  public void beforeTest() {
    geneInfoParser = new GeneInfoParser();
  }

  private LocalDate localDate(Date date) {
    return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  private void validateGeneInfos(List<GeneInfo> geneInfos) {
    assertEquals(4, geneInfos.size());
    GeneInfo geneInfo = geneInfos.get(0);
    assertEquals(9606, geneInfo.getOrganismId());
    assertEquals(1, geneInfo.getId());
    assertEquals("A1BG", geneInfo.getSymbol());
    assertNull(geneInfo.getLocusTag());
    assertEquals(4, geneInfo.getSynonyms().size());
    assertEquals("A1B", geneInfo.getSynonyms().get(0));
    assertEquals("ABG", geneInfo.getSynonyms().get(1));
    assertEquals("GAB", geneInfo.getSynonyms().get(2));
    assertEquals("HYST2477", geneInfo.getSynonyms().get(3));
    assertEquals(5, geneInfo.getDbXrefs().size());
    assertEquals("MIM:138670", geneInfo.getDbXrefs().get(0));
    assertEquals("HGNC:HGNC:5", geneInfo.getDbXrefs().get(1));
    assertEquals("Ensembl:ENSG00000121410", geneInfo.getDbXrefs().get(2));
    assertEquals("HPRD:00726", geneInfo.getDbXrefs().get(3));
    assertEquals("Vega:OTTHUMG00000183507", geneInfo.getDbXrefs().get(4));
    assertEquals("19", geneInfo.getChromosome());
    assertEquals("19q13.4", geneInfo.getMapLocation());
    assertEquals("alpha-1-B glycoprotein", geneInfo.getDescription());
    assertEquals("protein-coding", geneInfo.getTypeOfGene());
    assertEquals("A1BG", geneInfo.getSymbolFromNomenclatureAuthority());
    assertEquals("alpha-1-B glycoprotein", geneInfo.getFullNameFromNomenclatureAuthority());
    assertEquals("O", geneInfo.getNomenclatureStatus());
    assertEquals(2, geneInfo.getOtherDesignations().size());
    assertEquals("HEL-S-163pA", geneInfo.getOtherDesignations().get(0));
    assertEquals("epididymis secretory sperm binding protein Li 163pA",
        geneInfo.getOtherDesignations().get(1));
    assertEquals(LocalDate.of(2016, 2, 7), localDate(geneInfo.getModificationDate()));
    geneInfo = geneInfos.get(1);
    assertEquals(9606, geneInfo.getOrganismId());
    assertEquals(2149, geneInfo.getId());
    assertEquals("F2R", geneInfo.getSymbol());
    assertNull(geneInfo.getLocusTag());
    assertEquals(5, geneInfo.getSynonyms().size());
    assertEquals("CF2R", geneInfo.getSynonyms().get(0));
    assertEquals("HTR", geneInfo.getSynonyms().get(1));
    assertEquals("PAR-1", geneInfo.getSynonyms().get(2));
    assertEquals("PAR1", geneInfo.getSynonyms().get(3));
    assertEquals("TR", geneInfo.getSynonyms().get(4));
    assertEquals(5, geneInfo.getDbXrefs().size());
    assertEquals("MIM:187930", geneInfo.getDbXrefs().get(0));
    assertEquals("HGNC:HGNC:3537", geneInfo.getDbXrefs().get(1));
    assertEquals("Ensembl:ENSG00000181104", geneInfo.getDbXrefs().get(2));
    assertEquals("HPRD:01763", geneInfo.getDbXrefs().get(3));
    assertEquals("Vega:OTTHUMG00000131299", geneInfo.getDbXrefs().get(4));
    assertEquals("5", geneInfo.getChromosome());
    assertEquals("5q13", geneInfo.getMapLocation());
    assertEquals("coagulation factor II thrombin receptor", geneInfo.getDescription());
    assertEquals("protein-coding", geneInfo.getTypeOfGene());
    assertEquals("F2R", geneInfo.getSymbolFromNomenclatureAuthority());
    assertEquals("coagulation factor II thrombin receptor",
        geneInfo.getFullNameFromNomenclatureAuthority());
    assertEquals("O", geneInfo.getNomenclatureStatus());
    assertEquals(2, geneInfo.getOtherDesignations().size());
    assertEquals("coagulation factor II (thrombin) receptor",
        geneInfo.getOtherDesignations().get(0));
    assertEquals("protease-activated receptor 1", geneInfo.getOtherDesignations().get(1));
    assertEquals(LocalDate.of(2016, 2, 7), localDate(geneInfo.getModificationDate()));
    geneInfo = geneInfos.get(2);
    assertEquals(9606, geneInfo.getOrganismId());
    assertEquals(4404, geneInfo.getId());
    assertEquals("MRX39", geneInfo.getSymbol());
    assertNull(geneInfo.getLocusTag());
    assertEquals(0, geneInfo.getSynonyms().size());
    assertEquals(1, geneInfo.getDbXrefs().size());
    assertEquals("HGNC:HGNC:7270", geneInfo.getDbXrefs().get(0));
    assertEquals("X", geneInfo.getChromosome());
    assertNull(geneInfo.getMapLocation());
    assertEquals("mental retardation, X-linked 39", geneInfo.getDescription());
    assertEquals("unknown", geneInfo.getTypeOfGene());
    assertEquals("MRX39", geneInfo.getSymbolFromNomenclatureAuthority());
    assertEquals("mental retardation, X-linked 39",
        geneInfo.getFullNameFromNomenclatureAuthority());
    assertEquals("O", geneInfo.getNomenclatureStatus());
    assertEquals(0, geneInfo.getOtherDesignations().size());
    assertEquals(LocalDate.of(2014, 12, 7), localDate(geneInfo.getModificationDate()));
    geneInfo = geneInfos.get(3);
    assertEquals(741158, geneInfo.getOrganismId());
    assertEquals(8923219, geneInfo.getId());
    assertEquals("16S rRNA", geneInfo.getSymbol());
    assertNull(geneInfo.getLocusTag());
    assertEquals(0, geneInfo.getSynonyms().size());
    assertEquals(0, geneInfo.getDbXrefs().size());
    assertEquals("MT", geneInfo.getChromosome());
    assertNull(geneInfo.getMapLocation());
    assertEquals("l-rRNA", geneInfo.getDescription());
    assertEquals("rRNA", geneInfo.getTypeOfGene());
    assertNull(geneInfo.getSymbolFromNomenclatureAuthority());
    assertNull(geneInfo.getFullNameFromNomenclatureAuthority());
    assertNull(geneInfo.getNomenclatureStatus());
    assertEquals(0, geneInfo.getOtherDesignations().size());
    assertEquals(LocalDate.of(2012, 11, 27), localDate(geneInfo.getModificationDate()));
  }

  @Test
  public void parse() throws Throwable {
    Path file =
        Paths.get(getClass().getResource("/annotation/Homo_sapiens_small.gene_info").toURI());

    try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
      List<GeneInfo> geneInfos = geneInfoParser.parse(reader);

      validateGeneInfos(geneInfos);
    }
  }

  @Test
  public void parse_Empty() throws Throwable {
    try (BufferedReader reader = new BufferedReader(new StringReader(""))) {
      List<GeneInfo> geneInfos = geneInfoParser.parse(reader);

      assertTrue(geneInfos.isEmpty());
    }
  }

  @Test
  public void parse_Consumer() throws Throwable {
    Path file =
        Paths.get(getClass().getResource("/annotation/Homo_sapiens_small.gene_info").toURI());

    try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
      geneInfoParser.parse(reader, handler);

      verify(handler, times(4)).accept(geneInfoCaptor.capture());
      List<GeneInfo> geneInfos = geneInfoCaptor.getAllValues();
      validateGeneInfos(geneInfos);
    }
  }

  @Test
  public void parse_BiConsumer() throws Throwable {
    Path file =
        Paths.get(getClass().getResource("/annotation/Homo_sapiens_small.gene_info").toURI());
    List<String> expectedLines = Files.readAllLines(file);

    try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
      geneInfoParser.parse(reader, biHandler);

      verify(biHandler, times(4)).accept(geneInfoCaptor.capture(), lineCaptor.capture());
      List<GeneInfo> geneInfos = geneInfoCaptor.getAllValues();
      final List<String> lines = lineCaptor.getAllValues();
      validateGeneInfos(geneInfos);
      assertEquals(expectedLines.get(1), lines.get(0));
      assertEquals(expectedLines.get(2), lines.get(1));
      assertEquals(expectedLines.get(3), lines.get(2));
      assertEquals(expectedLines.get(4), lines.get(3));
    }
  }

  @Test
  public void parse_Consumer_Empty() throws Throwable {
    try (BufferedReader reader = new BufferedReader(new StringReader(""))) {
      geneInfoParser.parse(reader, handler);

      verify(handler, never()).accept(any(GeneInfo.class));
    }
  }
}
