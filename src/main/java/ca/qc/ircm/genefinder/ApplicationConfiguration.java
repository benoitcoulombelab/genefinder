package ca.qc.ircm.genefinder;

import java.nio.file.Path;

/**
 * Application's configuration.
 */
public interface ApplicationConfiguration {
  /**
   * Returns home folder.
   *
   * @return home folder
   */
  public Path home();

  /**
   * Returns annotations folder where to store remote files.
   *
   * @return annotations folder where to store remote files
   */
  public Path annotationsFolder();
}
