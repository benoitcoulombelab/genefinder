package ca.qc.ircm.genefinder.gui.drag.textfield;

import ca.qc.ircm.genefinder.util.FileUtils;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;

import java.io.File;

/**
 * Handles drag dropped for TextField containing file path.
 */
public class DragFileDroppedHandler implements EventHandler<DragEvent> {
  protected final TextField text;

  public DragFileDroppedHandler(TextField text) {
    this.text = text;
  }

  @Override
  public void handle(DragEvent event) {
    if (event.getDragboard().hasFiles() && validFile(event.getDragboard().getFiles().get(0))) {
      File file = FileUtils.resolveWindowsShorcut(event.getDragboard().getFiles().get(0));
      text.setText(file.getPath());
      text.positionCaret(text.getText().length());
      event.setDropCompleted(true);
      event.consume();
    } else if (event.getDragboard().hasString()) {
      text.setText(event.getDragboard().getString());
      text.positionCaret(text.getText().length());
      event.setDropCompleted(true);
      event.consume();
    }
  }

  protected boolean validFile(File file) {
    file = FileUtils.resolveWindowsShorcut(file);
    return file.isFile();
  }
}
