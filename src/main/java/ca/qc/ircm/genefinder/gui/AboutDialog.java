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

import ca.qc.ircm.javafx.JavafxUtils;
import java.util.ResourceBundle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * About dialog.
 */
public class AboutDialog {
  private Stage stage;
  private AboutDialogPresenter presenter;

  /**
   * Shows about dialog.
   *
   * @param owner
   *          dialog's owner
   */
  public AboutDialog(Window owner) {
    stage = new Stage();
    stage.initOwner(owner);
    stage.initModality(Modality.NONE);

    AboutDialogView view = new AboutDialogView();
    presenter = (AboutDialogPresenter) view.getPresenter();
    ResourceBundle resources = view.getResourceBundle();
    Parent root = view.getView();
    JavafxUtils.setMaxSizeForScreen(stage);

    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.setTitle(resources.getString("title"));

    presenter.focusOnDefault();
    stage.show();
  }

  public void show() {
    stage.show();
  }

  public void hide() {
    stage.hide();
  }
}
