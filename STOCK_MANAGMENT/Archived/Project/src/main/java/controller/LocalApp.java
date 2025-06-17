package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Main application class to launch the JavaFX local management application.
 */
public class LocalApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML from resources
            URL fxmlLocation = getClass().getResource("/com/example/project/Local.fxml");
            if (fxmlLocation == null) {
                throw new IOException("Cannot find Local.fxml");
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

            URL localCss = getClass().getResource("/styles/local.css");
            if (localCss != null) {
                scene.getStylesheets().add(localCss.toExternalForm());
            } else {
                System.err.println("Warning: Cannot find local.css");
            }

            primaryStage.setTitle("ISIMM - Gestion de Stock - Locaux");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Application Failed to Start");
            alert.setContentText("Unable to load Local.fxml: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}