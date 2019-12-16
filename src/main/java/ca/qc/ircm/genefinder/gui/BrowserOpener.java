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
