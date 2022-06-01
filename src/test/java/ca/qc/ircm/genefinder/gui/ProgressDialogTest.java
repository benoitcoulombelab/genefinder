/*
 * Copyright (c) 2020 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.genefinder.gui;

import static ca.qc.ircm.genefinder.test.config.JavaFxTestUtils.waitForPlatform;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.genefinder.test.config.TestFxTestAnnotations;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@TestFxTestAnnotations
@ExtendWith(ApplicationExtension.class)
public class ProgressDialogTest {
  private ProgressDialog dialog;
  private Scene scene;

  @Start
  private void start(Stage stage) throws Exception {
    scene = new Scene(new BorderPane());
    stage.setScene(scene);
  }

  private ProgressDialog dialog(Task<?> task) throws InterruptedException, ExecutionException {
    FutureTask<ProgressDialog> future = new FutureTask<>(() -> {
      return new ProgressDialog(scene.getWindow(), task);
    });
    Platform.runLater(future);
    return future.get();
  }

  @Test
  public void updateMessage(FxRobot robot) throws Throwable {
    UpdateMessageTask task = new UpdateMessageTask();
    dialog = dialog(task);
    Thread thread = new Thread(task);
    thread.start();
    thread.join();
    waitForPlatform();
    assertEquals(task.message, dialog.presenter.message.getText());
  }

  @Test
  public void updateProgress(FxRobot robot) throws Throwable {
    UpdateProgressTask task = new UpdateProgressTask();
    dialog = dialog(task);
    Thread thread = new Thread(task);
    thread.start();
    thread.join();
    waitForPlatform();
    assertEquals(task.progress, dialog.presenter.progressIndicator.getProgress());
    assertEquals(task.progress, dialog.presenter.progressBar.getProgress());
  }

  @Test
  @Disabled("Does not work on Mojave right now")
  public void cancel(FxRobot robot) throws Throwable {
    BlockingTask task = new BlockingTask();
    dialog = dialog(task);
    Thread thread = new Thread(task);
    thread.start();
    robot.sleep(100);
    robot.clickOn(".button");
    assertTrue(task.isCancelled());
  }

  private class UpdateTitleTask extends Task<Void> {
    private String title = "Test";

    @Override
    protected Void call() throws Exception {
      updateTitle(title);
      Thread.sleep(100);
      return null;
    }
  }

  private class UpdateMessageTask extends Task<Void> {
    private String message = "Test";

    @Override
    protected Void call() throws Exception {
      updateMessage(message);
      Thread.sleep(100);
      return null;
    }
  }

  private class UpdateProgressTask extends Task<Void> {
    private double progress = 0.6;

    @Override
    protected Void call() throws Exception {
      updateProgress(progress, 1.0);
      Thread.sleep(100);
      return null;
    }
  }

  private class BlockingTask extends Task<Void> {
    @Override
    protected Void call() throws Exception {
      Thread.sleep(10000);
      return null;
    }
  }
}
