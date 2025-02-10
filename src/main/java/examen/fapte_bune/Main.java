package examen.fapte_bune;

import examen.fapte_bune.gui.LoginController;
import examen.fapte_bune.repository.NevoiRepo;
import examen.fapte_bune.repository.PersoaneRepo;
import examen.fapte_bune.service.Service;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class Main extends Application {
    private Service service;

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeService();
            FXMLLoader loader = loadFXML("/examen/fapte_bune/login.fxml");
            initializeScene(primaryStage, loader);
        } catch (Exception e) {
            showErrorAndExit("Application Error", e.getMessage());
        }
    }

    private void initializeService() throws SQLException {
        PersoaneRepo persoaneRepo = new PersoaneRepo();
        NevoiRepo nevoiRepo = new NevoiRepo();
        service = new Service(persoaneRepo, nevoiRepo);
    }

    private FXMLLoader loadFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.load();
        LoginController controller = loader.getController();
        controller.setService(service);
        return loader;
    }

    private void initializeScene(Stage primaryStage, FXMLLoader loader) {
        Parent root = loader.getRoot();
        Scene scene = new Scene(root);

        primaryStage.setTitle("Fapte Bune - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void showErrorAndExit(String title, String message) {
        System.err.println(title + ": " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
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