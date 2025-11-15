package com.slginventory.ui;

import com.slginventory.model.Goods;
import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AddItemDialog {
    private final Stage dialog;
    private final User user;
    private boolean success = false;
    private Runnable onSuccess;

    public AddItemDialog(Stage parent, User user, Runnable onSuccess) {
        this.user = user;
        this.onSuccess = onSuccess;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle("Add New Item to Inventory");
        dialog.setResizable(false);
        
        build();
    }

    private void build() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(500);

        // Title
        Label title = new Label("➕ Add New Item");
        title.getStyleClass().add("dialog-title");
        root.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 20, 0));

        // Inventory Type Selection
        Label typeLabel = new Label("Inventory Type:");
        typeLabel.getStyleClass().add("dialog-label");
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton vendorRadio = new RadioButton("Vendor Stocks (Your Surplus)");
        RadioButton centerRadio = new RadioButton("Centers Inventory");
        vendorRadio.setToggleGroup(typeGroup);
        centerRadio.setToggleGroup(typeGroup);
        vendorRadio.setSelected(true);
        
        HBox typeBox = new HBox(15, vendorRadio, centerRadio);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        
        grid.add(typeLabel, 0, 0);
        grid.add(typeBox, 1, 0);

        // Center Selection (only visible when center radio is selected)
        Label centerLabel = new Label("Distribution Center:");
        centerLabel.getStyleClass().add("dialog-label");
        ComboBox<String> centerCombo = new ComboBox<>();
        centerCombo.getItems().addAll(InventoryService.getAllCenters());
        centerCombo.setValue("PETTAH");
        centerCombo.setVisible(false);
        centerLabel.setVisible(false);
        
        grid.add(centerLabel, 0, 1);
        grid.add(centerCombo, 1, 1);
        
        vendorRadio.setOnAction(e -> {
            centerCombo.setVisible(false);
            centerLabel.setVisible(false);
        });
        
        centerRadio.setOnAction(e -> {
            centerCombo.setVisible(true);
            centerLabel.setVisible(true);
        });

        // Goods Selection
        Label goodsLabel = new Label("Item Name:");
        goodsLabel.getStyleClass().add("dialog-label");
        ComboBox<String> goodsCombo = new ComboBox<>();
        try {
            List<Goods> allGoods = InventoryService.getAllGoods();
            for (Goods goods : allGoods) {
                goodsCombo.getItems().add(goods.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        goodsCombo.setEditable(true);
        goodsCombo.setPromptText("Select or type new item name");
        
        grid.add(goodsLabel, 0, 2);
        grid.add(goodsCombo, 1, 2);

        // Quantity
        Label qtyLabel = new Label("Quantity (kg):");
        qtyLabel.getStyleClass().add("dialog-label");
        TextField qtyField = new TextField();
        qtyField.setPromptText("Enter quantity");
        qtyField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                qtyField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        
        grid.add(qtyLabel, 0, 3);
        grid.add(qtyField, 1, 3);

        root.getChildren().add(grid);

        // Status label
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("dialog-status");
        root.getChildren().add(statusLabel);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> dialog.close());
        
        Button addBtn = new Button("Add Item");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> {
            String goodsName = goodsCombo.getValue() != null ? 
                goodsCombo.getValue().trim() : goodsCombo.getEditor().getText().trim();
            String qtyText = qtyField.getText().trim();
            boolean isVendor = vendorRadio.isSelected();
            
            // Clear previous status
            statusLabel.getStyleClass().removeAll("status-error", "status-success");
            
            // Validation
            if (goodsName.isEmpty()) {
                statusLabel.setText("❌ Please enter an item name");
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            
            int quantity;
            try {
                quantity = Integer.parseInt(qtyText);
                if (quantity <= 0) {
                    statusLabel.setText("❌ Please enter a quantity greater than 0");
                    statusLabel.getStyleClass().add("status-error");
                    return;
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("❌ Please enter a valid number for quantity");
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (!isVendor && centerCombo.getValue() == null) {
                statusLabel.setText("❌ Please select a distribution center");
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            
            try {
                // Get or create goods
                int goodsId = InventoryService.getOrCreateGoods(goodsName);
                
                if (isVendor) {
                    // Add to vendor inventory
                    int vendorId = InventoryService.getVendorId(user.getId());
                    InventoryService.addVendorInventory(vendorId, goodsId, quantity);
                    statusLabel.setText("✅ Item added to your vendor surplus successfully!");
                } else {
                    // Add to center inventory
                    String centerCode = centerCombo.getValue();
                    InventoryService.addCenterInventory(centerCode, goodsId, quantity);
                    statusLabel.setText("✅ Item added to " + centerCode + " center inventory successfully!");
                }
                
                statusLabel.getStyleClass().remove("status-error");
                statusLabel.getStyleClass().add("status-success");
                
                success = true;
                
                // Close dialog after a short delay
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(evt -> {
                    dialog.close();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                });
                delay.play();
                
            } catch (SQLException ex) {
                statusLabel.setText("❌ Error: " + ex.getMessage());
                statusLabel.getStyleClass().remove("status-success");
                statusLabel.getStyleClass().add("status-error");
                ex.printStackTrace();
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, addBtn);
        root.getChildren().add(buttonBox);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.setScene(scene);
    }

    public void show() {
        dialog.showAndWait();
    }

    public boolean isSuccess() {
        return success;
    }
}

