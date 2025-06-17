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
import model.dao.ConsommateurDAO;
import model.dao.ConsommateurDAOImpl;
import model.entities.Consommateur;
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
 * Controller for the consommateur management interface.
 * Handles dynamic filters, table interactions, form submissions, notifications, and navigation.
 */
public class ConsommateurController implements Initializable {

    private final ConsommateurDAO consommateurDAO = new ConsommateurDAOImpl();
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Filter section
    @FXML private TextField filterId;
    @FXML private TextField filterNom;
    @FXML private TextField filterEmail;
    @FXML private TextField filterTelephone;
    @FXML private ComboBox<String> filterType;
    @FXML private TextField filterDescription;
    @FXML private Button btnReset;

    // Table section
    @FXML private TableView<Consommateur> tableConsommateurs;
    @FXML private TableColumn<Consommateur, Long> colId;
    @FXML private TableColumn<Consommateur, String> colNom;
    @FXML private TableColumn<Consommateur, String> colEmail;
    @FXML private TableColumn<Consommateur, String> colTelephone;
    @FXML private TableColumn<Consommateur, String> colType;
    @FXML private TableColumn<Consommateur, String> colDescription;
    @FXML private TableColumn<Consommateur, Void> colActions;

    // Form section
    @FXML private TextField formNom;
    @FXML private TextField formEmail;
    @FXML private TextField formTelephone;
    @FXML private ComboBox<String> formType;
    @FXML private TextField formDescription;
    @FXML private Button btnAjouter;
    @FXML private Button btnResetForm;

    // Navigation
    @FXML private VBox menuContainer;
    @FXML private Button logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    private ObservableList<Consommateur> consommateurList = FXCollections.observableArrayList();
    private ObservableList<String> expiringArticles = FXCollections.observableArrayList();
    private boolean isEditing = false;
    private Consommateur currentConsommateur;

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
        loadConsommateurs();
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
            VBox menuItem = createMenuItem(MENU_ITEMS[i], MENU_ICONS[i], i == 4); // Consommateurs is active
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
        // Types for filter
        ObservableList<String> types = FXCollections.observableArrayList("Interne", "Externe", "Autre");
        filterType.setItems(types);

