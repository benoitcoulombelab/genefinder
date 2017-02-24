package ca.qc.ircm.genefinder.data.gui;

import ca.qc.ircm.genefinder.annotation.ProteinDatabase;
import javafx.util.StringConverter;

import java.util.Locale;

/**
 * StringConverter for {@link ProteinDatabase}.
 */
public class ProteinDatabaseStringConverter extends StringConverter<ProteinDatabase> {
  private Locale locale;

  public ProteinDatabaseStringConverter(Locale locale) {
    this.locale = locale;
  }

  @Override
  public ProteinDatabase fromString(String value) {
    for (ProteinDatabase proteinDatabase : ProteinDatabase.values()) {
      if (proteinDatabase.getLabel(locale).equals(value)) {
        return proteinDatabase;
      }
    }
    throw new AssertionError(
        ProteinDatabase.class.getSimpleName() + " " + value + " does not exists");
  }

  @Override
  public String toString(ProteinDatabase proteinDatabase) {
    return proteinDatabase.getLabel(locale);
  }
}
