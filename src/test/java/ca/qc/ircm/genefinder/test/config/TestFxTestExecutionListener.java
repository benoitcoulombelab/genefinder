/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

import javafx.application.Platform;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Ensure JavaFX toolkit is started.
 */
public class TestFxTestExecutionListener extends AbstractTestExecutionListener {
  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    if (System.getProperty("os.name").startsWith("Mac")) {
      System.setProperty("java.awt.headless", "false");
    }
    FxToolkit.registerPrimaryStage();
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    Object testInstance = testContext.getTestInstance();
    if (testInstance instanceof ApplicationTest) {
      closeAllWindows((ApplicationTest) testInstance);
    }
  }

  private void closeAllWindows(ApplicationTest testInstance)
      throws InterruptedException, ExecutionException {
    FutureTask<Void> closeAllWindows =
        new FutureTask<>(() -> testInstance.listWindows().forEach(w -> w.hide()), null);
    Platform.runLater(closeAllWindows);
    closeAllWindows.get();
  }
}
