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

package ca.qc.ircm.genefinder.gui.drag.textfield;

import java.io.File;
import java.util.Arrays;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Handles drag detected for TextField containing file path.
 */
public class DragFileDetectedHandler implements EventHandler<MouseEvent> {
  protected final TextField text;
  protected final TransferMode[] transferModes;

  public DragFileDetectedHandler(TextField text) {
    this(text, TransferMode.ANY);
  }

  public DragFileDetectedHandler(TextField text, TransferMode... transferModes) {
    this.text = text;
    this.transferModes = transferModes;
  }

  @Override
  public void handle(MouseEvent event) {
    final Dragboard db = text.startDragAndDrop(transferModes);
    ClipboardContent content = new ClipboardContent();
    text.selectAll();
    File file = new File(text.getSelectedText());
    if (file.isAbsolute() && file.exists()) {
      content.putFiles(Arrays.asList(file));
    }
    content.putString(text.getSelectedText());
    db.setContent(content);
    event.consume();
  }
}
