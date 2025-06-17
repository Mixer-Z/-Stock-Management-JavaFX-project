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
import model.dao.CommandeExterneDAO;
import model.dao.CommandeExterneDAOImpl;
import model.entities.*;
import org.controlsfx.control.CheckComboBox;
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
import java.util.stream.Collectors;

/**
 * Controller for the external order management interface.
 * Handles dynamic filters, table interactions, form submissions, and navigation.
 */
public class CommandeExterneController implements Initializable {

    private final CommandeExterneDAO commandeExterneDAO = new CommandeExterneDAOImpl();
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Filter section
    @FXML private TextField filterId;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<Magasinier> filterMagasinier;
    @FXML private ComboBox<Fournisseur> filterFournisseur;
    @FXML private CheckComboBox<Local> filterLocal;
    @FXML private CheckComboBox<Article> filterArticles;
    @FXML private DatePicker filterCreatedAt;
    @FXML private Button btnReset;

    // Table section
    @FXML private TableView<CommandeExterne> tableCommandes;
    @FXML private TableColumn<CommandeExterne, Long> colId;
    @FXML private TableColumn<CommandeExterne, String> colStatut;
    @FXML private TableColumn<CommandeExterne, String> colMagasinier;
    @FXML private TableColumn<CommandeExterne, String> colFournisseur;
    @FXML private TableColumn<CommandeExterne, String> colLocal;
    @FXML private TableColumn<CommandeExterne, String> colArticles;
    @FXML private TableColumn<CommandeExterne, String> colCreatedAt;
    @FXML private TableColumn<CommandeExterne, Void> colActions;

    // Form section
    @FXML private ComboBox<String> formStatut;
    @FXML private ComboBox<Fournisseur> formFournisseur;
    @FXML private CheckComboBox<Local> formLocal;
    @FXML private CheckComboBox<Article> formArticles;
    @FXML private DatePicker formCreatedAt;
    @FXML private Button btnAjouter;
    @FXML private Button btnResetForm;

    // Navigation
    @FXML private VBox menuContainer;
    @FXML private Button logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    private ObservableList<CommandeExterne> commandeList = FXCollections.observableArrayList();
    private ObservableList<String> expiringArticles = FXCollections.observableArrayList();
    private ObservableList<Magasinier> magasinierList = FXCollections.observableArrayList();
    private ObservableList<Fournisseur> fournisseurList = FXCollections.observableArrayList();
    private ObservableList<Local> localList = FXCollections.observableArrayList();
    private ObservableList<Local> filteredLocalList = FXCollections.observableArrayList();
    private ObservableList<Article> articleList = FXCollections.observableArrayList();
    private ObservableList<Article> filteredArticleList = FXCollections.observableArrayList();
    private boolean isEditing = false;
    private CommandeExterne currentCommande;

