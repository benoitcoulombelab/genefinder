package ca.qc.ircm.genefinder;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for {@link Named}.
 */
public class NamedComparator implements Comparator<Named>, Serializable {
  private static final long serialVersionUID = -157683438900880530L;
  private final Comparator<String> nameComparator;

  public NamedComparator() {
    this.nameComparator = new StringComparator(true);
  }

  public NamedComparator(Comparator<String> nameComparator) {
    this.nameComparator = nameComparator;
  }

  @Override
  public int compare(Named o1, Named o2) {
    if (o1 != null && o1.getName() != null && o2 != null && o2.getName() != null) {
      return nameComparator.compare(o1.getName(), o2.getName());
    } else if (o1 != null && o1.getName() != null) {
      return -1;
    } else if (o2 != null && o2.getName() != null) {
      return 1;
    } else {
      return 0;
    }
  }
}
