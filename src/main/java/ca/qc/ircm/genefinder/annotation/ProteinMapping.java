package ca.qc.ircm.genefinder.annotation;

/**
 * Protein linked to a gene.
 */
public class ProteinMapping {
  private String proteinId;
  private Long geneId;
  private String geneName;
  private String geneSynonyms;
  private String geneSummary;
  private Integer taxonomyId;
  private String sequence;
  private Double molecularWeight;

  public ProteinMapping() {
  }

  public ProteinMapping(String proteinId) {
    this.proteinId = proteinId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((proteinId == null) ? 0 : proteinId.toUpperCase().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ProteinMapping)) {
      return false;
    }
    ProteinMapping other = (ProteinMapping) obj;
    if (proteinId == null) {
      if (other.proteinId != null) {
        return false;
      }
    } else if (!proteinId.equalsIgnoreCase(other.proteinId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ProteinMapping [proteinId=" + proteinId + ", geneId=" + geneId + ", geneName="
        + geneName + ", taxonomyId=" + taxonomyId + "]";
  }

  public String getProteinId() {
    return proteinId;
  }

  public void setProteinId(String proteinId) {
    this.proteinId = proteinId;
  }

  public Long getGeneId() {
    return geneId;
  }

  public void setGeneId(Long geneId) {
    this.geneId = geneId;
  }

  public String getGeneName() {
    return geneName;
  }

  public void setGeneName(String geneName) {
    this.geneName = geneName;
  }

  public Integer getTaxonomyId() {
    return taxonomyId;
  }

  public void setTaxonomyId(Integer taxonomyId) {
    this.taxonomyId = taxonomyId;
  }

  public String getGeneSynonyms() {
    return geneSynonyms;
  }

  public void setGeneSynonyms(String geneSynonyms) {
    this.geneSynonyms = geneSynonyms;
  }

  public String getGeneSummary() {
    return geneSummary;
  }

  public void setGeneSummary(String geneSummary) {
    this.geneSummary = geneSummary;
  }

  public String getSequence() {
    return sequence;
  }

  public void setSequence(String sequence) {
    this.sequence = sequence;
  }

  public Double getMolecularWeight() {
    return molecularWeight;
  }

  public void setMolecularWeight(Double molecularWeight) {
    this.molecularWeight = molecularWeight;
  }
}
