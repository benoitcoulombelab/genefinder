package ca.qc.ircm.genefinder.data;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import ca.qc.ircm.genefinder.ncbi.NcbiService;
import ca.qc.ircm.genefinder.ncbi.ProteinMapping;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.TestLoggingRunner;
import ca.qc.ircm.progress_bar.ProgressBar;

@RunWith(TestLoggingRunner.class)
public class DataServiceBeanTest {
    private DataServiceBean dataServiceBean;
    @Mock
    private NcbiService ncbiService;
    @Mock
    private DataWriter dataWriter;
    @Mock
    private Organism organism;
    @Mock
    private ProgressBar progressBar;
    @Captor
    private ArgumentCaptor<Map<Integer, ProteinMapping>> mappingsCaptor;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private Locale locale;
    private Integer organismId = 9606;

    @Before
    public void beforeTest() {
        dataServiceBean = new DataServiceBean(ncbiService, dataWriter);
        locale = Locale.getDefault();
        when(organism.getId()).thenReturn(organismId);
        when(progressBar.step(any(Double.class))).thenReturn(progressBar);
    }

    @Test
    public void findGeneNames() throws Throwable {
        File file = new File(getClass().getResource("/proteinGroups.txt").toURI());
        File input = temporaryFolder.newFile("proteinGroups.txt");
        FileUtils.copyFile(file, input);
        File output = new File(temporaryFolder.getRoot(), "proteinGroupsWithGene.txt");
        List<File> files = Arrays.asList(input);
        List<ProteinMapping> mappings = new ArrayList<ProteinMapping>();
        mappings.add(getProteinMapping(4262120, "ABC"));
        mappings.add(getProteinMapping(58201131, "ABC"));
        mappings.add(getProteinMapping(270297794, "ABC"));
        mappings.add(getProteinMapping(13560677, "FFE"));
        mappings.add(getProteinMapping(13492060, "RTS"));
        mappings.add(getProteinMapping(63100331, "FAF"));
        mappings.add(getProteinMapping(30583211, "FAF"));
        mappings.add(getProteinMapping(17512236, "FAF"));
        when(ncbiService.allProteinMappings(any(Organism.class), any(ProgressBar.class), any(Locale.class)))
        .thenReturn(mappings);
        FindGenesParametersBean parameters = new FindGenesParametersBean();

        dataServiceBean.findGeneNames(organism, files, parameters, progressBar, locale);

        verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
        verify(progressBar, atLeastOnce()).setMessage(any(String.class));
        verify(ncbiService).allProteinMappings(organism, progressBar, locale);
        verify(dataWriter).writeGene(eq(input), eq(output), eq(parameters), mappingsCaptor.capture());
    }

    private ProteinMapping getProteinMapping(Integer gi, String geneName) {
        ProteinMapping mapping = new ProteinMapping();
        mapping.setGi(gi);
        mapping.setGeneName(geneName);
        return mapping;
    }
}
