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
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ResourceBundle;

/**
 * Main application controller.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ApplicationPresenter {
  @FXML
  private ResourceBundle resources;
  @FXML
  private BorderPane layout;

  @FXML
  private void initialize() {
    layout.setPrefHeight(Integer.parseInt(resources.getString("height")));
    layout.setPrefWidth(Integer.parseInt(resources.getString("width")));

    GeneFinderView geneFinderView = new GeneFinderView();
    layout.setCenter(geneFinderView.getView());
  }
}
