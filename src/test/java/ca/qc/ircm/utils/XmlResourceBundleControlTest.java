package ca.qc.ircm.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class XmlResourceBundleControlTest {
  private Set<String> convertKeys(Enumeration<String> keys) {
    Set<String> set = new HashSet<String>();
    while (keys.hasMoreElements()) {
      set.add(keys.nextElement());
    }
    return set;
  }

  @Test
  public void getBundle_Xml() throws Throwable {
    ResourceBundle resources = ResourceBundle.getBundle("utils.XmlResourceBundleControlTest_xml",
        Locale.CANADA_FRENCH, new XmlResourceBundleControl());

    assertEquals("Ceci est un test", resources.getString("message"));
    assertEquals("Parent test", resources.getString("parent_message"));
    Set<String> keys = convertKeys(resources.getKeys());
    assertTrue(keys.contains("message"));
    assertTrue(keys.contains("parent_message"));
  }

  @Test
  public void getBundle_Properties() throws Throwable {
    ResourceBundle resources =
        ResourceBundle.getBundle("utils.XmlResourceBundleControlTest_properties",
            Locale.CANADA_FRENCH, new XmlResourceBundleControl());

    assertEquals("Ceci est un test", resources.getString("message"));
    assertEquals("Parent test", resources.getString("parent_message"));
    Set<String> keys = convertKeys(resources.getKeys());
    assertTrue(keys.contains("message"));
    assertTrue(keys.contains("parent_message"));
  }

  @Test
  public void getBundle_XmlWithParentProperties() throws Throwable {
    ResourceBundle resources = ResourceBundle.getBundle("utils.XmlResourceBundleControlTest_mix1",
        Locale.CANADA_FRENCH, new XmlResourceBundleControl());

    assertEquals("Ceci est un test", resources.getString("message"));
    assertEquals("Parent test", resources.getString("parent_message"));
    Set<String> keys = convertKeys(resources.getKeys());
    assertTrue(keys.contains("message"));
    assertTrue(keys.contains("parent_message"));
  }

  @Test
  public void getBundle_PropertiesWithParentXml() throws Throwable {
    ResourceBundle resources = ResourceBundle.getBundle("utils.XmlResourceBundleControlTest_mix2",
        Locale.CANADA_FRENCH, new XmlResourceBundleControl());

    assertEquals("Ceci est un test", resources.getString("message"));
    assertEquals("Parent test", resources.getString("parent_message"));
    Set<String> keys = convertKeys(resources.getKeys());
    assertTrue(keys.contains("message"));
    assertTrue(keys.contains("parent_message"));
  }
}
