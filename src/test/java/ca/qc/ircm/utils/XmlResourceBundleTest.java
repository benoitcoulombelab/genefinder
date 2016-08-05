package ca.qc.ircm.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.genefinder.test.config.Rules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class XmlResourceBundleTest {
  @Rule
  public RuleChain rules = Rules.defaultRules(this);
  @Mock
  private ResourceBundle parent;

  @Test
  public void getString() throws Throwable {
    InputStream input = getClass().getResourceAsStream("/utils/XmlResourceBundleTest.xml");

    XmlResourceBundle resources = new XmlResourceBundle(input);

    assertEquals("This is a test", resources.getString("message"));
  }

  @Test(expected = NullPointerException.class)
  public void getString_Null() throws Throwable {
    InputStream input = getClass().getResourceAsStream("/utils/XmlResourceBundleTest.xml");

    XmlResourceBundle resources = new XmlResourceBundle(input);
    resources.getString(null);
  }

  @Test
  public void getKeys() throws Throwable {
    InputStream input = getClass().getResourceAsStream("/utils/XmlResourceBundleTest.xml");

    XmlResourceBundle resources = new XmlResourceBundle(input);

    Enumeration<String> keys = resources.getKeys();
    assertTrue(keys.hasMoreElements());
    assertEquals("message", keys.nextElement());
    assertFalse(keys.hasMoreElements());
  }
}
