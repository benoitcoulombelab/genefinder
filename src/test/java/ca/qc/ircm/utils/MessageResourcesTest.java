package ca.qc.ircm.utils;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.genefinder.test.config.Rules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.GregorianCalendar;
import java.util.Locale;

public class MessageResourcesTest {
  @Rule
  public RuleChain rules = Rules.defaultRules(this);

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
