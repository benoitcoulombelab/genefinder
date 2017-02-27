package ca.qc.ircm.genefinder.data;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.DownloadProteinMappingService;
import ca.qc.ircm.genefinder.annotation.GeneInfo;
import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DataServiceTest {
  private DataService dataServiceBean;
  @Mock
  private DownloadProteinMappingService proteinMappingService;
  @Mock
  private ProteinParser proteinParser;
  @Mock
  private DataWriter dataWriter;
  @Mock
  private Organism organism;
  @Mock
  private ProgressBar progressBar;
  @Mock
  private List<String> proteinIds;
  @Captor
  private ArgumentCaptor<Map<String, ProteinMapping>> mappingsCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale;
  private Integer organismId = 9606;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    dataServiceBean = new DataService(proteinMappingService, proteinParser, dataWriter);
    locale = Locale.getDefault();
    when(organism.getId()).thenReturn(organismId);
    when(progressBar.step(any(Double.class))).thenReturn(progressBar);
  }

  @Test
  public void findGeneNames() throws Throwable {
    File file = new File(getClass().getResource("/proteinGroups.txt").toURI());
    File input = temporaryFolder.newFile("proteinGroups.txt");
    FileUtils.copyFile(file, input);
    final File output = new File(temporaryFolder.getRoot(), "proteinGroupsWithGene.txt");
    final List<File> files = Arrays.asList(input);
    List<ProteinMapping> mappings = new ArrayList<>();
    mappings.add(getProteinMapping("4262120", "ABC"));
    mappings.add(getProteinMapping("58201131", "ABC"));
    mappings.add(getProteinMapping("270297794", "ABC"));
    mappings.add(getProteinMapping("13560677", "FFE"));
    mappings.add(getProteinMapping("13492060", "RTS"));
    mappings.add(getProteinMapping("63100331", "FAF"));
    mappings.add(getProteinMapping("30583211", "FAF"));
    mappings.add(getProteinMapping("17512236", "FAF"));
    when(proteinParser.parseProteinIds(any(), any())).thenReturn(proteinIds);
    when(proteinMappingService.downloadProteinMappings(any(), any(), any(), any()))
        .thenReturn(mappings);
    FindGenesParametersBean parameters = new FindGenesParametersBean();
    parameters.organism(organism);

    dataServiceBean.findGeneNames(files, parameters, progressBar, locale);

    verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
    verify(progressBar, atLeastOnce()).setMessage(any(String.class));
    verify(proteinParser).parseProteinIds(eq(input), eq(parameters));
    verify(proteinMappingService).downloadProteinMappings(eq(proteinIds), eq(parameters),
        eq(progressBar), eq(locale));
    verify(dataWriter).writeGene(eq(input), eq(output), eq(parameters), mappingsCaptor.capture());
  }

  private ProteinMapping getProteinMapping(String proteinId, String geneName) {
    ProteinMapping mapping = new ProteinMapping();
    mapping.setProteinId(proteinId);
    mapping.setGenes(Arrays.asList(new GeneInfo(1L, geneName)));
    return mapping;
  }
}
