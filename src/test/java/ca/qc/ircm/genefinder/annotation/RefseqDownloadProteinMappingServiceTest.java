package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.ApplicationConfiguration;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class RefseqDownloadProteinMappingServiceTest {
  private static final int SEARCH_COUNT = 1231;
  private static final int MAX_IDS_PER_REQUEST = 500;
  private static final int FETCH_COUNT =
      (int) Math.ceil((double) SEARCH_COUNT / MAX_IDS_PER_REQUEST);
  private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

  private RefseqDownloadProteinMappingService refseqDownloadProteinMappingService;
  @Mock
  private ApplicationConfiguration applicationConfiguration;
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
  private Client clientFetchIds;
  @Mock
  private WebTarget targetSearch;
  @Mock
  private WebTarget targetFetchIds;
  @Mock
  private Invocation.Builder invocationSearch;
  @Mock
  private Invocation.Builder invocationFetchIds;
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

  @Before
  public void beforeTest() throws Throwable {
    refseqDownloadProteinMappingService = new RefseqDownloadProteinMappingService(
        applicationConfiguration, ncbiConfiguration, restClientFactory, ftpService, proteinService);
    download = Files.createDirectory(temporaryFolder.getRoot().toPath().resolve("download"));
    when(applicationConfiguration.download()).thenReturn(download);
    when(ncbiConfiguration.eutils()).thenReturn(eutils);
    when(ncbiConfiguration.maxIdsPerRequest()).thenReturn(MAX_IDS_PER_REQUEST);
    when(ncbiConfiguration.ftp()).thenReturn(ftp);
    when(ncbiConfiguration.gene2accession()).thenReturn(gene2accession);
    when(ncbiConfiguration.geneInfo()).thenReturn(geneInfo);
    when(ncbiConfiguration.refseqSequences()).thenReturn(refseqSequences);
    when(ncbiConfiguration.refseqSequencesFilenamePattern())
        .thenReturn(refseqSequencesFilenamePattern);
    when(restClientFactory.createClient()).thenReturn(clientSearch, clientFetchIds);
    when(clientSearch.target(anyString())).thenReturn(targetSearch);
    when(targetSearch.path(anyString())).thenReturn(targetSearch);
    when(targetSearch.queryParam(anyString(), anyVararg())).thenReturn(targetSearch);
    when(targetSearch.request()).thenReturn(invocationSearch);
    when(clientFetchIds.target(anyString())).thenReturn(targetFetchIds);
    when(targetFetchIds.path(anyString())).thenReturn(targetFetchIds);
    when(targetFetchIds.queryParam(anyString(), anyVararg())).thenReturn(targetFetchIds);
    when(targetFetchIds.request()).thenReturn(invocationFetchIds);
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
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.REFSEQ);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/esearch.fcgi.xml");
    List<String> accessions =
        Files.readAllLines(Paths.get(getClass().getResource("/annotation/accessions.txt").toURI()));
    InputStream[] accessionInputs = new InputStream[FETCH_COUNT];
    IntStream.range(0, FETCH_COUNT).forEach(i -> {
      int accsIndex = i * MAX_IDS_PER_REQUEST;
      List<String> accs = accessions.subList(accsIndex,
          Math.min(accsIndex + MAX_IDS_PER_REQUEST, accessions.size()));
      String accsAsString = accs.stream().collect(Collectors.joining("\n"));
      accessionInputs[i] = new ByteArrayInputStream(accsAsString.getBytes(UTF_8_CHARSET));
    });
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);
    when(invocationFetchIds.get(InputStream.class)).thenReturn(accessionInputs[0],
        Arrays.copyOfRange(accessionInputs, 1, accessionInputs.length));

    List<ProteinMapping> mappings = refseqDownloadProteinMappingService
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
    verify(restClientFactory, times(2)).createClient();
    verify(clientSearch).target(eutils);
    verify(targetSearch).path("esearch.fcgi");
    verify(targetSearch).queryParam("db", "protein");
    verify(targetSearch).queryParam("term", "txid" + organismId + "[Organism] AND refseq[filter]");
    verify(targetSearch).queryParam("usehistory", "y");
    verify(targetSearch).request();
    verify(invocationSearch).get(InputStream.class);
    verify(clientFetchIds).target(eutils);
    verify(targetFetchIds).path("efetch.fcgi");
    verify(targetFetchIds).queryParam("db", "protein");
    verify(targetFetchIds).queryParam("WebEnv",
        "NCID_1_174988986_130.14.22.215_9001_1434470567_1270571334_0MetA0_S_MegaStore_F_1");
    verify(targetFetchIds).queryParam("query_key", "1");
    verify(targetFetchIds).queryParam("rettype", "acc");
    verify(targetFetchIds).queryParam("retmax", MAX_IDS_PER_REQUEST);
    for (int i = 0; i < SEARCH_COUNT; i += MAX_IDS_PER_REQUEST) {
      verify(targetFetchIds).queryParam("retstart", i);
    }
    verify(targetFetchIds, times(FETCH_COUNT)).request();
    verify(invocationFetchIds, times(FETCH_COUNT)).get(InputStream.class);
    assertEquals(SEARCH_COUNT, mappings.size());
    Set<String> mappingsAccessions = new HashSet<>();
    for (ProteinMapping mapping : mappings) {
      assertTrue(accessions.contains(mapping.getProteinId()));
      mappingsAccessions.add(mapping.getProteinId());
      assertNull(mapping.getTaxonomyId());
      assertNull(mapping.getGeneId());
      assertNull(mapping.getGeneName());
      assertNull(mapping.getGeneSummary());
      assertNull(mapping.getGeneSynonyms());
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
    InputStream searchInput = getClass().getResourceAsStream("/annotation/esearch3.fcgi.xml");
    List<String> accessions = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    InputStream accessionInput = new ByteArrayInputStream(
        accessions.stream().collect(Collectors.joining("\n")).getBytes(UTF_8_CHARSET));
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);
    when(invocationFetchIds.get(InputStream.class)).thenReturn(accessionInput);
    Path localGene2accession = download.resolve("refseq.gene2refseq.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene2refseq").toURI()),
        localGene2accession);
    when(ftpService.localFile(gene2accession)).thenReturn(localGene2accession);
    Path localGeneInfo = download.resolve("refseq.gene_info.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene_info").toURI()), localGeneInfo);
    when(ftpService.localFile(geneInfo)).thenReturn(localGeneInfo);

    List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

    verify(ftpService, times(2)).anonymousConnect(ncbiConfiguration.ftp());
    verify(ftpService).localFile(gene2accession);
    verify(ftpService).downloadFile(ftpClient, gene2accession, localGene2accession, progressBar,
        locale);
    verify(ftpService).localFile(geneInfo);
    verify(ftpService).downloadFile(ftpClient, geneInfo, localGeneInfo, progressBar, locale);
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("NP_001317102.1")) {
        assertEquals((Long) 1L, mapping.getGeneId());
        assertEquals("A1BG", mapping.getGeneName());
        assertEquals("alpha-1-B glycoprotein", mapping.getGeneSummary());
        assertEquals("A1B|ABG|GAB|HYST2477", mapping.getGeneSynonyms());
      } else {
        assertEquals((Long) 4404L, mapping.getGeneId());
        assertEquals("MRX39", mapping.getGeneName());
        assertEquals("mental retardation, X-linked 39", mapping.getGeneSummary());
        assertEquals(null, mapping.getGeneSynonyms());
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
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.REFSEQ);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    InputStream searchInput = getClass().getResourceAsStream("/annotation/esearch3.fcgi.xml");
    List<String> accessions = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    InputStream accessionInput = new ByteArrayInputStream(
        accessions.stream().collect(Collectors.joining("\n")).getBytes(UTF_8_CHARSET));
    when(invocationSearch.get(InputStream.class)).thenReturn(searchInput);
    when(invocationFetchIds.get(InputStream.class)).thenReturn(accessionInput);
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

    List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(parameters, progressBar, locale);

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
      assertNull(mapping.getGeneId());
      assertNull(mapping.getGeneName());
      assertNull(mapping.getGeneSummary());
      assertNull(mapping.getGeneSynonyms());
    }
  }
}
