package ca.qc.ircm.genefinder;

import java.io.File;

/**
 * Application's properties.
 */
public interface ApplicationProperties {
    public File getHome();

    public File getOrganismData();

    public String getProperty(String key);

    public String getProperty(String key, String defaultValue);
}