    private static final String[] MENU_ITEMS = {"Accueil", "Produits", "Locaux", "Fournisseurs", "Consommateurs", "Commandes internes", "Commandes externes", "Inventaire"};
    private static final String[] MENU_ICONS = {"🏠", "📦", "🏢", "🚚", "👥", "📥", "📤", "📊"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String magasinierName = getLoggedInMagasinierName();
        usernameLabel.setText(magasinierName);

        loadMagasiniers();
        loadFournisseurs();
        loadLocals();
        loadArticles();
        refreshNotifications();
        notificationBadge.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(expiringArticles.size()), expiringArticles));

        setupSidebar();
        setupFilters();
        setupTable();
        setupForm();
        loadCommandes();
    }

    private String getLoggedInMagasinierName() {
        UserSession session = UserSession.getInstance();
        String fullName = session.getFullName();
        return (fullName != null && !fullName.isEmpty()) ? fullName : "Utilisateur Inconnu";
    }

    private void loadMagasiniers() {
        String sql = "SELECT id, nom FROM magasinier";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            magasinierList.clear();
            while (rs.next()) {
                Magasinier magasinier = new Magasinier();
                magasinier.setId(rs.getLong("id"));
                magasinier.setNom(rs.getString("nom"));
                magasinierList.add(magasinier);
            }
            System.out.println("Loaded " + magasinierList.size() + " magasiniers");
        } catch (SQLException e) {
            System.err.println("Error loading magasiniers: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des magasiniers: " + e.getMessage());
        }
    }

    private void loadFournisseurs() {
        String sql = "SELECT id, nom FROM fournisseur";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            fournisseurList.clear();
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setId(rs.getLong("id"));
                fournisseur.setNom(rs.getString("nom"));
                fournisseurList.add(fournisseur);
            }
            System.out.println("Loaded " + fournisseurList.size() + " fournisseurs");
        } catch (SQLException e) {
            System.err.println("Error loading fournisseurs: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des fournisseurs: " + e.getMessage());
        }
    }

    private void loadLocals() {
        String sql = "SELECT id, nom FROM local";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            localList.clear();
            while (rs.next()) {
                Local local = new Local();
                local.setId(rs.getLong("id"));
                local.setNom(rs.getString("nom"));
                localList.add(local);
            }
            System.out.println("Loaded " + localList.size() + " locals: " + localList.stream().map(Local::getNom).collect(Collectors.joining(", ")));
            if (localList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun local chargé. Vérifiez la table 'local'.");
            }
        } catch (SQLException e) {
            System.err.println("Error loading locals: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des locaux: " + e.getMessage());
        }
    }

    private void loadArticles() {
        String sql = "SELECT id, nom FROM article";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            articleList.clear();
            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getLong("id"));
                article.setNom(rs.getString("nom"));
                articleList.add(article);
            }
            System.out.println("Loaded " + articleList.size() + " articles: " + articleList.stream().map(Article::getNom).collect(Collectors.joining(", ")));
            if (articleList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun article chargé. Vérifiez la table 'article'.");
            }
        } catch (SQLException e) {
            System.err.println("Error loading articles: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des articles: " + e.getMessage());
        }
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
        List<String> updatedArticles = getExpiringArticles();
        expiringArticles.setAll(updatedArticles);
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
            System.err.println("Erreur lors de la récupération des articles en péremption: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des articles en péremption: " + e.getMessage());
        }
        return expiring;
    }

    private void setupSidebar() {
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            VBox menuItem = createMenuItem(MENU_ITEMS[i], MENU_ICONS[i], i == 6); // Commandes externes is active
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
        ObservableList<String> statuts = FXCollections.observableArrayList("En attente", "Confirmé", "Annulé", "Livré");
        filterStatut.setItems(statuts);

        filterMagasinier.setItems(magasinierList);
        filterMagasinier.setConverter(new javafx.util.StringConverter<Magasinier>() {
            @Override
            public String toString(Magasinier magasinier) {
                return magasinier != null ? magasinier.getNom() != null ? magasinier.getNom() : "" : "";
            }

            @Override
            public Magasinier fromString(String string) {
                return magasinierList.stream().filter(m -> m.getNom() != null && m.getNom().equals(string)).findFirst().orElse(null);
            }
        });

        filterFournisseur.setItems(fournisseurList);
        filterFournisseur.setConverter(new javafx.util.StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur fournisseur) {
                return fournisseur != null ? fournisseur.getNom() != null ? fournisseur.getNom() : "" : "";
            }

            @Override
            public Fournisseur fromString(String string) {
                return fournisseurList.stream().filter(f -> f.getNom() != null && f.getNom().equals(string)).findFirst().orElse(null);
            }
        });

        filterLocal.getItems().addAll(localList);
        filterLocal.setConverter(new javafx.util.StringConverter<Local>() {
            @Override
            public String toString(Local local) {
                return local != null ? local.getNom() != null ? local.getNom() : "" : "";
            }

            @Override
            public Local fromString(String string) {
                return localList.stream().filter(l -> l.getNom() != null && l.getNom().equals(string)).findFirst().orElse(null);
            }
        });

        filterArticles.getItems().addAll(articleList);
        filterArticles.setConverter(new javafx.util.StringConverter<Article>() {
            @Override
            public String toString(Article article) {
                return article != null ? article.getNom() != null ? article.getNom() : "" : "";
            }

            @Override
            public Article fromString(String string) {
                return articleList.stream().filter(a -> a.getNom() != null && a.getNom().equals(string)).findFirst().orElse(null);
            }
        });

        filterId.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterStatut.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterMagasinier.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterFournisseur.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterLocal.getCheckModel().getCheckedItems().addListener((javafx.collections.ListChangeListener<Local>) c -> applyFilters());
        filterArticles.getCheckModel().getCheckedItems().addListener((javafx.collections.ListChangeListener<Article>) c -> applyFilters());
        filterCreatedAt.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTable() {
        tableCommandes.setPlaceholder(new Label("Aucune commande externe trouvée"));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colMagasinier.setCellValueFactory(cellData -> {
            Magasinier magasinier = cellData.getValue().getMagasinier();
            String magasinierName = "N/A";
            if (magasinier != null && magasinier.getNom() != null && !magasinier.getNom().isEmpty()) {
                magasinierName = magasinier.getNom();
            } else {
                System.err.println("Magasinier is null or nom is empty for commande ID: " + cellData.getValue().getId());
            }
            return new SimpleStringProperty(magasinierName);
        });

        colFournisseur.setCellValueFactory(cellData -> {
            Fournisseur fournisseur = cellData.getValue().getFournisseur();
            return new SimpleStringProperty(fournisseur != null ? fournisseur.getNom() != null ? fournisseur.getNom() : "N/A" : "N/A");
        });

        colLocal.setCellValueFactory(cellData -> {
            List<CommandeExterneLocal> locals = cellData.getValue().getCommandeExterneLocals();
            System.out.println("Commande ID " + cellData.getValue().getId() + " has " + (locals != null ? locals.size() : 0) + " locals");
            if (locals == null || locals.isEmpty()) {
                System.out.println("No locals for commande ID " + cellData.getValue().getId());
                return new SimpleStringProperty("N/A");
            }
            String localNames = locals.stream()
                    .map(cl -> cl.getLocal() != null && cl.getLocal().getNom() != null ? cl.getLocal().getNom() : "Unknown")
                    .collect(Collectors.joining(", "));
            System.out.println("Local names for commande ID " + cellData.getValue().getId() + ": " + localNames);
            return new SimpleStringProperty(localNames.isEmpty() ? "N/A" : localNames);
        });

        colArticles.setCellValueFactory(cellData -> {
            List<CommandeExterneArticle> articles = cellData.getValue().getCommandeExterneArticles();
            System.out.println("Commande ID " + cellData.getValue().getId() + " has " + (articles != null ? articles.size() : 0) + " articles");
            if (articles == null || articles.isEmpty()) {
                System.out.println("No articles for commande ID " + cellData.getValue().getId());
                return new SimpleStringProperty("N/A");
            }
            String articleNames = articles.stream()
                    .map(ca -> {
                        String name = ca.getArticle() != null && ca.getArticle().getNom() != null ? ca.getArticle().getNom() : "Unknown";
                        int quantite = ca.getQuantite();
                        return name + " (" + quantite + ")";
                    })
                    .collect(Collectors.joining(", "));
            System.out.println("Article names for commande ID " + cellData.getValue().getId() + ": " + articleNames);
            return new SimpleStringProperty(articleNames.isEmpty() ? "N/A" : articleNames);
        });

        colCreatedAt.setCellValueFactory(cellData -> {
            LocalDateTime createdAt = cellData.getValue().getCreerA();
            return new SimpleStringProperty(createdAt != null ? createdAt.format(DISPLAY_DATE_FORMATTER) : "N/A");
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
                    CommandeExterne commande = getTableRow().getItem();
                    modifyButton.setOnAction(e -> handleModify(commande));
                    deleteButton.setOnAction(e -> handleDelete(commande));
                    setGraphic(buttons);
                }
            }
        });

        tableCommandes.setItems(commandeList);
    }

    private void setupForm() {
        ObservableList<String> statuts = FXCollections.observableArrayList("En attente", "Confirmé", "Annulé", "Livré");
        formStatut.setItems(statuts);

        formFournisseur.setItems(fournisseurList);
        formFournisseur.setConverter(new javafx.util.StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur fournisseur) {
                return fournisseur != null ? fournisseur.getNom() != null ? fournisseur.getNom() : "" : "";
            }

            @Override
            public Fournisseur fromString(String string) {
                return fournisseurList.stream().filter(f -> f.getNom() != null && f.getNom().equals(string)).findFirst().orElse(null);
            }
        });

        filteredLocalList.setAll(localList);
        System.out.println("Initial filteredLocalList size: " + filteredLocalList.size());
        formLocal.getItems().clear();
        formLocal.getItems().addAll(filteredLocalList);
        formLocal.setConverter(new javafx.util.StringConverter<Local>() {
            @Override
            public String toString(Local local) {
                return local != null ? local.getNom() != null ? local.getNom() : "" : "";
            }

            @Override
            public Local fromString(String string) {
                return filteredLocalList.stream()
                        .filter(l -> l.getNom() != null && l.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        filteredArticleList.setAll(articleList);
        System.out.println("Initial filteredArticleList size: " + filteredArticleList.size());
        formArticles.getItems().clear();
        formArticles.getItems().addAll(filteredArticleList);
        formArticles.setConverter(new javafx.util.StringConverter<Article>() {
            @Override
            public String toString(Article article) {
                return article != null ? article.getNom() != null ? article.getNom() : "" : "";
            }

            @Override
            public Article fromString(String string) {
                return filteredArticleList.stream()
                        .filter(a -> a.getNom() != null && a.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // No filtering of locals based on fournisseur since Local is independent
        formFournisseur.valueProperty().addListener((obs, oldVal, newVal) -> {
            formLocal.getCheckModel().clearChecks();
            formArticles.getCheckModel().clearChecks();
            filteredArticleList.setAll(articleList);
            System.out.println("Fournisseur selected, showing all articles: " + articleList.size());
            formArticles.getItems().setAll(filteredArticleList);
        });

        formLocal.getCheckModel().getCheckedItems().addListener((javafx.collections.ListChangeListener<Local>) c -> {
            ObservableList<Local> selectedLocals = formLocal.getCheckModel().getCheckedItems();
            System.out.println("Selected locals: " + selectedLocals.stream().map(Local::getNom).collect(Collectors.joining(", ")));
        });
    }

    private void loadCommandes() {
        try {
            if (commandeExterneDAO == null) {
                throw new IllegalStateException("CommandeExterneDAO is not initialized");
            }
            List<CommandeExterne> commandes = commandeExterneDAO.getAll();
            if (commandes == null) {
                System.err.println("CommandeExterneDAO returned null list");
                commandes = new ArrayList<>();
            }
            for (CommandeExterne commande : commandes) {
                try {
                    if (commande.getCommandeExterneArticles() == null) {
                        commande.setCommandeExterneArticles(new ArrayList<>());
                        System.out.println("No articles loaded for commande_externe_id: " + commande.getId());
                    }
                    if (commande.getCommandeExterneLocals() == null) {
                        commande.setCommandeExterneLocals(new ArrayList<>());
                        System.out.println("No locals loaded for commande_externe_id: " + commande.getId());
                    }
                    if (commande.getMagasinier() == null) {
                        System.err.println("Magasinier is null for commande_externe_id: " + commande.getId());
                    } else {
                        System.out.println("Magasinier loaded for commande_externe_id: " + commande.getId() + ": " + commande.getMagasinier().getNom());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading articles/locals for commande_externe_id " + commande.getId() + ": " + e.getMessage());
                    commande.setCommandeExterneArticles(new ArrayList<>());
                    commande.setCommandeExterneLocals(new ArrayList<>());
                }
            }
            commandeList.setAll(commandes);
            System.out.println("Nombre de commandes externes chargées: " + commandes.size());
            applyFilters();
            refreshNotifications();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des commandes externes: " + e.getMessage());
            e.printStackTrace();
            String errorMessage = "Erreur lors du chargement des commandes externes: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "\nCause: " + e.getCause().getMessage();
            }
            showAlert(Alert.AlertType.ERROR, "Erreur", errorMessage);
            commandeList.clear();
            applyFilters();
            tableCommandes.refresh();
        }
    }

    private void applyFilters() {
        String idFilter = filterId.getText() != null ? filterId.getText().trim() : "";
        String statutFilter = filterStatut.getValue();
        Magasinier magasinierFilter = filterMagasinier.getValue();
        Fournisseur fournisseurFilter = filterFournisseur.getValue();
        ObservableList<Local> localFilters = filterLocal.getCheckModel().getCheckedItems();
        ObservableList<Article> articleFilters = filterArticles.getCheckModel().getCheckedItems();
        java.time.LocalDate createdAtFilter = filterCreatedAt.getValue();

        ObservableList<CommandeExterne> filteredList = FXCollections.observableArrayList();
        for (CommandeExterne commande : commandeList) {
            if (commande == null) continue;
            boolean matches = true;

            if (!idFilter.isEmpty()) {
                try {
                    String idString = String.valueOf(commande.getId());
                    if (!idString.startsWith(idFilter)) {
                        matches = false;
                    }
                } catch (NumberFormatException e) {
                    matches = false;
                }
            }
            if (statutFilter != null && !commande.getStatut().equals(statutFilter)) {
                matches = false;
            }
            if (magasinierFilter != null && (commande.getMagasinier() == null || !commande.getMagasinier().getId().equals(magasinierFilter.getId()))) {
                matches = false;
            }
            if (fournisseurFilter != null && (commande.getFournisseur() == null || !commande.getFournisseur().getId().equals(fournisseurFilter.getId()))) {
                matches = false;
            }
            if (!localFilters.isEmpty()) {
                boolean allLocalsMatch = true;
                for (Local filterLocal : localFilters) {
                    boolean localMatch = false;
                    for (CommandeExterneLocal cel : commande.getCommandeExterneLocals()) {
                        if (cel.getLocal() != null && cel.getLocal().getId().equals(filterLocal.getId())) {
                            localMatch = true;
                            break;
                        }
                    }
                    if (!localMatch) {
                        allLocalsMatch = false;
                        break;
                    }
                }
                if (!allLocalsMatch) {
                    matches = false;
                }
            }
            if (!articleFilters.isEmpty()) {
                boolean allArticlesMatch = true;
                for (Article filterArticle : articleFilters) {
                    boolean articleMatch = false;
                    for (CommandeExterneArticle cea : commande.getCommandeExterneArticles()) {
                        if (cea.getArticle() != null && cea.getArticle().getId().equals(filterArticle.getId())) {
                            articleMatch = true;
                            break;
                        }
                    }
                    if (!articleMatch) {
                        allArticlesMatch = false;
                        break;
                    }
                }
                if (!allArticlesMatch) {
                    matches = false;
                }
            }
            if (createdAtFilter != null) {
                java.time.LocalDate commandeDate = commande.getCreerA() != null ?
                        commande.getCreerA().toLocalDate() : null;
                if (commandeDate == null || !commandeDate.equals(createdAtFilter)) {
                    matches = false;
                }
            }

            if (matches) {
                filteredList.add(commande);
            }
        }
        tableCommandes.setItems(filteredList);
        tableCommandes.refresh();
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm()) return;

        UserSession session = UserSession.getInstance();
        Magasinier loggedInMagasinier = magasinierList.stream()
                .filter(m -> m.getNom().equals(session.getFullName()))
                .findFirst()
                .orElse(null);

        if (loggedInMagasinier == null) {
            System.err.println("No magasinier found for UserSession fullName: " + session.getFullName());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun magasinier correspondant à l'utilisateur connecté.");
            return;
        }

        if (!isEditing) {
            CommandeExterne newCommande = createCommandeFromForm();
            newCommande.setMagasinier(loggedInMagasinier);
            try {
                commandeExterneDAO.insert(newCommande);
                System.out.println("Inserted commande with " + newCommande.getCommandeExterneLocals().size() + " locals and " + newCommande.getCommandeExterneArticles().size() + " articles");
                loadCommandes();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande externe ajoutée avec succès");
            } catch (Exception e) {
                System.err.println("Error inserting commande: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de la commande: " + e.getMessage());
            }
        } else {
            currentCommande.setStatut(formStatut.getValue());
            currentCommande.setMagasinier(loggedInMagasinier);
            currentCommande.setFournisseur(formFournisseur.getValue());
            List<CommandeExterneLocal> locals = new ArrayList<>();
            for (Local local : formLocal.getCheckModel().getCheckedItems()) {
                CommandeExterneLocal cel = new CommandeExterneLocal();
                cel.setLocal(local);
                cel.setCreatedAt(LocalDateTime.now());
                locals.add(cel);
            }
            currentCommande.setCommandeExterneLocals(locals);
            List<CommandeExterneArticle> articles = new ArrayList<>();
            for (Article article : formArticles.getCheckModel().getCheckedItems()) {
                TextInputDialog dialog = new TextInputDialog("1");
                dialog.setTitle("Quantité");
                dialog.setHeaderText("Entrez la quantité pour l'article: " + article.getNom());
                dialog.setContentText("Quantité:");
                Optional<String> result = dialog.showAndWait();
                int quantite = 1; // Default quantity
                if (result.isPresent()) {
                    try {
                        quantite = Integer.parseInt(result.get().trim());
                        if (quantite <= 0) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être positive pour l'article: " + article.getNom());
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité invalide pour l'article: " + article.getNom());
                        return;
                    }
                }
                CommandeExterneArticle cea = new CommandeExterneArticle();
                cea.setArticle(article);
                cea.setQuantite(quantite);
                cea.setCreatedAt(LocalDateTime.now());
                articles.add(cea);
                System.out.println("Updated article: " + article.getNom() + " (Quantité: " + quantite + ")");
            }
            currentCommande.setCommandeExterneArticles(articles);
            System.out.println("Updating commande ID " + currentCommande.getId() + " with " + locals.size() + " locals and " + articles.size() + " articles");
            if (formCreatedAt.getValue() != null) {
                currentCommande.setCreerA(LocalDateTime.of(formCreatedAt.getValue(), java.time.LocalTime.MIDNIGHT));
            } else {
                currentCommande.setCreerA(null);
            }
            try {
                commandeExterneDAO.update(currentCommande);
                loadCommandes();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande externe mise à jour avec succès");
            } catch (Exception e) {
                System.err.println("Error updating commande: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de la commande: " + e.getMessage());
            }
            isEditing = false;
            btnAjouter.setText("Ajouter");
        }
        clearForm();
    }

    private void handleModify(CommandeExterne commande) {
        currentCommande = commande;
        isEditing = true;
        formStatut.setValue(commande.getStatut());
        formFournisseur.setValue(commande.getFournisseur());
        formLocal.getCheckModel().clearChecks();
        List<CommandeExterneLocal> locals = commande.getCommandeExterneLocals();
        System.out.println("Loading " + (locals != null ? locals.size() : 0) + " locals for commande ID " + commande.getId());
        if (locals != null) {
            for (CommandeExterneLocal cel : locals) {
                Local local = cel.getLocal();
                if (local != null && filteredLocalList.contains(local)) {
                    formLocal.getCheckModel().check(local);
                    System.out.println("Checked local: " + local.getNom());
                }
            }
        }
        formArticles.getCheckModel().clearChecks();
        List<CommandeExterneArticle> articles = commande.getCommandeExterneArticles();
        System.out.println("Loading " + (articles != null ? articles.size() : 0) + " articles for commande ID " + commande.getId());
        if (articles != null) {
            for (CommandeExterneArticle cea : articles) {
                Article article = cea.getArticle();
                if (article != null && filteredArticleList.contains(article)) {
                    formArticles.getCheckModel().check(article);
                    System.out.println("Checked article: " + article.getNom() + " (Quantité: " + cea.getQuantite() + ")");
                }
            }
        }
        if (commande.getCreerA() != null) {
            formCreatedAt.setValue(commande.getCreerA().toLocalDate());
        } else {
            formCreatedAt.setValue(null);
        }
        btnAjouter.setText("Sauvegarder");
    }

    private void handleDelete(CommandeExterne commande) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la commande");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette commande externe ?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commandeExterneDAO.delete(commande.getId());
                loadCommandes();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande externe supprimée avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la commande: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleReset() {
        filterId.clear();
        filterStatut.setValue(null);
        filterMagasinier.setValue(null);
        filterFournisseur.setValue(null);
        filterLocal.getCheckModel().clearChecks();
        filterArticles.getCheckModel().clearChecks();
        filterCreatedAt.setValue(null);
        applyFilters();
        tableCommandes.refresh();
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
        if (formStatut.getValue() == null) {
            errorMessage.append("- Le statut est requis\n");
        }
        ObservableList<Local> selectedLocals = formLocal.getCheckModel().getCheckedItems();
        ObservableList<Article> selectedArticles = formArticles.getCheckModel().getCheckedItems();
        if (selectedLocals.isEmpty()) {
            errorMessage.append("- Au moins un local doit être sélectionné\n");
        }
        if (selectedArticles.isEmpty()) {
            errorMessage.append("- Au moins un article doit être sélectionné\n");
        }
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return false;
        }
        return true;
    }

    private CommandeExterne createCommandeFromForm() {
        CommandeExterne commande = new CommandeExterne();
        commande.setStatut(formStatut.getValue());
        commande.setFournisseur(formFournisseur.getValue());
        List<CommandeExterneLocal> locals = new ArrayList<>();
        ObservableList<Local> selectedLocals = formLocal.getCheckModel().getCheckedItems();
        System.out.println("Creating commande with " + selectedLocals.size() + " selected locals");
        for (Local local : selectedLocals) {
            CommandeExterneLocal cel = new CommandeExterneLocal();
            cel.setLocal(local);
            cel.setCreatedAt(LocalDateTime.now());
            locals.add(cel);
            System.out.println("Added local to commande: " + local.getNom());
        }
        commande.setCommandeExterneLocals(locals);
        List<CommandeExterneArticle> articles = new ArrayList<>();
        ObservableList<Article> selectedArticles = formArticles.getCheckModel().getCheckedItems();
        System.out.println("Creating commande with " + selectedArticles.size() + " selected articles");
        for (Article article : selectedArticles) {
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Quantité");
            dialog.setHeaderText("Entrez la quantité pour l'article: " + article.getNom());
            dialog.setContentText("Quantité:");
            Optional<String> result = dialog.showAndWait();
            int quantite = 1; // Default quantity
            if (result.isPresent()) {
                try {
                    quantite = Integer.parseInt(result.get().trim());
                    if (quantite <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être positive pour l'article: " + article.getNom());
                        return null; // Cancel command creation if invalid
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité invalide pour l'article: " + article.getNom());
                    return null; // Cancel command creation if invalid
                }
            }
            CommandeExterneArticle cea = new CommandeExterneArticle();
            cea.setArticle(article);
            cea.setQuantite(quantite);
            cea.setCreatedAt(LocalDateTime.now());
            articles.add(cea);
            System.out.println("Added article to commande: " + article.getNom() + " (Quantité: " + quantite + ")");
        }
        commande.setCommandeExterneArticles(articles);
        if (formCreatedAt.getValue() != null) {
            commande.setCreerA(LocalDateTime.of(formCreatedAt.getValue(), java.time.LocalTime.MIDNIGHT));
        }
        return commande;
    }

    private void clearForm() {
        formStatut.setValue(null);
        formFournisseur.setValue(null);
        formLocal.getCheckModel().clearChecks();
        formArticles.getCheckModel().clearChecks();
        filteredLocalList.setAll(localList);
        filteredArticleList.setAll(articleList);
        System.out.println("Cleared form, reset filteredLocalList to " + localList.size() + " locals, filteredArticleList to " + articleList.size() + " articles");
        formCreatedAt.setValue(null);
        isEditing = false;
        btnAjouter.setText("Ajouter");
        formLocal.getItems().setAll(filteredLocalList);
        formArticles.getItems().setAll(filteredArticleList);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}