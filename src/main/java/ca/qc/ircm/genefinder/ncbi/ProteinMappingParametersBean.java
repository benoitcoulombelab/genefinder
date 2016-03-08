package ca.qc.ircm.genefinder.ncbi;

/**
 * Parameters for finding genes in data files.
 */
public class ProteinMappingParametersBean implements ProteinMappingParameters {
  private boolean geneId;
  private boolean geneDetails;
  private boolean sequence;
  private boolean molecularWeight;

  @Override
  public boolean isGeneId() {
    return geneId;
  }

  public ProteinMappingParametersBean geneId(boolean geneId) {
    this.geneId = geneId;
    return this;
  }

  @Override
  public boolean isGeneDetails() {
    return geneDetails;
  }

  public ProteinMappingParametersBean geneDetails(boolean geneDetails) {
    this.geneDetails = geneDetails;
    return this;
  }

  @Override
  public boolean isSequence() {
    return sequence;
  }

  public ProteinMappingParametersBean sequence(boolean sequence) {
    this.sequence = sequence;
    return this;
  }

  @Override
  public boolean isMolecularWeight() {
    return molecularWeight;
  }

  public ProteinMappingParametersBean molecularWeight(boolean molecularWeight) {
    this.molecularWeight = molecularWeight;
    return this;
  }
}
