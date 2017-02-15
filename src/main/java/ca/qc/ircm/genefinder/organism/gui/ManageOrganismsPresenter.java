package ca.qc.ircm.genefinder.organism.gui;

import ca.qc.ircm.genefinder.NamedComparator;
import ca.qc.ircm.genefinder.gui.NullOnExceptionConverter;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.organism.OrganismService;
import ca.qc.ircm.util.javafx.message.MessageDialog;
import ca.qc.ircm.util.javafx.message.MessageDialog.MessageDialogType;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

/**
 * Manage organism controller.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ManageOrganismsPresenter {
  private ListProperty<Organism> organismsProperty = new SimpleListProperty<>();
  private StringProperty nameProperty = new SimpleStringProperty();
  private ObjectProperty<Integer> idProperty = new SimpleObjectProperty<>();
  @FXML
  private ResourceBundle resources;
  @FXML
  private TableView<Organism> organisms;
  @FXML
  private TableColumn<Organism, String> nameColumn;
  @FXML
  private TableColumn<Organism, Integer> idColumn;
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
    organisms.setEditable(true);
    nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    nameColumn.setCellValueFactory(organismFeatures -> {
      return new SimpleStringProperty(organismFeatures.getValue(), "name",
          organismFeatures.getValue().getName());
    });
    nameColumn.setOnEditCommit(event -> {
      Organism organism = event.getTableView().getItems().get(event.getTablePosition().getRow());
      organism.setName(event.getNewValue());
      organismService.update(organism);
      updateOrganisms();
    });
    idColumn.setCellFactory(TextFieldTableCell
        .forTableColumn(new NullOnExceptionConverter<>(new IntegerStringConverter())));
    idColumn.setCellValueFactory(organismFeatures -> {
      return new SimpleObjectProperty<>(organismFeatures.getValue(), "id",
          organismFeatures.getValue().getId());
    });
    idColumn.setOnEditCommit(event -> {
      if (event.getNewValue() != null) {
        Organism organism = event.getTableView().getItems().get(event.getTablePosition().getRow());
        organism.setId(event.getNewValue());
        organismService.update(organism);
        updateOrganisms();
      } else {
        organisms.getColumns().get(0).setVisible(false);
        organisms.getColumns().get(0).setVisible(true);
      }
    });
    name.textProperty().bindBidirectional(nameProperty);
    id.textProperty().bindBidirectional(idProperty,
        new NullOnExceptionConverter<>(new IntegerStringConverter()));
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
    List<String> errors = new ArrayList<>();
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
      new MessageDialog(name.getScene().getWindow(), MessageDialogType.ERROR,
          resources.getString("error.title"), errors);
    }
    return valid;
  }
}
