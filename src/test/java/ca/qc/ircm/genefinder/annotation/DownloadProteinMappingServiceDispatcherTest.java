package ca.qc.ircm.genefinder.annotation;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.UNIPROT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
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
  private List<String> proteinIds;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private ProgressBar progressBar;
  private Locale locale = Locale.getDefault();

  @Before
  public void beforeTest() {
    downloadProteinMappingServiceDispatcher = new DownloadProteinMappingServiceDispatcher(
        refseqDownloadProteinMappingService, uniprotDownloadProteinMappingService);
  }

  @Test
  public void downloadProteinMappings_Refseq() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ);

    downloadProteinMappingServiceDispatcher.downloadProteinMappings(proteinIds, parameters,
        progressBar, locale);

    verify(refseqDownloadProteinMappingService).downloadProteinMappings(proteinIds, parameters,
        progressBar, locale);
  }

  @Test
  public void downloadProteinMappings_RefseqGi() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(REFSEQ_GI);

    downloadProteinMappingServiceDispatcher.downloadProteinMappings(proteinIds, parameters,
        progressBar, locale);

    verify(refseqDownloadProteinMappingService).downloadProteinMappings(proteinIds, parameters,
        progressBar, locale);
  }

  @Test
  public void downloadProteinMappings_Uniprot() throws Throwable {
    when(parameters.getProteinDatabase()).thenReturn(UNIPROT);

    downloadProteinMappingServiceDispatcher.downloadProteinMappings(proteinIds, parameters,
        progressBar, locale);

    verify(uniprotDownloadProteinMappingService).downloadProteinMappings(proteinIds, parameters,
        progressBar, locale);
  }
}
