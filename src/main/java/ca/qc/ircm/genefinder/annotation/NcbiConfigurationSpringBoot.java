package ca.qc.ircm.genefinder.annotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = NcbiConfigurationSpringBoot.PREFIX)
public class NcbiConfigurationSpringBoot implements NcbiConfiguration {
  public static final String PREFIX = "ncbi";
  private String refseqProteinAccessionPattern;
  private String refseqProteinGiPattern;
  private String eutils;
  private int maxIdsPerRequest;

  @Override
  public Pattern refseqProteinAccessionPattern() {
    return Pattern.compile(refseqProteinAccessionPattern);
  }

  @Override
  public Pattern refseqProteinGiPattern() {
    return Pattern.compile(refseqProteinGiPattern);
  }

  @Override
  public String eutils() {
    return eutils;
  }

  @Override
  public int maxIdsPerRequest() {
    return maxIdsPerRequest;
  }

  public String getEutils() {
    return eutils;
  }

  public void setEutils(String eutils) {
    this.eutils = eutils;
  }

  public int getMaxIdsPerRequest() {
    return maxIdsPerRequest;
  }

  public void setMaxIdsPerRequest(int maxIdsPerRequest) {
    this.maxIdsPerRequest = maxIdsPerRequest;
  }

  public String getRefseqProteinAccessionPattern() {
    return refseqProteinAccessionPattern;
  }

  public void setRefseqProteinAccessionPattern(String refseqProteinAccessionPattern) {
    this.refseqProteinAccessionPattern = refseqProteinAccessionPattern;
  }

  public String getRefseqProteinGiPattern() {
    return refseqProteinGiPattern;
  }

  public void setRefseqProteinGiPattern(String refseqProteinGiPattern) {
    this.refseqProteinGiPattern = refseqProteinGiPattern;
  }
}
