package ca.qc.ircm.genefinder.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Progress dialog controller.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProgressDialogPresenter {
  private final BooleanProperty cancelledProperty = new SimpleBooleanProperty();
  @FXML
  private BorderPane layout;
  @FXML
  private ProgressBar progressBar;
  @FXML
  private ProgressIndicator progressIndicator;
  @FXML
  private Label message;
  @FXML
  private Button cancel;

  @FXML
  private void initialize() {
    progressIndicator.progressProperty().bind(progressBar.progressProperty());

    cancel.requestFocus();
  }

  public DoubleProperty progressProperty() {
    return progressBar.progressProperty();
  }

  public StringProperty messageProperty() {
    return message.textProperty();
  }

  public ReadOnlyBooleanProperty cancelledProperty() {
    return cancelledProperty;
  }

  @FXML
  private void cancel(Event event) {
    cancelledProperty.set(true);
  }
}
