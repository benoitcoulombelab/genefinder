/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.genefinder.gui.drag;

import ca.qc.ircm.genefinder.util.FileUtils;
import java.io.File;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

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
