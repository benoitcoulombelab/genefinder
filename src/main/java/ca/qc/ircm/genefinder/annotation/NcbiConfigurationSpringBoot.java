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
  private String ftp;
  private String taxonomy;
  private String taxonomyNodes;
  private String gene2accession;
  private String geneInfo;
  private String refseqProteinAccessionPattern;
  private String refseqProteinGiPattern;
  private String refseqSequences;
  private String refseqSequencesFilenamePattern;
  private String eutils;
  private int maxIdsPerRequest;

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
  public String gene2accession() {
    return gene2accession;
  }

  @Override
  public String geneInfo() {
    return geneInfo;
  }

  @Override
  public Pattern refseqProteinAccessionPattern() {
    return Pattern.compile(refseqProteinAccessionPattern);
  }

  @Override
  public Pattern refseqProteinGiPattern() {
    return Pattern.compile(refseqProteinGiPattern);
  }

  @Override
  public String refseqSequences() {
    return refseqSequences;
  }

  @Override
  public Pattern refseqSequencesFilenamePattern() {
    return Pattern.compile(refseqSequencesFilenamePattern);
  }

  @Override
  public String eutils() {
    return eutils;
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

  public String getGene2accession() {
    return gene2accession;
  }

  public void setGene2accession(String gene2accession) {
    this.gene2accession = gene2accession;
  }

  public String getRefseqSequences() {
    return refseqSequences;
  }

  public void setRefseqSequences(String refseqSequences) {
    this.refseqSequences = refseqSequences;
  }

  public String getRefseqSequencesFilenamePattern() {
    return refseqSequencesFilenamePattern;
  }

  public void setRefseqSequencesFilenamePattern(String refseqSequencesFilenamePattern) {
    this.refseqSequencesFilenamePattern = refseqSequencesFilenamePattern;
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
