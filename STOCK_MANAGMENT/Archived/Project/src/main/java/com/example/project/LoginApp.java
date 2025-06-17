package com.example.project;


import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Profile Image Icon (you can use your own icon URL or resource)
        ImageView profileImage = new ImageView(new Image("https://img.icons8.com/?size=100&id=83190&format=png&color=1e90ff"));
        profileImage.setFitHeight(100);
        profileImage.setFitWidth(100);

        // Username Field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");
        usernameField.setPromptText("Nom d'utilisateur");
        usernameField.getStyleClass().add("text-field");

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setPromptText("Mot de passe");
        passwordField.getStyleClass().add("password-field");

        // Login Button
        Button loginButton = new Button("Se connecter");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.getStyleClass().add("button");

        // Signup Link
        Label signupLabel = new Label("Vous êtes nouveau ? Inscription");
        signupLabel.setStyle("-fx-text-fill: #1e90ff; -fx-underline: true;");
        signupLabel.setOnMouseClicked(e -> {
            try {
                new SignupApp().start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        signupLabel.getStyleClass().addAll("label", "signup");

        // Error Label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.getStyleClass().addAll("label", "error");

        // Login action
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.equals("admin") && password.equals("admin123")) {
                try {
                    new StockDashboard().start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                errorLabel.setText("Identifiants incorrects !");
            }
        });



        // Layout
        VBox loginLayout = new VBox(15,
                profileImage,
                usernameField,
                passwordField,
                loginButton,
                errorLabel,
                signupLabel
        );
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(30));
        loginLayout.setMaxWidth(300);
        loginLayout.setStyle("-fx-background-color: #f9f9f9; -fx-border-radius: 10; -fx-background-radius: 10;");

        StackPane root = new StackPane(loginLayout);
        root.setPadding(new Insets(40));
        Scene scene = new Scene(root, 400, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
        root.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
