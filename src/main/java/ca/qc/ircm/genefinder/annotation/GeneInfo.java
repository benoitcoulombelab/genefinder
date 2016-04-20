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
