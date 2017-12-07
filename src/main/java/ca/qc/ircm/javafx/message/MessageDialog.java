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

package ca.qc.ircm.javafx.message;

import ca.qc.ircm.javafx.JavafxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Message window.
 */
public class MessageDialog {
  public static enum MessageDialogType {
    ERROR, WARNING, INFORMATION, QUESTION
  }

  private ResourceBundle resources;
  private Stage stage;
  private BorderPane messageDialog = new BorderPane();
  private ScrollPane scrollPane = new ScrollPane();
  private VBox messageBox = new VBox();
  private HBox buttons = new HBox();
  private Button ok = new Button();

  public MessageDialog(MessageDialogType type, String title, String... messages) {
    this(null, type, title, Arrays.asList(messages));
  }

  public MessageDialog(MessageDialogType type, String title, List<String> messages) {
    this(null, type, title, messages);
  }

  public MessageDialog(Window owner, MessageDialogType type, String title, String... messages) {
    this(owner, type, title, Arrays.asList(messages));
  }

  /**
   * Creates message window.
   *
   * @param owner
   *          window's owner
   * @param type
   *          message type
   * @param title
   *          window's title
   * @param messages
   *          messages to show
   */
  public MessageDialog(Window owner, MessageDialogType type, String title, List<String> messages) {
    resources = ResourceBundle.getBundle(getClass().getName(), Locale.getDefault());
    initView();
    setMessages(FXCollections.observableArrayList(messages));
    computeMessageBoxBounds();

    stage = new Stage();
    if (owner != null) {
      stage.initOwner(owner);
      stage.initModality(Modality.WINDOW_MODAL);
    }
    Scene scene = new Scene(messageDialog);
    stage.setScene(scene);
    stage.setTitle(title);
    JavafxUtils.setMaxSizeForScreen(stage);
    if (type != null) {
      Image icon = getIcon(type);
      if (icon != null) {
        stage.getIcons().add(icon);
      }
    }
    stage.show();
  }

  private void initView() {
    messageDialog.getStylesheets().add("/" + getClass().getName().replace(".", "/") + ".css");
    messageDialog.getStyleClass().add("message-dialog");
    messageDialog.setMinWidth(200);
    messageDialog.setMinHeight(50);
    messageDialog.setCenter(scrollPane);
    scrollPane.setContent(messageBox);
    messageBox.getStyleClass().add("messages");
    messageBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    messageDialog.setBottom(buttons);
    buttons.getStyleClass().add("buttons");
    buttons.getChildren().add(ok);
    ok.setText(resources.getString("ok"));
    ok.setOnAction(e -> close(e));
    ok.setDefaultButton(true);
    ok.setCancelButton(true);
    ok.requestFocus();
  }

  /**
   * Sets messages to show.
   *
   * @param messages
   *          messages to show
   */
  public void setMessages(ObservableList<String> messages) {
    messages.stream().forEach(message -> {
      messageBox.getChildren().add(new Label(message));
    });
  }

  private void close(Event event) {
    ok.getScene().getWindow().hide();
  }

  private void computeMessageBoxBounds() {
    scrollPane.setContent(null);
    Stage messageBoundsStage = new Stage();
    messageBoundsStage.setOpacity(0.0);
    messageDialog.setCenter(messageBox);
    Scene messageBoundsScene = new Scene(messageDialog);
    messageBoundsStage.setScene(messageBoundsScene);
    messageBoundsStage.show();
    Bounds bounds = messageDialog.getBoundsInLocal();
    messageDialog.setPrefWidth(bounds.getWidth() + 15);
    messageDialog.setPrefHeight(bounds.getHeight() + 15);
    messageBoundsStage.hide();
    messageDialog.setCenter(scrollPane);
    messageBoundsScene.setRoot(new Label());
    scrollPane.setContent(messageBox);
  }

  private Image getIcon(MessageDialogType type) {
    Image image = null;
    switch (type) {
      case ERROR:
        image = new Image(getResourcePath("dialog-error-3.png"));
        break;
      case WARNING:
        image = new Image(getResourcePath("dialog-warning.png"));
        break;
      case INFORMATION:
        image = new Image(getResourcePath("dialog-information-3.png"));
        break;
      case QUESTION:
        image = new Image(getResourcePath("dialog-question-2.png"));
        break;
      default:
        image = new Image(getResourcePath("dialog-information-3.png"));
        break;
    }
    return image;
  }

  public void showAndWait() {
    stage.hide();
    stage.showAndWait();
  }

  private String getResourcePath(String resource) {
    String resourceFolder = getClass().getPackage().getName().replaceAll("\\.", "/");
    return resourceFolder + "/" + resource;
  }
}
