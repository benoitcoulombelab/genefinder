package ca.qc.ircm.genefinder.gui;

import ca.qc.ircm.util.javafx.JavafxUtils;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ResourceBundle;

/**
 * Progress dialog.
 */
public class ProgressDialog {
  private Stage stage;
  private Worker<?> worker;

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
    final ProgressDialogPresenter presenter = (ProgressDialogPresenter) view.getPresenter();
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
