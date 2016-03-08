package ca.qc.ircm.genefinder.gui.drag.textfield;

import ca.qc.ircm.genefinder.util.FileUtils;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;

import java.io.File;

/**
 * Handles drag dropped for TextField containing directory path.
 */
public class DragDirectoryDroppedHandler extends DragFileDroppedHandler
    implements EventHandler<DragEvent> {
  public DragDirectoryDroppedHandler(TextField text) {
    super(text);
  }

  @Override
  protected boolean validFile(File file) {
    file = FileUtils.resolveWindowsShorcut(file);
    return file.isDirectory();
  }
}
