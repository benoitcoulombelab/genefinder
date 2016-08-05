package ca.qc.ircm.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class XmlResourceBundleTest {
  @Mock
  private ResourceBundle parent;

  @Before
  public void beforeTest() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void afterTest() {
    Mockito.validateMockitoUsage();
  }

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
