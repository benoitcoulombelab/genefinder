package ca.qc.ircm.genefinder;

import java.nio.file.Path;

/**
 * Application's configuration.
 */
public interface ApplicationConfiguration {
  /**
   * Returns folder where to store remote files.
   *
   * @return folder where to store remote files
   */
  public Path download();
}
