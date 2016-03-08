package ca.qc.ircm.genefinder.data;

/**
 * Parameters for finding genes in data files.
 */
public interface FindGenesParameters {
  public boolean isGeneId();

  public boolean isGeneName();

  public boolean isGeneSynonyms();

  public boolean isGeneSummary();

  public boolean isProteinMolecularWeight();
}
