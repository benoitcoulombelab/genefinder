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

package ca.qc.ircm.genefinder.gui;

import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Splash screen.
 */
public class SplashScreen {
  private Stage stage;

  /**
   * Creates splash screen window.
   *
   * @param stage
   *          stage
   */
  public SplashScreen(Stage stage) {
    this.stage = stage;
    stage.initStyle(StageStyle.UNDECORATED);
    SplashScreenView view = new SplashScreenView();
    Parent viewNode = view.getView();
    viewNode.setCursor(Cursor.WAIT);
    Scene scene = new Scene(viewNode);
    stage.setScene(scene);
  }

  public void show() {
    stage.show();
  }

  public void hide() {
    stage.hide();
  }
}
