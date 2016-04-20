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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class XmlResourceBundleControl extends ResourceBundle.Control {
  private static final String XML = "xml";

  @Override
  public List<String> getFormats(String baseName) {
    if (baseName == null) {
      throw new NullPointerException();
    }
    List<String> formats = new ArrayList<>(super.getFormats(baseName));
    formats.add(XML);
    return formats;
  }

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
      boolean reload) throws IllegalAccessException, InstantiationException, IOException {
    if (baseName == null || locale == null || format == null || loader == null) {
      throw new NullPointerException();
    }
    ResourceBundle bundle = null;
    if (format.equals(XML)) {
      String bundleName = toBundleName(baseName, locale);
      String resourceName = toResourceName(bundleName, format);
      URL url = loader.getResource(resourceName);
      if (url != null) {
        URLConnection connection = url.openConnection();
        if (connection != null) {
          if (reload) {
            // disable caches if reloading
            connection.setUseCaches(false);
          }
          try (InputStream stream = connection.getInputStream()) {
            if (stream != null) {
              BufferedInputStream bis = new BufferedInputStream(stream);
              bundle = new XmlResourceBundle(bis);
            }
          }
        }
      }
    } else {
      bundle = super.newBundle(baseName, locale, format, loader, reload);
    }
    return bundle;
  }
}
