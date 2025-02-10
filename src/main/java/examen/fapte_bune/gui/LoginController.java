package examen.fapte_bune.gui;

import examen.fapte_bune.service.Service;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class LoginController {
    @FXML
    private ListView<String> usernameListView;
    @FXML
    private Label errorLabel;

    private Service service;
    private String resourcePath;

    public void setService(Service service) {
        this.service = service;
        try {
            this.resourcePath = new File(".").getCanonicalPath() + "/src/main/resources/examen/fapte_bune/";
            loadUsernames();
        } catch (IOException e) {
            errorLabel.setText("Error initializing: " + e.getMessage());
        }
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
            String registerPath = resourcePath + "register.fxml";
            FXMLLoader loader = new FXMLLoader(new File(registerPath).toURI().toURL());
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
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
            String mainPath = resourcePath + "main.fxml";
            FXMLLoader loader = new FXMLLoader(new File(mainPath).toURI().toURL());
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            MainController controller = loader.getController();
            controller.setService(service);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error opening main window: " + e.getMessage());
        }
    }
}