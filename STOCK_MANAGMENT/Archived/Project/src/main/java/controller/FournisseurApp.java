package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Main application class to launch the JavaFX fournisseur management application.
 */
public class FournisseurApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML from resources
            URL fxmlLocation = getClass().getResource("/com/example/project/Fournisseur.fxml");
            if (fxmlLocation == null) {
                throw new IOException("Cannot find Fournisseur.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(loader.load(), 1200, 1000);

            // Apply CSS stylesheets
            URL dashboardCss = getClass().getResource("/styles/dashboard.css");
            if (dashboardCss != null) {
                scene.getStylesheets().add(dashboardCss.toExternalForm());
            } else {
                System.err.println("Warning: Cannot find dashboard.css");
            }

            URL loginCss = getClass().getResource("/styles/login.css");
            if (loginCss != null) {
                scene.getStylesheets().add(loginCss.toExternalForm());
            } else {
                System.err.println("Warning: Cannot find login.css");
            }

            URL fournisseurCss = getClass().getResource("/styles/Fournisseur.css");
            if (fournisseurCss != null) {
                scene.getStylesheets().add(fournisseurCss.toExternalForm());
            } else {
                System.err.println("Warning: Cannot find Fournisseur.css");
            }

            primaryStage.setTitle("ISIMM - Gestion de Stock - Fournisseurs");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true); // Open in fullscreen
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Application Failed to Start");
            alert.setContentText("Unable to load Fournisseur.fxml: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        // Note: Ensure VM option --enable-native-access=javafx.graphics if required
        launch(args);
    }
}