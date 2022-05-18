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

import java.util.GregorianCalendar;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class MessageResourcesTest {
  @Test
  public void message() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class.getName(), Locale.CANADA);

    String message = messageResources.message("message");

    assertEquals("This is a test", message);
  }

  @Test
  public void message_NoReplacements() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class.getName(), Locale.CANADA);

    String message = messageResources.message("replacements");

    assertEquals("This is a test {0} {1} {2} {3}", message);
  }

  @Test
  public void message_Replacements() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class.getName(), Locale.CANADA);

    String message = messageResources.message("replacements", "test", 1, 0.32,
        new GregorianCalendar(2015, 7, 24, 15, 23, 45).getTime());

    assertEquals("This is a test test second 32% 2015-08-24T15:23:45", message);
  }

  @Test
  public void message_Class() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class, Locale.CANADA);

    String message = messageResources.message("message");

    assertEquals("This is a test", message);
  }

  @Test
  public void message_Class_NoReplacements() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class, Locale.CANADA);

    String message = messageResources.message("replacements");

    assertEquals("This is a test {0} {1} {2} {3}", message);
  }

  @Test
  public void message_Class_Replacements() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class, Locale.CANADA);

    String message = messageResources.message("replacements", "test", 1, 0.32,
        new GregorianCalendar(2015, 7, 24, 15, 23, 45).getTime());

    assertEquals("This is a test test second 32% 2015-08-24T15:23:45", message);
  }

  @Test
  public void message_French() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class.getName(), Locale.FRENCH);

    String message = messageResources.message("message");

    assertEquals("Ceci est un test", message);
  }

  @Test
  public void message_French_NoReplacements() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class.getName(), Locale.FRENCH);

    String message = messageResources.message("replacements");

    assertEquals("Ceci est un test {0} {1} {2} {3}", message);
  }

  @Test
  public void message_French_Replacements() {
    MessageResources messageResources =
        new MessageResources(MessageResourcesTest.class.getName(), Locale.FRENCH);

    String message = messageResources.message("replacements", "test", 1, 0.32,
        new GregorianCalendar(2015, 7, 24, 15, 23, 45).getTime());

    assertEquals("Ceci est un test test second 32% 2015-08-24T15:23:45", message);
  }
}
