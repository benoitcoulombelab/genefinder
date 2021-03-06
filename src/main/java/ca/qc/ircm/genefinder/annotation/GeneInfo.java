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

import java.util.Date;
import java.util.List;

/**
 * Gene info.
 */
public class GeneInfo {
  private int organismId;
  private long id;
  private String symbol;
  private String locusTag;
  private List<String> synonyms;
  private List<String> dbXrefs;
  private String chromosome;
  private String mapLocation;
  private String description;
  private String typeOfGene;
  private String symbolFromNomenclatureAuthority;
  private String fullNameFromNomenclatureAuthority;
  private String nomenclatureStatus;
  private List<String> otherDesignations;
  private Date modificationDate;

  public GeneInfo() {
  }

  public GeneInfo(Long id) {
    this.id = id;
  }

  public GeneInfo(Long id, String symbol) {
    this.id = id;
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return "GeneInfo [organismId=" + organismId + ", id=" + id + ", symbol=" + symbol + "]";
  }

  public int getOrganismId() {
    return organismId;
  }

  public void setOrganismId(int organismId) {
    this.organismId = organismId;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public List<String> getSynonyms() {
    return synonyms;
  }

  public void setSynonyms(List<String> synonyms) {
    this.synonyms = synonyms;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocusTag() {
    return locusTag;
  }

  public void setLocusTag(String locusTag) {
    this.locusTag = locusTag;
  }

  public List<String> getDbXrefs() {
    return dbXrefs;
  }

  public void setDbXrefs(List<String> dbXrefs) {
    this.dbXrefs = dbXrefs;
  }

  public String getChromosome() {
    return chromosome;
  }

  public void setChromosome(String chromosome) {
    this.chromosome = chromosome;
  }

  public String getMapLocation() {
    return mapLocation;
  }

  public void setMapLocation(String mapLocation) {
    this.mapLocation = mapLocation;
  }

  public String getTypeOfGene() {
    return typeOfGene;
  }

  public void setTypeOfGene(String typeOfGene) {
    this.typeOfGene = typeOfGene;
  }

  public String getSymbolFromNomenclatureAuthority() {
    return symbolFromNomenclatureAuthority;
  }

  public void setSymbolFromNomenclatureAuthority(String symbolFromNomenclatureAuthority) {
    this.symbolFromNomenclatureAuthority = symbolFromNomenclatureAuthority;
  }

  public String getFullNameFromNomenclatureAuthority() {
    return fullNameFromNomenclatureAuthority;
  }

  public void setFullNameFromNomenclatureAuthority(String fullNameFromNomenclatureAuthority) {
    this.fullNameFromNomenclatureAuthority = fullNameFromNomenclatureAuthority;
  }

  public String getNomenclatureStatus() {
    return nomenclatureStatus;
  }

  public void setNomenclatureStatus(String nomenclatureStatus) {
    this.nomenclatureStatus = nomenclatureStatus;
  }

  public List<String> getOtherDesignations() {
    return otherDesignations;
  }

  public void setOtherDesignations(List<String> otherDesignations) {
    this.otherDesignations = otherDesignations;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }
}
