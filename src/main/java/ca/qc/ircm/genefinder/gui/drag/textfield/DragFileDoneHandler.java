package ca.qc.ircm.genefinder.gui.drag.textfield;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

/**
 * Handles drag done for TextField containing file path.
 */
public class DragFileDoneHandler implements EventHandler<DragEvent> {
  protected final TextField text;

  public DragFileDoneHandler(TextField text) {
    this.text = text;
  }

  @Override
  public void handle(DragEvent event) {
    if (event.getTransferMode() == TransferMode.MOVE) {
      text.deleteText(text.getSelection());
    }
    event.consume();
  }
}
