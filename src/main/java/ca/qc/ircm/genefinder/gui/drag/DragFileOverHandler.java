package ca.qc.ircm.genefinder.gui.drag;

import ca.qc.ircm.genefinder.util.FileUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

import java.io.File;

/**
 * Handles drag over for TextField containing file path.
 */
public class DragFileOverHandler extends DragOverHandler implements EventHandler<DragEvent> {
  private final Node dragNode;

  public DragFileOverHandler(Node dropNode, Node dragNode) {
    super(dropNode);
    this.dragNode = dragNode;
  }

  @Override
  protected boolean accept(DragEvent event) {
    boolean accept = false;
    if (event.getGestureSource() != dragNode) {
      // Accept file.
      accept |= event.getDragboard().hasFiles() && event.getDragboard().getFiles().size() == 1
          && validFile(event.getDragboard().getFiles().get(0));
      // Accept string with a single line.
      accept |=
          event.getDragboard().hasString() && !event.getDragboard().getString().contains("\n");
    }
    return accept;
  }

  private boolean validFile(File file) {
    file = FileUtils.resolveWindowsShorcut(file);
    return file.isFile();
  }

  @Override
  protected void setAcceptTransferModes(DragEvent event) {
    if (event.getGestureSource() instanceof Node) {
      event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    } else {
      event.acceptTransferModes(TransferMode.COPY);
    }
  }
}
