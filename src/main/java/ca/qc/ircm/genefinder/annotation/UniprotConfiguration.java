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
   * Returns UniProt's id mapping URL.
   *
   * @return UniProt's id mapping URL
   */
  public String mapping();

  /**
   * Returns UniProt's protein id pattern.
   *
   * @return UniProt's protein id pattern
   */
  public Pattern proteinIdPattern();

  /**
   * Returns max ids per request on UniProt REST service.
   *
   * @return max ids per request on UniProt REST service
   */
  public int maxIdsPerRequest();
}
