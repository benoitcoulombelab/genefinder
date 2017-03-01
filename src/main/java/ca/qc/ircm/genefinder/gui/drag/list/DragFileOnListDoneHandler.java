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
