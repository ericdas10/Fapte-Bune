package examen.fapte_bune.gui;

import examen.fapte_bune.domain.Nevoie;
import examen.fapte_bune.domain.Status;
import examen.fapte_bune.service.Service;
import examen.fapte_bune.util.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class MainController implements Observer, Initializable {
    @FXML
    private TableView<Nevoie> nevoiDisponibileTable;
    @FXML
    private TableView<Nevoie> fapteleMeleTable;
    @FXML
    private TextField titluNevoieField;
    @FXML
    private TextArea descriereNevoieField;
    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private TableColumn<Nevoie, String> titluColumn;
    @FXML
    private TableColumn<Nevoie, String> descriereColumn;
    @FXML
    private TableColumn<Nevoie, LocalDateTime> deadlineColumn;
    @FXML
    private TableColumn<Nevoie, String> statusColumn;
    @FXML
    private TableColumn<Nevoie, Void> actionsColumn;

    private Service service;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupInputValidation();
    }

    private void setupColumns() {
        titluColumn.setCellValueFactory(new PropertyValueFactory<>("titlu"));
        descriereColumn.setCellValueFactory(new PropertyValueFactory<>("descriere"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupActionsColumn();
    }

    private void setupInputValidation() {
        titluNevoieField.textProperty().addListener((obs, oldVal, newVal) ->
                titluNevoieField.setStyle(newVal.isEmpty() ? "-fx-border-color: red;" : "-fx-border-color: none;"));

        descriereNevoieField.textProperty().addListener((obs, oldVal, newVal) ->
                descriereNevoieField.setStyle(newVal.isEmpty() ? "-fx-border-color: red;" : "-fx-border-color: none;"));
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");

            {
                acceptButton.setOnAction(event -> {
                    Nevoie nevoie = getTableView().getItems().get(getIndex());
                    try {
                        service.acceptaNevoie(nevoie.getId());
                    } catch (IOException e) {
                        showError("Error accepting need", e);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Nevoie nevoie = getTableView().getItems().get(getIndex());
                    setGraphic(nevoie.getStatus() == Status.Caut_Erou ? acceptButton : null);
                }
            }
        });
    }

    public void setService(Service service) {
        this.service = service;
        service.addObserver(this);
        refreshTables();
    }

    private void refreshTables() {
        try {
            nevoiDisponibileTable.setItems(FXCollections.observableArrayList(
                    service.getNevoiPentruOras(service.getCurrentUser().getOras())
            ));
            fapteleMeleTable.setItems(FXCollections.observableArrayList(
                    service.getFapteBuneForCurrentUser()
            ));
        } catch (IOException e) {
            showError("Error refreshing tables", e);
        }
    }

    @FXML
    private void handleAdaugaNevoie() {
        if (!validateInputs()) return;

        try {
            service.adaugaNevoie(
                    titluNevoieField.getText(),
                    descriereNevoieField.getText(),
                    deadlinePicker.getValue().atStartOfDay()
            );
            clearInputs();
        } catch (IOException e) {
            showError("Error adding need", e);
        }
    }

    private boolean validateInputs() {
        if (titluNevoieField.getText().isEmpty() ||
                descriereNevoieField.getText().isEmpty() ||
                deadlinePicker.getValue() == null) {
            showError("Validation Error", new Exception("Please fill in all fields"));
            return false;
        }
        return true;
    }

    private void clearInputs() {
        titluNevoieField.clear();
        descriereNevoieField.clear();
        deadlinePicker.setValue(null);
    }

    private void showError(String header, Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(header);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        });
    }

    @Override
    public void update() {
        refreshTables();
    }
}