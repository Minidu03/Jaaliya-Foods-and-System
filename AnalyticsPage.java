package com.slginventory.ui;

import com.slginventory.db.Database;
import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnalyticsPage {
    private final VBox root = new VBox();
    private final User user;

    public AnalyticsPage(User user) {
        this.user = user;
        build();
    }

    private void build() {
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("analytics-page");

        Label title = new Label("📊 Sales / Supply Analytics");
        title.getStyleClass().add("page-title");
        root.getChildren().add(title);

        // Statistics Cards Row
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.getChildren().addAll(
            createStatCard("📦", "Total Items", getTotalItems(), ""),
            createStatCard("📊", "Total Stock", formatQuantity(getTotalStock()), "kg"),
            createStatCard("🏬", "Active Centers", getActiveCenters(), ""),
            createStatCard("👤", "Active Vendors", getActiveVendors(), "")
        );
        root.getChildren().add(statsRow);

        // Charts Row
        HBox chartsRow = new HBox(20);
        chartsRow.setAlignment(Pos.CENTER);
        
        VBox chart1 = createChartBox("Top Products by Quantity", createTopProductsChart());
        VBox chart2 = createChartBox("Stock Distribution by Center", createCenterDistributionChart());
        
        chartsRow.getChildren().addAll(chart1, chart2);
        root.getChildren().add(chartsRow);

        // Demand Forecast Section
        VBox forecastBox = createForecastBox();
        root.getChildren().add(forecastBox);
    }

    private Node createStatCard(String icon, String title, String value, String unit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("summary-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setPrefHeight(150);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 36px;");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("summary-card-title");
        
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
        return card;
    }

    private VBox createChartBox(String title, Node chart) {
        VBox box = new VBox(10);
        box.getStyleClass().add("chart-container");
        box.setPadding(new Insets(15));
        box.setPrefWidth(450);
        box.setPrefHeight(350);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("chart-title");
        
        box.getChildren().addAll(titleLabel, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return box;
    }

    private Node createTopProductsChart() {
        PieChart chart = new PieChart();
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT g.name, SUM(ci.quantity + COALESCE(vi.quantity, 0)) AS total
                 FROM goods g
                 LEFT JOIN center_inventory ci ON ci.goods_id = g.id
                 LEFT JOIN vendor_inventory vi ON vi.goods_id = g.id
                 GROUP BY g.id, g.name
                 ORDER BY total DESC
                 LIMIT 5
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    int total = rs.getInt("total");
                    if (total > 0) {
                        chart.getData().add(new PieChart.Data(name, total));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return chart;
    }

    private Node createCenterDistributionChart() {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        
        CategoryAxis xAxis = (CategoryAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        xAxis.setLabel("Distribution Centers");
        yAxis.setLabel("Total Stock (kg)");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT center_code, SUM(quantity) AS total
                 FROM center_inventory
                 GROUP BY center_code
                 ORDER BY total DESC
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    series.getData().add(new XYChart.Data<>(
                        rs.getString("center_code"),
                        rs.getInt("total")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        chart.getData().add(series);
        return chart;
    }

    private VBox createForecastBox() {
        VBox box = new VBox(15);
        box.getStyleClass().add("info-box");
        box.setPadding(new Insets(20));
        
        Label title = new Label("📈 Demand Forecast & Insights");
        title.getStyleClass().add("info-box-title");
        
        VBox insights = new VBox(10);
        insights.setPadding(new Insets(10, 0, 0, 0));
        
        // Get insights
        String mostDemanded = getMostDemandedProduct();
        String leastStocked = getLeastStockedProduct();
        String topCenter = getTopCenter();
        
        insights.getChildren().addAll(
            createInsightItem("🔥 Most Demanded Product", mostDemanded),
            createInsightItem("⚠️ Low Stock Alert", leastStocked),
            createInsightItem("🏆 Top Distribution Center", topCenter)
        );
        
        box.getChildren().addAll(title, insights);
        return box;
    }

    private Node createInsightItem(String label, String value) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Label labelLbl = new Label(label + ":");
        labelLbl.getStyleClass().add("insight-label");
        
        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("insight-value");
        
        item.getChildren().addAll(labelLbl, valueLbl);
        return item;
    }

    private String getTotalItems() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(DISTINCT goods_id) FROM center_inventory")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private int getTotalStock() {
        int centerStock = 0;
        int vendorStock = 0;
        
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(quantity), 0) FROM center_inventory")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) centerStock = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(quantity), 0) FROM vendor_inventory")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) vendorStock = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return centerStock + vendorStock;
    }

    private String getActiveCenters() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(DISTINCT center_code) FROM center_inventory")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String getActiveVendors() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(DISTINCT vendor_id) FROM vendor_inventory")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
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

    private String getLeastStockedProduct() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT g.name, SUM(ci.quantity) AS total
                 FROM center_inventory ci
                 JOIN goods g ON g.id = ci.goods_id
                 GROUP BY g.id, g.name
                 ORDER BY total ASC
                 LIMIT 1
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name") + " (" + rs.getInt("total") + " kg)";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getTopCenter() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT center_code, SUM(quantity) AS total
                 FROM center_inventory
                 GROUP BY center_code
                 ORDER BY total DESC
                 LIMIT 1
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("center_code") + " (" + formatQuantity(rs.getInt("total")) + " kg)";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
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

