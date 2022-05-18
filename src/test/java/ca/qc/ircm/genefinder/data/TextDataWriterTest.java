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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.GeneInfo;
import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;

@ServiceTestAnnotations
public class TextDataWriterTest {
  private TextDataWriter textDataWriter;
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
  @TempDir
  File temporaryFolder;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    textDataWriter = new TextDataWriter(ncbiConfiguration, uniprotConfiguration);
    when(ncbiConfiguration.refseqProteinAccessionPattern())
        .thenReturn(realNcbiConfiguration.refseqProteinAccessionPattern());
    when(ncbiConfiguration.refseqProteinGiPattern())
        .thenReturn(realNcbiConfiguration.refseqProteinGiPattern());
    when(uniprotConfiguration.proteinIdPattern())
        .thenReturn(realUniprotConfiguration.proteinIdPattern());
  }

  @Test
  public void writeGene() throws Throwable {
    final File input = new File(getClass().getResource("/data/data.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119627830", columns[0]);
      assertEquals("1234", columns[1]);
      assertEquals("POLR2A", columns[2]);
      assertEquals("RPB1|RPO2A", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_ManyGenesForProtein() throws Throwable {
    final File input = new File(getClass().getResource("/data/data.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119627830", columns[0]);
      assertEquals("1234;4567", columns[1]);
      assertEquals("POLR2A;POLR2B", columns[2]);
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II;"
          + "This gene encodes the smallest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_MultipleLines() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119627830;gi|189054652", columns[0]);
      assertEquals("1234;4567", columns[1]);
      assertEquals("POLR2A;POLR2B", columns[2]);
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II;"
          + "This gene encodes the smallest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0;3.4", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_MultipleLines_Commas() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many_commas.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119627830,gi|189054652", columns[0]);
      assertEquals("1234;4567", columns[1]);
      assertEquals("POLR2A;POLR2B", columns[2]);
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II;"
          + "This gene encodes the smallest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0;3.4", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_MultipleLinesInDifferentColumns() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_manycolumns.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119627830;gi|189054652", columns[0]);
      assertEquals("1234;4567", columns[1]);
      assertEquals("POLR2A;POLR2B", columns[2]);
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II;"
          + "This gene encodes the smallest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0;3.4", columns[5]);
      assertEquals("gi|119621462", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("gi|119572880", columns[6]);
    }
  }

  @Test
  public void writeGene_MultipleLinesWithManyGenesForProtein() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119627830;gi|189054652", columns[0]);
      assertEquals("1234;4567;4568", columns[1]);
      assertEquals("POLR2A;POLR2B;POLR2C", columns[2]);
      assertEquals("RPB1|RPO2A;RPB2|RPO2B;RPB3|RPO2C", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II;"
          + "This gene encodes the smallest subunit of RNA polymerase II;"
          + "This gene encodes the second smallest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0;3.4", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_NoGi() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("119627830", columns[0]);
      assertEquals("1234", columns[1]);
      assertEquals("POLR2A", columns[2]);
      assertEquals("RPB1|RPO2A", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_ManyNoGi() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi_many.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("119627830;189054652", columns[0]);
      assertEquals("1234;4567", columns[1]);
      assertEquals("POLR2A;POLR2B", columns[2]);
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II;"
          + "This gene encodes the smallest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0;3.4", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_ManyNoGiInDifferentColumns() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi_manycolumns.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("119627830;189054652", columns[0]);
      assertEquals("1234;4567", columns[1]);
      assertEquals("POLR2A;POLR2B", columns[2]);
      assertEquals("RPB1|RPO2A;RPB2|RPO2B", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II;"
          + "This gene encodes the smallest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0;3.4", columns[5]);
      assertEquals("119621462", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("119580583", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("119572880", columns[6]);
    }
  }

  @Test
  public void writeGene_Uniprot() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_uniprot.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("sp|P11171", columns[0]);
      assertEquals("1234", columns[1]);
      assertEquals("POLR2A", columns[2]);
      assertEquals("RPB1|RPO2A", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("tr|Q08211", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_Refseq() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_refseq.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("ref|NP_001159477.1", columns[0]);
      assertEquals("1234", columns[1]);
      assertEquals("POLR2A", columns[2]);
      assertEquals("RPB1|RPO2A", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("ref|NP_001348.2", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }

  @Test
  public void writeGene_Scaffold() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_scaffold.txt").toURI());
    final File output = new File(temporaryFolder, "data.txt");
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

    textDataWriter.writeGene(input, output, parameters, mappings);

    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(output)))) {
      String line;
      line = reader.readLine();
      assertNotNull(line);
      String[] columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("human", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119627830 (+1)", columns[0]);
      assertEquals("1234", columns[1]);
      assertEquals("POLR2A", columns[2]);
      assertEquals("RPB1|RPO2A", columns[3]);
      assertEquals("This gene encodes the largest subunit of RNA polymerase II", columns[4]);
      assertEquals("20.0", columns[5]);
      assertEquals("", columns[6]);
      line = reader.readLine();
      columns = line.split("\t", -1);
      assertEquals(7, columns.length);
      assertEquals("gi|119580583 (+4)", columns[0]);
      assertEquals("", columns[1]);
      assertEquals("", columns[2]);
      assertEquals("", columns[3]);
      assertEquals("", columns[4]);
      assertEquals("", columns[5]);
      assertEquals("", columns[6]);
    }
  }
}
