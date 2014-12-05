package ca.qc.ircm.genefinder.gui;

import java.util.ResourceBundle;

import javafx.scene.Scene;
import javafx.stage.Stage;
import ca.qc.ircm.util.javafx.JavaFXUtils;

/**
 * Main application window.
 */
public class ApplicationGui {
    private ResourceBundle resources;
    private Stage stage;

    public ApplicationGui() {
        ApplicationView view = new ApplicationView();
        resources = view.getResourceBundle();
        stage = new Stage();
        stage.setTitle(resources.getString("title"));
        Scene scene = new Scene(view.getView());
        scene.getStylesheets().add("error.css");
        stage.setScene(scene);
        JavaFXUtils.setMaxSizeForScreen(stage);
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }
}