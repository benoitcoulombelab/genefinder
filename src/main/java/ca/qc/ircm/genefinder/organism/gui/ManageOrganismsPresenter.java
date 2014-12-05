package ca.qc.ircm.genefinder.organism.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;

import javax.inject.Inject;

import ca.qc.ircm.genefinder.NamedComparator;
import ca.qc.ircm.genefinder.gui.NullOnExceptionConverter;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.organism.OrganismService;
import ca.qc.ircm.util.javafx.message.MessageDialog;
import ca.qc.ircm.util.javafx.message.MessageDialog.MessageDialogType;

/**
 * Manage organism controller.
 */
public class ManageOrganismsPresenter {
    private ListProperty<Organism> organismsProperty = new SimpleListProperty<Organism>();
    private StringProperty nameProperty = new SimpleStringProperty();
    private ObjectProperty<Integer> idProperty = new SimpleObjectProperty<Integer>();
    @FXML
    private ResourceBundle resources;
    @FXML
    private TableView<Organism> organisms;
    @FXML
    private TableColumn<Organism, String> nameColumn;
    @FXML
    private TableColumn<Organism, Number> idColumn;
    @FXML
    private Label nameLabel;
    @FXML
    private TextField name;
    @FXML
    private Label idLabel;
    @FXML
    private TextField id;
    @Inject
    private OrganismService organismService;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new Callback<CellDataFeatures<Organism, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Organism, String> organismFeatures) {
                return new SimpleStringProperty(organismFeatures.getValue(), "name", organismFeatures.getValue()
                        .getName());
            }
        });
        idColumn.setCellValueFactory(new Callback<CellDataFeatures<Organism, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(CellDataFeatures<Organism, Number> organismFeatures) {
                return new SimpleIntegerProperty(organismFeatures.getValue(), "id", organismFeatures.getValue().getId());
            }
        });
        name.textProperty().bindBidirectional(nameProperty);
        id.textProperty().bindBidirectional(idProperty,
                new NullOnExceptionConverter<Integer>(new IntegerStringConverter()));
        organisms.setItems(organismsProperty);

        // Default values.
        updateOrganisms();
    }

    public ListProperty<Organism> organismsProperty() {
        return organismsProperty;
    }

    private void updateOrganisms() {
        List<Organism> organisms = organismService.all();
        Collections.sort(organisms, new NamedComparator());
        organismsProperty.set(FXCollections.observableArrayList(organisms));
    }

    @FXML
    private void delete() {
        organismService.delete(organisms.getSelectionModel().getSelectedItems());
        updateOrganisms();
    }

    @FXML
    private void save() {
        if (validateAdd()) {
            Organism organism = new Organism(idProperty.get(), nameProperty.get());
            organismService.insert(organism);
            updateOrganisms();
        }
    }

    private boolean validateAdd() {
        nameLabel.getStyleClass().remove("error");
        name.getStyleClass().remove("error");
        idLabel.getStyleClass().remove("error");
        id.getStyleClass().remove("error");
        List<String> errors = new ArrayList<String>();
        if (nameProperty.get() == null || nameProperty.get().isEmpty()) {
            errors.add(resources.getString("error.name.required"));
            nameLabel.getStyleClass().add("error");
            name.getStyleClass().add("error");
        }
        if (id.getText() == null || id.getText().isEmpty()) {
            errors.add(resources.getString("error.id.required"));
            idLabel.getStyleClass().add("error");
            id.getStyleClass().add("error");
        } else if (idProperty.get() == null) {
            errors.add(resources.getString("error.id.notNumber"));
            idLabel.getStyleClass().add("error");
            id.getStyleClass().add("error");
        } else if (organismService.get(idProperty.get()) != null) {
            errors.add(resources.getString("error.id.exists"));
            idLabel.getStyleClass().add("error");
            id.getStyleClass().add("error");
        }
        boolean valid = errors.isEmpty();
        if (!valid) {
            new MessageDialog(name.getScene().getWindow(), MessageDialogType.ERROR, resources.getString("error.title"),
                    errors);
        }
        return valid;
    }
}
