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

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.UNIPROT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import java.io.File;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@ServiceTestAnnotations
public class TextProteinParserTest {
  private TextProteinParser textProteinParser;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private NcbiConfiguration realNcbiConfiguration;
  @Mock
  private UniprotConfiguration uniprotConfiguration;
  @Inject
  private UniprotConfiguration realUniprotConfiguration;

  /**
   * Before test.
   */
  @BeforeEach
  @SuppressWarnings("checkstyle:linelength")
  public void beforeTest() {
    textProteinParser = new TextProteinParser(ncbiConfiguration, uniprotConfiguration);
    when(ncbiConfiguration.refseqProteinAccessionPattern())
        .thenReturn(realNcbiConfiguration.refseqProteinAccessionPattern());
    when(ncbiConfiguration.refseqProteinGiPattern())
        .thenReturn(realNcbiConfiguration.refseqProteinGiPattern());
    when(uniprotConfiguration.proteinIdPattern())
        .thenReturn(realUniprotConfiguration.proteinIdPattern());
  }

  @Test
  public void parseProteinIds() throws Throwable {
    final File input = new File(getClass().getResource("/data/data.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

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
    final File input = new File(getClass().getResource("/data/data_many.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

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
  public void parseProteinIds_MultipleLines_Commas() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many_commas.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

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
    final File input = new File(getClass().getResource("/data/data_manycolumns.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

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
    final File input = new File(getClass().getResource("/data/data_nogi.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

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
    final File input = new File(getClass().getResource("/data/data_nogi_many.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

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
    final File input = new File(getClass().getResource("/data/data_nogi_manycolumns.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

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
    final File input = new File(getClass().getResource("/data/data_uniprot.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

    assertEquals(2, ids.size());
    assertTrue(ids.contains("P11171"));
    assertTrue(ids.contains("Q08211"));
  }

  @Test
  public void parseProteinIds_Refseq() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_refseq.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

    assertEquals(2, ids.size());
    assertTrue(ids.contains("NP_001159477.1"));
    assertTrue(ids.contains("NP_001348.2"));
  }

  @Test
  public void parseProteinIds_RefseqWrongDatabase() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_refseq.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

    assertEquals(0, ids.size());
  }

  @Test
  public void parseProteinIds_Scaffold() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_scaffold.txt").toURI());
    when(parameters.getProteinColumn()).thenReturn(0);
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);

    List<String> ids = textProteinParser.parseProteinIds(input, parameters);

    assertEquals(6, ids.size());
    assertTrue(ids.contains("119627830"));
    assertTrue(ids.contains("119580583"));
    assertTrue(ids.contains("108250308"));
    assertTrue(ids.contains("100913206"));
    assertTrue(ids.contains("269849686"));
    assertTrue(ids.contains("119580714"));
  }
}
