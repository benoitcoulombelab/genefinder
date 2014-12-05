package ca.qc.ircm.genefinder.organism.gui;

import ca.qc.ircm.genefinder.organism.Organism;
import javafx.util.StringConverter;

/**
 * StringConverter for {@link Organism}.
 */
public class OrganismStringConverter extends StringConverter<Organism> {
    @Override
    public Organism fromString(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Organism organism) {
        return organism.getName();
    }
}
