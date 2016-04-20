/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

public class XmlResourceBundle extends ResourceBundle {
  private final Properties properties;

  public XmlResourceBundle(InputStream input) throws IOException {
    properties = new Properties();
    properties.loadFromXML(input);
  }

  @Override
  protected Object handleGetObject(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return properties.getProperty(key);
  }

  @Override
  public Enumeration<String> getKeys() {
    Set<String> handleKeys = new HashSet<>(properties.stringPropertyNames());
    if (parent != null) {
      handleKeys.addAll(Collections.list(parent.getKeys()));
    }
    return Collections.enumeration(handleKeys);
  }
}
