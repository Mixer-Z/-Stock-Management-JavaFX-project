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
import utils.UserSession;

import java.time.LocalDateTime;

public class LoginController {

    @FXML
    private ImageView profileImage;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label signupLabel;

    private MagasinierDAOImpl magasinierDAO;

    @FXML
    private void initialize() {
        magasinierDAO = new MagasinierDAOImpl();
        profileImage.setFitHeight(100);
        profileImage.setFitWidth(100);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Magasinier magasinier = magasinierDAO.getByNomUtilisateur(username);
        if (magasinier != null && magasinier.getHashedPassword().equals(password) && magasinier.isActif()) {
            try {
                // Store user details in UserSession
                UserSession session = UserSession.getInstance();
                session.setUser(username, magasinier.getNom()); // Adjust if field name differs

                // Update last login time
                magasinier.setDernierConnexion(LocalDateTime.now());
                magasinierDAO.update(magasinier);

                // Start dashboard and close login window
                new StockDashboard().start(new Stage());
                ((Stage) loginButton.getScene().getWindow()).close();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Erreur lors du chargement du tableau de bord: " + ex.getMessage());
            }
        } else {
            errorLabel.setText("Identifiants incorrects ou compte inactif !");
        }
    }

    @FXML
    private void handleSignup() {
        try {
            new SignupApp().start((Stage) loginButton.getScene().getWindow());
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Erreur lors du chargement de l'inscription: " + ex.getMessage());
        }
    }
}