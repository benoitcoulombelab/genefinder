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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * {@link ResourceBundle} that formats messages using {@link MessageFormat}.
 */
public class MessageResources {
  private final ResourceBundle resources;

  public MessageResources(String baseName, Locale locale) {
    resources = ResourceBundle.getBundle(baseName, locale, new XmlResourceBundleControl());
  }

  public MessageResources(Class<?> baseClass, Locale locale) {
    resources =
        ResourceBundle.getBundle(baseClass.getName(), locale, new XmlResourceBundleControl());
  }

  /**
   * Returns message.
   *
   * @param key
   *          message's key
   * @param replacements
   *          message's replacements
   * @return message
   */
  public String message(String key, Object... replacements) {
    return MessageFormat.format(resources.getString(key), replacements);
  }
}
