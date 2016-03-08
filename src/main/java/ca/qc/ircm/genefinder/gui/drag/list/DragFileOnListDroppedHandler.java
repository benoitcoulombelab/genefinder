package ca.qc.ircm.genefinder.gui.drag.list;

import ca.qc.ircm.genefinder.util.FileUtils;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;

import java.io.File;

/**
 * Handles drag dropped for TextField containing file path.
 */
public class DragFileOnListDroppedHandler implements EventHandler<DragEvent> {
  private final ListView<File> list;
  private final boolean rejectDuplicate;

  public DragFileOnListDroppedHandler(ListView<File> list) {
    this(list, false);
  }

  public DragFileOnListDroppedHandler(ListView<File> list, boolean rejectDuplicate) {
    this.list = list;
    this.rejectDuplicate = rejectDuplicate;
  }

  @Override
  public void handle(DragEvent event) {
    if (event.getDragboard().hasFiles()) {
      event.getDragboard().getFiles().forEach(file -> {
        addFileToList(file);
      });
    } else if (event.getDragboard().hasString()) {
      String[] fileLocations = event.getDragboard().getString().split("\\r?\\n");
      for (int i = 0; i < fileLocations.length; i++) {
        File file = new File(fileLocations[i]);
        addFileToList(file);
      }
    }
  }

  private void addFileToList(File file) {
    if (validFile(file)) {
      File resolved = FileUtils.resolveWindowsShorcut(file);
      if (!rejectDuplicate || !list.getItems().contains(resolved)) {
        list.getItems().add(resolved);
      }
    }
  }

  private boolean validFile(File file) {
    file = FileUtils.resolveWindowsShorcut(file);
    return file.isFile();
  }
}
