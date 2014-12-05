package ca.qc.ircm.genefinder.ncbi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import ca.qc.ircm.genefinder.ApplicationProperties;
import ca.qc.ircm.genefinder.ncbi.NcbiServiceBean;
import ca.qc.ircm.genefinder.ncbi.ProteinMapping;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.TestLoggingRunner;
import ca.qc.ircm.progress_bar.ProgressBar;

@RunWith(TestLoggingRunner.class)
public class NcbiServiceBeanTest {
    private NcbiServiceBean ncbiServiceBean;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private Organism organism;
    @Mock
    private ProgressBar progressBar;
    @Captor
    private ArgumentCaptor<Collection<ProteinMapping>> proteinMappingsCaptor;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File home;
    private Locale locale;

    @Before
    public void beforeTest() throws Throwable {
        ncbiServiceBean = new NcbiServiceBean(applicationProperties);
        locale = Locale.getDefault();
        home = temporaryFolder.newFolder("home");
        when(applicationProperties.getHome()).thenReturn(home);
    }

    @Test
    public void fillGeneDatabase_9606() throws Throwable {
        when(organism.getId()).thenReturn(9606);
        File file = temporaryFolder.newFile("gene2accession.gz");
        try (InputStream input = getClass().getResourceAsStream("/gene2accession.txt");
                OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
            IOUtils.copy(input, output);
        }
        when(applicationProperties.getProperty("ncbi.gene2accession"))
                .thenReturn(file.toURI().toURL().toExternalForm());
        file = temporaryFolder.newFile("gene_info.gz");
        try (InputStream input = getClass().getResourceAsStream("/gene_info.txt");
                OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
            IOUtils.copy(input, output);
        }
        when(applicationProperties.getProperty("ncbi.gene_info")).thenReturn(file.toURI().toURL().toExternalForm());

        List<ProteinMapping> mappings = ncbiServiceBean.allProteinMappings(organism, progressBar, locale);

        verify(applicationProperties).getProperty("ncbi.gene2accession");
        verify(applicationProperties).getProperty("ncbi.gene_info");
        verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
        verify(progressBar, atLeastOnce()).setMessage(any(String.class));
        assertEquals(20, mappings.size());
        ProteinMapping mapping = mappings.get(0);
        assertEquals((Integer) 119592981, mapping.getGi());
        assertEquals((Integer) 1, mapping.getGeneId());
        assertEquals("A1BG", mapping.getGeneName());
        assertEquals((Integer) 9606, mapping.getTaxonomyId());
        mapping = mappings.get(1);
        assertEquals((Integer) 317373553, mapping.getGi());
        assertEquals((Integer) 1, mapping.getGeneId());
        assertEquals("A1BG", mapping.getGeneName());
        assertEquals((Integer) 9606, mapping.getTaxonomyId());
        mapping = mappings.get(5);
        assertEquals((Integer) 13661814, mapping.getGi());
        assertEquals((Integer) 2, mapping.getGeneId());
        assertEquals("A2M", mapping.getGeneName());
        assertEquals((Integer) 9606, mapping.getTaxonomyId());
        mapping = mappings.get(10);
        assertEquals((Integer) 2245376, mapping.getGi());
        assertEquals((Integer) 9, mapping.getGeneId());
        assertEquals("NAT1", mapping.getGeneName());
        assertEquals((Integer) 9606, mapping.getTaxonomyId());
    }

    @Test
    public void fillGeneDatabase_10090() throws Throwable {
        when(organism.getId()).thenReturn(10090);
        File file = temporaryFolder.newFile("gene2accession.gz");
        try (InputStream input = getClass().getResourceAsStream("/gene2accession.txt");
                OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
            IOUtils.copy(input, output);
        }
        when(applicationProperties.getProperty("ncbi.gene2accession"))
                .thenReturn(file.toURI().toURL().toExternalForm());
        file = temporaryFolder.newFile("gene_info.gz");
        try (InputStream input = getClass().getResourceAsStream("/gene_info.txt");
                OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
            IOUtils.copy(input, output);
        }
        when(applicationProperties.getProperty("ncbi.gene_info")).thenReturn(file.toURI().toURL().toExternalForm());

        List<ProteinMapping> mappings = ncbiServiceBean.allProteinMappings(organism, progressBar, locale);

        verify(applicationProperties).getProperty("ncbi.gene2accession");
        verify(applicationProperties).getProperty("ncbi.gene_info");
        verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
        verify(progressBar, atLeastOnce()).setMessage(any(String.class));
        assertEquals(25, mappings.size());
        ProteinMapping mapping = mappings.get(0);
        assertEquals((Integer) 463884, mapping.getGi());
        assertEquals((Integer) 11287, mapping.getGeneId());
        assertEquals("Pzp", mapping.getGeneName());
        assertEquals((Integer) 10090, mapping.getTaxonomyId());
        mapping = mappings.get(1);
        assertEquals((Integer) 148667474, mapping.getGi());
        assertEquals((Integer) 11287, mapping.getGeneId());
        assertEquals("Pzp", mapping.getGeneName());
        assertEquals((Integer) 10090, mapping.getTaxonomyId());
        mapping = mappings.get(5);
        assertEquals((Integer) 4099097, mapping.getGi());
        assertEquals((Integer) 11298, mapping.getGeneId());
        assertEquals("Aanat", mapping.getGeneName());
        assertEquals((Integer) 10090, mapping.getTaxonomyId());
        mapping = mappings.get(11);
        assertEquals((Integer) 81912939, mapping.getGi());
        assertEquals((Integer) 11302, mapping.getGeneId());
        assertEquals("Aatk", mapping.getGeneName());
        assertEquals((Integer) 10090, mapping.getTaxonomyId());
    }
}
