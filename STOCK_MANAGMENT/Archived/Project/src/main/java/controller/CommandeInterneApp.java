package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Main application class to launch the JavaFX internal order management application.
 */
public class CommandeInterneApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML from resources
            URL fxmlLocation = getClass().getResource("/com/example/project/CommandeInterne.fxml");
            if (fxmlLocation == null) {
                throw new IOException("Cannot find CommandeInterne.fxml");
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

            URL commandeInterneCss = getClass().getResource("/styles/commandeInterne.css");
            if (commandeInterneCss != null) {
                scene.getStylesheets().add(commandeInterneCss.toExternalForm());
            } else {
                System.err.println("Warning: Cannot find commandeInterne.css");
            }

            primaryStage.setTitle("ISIMM - Gestion de Stock - Commandes Internes");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Application Failed to Start");
            alert.setContentText("Unable to load CommandeInterne.fxml: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}