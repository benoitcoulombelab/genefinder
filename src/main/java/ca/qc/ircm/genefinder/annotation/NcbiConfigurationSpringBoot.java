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
  private Gene gene;

  @Override
  public String ftp() {
    return ftp;
  }

  @Override
  public String geneInfo() {
    return gene.geneInfo;
  }

  public String getFtp() {
    return ftp;
  }

  public void setFtp(String ftp) {
    this.ftp = ftp;
  }

  public Gene getGene() {
    return gene;
  }

  public void setGene(Gene gene) {
    this.gene = gene;
  }

  public static class Gene {
    private String geneInfo;

    public String getGeneInfo() {
      return geneInfo;
    }

    public void setGeneInfo(String geneInfo) {
      this.geneInfo = geneInfo;
    }
  }
}
