package ca.qc.ircm.genefinder.data.gui;

import ca.qc.ircm.genefinder.annotation.ProteinDatabase;
import ca.qc.ircm.genefinder.data.FindGenesInDataTask;
import ca.qc.ircm.genefinder.data.FindGenesInDataTaskFactory;
import ca.qc.ircm.genefinder.data.FindGenesParameters;
import ca.qc.ircm.genefinder.data.FindGenesParametersBean;
import ca.qc.ircm.genefinder.gui.FileListCellFactory;
import ca.qc.ircm.genefinder.gui.ProgressDialog;
import ca.qc.ircm.genefinder.gui.drag.DragFilesOverHandler;
import ca.qc.ircm.genefinder.gui.drag.list.DragFileOnListDetectedHandler;
import ca.qc.ircm.genefinder.gui.drag.list.DragFileOnListDoneHandler;
import ca.qc.ircm.genefinder.gui.drag.list.DragFileOnListDroppedHandler;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.organism.gui.OrganismStringConverter;
import ca.qc.ircm.util.javafx.JavafxUtils;
import ca.qc.ircm.util.javafx.message.MessageDialog;
import ca.qc.ircm.util.javafx.message.MessageDialog.MessageDialogType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Inject;

/**
 * Gene finder controller.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GeneFinderPresenter {
  private static final Logger logger = LoggerFactory.getLogger(GeneFinderPresenter.class);
  private ListProperty<Organism> organismsProperty = new SimpleListProperty<>();
  private BooleanProperty geneIdProperty = new SimpleBooleanProperty();
  private BooleanProperty geneNameProperty = new SimpleBooleanProperty();
  private BooleanProperty geneSynonymsProperty = new SimpleBooleanProperty();
  private BooleanProperty geneSummaryProperty = new SimpleBooleanProperty();
  private BooleanProperty proteinMolecularWeightProperty = new SimpleBooleanProperty();
  @FXML
  private ResourceBundle resources;
  @FXML
  private Label filesLabel;
  @FXML
  private ListView<File> files;
  @FXML
  private Label proteinColumnLabel;
  @FXML
  private TextField proteinColumn;
  @FXML
  private Label organismLabel;
  @FXML
  private ChoiceBox<Organism> organism;
  @FXML
  private Label proteinDatabaseLabel;
  @FXML
  private ChoiceBox<ProteinDatabase> proteinDatabase;
  @FXML
  private CheckBox geneId;
  @FXML
  private CheckBox geneName;
  @FXML
  private CheckBox geneSynonyms;
  @FXML
  private CheckBox geneSummary;
  @FXML
  private CheckBox proteinMolecularWeight;
  @Inject
  private FindGenesInDataTaskFactory findGenesInDataTaskFactory;
  private FileChooser fileChooser = new FileChooser();

  @FXML
  private void initialize() {
    fileChooser.getExtensionFilters()
        .add(new ExtensionFilter(resources.getString("file.description"), "*", "*.*"));

    files.setCellFactory(new FileListCellFactory());
    files.setOnDragDetected(new DragFileOnListDetectedHandler(files));
    files.setOnDragDone(new DragFileOnListDoneHandler(files));
    files.setOnDragOver(new DragFilesOverHandler(files, files));
    files.setOnDragDropped(new DragFileOnListDroppedHandler(files, true));
    files.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    files.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.DELETE) {
        removeSelectedFiles();
      }
    });
    organism.setItems(organismsProperty);
    organism.setConverter(new OrganismStringConverter());
    organismsProperty.addListener((ListChangeListener<Organism>) event -> {
      while (event.next()) {
        if (event.wasAdded() || event.wasRemoved()) {
          if (!organism.getItems().isEmpty()) {
            organism.getSelectionModel().select(0);
          }
        }
      }
    });
    proteinDatabase.setItems(FXCollections.observableArrayList(ProteinDatabase.values()));
    proteinDatabase.setConverter(new ProteinDatabaseStringConverter(Locale.getDefault()));
    geneIdProperty.bind(geneId.selectedProperty());
    geneNameProperty.bind(geneName.selectedProperty());
    geneSynonymsProperty.bind(geneSynonyms.selectedProperty());
    geneSummaryProperty.bind(geneSummary.selectedProperty());
    proteinMolecularWeightProperty.bind(proteinMolecularWeight.selectedProperty());

    proteinDatabase.setValue(ProteinDatabase.values()[0]);
    geneId.setSelected(true);
    geneName.setSelected(true);
  }

  public ListProperty<Organism> organismsProperty() {
    return organismsProperty;
  }

  private FindGenesParameters getFindGenesParameters() {
    FindGenesParametersBean parameters = new FindGenesParametersBean();
    parameters.organism(organism.getSelectionModel().getSelectedItem());
    parameters.proteinColumn(parseProteinColumn());
    parameters.proteinDatabase(proteinDatabase.getSelectionModel().getSelectedItem());
    parameters.geneId(geneIdProperty.get());
    parameters.geneName(geneNameProperty.get());
    parameters.geneSynonyms(geneSynonymsProperty.get());
    parameters.geneSummary(geneSummaryProperty.get());
    parameters.proteinMolecularWeight(proteinMolecularWeightProperty.get());
    return parameters;
  }

  private int parseProteinColumn() {
    try {
      return Integer.parseInt(proteinColumn.getText()) - 1;
    } catch (NumberFormatException e) {
      if (!StringUtils.isAlpha(proteinColumn.getText())) {
        int column = 0;
        char[] chars = proteinColumn.getText().toUpperCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
          char letter = chars[chars.length - i - 1];
          column += Math.max(i * 26, 1) * (letter - 'A' + 1);
        }
        return column - 1;
      } else {
        return 0;
      }
    }
  }

  @FXML
  private void addFiles() {
    JavafxUtils.setValidInitialDirectory(fileChooser);
    List<File> selections = fileChooser.showOpenMultipleDialog(files.getScene().getWindow());
    if (selections != null) {
      if (!selections.isEmpty()) {
        fileChooser.setInitialDirectory(selections.get(0).getParentFile());
      }
      selections.forEach(file -> {
        if (!files.getItems().contains(file)) {
          files.getItems().add(file);
        }
      });
    }
  }

  private void removeSelectedFiles() {
    List<Integer> selections = new ArrayList<>(files.getSelectionModel().getSelectedIndices());
    Collections.sort(selections);
    Collections.reverse(selections);
    for (Integer selection : selections) {
      if (selection >= 0) {
        files.getItems().remove(selection.intValue());
      }
    }
  }

  @FXML
  private void start() {
    if (validate()) {
      List<File> files = new ArrayList<>(this.files.getItems());
      final Window window = this.files.getScene().getWindow();
      final FindGenesInDataTask task =
          findGenesInDataTaskFactory.create(files, getFindGenesParameters(), Locale.getDefault());
      final ProgressDialog progressDialog = new ProgressDialog(window, task);
      task.stateProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue == State.FAILED || newValue == State.SUCCEEDED
            || newValue == State.CANCELLED) {
          progressDialog.close();
        }
        if (newValue == State.FAILED) {
          // Show error message.
          Throwable error = task.getException();
          logger.error("failed", error);
          new MessageDialog(window, MessageDialogType.ERROR,
              resources.getString("task.failed.title"), resources.getString("task.failed.message"),
              error.getMessage());
        } else if (newValue == State.SUCCEEDED) {
          // Show confirm message.
          new MessageDialog(window, MessageDialogType.INFORMATION,
              resources.getString("task.succeeded.title"),
              resources.getString("task.succeeded.message"));
        }
      });
      new Thread(task).start();
    }
  }

  private boolean validate() {
    filesLabel.getStyleClass().remove("error");
    files.getStyleClass().remove("error");
    proteinColumnLabel.getStyleClass().remove("error");
    proteinColumn.getStyleClass().remove("error");
    organismLabel.getStyleClass().remove("error");
    organism.getStyleClass().remove("error");
    proteinDatabaseLabel.getStyleClass().remove("error");
    proteinDatabase.getStyleClass().remove("error");
    List<String> errors = new ArrayList<>();
    if (files.getItems().isEmpty()) {
      errors.add(resources.getString("error.files.required"));
      filesLabel.getStyleClass().add("error");
      files.getStyleClass().add("error");
    }
    if (proteinColumn.getText() == null || proteinColumn.getText().isEmpty()) {
      errors.add(resources.getString("proteinColumn.empty"));
      proteinColumnLabel.getStyleClass().add("error");
      proteinColumn.getStyleClass().add("error");
    } else {
      try {
        int column = Integer.parseInt(proteinColumn.getText());
        if (column < 1) {
          errors.add(resources.getString("proteinColumn.belowMinimum"));
          proteinColumnLabel.getStyleClass().add("error");
          proteinColumn.getStyleClass().add("error");
        }
      } catch (NumberFormatException e) {
        if (!StringUtils.isAlpha(proteinColumn.getText())) {
          errors.add(resources.getString("proteinColumn.invalid"));
          proteinColumnLabel.getStyleClass().add("error");
          proteinColumn.getStyleClass().add("error");
        }
      }
    }
    if (organism.getSelectionModel().getSelectedItem() == null) {
      errors.add(resources.getString("error.organism.required"));
      organismLabel.getStyleClass().add("error");
      organism.getStyleClass().add("error");
    }
    if (proteinDatabase.getSelectionModel().getSelectedItem() == null) {
      errors.add(resources.getString("proteinDatabase.required"));
      proteinDatabaseLabel.getStyleClass().add("error");
      proteinDatabase.getStyleClass().add("error");
    }
    boolean valid = errors.isEmpty();
    if (!valid) {
      new MessageDialog(files.getScene().getWindow(), MessageDialogType.ERROR,
          resources.getString("error.title"), errors);
    }
    return valid;
  }
}
