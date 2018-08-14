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

import ca.qc.ircm.genefinder.util.FileUtils;
import java.io.File;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;

/**
 * Handles drag dropped for TextField containing file path.
 */
public class DragFileDroppedHandler implements EventHandler<DragEvent> {
  protected final TextField text;

  public DragFileDroppedHandler(TextField text) {
    this.text = text;
  }

  @Override
  public void handle(DragEvent event) {
    if (event.getDragboard().hasFiles() && validFile(event.getDragboard().getFiles().get(0))) {
      File file = FileUtils.resolveWindowsShorcut(event.getDragboard().getFiles().get(0));
      text.setText(file.getPath());
      text.positionCaret(text.getText().length());
      event.setDropCompleted(true);
      event.consume();
    } else if (event.getDragboard().hasString()) {
      text.setText(event.getDragboard().getString());
      text.positionCaret(text.getText().length());
      event.setDropCompleted(true);
      event.consume();
    }
  }

  protected boolean validFile(File file) {
    file = FileUtils.resolveWindowsShorcut(file);
    return file.isFile();
  }
}
