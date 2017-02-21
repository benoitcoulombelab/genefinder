package ca.qc.ircm.genefinder.annotation;

/**
 * NCBI's configuration.
 */
public interface NcbiConfiguration {
  /**
   * Returns NCBI's FTP.
   *
   * @return NCBI's FTP
   */
  public String ftp();

  /**
   * Returns gene_info file on NCBI's FTP server.
   *
   * @return gene_info file on NCBI's FTP server
   */
  public String geneInfo();
}
