package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.ftp.FtpService;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.rest.RestClientFactory;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UniprotDownloadProteinMappingServiceTest {
  private static final int SEARCH_COUNT = 20168;
  private UniprotDownloadProteinMappingService uniprotDownloadProteinMappingService;
  @Mock
  private UniprotConfiguration uniprotConfiguration;
  @Mock
  private NcbiConfiguration ncbiConfiguration;
  @Mock
  private RestClientFactory restClientFactory;
  @Mock
  private FtpService ftpService;
  @Mock
  private ProteinService proteinService;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private ProgressBar progressBar;
  @Mock
  private Organism organism;
  @Mock
  private Client clientSearch;
  @Mock
  private WebTarget targetSearch;
  @Mock
  private Invocation.Builder invocationSearch;
  @Mock
  private FTPClient ftpClient;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.getDefault();
  private Path download;
  private String ftp = "ftp.uniprot.org";
  private String search = "http://www.uniprot.org/uniprot";
  private String idmapping =
      "/pub/databases/uniprot/current_release/knowledgebase/idmapping/idmapping.dat.gz";
  private String geneMapping = "GeneID";
  private String ncbiFtp = "ftp.ncbi.nlm.nih.gov";
  private String geneInfo = "/gene/DATA/gene_info.gz";
  private String swissprotFasta =
      "/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.fasta.gz";
  private String tremblFasta =
      "/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_trembl.fasta.gz";

  @Before
  public void beforeTest() throws Throwable {
    uniprotDownloadProteinMappingService = new UniprotDownloadProteinMappingService(
        uniprotConfiguration, ncbiConfiguration, restClientFactory, ftpService, proteinService);
    download = Files.createDirectory(temporaryFolder.getRoot().toPath().resolve("download"));
    when(uniprotConfiguration.ftp()).thenReturn(ftp);
    when(uniprotConfiguration.search()).thenReturn(search);
    when(uniprotConfiguration.idmapping()).thenReturn(idmapping);
    when(uniprotConfiguration.geneMapping()).thenReturn(geneMapping);
    when(uniprotConfiguration.swissprotFasta()).thenReturn(swissprotFasta);
    when(uniprotConfiguration.tremblFasta()).thenReturn(tremblFasta);
    when(ncbiConfiguration.ftp()).thenReturn(ncbiFtp);
    when(ncbiConfiguration.geneInfo()).thenReturn(geneInfo);
    when(restClientFactory.createClient()).thenReturn(clientSearch);
    when(clientSearch.target(anyString())).thenReturn(targetSearch);
    when(targetSearch.path(anyString())).thenReturn(targetSearch);
    when(targetSearch.queryParam(anyString(), anyVararg())).thenReturn(targetSearch);
    when(targetSearch.request()).thenReturn(invocationSearch);
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
    int organismId = 9606;
    when(organism.getId()).thenReturn(organismId);
    when(parameters.getOrganism()).thenReturn(organism);
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/uniprot-accessions.txt");
    List<String> proteinsIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions.txt").toURI()));
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);

    List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

    verify(parameters, atLeastOnce()).getOrganism();
    verify(parameters, atLeastOnce()).getProteinDatabase();
    verify(parameters, atLeastOnce()).isGeneId();
    verify(parameters, atLeastOnce()).isGeneName();
    verify(parameters, atLeastOnce()).isGeneSummary();
    verify(parameters, atLeastOnce()).isGeneSynonyms();
    verify(parameters, atLeastOnce()).isProteinMolecularWeight();
    verify(parameters, atLeastOnce()).isSequence();
    verify(organism, atLeastOnce()).getId();
    verify(restClientFactory).createClient();
    verify(clientSearch).target(search);
    verify(targetSearch).queryParam("query", "organism:9606");
    verify(targetSearch).queryParam("format", "list");
    verify(targetSearch).request();
    verify(invocationSearch).get(InputStream.class);
    assertEquals(SEARCH_COUNT, mappings.size());
    Set<String> mappingsAccessions = new HashSet<>();
    for (ProteinMapping mapping : mappings) {
      assertTrue(proteinsIds.contains(mapping.getProteinId()));
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
    int organismId = 9606;
    when(organism.getId()).thenReturn(organismId);
    when(parameters.getOrganism()).thenReturn(organism);
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.REFSEQ);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/uniprot-accessions3.txt");
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);
    Path localIdMapping = download.resolve("idmapping.gz");
    gzip(Paths.get(getClass().getResource("/annotation/idmapping").toURI()), localIdMapping);
    when(ftpService.localFile(idmapping)).thenReturn(localIdMapping);
    Path localGeneInfo = download.resolve("refseq.gene_info.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene_info").toURI()), localGeneInfo);
    when(ftpService.localFile(geneInfo)).thenReturn(localGeneInfo);

    List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

    verify(ftpService).anonymousConnect(uniprotConfiguration.ftp());
    verify(ftpService).anonymousConnect(ncbiConfiguration.ftp());
    verify(ftpService).localFile(idmapping);
    verify(ftpService).downloadFile(ftpClient, idmapping, localIdMapping, progressBar, locale);
    verify(ftpService).localFile(geneInfo);
    verify(ftpService).downloadFile(ftpClient, geneInfo, localGeneInfo, progressBar, locale);
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("A0A075B759")) {
        assertNotNull(mapping.getGenes());
        assertEquals(2, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(1L, gene.getId());
        assertEquals("A1BG", gene.getSymbol());
        assertEquals("alpha-1-B glycoprotein", gene.getDescription());
        assertArrayEquals("A1B|ABG|GAB|HYST2477".split("\\|"), gene.getSynonyms().toArray());
        gene = mapping.getGenes().get(1);
        assertEquals(2149L, gene.getId());
        assertEquals("F2R", gene.getSymbol());
        assertEquals("coagulation factor II thrombin receptor", gene.getDescription());
        assertArrayEquals("CF2R|HTR|PAR-1|PAR1|TR".split("\\|"), gene.getSynonyms().toArray());
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
    int organismId = 9606;
    when(organism.getId()).thenReturn(organismId);
    when(parameters.getOrganism()).thenReturn(organism);
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/uniprot-accessions3.txt");
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);
    Path swissprotRessource =
        Paths.get(getClass().getResource("/annotation/swissprot.fasta").toURI());
    Path localSwissprot = download.resolve("swissprot.fasta.gz");
    gzip(swissprotRessource, localSwissprot);
    when(ftpService.localFile(swissprotFasta)).thenReturn(localSwissprot);
    Path tremblRessource = Paths.get(getClass().getResource("/annotation/trembl.fasta").toURI());
    Path localTrembl = download.resolve("trembl.fasta.gz");
    gzip(tremblRessource, localTrembl);
    when(ftpService.localFile(tremblFasta)).thenReturn(localTrembl);
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

    verify(ftpService).anonymousConnect(uniprotConfiguration.ftp());
    verify(ftpService).localFile(swissprotFasta);
    verify(ftpService).downloadFile(ftpClient, swissprotFasta, localSwissprot, progressBar, locale);
    verify(ftpService).localFile(tremblFasta);
    verify(ftpService).downloadFile(ftpClient, tremblFasta, localTrembl, progressBar, locale);
    verify(proteinService).weight(parseSequence(swissprotRessource, 1));
    verify(proteinService).weight(parseSequence(tremblRessource, 1));
    verify(proteinService).weight(parseSequence(tremblRessource, 0));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("A0A075B759")) {
        assertEquals(parseSequence(swissprotRessource, 1), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("A0AV96")) {
        assertEquals(parseSequence(tremblRessource, 1), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(tremblRessource, 0), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }

  @Test
  public void downloadProteinMappings_Gene_Sequence() throws Throwable {
    int organismId = 9606;
    when(organism.getId()).thenReturn(organismId);
    when(parameters.getOrganism()).thenReturn(organism);
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/uniprot-accessions3.txt");
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);
    Path localIdMapping = download.resolve("idmapping.gz");
    gzip(Paths.get(getClass().getResource("/annotation/idmapping").toURI()), localIdMapping);
    when(ftpService.localFile(idmapping)).thenReturn(localIdMapping);
    Path localGeneInfo = download.resolve("refseq.gene_info.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene_info").toURI()), localGeneInfo);
    when(ftpService.localFile(geneInfo)).thenReturn(localGeneInfo);
    Path swissprotRessource =
        Paths.get(getClass().getResource("/annotation/swissprot.fasta").toURI());
    Path localSwissprot = download.resolve("swissprot.fasta.gz");
    gzip(swissprotRessource, localSwissprot);
    when(ftpService.localFile(swissprotFasta)).thenReturn(localSwissprot);
    Path tremblRessource = Paths.get(getClass().getResource("/annotation/trembl.fasta").toURI());
    Path localTrembl = download.resolve("trembl.fasta.gz");
    gzip(tremblRessource, localTrembl);
    when(ftpService.localFile(tremblFasta)).thenReturn(localTrembl);
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

    verify(proteinService).weight(parseSequence(swissprotRessource, 1));
    verify(proteinService).weight(parseSequence(tremblRessource, 1));
    verify(proteinService).weight(parseSequence(tremblRessource, 0));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("A0A075B759")) {
        assertNotNull(mapping.getGenes());
        assertEquals(2, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(1L, gene.getId());
        assertEquals("A1BG", gene.getSymbol());
        assertEquals("alpha-1-B glycoprotein", gene.getDescription());
        assertArrayEquals("A1B|ABG|GAB|HYST2477".split("\\|"), gene.getSynonyms().toArray());
        gene = mapping.getGenes().get(1);
        assertEquals(2149L, gene.getId());
        assertEquals("F2R", gene.getSymbol());
        assertEquals("coagulation factor II thrombin receptor", gene.getDescription());
        assertArrayEquals("CF2R|HTR|PAR-1|PAR1|TR".split("\\|"), gene.getSynonyms().toArray());
        assertEquals(parseSequence(swissprotRessource, 1), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("A0AV96")) {
        assertNotNull(mapping.getGenes());
        assertEquals(1, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(4404L, gene.getId());
        assertEquals("MRX39", gene.getSymbol());
        assertEquals("mental retardation, X-linked 39", gene.getDescription());
        assertEquals(null, gene.getSynonyms());
        assertEquals(parseSequence(tremblRessource, 1), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      } else {
        assertNotNull(mapping.getGenes());
        assertEquals(1, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(4404L, gene.getId());
        assertEquals("MRX39", gene.getSymbol());
        assertEquals("mental retardation, X-linked 39", gene.getDescription());
        assertEquals(null, gene.getSynonyms());
        assertEquals(parseSequence(tremblRessource, 0), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
    }
  }

  @Test
  public void downloadProteinMappings_Swissprot() throws Throwable {
    int organismId = 9606;
    when(organism.getId()).thenReturn(organismId);
    when(parameters.getOrganism()).thenReturn(organism);
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.SWISSPROT);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/uniprot-accessions.txt");
    List<String> proteinsIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions.txt").toURI()));
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);

    List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

    verify(parameters, atLeastOnce()).getOrganism();
    verify(parameters, atLeastOnce()).getProteinDatabase();
    verify(parameters, atLeastOnce()).isGeneId();
    verify(parameters, atLeastOnce()).isGeneName();
    verify(parameters, atLeastOnce()).isGeneSummary();
    verify(parameters, atLeastOnce()).isGeneSynonyms();
    verify(parameters, atLeastOnce()).isProteinMolecularWeight();
    verify(parameters, atLeastOnce()).isSequence();
    verify(organism, atLeastOnce()).getId();
    verify(restClientFactory).createClient();
    verify(clientSearch).target(search);
    verify(targetSearch).queryParam("query", "organism:9606+AND+reviewed:yes");
    verify(targetSearch).queryParam("format", "list");
    verify(targetSearch).request();
    verify(invocationSearch).get(InputStream.class);
    assertEquals(SEARCH_COUNT, mappings.size());
    Set<String> mappingsAccessions = new HashSet<>();
    for (ProteinMapping mapping : mappings) {
      assertTrue(proteinsIds.contains(mapping.getProteinId()));
      mappingsAccessions.add(mapping.getProteinId());
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
      assertNull(mapping.getSequence());
      assertNull(mapping.getMolecularWeight());
    }
    assertEquals(SEARCH_COUNT, mappingsAccessions.size());
  }

  @Test
  public void downloadProteinMappings_Swissprot_Gene() throws Throwable {
    int organismId = 9606;
    when(organism.getId()).thenReturn(organismId);
    when(parameters.getOrganism()).thenReturn(organism);
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.SWISSPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/uniprot-accessions3.txt");
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);
    Path localIdMapping = download.resolve("idmapping.gz");
    gzip(Paths.get(getClass().getResource("/annotation/idmapping").toURI()), localIdMapping);
    when(ftpService.localFile(idmapping)).thenReturn(localIdMapping);
    Path localGeneInfo = download.resolve("refseq.gene_info.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene_info").toURI()), localGeneInfo);
    when(ftpService.localFile(geneInfo)).thenReturn(localGeneInfo);

    List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

    verify(ftpService).anonymousConnect(uniprotConfiguration.ftp());
    verify(ftpService).anonymousConnect(ncbiConfiguration.ftp());
    verify(ftpService).localFile(idmapping);
    verify(ftpService).downloadFile(ftpClient, idmapping, localIdMapping, progressBar, locale);
    verify(ftpService).localFile(geneInfo);
    verify(ftpService).downloadFile(ftpClient, geneInfo, localGeneInfo, progressBar, locale);
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("A0A075B759")) {
        assertNotNull(mapping.getGenes());
        assertEquals(2, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(1L, gene.getId());
        assertEquals("A1BG", gene.getSymbol());
        assertEquals("alpha-1-B glycoprotein", gene.getDescription());
        assertArrayEquals("A1B|ABG|GAB|HYST2477".split("\\|"), gene.getSynonyms().toArray());
        gene = mapping.getGenes().get(1);
        assertEquals(2149L, gene.getId());
        assertEquals("F2R", gene.getSymbol());
        assertEquals("coagulation factor II thrombin receptor", gene.getDescription());
        assertArrayEquals("CF2R|HTR|PAR-1|PAR1|TR".split("\\|"), gene.getSynonyms().toArray());
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
  public void downloadProteinMappings_Swissprot_Sequence() throws Throwable {
    int organismId = 9606;
    when(organism.getId()).thenReturn(organismId);
    when(parameters.getOrganism()).thenReturn(organism);
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.SWISSPROT);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/uniprot-accessions3.txt");
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);
    Path swissprotRessource =
        Paths.get(getClass().getResource("/annotation/swissprot.fasta").toURI());
    Path localSwissprot = download.resolve("swissprot.fasta.gz");
    gzip(swissprotRessource, localSwissprot);
    when(ftpService.localFile(swissprotFasta)).thenReturn(localSwissprot);
    double sequenceWeight1 = 127.3;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1);

    List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

    verify(ftpService).anonymousConnect(uniprotConfiguration.ftp());
    verify(ftpService).localFile(swissprotFasta);
    verify(ftpService).downloadFile(ftpClient, swissprotFasta, localSwissprot, progressBar, locale);
    verify(proteinService).weight(parseSequence(swissprotRessource, 1));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("A0A075B759")) {
        assertEquals(parseSequence(swissprotRessource, 1), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else {
        assertNull(mapping.getSequence());
        assertNull(mapping.getMolecularWeight());
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }
}
