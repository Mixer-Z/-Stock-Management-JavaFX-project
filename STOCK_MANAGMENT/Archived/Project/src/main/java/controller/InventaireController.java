package controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for the inventory management interface.
 * Displays articles with their quantities (CommandeExterne - CommandeInterne) and associated locals.
 */
public class InventaireController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(InventaireController.class.getName());
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Filter section
    @FXML private TextField filterLocal;
    @FXML private TextField filterArticle;
    @FXML private TextField filterQuantite;
    @FXML private Button btnReset;

    // Table section
    @FXML private TableView<ArticleInventory> tableStock;
    @FXML private TableColumn<ArticleInventory, String> colArticle;
    @FXML private TableColumn<ArticleInventory, Integer> colQuantite;
    @FXML private TableColumn<ArticleInventory, String> colLocals;

    // Navigation
    @FXML private VBox menuContainer;
    @FXML private Button logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    private ObservableList<ArticleInventory> inventoryList = FXCollections.observableArrayList();
    private ObservableList<String> expiringArticles = FXCollections.observableArrayList();

    private static final String[] MENU_ITEMS = {"Accueil", "Produits", "Locaux", "Fournisseurs", "Consommateurs", "Commandes internes", "Commandes externes", "Inventaire"};
    private static final String[] MENU_ICONS = {"🏠", "📦", "🏢", "🚚", "👥", "📥", "📤", "📊"};

    /**
     * Class to represent article inventory data for display.
     */
    public static class ArticleInventory {
        private final String articleName;
        private final int quantite;
        private final String locals;

        public ArticleInventory(String articleName, int quantite, String locals) {
            this.articleName = articleName;
            this.quantite = quantite;
            this.locals = locals;
        }

        public String getArticleName() {
            return articleName;
        }

        public int getQuantite() {
            return quantite;
        }

        public String getLocals() {
            return locals;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (notificationBadge == null) {
            LOGGER.severe("notificationBadge is null. Check FXML fx:id.");
        }
        if (notificationIcon == null) {
            LOGGER.severe("notificationIcon is null. Check FXML fx:id.");
        }
        if (usernameLabel == null) {
            LOGGER.severe("usernameLabel is null. Check FXML fx:id.");
        }

        String magasinierName = getLoggedInMagasinierName();
        if (usernameLabel != null) {
            usernameLabel.setText(magasinierName);
        }

        if (notificationBadge != null) {
            notificationBadge.textProperty().bind(Bindings.createStringBinding(
                    () -> String.valueOf(expiringArticles.size()), expiringArticles));
        }

        setupSidebar();
        setupFilters();
        setupTable();
        loadInventory();
        refreshNotifications();
    }

    private String getLoggedInMagasinierName() {
        UserSession session = UserSession.getInstance();
        String fullName = session.getFullName();
        return (fullName != null && !fullName.isEmpty()) ? fullName : "Utilisateur Inconnu";
    }

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

    private void refreshNotifications() {
        List<String> expiring = getExpiringArticles();
        expiringArticles.setAll(expiring);
    }

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
            LOGGER.severe("Erreur lors de la récupération des articles en péremption: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des articles en péremption: " + e.getMessage());
        }
        return expiring;
    }

    private void setupSidebar() {
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            VBox menuItem = createMenuItem(MENU_ITEMS[i], MENU_ICONS[i], i == 7); // Inventaire is active
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
                    default:
                        LOGGER.warning("Unknown menu item: " + text);
                        return;
                }
                ((Stage) menuContainer.getScene().getWindow()).close();
            } catch (Exception ex) {
                LOGGER.severe("Error navigating to " + text + ": " + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers " + text + ": " + ex.getMessage());
            }
        });

        return item;
    }

    private void setupFilters() {
        filterLocal.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterArticle.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterQuantite.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTable() {
        tableStock.setPlaceholder(new Label("Aucun inventaire trouvé"));

        colArticle.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getArticleName()));
        colQuantite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());
        colLocals.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLocals()));

        tableStock.setItems(inventoryList);
    }

    private void loadInventory() {
        try {
            List<ArticleInventory> inventories = getArticleInventories();
            inventoryList.setAll(inventories);
            LOGGER.info("Nombre d'inventaires chargés: " + inventories.size());
        } catch (RuntimeException e) {
            LOGGER.severe("Erreur lors du chargement de l'inventaire: " + e.getMessage());
            inventoryList.clear();
            tableStock.setPlaceholder(new Label("Erreur lors du chargement de l'inventaire"));
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'inventaire: " + e.getMessage());
        }
        applyFilters();
    }

    private List<ArticleInventory> getArticleInventories() {
        List<ArticleInventory> inventories = new ArrayList<>();
        String sql = """
            SELECT 
                a.nom AS article_name,
                COALESCE(SUM(cea.quantite), 0) - COALESCE(SUM(cia.quantite), 0) AS total_quantite,
                GROUP_CONCAT(DISTINCT l.nom) AS locals
            FROM article a
            LEFT JOIN commande_externe_article cea ON a.id = cea.article_id
            LEFT JOIN commande_externe_local cel ON cea.commande_externe_id = cel.commande_externe_id
            LEFT JOIN commande_interne_article cia ON a.id = cia.article_id
            LEFT JOIN commande_interne_local cil ON cia.commande_interne_id = cil.commande_interne_id
            LEFT JOIN local l ON l.id = cel.local_id OR l.id = cil.local_id
            GROUP BY a.id, a.nom
            HAVING total_quantite != 0 OR locals IS NOT NULL
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String articleName = rs.getString("article_name");
                int quantite = rs.getInt("total_quantite");
                String locals = rs.getString("locals") != null ? rs.getString("locals") : "N/A";
                LOGGER.fine("Loaded inventory: article=" + articleName + ", quantite=" + quantite + ", locals=" + locals);
                inventories.add(new ArticleInventory(articleName, quantite, locals));
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la récupération des inventaires: " + e.getMessage());
            throw new RuntimeException("Erreur SQL: " + e.getMessage(), e);
        }
        return inventories;
    }

    private void applyFilters() {
        String localFilter = filterLocal.getText() != null ? filterLocal.getText().trim().toLowerCase() : "";
        String articleFilter = filterArticle.getText() != null ? filterArticle.getText().trim().toLowerCase() : "";
        String quantiteFilter = filterQuantite.getText() != null ? filterQuantite.getText().trim() : "";

        ObservableList<ArticleInventory> filteredList = FXCollections.observableArrayList();
        LOGGER.info("Applying filters: local=" + localFilter + ", article=" + articleFilter + ", quantite=" + quantiteFilter);

        for (ArticleInventory inventory : inventoryList) {
            if (inventory == null) {
                LOGGER.warning("Null inventory entry found, skipping.");
                continue;
            }
            boolean matches = true;

            // Local filter
            String locals = inventory.getLocals() != null ? inventory.getLocals().toLowerCase() : "";
            if (!localFilter.isEmpty()) {
                boolean localMatches = false;
                if (locals.equals("n/a") || locals.isEmpty()) {
                    matches = false;
                } else {
                    String[] localArray = locals.split(",");
                    for (String local : localArray) {
                        String trimmedLocal = local.trim();
                        LOGGER.fine("Checking local: " + trimmedLocal + " against filter: " + localFilter);
                        if (trimmedLocal.startsWith(localFilter)) {
                            localMatches = true;
                            break;
                        }
                    }
                    if (!localMatches) {
                        matches = false;
                    }
                }
            }

            // Article filter
            String articleName = inventory.getArticleName() != null ? inventory.getArticleName().toLowerCase() : "";
            if (!articleFilter.isEmpty() && !articleName.startsWith(articleFilter)) {
                matches = false;
            }

            // Quantity filter
            if (!quantiteFilter.isEmpty()) {
                String quantiteStr = String.valueOf(inventory.getQuantite());
                LOGGER.fine("Checking quantity: " + quantiteStr + " against filter: " + quantiteFilter);
                if (!quantiteStr.startsWith(quantiteFilter)) {
                    matches = false;
                }
            }

            if (matches) {
                filteredList.add(inventory);
                LOGGER.fine("Match found: " + inventory.getArticleName() + ", " + inventory.getQuantite() + ", " + inventory.getLocals());
            }
        }

        tableStock.setItems(filteredList);
        tableStock.setPlaceholder(new Label(filteredList.isEmpty() ? "Aucun résultat ne correspond aux filtres" : "Aucun inventaire trouvé"));
        tableStock.refresh();
        LOGGER.info("Filtered list size: " + filteredList.size());
    }

    @FXML
    private void handleReset() {
        filterLocal.clear();
        filterArticle.clear();
        filterQuantite.clear();
        applyFilters();
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().clear();
            new LoginApp().start(new Stage());
            ((Stage) logoutBtn.getScene().getWindow()).close();
        } catch (Exception ex) {
            LOGGER.severe("Error during logout: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}