package com.slginventory.ui;

import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class VendorsManagementPage {
    private final VBox root = new VBox();
    private final Stage stage;
    private final User user;
    private TableView<VendorManagementService.VendorInfo> vendorTable;

    public VendorsManagementPage(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        build();
        refresh();
    }

    private void build() {
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("management-page");

        // Title and Add Button Row
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("👤 Vendors Management");
        title.getStyleClass().add("page-title");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("➕ Add Vendor");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> showAddVendorDialog());
        
        titleRow.getChildren().addAll(title, spacer, addBtn);
        root.getChildren().add(titleRow);

        // Search bar
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search vendors...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterVendors(newVal));
        
        searchRow.getChildren().addAll(new Label("Search:"), searchField);
        root.getChildren().add(searchRow);

        // Table
        vendorTable = new TableView<>();
        vendorTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        vendorTable.getStyleClass().add("management-table");
        
        // Columns
        TableColumn<VendorManagementService.VendorInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getName()));
        nameCol.setPrefWidth(200);
        
        TableColumn<VendorManagementService.VendorInfo, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUsername()));
        usernameCol.setPrefWidth(150);
        
        TableColumn<VendorManagementService.VendorInfo, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCompany()));
        companyCol.setPrefWidth(200);
        
        TableColumn<VendorManagementService.VendorInfo, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getPhone()));
        phoneCol.setPrefWidth(150);
        
        TableColumn<VendorManagementService.VendorInfo, String> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(""));
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Edit");
            private final Button deleteBtn = new Button("🗑️ Delete");
            private final HBox box = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.getStyleClass().add("button-secondary");
                editBtn.setPrefWidth(80);
                deleteBtn.getStyleClass().add("button-danger");
                deleteBtn.setPrefWidth(80);
                box.setAlignment(Pos.CENTER);
                
                editBtn.setOnAction(e -> {
                    VendorManagementService.VendorInfo vendor = getTableView().getItems().get(getIndex());
                    showEditVendorDialog(vendor);
                });
                
                deleteBtn.setOnAction(e -> {
                    VendorManagementService.VendorInfo vendor = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(vendor);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
        actionsCol.setPrefWidth(200);
        
        vendorTable.getColumns().addAll(nameCol, usernameCol, companyCol, phoneCol, actionsCol);
        
        VBox.setVgrow(vendorTable, Priority.ALWAYS);
        root.getChildren().add(vendorTable);
    }


    private void filterVendors(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            refresh();
            return;
        }
        
        String lowerSearch = searchText.toLowerCase();
        vendorTable.getItems().removeIf(vendor -> 
            !vendor.getName().toLowerCase().contains(lowerSearch) &&
            !vendor.getUsername().toLowerCase().contains(lowerSearch) &&
            !vendor.getCompany().toLowerCase().contains(lowerSearch) &&
            (vendor.getPhone() == null || !vendor.getPhone().toLowerCase().contains(lowerSearch))
        );
    }

    private void showAddVendorDialog() {
        VendorDialog dialog = new VendorDialog(stage, null, this::refresh);
        dialog.show();
    }

    private void showEditVendorDialog(VendorManagementService.VendorInfo vendor) {
        VendorDialog dialog = new VendorDialog(stage, vendor, this::refresh);
        dialog.show();
    }

    private void showDeleteConfirmation(VendorManagementService.VendorInfo vendor) {
        try {
            int inventoryCount = VendorManagementService.getVendorInventoryCount(vendor.getId());
            String message = "Are you sure you want to delete vendor:\n\n" +
                    "Name: " + vendor.getName() + "\n" +
                    "Company: " + vendor.getCompany() + "\n\n";
            
            if (inventoryCount > 0) {
                message += "⚠️ WARNING: This vendor has " + inventoryCount + " inventory items that will also be deleted!";
            }
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Vendor");
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText(message);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        VendorManagementService.deleteVendor(vendor.getId());
                        refresh();
                        showSuccess("Vendor deleted successfully!");
                    } catch (SQLException e) {
                        showError("Failed to delete vendor: " + e.getMessage());
                    }
                }
            });
        } catch (SQLException e) {
            showError("Failed to check vendor inventory: " + e.getMessage());
        }
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

    public void refresh() {
        try {
            List<VendorManagementService.VendorInfo> vendors = VendorManagementService.getAllVendors();
            vendorTable.getItems().setAll(vendors);
        } catch (SQLException e) {
            showError("Failed to load vendors: " + e.getMessage());
        }
    }

    public Node getRoot() {
        return root;
    }
}

