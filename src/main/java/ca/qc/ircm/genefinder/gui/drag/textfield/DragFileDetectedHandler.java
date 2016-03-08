package ca.qc.ircm.genefinder.gui.drag.textfield;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.util.Arrays;

/**
 * Handles drag detected for TextField containing file path.
 */
public class DragFileDetectedHandler implements EventHandler<MouseEvent> {
  protected final TextField text;
  protected final TransferMode[] transferModes;

  public DragFileDetectedHandler(TextField text) {
    this(text, TransferMode.ANY);
  }

  public DragFileDetectedHandler(TextField text, TransferMode... transferModes) {
    this.text = text;
    this.transferModes = transferModes;
  }

  @Override
  public void handle(MouseEvent event) {
    Dragboard db = text.startDragAndDrop(transferModes);
    ClipboardContent content = new ClipboardContent();
    text.selectAll();
    File file = new File(text.getSelectedText());
    if (file.isAbsolute() && file.exists()) {
      content.putFiles(Arrays.asList(file));
    }
    content.putString(text.getSelectedText());
    db.setContent(content);
    event.consume();
  }
}
