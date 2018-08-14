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
public class DragFilesOverHandler extends DragOverHandler implements EventHandler<DragEvent> {
  private final Node excludeDragNode;

  public DragFilesOverHandler(Node dropNode, Node excludeDragNode) {
    super(dropNode);
    this.excludeDragNode = excludeDragNode;
  }

  @Override
  protected boolean accept(DragEvent event) {
    boolean accept = false;
    if (event.getGestureSource() != excludeDragNode) {
      // Accept files.
      accept |= event.getDragboard().hasFiles() && validFiles(event.getDragboard().getFiles());
      // Accept any string.
      accept |= event.getDragboard().hasString();
    }
    return accept;
  }

  private boolean validFiles(Iterable<File> files) {
    boolean validFiles = true;
    for (File file : files) {
      file = FileUtils.resolveWindowsShorcut(file);
      if (!file.isFile()) {
        validFiles = false;
      }
    }
    return validFiles;
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
