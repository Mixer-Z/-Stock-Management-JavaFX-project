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
import model.dao.ArticleDAO;
import model.dao.ArticleDAOImpl;
import model.entities.Article;
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
 * Controller for the article management interface.
 * Handles dynamic filters, range sliders, table interactions, form submissions, and navigation.
 */
public class ArticleController implements Initializable {

    private final ArticleDAO articleDAO = new ArticleDAOImpl();
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Filter section
    @FXML private TextField filterId;
    @FXML private TextField filterReference;
    @FXML private TextField filterNom;
    @FXML private ComboBox<String> filterCategorie;
    @FXML private Slider filterStockMinMin;
    @FXML private Slider filterStockMinMax;
    @FXML private Label stockMinMinLabel;
    @FXML private Label stockMinMaxLabel;
    @FXML private DatePicker filterDatePeremption;
    @FXML private CheckBox filterEstCritique;
    @FXML private CheckBox filterEstConsommable;
    @FXML private Button btnReset;

    // Table section
    @FXML private TableView<Article> tableArticles;
    @FXML private TableColumn<Article, Long> colId;
    @FXML private TableColumn<Article, String> colReference;
    @FXML private TableColumn<Article, String> colNom;
    @FXML private TableColumn<Article, String> colCategorie;
    @FXML private TableColumn<Article, Integer> colStockMinimal;
    @FXML private TableColumn<Article, String> colDatePeremption;
    @FXML private TableColumn<Article, Boolean> colEstCritique;
    @FXML private TableColumn<Article, Boolean> colEstConsommable;
    @FXML private TableColumn<Article, Void> colActions;

    // Form section
    @FXML private TextField formReference;
    @FXML private TextField formNom;
    @FXML private ComboBox<String> formCategorie;
    @FXML private Spinner<Integer> formStockMinimal;
    @FXML private DatePicker formDatePeremption;
    @FXML private CheckBox formEstCritique;
    @FXML private CheckBox formEstConsommable;
    @FXML private Button btnAjouter;
    @FXML private Button btnResetForm;

