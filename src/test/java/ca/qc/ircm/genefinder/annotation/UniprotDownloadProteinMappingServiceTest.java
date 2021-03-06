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

package ca.qc.ircm.genefinder.annotation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.rest.RestClientFactory;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

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
  private Locale locale = Locale.getDefault();
  private String mapping = "http://www.uniprot.org/uploadlists";
  private String eutils = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils";
  private String esummary = "esummary.fcgi";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() throws Throwable {
    uniprotDownloadProteinMappingService = new UniprotDownloadProteinMappingService(
        uniprotConfiguration, ncbiConfiguration, restClientFactory, proteinService);
    when(uniprotConfiguration.mapping()).thenReturn(mapping);
    when(uniprotConfiguration.maxIdsPerRequest()).thenReturn(MAX_IDS_PER_REQUEST);
    when(ncbiConfiguration.eutils()).thenReturn(eutils);
    when(ncbiConfiguration.maxIdsPerRequest()).thenReturn(MAX_IDS_PER_REQUEST);
    when(restClientFactory.createClient()).thenReturn(client);
    when(client.target(anyString())).thenReturn(target);
    when(target.path(anyString())).thenReturn(target);
    when(target.queryParam(anyString(), any())).thenReturn(target);
    when(target.request()).thenReturn(request);
    when(progressBar.step(anyDouble())).thenReturn(progressBar);
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
    assertEquals(3, geneIds.size());
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
    assertEquals(3, geneIds.size());
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
