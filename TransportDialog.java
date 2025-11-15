package com.slginventory.ui;

import com.slginventory.model.Goods;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TransportDialog {
    private final Stage dialog;
    private final TransportManagementService.TransportInfo existingTransport;
    private final Runnable onSuccess;

    public TransportDialog(Stage parent, TransportManagementService.TransportInfo existingTransport, Runnable onSuccess) {
        this.existingTransport = existingTransport;
        this.onSuccess = onSuccess;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(existingTransport == null ? "Create New Transport" : "Edit Transport");
        dialog.setResizable(false);
        
        build();
    }

    private void build() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(550);

        Label title = new Label(existingTransport == null ? "🚚 Create New Transport" : "✏️ Edit Transport");
        title.getStyleClass().add("dialog-title");
        root.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 20, 0));

        // Vehicle
        Label vehicleLabel = new Label("Vehicle:");
        vehicleLabel.getStyleClass().add("dialog-label");
        ComboBox<TransportManagementService.VehicleInfo> vehicleCombo = new ComboBox<>();
        try {
            List<TransportManagementService.VehicleInfo> vehicles = TransportManagementService.getAllVehicles();
            vehicleCombo.getItems().addAll(vehicles);
            vehicleCombo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(TransportManagementService.VehicleInfo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getVehicleNumber() + " (" + item.getVehicleType() + " - " + item.getStatus() + ")");
                    }
                }
            });
            vehicleCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(TransportManagementService.VehicleInfo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getVehicleNumber() + " (" + item.getVehicleType() + " - " + item.getStatus() + ")");
                    }
                }
            });
            if (existingTransport != null) {
                for (TransportManagementService.VehicleInfo v : vehicles) {
                    if (v.getId() == existingTransport.getVehicleId()) {
                        vehicleCombo.setValue(v);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            // Handle error
        }
        vehicleCombo.setPrefWidth(300);
        
        grid.add(vehicleLabel, 0, 0);
        grid.add(vehicleCombo, 1, 0);

        // Source Center
        Label sourceLabel = new Label("Source Center:");
        sourceLabel.getStyleClass().add("dialog-label");
        ComboBox<String> sourceCombo = new ComboBox<>();
        sourceCombo.getItems().addAll(InventoryService.getAllCenters());
        if (existingTransport != null) {
            sourceCombo.setValue(existingTransport.getSourceCenter());
        }
        sourceCombo.setPrefWidth(200);
        
        grid.add(sourceLabel, 0, 1);
        grid.add(sourceCombo, 1, 1);

        // Destination Center
        Label destLabel = new Label("Destination Center:");
        destLabel.getStyleClass().add("dialog-label");
        ComboBox<String> destCombo = new ComboBox<>();
        destCombo.getItems().addAll(InventoryService.getAllCenters());
        if (existingTransport != null) {
            destCombo.setValue(existingTransport.getDestinationCenter());
        }
        destCombo.setPrefWidth(200);
        
        grid.add(destLabel, 0, 2);
        grid.add(destCombo, 1, 2);

        // Goods
        Label goodsLabel = new Label("Goods:");
        goodsLabel.getStyleClass().add("dialog-label");
        ComboBox<Goods> goodsCombo = new ComboBox<>();
        try {
            List<Goods> goodsList = InventoryService.getAllGoods();
            goodsCombo.getItems().addAll(goodsList);
            if (existingTransport != null) {
                for (Goods g : goodsList) {
                    if (g.getId() == existingTransport.getGoodsId()) {
                        goodsCombo.setValue(g);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            // Handle error
        }
        goodsCombo.setPrefWidth(200);
        
        grid.add(goodsLabel, 0, 3);
        grid.add(goodsCombo, 1, 3);

        // Quantity
        Label quantityLabel = new Label("Quantity:");
        quantityLabel.getStyleClass().add("dialog-label");
        TextField quantityField = new TextField();
        if (existingTransport != null) {
            quantityField.setText(String.valueOf(existingTransport.getQuantity()));
        }
        quantityField.setPromptText("Enter quantity");
        
        grid.add(quantityLabel, 0, 4);
        grid.add(quantityField, 1, 4);

        // Scheduled Date
        Label scheduledDateLabel = new Label("Scheduled Date:");
        scheduledDateLabel.getStyleClass().add("dialog-label");
        DatePicker scheduledDatePicker = new DatePicker();
        if (existingTransport != null && existingTransport.getScheduledDate() != null) {
            scheduledDatePicker.setValue(existingTransport.getScheduledDate().toLocalDate());
        } else {
            scheduledDatePicker.setValue(LocalDate.now());
        }
        scheduledDatePicker.setPrefWidth(200);
        
        grid.add(scheduledDateLabel, 0, 5);
        grid.add(scheduledDatePicker, 1, 5);

        // Status
        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("dialog-label");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("SCHEDULED", "IN_TRANSIT", "DELIVERED", "CANCELLED");
        if (existingTransport != null) {
            statusCombo.setValue(existingTransport.getStatus());
        } else {
            statusCombo.setValue("SCHEDULED");
        }
        statusCombo.setPrefWidth(200);
        
        grid.add(statusLabel, 0, 6);
        grid.add(statusCombo, 1, 6);

        // Notes
        Label notesLabel = new Label("Notes:");
        notesLabel.getStyleClass().add("dialog-label");
        TextArea notesArea = new TextArea();
        if (existingTransport != null) {
            notesArea.setText(existingTransport.getNotes());
        }
        notesArea.setPromptText("Enter any additional notes...");
        notesArea.setPrefRowCount(3);
        notesArea.setPrefWidth(300);
        
        grid.add(notesLabel, 0, 7);
        grid.add(notesArea, 1, 7);

        root.getChildren().add(grid);

        // Status label
        Label statusMessageLabel = new Label();
        statusMessageLabel.getStyleClass().add("dialog-status");
        root.getChildren().add(statusMessageLabel);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> dialog.close());
        
        Button saveBtn = new Button(existingTransport == null ? "Create Transport" : "Save Changes");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> {
            TransportManagementService.VehicleInfo selectedVehicle = vehicleCombo.getValue();
            String sourceCenter = sourceCombo.getValue();
            String destCenter = destCombo.getValue();
            Goods selectedGoods = goodsCombo.getValue();
            String quantityText = quantityField.getText().trim();
            LocalDate scheduledDate = scheduledDatePicker.getValue();
            String status = statusCombo.getValue();
            String notes = notesArea.getText().trim();
            
            statusMessageLabel.getStyleClass().removeAll("status-error", "status-success");
            
            // Validation
            if (selectedVehicle == null) {
                statusMessageLabel.setText("❌ Please select a vehicle");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (sourceCenter == null) {
                statusMessageLabel.setText("❌ Please select source center");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (destCenter == null) {
                statusMessageLabel.setText("❌ Please select destination center");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (sourceCenter.equals(destCenter)) {
                statusMessageLabel.setText("❌ Source and destination cannot be the same");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (selectedGoods == null) {
                statusMessageLabel.setText("❌ Please select goods");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    statusMessageLabel.setText("❌ Quantity must be greater than 0");
                    statusMessageLabel.getStyleClass().add("status-error");
                    return;
                }
            } catch (NumberFormatException ex) {
                statusMessageLabel.setText("❌ Please enter a valid quantity (number)");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (scheduledDate == null) {
                statusMessageLabel.setText("❌ Please select scheduled date");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (status == null) {
                statusMessageLabel.setText("❌ Please select status");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            try {
                LocalDateTime scheduledDateTime = scheduledDate.atTime(LocalTime.of(9, 0)); // Default to 9 AM
                if (existingTransport == null) {
                    TransportManagementService.createTransport(
                        selectedVehicle.getId(), sourceCenter, destCenter, selectedGoods.getId(),
                        quantity, status, scheduledDateTime, notes
                    );
                    statusMessageLabel.setText("✅ Transport created successfully!");
                } else {
                    TransportManagementService.updateTransport(
                        existingTransport.getId(), selectedVehicle.getId(), sourceCenter, destCenter,
                        selectedGoods.getId(), quantity, status, scheduledDateTime,
                        existingTransport.getDepartureDate(), existingTransport.getArrivalDate(), notes
                    );
                    statusMessageLabel.setText("✅ Transport updated successfully!");
                }
                statusMessageLabel.getStyleClass().add("status-success");
                
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(evt -> {
                    dialog.close();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                });
                delay.play();
            } catch (SQLException ex) {
                statusMessageLabel.setText("❌ Error: " + ex.getMessage());
                statusMessageLabel.getStyleClass().add("status-error");
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().add(buttonBox);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.setScene(scene);
    }

    public void show() {
        dialog.showAndWait();
    }
}

