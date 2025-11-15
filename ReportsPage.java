package com.slginventory.ui;

import com.slginventory.db.Database;
import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportsPage {
    private final VBox root = new VBox();
    private final Stage stage;
    private final User user;

    public ReportsPage(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        build();
    }

    private void build() {
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("reports-page");

        Label title = new Label("📄 Export Reports");
        title.getStyleClass().add("page-title");
        root.getChildren().add(title);

        // Report Type Selection
        VBox reportTypesBox = new VBox(15);
        reportTypesBox.getStyleClass().add("info-box");
        reportTypesBox.setPadding(new Insets(20));
        
        Label sectionTitle = new Label("Select Report Type");
        sectionTitle.getStyleClass().add("info-box-title");
        reportTypesBox.getChildren().add(sectionTitle);

        // Report buttons
        Button stockReportBtn = createReportButton("📦 Stock Report", "Generate comprehensive stock inventory report");
        stockReportBtn.setOnAction(e -> exportStockReport());
        
        Button vendorListBtn = createReportButton("👤 Vendor List", "Export list of all registered vendors");
        vendorListBtn.setOnAction(e -> exportVendorList());
        
        Button surplusReportBtn = createReportButton("📊 Surplus Summary", "Export vendor surplus inventory summary");
        surplusReportBtn.setOnAction(e -> exportSurplusReport());
        
        Button routeReportBtn = createReportButton("🗺️ Route Usage Report", "Export distribution route usage statistics");
        routeReportBtn.setOnAction(e -> exportRouteReport());
        
        Button centerReportBtn = createReportButton("🏢 Center Inventory Report", "Export detailed center inventory report");
        centerReportBtn.setOnAction(e -> exportCenterReport());

        VBox buttonsBox = new VBox(10, stockReportBtn, vendorListBtn, surplusReportBtn, routeReportBtn, centerReportBtn);
        reportTypesBox.getChildren().add(buttonsBox);
        
        root.getChildren().add(reportTypesBox);

        // Export Format Selection
        VBox formatBox = new VBox(10);
        formatBox.getStyleClass().add("info-box");
        formatBox.setPadding(new Insets(20));
        
        Label formatTitle = new Label("Export Format");
        formatTitle.getStyleClass().add("info-box-title");
        
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton csvRadio = new RadioButton("CSV (Comma Separated Values)");
        RadioButton txtRadio = new RadioButton("TXT (Plain Text)");
        csvRadio.setToggleGroup(formatGroup);
        txtRadio.setToggleGroup(formatGroup);
        csvRadio.setSelected(true);
        
        HBox formatRow = new HBox(15, csvRadio, txtRadio);
        formatRow.setAlignment(Pos.CENTER_LEFT);
        
        formatBox.getChildren().addAll(formatTitle, formatRow);
        root.getChildren().add(formatBox);
    }

    private Button createReportButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.getStyleClass().add("button");
        btn.setPrefWidth(400);
        btn.setPrefHeight(50);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setTooltip(new Tooltip(tooltip));
        return btn;
    }

    private void exportStockReport() {
        File file = chooseFile("Stock_Report", "CSV");
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Stock Inventory Report\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            writer.write("Item Name,Total Quantity (kg),Center Stock,Vendor Stock\n");
            
            try (Connection conn = Database.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                     SELECT g.name,
                            COALESCE(SUM(ci.quantity), 0) AS center_qty,
                            COALESCE(SUM(vi.quantity), 0) AS vendor_qty
                     FROM goods g
                     LEFT JOIN center_inventory ci ON ci.goods_id = g.id
                     LEFT JOIN vendor_inventory vi ON vi.goods_id = g.id
                     GROUP BY g.id, g.name
                     ORDER BY g.name
                 """)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int centerQty = rs.getInt("center_qty");
                        int vendorQty = rs.getInt("vendor_qty");
                        int total = centerQty + vendorQty;
                        writer.write(String.format("%s,%d,%d,%d\n",
                            rs.getString("name"), total, centerQty, vendorQty));
                    }
                }
            }
            
            showSuccess("Stock report exported successfully!");
        } catch (IOException | SQLException e) {
            showError("Failed to export report: " + e.getMessage());
        }
    }

    private void exportVendorList() {
        File file = chooseFile("Vendor_List", "CSV");
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Vendor List Report\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            writer.write("Vendor Name,Username,Company,Phone,Inventory Items,Total Quantity (kg)\n");
            
            try (Connection conn = Database.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                     SELECT v.name, u.username, u.company, v.phone,
                            COUNT(DISTINCT vi.goods_id) AS item_count,
                            COALESCE(SUM(vi.quantity), 0) AS total_qty
                     FROM vendors v
                     JOIN users u ON u.id = v.user_id
                     LEFT JOIN vendor_inventory vi ON vi.vendor_id = v.id
                     GROUP BY v.id, v.name, u.username, u.company, v.phone
                     ORDER BY v.name
                 """)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        writer.write(String.format("%s,%s,%s,%s,%d,%d\n",
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("company"),
                            rs.getString("phone") != null ? rs.getString("phone") : "",
                            rs.getInt("item_count"),
                            rs.getInt("total_qty")));
                    }
                }
            }
            
            showSuccess("Vendor list exported successfully!");
        } catch (IOException | SQLException e) {
            showError("Failed to export report: " + e.getMessage());
        }
    }

    private void exportSurplusReport() {
        File file = chooseFile("Surplus_Summary", "CSV");
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Vendor Surplus Summary Report\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            writer.write("Vendor Name,Item Name,Quantity (kg)\n");
            
            try (Connection conn = Database.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                     SELECT v.name AS vendor_name, g.name AS item_name, vi.quantity
                     FROM vendor_inventory vi
                     JOIN vendors v ON v.id = vi.vendor_id
                     JOIN goods g ON g.id = vi.goods_id
                     ORDER BY v.name, g.name
                 """)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        writer.write(String.format("%s,%s,%d\n",
                            rs.getString("vendor_name"),
                            rs.getString("item_name"),
                            rs.getInt("quantity")));
                    }
                }
            }
            
            showSuccess("Surplus summary exported successfully!");
        } catch (IOException | SQLException e) {
            showError("Failed to export report: " + e.getMessage());
        }
    }

    private void exportRouteReport() {
        File file = chooseFile("Route_Usage", "CSV");
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Distribution Route Usage Report\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            writer.write("From Center,To Center,Distance (km)\n");
            
            // Get route information from graph
            var graph = com.slginventory.algorithms.Graph.buildSriLankaCenterGraph();
            var adjacency = graph.getAdjacency();
            
            for (var entry : adjacency.entrySet()) {
                String from = entry.getKey();
                for (var edge : entry.getValue()) {
                    writer.write(String.format("%s,%s,%.1f\n", from, edge.to, edge.weightKm));
                }
            }
            
            showSuccess("Route usage report exported successfully!");
        } catch (IOException e) {
            showError("Failed to export report: " + e.getMessage());
        }
    }

    private void exportCenterReport() {
        File file = chooseFile("Center_Inventory", "CSV");
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Distribution Center Inventory Report\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            writer.write("Center Code,Item Name,Quantity (kg)\n");
            
            try (Connection conn = Database.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                     SELECT ci.center_code, g.name AS item_name, ci.quantity
                     FROM center_inventory ci
                     JOIN goods g ON g.id = ci.goods_id
                     ORDER BY ci.center_code, g.name
                 """)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        writer.write(String.format("%s,%s,%d\n",
                            rs.getString("center_code"),
                            rs.getString("item_name"),
                            rs.getInt("quantity")));
                    }
                }
            }
            
            showSuccess("Center inventory report exported successfully!");
        } catch (IOException | SQLException e) {
            showError("Failed to export report: " + e.getMessage());
        }
    }

    private File chooseFile(String baseName, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(baseName + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + extension.toLowerCase());
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(extension + " Files", "*." + extension.toLowerCase())
        );
        return fileChooser.showSaveDialog(stage);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Node getRoot() {
        return root;
    }
}

