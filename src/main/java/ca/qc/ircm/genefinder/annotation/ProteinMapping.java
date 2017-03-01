/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.genefinder.annotation;

import java.util.List;

/**
 * Protein linked to a gene.
 */
public class ProteinMapping {
  private String proteinId;
  private List<GeneInfo> genes;
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
    return "ProteinMapping [proteinId=" + proteinId + ", taxonomyId=" + taxonomyId + "]";
  }

  public String getProteinId() {
    return proteinId;
  }

  public void setProteinId(String proteinId) {
    this.proteinId = proteinId;
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

  public List<GeneInfo> getGenes() {
    return genes;
  }

  public void setGenes(List<GeneInfo> genes) {
    this.genes = genes;
  }

  public Integer getTaxonomyId() {
    return taxonomyId;
  }

  public void setTaxonomyId(Integer taxonomyId) {
    this.taxonomyId = taxonomyId;
  }
}
