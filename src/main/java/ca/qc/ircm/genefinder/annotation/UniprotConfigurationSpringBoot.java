package ca.qc.ircm.genefinder.annotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = UniprotConfigurationSpringBoot.PREFIX)
public class UniprotConfigurationSpringBoot implements UniprotConfiguration {
  public static final String PREFIX = "uniprot";
  private String ftp;
  private String mapping;
  private String proteinIdPattern;
  private int maxIdsPerRequest;

  @Override
  public String ftp() {
    return ftp;
  }

  @Override
  public String mapping() {
    return mapping;
  }

  @Override
  public Pattern proteinIdPattern() {
    return Pattern.compile(proteinIdPattern);
  }

  @Override
  public int maxIdsPerRequest() {
    return maxIdsPerRequest;
  }

  public String getFtp() {
    return ftp;
  }

  public void setFtp(String ftp) {
    this.ftp = ftp;
  }

  public String getProteinIdPattern() {
    return proteinIdPattern;
  }

  public void setProteinIdPattern(String proteinIdPattern) {
    this.proteinIdPattern = proteinIdPattern;
  }

  public int getMaxIdsPerRequest() {
    return maxIdsPerRequest;
  }

  public void setMaxIdsPerRequest(int maxIdsPerRequest) {
    this.maxIdsPerRequest = maxIdsPerRequest;
  }

  public String getMapping() {
    return mapping;
  }

  public void setMapping(String mapping) {
    this.mapping = mapping;
  }
}
