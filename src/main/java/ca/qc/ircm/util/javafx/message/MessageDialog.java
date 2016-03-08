package ca.qc.ircm.util.javafx.message;

import ca.qc.ircm.util.javafx.JavaFXUtils;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Arrays;
import java.util.List;

/**
 * Message window.
 */
public class MessageDialog {
  public static enum MessageDialogType {
    ERROR, WARNING, INFORMATION, QUESTION
  }

  private Stage stage;

  public MessageDialog(MessageDialogType type, String title, String... messages) {
    this(null, type, title, Arrays.asList(messages));
  }

  public MessageDialog(MessageDialogType type, String title, List<String> messages) {
    this(null, type, title, messages);
  }

  public MessageDialog(Window owner, MessageDialogType type, String title, String... messages) {
    this(owner, type, title, Arrays.asList(messages));
  }

  public MessageDialog(Window owner, MessageDialogType type, String title, List<String> messages) {
    MessageDialogView view = new MessageDialogView();
    MessageDialogPresenter presenter = (MessageDialogPresenter) view.getPresenter();
    Region viewNode = (Region) view.getView();
    presenter.setMessages(FXCollections.observableArrayList(messages));
    presenter.computeMessageBoxBounds();

    stage = new Stage();
    if (owner != null) {
      stage.initOwner(owner);
      stage.initModality(Modality.WINDOW_MODAL);
    }
    Scene scene = new Scene(viewNode);
    stage.setScene(scene);
    stage.setTitle(title);
    JavaFXUtils.setMaxSizeForScreen(stage);
    if (type != null) {
      Image icon = getIcon(type);
      if (icon != null) {
        stage.getIcons().add(icon);
      }
    }
    stage.show();
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
    }
    return image;
  }

  private String getResourcePath(String resource) {
    String resourceFolder = getClass().getPackage().getName().replaceAll("\\.", "/");
    return resourceFolder + "/" + resource;
  }
}
