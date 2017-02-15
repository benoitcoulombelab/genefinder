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

package ca.qc.ircm.genefinder;

import ca.qc.ircm.genefinder.gui.SplashScreen;
import ca.qc.ircm.util.javafx.message.MessageDialog;
import ca.qc.ircm.util.javafx.message.MessageDialog.MessageDialogType;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * JavaFX application preloader.
 */
public class MainPreloader extends Preloader {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private Stage stage;
  private SplashScreen splash;

  @Override
  public void start(Stage primaryStage) throws Exception {
    this.stage = primaryStage;
    splash = new SplashScreen(primaryStage);
    splash.show();
  }

  @Override
  public void handleApplicationNotification(PreloaderNotification info) {
    if (info instanceof ApplicationStarted) {
      splash.hide();
    }
  }

  @Override
  public boolean handleErrorNotification(ErrorNotification info) {
    logger.error("Could not start application", info.getCause());
    com.airhacks.afterburner.injection.Injector.resetInstanceSupplier();
    ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), Locale.getDefault());
    new MessageDialog(stage, MessageDialogType.ERROR, resources.getString("error.title"),
        info.getCause().getMessage()).showAndWait();
    return true;
  }
}
