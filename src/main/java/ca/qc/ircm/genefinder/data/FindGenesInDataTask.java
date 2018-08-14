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

import ca.qc.ircm.progressbar.JavafxProgressBar;
import java.io.File;
import java.util.Collection;
import java.util.Locale;
import javafx.concurrent.Task;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that download protein database and find genes for MaxQuant file.
 */
public class FindGenesInDataTask extends Task<Void> {
  private static final Logger logger = LoggerFactory.getLogger(FindGenesInDataTask.class);
  private DataService dataService;
  private Collection<File> files;
  private FindGenesParameters findGenesParameter;
  private Locale locale;

  /**
   * Creates find genes in data task.
   *
   * @param dataService
   *          data service
   * @param files
   *          files to search
   * @param findGenesParameter
   *          parameters
   * @param locale
   *          locale
   */
  @Inject
  public FindGenesInDataTask(DataService dataService, Collection<File> files,
      FindGenesParameters findGenesParameter, Locale locale) {
    this.dataService = dataService;
    this.files = files;
    this.findGenesParameter = findGenesParameter;
    this.locale = locale;
  }

  @Override
  protected Void call() throws Exception {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    progressBar.message().addListener((observable, oldValue, newValue) -> {
      updateMessage(newValue);
      logger.trace("updateMessage {}", newValue);
    });
    progressBar.progress().addListener((observable, oldValue, newValue) -> {
      updateProgress(newValue.doubleValue(), Math.max(newValue.doubleValue(), 1.0));
    });
    dataService.findGeneNames(files, findGenesParameter, progressBar, locale);
    logger.debug("completed files {}", files);
    return null;
  }
}
