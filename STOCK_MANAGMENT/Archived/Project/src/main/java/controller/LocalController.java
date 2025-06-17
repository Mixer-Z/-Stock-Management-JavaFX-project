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
import model.dao.LocalDAO;
import model.dao.LocalDAOImpl;
import model.entities.Local;
import model.entities.Consommateur;
import utils.DatabaseConnection;
import utils.UserSession;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the local management interface.
 * Handles dynamic filters, table interactions, form submissions, and navigation.
 */
public class LocalController implements Initializable {

    private final LocalDAO localDAO = new LocalDAOImpl();
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Filter section
    @FXML private TextField filterId;
    @FXML private TextField filterNom;
    @FXML private TextField filterEmplacement;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<Consommateur> filterConsommateur;
    @FXML private DatePicker filterCreatedAt;
    @FXML private Button btnReset;

    // Table section
    @FXML private TableView<Local> tableLocaux;
    @FXML private TableColumn<Local, Long> colId;
    @FXML private TableColumn<Local, String> colNom;
    @FXML private TableColumn<Local, String> colEmplacement;
    @FXML private TableColumn<Local, String> colType;
    @FXML private TableColumn<Local, String> colCreatedAt;
    @FXML private TableColumn<Local, String> colConsommateur;
    @FXML private TableColumn<Local, Void> colActions;

    // Form section
    @FXML private TextField formNom;
    @FXML private TextField formEmplacement;
    @FXML private ComboBox<String> formType;
    @FXML private ComboBox<Consommateur> formConsommateur;
    @FXML private DatePicker formCreatedAt;
    @FXML private Button btnAjouter;
    @FXML private Button btnResetForm;

    // Navigation
    @FXML private VBox menuContainer;
    @FXML private Button logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    private ObservableList<Local> localList = FXCollections.observableArrayList();
    private ObservableList<String> expiringArticles = FXCollections.observableArrayList();
    private ObservableList<Consommateur> consommateurList = FXCollections.observableArrayList();
    private static final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean isEditing = false;
    private Local currentLocal;

