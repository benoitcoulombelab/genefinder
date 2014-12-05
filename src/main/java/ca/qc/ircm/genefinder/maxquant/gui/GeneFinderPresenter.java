package ca.qc.ircm.genefinder.maxquant.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.qc.ircm.genefinder.gui.FileListCellFactory;
import ca.qc.ircm.genefinder.gui.ProgressDialog;
import ca.qc.ircm.genefinder.gui.drag.DragFilesOverHandler;
import ca.qc.ircm.genefinder.gui.drag.list.DragFileOnListDetectedHandler;
import ca.qc.ircm.genefinder.gui.drag.list.DragFileOnListDoneHandler;
import ca.qc.ircm.genefinder.gui.drag.list.DragFileOnListDroppedHandler;
import ca.qc.ircm.genefinder.maxquant.FindGenesInMaxQuantTask;
import ca.qc.ircm.genefinder.maxquant.FindGenesInMaxQuantTaskFactory;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.organism.gui.OrganismStringConverter;
import ca.qc.ircm.util.javafx.JavaFXUtils;
import ca.qc.ircm.util.javafx.message.MessageDialog;
import ca.qc.ircm.util.javafx.message.MessageDialog.MessageDialogType;

/**
 * Gene finder controller.
 */
public class GeneFinderPresenter {
    private static final Logger logger = LoggerFactory.getLogger(GeneFinderPresenter.class);
    private ListProperty<Organism> organismsProperty = new SimpleListProperty<Organism>();
    @FXML
    private ResourceBundle resources;
    @FXML
    private Label filesLabel;
    @FXML
    private ListView<File> files;
    @FXML
    private Label organismLabel;
    @FXML
    private ChoiceBox<Organism> organism;
    @Inject
    private FindGenesInMaxQuantTaskFactory findGenesInMaxQuantTaskFactory;
    private FileChooser fileChooser = new FileChooser();

    @FXML
    private void initialize() {
        fileChooser.getExtensionFilters().add(new ExtensionFilter(resources.getString("file.description"), "*.txt"));

        files.setCellFactory(new FileListCellFactory());
        files.setOnDragDetected(new DragFileOnListDetectedHandler(files));
        files.setOnDragDone(new DragFileOnListDoneHandler(files));
        files.setOnDragOver(new DragFilesOverHandler(files, files));
        files.setOnDragDropped(new DragFileOnListDroppedHandler(files, true));
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
    }

    public ListProperty<Organism> organismsProperty() {
        return organismsProperty;
    }

    @FXML
    private void addFiles() {
        JavaFXUtils.setValidInitialDirectory(fileChooser);
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

    @FXML
    private void start() {
        if (validate()) {
            List<File> files = new ArrayList<File>(this.files.getItems());
            Organism organism = this.organism.getSelectionModel().getSelectedItem();
            final Window window = this.files.getScene().getWindow();
            final FindGenesInMaxQuantTask task = findGenesInMaxQuantTaskFactory.create(organism, files,
                    Locale.getDefault());
            final ProgressDialog progressDialog = new ProgressDialog(window, task);
            task.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == State.FAILED || newValue == State.SUCCEEDED || newValue == State.CANCELLED) {
                    progressDialog.close();
                }
                if (newValue == State.FAILED) {
                    // Show error message.
                    Throwable error = task.getException();
                    logger.error("failed", error);
                    new MessageDialog(window, MessageDialogType.ERROR, resources.getString("task.failed.title"),
                            resources.getString("task.failed.message"), error.getMessage());
                } else if (newValue == State.SUCCEEDED) {
                    // Show confirm message.
                    new MessageDialog(window, MessageDialogType.INFORMATION, resources
                            .getString("task.succeeded.title"), resources.getString("task.succeeded.message"));
                }
            });
            new Thread(task).start();
        }
    }

    private boolean validate() {
        filesLabel.getStyleClass().remove("error");
        files.getStyleClass().remove("error");
        organismLabel.getStyleClass().remove("error");
        organism.getStyleClass().remove("error");
        List<String> errors = new ArrayList<String>();
        if (files.getItems().isEmpty()) {
            errors.add(resources.getString("error.files.required"));
            filesLabel.getStyleClass().add("error");
            files.getStyleClass().add("error");
        }
        if (organism.getSelectionModel().getSelectedItem() == null) {
            errors.add(resources.getString("error.organism.required"));
            organismLabel.getStyleClass().add("error");
            organism.getStyleClass().add("error");
        }
        boolean valid = errors.isEmpty();
        if (!valid) {
            new MessageDialog(files.getScene().getWindow(), MessageDialogType.ERROR,
                    resources.getString("error.title"), errors);
        }
        return valid;
    }
}
