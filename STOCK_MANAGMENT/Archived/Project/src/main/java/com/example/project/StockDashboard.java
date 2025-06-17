package com.example.project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.util.Arrays;
import javafx.scene.control.ScrollPane;


public class StockDashboard extends Application {

    // Color constants (keeping as reference)
    private static final String CATSKILL_WHITE = "#f4f7fa";
    private static final String DODGER_BLUE = "#1f95fc";
    private static final String SPINDLE = "#a9c3e7";
    private static final String CLAM_SHELL = "#d5b8b5";

    @Override
    public void start(Stage primaryStage) {
        // Root layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Left sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Top navigation bar
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Main content
        VBox mainContent = createMainContent();
        root.setCenter(mainContent);

        // Create scene
        Scene scene = new Scene(root, 1200, 800);

        // Load CSS stylesheets
        scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Could not load stylesheet: " + e.getMessage());
        }

        primaryStage.setTitle("ISIMM - Gestion de Stock");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.centerOnScreen();
        root.requestFocus();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Logo
        Label logoLabel = new Label("📦 Mr.Stock");
        logoLabel.getStyleClass().add("logo-label");

        HBox logoContainer = new HBox(logoLabel);
        logoContainer.getStyleClass().add("logo-container");

        // Menu items with emoji icons - Modified to separate commandes into interne/externe
        String[] menuItems = {"Produits", "Locaux", "Fournisseurs", "Consommateurs", "Commandes internes", "Commandes externes", "Inventaire"};
        String[] menuIcons = {"📦", "🏢", "🚚", "👥", "📥", "📤", "📊"};

        VBox menuContainer = new VBox();
        menuContainer.setSpacing(10);

        for (int i = 0; i < menuItems.length; i++) {
            HBox menuItem = createMenuItem(menuItems[i], menuIcons[i], i == 0);
            menuContainer.getChildren().add(menuItem);
        }

