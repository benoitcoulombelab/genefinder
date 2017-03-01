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

package ca.qc.ircm.genefinder.gui;

import javafx.util.StringConverter;

/**
 * {@link StringConverter} that returns null when an exception is thrown by the supplied converter.
 */
public class NullOnExceptionConverter<T> extends StringConverter<T> {
  private final StringConverter<T> converter;

  public NullOnExceptionConverter(StringConverter<T> converter) {
    this.converter = converter;
  }

  @Override
  public T fromString(String string) {
    try {
      return converter.fromString(string);
    } catch (Throwable e) {
      return null;
    }
  }

  @Override
  public String toString(T object) {
    try {
      return converter.toString(object);
    } catch (Throwable e) {
      return null;
    }
  }
}
