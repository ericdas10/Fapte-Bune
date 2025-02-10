package examen.fapte_bune.gui;

import examen.fapte_bune.domain.Oras;
import examen.fapte_bune.service.Service;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField numeField;
    @FXML
    private TextField prenumeField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField parolaField;
    @FXML
    private PasswordField confirmParolaField;
    @FXML
    private ComboBox<Oras> orasComboBox;
    @FXML
    private TextField stradaField;
    @FXML
    private TextField numarStradaField;
    @FXML
    private TextField telefonField;
    @FXML
    private Label errorLabel;

    private Service service;
    private LoginController loginController;

    @FXML
    public void initialize() {
        orasComboBox.setItems(FXCollections.observableArrayList(Oras.values()));
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    @FXML
    private void handleRegister() {
        if (!validateInputs()) return;

        try {
            service.register(
                    numeField.getText(),
                    prenumeField.getText(),
                    usernameField.getText(),
                    parolaField.getText(),
                    orasComboBox.getValue(),
                    stradaField.getText(),
                    numarStradaField.getText(),
                    telefonField.getText()
            );
            closeWindow();
        } catch (IOException e) {
            errorLabel.setText("Registration error: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (!parolaField.getText().equals(confirmParolaField.getText())) {
            errorLabel.setText("Passwords don't match");
            return false;
        }
        // Add more validations as needed
        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) numeField.getScene().getWindow()).close();
    }
}
