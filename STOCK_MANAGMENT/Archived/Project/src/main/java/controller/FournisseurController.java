package controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.dao.FournisseurDAO;
import model.dao.FournisseurDAOImpl;
import model.entities.Fournisseur;
import utils.DatabaseConnection;
import utils.UserSession;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the fournisseur management interface.
 * Handles dynamic filters, table interactions, form submissions, notifications, and navigation.
 */
public class FournisseurController implements Initializable {

    private final FournisseurDAO fournisseurDAO = new FournisseurDAOImpl();
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Filter section
    @FXML private TextField filterId;
    @FXML private TextField filterNom;
    @FXML private TextField filterAdresse;
    @FXML private TextField filterTelephone;
    @FXML private TextField filterEmail;
    @FXML private TextField filterSiteWeb;
    @FXML private TextField filterPersonneContact;
    @FXML private TextField filterNotes;
    @FXML private Button btnReset;

    // Table section
    @FXML private TableView<Fournisseur> tableFournisseurs;
    @FXML private TableColumn<Fournisseur, Long> colId;
    @FXML private TableColumn<Fournisseur, String> colNom;
    @FXML private TableColumn<Fournisseur, String> colAdresse;
    @FXML private TableColumn<Fournisseur, String> colTelephone;
    @FXML private TableColumn<Fournisseur, String> colEmail;
    @FXML private TableColumn<Fournisseur, String> colSiteWeb;
    @FXML private TableColumn<Fournisseur, String> colPersonneContact;
    @FXML private TableColumn<Fournisseur, String> colNotes;
    @FXML private TableColumn<Fournisseur, Void> colActions;

    // Form section
    @FXML private TextField formNom;
    @FXML private TextField formAdresse;
    @FXML private TextField formTelephone;
    @FXML private TextField formEmail;
    @FXML private TextField formSiteWeb;
    @FXML private TextField formPersonneContact;
    @FXML private TextField formNotes;
    @FXML private Button btnAjouter;
    @FXML private Button btnResetForm;

    // Navigation
    @FXML private VBox menuContainer;
    @FXML private Button logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    private ObservableList<Fournisseur> fournisseurList = FXCollections.observableArrayList();
    private ObservableList<String> expiringArticles = FXCollections.observableArrayList();
    private boolean isEditing = false;
    private Fournisseur currentFournisseur;

