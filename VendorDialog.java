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

public class VendorDialog {
    private final Stage dialog;
    private final VendorManagementService.VendorInfo existingVendor;
    private final Runnable onSuccess;

    public VendorDialog(Stage parent, VendorManagementService.VendorInfo existingVendor, Runnable onSuccess) {
        this.existingVendor = existingVendor;
        this.onSuccess = onSuccess;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(existingVendor == null ? "Add New Vendor" : "Edit Vendor");
        dialog.setResizable(false);
        
        build();
    }

    private void build() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(500);

        Label title = new Label(existingVendor == null ? "➕ Add New Vendor" : "✏️ Edit Vendor");
        title.getStyleClass().add("dialog-title");
        root.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 20, 0));

        // Name
        Label nameLabel = new Label("Vendor Name:");
        nameLabel.getStyleClass().add("dialog-label");
        TextField nameField = new TextField();
        if (existingVendor != null) {
            nameField.setText(existingVendor.getName());
        }
        nameField.setPromptText("Enter vendor name");
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);

        // Phone
        Label phoneLabel = new Label("Phone:");
        phoneLabel.getStyleClass().add("dialog-label");
        TextField phoneField = new TextField();
        if (existingVendor != null) {
            phoneField.setText(existingVendor.getPhone());
        }
        phoneField.setPromptText("Enter phone number");
        
        grid.add(phoneLabel, 0, 1);
        grid.add(phoneField, 1, 1);

        // Read-only fields for existing vendor
        if (existingVendor != null) {
            Label usernameLabel = new Label("Username:");
            usernameLabel.getStyleClass().add("dialog-label");
            Label usernameValue = new Label(existingVendor.getUsername());
            usernameValue.getStyleClass().add("dialog-readonly");
            
            grid.add(usernameLabel, 0, 2);
            grid.add(usernameValue, 1, 2);
            
            Label companyLabel = new Label("Company:");
            companyLabel.getStyleClass().add("dialog-label");
            Label companyValue = new Label(existingVendor.getCompany());
            companyValue.getStyleClass().add("dialog-readonly");
            
            grid.add(companyLabel, 0, 3);
            grid.add(companyValue, 1, 3);
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
        
        Button saveBtn = new Button(existingVendor == null ? "Add Vendor" : "Save Changes");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            
            statusLabel.getStyleClass().removeAll("status-error", "status-success");
            
            if (name.isEmpty()) {
                statusLabel.setText("❌ Please enter vendor name");
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            
            try {
                if (existingVendor == null) {
                    statusLabel.setText("❌ Adding new vendors requires user registration. Please use the registration page.");
                    statusLabel.getStyleClass().add("status-error");
                } else {
                    VendorManagementService.updateVendor(existingVendor.getId(), name, phone);
                    statusLabel.setText("✅ Vendor updated successfully!");
                    statusLabel.getStyleClass().add("status-success");
                    
                    javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                    delay.setOnFinished(evt -> {
                        dialog.close();
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    });
                    delay.play();
                }
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

