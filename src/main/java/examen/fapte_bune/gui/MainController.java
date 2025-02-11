package examen.fapte_bune.gui;

import examen.fapte_bune.domain.Nevoie;
import examen.fapte_bune.domain.Status;
import examen.fapte_bune.service.Service;
import examen.fapte_bune.util.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        // Pentru tabelul nevoiDisponibileTable
        titluColumn.setCellValueFactory(new PropertyValueFactory<>("titlu"));
        descriereColumn.setCellValueFactory(new PropertyValueFactory<>("descriere"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Formatare specială pentru deadline
        deadlineColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
                }
            }
        });

        // Pentru tabelul fapteleMeleTable - trebuie să adăugăm și coloanele pentru acesta
        TableColumn<Nevoie, String> titluFaptaColumn = new TableColumn<>("Titlu");
        TableColumn<Nevoie, String> descriereFaptaColumn = new TableColumn<>("Descriere");
        TableColumn<Nevoie, LocalDateTime> deadlineFaptaColumn = new TableColumn<>("Deadline");
        TableColumn<Nevoie, Status> statusFaptaColumn = new TableColumn<>("Status");

        titluFaptaColumn.setCellValueFactory(new PropertyValueFactory<>("titlu"));
        descriereFaptaColumn.setCellValueFactory(new PropertyValueFactory<>("descriere"));
        deadlineFaptaColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        statusFaptaColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Același formatter pentru deadline în al doilea tabel
        deadlineFaptaColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
                }
            }
        });

        fapteleMeleTable.getColumns().setAll(
                titluFaptaColumn,
                descriereFaptaColumn,
                deadlineFaptaColumn,
                statusFaptaColumn
        );

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

        // Initial load of data
        Platform.runLater(this::refreshTables);
    }

    private void refreshTables() {
        try {
            // Obținem datele curente din tabele
            ObservableList<Nevoie> currentNevoiDisponibile = nevoiDisponibileTable.getItems();
            ObservableList<Nevoie> currentFapteMele = fapteleMeleTable.getItems();

            // Obținem noile date
            List<Nevoie> newNevoiDisponibile = service.getNevoiPentruOras(service.getCurrentUser().getOras());
            List<Nevoie> newFapteBune = service.getFapteBuneForCurrentUser();

            // Actualizăm tabelele păstrând datele existente
            Platform.runLater(() -> {
                // Pentru nevoiDisponibileTable
                if (currentNevoiDisponibile == null || currentNevoiDisponibile.isEmpty()) {
                    nevoiDisponibileTable.setItems(FXCollections.observableArrayList(newNevoiDisponibile));
                } else {
                    currentNevoiDisponibile.clear();
                    currentNevoiDisponibile.addAll(newNevoiDisponibile);
                }

                // Pentru fapteleMeleTable
                if (currentFapteMele == null || currentFapteMele.isEmpty()) {
                    fapteleMeleTable.setItems(FXCollections.observableArrayList(newFapteBune));
                } else {
                    currentFapteMele.clear();
                    currentFapteMele.addAll(newFapteBune);
                }
            });
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