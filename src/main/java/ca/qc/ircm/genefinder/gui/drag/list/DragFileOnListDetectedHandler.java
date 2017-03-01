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

package ca.qc.ircm.genefinder.gui.drag.list;

import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.util.List;

/**
 * Handles drag detected for TextField containing file path.
 */
public class DragFileOnListDetectedHandler implements EventHandler<MouseEvent> {
  protected final ListView<File> list;
  protected final TransferMode[] transferModes;

  public DragFileOnListDetectedHandler(ListView<File> list) {
    this(list, TransferMode.ANY);
  }

  public DragFileOnListDetectedHandler(ListView<File> list, TransferMode... transferModes) {
    this.list = list;
    this.transferModes = transferModes;
  }

  @Override
  public void handle(MouseEvent event) {
    final Dragboard db = list.startDragAndDrop(transferModes);
    ClipboardContent content = new ClipboardContent();
    List<File> files = list.getSelectionModel().getSelectedItems();
    content.putFiles(files);
    StringBuilder builder = new StringBuilder();
    files.forEach(file -> {
      builder.append("\n");
      builder.append(file.getAbsolutePath());
    });
    if (builder.length() > 0) {
      builder.deleteCharAt(0);
    }
    content.putString(builder.toString());
    db.setContent(content);
    event.consume();
  }
}