    private static final String[] MENU_ITEMS = {"Accueil", "Produits", "Locaux", "Fournisseurs", "Consommateurs", "Commandes internes", "Commandes externes", "Inventaire"};
    private static final String[] MENU_ICONS = {"🏠", "📦", "🏢", "🚚", "👥", "📥", "📤", "📊"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set the magasinier's name
        String magasinierName = getLoggedInMagasinierName();
        usernameLabel.setText(magasinierName);

        // Initialize notifications
        refreshNotifications();
        notificationBadge.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(expiringArticles.size()), expiringArticles));

        setupSidebar();
        setupFilters();
        setupTable();
        setupForm();
        loadFournisseurs();
    }

    /**
     * Retrieves the logged-in magasinier's full name from UserSession.
     */
    private String getLoggedInMagasinierName() {
        UserSession session = UserSession.getInstance();
        String fullName = session.getFullName();
        return (fullName != null && !fullName.isEmpty()) ? fullName : "Utilisateur Inconnu";
    }

    /**
     * Handles notification icon click, showing articles expiring within a week.
     */
    @FXML
    private void handleNotificationClick() {
        refreshNotifications();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications - Articles en Péremption");
        alert.setHeaderText(null);
        if (expiringArticles.isEmpty()) {
            alert.setContentText("Aucun article ne périme dans les 7 prochains jours.");
        } else {
            StringBuilder message = new StringBuilder("Articles périmant dans les 7 prochains jours:\n");
            for (String article : expiringArticles) {
                message.append("- ").append(article).append("\n");
            }
            alert.setContentText(message.toString());
        }
        alert.showAndWait();
    }

    /**
     * Refreshes the list of expiring articles.
     */
    private void refreshNotifications() {
        List<String> updatedArticles = getExpiringArticles();
        expiringArticles.setAll(updatedArticles);
    }

    /**
     * Queries the article table for products with date_peremption within 7 days.
     */
    private List<String> getExpiringArticles() {
        List<String> expiring = new ArrayList<>();
        String sql = "SELECT nom, date_peremption FROM article WHERE date_peremption IS NOT NULL AND date_peremption <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalDateTime oneWeekFromNow = LocalDateTime.now().plusDays(7);
            pstmt.setString(1, oneWeekFromNow.format(SQLITE_DATETIME_FORMATTER));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("nom");
                    String datePeremptionStr = rs.getString("date_peremption");
                    LocalDateTime datePeremption = LocalDateTime.parse(datePeremptionStr, SQLITE_DATETIME_FORMATTER);
                    String formattedDate = datePeremption.format(DISPLAY_DATE_FORMATTER);
                    expiring.add(nom + " (Périme le: " + formattedDate + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des articles en péremption: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la récupération des articles en péremption: " + e.getMessage());
        }
        return expiring;
    }

    /**
     * Initializes the sidebar menu.
     */
    private void setupSidebar() {
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            VBox menuItem = createMenuItem(MENU_ITEMS[i], MENU_ICONS[i], i == 3); // Fournisseurs is active
            menuContainer.getChildren().add(menuItem);
        }
    }

    /**
     * Creates a vertical menu item with icon above label.
     */
    private VBox createMenuItem(String text, String icon, boolean isActive) {
        VBox item = new VBox(5);
        item.getStyleClass().add("menu-item");
        if (isActive) {
            item.getStyleClass().add("menu-item-active");
        }

        item.setOnMouseEntered(e -> {
            if (!isActive) {
                item.getStyleClass().add("menu-item-hover");
            }
        });

        item.setOnMouseExited(e -> {
            if (!isActive) {
                item.getStyleClass().remove("menu-item-hover");
            }
        });

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("menu-item-icon");

        Label label = new Label(text);
        label.getStyleClass().add("menu-item-label");
        label.setWrapText(true);

        item.getChildren().addAll(iconLabel, label);

        item.setOnMouseClicked(e -> {
            try {
                Stage newStage = new Stage();
                switch (text) {
                    case "Accueil":
                        new StockDashboard().start(newStage);
                        break;
                    case "Produits":
                        new ArticleApp().start(newStage);
                        break;
                    case "Consommateurs":
                        new ConsommateurApp().start(newStage);
                        break;
                    case "Locaux":
                        new LocalApp().start(newStage);
                        break;
                    case "Fournisseurs":
                        new FournisseurApp().start(newStage);
                        break;
                    case "Commandes internes":
                        new CommandeInterneApp().start(newStage);
                        break;
                    case "Commandes externes":
                        new CommandeExterneApp().start(newStage);
                        break;
                    case "Inventaire":
                        new InventaireApp().start(newStage);
                        break;
                }
                ((Stage) menuContainer.getScene().getWindow()).close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return item;
    }

    /**
     * Configures filter section components.
     */
    private void setupFilters() {
        // Add dynamic filter listeners
        filterId.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterNom.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterAdresse.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterTelephone.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterEmail.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterSiteWeb.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterPersonneContact.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterNotes.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Configures the table columns and action buttons.
     */
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colSiteWeb.setCellValueFactory(new PropertyValueFactory<>("siteWeb"));
        colPersonneContact.setCellValueFactory(new PropertyValueFactory<>("personneContact"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Action column with Modify and Delete buttons
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button modifyButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final VBox buttons = new VBox(5, modifyButton, deleteButton);

            {
                modifyButton.getStyleClass().addAll("btn", "btn-modify");
                deleteButton.getStyleClass().addAll("btn", "btn-delete");
                buttons.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Fournisseur fournisseur = getTableRow().getItem();
                    modifyButton.setOnAction(e -> handleModify(fournisseur));
                    deleteButton.setOnAction(e -> handleDelete(fournisseur));
                    setGraphic(buttons);
                }
            }
        });

        tableFournisseurs.setItems(fournisseurList);
    }

    /**
     * Configures the form section for adding/editing fournisseurs.
     */
    private void setupForm() {
        // No specific setup needed for text fields
    }

    /**
     * Loads fournisseurs from the database.
     */
    private void loadFournisseurs() {
        try {
            List<Fournisseur> fournisseurs = fournisseurDAO.findAll();
            fournisseurList.setAll(fournisseurs);
            System.out.println("Nombre de fournisseurs chargés: " + fournisseurs.size());
            applyFilters();
            refreshNotifications();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des fournisseurs: " + e.getMessage());
            e.printStackTrace();
            String errorMessage = "Erreur lors du chargement des fournisseurs: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "\nCause: " + e.getCause().getMessage();
            }
            showAlert(Alert.AlertType.ERROR, "Erreur", errorMessage);
            applyFilters();
        }
    }

    /**
     * Applies filters to the table based on all filter inputs.
     */
    private void applyFilters() {
        String idFilter = filterId.getText() != null ? filterId.getText().trim() : "";
        String nomFilter = filterNom.getText() != null ? filterNom.getText().trim().toLowerCase() : "";
        String adresseFilter = filterAdresse.getText() != null ? filterAdresse.getText().trim().toLowerCase() : "";
        String telephoneFilter = filterTelephone.getText() != null ? filterTelephone.getText().trim().toLowerCase() : "";
        String emailFilter = filterEmail.getText() != null ? filterEmail.getText().trim().toLowerCase() : "";
        String siteWebFilter = filterSiteWeb.getText() != null ? filterSiteWeb.getText().trim().toLowerCase() : "";
        String personneContactFilter = filterPersonneContact.getText() != null ? filterPersonneContact.getText().trim().toLowerCase() : "";
        String notesFilter = filterNotes.getText() != null ? filterNotes.getText().trim().toLowerCase() : "";

        ObservableList<Fournisseur> filteredList = FXCollections.observableArrayList();
        for (Fournisseur fournisseur : fournisseurList) {
            if (fournisseur == null) continue;
            boolean matches = true;

            if (!idFilter.isEmpty()) {
                try {
                    String idString = String.valueOf(fournisseur.getId());
                    if (!idString.startsWith(idFilter)) {
                        matches = false;
                    }
                } catch (NumberFormatException e) {
                    matches = false;
                }
            }
            if (!nomFilter.isEmpty() && (fournisseur.getNom() == null || !fournisseur.getNom().toLowerCase().startsWith(nomFilter))) {
                matches = false;
            }
            if (!adresseFilter.isEmpty() && (fournisseur.getAdresse() == null || !fournisseur.getAdresse().toLowerCase().startsWith(adresseFilter))) {
                matches = false;
            }
            if (!telephoneFilter.isEmpty() && (fournisseur.getTelephone() == null || !fournisseur.getTelephone().toLowerCase().startsWith(telephoneFilter))) {
                matches = false;
            }
            if (!emailFilter.isEmpty() && (fournisseur.getEmail() == null || !fournisseur.getEmail().toLowerCase().startsWith(emailFilter))) {
                matches = false;
            }
            if (!siteWebFilter.isEmpty() && (fournisseur.getSiteWeb() == null || !fournisseur.getSiteWeb().toLowerCase().startsWith(siteWebFilter))) {
                matches = false;
            }
            if (!personneContactFilter.isEmpty() && (fournisseur.getPersonneContact() == null || !fournisseur.getPersonneContact().toLowerCase().startsWith(personneContactFilter))) {
                matches = false;
            }
            if (!notesFilter.isEmpty() && (fournisseur.getNotes() == null || !fournisseur.getNotes().toLowerCase().startsWith(notesFilter))) {
                matches = false;
            }

            if (matches) {
                filteredList.add(fournisseur);
            }
        }
        tableFournisseurs.setItems(filteredList);
        tableFournisseurs.refresh();
    }

    /**
     * Handles form submission to add or update a fournisseur.
     */
    @FXML
    private void handleAjouter() {
        if (!validateForm()) return;

        if (!isEditing) {
            // Add new fournisseur
            Fournisseur newFournisseur = createFournisseurFromForm();
            try {
                fournisseurDAO.create(newFournisseur);
                loadFournisseurs();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur ajouté avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du fournisseur: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Update existing fournisseur
            currentFournisseur.setNom(formNom.getText().trim());
            currentFournisseur.setAdresse(formAdresse.getText().trim().isEmpty() ? null : formAdresse.getText().trim());
            currentFournisseur.setTelephone(formTelephone.getText().trim().isEmpty() ? null : formTelephone.getText().trim());
            currentFournisseur.setEmail(formEmail.getText().trim().isEmpty() ? null : formEmail.getText().trim());
            currentFournisseur.setSiteWeb(formSiteWeb.getText().trim().isEmpty() ? null : formSiteWeb.getText().trim());
            currentFournisseur.setPersonneContact(formPersonneContact.getText().trim().isEmpty() ? null : formPersonneContact.getText().trim());
            currentFournisseur.setNotes(formNotes.getText().trim().isEmpty() ? null : formNotes.getText().trim());
            try {
                fournisseurDAO.update(currentFournisseur);
                loadFournisseurs();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur mis à jour avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du fournisseur: " + e.getMessage());
                e.printStackTrace();
            }
            isEditing = false;
            btnAjouter.setText("Ajouter");
        }
        clearForm();
    }

    /**
     * Populates form with fournisseur data for editing.
     */
    private void handleModify(Fournisseur fournisseur) {
        currentFournisseur = fournisseur;
        isEditing = true;
        formNom.setText(fournisseur.getNom() != null ? fournisseur.getNom() : "");
        formAdresse.setText(fournisseur.getAdresse() != null ? fournisseur.getAdresse() : "");
        formTelephone.setText(fournisseur.getTelephone() != null ? fournisseur.getTelephone() : "");
        formEmail.setText(fournisseur.getEmail() != null ? fournisseur.getEmail() : "");
        formSiteWeb.setText(fournisseur.getSiteWeb() != null ? fournisseur.getSiteWeb() : "");
        formPersonneContact.setText(fournisseur.getPersonneContact() != null ? fournisseur.getPersonneContact() : "");
        formNotes.setText(fournisseur.getNotes() != null ? fournisseur.getNotes() : "");
        btnAjouter.setText("Sauvegarder");
    }

    /**
     * Deletes a fournisseur after confirmation.
     */
    private void handleDelete(Fournisseur fournisseur) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le fournisseur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce fournisseur ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                fournisseurDAO.delete(fournisseur.getId());
                loadFournisseurs();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du fournisseur: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Resets all filter inputs to default values.
     */
    @FXML
    private void handleReset() {
        filterId.clear();
        filterNom.clear();
        filterAdresse.clear();
        filterTelephone.clear();
        filterEmail.clear();
        filterSiteWeb.clear();
        filterPersonneContact.clear();
        filterNotes.clear();
        applyFilters();
        tableFournisseurs.refresh();
    }

    /**
     * Resets the form inputs to default values.
     */
    @FXML
    private void handleResetForm() {
        clearForm();
    }

    /**
     * Handles logout action.
     */
    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().clear();
            new LoginApp().start(new Stage());
            ((Stage) logoutBtn.getScene().getWindow()).close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Validates form inputs before submission.
     */
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();

        if (formNom.getText().trim().isEmpty()) {
            errorMessage.append("- Le nom est requis\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return false;
        }
        return true;
    }

    /**
     * Creates a Fournisseur object from form inputs.
     */
    private Fournisseur createFournisseurFromForm() {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setNom(formNom.getText().trim());
        String adresse = formAdresse.getText().trim();
        fournisseur.setAdresse(adresse.isEmpty() ? null : adresse);
        String telephone = formTelephone.getText().trim();
        fournisseur.setTelephone(telephone.isEmpty() ? null : telephone);
        String email = formEmail.getText().trim();
        fournisseur.setEmail(email.isEmpty() ? null : email);
        String siteWeb = formSiteWeb.getText().trim();
        fournisseur.setSiteWeb(siteWeb.isEmpty() ? null : siteWeb);
        String personneContact = formPersonneContact.getText().trim();
        fournisseur.setPersonneContact(personneContact.isEmpty() ? null : personneContact);
        String notes = formNotes.getText().trim();
        fournisseur.setNotes(notes.isEmpty() ? null : notes);
        fournisseur.setCreatedAt(LocalDateTime.now());
        fournisseur.setUpdatedAt(LocalDateTime.now());
        return fournisseur;
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        formNom.clear();
        formAdresse.clear();
        formTelephone.clear();
        formEmail.clear();
        formSiteWeb.clear();
        formPersonneContact.clear();
        formNotes.clear();
        isEditing = false;
        btnAjouter.setText("Ajouter");
    }

    /**
     * Shows an alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}