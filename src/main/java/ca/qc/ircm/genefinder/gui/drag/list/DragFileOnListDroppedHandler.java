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

import ca.qc.ircm.genefinder.util.FileUtils;
import java.io.File;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;

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
