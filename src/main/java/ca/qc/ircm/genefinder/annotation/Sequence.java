package ca.qc.ircm.genefinder.annotation;

/**
 * A biological sequence.
 */
public class Sequence {
  private String name;
  private String sequence;

  public Sequence() {
  }

  public Sequence(String name, String sequence) {
    this.name = name;
    this.sequence = sequence;
  }

  @Override
  public String toString() {
    return "Sequence [name=" + name + ", sequence=" + sequence + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSequence() {
    return sequence;
  }

  public void setSequence(String sequence) {
    this.sequence = sequence;
  }
}