        // Add dynamic filter listeners
        filterId.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterNom.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterEmail.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterTelephone.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterType.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterDescription.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Configures the table columns and action buttons.
     */
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));


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
                    Consommateur consommateur = getTableRow().getItem();
                    modifyButton.setOnAction(e -> handleModify(consommateur));
                    deleteButton.setOnAction(e -> handleDelete(consommateur));
                    setGraphic(buttons);
                }
            }
        });

        tableConsommateurs.setItems(consommateurList);
    }

    /**
     * Configures the form section for adding/editing consommateurs.
     */
    private void setupForm() {
        // Types for form
        ObservableList<String> types = FXCollections.observableArrayList("Interne", "Externe", "Autre");
        formType.setItems(types);
    }

    /**
     * Loads consommateurs from the database.
     */
    private void loadConsommateurs() {
        try {
            if (consommateurDAO == null) {
                throw new IllegalStateException("ConsommateurDAO is not initialized");
            }
            List<Consommateur> consommateurs = consommateurDAO.getAll();
            if (consommateurs == null) {
                System.err.println("ConsommateurDAO returned null list");
                consommateurs = new ArrayList<>();
            }
            consommateurList.setAll(consommateurs);
            System.out.println("Nombre de consommateurs chargés: " + consommateurs.size());
            applyFilters();
            refreshNotifications();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des consommateurs: " + e.getMessage());
            e.printStackTrace();
            String errorMessage = "Erreur lors du chargement des consommateurs: " + e.getMessage();
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
        String emailFilter = filterEmail.getText() != null ? filterEmail.getText().trim().toLowerCase() : "";
        String telephoneFilter = filterTelephone.getText() != null ? filterTelephone.getText().trim().toLowerCase() : "";
        String typeFilter = filterType.getValue();
        String descriptionFilter = filterDescription.getText() != null ? filterDescription.getText().trim().toLowerCase() : "";

        ObservableList<Consommateur> filteredList = FXCollections.observableArrayList();
        for (Consommateur consommateur : consommateurList) {
            if (consommateur == null) continue;
            boolean matches = true;

            if (!idFilter.isEmpty()) {
                try {
                    String idString = String.valueOf(consommateur.getId());
                    if (!idString.startsWith(idFilter)) {
                        matches = false;
                    }
                } catch (NumberFormatException e) {
                    matches = false;
                }
            }
            if (!nomFilter.isEmpty() && (consommateur.getNom() == null || !consommateur.getNom().toLowerCase().startsWith(nomFilter))) {
                matches = false;
            }
            if (!emailFilter.isEmpty() && (consommateur.getEmail() == null || !consommateur.getEmail().toLowerCase().startsWith(emailFilter))) {
                matches = false;
            }
            if (!telephoneFilter.isEmpty() && (consommateur.getTelephone() == null || !consommateur.getTelephone().toLowerCase().startsWith(telephoneFilter))) {
                matches = false;
            }
            if (typeFilter != null && (consommateur.getType() == null || !consommateur.getType().equals(typeFilter))) {
                matches = false;
            }
            if (!descriptionFilter.isEmpty() && (consommateur.getDescription() == null || !consommateur.getDescription().toLowerCase().startsWith(descriptionFilter))) {
                matches = false;
            }

            if (matches) {
                filteredList.add(consommateur);
            }
        }
        tableConsommateurs.setItems(filteredList);
        tableConsommateurs.refresh();
    }

    /**
     * Handles form submission to add or update a consommateur.
     */
    @FXML
    private void handleAjouter() {
        if (!validateForm()) return;

        if (!isEditing) {
            // Add new consommateur
            Consommateur newConsommateur = createConsommateurFromForm();
            try {
                consommateurDAO.insert(newConsommateur);
                loadConsommateurs();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Consommateur ajouté avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du consommateur: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Update existing consommateur
            currentConsommateur.setNom(formNom.getText().trim());
            currentConsommateur.setEmail(formEmail.getText().trim().isEmpty() ? null : formEmail.getText().trim());
            currentConsommateur.setTelephone(formTelephone.getText().trim().isEmpty() ? null : formTelephone.getText().trim());
            currentConsommateur.setType(formType.getValue());
            currentConsommateur.setDescription(formDescription.getText().trim().isEmpty() ? null : formDescription.getText().trim());
            try {
                consommateurDAO.update(currentConsommateur);
                loadConsommateurs();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Consommateur mis à jour avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du consommateur: " + e.getMessage());
                e.printStackTrace();
            }
            isEditing = false;
            btnAjouter.setText("Ajouter");
        }
        clearForm();
    }

    /**
     * Populates form with consommateur data for editing.
     */
    private void handleModify(Consommateur consommateur) {
        currentConsommateur = consommateur;
        isEditing = true;
        formNom.setText(consommateur.getNom() != null ? consommateur.getNom() : "");
        formEmail.setText(consommateur.getEmail() != null ? consommateur.getEmail() : "");
        formTelephone.setText(consommateur.getTelephone() != null ? consommateur.getTelephone() : "");
        formType.setValue(consommateur.getType());
        formDescription.setText(consommateur.getDescription() != null ? consommateur.getDescription() : "");
        btnAjouter.setText("Sauvegarder");
    }

    /**
     * Deletes a consommateur after confirmation.
     */
    private void handleDelete(Consommateur consommateur) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le consommateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce consommateur ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                consommateurDAO.delete(consommateur.getId());
                loadConsommateurs();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Consommateur supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du consommateur: " + e.getMessage());
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
        filterEmail.clear();
        filterTelephone.clear();
        filterType.setValue(null);
        filterDescription.clear();
        applyFilters();
        tableConsommateurs.refresh();
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
     * Creates a Consommateur object from form inputs.
     */
    private Consommateur createConsommateurFromForm() {
        Consommateur consommateur = new Consommateur();
        consommateur.setNom(formNom.getText().trim());
        String email = formEmail.getText().trim();
        consommateur.setEmail(email.isEmpty() ? null : email);
        String telephone = formTelephone.getText().trim();
        consommateur.setTelephone(telephone.isEmpty() ? null : telephone);
        consommateur.setType(formType.getValue());
        String description = formDescription.getText().trim();
        consommateur.setDescription(description.isEmpty() ? null : description);
        consommateur.setCreatedAt(LocalDateTime.now());
        consommateur.setUpdatedAt(LocalDateTime.now());
        return consommateur;
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        formNom.clear();
        formEmail.clear();
        formTelephone.clear();
        formType.setValue(null);
        formDescription.clear();
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