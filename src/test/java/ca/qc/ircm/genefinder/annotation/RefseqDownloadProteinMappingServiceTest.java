package ca.qc.ircm.genefinder.annotation;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.ftp.FtpService;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class RefseqDownloadProteinMappingServiceTest {
  private static final int SEARCH_COUNT = 1231;

  private RefseqDownloadProteinMappingService refseqDownloadProteinMappingService;
  @Mock
  private NcbiConfiguration ncbiConfiguration;
  @Mock
  private FtpService ftpService;
  @Mock
  private ProteinService proteinService;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private ProgressBar progressBar;
  @Mock
  private FTPClient ftpClient;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.getDefault();
  private Path download;
  private String eutils = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils";
  private String ftp = "ftp.ncbi.nlm.nih.gov";
  private String gene2accession = "/gene/DATA/gene2refseq.gz";
  private String geneInfo = "/gene/DATA/gene_info.gz";
  private String refseqSequences = "/refseq/release/complete";
  private Pattern refseqSequencesFilenamePattern = Pattern.compile(".+\\.protein\\.faa\\.gz");

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    refseqDownloadProteinMappingService =
        new RefseqDownloadProteinMappingService(ncbiConfiguration, ftpService, proteinService);
    download = Files.createDirectory(temporaryFolder.getRoot().toPath().resolve("download"));
    when(ncbiConfiguration.eutils()).thenReturn(eutils);
    when(ncbiConfiguration.ftp()).thenReturn(ftp);
    when(ncbiConfiguration.gene2accession()).thenReturn(gene2accession);
    when(ncbiConfiguration.geneInfo()).thenReturn(geneInfo);
    when(ncbiConfiguration.refseqSequences()).thenReturn(refseqSequences);
    when(ncbiConfiguration.refseqSequencesFilenamePattern())
        .thenReturn(refseqSequencesFilenamePattern);
    when(ftpService.anonymousConnect(anyString())).thenReturn(ftpClient);
    when(progressBar.step(anyDouble())).thenReturn(progressBar);
  }

  private void gzip(Path input, Path output) throws IOException {
    try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(output))) {
      Files.copy(input, out);
    }
  }

  private String parseSequence(Path fasta, int sequenceIndex) throws IOException {
    List<String> lines = Files.readAllLines(fasta);
    int sequenceCount = -1;
    int lineIndex = 0;
    while (lineIndex < lines.size() && sequenceCount < sequenceIndex) {
      if (lines.get(lineIndex++).startsWith(">")) {
        sequenceCount++;
      }
    }
    StringBuilder sequence = new StringBuilder();
    while (lineIndex < lines.size() && !lines.get(lineIndex).startsWith(">")) {
      sequence.append(lines.get(lineIndex++));
    }
    return sequence.toString();
  }

  @Test
  public void downloadProteinMappings() throws Throwable {
    List<String> proteinIds =
        Files.readAllLines(Paths.get(getClass().getResource("/annotation/accessions.txt").toURI()));

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(parameters, atLeastOnce()).isGeneId();
    verify(parameters, atLeastOnce()).isGeneName();
    verify(parameters, atLeastOnce()).isGeneSummary();
    verify(parameters, atLeastOnce()).isGeneSynonyms();
    verify(parameters, atLeastOnce()).isProteinMolecularWeight();
    verify(parameters, atLeastOnce()).isSequence();
    assertEquals(SEARCH_COUNT, mappings.size());
    Set<String> mappingsAccessions = new HashSet<>();
    for (ProteinMapping mapping : mappings) {
      assertTrue(proteinIds.contains(mapping.getProteinId()));
      mappingsAccessions.add(mapping.getProteinId());
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
      assertNull(mapping.getSequence());
      assertNull(mapping.getMolecularWeight());
    }
    assertEquals(SEARCH_COUNT, mappingsAccessions.size());
  }

  @Test
  public void downloadProteinMappings_Gene() throws Throwable {
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    Path localGene2accession = download.resolve("refseq.gene2refseq.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene2refseq").toURI()),
        localGene2accession);
    when(ftpService.localFile(gene2accession)).thenReturn(localGene2accession);
    Path localGeneInfo = download.resolve("refseq.gene_info.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene_info").toURI()), localGeneInfo);
    when(ftpService.localFile(geneInfo)).thenReturn(localGeneInfo);

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(ftpService, times(2)).anonymousConnect(ncbiConfiguration.ftp());
    verify(ftpService).localFile(gene2accession);
    verify(ftpService).downloadFile(ftpClient, gene2accession, localGene2accession, progressBar,
        locale);
    verify(ftpService).localFile(geneInfo);
    verify(ftpService).downloadFile(ftpClient, geneInfo, localGeneInfo, progressBar, locale);
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("NP_001317102.1")) {
        assertNotNull(mapping.getGenes());
        assertEquals(1, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(1L, gene.getId());
        assertEquals("A1BG", gene.getSymbol());
        assertEquals("alpha-1-B glycoprotein", gene.getDescription());
        assertArrayEquals("A1B|ABG|GAB|HYST2477".split("\\|"), gene.getSynonyms().toArray());
      } else {
        assertNotNull(mapping.getGenes());
        assertEquals(1, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(4404L, gene.getId());
        assertEquals("MRX39", gene.getSymbol());
        assertEquals("mental retardation, X-linked 39", gene.getDescription());
        assertEquals(null, gene.getSynonyms());
      }
      assertNull(mapping.getTaxonomyId());
      assertNull(mapping.getSequence());
      assertNull(mapping.getMolecularWeight());
    }
  }

  @Test
  public void downloadProteinMappings_Sequence() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final List<String> proteinIds = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    String remoteSequence1 = "/refseq/refseq1.protein.faa.gz";
    String remoteSequence2 = "/refseq/refseq2.protein.faa.gz";
    List<String> refseqFiles = new ArrayList<>();
    refseqFiles.add(remoteSequence1);
    refseqFiles.add("/refseq/refseq1.protein.gpff.gz");
    refseqFiles.add("/refseq/refseq1.rna.fna.gz");
    refseqFiles.add(remoteSequence2);
    when(ftpService.walkTree(any(), any())).thenReturn(refseqFiles);
    Path refseqSequencesFolder =
        Files.createDirectories(download.resolve(refseqSequences.substring(1)));
    Path sequenceRessource1 =
        Paths.get(getClass().getResource("/annotation/refseq1.protein.faa").toURI());
    Path sequenceRessource2 =
        Paths.get(getClass().getResource("/annotation/refseq2.protein.faa").toURI());
    Path localSequence1 = refseqSequencesFolder.resolve("refseq1.protein.faa.gz");
    gzip(sequenceRessource1, localSequence1);
    when(ftpService.localFile(remoteSequence1)).thenReturn(localSequence1);
    Path localSequence2 = refseqSequencesFolder.resolve("refseq2.protein.faa.gz");
    gzip(sequenceRessource2, localSequence2);
    when(ftpService.localFile(remoteSequence2)).thenReturn(localSequence2);
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(ftpService).anonymousConnect(ncbiConfiguration.ftp());
    verify(ftpService).walkTree(ftpClient, refseqSequences);
    verify(ftpService).localFile(remoteSequence1);
    verify(ftpService).downloadFile(ftpClient, remoteSequence1, localSequence1, progressBar,
        locale);
    verify(ftpService).localFile(remoteSequence2);
    verify(ftpService).downloadFile(ftpClient, remoteSequence2, localSequence2, progressBar,
        locale);
    verify(proteinService).weight(parseSequence(sequenceRessource1, 0));
    verify(proteinService).weight(parseSequence(sequenceRessource2, 1));
    verify(proteinService).weight(parseSequence(sequenceRessource2, 2));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("NP_001317102.1")) {
        assertEquals(parseSequence(sequenceRessource1, 0), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("NP_001317083.1")) {
        assertEquals(parseSequence(sequenceRessource2, 1), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(sequenceRessource2, 2), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }

  @Test
  public void downloadProteinMappings_Gi() throws Throwable {
    List<String> proteinIds =
        Files.readAllLines(Paths.get(getClass().getResource("/annotation/gis.txt").toURI()));

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(parameters, atLeastOnce()).isGeneId();
    verify(parameters, atLeastOnce()).isGeneName();
    verify(parameters, atLeastOnce()).isGeneSummary();
    verify(parameters, atLeastOnce()).isGeneSynonyms();
    verify(parameters, atLeastOnce()).isProteinMolecularWeight();
    verify(parameters, atLeastOnce()).isSequence();
    assertEquals(SEARCH_COUNT, mappings.size());
    Set<String> mappingsAccessions = new HashSet<>();
    for (ProteinMapping mapping : mappings) {
      assertTrue(proteinIds.contains(mapping.getProteinId()));
      mappingsAccessions.add(mapping.getProteinId());
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
      assertNull(mapping.getSequence());
      assertNull(mapping.getMolecularWeight());
    }
    assertEquals(SEARCH_COUNT, mappingsAccessions.size());
  }

  @Test
  public void downloadProteinMappings_Gi_Gene() throws Throwable {
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds =
        Files.readAllLines(Paths.get(getClass().getResource("/annotation/gis3.txt").toURI()));
    Path localGene2accession = download.resolve("refseq.gene2refseq.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene2refseq").toURI()),
        localGene2accession);
    when(ftpService.localFile(gene2accession)).thenReturn(localGene2accession);
    Path localGeneInfo = download.resolve("refseq.gene_info.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene_info").toURI()), localGeneInfo);
    when(ftpService.localFile(geneInfo)).thenReturn(localGeneInfo);

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(ftpService, times(2)).anonymousConnect(ncbiConfiguration.ftp());
    verify(ftpService).localFile(gene2accession);
    verify(ftpService).downloadFile(ftpClient, gene2accession, localGene2accession, progressBar,
        locale);
    verify(ftpService).localFile(geneInfo);
    verify(ftpService).downloadFile(ftpClient, geneInfo, localGeneInfo, progressBar, locale);
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("829098688")) {
        assertNotNull(mapping.getGenes());
        assertEquals(1, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(1L, gene.getId());
        assertEquals("A1BG", gene.getSymbol());
        assertEquals("alpha-1-B glycoprotein", gene.getDescription());
        assertArrayEquals("A1B|ABG|GAB|HYST2477".split("\\|"), gene.getSynonyms().toArray());
      } else {
        assertNotNull(mapping.getGenes());
        assertEquals(1, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(4404L, gene.getId());
        assertEquals("MRX39", gene.getSymbol());
        assertEquals("mental retardation, X-linked 39", gene.getDescription());
        assertEquals(null, gene.getSynonyms());
      }
      assertNull(mapping.getTaxonomyId());
      assertNull(mapping.getSequence());
      assertNull(mapping.getMolecularWeight());
    }
  }

  @Test
  public void downloadProteinMappings_Gi_Sequence() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final List<String> proteinIds =
        Files.readAllLines(Paths.get(getClass().getResource("/annotation/gis3.txt").toURI()));
    String remoteSequence1 = "/refseq/refseq1.protein.faa.gz";
    String remoteSequence2 = "/refseq/refseq2.protein.faa.gz";
    List<String> refseqFiles = new ArrayList<>();
    refseqFiles.add(remoteSequence1);
    refseqFiles.add("/refseq/refseq1.protein.gpff.gz");
    refseqFiles.add("/refseq/refseq1.rna.fna.gz");
    refseqFiles.add(remoteSequence2);
    when(ftpService.walkTree(any(), any())).thenReturn(refseqFiles);
    Path refseqSequencesFolder =
        Files.createDirectories(download.resolve(refseqSequences.substring(1)));
    Path sequenceRessource1 =
        Paths.get(getClass().getResource("/annotation/refseq1.protein.faa").toURI());
    Path sequenceRessource2 =
        Paths.get(getClass().getResource("/annotation/refseq2.protein.faa").toURI());
    Path localSequence1 = refseqSequencesFolder.resolve("refseq1.protein.faa.gz");
    gzip(sequenceRessource1, localSequence1);
    when(ftpService.localFile(remoteSequence1)).thenReturn(localSequence1);
    Path localSequence2 = refseqSequencesFolder.resolve("refseq2.protein.faa.gz");
    gzip(sequenceRessource2, localSequence2);
    when(ftpService.localFile(remoteSequence2)).thenReturn(localSequence2);
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(ftpService).anonymousConnect(ncbiConfiguration.ftp());
    verify(ftpService).walkTree(ftpClient, refseqSequences);
    verify(ftpService).localFile(remoteSequence1);
    verify(ftpService).downloadFile(ftpClient, remoteSequence1, localSequence1, progressBar,
        locale);
    verify(ftpService).localFile(remoteSequence2);
    verify(ftpService).downloadFile(ftpClient, remoteSequence2, localSequence2, progressBar,
        locale);
    verify(proteinService).weight(parseSequence(sequenceRessource1, 0));
    verify(proteinService).weight(parseSequence(sequenceRessource2, 1));
    verify(proteinService).weight(parseSequence(sequenceRessource2, 2));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("829098688")) {
        assertEquals(parseSequence(sequenceRessource1, 0), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("829098686")) {
        assertEquals(parseSequence(sequenceRessource2, 1), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(sequenceRessource2, 2), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }
}
