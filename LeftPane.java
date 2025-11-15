package com.slginventory.ui;

import com.slginventory.db.Database;
import com.slginventory.model.Goods;
import com.slginventory.model.InventoryItem;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeftPane {
    private final VBox root = new VBox();
    private final ListView<InventoryItem> centerList = new ListView<>();
    private final ListView<InventoryItem> vendorList = new ListView<>();

    public LeftPane() {
        root.setPadding(new Insets(8));
        root.setSpacing(8);
        TitledPane centers = new TitledPane("Stocks Inventory (Centers)", centerList);
        centers.setExpanded(true);
        TitledPane vendors = new TitledPane("Vendor Inventory (Your Surplus)", vendorList);
        vendors.setExpanded(true);
        root.getChildren().addAll(centers, vendors, new Label("Tip: Items are sorted by quantity desc"));

        centerList.setCellFactory(lv -> new InventoryCell());
        vendorList.setCellFactory(lv -> new InventoryCell());
        centerList.setPrefWidth(360);
        vendorList.setPrefWidth(360);
    }

    public Node getRoot() {
        return root;
    }

    public List<InventoryItem> loadCenterInventory() {
        List<InventoryItem> list = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT g.id, g.name, SUM(ci.quantity) AS qty
                 FROM center_inventory ci
                 JOIN goods g ON g.id = ci.goods_id
                 GROUP BY g.id, g.name
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Goods goods = new Goods(rs.getInt("id"), rs.getString("name"));
                    list.add(new InventoryItem(goods, rs.getInt("qty")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load center inventory", e);
        }
        return list;
    }

    public List<InventoryItem> loadVendorInventory(int userId) {
        List<InventoryItem> list = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT g.id, g.name, SUM(vi.quantity) AS qty
                 FROM vendor_inventory vi
                 JOIN vendors v ON v.id = vi.vendor_id
                 JOIN users u ON u.id = v.user_id
                 JOIN goods g ON g.id = vi.goods_id
                 WHERE u.id = ?
                 GROUP BY g.id, g.name
             """)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Goods goods = new Goods(rs.getInt("id"), rs.getString("name"));
                    list.add(new InventoryItem(goods, rs.getInt("qty")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load vendor inventory", e);
        }
        return list;
    }

    public void showInventories(List<InventoryItem> centers, List<InventoryItem> vendors) {
        centerList.getItems().setAll(centers);
        vendorList.getItems().setAll(vendors);
    }
}
