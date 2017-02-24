package ca.qc.ircm.genefinder.annotation;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.SWISSPROT;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.UNIPROT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Locale;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DownloadProteinMappingServiceDispatcherTest {
  private DownloadProteinMappingServiceDispatcher downloadProteinMappingServiceDispatcher;
  @Mock
  private RefseqDownloadProteinMappingService refseqDownloadProteinMappingService;
  @Mock
  private UniprotDownloadProteinMappingService uniprotDownloadProteinMappingService;
  @Mock
  private DownloadProteinMappingServiceBean downloadProteinMappingServiceBean;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private Organism organism;
  @Mock
  private ProgressBar progressBar;
  private Locale locale = Locale.getDefault();

  @Before
  public void beforeTest() {
    downloadProteinMappingServiceDispatcher =
        new DownloadProteinMappingServiceDispatcher(refseqDownloadProteinMappingService,
            uniprotDownloadProteinMappingService, downloadProteinMappingServiceBean);
  }

  @Test
  public void allProteinMappings() throws Throwable {
    downloadProteinMappingServiceDispatcher.allProteinMappings(organism, progressBar, locale);

    verify(downloadProteinMappingServiceBean).allProteinMappings(organism, progressBar, locale);
  }

  @Test
  public void downloadProteinMappings_Refseq() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);

    downloadProteinMappingServiceDispatcher.downloadProteinMappings(parameters, progressBar,
        locale);

    verify(refseqDownloadProteinMappingService).downloadProteinMappings(parameters, progressBar,
        locale);
  }

  @Test
  public void downloadProteinMappings_Refseqgi() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);

    downloadProteinMappingServiceDispatcher.downloadProteinMappings(parameters, progressBar,
        locale);

    verify(refseqDownloadProteinMappingService).downloadProteinMappings(parameters, progressBar,
        locale);
  }

  @Test
  public void downloadProteinMappings_Uniprot() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(UNIPROT);

    downloadProteinMappingServiceDispatcher.downloadProteinMappings(parameters, progressBar,
        locale);

    verify(uniprotDownloadProteinMappingService).downloadProteinMappings(parameters, progressBar,
        locale);
  }

  @Test
  public void downloadProteinMappings_Swissprot() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(SWISSPROT);

    downloadProteinMappingServiceDispatcher.downloadProteinMappings(parameters, progressBar,
        locale);

    verify(uniprotDownloadProteinMappingService).downloadProteinMappings(parameters, progressBar,
        locale);
  }
}
