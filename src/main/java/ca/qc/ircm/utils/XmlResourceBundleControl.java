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
