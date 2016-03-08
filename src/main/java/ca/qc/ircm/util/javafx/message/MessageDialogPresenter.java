package ca.qc.ircm.util.javafx.message;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Message dialog controller.
 */
public class MessageDialogPresenter {
  @FXML
  private BorderPane messageDialog;
  @FXML
  private ScrollPane scrollPane;
  @FXML
  private VBox messageBox;
  @FXML
  private Button ok;

  @FXML
  private void initialize() {
    messageBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    ok.requestFocus();
  }

  public void setMessages(ObservableList<String> messages) {
    messages.stream().forEach(message -> {
      messageBox.getChildren().add(new Label(message));
    });
  }

  @FXML
  private void close(Event event) {
    ok.getScene().getWindow().hide();
  }

  void computeMessageBoxBounds() {
    scrollPane.setContent(null);
    Stage messageBoundsStage = new Stage();
    messageDialog.setCenter(messageBox);
    Scene messageBoundsScene = new Scene(messageDialog);
    messageBoundsStage.setScene(messageBoundsScene);
    messageDialog.snapshot(null, null);
    Bounds bounds = messageDialog.getBoundsInLocal();
    messageDialog.setPrefWidth(bounds.getWidth() + 15);
    messageDialog.setPrefHeight(bounds.getHeight() + 15);
    messageDialog.setCenter(scrollPane);
    messageBoundsScene.setRoot(new Label());
    scrollPane.setContent(messageBox);
  }
}
