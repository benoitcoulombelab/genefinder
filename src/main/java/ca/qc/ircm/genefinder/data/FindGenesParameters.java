package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.annotation.ProteinDatabase;

/**
 * Parameters for finding genes in data files.
 */
public interface FindGenesParameters {
  public ProteinDatabase getProteinDatabase();

  public int getProteinColumn();

  public boolean isGeneId();

  public boolean isGeneName();

  public boolean isGeneSynonyms();

  public boolean isGeneSummary();

  public boolean isProteinMolecularWeight();

  public boolean isSequence();
}
