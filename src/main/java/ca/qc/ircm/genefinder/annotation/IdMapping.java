package ca.qc.ircm.genefinder.annotation;

import java.util.List;
import java.util.Map;

/**
 * UniProt's id mapping entry.
 */
public class IdMapping {
  private String protein;
  private Map<String, List<String>> mappings;

  public String getProtein() {
    return protein;
  }

  public void setProtein(String protein) {
    this.protein = protein;
  }

  public Map<String, List<String>> getMappings() {
    return mappings;
  }

  public void setMappings(Map<String, List<String>> mappings) {
    this.mappings = mappings;
  }
}
