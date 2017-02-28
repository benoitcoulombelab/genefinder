package ca.qc.ircm.genefinder.annotation;

import java.util.regex.Pattern;

/**
 * NCBI's configuration.
 */
public interface NcbiConfiguration {
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
