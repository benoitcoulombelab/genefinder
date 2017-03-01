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

import ca.qc.ircm.util.javafx.JavafxUtils;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * Main application window.
 */
public class ApplicationGui {
  private ResourceBundle resources;
  private Stage stage;

  /**
   * Creates application window.
   */
  public ApplicationGui() {
    ApplicationView view = new ApplicationView();
    resources = view.getResourceBundle();
    stage = new Stage();
    stage.setTitle(resources.getString("title"));
    Scene scene = new Scene(view.getView());
    scene.getStylesheets().add("error.css");
    stage.setScene(scene);
    JavafxUtils.setMaxSizeForScreen(stage);
  }

  public void show() {
    stage.show();
  }

  public void hide() {
    stage.hide();
  }
}
