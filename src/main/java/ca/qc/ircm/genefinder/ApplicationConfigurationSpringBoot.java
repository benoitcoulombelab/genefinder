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
  private Path download;

  @Override
  public Path download() {
    try {
      Files.createDirectories(download);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Could not create directory to store downloaded files " + download.getParent());
    }
    return download;
  }

  public Path getDownload() {
    return download;
  }

  public void setDownload(Path download) {
    this.download = download;
  }
}
