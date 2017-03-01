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
