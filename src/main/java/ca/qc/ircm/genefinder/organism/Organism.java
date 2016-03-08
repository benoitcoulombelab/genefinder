package ca.qc.ircm.genefinder.organism;

import ca.qc.ircm.genefinder.Named;

/**
 * An organism.
 */
public class Organism implements Named {
  private Integer id;
  private String name;

  public Organism() {
  }

  public Organism(Integer id) {
    this.id = id;
  }

  public Organism(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.toUpperCase().hashCode());
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
    if (!(obj instanceof Organism)) {
      return false;
    }
    Organism other = (Organism) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equalsIgnoreCase(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Organism [id=" + id + ", name=" + name + "]";
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
