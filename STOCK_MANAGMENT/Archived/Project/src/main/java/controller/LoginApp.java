package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project/login.fxml"));
        StackPane root = loader.load(); // Explicitly cast to StackPane
        Scene scene = new Scene(root, 400, 600);
        // Apply CSS
        scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
        // Configure stage
        primaryStage.setTitle("ISIMM - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.centerOnScreen();
        // Prevent initial focus on input fields
        root.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}