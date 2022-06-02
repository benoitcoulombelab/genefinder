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

package ca.qc.ircm.genefinder.test.config;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;

/**
 * Utilities for JavaFX tests.
 */
public class JavaFxTestUtils {
  /**
   * Wait for all JavaFX tasks to complete.
   */
  public static void waitForPlatform() {
    FutureTask<Void> waitForPlatform = new FutureTask<>((Callable<Void>) () -> null);
    Platform.runLater(waitForPlatform);
    try {
      waitForPlatform.get();
    } catch (InterruptedException | ExecutionException e) {
      // Ignore.
    }
  }
}
