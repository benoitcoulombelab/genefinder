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

package ca.qc.ircm.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class XmlResourceBundleTest {
  @Mock
  private ResourceBundle parent;

  @BeforeEach
  public void beforeTest() {
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  public void afterTest() {
    Mockito.validateMockitoUsage();
  }

  @Test
  public void getString() throws Throwable {
    InputStream input = getClass().getResourceAsStream("/utils/XmlResourceBundleTest.xml");

    XmlResourceBundle resources = new XmlResourceBundle(input);

    assertEquals("This is a test", resources.getString("message"));
  }

  @Test
  public void getString_Null() throws Throwable {
    InputStream input = getClass().getResourceAsStream("/utils/XmlResourceBundleTest.xml");

    XmlResourceBundle resources = new XmlResourceBundle(input);
    assertThrows(NullPointerException.class, () -> resources.getString(null));
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
