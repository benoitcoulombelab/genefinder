package ca.qc.ircm.genefinder.annotation;

import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.progressbar.ProgressBar;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Dispatch calls to proper implementation.
 */
@Component
@Primary
public class DownloadProteinMappingServiceDispatcher implements DownloadProteinMappingService {
  @Inject
  private RefseqDownloadProteinMappingService refseqDownloadProteinMappingService;
  @Inject
  private UniprotDownloadProteinMappingService uniprotDownloadProteinMappingService;

  protected DownloadProteinMappingServiceDispatcher() {
  }

  protected DownloadProteinMappingServiceDispatcher(
      RefseqDownloadProteinMappingService refseqDownloadProteinMappingService,
      UniprotDownloadProteinMappingService uniprotDownloadProteinMappingService) {
    this.refseqDownloadProteinMappingService = refseqDownloadProteinMappingService;
    this.uniprotDownloadProteinMappingService = uniprotDownloadProteinMappingService;
  }

  @Override
  public List<ProteinMapping> downloadProteinMappings(FindGenesParameters parameters,
      ProgressBar progressBar, Locale locale) throws IOException, InterruptedException {
    switch (parameters.getProteinDatabase()) {
      case REFSEQ:
      case REFSEQ_GI:
        return refseqDownloadProteinMappingService.downloadProteinMappings(parameters, progressBar,
            locale);
      case UNIPROT:
      case SWISSPROT:
        return uniprotDownloadProteinMappingService.downloadProteinMappings(parameters, progressBar,
            locale);
      default:
        throw new AssertionError(ProteinDatabase.class.getSimpleName() + " "
            + parameters.getProteinDatabase() + " not covered in switch");
    }
  }
}
