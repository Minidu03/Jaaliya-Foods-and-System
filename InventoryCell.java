package com.slginventory.ui;

import com.slginventory.model.InventoryItem;
import javafx.scene.control.ListCell;

/**
 * Custom ListCell for displaying InventoryItem objects in ListView
 * Formats the display of inventory items with quantity and styling
 */
public class InventoryCell extends ListCell<InventoryItem> {
    
    @Override
    protected void updateItem(InventoryItem item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setText(null);
            setStyle(""); // Reset style
        } else {
            // Format: "Goods Name — Quantity kg"
            String displayText = String.format("%s — %d kg", 
                item.getGoods().getName(), 
                item.getQuantity());
            setText(displayText);
            
            // Apply styling based on quantity
            applyQuantityStyling(item.getQuantity());
        }
    }
    
    /**
     * Applies CSS styling based on inventory quantity levels
     * @param quantity the quantity of the inventory item
     */
    private void applyQuantityStyling(int quantity) {
        // Remove any existing style classes
        getStyleClass().removeAll("quantity-high", "quantity-medium", "quantity-low");
        
        // Apply appropriate styling based on quantity
        if (quantity >= 1000) {
            // High quantity - green
            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            getStyleClass().add("quantity-high");
        } else if (quantity >= 100) {
            // Medium quantity - orange  
            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            getStyleClass().add("quantity-medium");
        } else {
            // Low quantity - red
            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            getStyleClass().add("quantity-low");
        }
    }
}
