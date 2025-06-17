package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.dao.MagasinierDAOImpl;
import model.entities.Magasinier;

public class SignupController {

    @FXML
    private ImageView profileImage;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signupButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label loginLabel;

    private MagasinierDAOImpl magasinierDAO;

    @FXML
    private void initialize() {
        magasinierDAO = new MagasinierDAOImpl();
        profileImage.setFitHeight(100);
        profileImage.setFitWidth(100);
    }

    @FXML
    private void handleSignup() {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
        } else if (!password.equals(confirmPassword)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
        } else if (magasinierDAO.getByNomUtilisateur(username) != null) {
            errorLabel.setText("Ce nom d'utilisateur existe déjà.");
        } else {
            Magasinier magasinier = new Magasinier(fullName, username, password); // Plain text password for simplicity
            try {
                magasinierDAO.insert(magasinier);
                errorLabel.setText("Inscription réussie !");
                new LoginApp().start((Stage) signupButton.getScene().getWindow());
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Erreur lors de l'inscription.");
            }
        }
    }

    @FXML
    private void handleLogin() {
        try {
            new LoginApp().start((Stage) signupButton.getScene().getWindow());
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Erreur lors du retour à la connexion.");
        }
    }
}