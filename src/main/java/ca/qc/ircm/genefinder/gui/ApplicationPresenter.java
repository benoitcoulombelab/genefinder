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
