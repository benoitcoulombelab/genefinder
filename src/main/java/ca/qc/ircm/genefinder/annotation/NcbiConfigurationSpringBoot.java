package ca.qc.ircm.genefinder.annotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = NcbiConfigurationSpringBoot.PREFIX)
public class NcbiConfigurationSpringBoot implements NcbiConfiguration {
  public static final String PREFIX = "ncbi";
  private String ftp;
  private String taxonomy;
  private String taxonomyNodes;
  private String geneInfo;

  @Override
  public String ftp() {
    return ftp;
  }

  @Override
  public String taxonomy() {
    return taxonomy;
  }

  @Override
  public String taxonomyNodes() {
    return taxonomyNodes;
  }

  @Override
  public String geneInfo() {
    return geneInfo;
  }

  public String getFtp() {
    return ftp;
  }

  public void setFtp(String ftp) {
    this.ftp = ftp;
  }

  public String getTaxonomy() {
    return taxonomy;
  }

  public void setTaxonomy(String taxonomy) {
    this.taxonomy = taxonomy;
  }

  public String getTaxonomyNodes() {
    return taxonomyNodes;
  }

  public void setTaxonomyNodes(String taxonomyNodes) {
    this.taxonomyNodes = taxonomyNodes;
  }

  public String getGeneInfo() {
    return geneInfo;
  }

  public void setGeneInfo(String geneInfo) {
    this.geneInfo = geneInfo;
  }
}
