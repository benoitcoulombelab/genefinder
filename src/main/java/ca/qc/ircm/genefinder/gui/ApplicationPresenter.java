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

package ca.qc.ircm.genefinder.gui;

import ca.qc.ircm.genefinder.data.gui.GeneFinderView;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Main application controller.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ApplicationPresenter {
  @FXML
  private BorderPane layout;
  @FXML
  private MenuBar menu;
  @FXML
  private ResourceBundle resources;

  @FXML
  private void initialize() {
    if (SystemUtils.IS_OS_MAC_OSX) {
      menu.setUseSystemMenuBar(true);
    }
    layout.setPrefHeight(Integer.parseInt(resources.getString("height")));
    layout.setPrefWidth(Integer.parseInt(resources.getString("width")));

    GeneFinderView geneFinderView = new GeneFinderView();
    layout.setCenter(geneFinderView.getView());
  }

  @FXML
  private void about() {
    new AboutDialog(layout.getScene().getWindow());
  }
}
