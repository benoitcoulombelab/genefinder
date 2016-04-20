package ca.qc.ircm.genefinder;

import java.nio.file.Path;

/**
 * Application's properties.
 */
public interface ApplicationProperties {
  public Path getHome();

  public Path getOrganismData();

  public String getProperty(String key);

  public String getProperty(String key, String defaultValue);
}
