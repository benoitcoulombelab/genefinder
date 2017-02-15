package ca.qc.ircm.genefinder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = ApplicationConfigurationSpringBoot.PREFIX)
public class ApplicationConfigurationSpringBoot implements ApplicationConfiguration {
  public static final String PREFIX = "application";
  private static final String ANNOTATIONS_FOLDER = "annotations";
  private Path home;

  @Override
  public Path home() {
    return home;
  }

  @Override
  public Path annotationsFolder() {
    String filename = ANNOTATIONS_FOLDER;
    Path path = getHome().resolve(filename);
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Could not create directory to store saint file " + path.getParent());
    }
    return path;
  }

  public Path getHome() {
    return home;
  }

  public void setHome(Path home) {
    this.home = home;
  }
}
