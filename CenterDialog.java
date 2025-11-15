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

public class CenterDialog {
    private final Stage dialog;
    private final CenterManagementService.CenterInfo existingCenter;
    private final Runnable onSuccess;

    public CenterDialog(Stage parent, CenterManagementService.CenterInfo existingCenter, Runnable onSuccess) {
        this.existingCenter = existingCenter;
        this.onSuccess = onSuccess;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(existingCenter == null ? "Add New Distribution Center" : "Edit Distribution Center");
        dialog.setResizable(false);
        
        build();
    }

    private void build() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(500);

        Label title = new Label(existingCenter == null ? "➕ Add New Distribution Center" : "✏️ Edit Distribution Center");
        title.getStyleClass().add("dialog-title");
        root.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 20, 0));

        // Code
        Label codeLabel = new Label("Center Code:");
        codeLabel.getStyleClass().add("dialog-label");
        TextField codeField = new TextField();
        if (existingCenter != null) {
            codeField.setText(existingCenter.getCode());
            codeField.setEditable(false);
            codeField.getStyleClass().add("dialog-readonly");
        } else {
            codeField.setPromptText("e.g., COLOMBO, KURUNEGALA");
        }
        
        grid.add(codeLabel, 0, 0);
        grid.add(codeField, 1, 0);

        // Name
        Label nameLabel = new Label("Center Name:");
        nameLabel.getStyleClass().add("dialog-label");
        TextField nameField = new TextField();
        if (existingCenter != null) {
            nameField.setText(existingCenter.getName());
        }
        nameField.setPromptText("Enter center name");
        
        grid.add(nameLabel, 0, 1);
        grid.add(nameField, 1, 1);

        // Location
        Label locationLabel = new Label("Location:");
        locationLabel.getStyleClass().add("dialog-label");
        TextField locationField = new TextField();
        if (existingCenter != null) {
            locationField.setText(existingCenter.getLocation());
        }
        locationField.setPromptText("Enter location");
        
        grid.add(locationLabel, 0, 2);
        grid.add(locationField, 1, 2);

        // Description
        Label descLabel = new Label("Description:");
        descLabel.getStyleClass().add("dialog-label");
        TextArea descArea = new TextArea();
        if (existingCenter != null) {
            descArea.setText(existingCenter.getDescription());
        }
        descArea.setPromptText("Enter description");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        
        grid.add(descLabel, 0, 3);
        grid.add(descArea, 1, 3);

        // Read-only stats for existing center
        if (existingCenter != null) {
            Label itemsLabel = new Label("Inventory Items:");
            itemsLabel.getStyleClass().add("dialog-label");
            Label itemsValue = new Label(String.valueOf(existingCenter.getItemCount()));
            itemsValue.getStyleClass().add("dialog-readonly");
            
            grid.add(itemsLabel, 0, 4);
            grid.add(itemsValue, 1, 4);
            
            Label qtyLabel = new Label("Total Quantity:");
            qtyLabel.getStyleClass().add("dialog-label");
            Label qtyValue = new Label(existingCenter.getTotalQuantity() + " kg");
            qtyValue.getStyleClass().add("dialog-readonly");
            
            grid.add(qtyLabel, 0, 5);
            grid.add(qtyValue, 1, 5);
        }

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
        
        Button saveBtn = new Button(existingCenter == null ? "Add Center" : "Save Changes");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> {
            String code = codeField.getText().trim().toUpperCase().replaceAll("\\s+", "_");
            String name = nameField.getText().trim();
            String location = locationField.getText().trim();
            String description = descArea.getText().trim();
            
            statusLabel.getStyleClass().removeAll("status-error", "status-success");
            
            if (code.isEmpty()) {
                statusLabel.setText("❌ Please enter center code");
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (name.isEmpty()) {
                statusLabel.setText("❌ Please enter center name");
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            
            if (location.isEmpty()) {
                statusLabel.setText("❌ Please enter location");
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            
            try {
                if (existingCenter == null) {
                    CenterManagementService.addCenter(code, name, location, description);
                    statusLabel.setText("✅ Distribution center added successfully!");
                } else {
                    CenterManagementService.updateCenter(code, name, location, description);
                    statusLabel.setText("✅ Distribution center updated successfully!");
                }
                
                statusLabel.getStyleClass().add("status-success");
                
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
                statusLabel.getStyleClass().add("status-error");
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

