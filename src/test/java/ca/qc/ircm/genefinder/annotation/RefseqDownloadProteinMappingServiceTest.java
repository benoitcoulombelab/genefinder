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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
public class RefseqDownloadProteinMappingServiceTest {
  private static final int SEARCH_COUNT = 1231;
  private static final int MAX_IDS_PER_REQUEST = 1000;

  private RefseqDownloadProteinMappingService refseqDownloadProteinMappingService;
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
  private ArgumentCaptor<Entity<?>> entityCaptor;
  private Locale locale = Locale.getDefault();
  private String eutils = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils";
  private String esummary = "esummary.fcgi";
  private String elink = "elink.fcgi";
  private String efetch = "efetch.fcgi";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    refseqDownloadProteinMappingService = new RefseqDownloadProteinMappingService(ncbiConfiguration,
        restClientFactory, proteinService);
    when(ncbiConfiguration.eutils()).thenReturn(eutils);
    when(ncbiConfiguration.maxIdsPerRequest()).thenReturn(MAX_IDS_PER_REQUEST);
    when(restClientFactory.createClient()).thenReturn(client);
    when(client.target(anyString())).thenReturn(target);
    when(target.path(anyString())).thenReturn(target);
    when(target.queryParam(anyString(), anyVararg())).thenReturn(target);
    when(target.request()).thenReturn(request);
    when(progressBar.step(anyDouble())).thenReturn(progressBar);
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
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
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
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    byte[] proteinSummary = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/refseq-esummary.fcgi.xml").toURI()));
    byte[] geneMappings = Files
        .readAllBytes(Paths.get(getClass().getResource("/annotation/gene-elink.fcgi.xml").toURI()));
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(proteinSummary))
        .thenReturn(new ByteArrayInputStream(geneMappings))
        .thenReturn(new ByteArrayInputStream(geneInfos));

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target, times(2)).path(esummary);
    verify(target).path(elink);
    verify(request, times(3)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getAllValues().get(0);
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> proteinSummaryIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(3, proteinSummaryIds.size());
    assertTrue(proteinSummaryIds.contains("NP_001317102.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317083.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(1);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("dbfrom").size());
    assertEquals("protein", form.asMap().getFirst("dbfrom"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("829098688"));
    assertTrue(form.asMap().get("id").contains("829098686"));
    assertTrue(form.asMap().get("id").contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(2);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(2, geneIds.size());
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(3)).request();
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
    Path sequencesResource =
        Paths.get(getClass().getResource("/annotation/refseq-sequences.fasta").toURI());
    byte[] sequences = Files.readAllBytes(sequencesResource);
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(sequences));
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target).path(efetch);
    verify(request).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getValue();
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("rettype").size());
    assertEquals("fasta", form.asMap().getFirst("rettype"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("NP_001317102.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317083.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target).request();
    verify(proteinService).weight(parseSequence(sequencesResource, 0));
    verify(proteinService).weight(parseSequence(sequencesResource, 1));
    verify(proteinService).weight(parseSequence(sequencesResource, 2));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("NP_001317102.1")) {
        assertEquals(parseSequence(sequencesResource, 0), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("NP_001317083.1")) {
        assertEquals(parseSequence(sequencesResource, 1), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(sequencesResource, 2), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }

  @Test
  public void downloadProteinMappings_Gi() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
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
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds =
        Files.readAllLines(Paths.get(getClass().getResource("/annotation/gis3.txt").toURI()));
    byte[] geneMappings = Files
        .readAllBytes(Paths.get(getClass().getResource("/annotation/gene-elink.fcgi.xml").toURI()));
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(geneMappings))
        .thenReturn(new ByteArrayInputStream(geneInfos));

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target).path(esummary);
    verify(target).path(elink);
    verify(request, times(2)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getAllValues().get(0);
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("dbfrom").size());
    assertEquals("protein", form.asMap().getFirst("dbfrom"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("829098688"));
    assertTrue(form.asMap().get("id").contains("829098686"));
    assertTrue(form.asMap().get("id").contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(1);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(2)).request();
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
    byte[] proteinSummary = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/refseq-esummary.fcgi.xml").toURI()));
    Path sequencesResource =
        Paths.get(getClass().getResource("/annotation/refseq-sequences.fasta").toURI());
    byte[] sequences = Files.readAllBytes(sequencesResource);
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(proteinSummary))
        .thenReturn(new ByteArrayInputStream(sequences));
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target).path(esummary);
    verify(target).path(efetch);
    verify(request, times(2)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getAllValues().get(0);
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> proteinSummaryIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(3, proteinSummaryIds.size());
    assertTrue(proteinSummaryIds.contains("829098688"));
    assertTrue(proteinSummaryIds.contains("829098686"));
    assertTrue(proteinSummaryIds.contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(1);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("rettype").size());
    assertEquals("fasta", form.asMap().getFirst("rettype"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("NP_001317102.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317083.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(2)).request();
    verify(proteinService).weight(parseSequence(sequencesResource, 0));
    verify(proteinService).weight(parseSequence(sequencesResource, 1));
    verify(proteinService).weight(parseSequence(sequencesResource, 2));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("829098688")) {
        assertEquals(parseSequence(sequencesResource, 0), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("829098686")) {
        assertEquals(parseSequence(sequencesResource, 1), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(sequencesResource, 2), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }

  @Test
  public void downloadProteinMappings_Gene_ProteinSummaryRestError() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    byte[] proteinSummary = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/refseq-esummary.fcgi.xml").toURI()));
    byte[] geneMappings = Files
        .readAllBytes(Paths.get(getClass().getResource("/annotation/gene-elink.fcgi.xml").toURI()));
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenThrow(new ResponseProcessingException(Response.status(404).build(), "Test"))
        .thenReturn(new ByteArrayInputStream(proteinSummary))
        .thenReturn(new ByteArrayInputStream(geneMappings))
        .thenReturn(new ByteArrayInputStream(geneInfos));

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target, times(2)).path(esummary);
    verify(target).path(elink);
    verify(request, times(4)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getAllValues().get(0);
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> proteinSummaryIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(3, proteinSummaryIds.size());
    assertTrue(proteinSummaryIds.contains("NP_001317102.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317083.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(1);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    proteinSummaryIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(3, proteinSummaryIds.size());
    assertTrue(proteinSummaryIds.contains("NP_001317102.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317083.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(2);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("dbfrom").size());
    assertEquals("protein", form.asMap().getFirst("dbfrom"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("829098688"));
    assertTrue(form.asMap().get("id").contains("829098686"));
    assertTrue(form.asMap().get("id").contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(3);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(2, geneIds.size());
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(3)).request();
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
  public void downloadProteinMappings_Gene_GeneLinkRestError() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    byte[] proteinSummary = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/refseq-esummary.fcgi.xml").toURI()));
    byte[] geneMappings = Files
        .readAllBytes(Paths.get(getClass().getResource("/annotation/gene-elink.fcgi.xml").toURI()));
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(proteinSummary))
        .thenThrow(new ResponseProcessingException(Response.status(404).build(), "Test"))
        .thenReturn(new ByteArrayInputStream(geneMappings))
        .thenReturn(new ByteArrayInputStream(geneInfos));

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target, times(2)).path(esummary);
    verify(target).path(elink);
    verify(request, times(4)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getAllValues().get(0);
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> proteinSummaryIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(3, proteinSummaryIds.size());
    assertTrue(proteinSummaryIds.contains("NP_001317102.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317083.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(1);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("dbfrom").size());
    assertEquals("protein", form.asMap().getFirst("dbfrom"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("829098688"));
    assertTrue(form.asMap().get("id").contains("829098686"));
    assertTrue(form.asMap().get("id").contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(2);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("dbfrom").size());
    assertEquals("protein", form.asMap().getFirst("dbfrom"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("829098688"));
    assertTrue(form.asMap().get("id").contains("829098686"));
    assertTrue(form.asMap().get("id").contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(3);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(2, geneIds.size());
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(3)).request();
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
  public void downloadProteinMappings_Gene_GeneInfoRestError() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isGeneId()).thenReturn(true);
    when(parameters.isGeneName()).thenReturn(true);
    when(parameters.isGeneSynonyms()).thenReturn(true);
    when(parameters.isGeneSummary()).thenReturn(true);
    final List<String> proteinIds = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    byte[] proteinSummary = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/refseq-esummary.fcgi.xml").toURI()));
    byte[] geneMappings = Files
        .readAllBytes(Paths.get(getClass().getResource("/annotation/gene-elink.fcgi.xml").toURI()));
    byte[] geneInfos = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/gene-esummary.fcgi.xml").toURI()));
    when(request.post(any(), eq(InputStream.class)))
        .thenReturn(new ByteArrayInputStream(proteinSummary))
        .thenReturn(new ByteArrayInputStream(geneMappings))
        .thenThrow(new ResponseProcessingException(Response.status(404).build(), "Test"))
        .thenReturn(new ByteArrayInputStream(geneInfos));

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target, times(2)).path(esummary);
    verify(target).path(elink);
    verify(request, times(4)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getAllValues().get(0);
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> proteinSummaryIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(3, proteinSummaryIds.size());
    assertTrue(proteinSummaryIds.contains("NP_001317102.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317083.1"));
    assertTrue(proteinSummaryIds.contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(1);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("dbfrom").size());
    assertEquals("protein", form.asMap().getFirst("dbfrom"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("829098688"));
    assertTrue(form.asMap().get("id").contains("829098686"));
    assertTrue(form.asMap().get("id").contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(2);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(2, geneIds.size());
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(3);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("gene", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    geneIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(2, geneIds.size());
    assertTrue(geneIds.contains("1"));
    assertTrue(geneIds.contains("4404"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(3)).request();
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
  public void downloadProteinMappings_Sequence_ProteinSummaryRestError() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final List<String> proteinIds =
        Files.readAllLines(Paths.get(getClass().getResource("/annotation/gis3.txt").toURI()));
    byte[] proteinSummary = Files.readAllBytes(
        Paths.get(getClass().getResource("/annotation/refseq-esummary.fcgi.xml").toURI()));
    Path sequencesResource =
        Paths.get(getClass().getResource("/annotation/refseq-sequences.fasta").toURI());
    byte[] sequences = Files.readAllBytes(sequencesResource);
    when(request.post(any(), eq(InputStream.class)))
        .thenThrow(new ResponseProcessingException(Response.status(404).build(), "Test"))
        .thenReturn(new ByteArrayInputStream(proteinSummary))
        .thenReturn(new ByteArrayInputStream(sequences));
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target).path(esummary);
    verify(target).path(efetch);
    verify(request, times(3)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getAllValues().get(0);
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    List<String> proteinSummaryIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(3, proteinSummaryIds.size());
    assertTrue(proteinSummaryIds.contains("829098688"));
    assertTrue(proteinSummaryIds.contains("829098686"));
    assertTrue(proteinSummaryIds.contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(1);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("id").size());
    proteinSummaryIds = Arrays.asList(form.asMap().getFirst("id").split(","));
    assertEquals(3, proteinSummaryIds.size());
    assertTrue(proteinSummaryIds.contains("829098688"));
    assertTrue(proteinSummaryIds.contains("829098686"));
    assertTrue(proteinSummaryIds.contains("829098684"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(2);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("NP_001317102.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317083.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target, times(2)).request();
    verify(proteinService).weight(parseSequence(sequencesResource, 0));
    verify(proteinService).weight(parseSequence(sequencesResource, 1));
    verify(proteinService).weight(parseSequence(sequencesResource, 2));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("829098688")) {
        assertEquals(parseSequence(sequencesResource, 0), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("829098686")) {
        assertEquals(parseSequence(sequencesResource, 1), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(sequencesResource, 2), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }

  @Test
  public void downloadProteinMappings_Sequence_SequenceRestError() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);
    when(parameters.isSequence()).thenReturn(true);
    when(parameters.isProteinMolecularWeight()).thenReturn(true);
    final List<String> proteinIds = Files
        .readAllLines(Paths.get(getClass().getResource("/annotation/accessions3.txt").toURI()));
    Path sequencesResource =
        Paths.get(getClass().getResource("/annotation/refseq-sequences.fasta").toURI());
    byte[] sequences = Files.readAllBytes(sequencesResource);
    when(request.post(any(), eq(InputStream.class)))
        .thenThrow(new ResponseProcessingException(Response.status(404).build(), "Test"))
        .thenReturn(new ByteArrayInputStream(sequences));
    double sequenceWeight1 = 127.3;
    double sequenceWeight2 = 58.9;
    double sequenceWeight3 = 41.4;
    when(proteinService.weight(anyString())).thenReturn(sequenceWeight1, sequenceWeight2,
        sequenceWeight3);

    final List<ProteinMapping> mappings = refseqDownloadProteinMappingService
        .downloadProteinMappings(proteinIds, parameters, progressBar, locale);

    verify(target).path(efetch);
    verify(request, times(2)).post(entityCaptor.capture(), eq(InputStream.class));
    Entity<?> entity = entityCaptor.getAllValues().get(0);
    assertTrue(entity.getEntity() instanceof Form);
    Form form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(1, form.asMap().get("rettype").size());
    assertEquals("fasta", form.asMap().getFirst("rettype"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("NP_001317102.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317083.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    entity = entityCaptor.getAllValues().get(1);
    assertTrue(entity.getEntity() instanceof Form);
    form = (Form) entity.getEntity();
    assertEquals(1, form.asMap().get("db").size());
    assertEquals("protein", form.asMap().getFirst("db"));
    assertEquals(3, form.asMap().get("id").size());
    assertTrue(form.asMap().get("id").contains("NP_001317102.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317083.1"));
    assertTrue(form.asMap().get("id").contains("NP_001317082.1"));
    assertEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, entity.getMediaType());
    verify(target).request();
    verify(proteinService).weight(parseSequence(sequencesResource, 0));
    verify(proteinService).weight(parseSequence(sequencesResource, 1));
    verify(proteinService).weight(parseSequence(sequencesResource, 2));
    assertEquals(3, mappings.size());
    for (ProteinMapping mapping : mappings) {
      if (mapping.getProteinId().equals("NP_001317102.1")) {
        assertEquals(parseSequence(sequencesResource, 0), mapping.getSequence());
        assertEquals(sequenceWeight1, mapping.getMolecularWeight(), 0.001);
      } else if (mapping.getProteinId().equals("NP_001317083.1")) {
        assertEquals(parseSequence(sequencesResource, 1), mapping.getSequence());
        assertEquals(sequenceWeight2, mapping.getMolecularWeight(), 0.001);
      } else {
        assertEquals(parseSequence(sequencesResource, 2), mapping.getSequence());
        assertEquals(sequenceWeight3, mapping.getMolecularWeight(), 0.001);
      }
      assertNull(mapping.getTaxonomyId());
      assertTrue(mapping.getGenes() == null || mapping.getGenes().isEmpty());
    }
  }
}
