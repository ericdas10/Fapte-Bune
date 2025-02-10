package examen.fapte_bune;

import examen.fapte_bune.gui.LoginController;
import examen.fapte_bune.repository.NevoiRepo;
import examen.fapte_bune.repository.PersoaneRepo;
import examen.fapte_bune.service.Service;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize repositories
            PersoaneRepo persoaneRepo = new PersoaneRepo();
            NevoiRepo nevoiRepo = new NevoiRepo();

            // Initialize service
            Service service = new Service(persoaneRepo, nevoiRepo);

            // Load login view using absolute path
            String currentPath = new File(".").getCanonicalPath();
            String fxmlPath = currentPath + "/src/main/resources/examen/fapte_bune/login.fxml";
            System.out.println("Loading FXML from: " + fxmlPath);

            FXMLLoader loginLoader = new FXMLLoader(new File(fxmlPath).toURI().toURL());
            Scene scene = new Scene(loginLoader.load(), 400, 600);

            // Initialize controller
            LoginController loginController = loginLoader.getController();
            loginController.setService(service);

            // Configure and show window
            primaryStage.setTitle("Fapte Bune - Login");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (SQLException e) {
            showErrorAndExit("Database Error", "Could not connect to database: " + e.getMessage());
        } catch (IOException e) {
            showErrorAndExit("Loading Error", "Could not load application: " + e.getMessage());
        }
    }

    private void showErrorAndExit(String title, String message) {
        System.err.println(title + ": " + message);
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}