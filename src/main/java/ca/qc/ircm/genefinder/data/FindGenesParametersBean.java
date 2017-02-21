package ca.qc.ircm.genefinder.data;

/**
 * Parameters for finding genes in data files.
 */
public class FindGenesParametersBean implements FindGenesParameters {
  private int proteinColumn;
  private boolean geneId;
  private boolean geneName;
  private boolean geneSynonyms;
  private boolean geneSummary;
  private boolean proteinMolecularWeight;

  @Override
  public int getProteinColumn() {
    return proteinColumn;
  }

  public FindGenesParametersBean proteinColumn(int proteinColumn) {
    this.proteinColumn = proteinColumn;
    return this;
  }

  @Override
  public boolean isGeneId() {
    return geneId;
  }

  public FindGenesParametersBean geneId(boolean geneId) {
    this.geneId = geneId;
    return this;
  }

  @Override
  public boolean isGeneName() {
    return geneName;
  }

  public FindGenesParametersBean geneName(boolean geneName) {
    this.geneName = geneName;
    return this;
  }

  @Override
  public boolean isGeneSynonyms() {
    return geneSynonyms;
  }

  public FindGenesParametersBean geneSynonyms(boolean geneSynonyms) {
    this.geneSynonyms = geneSynonyms;
    return this;
  }

  @Override
  public boolean isGeneSummary() {
    return geneSummary;
  }

  public FindGenesParametersBean geneSummary(boolean geneSummary) {
    this.geneSummary = geneSummary;
    return this;
  }

  @Override
  public boolean isProteinMolecularWeight() {
    return proteinMolecularWeight;
  }

  public FindGenesParametersBean proteinMolecularWeight(boolean proteinMolecularWeight) {
    this.proteinMolecularWeight = proteinMolecularWeight;
    return this;
  }
}
