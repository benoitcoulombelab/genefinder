package ca.qc.ircm.genefinder.gui.drag;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;

/**
 * Handles drag exited.
 */
public class DragExitedHandler implements EventHandler<DragEvent> {
  protected final Node dropNode;

  public DragExitedHandler(Node dropNode) {
    this.dropNode = dropNode;
  }

  @Override
  public void handle(DragEvent event) {
    dropNode.setOpacity(1.0);
    event.consume();
  }
}
