package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.rest.RestClientFactory;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UniprotDownloadProteinMappingServiceTest {
  private static final int SEARCH_COUNT = 20168;
  private static final int MAX_IDS_PER_REQUEST = 1000;
  private UniprotDownloadProteinMappingService uniprotDownloadProteinMappingService;
  @Mock
  private UniprotConfiguration uniprotConfiguration;
  @Mock
  private NcbiConfiguration ncbiConfiguration;
  @Mock
  private RestClientFactory restClientFactory;
  @Mock
  private Client client;
  @Mock
  private WebTarget target;
  @Mock
  private Invocation.Builder request;
  @Mock
  private ProteinService proteinService;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private ProgressBar progressBar;
  @Captor
  private ArgumentCaptor<String> stringCaptor;
  @Captor
  private ArgumentCaptor<Entity<?>> entityCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.getDefault();
  private Path download;
  private String mapping = "http://www.uniprot.org/uploadlists";
  private String eutils = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils";
  private String esummary = "esummary.fcgi";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    uniprotDownloadProteinMappingService = new UniprotDownloadProteinMappingService(
        uniprotConfiguration, ncbiConfiguration, restClientFactory, proteinService);
    download = Files.createDirectory(temporaryFolder.getRoot().toPath().resolve("download"));
    when(uniprotConfiguration.mapping()).thenReturn(mapping);
    when(uniprotConfiguration.maxIdsPerRequest()).thenReturn(MAX_IDS_PER_REQUEST);
    when(ncbiConfiguration.eutils()).thenReturn(eutils);
    when(ncbiConfiguration.maxIdsPerRequest()).thenReturn(MAX_IDS_PER_REQUEST);
    when(restClientFactory.createClient()).thenReturn(client);
    when(client.target(anyString())).thenReturn(target);
    when(target.path(anyString())).thenReturn(target);
    when(target.queryParam(anyString(), anyVararg())).thenReturn(target);
    when(target.request()).thenReturn(request);
    when(progressBar.step(anyDouble())).thenReturn(progressBar);
  }

  private void gzip(Path input, Path output) throws IOException {
    try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(output))) {
      Files.copy(input, out);
    }
  }

  private String parseSequence(Path sequenceMapping, String proteinId) throws IOException {
    List<String> lines = Files.readAllLines(sequenceMapping);
    for (String line : lines) {
      String[] columns = line.split("\t", -1);
      if (columns[0].equals(proteinId)) {
        return columns[columns.length - 1];
      }
    }
    return null;
  }

  @Test
  public void downloadProteinMappings() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    List<String> proteinIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions.txt").toURI()));

    final List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
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
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions3.txt").toURI()));
    byte[] remoteMappings = Files
        .readAllBytes(Paths.get(getClass().getResource("/annotation/idmapping3-gene").toURI()));
    when(request.get(InputStream.class)).thenReturn(new ByteArrayInputStream(remoteMappings));
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(geneInfos));

    final List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(restClientFactory, times(2)).createClient();
    verify(client).target(mapping);
    verify(target).queryParam("from", "ACC,ID");
    verify(target).queryParam("to", "ACC");
    verify(target).queryParam("format", "tab");
    verify(target).queryParam("columns", "id,database(GeneID)");
    verify(target).queryParam(eq("query"), stringCaptor.capture());
    verify(request).get(InputStream.class);
    List<String> queries = Arrays.asList(stringCaptor.getValue().split(" "));
    assertEquals(3, queries.size());
    assertTrue(queries.containsAll(proteinIds));
    verify(target).path(esummary);
    verify(request).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getValue();
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("2149"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(2)).request();
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
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final List<String> proteinIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions3.txt").toURI()));
    Path remoteMappingsPath =
        Paths.get(getClass().getResource("/annotation/idmapping3-sequence").toURI());
    byte[] remoteMappings = Files.readAllBytes(remoteMappingsPath);
    when(request.get(InputStream.class)).thenReturn(new ByteArrayInputStream(remoteMappings));
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(restClientFactory).createClient();
    verify(client).target(mapping);
    verify(target).queryParam("from", "ACC,ID");
    verify(target).queryParam("to", "ACC");
    verify(target).queryParam("format", "tab");
    verify(target).queryParam("columns", "id,sequence");
    verify(target).queryParam(eq("query"), stringCaptor.capture());
    List<String> queries = Arrays.asList(stringCaptor.getValue().split(" "));
    assertEquals(3, queries.size());
    assertTrue(queries.containsAll(proteinIds));
    verify(target).request();
    verify(request).get(InputStream.class);
    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(0)));
    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(1)));
    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(2)));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("A0A075B759")) {
        assertEquals(parseSequence(remoteMappingsPath, "A0A075B759"), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("A0AV96")) {
        assertEquals(parseSequence(remoteMappingsPath, "A0AV96"), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(remoteMappingsPath, "A0A024RAP8"), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }

  @Test
  public void downloadProteinMappings_Gene_Sequence() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final List<String> proteinIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions3.txt").toURI()));
    Path remoteMappingsPath =
        Paths.get(getClass().getResource("/annotation/idmapping3-gene-sequence").toURI());
    byte[] remoteMappings = Files.readAllBytes(remoteMappingsPath);
    when(request.get(InputStream.class)).thenReturn(new ByteArrayInputStream(remoteMappings));
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(geneInfos));
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(0)));
    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(1)));
    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(2)));
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
        assertEquals(parseSequence(remoteMappingsPath, "A0A075B759"), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("A0AV96")) {
        assertNotNull(mapping.getGenes());
        assertEquals(1, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(4404L, gene.getId());
        assertEquals("MRX39", gene.getSymbol());
        assertEquals("mental retardation, X-linked 39", gene.getDescription());
        assertEquals(null, gene.getSynonyms());
        assertEquals(parseSequence(remoteMappingsPath, "A0AV96"), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertNotNull(mapping.getGenes());
        assertEquals(1, mapping.getGenes().size());
        GeneInfo gene = mapping.getGenes().get(0);
        assertEquals(4404L, gene.getId());
        assertEquals("MRX39", gene.getSymbol());
        assertEquals("mental retardation, X-linked 39", gene.getDescription());
        assertEquals(null, gene.getSynonyms());
        assertEquals(parseSequence(remoteMappingsPath, "A0A024RAP8"), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
    }
  }

  @Test
  public void downloadProteinMappings_OverMaxIdsPerRequest() throws Throwable {
    when(uniprotConfiguration.maxIdsPerRequest()).thenReturn(2);
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions3.txt").toURI()));
    byte[] remoteMappings = Files
        .readAllBytes(Paths.get(getClass().getResource("/annotation/idmapping3-gene").toURI()));
    when(request.get(InputStream.class)).thenReturn(new ByteArrayInputStream(remoteMappings));
    Path localGeneInfo = download.resolve("refseq.gene_info.gz");
    gzip(Paths.get(getClass().getResource("/annotation/refseq.gene_info").toURI()), localGeneInfo);
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(geneInfos));

    final List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(restClientFactory, times(2)).createClient();
    verify(client).target(mapping);
    verify(target).queryParam("from", "ACC,ID");
    verify(target).queryParam("to", "ACC");
    verify(target).queryParam("format", "tab");
    verify(target).queryParam("columns", "id,database(GeneID)");
    verify(target, times(2)).queryParam(eq("query"), stringCaptor.capture());
    List<String> queries = stringCaptor.getAllValues().stream()
        .flatMap(q -> Arrays.asList(q.split(" ")).stream()).collect(Collectors.toList());
    assertEquals(3, queries.size());
    assertTrue(queries.containsAll(proteinIds));
    assertEquals(2, stringCaptor.getAllValues().get(0).split(" ").length);
    assertEquals(1, stringCaptor.getAllValues().get(1).split(" ").length);
    verify(request, times(2)).get(InputStream.class);
    verify(target).path(esummary);
    verify(request).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getValue();
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("2149"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(3)).request();
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
  public void downloadProteinMappings_Sequence_RestError() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final List<String> proteinIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions3.txt").toURI()));
    Path remoteMappingsPath =
        Paths.get(getClass().getResource("/annotation/idmapping3-sequence").toURI());
    byte[] remoteMappings = Files.readAllBytes(remoteMappingsPath);
    when(request.get(InputStream.class))
        .thenThrow(new ResponseProcessingException(Response.status(404).build(), "Test"))
        .thenReturn(new ByteArrayInputStream(remoteMappings));
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(restClientFactory).createClient();
    verify(client).target(mapping);
    verify(target).queryParam("from", "ACC,ID");
    verify(target).queryParam("to", "ACC");
    verify(target).queryParam("format", "tab");
    verify(target).queryParam("columns", "id,sequence");
    verify(target).queryParam(eq("query"), stringCaptor.capture());
    List<String> queries = Arrays.asList(stringCaptor.getValue().split(" "));
    assertEquals(3, queries.size());
    assertTrue(queries.containsAll(proteinIds));
    verify(target).request();
    verify(request, times(2)).get(InputStream.class);
    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(0)));
    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(1)));
    verify(proteinService).weight(parseSequence(remoteMappingsPath, proteinIds.get(2)));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("A0A075B759")) {
        assertEquals(parseSequence(remoteMappingsPath, "A0A075B759"), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("A0AV96")) {
        assertEquals(parseSequence(remoteMappingsPath, "A0AV96"), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(remoteMappingsPath, "A0A024RAP8"), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }

  @Test
  public void downloadProteinMappings_Gene_RestError() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(ProteinDatabase.UNIPROT);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds = Files.readAllLines(
        Paths.get(getClass().getResource("/annotation/uniprot-accessions3.txt").toURI()));
    byte[] remoteMappings = Files
        .readAllBytes(Paths.get(getClass().getResource("/annotation/idmapping3-gene").toURI()));
    when(request.get(InputStream.class)).thenReturn(new ByteArrayInputStream(remoteMappings));
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenThrow(new ResponseProcessingException(Response.status(404).build(), "Test"))
        .thenReturn(new ByteArrayInputStream(geneInfos));

    final List<ProteinMapping> mappings = uniprotDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(restClientFactory, times(2)).createClient();
    verify(client).target(mapping);
    verify(target).queryParam("from", "ACC,ID");
    verify(target).queryParam("to", "ACC");
    verify(target).queryParam("format", "tab");
    verify(target).queryParam("columns", "id,database(GeneID)");
    verify(target).queryParam(eq("query"), stringCaptor.capture());
    verify(request).get(InputStream.class);
    List<String> queries = Arrays.asList(stringCaptor.getValue().split(" "));
    assertEquals(3, queries.size());
    assertTrue(queries.containsAll(proteinIds));
    verify(target).path(esummary);
    verify(request, times(2)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getValue();
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("2149"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(2)).request();
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
}
