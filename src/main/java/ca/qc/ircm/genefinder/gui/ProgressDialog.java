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
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Progress dialog.
 */
public class ProgressDialog {
  Stage stage;
  Worker<?> worker;
  ProgressDialogPresenter presenter;

  /**
   * Creates progress window.
   *
   * @param owner
   *          window's owner
   * @param worker
   *          worker
   */
  public ProgressDialog(Window owner, Worker<?> worker) {
    this.worker = worker;

    ProgressDialogView view = new ProgressDialogView();
    presenter = (ProgressDialogPresenter) view.getPresenter();
    final ResourceBundle resources = view.getResourceBundle();
    stage = new Stage();
    stage.initOwner(owner);
    stage.initModality(Modality.WINDOW_MODAL);
    Scene scene = new Scene(view.getView());
    stage.setScene(scene);
    stage.setTitle(resources.getString("title"));
    JavafxUtils.setMaxSizeForScreen(stage);

    presenter.progressProperty().bind(worker.progressProperty());
    presenter.messageProperty().bind(worker.messageProperty());
    presenter.cancelledProperty().addListener((observable, oldvalue, newvalue) -> close());

    stage.show();
  }

  public void close() {
    worker.cancel();
    stage.close();
  }
}
