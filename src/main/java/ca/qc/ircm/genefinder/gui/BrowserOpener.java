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

import java.io.IOException;
import org.apache.commons.lang3.SystemUtils;

/**
 * Opens an URL in the system's browser.
 */
public class BrowserOpener {
  /**
   * Opens an URL in the system's browser.
   *
   * @param url
   *          URL
   * @throws IOException
   *           could not run the command to open the browser
   */
  public static void open(String url) throws IOException {
    Runtime rt = Runtime.getRuntime();
    if (SystemUtils.IS_OS_WINDOWS) {
      rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
    } else if (SystemUtils.IS_OS_MAC_OSX) {
      rt.exec("open " + url);
    } else {
      // Expect Linux.
      rt.exec("xdg-open " + url);
    }
  }
}
