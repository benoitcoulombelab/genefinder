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
