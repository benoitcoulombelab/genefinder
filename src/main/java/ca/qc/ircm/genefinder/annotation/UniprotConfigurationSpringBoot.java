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
  private String referenceProteomes;
  private String filenamePattern;
  private String giMapping;
  private String refseqMapping;
  private String taxonMapping;
  private String geneMapping;
  private String proteinIdPattern;

  @Override
  public String ftp() {
    return ftp;
  }

  @Override
  public String referenceProteomes() {
    return referenceProteomes;
  }

  @Override
  public Pattern filenamePattern() {
    return Pattern.compile(filenamePattern);
  }

  @Override
  public String giMapping() {
    return giMapping;
  }

  @Override
  public String refseqMapping() {
    return refseqMapping;
  }

  @Override
  public String taxonMapping() {
    return taxonMapping;
  }

  @Override
  public String geneMapping() {
    return geneMapping;
  }

  @Override
  public Pattern proteinIdPattern() {
    return Pattern.compile(proteinIdPattern);
  }

  public String getFtp() {
    return ftp;
  }

  public void setFtp(String ftp) {
    this.ftp = ftp;
  }

  public String getReferenceProteomes() {
    return referenceProteomes;
  }

  public void setReferenceProteomes(String referenceProteomes) {
    this.referenceProteomes = referenceProteomes;
  }

  public String getFilenamePattern() {
    return filenamePattern;
  }

  public void setFilenamePattern(String filenamePattern) {
    this.filenamePattern = filenamePattern;
  }

  public String getGiMapping() {
    return giMapping;
  }

  public void setGiMapping(String giMapping) {
    this.giMapping = giMapping;
  }

  public String getRefseqMapping() {
    return refseqMapping;
  }

  public void setRefseqMapping(String refseqMapping) {
    this.refseqMapping = refseqMapping;
  }

  public String getTaxonMapping() {
    return taxonMapping;
  }

  public void setTaxonMapping(String taxonMapping) {
    this.taxonMapping = taxonMapping;
  }

  public String getGeneMapping() {
    return geneMapping;
  }

  public void setGeneMapping(String geneMapping) {
    this.geneMapping = geneMapping;
  }

  public String getProteinIdPattern() {
    return proteinIdPattern;
  }

  public void setProteinIdPattern(String proteinIdPattern) {
    this.proteinIdPattern = proteinIdPattern;
  }
}
