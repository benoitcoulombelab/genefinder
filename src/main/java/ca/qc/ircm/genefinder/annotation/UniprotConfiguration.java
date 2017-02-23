package ca.qc.ircm.genefinder.annotation;

import java.util.regex.Pattern;

/**
 * UniProt's configuration.
 */
public interface UniprotConfiguration {
  /**
   * Returns UniProt's FTP.
   *
   * @return UniProt's FTP
   */
  public String ftp();

  /**
   * Returns UniProt's search URL.
   *
   * @return UniProt's search URL
   */
  public String search();

  /**
   * Returns UniProt's ID mapping file.
   *
   * @return UniProt's ID mapping file
   */
  public String idmapping();

  /**
   * Returns UniProt's Swiss-Prot sequence file.
   *
   * @return UniProt's Swiss-Prot sequence file
   */
  public String swissprotFasta();

  /**
   * Returns UniProt's TrEMBL sequence file.
   *
   * @return UniProt's TrEMBL sequence file
   */
  public String tremblFasta();

  public String referenceProteomes();

  public Pattern filenamePattern();

  public String giMapping();

  public String refseqMapping();

  public String taxonMapping();

  /**
   * Returns gene ID mapping in UniProt's ID mapping file.
   *
   * @return gene ID mapping in UniProt's ID mapping file
   */
  public String geneMapping();

  public Pattern proteinIdPattern();
}
