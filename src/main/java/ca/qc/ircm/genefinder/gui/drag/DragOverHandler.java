package ca.qc.ircm.genefinder.gui.drag;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;

/**
 * Handles drag over.
 */
public abstract class DragOverHandler implements EventHandler<DragEvent> {
  protected final Node dropNode;

  public DragOverHandler(Node dropNode) {
    this.dropNode = dropNode;
  }

  @Override
  public void handle(DragEvent event) {
    if (accept(event)) {
      setAcceptTransferModes(event);
      dropNode.setOpacity(0.65);
      event.consume();
    }
  }

  protected abstract boolean accept(DragEvent event);

  protected abstract void setAcceptTransferModes(DragEvent event);
}
