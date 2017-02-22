package ca.qc.ircm.genefinder.data;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Creates instances of {@link FindGenesInDataTask}.
 */
@Component
public class FindGenesInDataTaskFactory {
  @Inject
  private DataService dataService;

  public FindGenesInDataTask create(Collection<File> files, FindGenesParameters findGenesParameter,
      Locale locale) {
    return new FindGenesInDataTask(dataService, files, findGenesParameter, locale);
  }
}