    private static final String[] MENU_ITEMS = {"Accueil", "Produits", "Locaux", "Fournisseurs", "Consommateurs", "Commandes internes", "Commandes externes", "Inventaire"};
    private static final String[] MENU_ICONS = {"🏠", "📦", "🏢", "🚚", "👥", "📥", "📤", "📊"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String magasinierName = getLoggedInMagasinierName();
        usernameLabel.setText(magasinierName);

        loadConsommateurs();
        debugArticleDates();
        refreshNotifications();
        notificationBadge.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(expiringArticles.size()), expiringArticles));

        setupSidebar();
        setupFilters();
        setupTable();
        setupForm();
        loadLocals();
    }

    /**
     * Retrieves the logged-in magasinier's full name from UserSession.
     */
    private String getLoggedInMagasinierName() {
        UserSession session = UserSession.getInstance();
        String fullName = session.getFullName();
        return (fullName != null && !fullName.isEmpty()) ? fullName : "Utilisateur Inconnu";
    }

    private void loadConsommateurs() {
        String sql = "SELECT id, nom FROM consommateur";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Consommateur consommateur = new Consommateur();
                consommateur.setId(rs.getLong("id"));
                consommateur.setNom(rs.getString("nom"));
                consommateurList.add(consommateur);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la récupération des consommateurs: " + e.getMessage());
        }
    }

    /**
     * Debugs all date_peremption values in the article table.
     */
    private void debugArticleDates() {
        String sql = "SELECT nom, date_peremption FROM article WHERE date_peremption IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("Debugging article date_peremption values:");
            while (rs.next()) {
                String nom = rs.getString("nom");
                String datePeremption = rs.getString("date_peremption");
                System.out.println("Article: " + nom + ", date_peremption: " + datePeremption);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du débogage des dates de péremption: " + e.getMessage());
            e.printStackTrace();
        }
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
        String sql = "SELECT nom, date_peremption FROM article " +
                "WHERE date_peremption IS NOT NULL " +
                "AND DATE(date_peremption) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalDate now = LocalDate.now();
            LocalDate oneWeekFromNow = now.plusDays(7);
            pstmt.setString(1, now.toString());
            pstmt.setString(2, oneWeekFromNow.toString());
            System.out.println("Executing query with date range: " + now + " to " + oneWeekFromNow);
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    String nom = rs.getString("nom");
                    String datePeremptionStr = rs.getString("date_peremption");
                    try {
                        LocalDateTime datePeremption = LocalDateTime.parse(datePeremptionStr, SQLITE_DATETIME_FORMATTER);
                        String formattedDate = datePeremption.format(DISPLAY_DATE_FORMATTER);
                        expiring.add(nom + " (Périme le: " + formattedDate + ")");
                        count++;
                        System.out.println("Found article: " + nom + ", Expires: " + datePeremptionStr);
                    } catch (Exception e) {
                        System.err.println("Error parsing date_peremption for article " + nom + ": " + datePeremptionStr + ", Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                System.out.println("Found " + count + " expiring articles");
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
            VBox menuItem = createMenuItem(MENU_ITEMS[i], MENU_ICONS[i], i == 2); // Locaux is active
            menuContainer.getChildren().add(menuItem);
        }
    }

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

    private void setupFilters() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Bureau", "Entrepôt", "Salle", "Atelier", "Autre"
        );
        filterType.setItems(types);

        filterConsommateur.setItems(consommateurList);
        filterConsommateur.setConverter(new javafx.util.StringConverter<Consommateur>() {
            @Override
            public String toString(Consommateur consommateur) {
                return consommateur != null ? consommateur.getNom() : "";
            }

            @Override
            public Consommateur fromString(String string) {
                return consommateurList.stream()
                        .filter(c -> c.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        filterId.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterNom.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterEmplacement.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterType.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterConsommateur.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterCreatedAt.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmplacement.setCellValueFactory(new PropertyValueFactory<>("emplacement"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        colCreatedAt.setCellValueFactory(cellData -> {
            LocalDateTime createdAt = cellData.getValue().getCreatedAt();
            if (createdAt != null) {
                return new SimpleStringProperty(createdAt.format(displayFormatter));
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        colConsommateur.setCellValueFactory(cellData -> {
            if (cellData.getValue().getConsommateur() != null) {
                String consommateurNom = consommateurList.stream()
                        .filter(c -> c.getId().equals(cellData.getValue().getConsommateur().getId()))
                        .map(Consommateur::getNom)
                        .findFirst()
                        .orElse("ID: " + cellData.getValue().getConsommateur().getId());
                return new SimpleStringProperty(consommateurNom);
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

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
                    Local local = getTableRow().getItem();
                    modifyButton.setOnAction(e -> handleModify(local));
                    deleteButton.setOnAction(e -> handleDelete(local));
                    setGraphic(buttons);
                }
            }
        });

        tableLocaux.setItems(localList);
    }

    private void setupForm() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Bureau", "Entrepôt", "Salle", "Atelier", "Autre"
        );
        formType.setItems(types);

        formConsommateur.setItems(consommateurList);
        formConsommateur.setConverter(new javafx.util.StringConverter<Consommateur>() {
            @Override
            public String toString(Consommateur consommateur) {
                return consommateur != null ? consommateur.getNom() : "";
            }

            @Override
            public Consommateur fromString(String string) {
                return consommateurList.stream()
                        .filter(c -> c.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void loadLocals() {
        try {
            List<Local> locals = localDAO.getAll();
            localList.setAll(locals);
            applyFilters();
            refreshNotifications();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des locaux: " + e.getMessage());
            localList.clear();
        }
    }

    private void applyFilters() {
        String idFilter = filterId.getText().trim();
        String nomFilter = filterNom.getText().trim().toLowerCase();
        String emplacementFilter = filterEmplacement.getText().trim().toLowerCase();
        String typeFilter = filterType.getValue();
        Consommateur consommateurFilter = filterConsommateur.getValue();
        java.time.LocalDate createdAtFilter = filterCreatedAt.getValue();

        ObservableList<Local> filteredList = FXCollections.observableArrayList();
        for (Local local : localList) {
            boolean matches = true;

            if (!idFilter.isEmpty()) {
                try {
                    String idString = String.valueOf(local.getId());
                    if (!idString.startsWith(idFilter)) {
                        matches = false;
                    }
                } catch (NumberFormatException e) {
                    matches = false;
                }
            }
            if (!nomFilter.isEmpty() && !local.getNom().toLowerCase().startsWith(nomFilter)) {
                matches = false;
            }
            if (!emplacementFilter.isEmpty() && !local.getEmplacement().toLowerCase().startsWith(emplacementFilter)) {
                matches = false;
            }
            if (typeFilter != null && !local.getType().equals(typeFilter)) {
                matches = false;
            }
            if (consommateurFilter != null) {
                if (local.getConsommateur() == null || !local.getConsommateur().getId().equals(consommateurFilter.getId())) {
                    matches = false;
                }
            }
            if (createdAtFilter != null) {
                java.time.LocalDate localDate = local.getCreatedAt() != null ?
                        local.getCreatedAt().toLocalDate() : null;
                if (localDate == null || !localDate.equals(createdAtFilter)) {
                    matches = false;
                }
            }

            if (matches) {
                filteredList.add(local);
            }
        }
        tableLocaux.setItems(filteredList);
        tableLocaux.refresh();
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm()) return;

        if (!isEditing) {
            Local newLocal = createLocalFromForm();
            try {
                localDAO.insert(newLocal);
                loadLocals();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Local ajouté avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du local: " + e.getMessage());
            }
        } else {
            currentLocal.setNom(formNom.getText());
            currentLocal.setEmplacement(formEmplacement.getText());
            currentLocal.setType(formType.getValue());
            if (formCreatedAt.getValue() != null) {
                currentLocal.setCreatedAt(
                        LocalDateTime.of(formCreatedAt.getValue(), java.time.LocalTime.MIDNIGHT));
            } else {
                currentLocal.setCreatedAt(null);
            }
            currentLocal.setConsommateur(formConsommateur.getValue());
            try {
                localDAO.update(currentLocal);
                loadLocals();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Local mis à jour avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du local: " + e.getMessage());
            }
            isEditing = false;
            btnAjouter.setText("Ajouter");
        }
        clearForm();
    }

    private void handleModify(Local local) {
        currentLocal = local;
        isEditing = true;
        formNom.setText(local.getNom());
        formEmplacement.setText(local.getEmplacement());
        formType.setValue(local.getType());
        if (local.getCreatedAt() != null) {
            formCreatedAt.setValue(local.getCreatedAt().toLocalDate());
        } else {
            formCreatedAt.setValue(null);
        }
        formConsommateur.setValue(local.getConsommateur());
        btnAjouter.setText("Sauvegarder");
    }

    private void handleDelete(Local local) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le local");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce local ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                localDAO.delete(local.getId());
                loadLocals();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Local supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du local: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleReset() {
        filterId.clear();
        filterNom.clear();
        filterEmplacement.clear();
        filterType.setValue(null);
        filterConsommateur.setValue(null);
        filterCreatedAt.setValue(null);
        applyFilters();
        tableLocaux.refresh();
    }

    @FXML
    private void handleResetForm() {
        clearForm();
    }

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

    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();

        if (formNom.getText().trim().isEmpty()) {
            errorMessage.append("- Le nom est requis\n");
        }
        if (formEmplacement.getText().trim().isEmpty()) {
            errorMessage.append("- L'emplacement est requis\n");
        }
        if (formType.getValue() == null) {
            errorMessage.append("- Le type est requis\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return false;
        }
        return true;
    }

    private Local createLocalFromForm() {
        String nom = formNom.getText().trim();
        String emplacement = formEmplacement.getText().trim();
        String type = formType.getValue();
        Local local = new Local();
        local.setNom(nom);
        local.setEmplacement(emplacement);
        local.setType(type);
        if (formCreatedAt.getValue() != null) {
            local.setCreatedAt(LocalDateTime.of(formCreatedAt.getValue(), java.time.LocalTime.MIDNIGHT));
        }
        local.setConsommateur(formConsommateur.getValue());
        return local;
    }

    private void clearForm() {
        formNom.clear();
        formEmplacement.clear();
        formType.setValue(null);
        formConsommateur.setValue(null);
        formCreatedAt.setValue(null);
        isEditing = false;
        btnAjouter.setText("Ajouter");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}