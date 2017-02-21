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

  public String referenceProteomes();

  public Pattern filenamePattern();

  public String giMapping();

  public String refseqMapping();

  public String taxonMapping();

  public String geneMapping();

  public Pattern proteinIdPattern();
}
