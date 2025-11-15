package com.slginventory.ui;

import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardView {
    private final BorderPane root = new BorderPane();
    private final Stage stage;
    private final User user;
    private final StackPane contentArea = new StackPane();
    private ScrollPane scrollPane;
    private Sidebar sidebar;
    private DashboardOverviewPage overviewPage;
    private InventoryPage inventoryPage;
    private MapPane mapPane;
    private VendorsManagementPage vendorsPage;
    private DistributionCentersManagementPage centersPage;
    private AnalyticsPage analyticsPage;
    private ReportsPage reportsPage;
    private SettingsPage settingsPage;
    private TransportManagementPage transportPage;
    private boolean isDarkTheme = true;

    public DashboardView(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        build();
    }

    private void build() {
        // Root theming
        root.getStyleClass().add("dashboard-root");

        // Create sidebar with menu change handler
        sidebar = new Sidebar(this::handleMenuChange);
        root.setLeft(sidebar.getRoot());

        // Content area for pages with scrolling (will be toggled based on page)
        scrollPane = new ScrollPane();
        scrollPane.setContent(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false); // Allow vertical scrolling
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("content-scroll-pane");
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        contentArea.getStyleClass().add("content-area");
        contentArea.setMinHeight(Region.USE_PREF_SIZE);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        root.setCenter(scrollPane);

        // Create top bar
        root.setTop(createTopBar());

        // Initialize with dashboard overview
        showDashboardOverview();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("dashboard-header");
        topBar.setPadding(new Insets(12, 20, 12, 20));

        // Brand/Title
        Label brand = new Label("Jaaliya Foods - Dashboard");
        brand.getStyleClass().add("dashboard-title");

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("search-field");

        // Spacer
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Notification button
        Button notifications = new Button("🔔");
        notifications.getStyleClass().add("icon-button");
        notifications.setTooltip(new javafx.scene.control.Tooltip("Notifications"));

        // Theme toggle
        Button themeToggle = new Button("🌗");
        themeToggle.getStyleClass().add("icon-button");
        themeToggle.setTooltip(new javafx.scene.control.Tooltip("Toggle Theme"));
        themeToggle.setOnAction(e -> toggleTheme());

        // Profile button
        Button profile = new Button("👤");
        profile.getStyleClass().add("icon-button");
        profile.setTooltip(new javafx.scene.control.Tooltip("Profile"));

        // User info
        Label userInfo = new Label(user.getUsername() + " | " + user.getCompany());
        userInfo.getStyleClass().add("dashboard-userinfo");

        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setOnAction(e -> returnToLogin());

        topBar.getChildren().addAll(brand, spacer, searchField, notifications, themeToggle, profile, userInfo, logoutBtn);
        return topBar;
    }

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            root.getStyleClass().remove("dashboard-root-light");
            root.getStyleClass().add("dashboard-root");
        } else {
            root.getStyleClass().remove("dashboard-root");
            root.getStyleClass().add("dashboard-root-light");
        }
    }

    private void handleMenuChange(String menuId) {
        contentArea.getChildren().clear();
        
        // Enable scrolling by default for most pages
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToHeight(false);
        
        switch (menuId) {
            case "dashboard":
                showDashboardOverview();
                break;
            case "centers-inventory":
            case "vendor-stocks":
                showInventoryPage();
                break;
            case "add-item":
                showInventoryPage();
                // Automatically open the add item dialog after page is shown
                javafx.application.Platform.runLater(() -> {
                    if (inventoryPage != null) {
                        inventoryPage.showAddItemDialog();
                    }
                });
                break;
            case "distribution-map":
            case "path-finder":
                showDistributionMap();
                // Disable scrolling for map
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setFitToHeight(true);
                break;
            case "transport":
                showTransportManagement();
                // Enable scrolling for transport page
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setFitToHeight(false);
                break;
            case "vendors":
                showVendorsManagement();
                break;
            case "centers":
                showCentersManagement();
                break;
            case "analytics":
                showAnalytics();
                break;
            case "reports":
                showReports();
                break;
            case "settings":
                showSettings();
                break;
            case "help":
                showPlaceholderPage("Help", "❓ Help and documentation coming soon!");
                break;
            case "logout":
                returnToLogin();
                break;
            default:
                showDashboardOverview();
        }
    }

    private void showDashboardOverview() {
        if (overviewPage == null) {
            overviewPage = new DashboardOverviewPage(user);
        }
        VBox container = new VBox(overviewPage.getRoot());
        container.getStyleClass().add("page-container");
        VBox.setVgrow(overviewPage.getRoot(), Priority.ALWAYS);
        contentArea.getChildren().add(container);
    }

    private void showInventoryPage() {
        if (inventoryPage == null) {
            inventoryPage = new InventoryPage(user, stage);
        }
        inventoryPage.refresh();
        VBox container = new VBox(inventoryPage.getRoot());
        container.getStyleClass().add("page-container");
        VBox.setVgrow(inventoryPage.getRoot(), Priority.ALWAYS);
        contentArea.getChildren().add(container);
    }

    private void showDistributionMap() {
        if (mapPane == null) {
            mapPane = new MapPane();
        }
        VBox container = new VBox(mapPane.getRoot());
        container.getStyleClass().addAll("page-container", "panel-card");
        container.setPadding(new Insets(20));
        // Map has fixed size, so don't allow it to grow - prevents scrolling
        container.setAlignment(Pos.CENTER);
        // Ensure container doesn't exceed viewport
        container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        contentArea.getChildren().add(container);
    }

    private void showVendorsManagement() {
        if (vendorsPage == null) {
            vendorsPage = new VendorsManagementPage(stage, user);
        }
        vendorsPage.refresh();
        VBox container = new VBox(vendorsPage.getRoot());
        container.getStyleClass().add("page-container");
        VBox.setVgrow(vendorsPage.getRoot(), Priority.ALWAYS);
        contentArea.getChildren().add(container);
    }

    private void showCentersManagement() {
        if (centersPage == null) {
            centersPage = new DistributionCentersManagementPage(stage, user);
        }
        centersPage.refresh();
        VBox container = new VBox(centersPage.getRoot());
        container.getStyleClass().add("page-container");
        VBox.setVgrow(centersPage.getRoot(), Priority.ALWAYS);
        contentArea.getChildren().add(container);
    }

    private void showAnalytics() {
        if (analyticsPage == null) {
            analyticsPage = new AnalyticsPage(user);
        }
        VBox container = new VBox(analyticsPage.getRoot());
        container.getStyleClass().add("page-container");
        VBox.setVgrow(analyticsPage.getRoot(), Priority.ALWAYS);
        contentArea.getChildren().add(container);
    }

    private void showReports() {
        if (reportsPage == null) {
            reportsPage = new ReportsPage(stage, user);
        }
        VBox container = new VBox(reportsPage.getRoot());
        container.getStyleClass().add("page-container");
        VBox.setVgrow(reportsPage.getRoot(), Priority.ALWAYS);
        contentArea.getChildren().add(container);
    }

    private void showSettings() {
        if (settingsPage == null) {
            settingsPage = new SettingsPage(stage, user);
        }
        VBox container = new VBox(settingsPage.getRoot());
        container.getStyleClass().add("page-container");
        VBox.setVgrow(settingsPage.getRoot(), Priority.ALWAYS);
        contentArea.getChildren().add(container);
    }

    private void showTransportManagement() {
        if (transportPage == null) {
            transportPage = new TransportManagementPage(stage, user);
        }
        transportPage.refresh();
        VBox container = new VBox(transportPage.getRoot());
        container.getStyleClass().add("page-container");
        VBox.setVgrow(transportPage.getRoot(), Priority.ALWAYS);
        contentArea.getChildren().add(container);
    }

    private void showPlaceholderPage(String title, String message) {
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(50));
        placeholder.getStyleClass().add("placeholder-page");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("placeholder-message");
        messageLabel.setWrapText(true);
        
        placeholder.getChildren().addAll(titleLabel, messageLabel);
        
        VBox container = new VBox(placeholder);
        container.getStyleClass().add("page-container");
        container.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(container);
    }

    private void returnToLogin() {
        LoginView loginView = new LoginView(stage);
        Scene loginScene = new Scene(loginView.getRoot(), 1100, 720);
        loginScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(loginScene);
    }

    public BorderPane getRoot() {
        return root;
    }
}
