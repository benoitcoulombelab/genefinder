package ca.qc.ircm.genefinder.gui.drag.list;

import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

import java.io.File;

/**
 * Handles drag done for TextField containing file path.
 */
public class DragFileOnListDoneHandler implements EventHandler<DragEvent> {
  protected final ListView<File> list;

  public DragFileOnListDoneHandler(ListView<File> list) {
    this.list = list;
  }

  @Override
  public void handle(DragEvent event) {
    if (event.getTransferMode() == TransferMode.MOVE) {
      list.getItems().remove(event.getDragboard().getFiles());
    }
    event.consume();
  }
}
