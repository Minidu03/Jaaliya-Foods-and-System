package com.slginventory.ui;

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

public class VehicleDialog {
    private final Stage dialog;
    private final TransportManagementService.VehicleInfo existingVehicle;
    private final Runnable onSuccess;

    public VehicleDialog(Stage parent, TransportManagementService.VehicleInfo existingVehicle, Runnable onSuccess) {
        this.existingVehicle = existingVehicle;
        this.onSuccess = onSuccess;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(existingVehicle == null ? "Add New Vehicle" : "Edit Vehicle");
        dialog.setResizable(false);
        
        build();
    }

    private void build() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(500);

        Label title = new Label(existingVehicle == null ? "➕ Add New Vehicle" : "✏️ Edit Vehicle");
        title.getStyleClass().add("dialog-title");
        root.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 20, 0));

        // Vehicle Number
        Label vehicleNumberLabel = new Label("Vehicle Number:");
        vehicleNumberLabel.getStyleClass().add("dialog-label");
        TextField vehicleNumberField = new TextField();
        if (existingVehicle != null) {
            vehicleNumberField.setText(existingVehicle.getVehicleNumber());
        }
        vehicleNumberField.setPromptText("e.g., ABC-1234");
        
        grid.add(vehicleNumberLabel, 0, 0);
        grid.add(vehicleNumberField, 1, 0);

        // Vehicle Type
        Label vehicleTypeLabel = new Label("Vehicle Type:");
        vehicleTypeLabel.getStyleClass().add("dialog-label");
        ComboBox<String> vehicleTypeCombo = new ComboBox<>();
        vehicleTypeCombo.getItems().addAll("Truck", "Van", "Lorry", "Container Truck", "Pickup");
        if (existingVehicle != null) {
            vehicleTypeCombo.setValue(existingVehicle.getVehicleType());
        } else {
            vehicleTypeCombo.setValue("Truck");
        }
        vehicleTypeCombo.setPrefWidth(200);
        
        grid.add(vehicleTypeLabel, 0, 1);
        grid.add(vehicleTypeCombo, 1, 1);

        // Capacity
        Label capacityLabel = new Label("Capacity (kg):");
        capacityLabel.getStyleClass().add("dialog-label");
        TextField capacityField = new TextField();
        if (existingVehicle != null) {
            capacityField.setText(String.valueOf(existingVehicle.getCapacityKg()));
        }
        capacityField.setPromptText("e.g., 5000");
        
        grid.add(capacityLabel, 0, 2);
        grid.add(capacityField, 1, 2);

        // Driver Name
        Label driverNameLabel = new Label("Driver Name:");
        driverNameLabel.getStyleClass().add("dialog-label");
        TextField driverNameField = new TextField();
        if (existingVehicle != null) {
            driverNameField.setText(existingVehicle.getDriverName());
        }
        driverNameField.setPromptText("Enter driver name");
        
        grid.add(driverNameLabel, 0, 3);
        grid.add(driverNameField, 1, 3);

        // Driver Phone
        Label driverPhoneLabel = new Label("Driver Phone:");
        driverPhoneLabel.getStyleClass().add("dialog-label");
        TextField driverPhoneField = new TextField();
        if (existingVehicle != null) {
            driverPhoneField.setText(existingVehicle.getDriverPhone());
        }
        driverPhoneField.setPromptText("e.g., 0771234567");
        
        grid.add(driverPhoneLabel, 0, 4);
        grid.add(driverPhoneField, 1, 4);

        // Status
        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("dialog-label");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("AVAILABLE", "IN_USE", "MAINTENANCE");
        if (existingVehicle != null) {
            statusCombo.setValue(existingVehicle.getStatus());
        } else {
            statusCombo.setValue("AVAILABLE");
        }
        statusCombo.setPrefWidth(200);
        
        grid.add(statusLabel, 0, 5);
        grid.add(statusCombo, 1, 5);

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
        
        Button saveBtn = new Button(existingVehicle == null ? "Add Vehicle" : "Save Changes");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> {
            String vehicleNumber = vehicleNumberField.getText().trim();
            String vehicleType = vehicleTypeCombo.getValue();
            String capacityText = capacityField.getText().trim();
            String driverName = driverNameField.getText().trim();
            String driverPhone = driverPhoneField.getText().trim();
            String status = statusCombo.getValue();
            
            statusMessageLabel.getStyleClass().removeAll("status-error", "status-success");
            
            // Validation
            if (vehicleNumber.isEmpty()) {
                statusMessageLabel.setText("❌ Please enter vehicle number");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (vehicleType == null) {
                statusMessageLabel.setText("❌ Please select vehicle type");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            int capacity;
            try {
                capacity = Integer.parseInt(capacityText);
                if (capacity <= 0) {
                    statusMessageLabel.setText("❌ Capacity must be greater than 0");
                    statusMessageLabel.getStyleClass().add("status-error");
                    return;
                }
            } catch (NumberFormatException ex) {
                statusMessageLabel.setText("❌ Please enter a valid capacity (number)");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (status == null) {
                statusMessageLabel.setText("❌ Please select status");
                statusMessageLabel.getStyleClass().add("status-error");
                return;
            }
            
            try {
                if (existingVehicle == null) {
                    TransportManagementService.addVehicle(vehicleNumber, vehicleType, capacity, driverName, driverPhone, status);
                    statusMessageLabel.setText("✅ Vehicle added successfully!");
                } else {
                    TransportManagementService.updateVehicle(existingVehicle.getId(), vehicleNumber, vehicleType, 
                                                            capacity, driverName, driverPhone, status);
                    statusMessageLabel.setText("✅ Vehicle updated successfully!");
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

