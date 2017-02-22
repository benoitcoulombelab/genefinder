package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.progressbar.JavafxProgressBar;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import javax.inject.Inject;

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
