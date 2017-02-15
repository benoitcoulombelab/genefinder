package ca.qc.ircm.genefinder;

import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Properties;

import javax.annotation.PostConstruct;

/**
 * Default implementation for {@link ApplicationProperties}.
 */
@Component
public class ApplicationPropertiesBean implements ApplicationProperties {
  private static final String ANNOTATIONS_FOLDER = "annotations";
  private Properties properties;

  protected ApplicationPropertiesBean() {
  }

  @PostConstruct
  protected void init() {
    loadProperties();
  }

  private void loadProperties() {
    this.properties = new Properties();
    try {
      URL file = getClass().getResource("/application.properties");
      if (file != null) {
        try (InputStream input = new BufferedInputStream(file.openStream())) {
          properties.load(input);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not create " + getClass().getSimpleName(), e);
    }
  }

  @Override
  public Path getHome() {
    return Paths.get(MessageFormat.format(properties.getProperty("home.directory"),
        System.getProperty("user.home")));
  }

  @Override
  public Path getAnnotationsFolder() {
    Path path = getHome().resolve(ANNOTATIONS_FOLDER);
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Could not create directory to store annotations " + path.getParent());
    }
    return path;
  }

  @Override
  public Path getOrganismData() {
    return Paths.get(MessageFormat.format(properties.getProperty("organism.data"), getHome()));
  }

  @Override
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }
}
