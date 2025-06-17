package controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import utils.UserSession;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StockDashboardController {

    private static final Logger LOGGER = Logger.getLogger(StockDashboardController.class.getName());

    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private HBox topBar;
    @FXML private VBox mainContent;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox menuContainer;
    @FXML private HBox categories;
    @FXML private Button generatePdfBtn;
    @FXML private Button logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    private static final String[] MENU_ITEMS = {"Accueil", "Produits", "Locaux", "Fournisseurs", "Consommateurs", "Commandes internes", "Commandes externes", "Inventaire"};
    private static final String[] MENU_ICONS = {"🏠", "📦", "🏢", "🚚", "👥", "📥", "📤", "📊"};
    private ObservableList<String> expiringProducts = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        initializeDatabase();
        logAllProducts();

        String magasinierName = getLoggedInMagasinierName();
        usernameLabel.setText(magasinierName);

        for (int i = 0; i < MENU_ITEMS.length; i++) {
            VBox menuItem = createMenuItem(MENU_ITEMS[i], MENU_ICONS[i], i == 0);
            menuContainer.getChildren().add(menuItem);
        }

        updateCategories();

        scrollPane.getContent().setOnScroll(event -> {
            double deltaY = event.getDeltaY() * 2;
            double width = scrollPane.getContent().getBoundsInLocal().getWidth();
            double vvalue = scrollPane.getVvalue();
            scrollPane.setVvalue(vvalue - deltaY / width);
            event.consume();
        });

        if (notificationIcon != null) {
            LOGGER.info("Notification icon initialized successfully.");
        } else {
            LOGGER.severe("Notification icon is null. Check fx:id in FXML.");
        }

        refreshNotifications();
        notificationBadge.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(expiringProducts.size()), expiringProducts));

        generatePdfBtn.setOnAction(event -> generateCommandesText());
    }

    private void initializeDatabase() {
        // Schema is already created as per provided SQL; no changes needed here
    }

    private void logAllProducts() {
        String query = "SELECT id, reference, nom, stock_minimal, date_peremption FROM article";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                LOGGER.info("Product: [ID=" + rs.getInt("id") + ", Nom=" + rs.getString("nom") + ", Stock Minimal=" + rs.getInt("stock_minimal") + "]");
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error while logging products: " + e.getMessage());
        }
    }

    private String getLoggedInMagasinierName() {
        UserSession session = UserSession.getInstance();
        String fullName = session.getFullName();
        return fullName != null ? fullName : "Utilisateur Inconnu";
    }

    private void refreshNotifications() {
        List<String> updatedProducts = getExpiringProducts();
        expiringProducts.setAll(updatedProducts);
    }

    @FXML
    private void handleNotificationClick() {
        refreshNotifications();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications - Produits en Péremption");
        alert.setHeaderText(null);
        alert.setContentText(expiringProducts.isEmpty() ? "Aucun produit ne périme dans les 7 prochains jours." :
                "Produits périmant dans les 7 prochains jours:\n" + String.join("\n", expiringProducts));
        alert.showAndWait();
    }

    private List<String> getExpiringProducts() {
        List<String> products = new ArrayList<>();
        String query = "SELECT nom, date_peremption FROM article WHERE date_peremption IS NOT NULL AND date_peremption <= datetime('now', '+7 days')";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                products.add(rs.getString("nom") + " (Périme le: " + rs.getString("date_peremption") + ")");
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des produits en péremption: " + e.getMessage());
        }
        return products;
    }

    private void updateCategories() {
        categories.getChildren().clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            categories.getChildren().addAll(
                    createCategoryBox("Produits", getCount(conn, "SELECT COUNT(*) FROM article"), "📦"),
                    createCategoryBox("Commandes internes", getCount(conn, "SELECT COUNT(*) FROM commande_interne"), "📥"),
                    createCategoryBox("Commandes externes", getCount(conn, "SELECT COUNT(*) FROM commande_externe"), "📤"),
                    createCategoryBox("Fournisseurs", getCount(conn, "SELECT COUNT(*) FROM fournisseur"), "🚚"),
                    createCategoryBox("Consommateurs", getCount(conn, "SELECT COUNT(*) FROM consommateur"), "👥"),
                    createCategoryBox("Locaux", getCount(conn, "SELECT COUNT(*) FROM local"), "🏢")
            );
        } catch (SQLException e) {
            LOGGER.severe("Error updating categories: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour des catégories: " + e.getMessage());
        }
    }

    private int getCount(Connection conn, String query) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void generateCommandesText() {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT 'Interne' AS type, ci.id, ci.created_at AS date, GROUP_CONCAT(a.nom) AS articles, SUM(cia.quantite) AS quantite, COALESCE(l.nom, 'No Location') AS local " +
                "FROM commande_interne ci " +
                "LEFT JOIN commande_interne_article cia ON ci.id = cia.commande_interne_id " +
                "LEFT JOIN article a ON cia.article_id = a.id " +
                "LEFT JOIN local l ON ci.local_id = l.id " +
                "GROUP BY ci.id " +
                "UNION ALL " +
                "SELECT 'Externe' AS type, ce.id, ce.created_at AS date, GROUP_CONCAT(a.nom) AS articles, SUM(cea.quantite) AS quantite, COALESCE(l.nom, 'No Location') AS local " +
                "FROM commande_externe ce " +
                "LEFT JOIN commande_externe_article cea ON ce.id = cea.commande_externe_id " +
                "LEFT JOIN article a ON cea.article_id = a.id " +
                "LEFT JOIN local l ON ce.local_id = l.id " +
                "GROUP BY ce.id";
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Log table counts for debugging
            LOGGER.info("Commande_interne count: " + getCount(conn, "SELECT COUNT(*) FROM commande_interne"));
            LOGGER.info("Commande_externe count: " + getCount(conn, "SELECT COUNT(*) FROM commande_externe"));
            LOGGER.info("Commande_interne_article count: " + getCount(conn, "SELECT COUNT(*) FROM commande_interne_article"));
            LOGGER.info("Commande_externe_article count: " + getCount(conn, "SELECT COUNT(*) FROM commande_externe_article"));
            LOGGER.info("Article count: " + getCount(conn, "SELECT COUNT(*) FROM article"));
            LOGGER.info("Local count: " + getCount(conn, "SELECT COUNT(*) FROM local"));

            try (PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    String articles = rs.getString("articles") != null ? rs.getString("articles") : "Aucun article";
                    String local = rs.getString("local"); // COALESCE ensures non-null value
                    int quantite = rs.getInt("quantite"); // Will be 0 if no articles
                    commandes.add(new Commande(
                            rs.getInt("id"),
                            rs.getString("date"),
                            articles,
                            quantite,
                            local,
                            rs.getString("type")
                    ));
                    LOGGER.fine("Commande found: [ID=" + rs.getInt("id") + ", Type=" + rs.getString("type") + ", Articles=" + articles + ", Local=" + local + "]");
                }
                LOGGER.info("Total commandes retrieved: " + count);
                if (count == 0) {
                    LOGGER.warning("No commandes found in the database.");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving commandes: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des commandes: " + e.getMessage());
            return;
        }

        // Format text file content
        StringBuilder textContent = new StringBuilder();
        textContent.append("Commandes Log\n");
        textContent.append("-------------\n");
        textContent.append(String.format("%-4s | %-19s | %-30s | %-8s | %-20s | %-8s\n",
                "ID", "Date", "Articles", "Quantité", "Local", "Type"));
        textContent.append(String.format("%-4s | %-19s | %-30s | %-8s | %-20s | %-8s\n",
                "----", "-------------------", "------------------------------", "--------", "--------------------", "--------"));

        if (commandes.isEmpty()) {
            textContent.append("Aucune commande trouvée\n");
        } else {
            for (Commande cmd : commandes) {
                // Truncate or pad fields to align columns
                String articles = cmd.articles.length() > 30 ? cmd.articles.substring(0, 27) + "..." : String.format("%-30s", cmd.articles);
                String local = cmd.local.length() > 20 ? cmd.local.substring(0, 17) + "..." : String.format("%-20s", cmd.local);
                textContent.append(String.format("%-4d | %-19s | %s | %-8d | %s | %-8s\n",
                        cmd.id, cmd.date, articles, cmd.quantite, local, cmd.type));
            }
        }

        // Save to text file in project folder
        String outputPath = "./commandes_log.txt";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(textContent.toString());
            LOGGER.info("Text file saved to: " + new File(outputPath).getAbsolutePath());
        } catch (IOException e) {
            LOGGER.severe("Error saving text file: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la sauvegarde du fichier texte: " + e.getMessage());
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Fichier texte généré à: " + outputPath);
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
                ((Stage) root.getScene().getWindow()).close();
            } catch (Exception ex) {
                LOGGER.severe("Navigation error: " + ex.getMessage());
            }
        });

        return item;
    }

    private VBox createCategoryBox(String name, int count, String icon) {
        VBox box = new VBox(8);
        box.getStyleClass().add("category-box");
        box.setPrefSize(200, 120);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("category-icon");

        Label title = new Label(name);
        title.getStyleClass().add("category-title");

        Label subtitle = new Label(count + " éléments");
        subtitle.getStyleClass().add("category-subtitle");

        box.getChildren().addAll(iconLabel, title, subtitle);
        return box;
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().clear();
            new LoginApp().start(new Stage());
            ((Stage) logoutBtn.getScene().getWindow()).close();
        } catch (Exception ex) {
            LOGGER.severe("Logout error: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static class Commande {
        int id;
        String date;
        String articles;
        int quantite;
        String local;
        String type;

        Commande(int id, String date, String articles, int quantite, String local, String type) {
            this.id = id;
            this.date = date;
            this.articles = articles;
            this.quantite = quantite;
            this.local = local;
            this.type = type;
        }
    }
}