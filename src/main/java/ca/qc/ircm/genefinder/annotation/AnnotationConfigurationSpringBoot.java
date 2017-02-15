package ca.qc.ircm.genefinder.annotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = AnnotationConfigurationSpringBoot.PREFIX)
public class AnnotationConfigurationSpringBoot implements AnnotationConfiguration {
  public static final String PREFIX = "annotation";
  private String gene2accession;
  private String geneInfo;
  private String giTaxid;
  private String nr;

  @Override
  public String gene2accession() {
    return gene2accession;
  }

  @Override
  public String geneInfo() {
    return geneInfo;
  }

  @Override
  public String giTaxid() {
    return giTaxid;
  }

  @Override
  public String nr() {
    return nr;
  }

  public String getGene2accession() {
    return gene2accession;
  }

  public void setGene2accession(String gene2accession) {
    this.gene2accession = gene2accession;
  }

  public String getGeneInfo() {
    return geneInfo;
  }

  public void setGeneInfo(String geneInfo) {
    this.geneInfo = geneInfo;
  }

  public String getGiTaxid() {
    return giTaxid;
  }

  public void setGiTaxid(String giTaxid) {
    this.giTaxid = giTaxid;
  }

  public String getNr() {
    return nr;
  }

  public void setNr(String nr) {
    this.nr = nr;
  }
}
