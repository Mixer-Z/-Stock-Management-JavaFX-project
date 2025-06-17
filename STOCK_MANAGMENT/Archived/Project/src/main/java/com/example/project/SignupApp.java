package com.example.project;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SignupApp extends Application {

    @Override
    public void start(Stage stage) {
        // Profile Image
        ImageView profileImage = new ImageView(new Image("https://img.icons8.com/?size=100&id=83190&format=png&color=1e90ff"));
        profileImage.setFitHeight(100);
        profileImage.setFitWidth(100);

        // Full Name Field
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Nom complet");
        fullNameField.getStyleClass().add("text-field");

        // Username Field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");
        usernameField.getStyleClass().add("text-field");

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.getStyleClass().add("password-field");

        // Confirm Password Field
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le mot de passe");
        confirmPasswordField.getStyleClass().add("password-field");

        // Signup Button
        Button signupButton = new Button("S'inscrire");
        signupButton.setMaxWidth(Double.MAX_VALUE);
        signupButton.getStyleClass().add("button");

        // Error Label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.getStyleClass().addAll("label", "error");

        // Back to Login Link
        Label loginLabel = new Label("Déjà inscrit ? Se connecter");
        loginLabel.setStyle("-fx-text-fill: #1e90ff; -fx-underline: true;");
        loginLabel.setOnMouseClicked(e -> {
            try {
                new LoginApp().start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        loginLabel.getStyleClass().addAll("label", "signup");

        // Signup action
        signupButton.setOnAction(e -> {
            String fullName = fullNameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Veuillez remplir tous les champs.");
            } else if (!password.equals(confirmPassword)) {
                errorLabel.setText("Les mots de passe ne correspondent pas.");
            } else {
                errorLabel.setText("");
                System.out.println("Inscription réussie pour: " + username);
                try {
                    new LoginApp().start(stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Layout
        VBox signupLayout = new VBox(15,
                profileImage,
                fullNameField,
                usernameField,
                passwordField,
                confirmPasswordField,
                signupButton,
                errorLabel,
                loginLabel
        );
        signupLayout.setAlignment(Pos.CENTER);
        signupLayout.setPadding(new Insets(30));
        signupLayout.setMaxWidth(300);
        signupLayout.setStyle("-fx-background-color: #f9f9f9; -fx-border-radius: 10; -fx-background-radius: 10;");

        StackPane root = new StackPane(signupLayout);
        root.setPadding(new Insets(40));
        Scene scene = new Scene(root, 400, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Créer un compte");
        stage.show();
        root.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
