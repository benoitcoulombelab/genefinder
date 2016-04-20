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
