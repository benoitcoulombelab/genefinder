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

package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.annotation.DownloadProteinMappingService;
import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progressbar.ProgressBar;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

/**
 * Services for data file.
 */
@Service
public class DataService {
  @Inject
  private DownloadProteinMappingService downloadProteinMappingService;
  @Inject
  private ProteinParser proteinParser;
  @Inject
  private DataWriter dataWriter;

  protected DataService() {
  }

  protected DataService(DownloadProteinMappingService downloadProteinMappingService,
      ProteinParser proteinParser, DataWriter dataWriter) {
    this.downloadProteinMappingService = downloadProteinMappingService;
    this.proteinParser = proteinParser;
    this.dataWriter = dataWriter;
  }

  /**
   * Find selected gene and protein information for proteins found in files.
   *
   * @param files
   *          files
   * @param parameters
   *          information to find
   * @param progressBar
   *          records progression
   * @param locale
   *          user's locale
   * @throws IOException
   *           could not read file
   * @throws InterruptedException
   *           process interrupted by user
   */
  public void findGeneNames(Collection<File> files, FindGenesParameters parameters,
      ProgressBar progressBar, Locale locale) throws IOException, InterruptedException {
    ResourceBundle bundle = ResourceBundle.getBundle(DataService.class.getName(), locale);
    double step = 1.0 / Math.max(files.size(), 1);
    int count = 0;
    for (File file : files) {
      progressBar.setMessage(MessageFormat.format(bundle.getString("mappings"), file.getName()));
      ExceptionUtils.throwIfInterrupted("Interrupted gene finding");
      List<String> proteinIds = proteinParser.parseProteinIds(file, parameters);
      ExceptionUtils.throwIfInterrupted("Interrupted gene finding");
      List<ProteinMapping> rawMappings = downloadProteinMappingService
          .downloadProteinMappings(proteinIds, parameters, progressBar.step(0.8), locale);
      Map<String, ProteinMapping> mappings = rawMappings.stream().collect(
          Collectors.toMap(ProteinMapping::getProteinId, Function.<ProteinMapping>identity()));
      ExceptionUtils.throwIfInterrupted("Interrupted gene finding");
      progressBar.setMessage(MessageFormat.format(bundle.getString("writing"), file.getName()));
      String extension = FilenameUtils.getExtension(file.getName());
      String filename = MessageFormat.format(bundle.getString("output.filename"),
          FilenameUtils.getBaseName(file.getName()), extension.isEmpty() ? 0 : 1, extension);
      File output = new File(file.getParentFile(), filename);
      dataWriter.writeGene(file, output, parameters, mappings);
      progressBar.setProgress(step * count++);
    }
    progressBar.setProgress(1.0);
  }
}