        sidebar.getChildren().addAll(logoContainer, menuContainer);
        return sidebar;
    }

    private HBox createMenuItem(String text, String icon, boolean isActive) {
        HBox item = new HBox();
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

        // Icon (using Label for emoji)
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("menu-item-icon");

        // Text
        Label label = new Label(text);
        label.getStyleClass().add("menu-item-label");

        item.getChildren().addAll(iconLabel, label);

        // Add click handler to simulate navigation
        item.setOnMouseClicked(e -> {
            // This would be where you implement navigation logic
            System.out.println("Navigating to: " + text);
        });

        return item;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");

        // Greeting
        Label greeting = new Label("👋 Hello, ISIMM Store Manager!");
        greeting.getStyleClass().add("greeting-label");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User section with notification and profile
        HBox userSection = new HBox();
        userSection.getStyleClass().add("user-section");

        // Notification icon
        Label notifIcon = new Label("🔔");
        notifIcon.getStyleClass().add("notification-icon");
        StackPane notificationIcon = new StackPane();
        notificationIcon.getChildren().add(notifIcon);

        // Notification badge
        Label notifBadge = new Label("3");
        notifBadge.getStyleClass().add("notification-badge");

        notificationIcon.getChildren().add(notifBadge);

        // User profile
        HBox userProfile = new HBox();
        userProfile.getStyleClass().add("user-profile");

        Label profileIcon = new Label("👤");
        profileIcon.getStyleClass().add("profile-icon");

        Label username = new Label("Admin");
        username.getStyleClass().add("username-label");

        userProfile.getChildren().addAll(username, profileIcon);

        // Logout button
        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(e -> {
            try {
                // Return to login screen
                new LoginApp().start(new Stage());
                // Close current window
                ((Stage) topBar.getScene().getWindow()).close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        userSection.getChildren().addAll(notificationIcon, userProfile, logoutBtn);

        topBar.getChildren().addAll(greeting, spacer, userSection);

        return topBar;
    }

    private VBox createMainContent() {

        VBox content = new VBox();
        content.getStyleClass().add("main-content");

        // Dashboard header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Label dashboardTitle = new Label("Vue d'ensemble");
        dashboardTitle.getStyleClass().add("dashboard-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addProductBtn = new Button("Ajouter un produit");
        addProductBtn.getStyleClass().add("blue-button");

        Button transferBtn = new Button("Transfert d'inventaire");
        transferBtn.getStyleClass().add("white-button");

        header.getChildren().addAll(dashboardTitle, spacer, addProductBtn, transferBtn);

        // Categories boxes
        Label categoriesTitle = new Label("Catégories");
        categoriesTitle.getStyleClass().add("section-title");

        HBox categories = new HBox(15);
        categories.setAlignment(Pos.CENTER_LEFT);
        categories.getChildren().addAll(
                createCategoryBox("Articles", 300, "📦"),
                createCategoryBox("Commandes internes", 75, "📥"),
                createCategoryBox("Commandes externes", 75, "📤"),
                createCategoryBox("Fournisseurs", 50, "🚚"),
                createCategoryBox("Inventaire", 500, "📊")
        );

        // KPI cards
        HBox kpiCards = createKpiCards();

        // Recent activities
        Label recentTitle = new Label("Activités récentes");
        recentTitle.getStyleClass().add("section-title");

        VBox recentList = new VBox(10);
        recentList.getStyleClass().add("recent-list");
        recentList.getChildren().addAll(
                new Label("• Nouvelle commande interne reçue du Laboratoire de Chimie"),
                new Label("• Ajout de 20x Papier A4"),
                new Label("• Mise à jour du fournisseur: TechnoTools SARL"),
                new Label("• Commande externe #4532 expédiée")
        );

        // Charts and tables section
        HBox dataSection = new HBox();
        dataSection.setSpacing(20);

        VBox chartSection = createChartSection();
        VBox tableSection = createTableSection();

        HBox.setHgrow(chartSection, Priority.ALWAYS);
        HBox.setHgrow(tableSection, Priority.ALWAYS);

        dataSection.getChildren().addAll(chartSection, tableSection);

        content.getChildren().addAll(header, categoriesTitle, categories, kpiCards, recentTitle, recentList, dataSection);

        // Create ScrollPane and add content to it
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("main-content-scroll");

        // Improve scrolling speed and feel
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide horizontal scrollbar
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // These properties make scrolling smoother and faster
        scrollPane.setPannable(true); // Allows dragging the content with mouse
        scrollPane.setVvalue(0); // Start at the top

        // Add scroll speed enhancement
        scrollPane.getContent().setOnScroll(event -> {
            double deltaY = event.getDeltaY() * 2; // Multiply by 2 for faster scrolling
            double width = scrollPane.getContent().getBoundsInLocal().getWidth();
            double vvalue = scrollPane.getVvalue();

            // Adjust scrolling speed
            scrollPane.setVvalue(vvalue - deltaY / width);

            event.consume();
        });

        // Create a container for the ScrollPane
        VBox contentContainer = new VBox();
        contentContainer.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return contentContainer;
    }

    private VBox createCategoryBox(String name, int count, String icon) {
        VBox box = new VBox(5);
        box.getStyleClass().add("category-box");
        box.setPrefSize(160, 100);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("category-icon");

        Label title = new Label(name);
        title.getStyleClass().add("category-title");

        Label subtitle = new Label(count + " articles");
        subtitle.getStyleClass().add("category-subtitle");

        box.getChildren().addAll(iconLabel, title, subtitle);
        return box;
    }

    private HBox createKpiCards() {
        HBox cards = new HBox();
        cards.setSpacing(20);

        // Total sales card
        VBox totalSalesCard = createKpiCard(
                "Ventes totales",
                "325,000.00 DT",
                "-45% vs semaine dernière",
                "kpi-value-blue"
        );

        // Units sold card
        VBox unitsSoldCard = createKpiCard(
                "Unités vendues",
                "6,098",
                "-9% vs semaine dernière",
                "kpi-value-spindle"
        );

        // Out of stock card
        VBox outOfStockCard = createKpiCard(
                "Rupture de stock",
                "300",
                "",
                "kpi-value-clam"
        );

        // Set equal widths
        HBox.setHgrow(totalSalesCard, Priority.ALWAYS);
        HBox.setHgrow(unitsSoldCard, Priority.ALWAYS);
        HBox.setHgrow(outOfStockCard, Priority.ALWAYS);

        cards.getChildren().addAll(totalSalesCard, unitsSoldCard, outOfStockCard);
        return cards;
    }

    private VBox createKpiCard(String title, String value, String change, String valueStyleClass) {
        VBox card = new VBox();
        card.getStyleClass().add("kpi-card");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("kpi-title");

        // Value
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll("kpi-value", valueStyleClass);

        // Change percentage
        Label changeLabel = new Label(change);
        changeLabel.getStyleClass().add("kpi-change");

        card.getChildren().addAll(titleLabel, valueLabel);

        if (!change.isEmpty()) {
            card.getChildren().add(changeLabel);
        }

        return card;
    }

    private VBox createChartSection() {
        VBox section = new VBox();
        section.getStyleClass().add("data-section");
        section.setSpacing(15);

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("section-header");

        Label title = new Label("Ventes");
        title.getStyleClass().add("section-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> periodSelector = new ComboBox<>();
        periodSelector.getItems().addAll("7 derniers jours", "30 derniers jours", "90 derniers jours");
        periodSelector.setValue("7 derniers jours");

        header.getChildren().addAll(title, spacer, periodSelector);

        // Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Jour");
        yAxis.setLabel("Ventes");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Lun", 800));
        series.getData().add(new XYChart.Data<>("Mar", 950));
        series.getData().add(new XYChart.Data<>("Mer", 650));
        series.getData().add(new XYChart.Data<>("Jeu", 830));
        series.getData().add(new XYChart.Data<>("Ven", 830));
        series.getData().add(new XYChart.Data<>("Sam", 820));
        series.getData().add(new XYChart.Data<>("Dim", 830));

        barChart.getData().add(series);

        section.getChildren().addAll(header, barChart);
        return section;
    }

    private VBox createTableSection() {
        VBox section = new VBox();
        section.getStyleClass().add("data-section");
        section.setSpacing(15);

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("section-header");

        Label title = new Label("Rupture de stock");
        title.getStyleClass().add("section-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label viewAll = new Label("Voir tout >");
        viewAll.getStyleClass().add("view-all");

        header.getChildren().addAll(title, spacer, viewAll);

        // Table
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, String> nameCol = new TableColumn<>("Nom du produit");
        TableColumn<Product, String> skuCol = new TableColumn<>("SKU");
        TableColumn<Product, String> priceCol = new TableColumn<>("Prix");
        TableColumn<Product, String> actionsCol = new TableColumn<>("");

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        skuCol.setCellValueFactory(cellData -> cellData.getValue().skuProperty());
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty());

        table.getColumns().addAll(nameCol, skuCol, priceCol, actionsCol);

        // Add sample data
        table.getItems().add(new Product("Ravel chips", "Ravel-45678-S-CH", "40.00 DT"));
        table.getItems().add(new Product("HP 840 G3", "Ravel-45678-S-CH", "783.00 DT"));
        table.getItems().add(new Product("HP 840 G3", "Ravel-45678-S-CH", "783.00 DT"));
        table.getItems().add(new Product("HP 840 G3", "Ravel-45678-S-CH", "783.00 DT"));

        section.getChildren().addAll(header, table);
        return section;
    }

    // Product class for table
    public static class Product {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty sku;
        private final javafx.beans.property.SimpleStringProperty price;

        public Product(String name, String sku, String price) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.sku = new javafx.beans.property.SimpleStringProperty(sku);
            this.price = new javafx.beans.property.SimpleStringProperty(price);
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public javafx.beans.property.StringProperty skuProperty() {
            return sku;
        }

        public javafx.beans.property.StringProperty priceProperty() {
            return price;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}