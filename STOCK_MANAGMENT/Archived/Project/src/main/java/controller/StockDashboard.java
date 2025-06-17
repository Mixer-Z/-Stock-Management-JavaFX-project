package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StockDashboard extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project/dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("dashboard.fxml not found at /com/example/project/dashboard.fxml");
            }
            Scene scene = new Scene(loader.load());
            try {
                scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Failed to load dashboard.css: " + e.getMessage());
            }
            try {
                scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Failed to load login.css: " + e.getMessage());
            }
            primaryStage.setTitle("ISIMM - Gestion de Stock");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true); // Maximize the window to fill the screen
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start StockDashboard: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}