    // Navigation
    @FXML private VBox menuContainer;
    @FXML private Button logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    private ObservableList<Article> articleList = FXCollections.observableArrayList();
    private ObservableList<String> expiringArticles = FXCollections.observableArrayList();
    private static final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean isEditing = false;
    private Article currentArticle;

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
        loadArticles();
    }

    /**
     * Retrieves the logged-in magasinier's full name from UserSession.
     * Returns a default name if the session is not initialized or user data is missing.
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
     * Queries the database for articles with date_peremption within 7 days.
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
            VBox menuItem = createMenuItem(MENU_ITEMS[i], MENU_ICONS[i], i == 1); // Produits is active
            menuContainer.getChildren().add(menuItem);
        }
    }

    /**
     * Creates a vertical menu item with icon above label.
     */
    private VBox createMenuItem(String text, String icon, boolean isActive) {
        VBox item = new VBox(5); // Use VBox for vertical layout
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
        label.setWrapText(true); // Allow text to wrap if needed

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
     * Configures filter section components, including dynamic range sliders and listeners.
     */
    private void setupFilters() {
        // Categories for filter
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Matériel médical", "Médicament", "Fourniture", "Équipement", "Autre"
        );
        filterCategorie.setItems(categories);

        // Initialize sliders (ranges set in updateSliderRanges)
        filterStockMinMin.setShowTickLabels(true);
        filterStockMinMin.setShowTickMarks(true);
        filterStockMinMax.setShowTickLabels(true);
        filterStockMinMax.setShowTickMarks(true);

        // Bind labels to slider values
        stockMinMinLabel.textProperty().bind(Bindings.format("%.0f", filterStockMinMin.valueProperty()));
        stockMinMaxLabel.textProperty().bind(Bindings.format("%.0f", filterStockMinMax.valueProperty()));

        // Adjust max slider if min exceeds it
        filterStockMinMin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > filterStockMinMax.getValue()) {
                filterStockMinMax.setValue(newVal.doubleValue());
            }
            applyFilters();
        });

        filterStockMinMax.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < filterStockMinMin.getValue()) {
                filterStockMinMin.setValue(newVal.doubleValue());
            }
            applyFilters();
        });

        // Default checkbox states
        filterEstCritique.setSelected(false);
        filterEstConsommable.setSelected(false);

        // Add dynamic filter listeners
        filterId.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterReference.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterNom.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterCategorie.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterDatePeremption.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterEstCritique.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterEstConsommable.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Configures the table columns and action buttons.
     */
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colStockMinimal.setCellValueFactory(new PropertyValueFactory<>("stockMinimal"));

        colDatePeremption.setCellValueFactory(cellData -> {
            LocalDateTime datePeremption = cellData.getValue().getDatePeremption();
            if (datePeremption != null) {
                return new SimpleStringProperty(datePeremption.format(displayFormatter));
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        colEstCritique.setCellValueFactory(new PropertyValueFactory<>("estCritique"));
        colEstConsommable.setCellValueFactory(new PropertyValueFactory<>("estConsommable"));

        // Boolean cell factories
        colEstCritique.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item ? "Oui" : "Non"));
            }
        });

        colEstConsommable.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item ? "Oui" : "Non"));
            }
        });

        // Action column with Modify and Delete buttons stacked vertically
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
                    Article article = getTableRow().getItem();
                    modifyButton.setOnAction(e -> handleModify(article));
                    deleteButton.setOnAction(e -> handleDelete(article));
                    setGraphic(buttons);
                }
            }
        });

        tableArticles.setItems(articleList);
    }

    /**
     * Configures the form section for adding/editing articles.
     */
    private void setupForm() {
        // Categories for form
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Matériel médical", "Médicament", "Fourniture", "Équipement", "Autre"
        );
        formCategorie.setItems(categories);

        // Stock minimal spinner
        SpinnerValueFactory<Integer> stockFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 5);
        formStockMinimal.setValueFactory(stockFactory);
        formStockMinimal.setEditable(true);

        // Default checkbox states
        formEstCritique.setSelected(false);
        formEstConsommable.setSelected(false);
    }

    /**
     * Loads articles from the database and updates slider ranges.
     */
    private void loadArticles() {
        try {
            List<Article> articles = articleDAO.getAll();
            articleList.setAll(articles);
            System.out.println("Nombre d'articles chargés: " + articles.size());
            updateSliderRanges();
            applyFilters();
            refreshNotifications();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des articles: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des articles: " + e.getMessage());
            articleList.clear();
        }
    }

    /**
     * Updates slider ranges based on article data.
     */
    private void updateSliderRanges() {
        if (articleList.isEmpty()) {
            filterStockMinMin.setMin(0);
            filterStockMinMax.setMax(500);
            return;
        }

        // Calculate min/max for stock minimal
        int minStock = articleList.stream().mapToInt(Article::getStockMinimal).min().orElse(0);
        int maxStock = articleList.stream().mapToInt(Article::getStockMinimal).max().orElse(500);
        maxStock = Math.max(maxStock, minStock + 1);

        filterStockMinMin.setMin(minStock);
        filterStockMinMin.setMax(maxStock);
        filterStockMinMin.setValue(minStock);
        filterStockMinMax.setMin(minStock);
        filterStockMinMax.setMax(maxStock);
        filterStockMinMax.setValue(maxStock);
    }

    /**
     * Applies filters to the table based on all filter inputs.
     */
    private void applyFilters() {
        String idFilter = filterId.getText().trim();
        String refFilter = filterReference.getText().trim().toLowerCase();
        String nomFilter = filterNom.getText().trim().toLowerCase();
        String catFilter = filterCategorie.getValue();
        double stockMin = filterStockMinMin.getValue();
        double stockMax = filterStockMinMax.getValue();
        java.time.LocalDate dateFilter = filterDatePeremption.getValue();
        boolean filterCritique = filterEstCritique.isSelected();
        boolean filterConsommable = filterEstConsommable.isSelected();

        ObservableList<Article> filteredList = FXCollections.observableArrayList();
        for (Article article : articleList) {
            boolean matches = true;

            if (!idFilter.isEmpty()) {
                try {
                    String idString = String.valueOf(article.getId());
                    if (!idString.startsWith(idFilter)) {
                        matches = false;
                    }
                } catch (NumberFormatException e) {
                    matches = false; // Invalid ID format
                }
            }
            if (!refFilter.isEmpty() && !article.getReference().toLowerCase().startsWith(refFilter)) {
                matches = false;
            }
            if (!nomFilter.isEmpty() && !article.getNom().toLowerCase().startsWith(nomFilter)) {
                matches = false;
            }
            if (catFilter != null && !article.getCategorie().equals(catFilter)) {
                matches = false;
            }
            if (article.getStockMinimal() < stockMin || article.getStockMinimal() > stockMax) {
                matches = false;
            }
            if (dateFilter != null) {
                java.time.LocalDate articleDate = article.getDatePeremption() != null ?
                        article.getDatePeremption().toLocalDate() : null;
                if (articleDate == null || !articleDate.equals(dateFilter)) {
                    matches = false;
                }
            }
            if (filterCritique && !article.isEstCritique()) {
                matches = false;
            }
            if (filterConsommable && !article.isEstConsommable()) {
                matches = false;
            }

            if (matches) {
                filteredList.add(article);
            }
        }
        tableArticles.setItems(filteredList);
        tableArticles.refresh(); // Force refresh to ensure buttons render
    }

    /**
     * Handles form submission to add or update an article.
     */
    @FXML
    private void handleAjouter() {
        if (!validateForm()) return;

        if (!isEditing) {
            // Add new article
            Article newArticle = createArticleFromForm();
            try {
                articleDAO.insert(newArticle);
                // Reload articles to ensure database consistency
                loadArticles();
                System.out.println("Article added: " + newArticle.getNom());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Article ajouté avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de l'article: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Update existing article
            currentArticle.setReference(formReference.getText());
            currentArticle.setNom(formNom.getText());
            currentArticle.setCategorie(formCategorie.getValue());
            currentArticle.setStockMinimal(formStockMinimal.getValue());
            if (formDatePeremption.getValue() != null) {
                currentArticle.setDatePeremption(
                        LocalDateTime.of(formDatePeremption.getValue(), java.time.LocalTime.MIDNIGHT));
            } else {
                currentArticle.setDatePeremption(null);
            }
            currentArticle.setEstCritique(formEstCritique.isSelected());
            currentArticle.setEstConsommable(formEstConsommable.isSelected());
            try {
                articleDAO.update(currentArticle);
                loadArticles();
                System.out.println("Article updated: " + currentArticle.getNom());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Article mis à jour avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de l'article: " + e.getMessage());
                e.printStackTrace();
            }
            isEditing = false;
            btnAjouter.setText("Ajouter");
        }
        clearForm();
    }

    /**
     * Populates form with article data for editing.
     */
    private void handleModify(Article article) {
        currentArticle = article;
        isEditing = true;
        formReference.setText(article.getReference());
        formNom.setText(article.getNom());
        formCategorie.setValue(article.getCategorie());
        formStockMinimal.getValueFactory().setValue(article.getStockMinimal());
        if (article.getDatePeremption() != null) {
            formDatePeremption.setValue(article.getDatePeremption().toLocalDate());
        } else {
            formDatePeremption.setValue(null);
        }
        formEstCritique.setSelected(article.isEstCritique());
        formEstConsommable.setSelected(article.isEstConsommable());
        btnAjouter.setText("Sauvegarder");
    }

    /**
     * Deletes an article after confirmation.
     */
    private void handleDelete(Article article) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'article");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet article ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                articleDAO.delete(article.getId());
                loadArticles();
                System.out.println("Article deleted: " + article.getNom());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Article supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'article: " + e.getMessage());
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
        filterReference.clear();
        filterNom.clear();
        filterCategorie.setValue(null);
        filterDatePeremption.setValue(null);
        filterEstCritique.setSelected(false);
        filterEstConsommable.setSelected(false);

        // Reset sliders to full range
        updateSliderRanges();
        applyFilters();
        tableArticles.refresh(); // Force refresh to ensure buttons render
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
            UserSession.getInstance().clear(); // Clear the session on logout
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

        if (formReference.getText().trim().isEmpty()) {
            errorMessage.append("- La référence est requise\n");
        }
        if (formNom.getText().trim().isEmpty()) {
            errorMessage.append("- Le nom est requis\n");
        }
        if (formCategorie.getValue() == null) {
            errorMessage.append("- La catégorie est requise\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return false;
        }
        return true;
    }

    /**
     * Creates an Article object from form inputs.
     */
    private Article createArticleFromForm() {
        String reference = formReference.getText().trim();
        String nom = formNom.getText().trim();
        String categorie = formCategorie.getValue();
        int stockMinimal = formStockMinimal.getValue();
        boolean estCritique = formEstCritique.isSelected();
        boolean estConsommable = formEstConsommable.isSelected();

        Article article = new Article(reference, nom, categorie, stockMinimal, estCritique, estConsommable);
        if (formDatePeremption.getValue() != null) {
            article.setDatePeremption(
                    LocalDateTime.of(formDatePeremption.getValue(), java.time.LocalTime.MIDNIGHT));
        }
        return article;
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        formReference.clear();
        formNom.clear();
        formCategorie.setValue(null);
        formStockMinimal.getValueFactory().setValue(5);
        formDatePeremption.setValue(null);
        formEstCritique.setSelected(false);
        formEstConsommable.setSelected(false);
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