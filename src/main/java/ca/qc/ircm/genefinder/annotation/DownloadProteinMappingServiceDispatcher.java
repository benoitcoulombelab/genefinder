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
  public List<ProteinMapping> downloadProteinMappings(List<String> proteinIds,
      FindGenesParameters parameters, ProgressBar progressBar, Locale locale)
      throws IOException, InterruptedException {
    switch (parameters.getProteinDatabase()) {
      case REFSEQ:
      case REFSEQ_GI:
        return refseqDownloadProteinMappingService.downloadProteinMappings(proteinIds, parameters,
            progressBar, locale);
      case UNIPROT:
        return uniprotDownloadProteinMappingService.downloadProteinMappings(proteinIds, parameters,
            progressBar, locale);
      default:
        throw new AssertionError(ProteinDatabase.class.getSimpleName() + " "
            + parameters.getProteinDatabase() + " not covered in switch");
    }
  }
}
