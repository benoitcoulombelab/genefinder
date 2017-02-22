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
   * Returns NCBI's taxonomy file.
   *
   * @return NCBI's taxonomy file
   */
  public String taxonomy();

  /**
   * Returns NCBI's taxonomy nodes file inside taxonomy file.
   *
   * @return NCBI's taxonomy nodes file inside taxonomy file
   */
  public String taxonomyNodes();

  /**
   * Returns gene_info file on NCBI's FTP server.
   *
   * @return gene_info file on NCBI's FTP server
   */
  public String geneInfo();
}
