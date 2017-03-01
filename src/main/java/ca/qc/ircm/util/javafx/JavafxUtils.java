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

package ca.qc.ircm.util.javafx;

import javafx.geometry.Rectangle2D;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

/**
 * Utilities for JavaFX.
 */
public class JavafxUtils {
  /**
   * Set maximum size for screen.
   *
   * @param stage
   *          stage
   */
  public static void setMaxSizeForScreen(Stage stage) {
    Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
    stage.setMaxHeight(visualBounds.getHeight());
    stage.setMaxWidth(visualBounds.getWidth());
  }

  /**
   * Sets a valid directory in file chooser.
   *
   * @param fileChooser
   *          file chooser
   */
  public static void setValidInitialDirectory(FileChooser fileChooser) {
    File initialDirectory = fileChooser.getInitialDirectory();
    while (initialDirectory != null && !initialDirectory.exists()) {
      initialDirectory = initialDirectory.getParentFile();
    }
    fileChooser.setInitialDirectory(initialDirectory);
  }

  /**
   * Sets a valid directory in directory chooser.
   * 
   * @param directoryChooser
   *          directory chooser
   */
  public static void setValidInitialDirectory(DirectoryChooser directoryChooser) {
    File initialDirectory = directoryChooser.getInitialDirectory();
    while (initialDirectory != null && !initialDirectory.exists()) {
      initialDirectory = initialDirectory.getParentFile();
    }
    directoryChooser.setInitialDirectory(initialDirectory);
  }
}
