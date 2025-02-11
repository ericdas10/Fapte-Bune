package examen.fapte_bune.gui;

import examen.fapte_bune.service.Service;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private ListView<String> usernameListView;
    @FXML
    private Label errorLabel;

    private Service service;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UI components
        usernameListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        errorLabel.setText("");

        // Set cell factory for better display
        usernameListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // Add listener for selection changes
        usernameListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                errorLabel.setText("");
            }
        });
    }

    public void setService(Service service) {
        this.service = service;
        loadUsernames();
    }

    private void loadUsernames() {
        try {
            usernameListView.setItems(FXCollections.observableArrayList(service.getAllUsernames()));
        } catch (IOException e) {
            errorLabel.setText("Error loading usernames: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String selectedUsername = usernameListView.getSelectionModel().getSelectedItem();
        if (selectedUsername == null) {
            errorLabel.setText("Please select a username");
            return;
        }

        try {
            service.login(selectedUsername);
            openMainWindow();
        } catch (IOException e) {
            errorLabel.setText("Login error: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/examen/fapte_bune/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            RegisterController controller = loader.getController();
            controller.setService(service);
            controller.setLoginController(this);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error opening register window: " + e.getMessage());
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/examen/fapte_bune/main.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            MainController controller = loader.getController();
            controller.setService(service);
            stage.show();

            // Don't close login window
            // ((Stage) usernameListView.getScene().getWindow()).close();

            // Clear selection after successful login
            usernameListView.getSelectionModel().clearSelection();
            errorLabel.setText("");
        } catch (IOException e) {
            errorLabel.setText("Error opening main window: " + e.getMessage());
        }
    }

    public void refresh() {
        loadUsernames();
    }
}