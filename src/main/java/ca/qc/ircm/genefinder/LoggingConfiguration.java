package ca.qc.ircm.genefinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Configures java logging for tests.
 */
public class LoggingConfiguration {
  public static void init() {
    try (InputStream config =
        LoggingConfiguration.class.getResourceAsStream("/logging.properties")) {
      LogManager.getLogManager().readConfiguration(config);
      // Seems required or logs don't work.
      Logger.getLogger("ca.qc.ircm.maxquantgenefinder").isLoggable(Level.FINE);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
