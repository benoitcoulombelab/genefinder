package ca.qc.ircm.genefinder.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.test.config.Rules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

public class TextDataWriterTest {
  private TextDataWriter textDataWriter;
  @Mock
  private FindGenesParameters parameters;
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public RuleChain rules = Rules.defaultRules(this).around(temporaryFolder);

  @Before
  public void beforeTest() {
    textDataWriter = new TextDataWriter();
  }

  @Test
  public void writeGene() throws Throwable {
    final File input = new File(getClass().getResource("/data/data.txt").toURI());
    final File output = temporaryFolder.newFile();
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
  public void writeGene_Many() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_many.txt").toURI());
    final File output = temporaryFolder.newFile();
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
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          columns[4]);
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
  public void writeGene_ManyInDifferentColumns() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_manycolumns.txt").toURI());
    final File output = temporaryFolder.newFile();
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
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          columns[4]);
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
  public void writeGene_NoGi() throws Throwable {
    final File input = new File(getClass().getResource("/data/data_nogi.txt").toURI());
    final File output = temporaryFolder.newFile();
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
    final File output = temporaryFolder.newFile();
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
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          columns[4]);
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
    final File output = temporaryFolder.newFile();
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
      assertEquals(
          "This gene encodes the largest subunit of RNA polymerase II;This gene encodes the smallest subunit of RNA polymerase II",
          columns[4]);
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
    final File output = temporaryFolder.newFile();
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
      assertEquals("sp|Q08211", columns[0]);
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
    final File output = temporaryFolder.newFile();
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
}
