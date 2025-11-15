package com.slginventory.ui;

import com.slginventory.db.Database;
import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardOverviewPage {
    private final VBox root = new VBox();
    private final User user;

    public DashboardOverviewPage(User user) {
        this.user = user;
        build();
    }

    private void build() {
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("dashboard-overview-page");

        // Title
        Label title = new Label("Dashboard Overview");
        title.getStyleClass().add("page-title");
        root.getChildren().add(title);

        // Summary Cards Row
        HBox summaryRow = new HBox(20);
        summaryRow.setAlignment(Pos.CENTER);
        summaryRow.getChildren().addAll(
            createSummaryCard("📦", "Total Stock in Centers", getTotalStockInCenters(), "kg", "#4CAF50"),
            createSummaryCard("📊", "Your Vendor Surplus", getVendorSurplus(), "kg", "#2196F3"),
            createSummaryCard("🔥", "Most Demanded Product", getMostDemandedProduct(), "", "#FF9800")
        );
        root.getChildren().add(summaryRow);

        // Information Box
        VBox infoBox = createInfoBox();
        root.getChildren().add(infoBox);
    }

    private Node createSummaryCard(String icon, String title, String value, String unit, String color) {
        VBox card = new VBox(10);
        card.getStyleClass().add("summary-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setPrefWidth(280);
        card.setPrefHeight(180);
        
        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("summary-card-title");
        
        // Value
        HBox valueBox = new HBox(5);
        valueBox.setAlignment(Pos.CENTER);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("summary-card-value");
        if (!unit.isEmpty()) {
            Label unitLabel = new Label(unit);
            unitLabel.getStyleClass().add("summary-card-unit");
            valueBox.getChildren().addAll(valueLabel, unitLabel);
        } else {
            valueBox.getChildren().add(valueLabel);
        }
        
        card.getChildren().addAll(iconLabel, titleLabel, valueBox);
        
        // Set card color accent
        card.setStyle(card.getStyle() + String.format(
            "-fx-border-color: %s; -fx-border-width: 0 0 4 0;", color));
        
        return card;
    }

    private VBox createInfoBox() {
        VBox infoBox = new VBox(15);
        infoBox.getStyleClass().add("info-box");
        infoBox.setPadding(new Insets(20));
        infoBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        Label infoTitle = new Label("Quick Information");
        infoTitle.getStyleClass().add("info-box-title");
        
        Label infoText = new Label(
            "• Total stock in centers: " + getTotalStockInCenters() + " kg\n" +
            "• Your vendor surplus: " + getVendorSurplus() + " kg\n" +
            "• Most demanded product: " + getMostDemandedProduct()
        );
        infoText.getStyleClass().add("info-box-text");
        infoText.setWrapText(true);
        
        infoBox.getChildren().addAll(infoTitle, infoText);
        return infoBox;
    }

    private String getTotalStockInCenters() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT SUM(quantity) AS total
                 FROM center_inventory
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    return formatNumber(total);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String getVendorSurplus() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT SUM(vi.quantity) AS total
                 FROM vendor_inventory vi
                 JOIN vendors v ON v.id = vi.vendor_id
                 JOIN users u ON u.id = v.user_id
                 WHERE u.id = ?
             """)) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    return formatNumber(total);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String getMostDemandedProduct() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT g.name, SUM(ci.quantity) AS total
                 FROM center_inventory ci
                 JOIN goods g ON g.id = ci.goods_id
                 GROUP BY g.id, g.name
                 ORDER BY total DESC
                 LIMIT 1
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }

    public Node getRoot() {
        return root;
    }
}

