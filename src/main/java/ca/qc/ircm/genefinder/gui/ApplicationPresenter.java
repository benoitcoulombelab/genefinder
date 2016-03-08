package ca.qc.ircm.genefinder.gui;

import ca.qc.ircm.genefinder.data.gui.GeneFinderPresenter;
import ca.qc.ircm.genefinder.data.gui.GeneFinderView;
import ca.qc.ircm.genefinder.organism.gui.ManageOrganismsPresenter;
import ca.qc.ircm.genefinder.organism.gui.ManageOrganismsView;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.Region;

import java.util.ResourceBundle;

/**
 * Main application controller.
 */
public class ApplicationPresenter {
  @FXML
  private ResourceBundle resources;
  @FXML
  private Region layout;
  @FXML
  private Tab geneFinderTab;
  @FXML
  private Tab organismsTab;

  @FXML
  private void initialize() {
    layout.setPrefHeight(Integer.parseInt(resources.getString("height")));
    layout.setPrefWidth(Integer.parseInt(resources.getString("width")));

    GeneFinderView geneFinderView = new GeneFinderView();
    GeneFinderPresenter geneFinderPresenter = (GeneFinderPresenter) geneFinderView.getPresenter();
    ManageOrganismsView manageOrganismsView = new ManageOrganismsView();
    ManageOrganismsPresenter manageOrganismsPresenter =
        (ManageOrganismsPresenter) manageOrganismsView.getPresenter();
    geneFinderPresenter.organismsProperty()
        .bindBidirectional(manageOrganismsPresenter.organismsProperty());
    geneFinderTab.setContent(geneFinderView.getView());
    organismsTab.setContent(manageOrganismsView.getView());
  }
}
