package ca.qc.ircm.genefinder.gui;

import java.util.ResourceBundle;

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ca.qc.ircm.util.javafx.JavaFXUtils;

/**
 * Progress dialog.
 */
public class ProgressDialog {
    private Stage stage;
    private Worker<?> worker;

    public ProgressDialog(Window owner, Worker<?> worker) {
        this.worker = worker;

        ProgressDialogView view = new ProgressDialogView();
        ProgressDialogPresenter presenter = (ProgressDialogPresenter) view.getPresenter();
        ResourceBundle resources = view.getResourceBundle();
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        Scene scene = new Scene(view.getView());
        stage.setScene(scene);
        stage.setTitle(resources.getString("title"));
        JavaFXUtils.setMaxSizeForScreen(stage);

        presenter.progressProperty().bind(worker.progressProperty());
        presenter.messageProperty().bind(worker.messageProperty());
        presenter.cancelledProperty().addListener((ob, o, n) -> close());

        stage.show();
    }

    public void close() {
        worker.cancel();
        stage.close();
    }
}
