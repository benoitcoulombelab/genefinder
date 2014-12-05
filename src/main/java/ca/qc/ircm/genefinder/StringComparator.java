package ca.qc.ircm.genefinder;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for strings.
 */
public class StringComparator implements Comparator<String>, Serializable {

    private static final long serialVersionUID = 9139553099085193216L;

    /**
     * Uses compareTo method of String.
     * 
     * @see String#compareTo(java.lang.String)
     */
    public StringComparator() {
        super();
    }

    /**
     * Comparato for strings that can ignore case.
     * 
     * @param ignoreCase
     *            True to ignore case when comparing strings.
     */
    public StringComparator(boolean ignoreCase) {
        super();
        this.ignoreCase = ignoreCase;
    }

    /**
     * If true, comparator ignores case.
     */
    private boolean ignoreCase = false;

    @Override
    public int compare(String o1, String o2) {
        if (o1 != null && o2 != null) {
            if (ignoreCase) {
                return o1.compareToIgnoreCase(o2);
            } else {
                return o1.compareTo(o2);
            }
        } else if (o1 != null) {
            return -1;
        } else if (o2 != null) {
            return 1;
        } else {
            return 0;
        }
    }
}
