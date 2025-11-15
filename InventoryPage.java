package com.slginventory.ui;

import com.slginventory.algorithms.Sorting;
import com.slginventory.model.InventoryItem;
import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class InventoryPage {
    private final VBox root = new VBox();
    private final User user;
    private final LeftPane leftPane;
    private final Stage stage;

    public InventoryPage(User user, Stage stage) {
        this.user = user;
        this.stage = stage;
        this.leftPane = new LeftPane();
        build();
    }

    public void refresh() {
        List<InventoryItem> centerInventory = leftPane.loadCenterInventory();
        List<InventoryItem> vendorInventory = leftPane.loadVendorInventory(user.getId());
        
        List<InventoryItem> sortedCenters = Sorting.sortByQuantityDesc(centerInventory);
        List<InventoryItem> sortedVendors = Sorting.sortByQuantityDesc(vendorInventory);
        
        displayInventories(sortedCenters, sortedVendors);
    }

    private void build() {
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("inventory-page");

        // Title and Add Button Row
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Inventory Management");
        title.getStyleClass().add("page-title");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addItemBtn = new Button("➕ Add New Item");
        addItemBtn.getStyleClass().add("button");
        addItemBtn.setOnAction(e -> showAddItemDialog());
        
        titleRow.getChildren().addAll(title, spacer, addItemBtn);
        root.getChildren().add(titleRow);

        refresh();
    }
    
    public void showAddItemDialog() {
        AddItemDialog dialog = new AddItemDialog(stage, user, this::refresh);
        dialog.show();
    }

    private void displayInventories(List<InventoryItem> centers, List<InventoryItem> vendors) {
        // Clear existing content except title
        if (root.getChildren().size() > 1) {
            root.getChildren().remove(1, root.getChildren().size());
        }

        // Centers Inventory Section
        Label centersTitle = new Label("🏬 Centers Inventory");
        centersTitle.getStyleClass().add("section-title");
        root.getChildren().add(centersTitle);

        FlowPane centersGrid = new FlowPane();
        centersGrid.setHgap(15);
        centersGrid.setVgap(15);
        centersGrid.setPadding(new Insets(10, 0, 20, 0));
        
        int maxCenterQty = centers.isEmpty() ? 1 : centers.stream()
            .mapToInt(InventoryItem::getQuantity)
            .max()
            .orElse(1);

        for (InventoryItem item : centers) {
            centersGrid.getChildren().add(createInventoryCard(item, maxCenterQty, true));
        }
        root.getChildren().add(centersGrid);

        // Vendor Inventory Section
        Label vendorsTitle = new Label("📦 Vendor Stocks (Your Surplus)");
        vendorsTitle.getStyleClass().add("section-title");
        root.getChildren().add(vendorsTitle);

        FlowPane vendorsGrid = new FlowPane();
        vendorsGrid.setHgap(15);
        vendorsGrid.setVgap(15);
        vendorsGrid.setPadding(new Insets(10, 0, 20, 0));
        
        int maxVendorQty = vendors.isEmpty() ? 1 : vendors.stream()
            .mapToInt(InventoryItem::getQuantity)
            .max()
            .orElse(1);

        for (InventoryItem item : vendors) {
            vendorsGrid.getChildren().add(createInventoryCard(item, maxVendorQty, false));
        }
        root.getChildren().add(vendorsGrid);
    }

    private Node createInventoryCard(InventoryItem item, int maxQuantity, boolean isCenter) {
        VBox card = new VBox(12);
        card.getStyleClass().add("inventory-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setPrefHeight(180);
        
        // Icon based on item type
        String icon = getIconForItem(item.getGoods().getName());
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 36px;");
        
        // Item name
        Label nameLabel = new Label(item.getGoods().getName());
        nameLabel.getStyleClass().add("inventory-card-name");
        nameLabel.setWrapText(true);
        
        // Quantity
        Label qtyLabel = new Label(formatQuantity(item.getQuantity()) + " kg");
        qtyLabel.getStyleClass().add("inventory-card-quantity");
        
        // Progress bar
        double progress = Math.min(1.0, (double) item.getQuantity() / maxQuantity);
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.getStyleClass().add("inventory-progress-bar");
        progressBar.setPrefWidth(180);
        progressBar.setPrefHeight(8);
        
        // Capacity indicator
        int capacityPercent = (int) (progress * 100);
        Label capacityLabel = new Label(capacityPercent + "% capacity");
        capacityLabel.getStyleClass().add("inventory-card-capacity");
        
        // Color code based on quantity
        String colorClass = getQuantityColorClass(item.getQuantity());
        card.getStyleClass().add(colorClass);
        
        card.getChildren().addAll(iconLabel, nameLabel, qtyLabel, progressBar, capacityLabel);
        return card;
    }

    private String getIconForItem(String itemName) {
        String lower = itemName.toLowerCase();
        if (lower.contains("rice")) return "🌾";
        if (lower.contains("onion")) return "🧅";
        if (lower.contains("potato")) return "🥔";
        if (lower.contains("tomato")) return "🍅";
        if (lower.contains("carrot")) return "🥕";
        if (lower.contains("cabbage")) return "🥬";
        if (lower.contains("pumpkin")) return "🎃";
        if (lower.contains("brinjal") || lower.contains("eggplant")) return "🍆";
        if (lower.contains("chili")) return "🌶️";
        if (lower.contains("lentil")) return "🫘";
        if (lower.contains("sugar")) return "🍬";
        if (lower.contains("wheat") || lower.contains("flour")) return "🌾";
        if (lower.contains("coconut") || lower.contains("oil")) return "🥥";
        if (lower.contains("tea")) return "🍵";
        if (lower.contains("spice")) return "🧂";
        return "📦";
    }

    private String getQuantityColorClass(int quantity) {
        if (quantity >= 10000) return "inventory-card-high";
        if (quantity >= 1000) return "inventory-card-medium";
        return "inventory-card-low";
    }

    private String formatQuantity(int qty) {
        if (qty >= 1000000) {
            return String.format("%.1fM", qty / 1000000.0);
        } else if (qty >= 1000) {
            return String.format("%.1fK", qty / 1000.0);
        }
        return String.valueOf(qty);
    }

    public Node getRoot() {
        return root;
    }
}

