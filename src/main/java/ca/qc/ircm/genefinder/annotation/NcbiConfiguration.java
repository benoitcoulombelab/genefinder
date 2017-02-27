package ca.qc.ircm.genefinder.annotation;

import java.util.regex.Pattern;

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
   * Returns gene2accession file on NCBI's FTP server.
   *
   * @return gene2accession file on NCBI's FTP server
   */
  public String gene2accession();

  /**
   * Returns gene_info file on NCBI's FTP server.
   *
   * @return gene_info file on NCBI's FTP server
   */
  public String geneInfo();

  /**
   * Returns RefSeq's protein accession pattern.
   *
   * @return RefSeq's protein accession pattern
   */
  public Pattern refseqProteinAccessionPattern();

  /**
   * Returns RefSeq's protein GI pattern.
   *
   * @return RefSeq's protein GI pattern
   */
  public Pattern refseqProteinGiPattern();

  /**
   * Returns RefSeq's sequences folder on NCBI's FTP server.
   *
   * @return RefSeq's sequences folder on NCBI's FTP server
   */
  public String refseqSequences();

  /**
   * Returns RefSeq's sequences filename pattern.
   *
   * @return RefSeq's sequences filename pattern
   */
  public Pattern refseqSequencesFilenamePattern();

  /**
   * Returns NCBI's EUtils URL.
   *
   * @return NCBI's EUtils URL
   */
  public String eutils();

  /**
   * Returns max ids per request on NCBI's EUtils service.
   *
   * @return max ids per request on NCBI's EUtils service
   */
  public int maxIdsPerRequest();
}
