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
