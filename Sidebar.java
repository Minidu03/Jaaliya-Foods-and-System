package com.slginventory.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Sidebar {
    private final VBox root = new VBox();
    private final Map<String, Button> menuButtons = new HashMap<>();
    private String activeMenu = "dashboard";
    private Consumer<String> onMenuChange;

    public Sidebar(Consumer<String> onMenuChange) {
        this.onMenuChange = onMenuChange;
        build();
    }

    private void build() {
        root.getStyleClass().add("modern-sidebar");
        root.setPrefWidth(250);
        root.setMinWidth(250);
        root.setSpacing(4);
        root.setPadding(new Insets(20, 0, 20, 0));

        // Brand/Logo area
        Button brandBtn = createMenuButton("🏠 Dashboard Overview", "dashboard");
        brandBtn.getStyleClass().add("sidebar-brand");
        root.getChildren().add(brandBtn);

        // Section: Dashboard Overview
        root.getChildren().add(createSectionLabel("DASHBOARD"));

        // Section: Inventory Management
        root.getChildren().add(createSectionLabel("INVENTORY"));
        root.getChildren().add(createMenuButton("🏬 Centers Inventory", "centers-inventory"));
        root.getChildren().add(createMenuButton("📦 Vendor Stocks (Your Surplus)", "vendor-stocks"));
        root.getChildren().add(createMenuButton("➕ Add New Item", "add-item"));

        // Section: Distribution & Logistics
        root.getChildren().add(createSectionLabel("DISTRIBUTION"));
        root.getChildren().add(createMenuButton("🗺️ Distribution Map", "distribution-map"));
        root.getChildren().add(createMenuButton("📍 Shortest Path Finder", "path-finder"));
        root.getChildren().add(createMenuButton("🚚 Transport Management", "transport"));

        // Section: Vendors & Centers
        root.getChildren().add(createSectionLabel("MANAGEMENT"));
        root.getChildren().add(createMenuButton("👤 Vendors", "vendors"));
        root.getChildren().add(createMenuButton("🏢 Distribution Centers", "centers"));

        // Section: Reports & Analytics
        root.getChildren().add(createSectionLabel("ANALYTICS"));
        root.getChildren().add(createMenuButton("📊 Sales / Supply Analytics", "analytics"));
        root.getChildren().add(createMenuButton("📄 Export Reports", "reports"));

        // Section: Settings
        root.getChildren().add(createSectionLabel("SETTINGS"));
        root.getChildren().add(createMenuButton("⚙️ User Account", "settings"));
        root.getChildren().add(createMenuButton("❓ Help / Documentation", "help"));

        // Logout button (separated)
        VBox spacer = new VBox();
        spacer.setPrefHeight(20);
        root.getChildren().add(spacer);
        
        Button logoutBtn = createMenuButton("🔒 Logout", "logout");
        logoutBtn.getStyleClass().add("sidebar-logout");
        root.getChildren().add(logoutBtn);

        // Set initial active state
        setActiveMenu("dashboard");
    }

    private Button createMenuButton(String text, String menuId) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-menu-button");
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setPadding(new Insets(10, 20, 10, 20));
        
        menuButtons.put(menuId, btn);
        
        btn.setOnAction(e -> {
            setActiveMenu(menuId);
            if (onMenuChange != null) {
                onMenuChange.accept(menuId);
            }
        });
        
        return btn;
    }

    private Node createSectionLabel(String text) {
        javafx.scene.control.Label label = new javafx.scene.control.Label(text);
        label.getStyleClass().add("sidebar-section-label");
        label.setPadding(new Insets(15, 20, 8, 20));
        label.setFont(Font.font("System", FontWeight.BOLD, 10));
        return label;
    }

    public void setActiveMenu(String menuId) {
        // Remove active state from all buttons
        menuButtons.values().forEach(btn -> {
            btn.getStyleClass().remove("sidebar-menu-button-active");
        });
        
        // Add active state to selected button
        Button activeBtn = menuButtons.get(menuId);
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("sidebar-menu-button-active");
        }
        
        this.activeMenu = menuId;
    }

    public Node getRoot() {
        return root;
    }

    public String getActiveMenu() {
        return activeMenu;
    }
}

