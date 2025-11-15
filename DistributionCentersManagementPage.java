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

public class DistributionCentersManagementPage {
    private final VBox root = new VBox();
    private final Stage stage;
    private final User user;
    private TableView<CenterManagementService.CenterInfo> centerTable;

    public DistributionCentersManagementPage(Stage stage, User user) {
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
        
        Label title = new Label("🏢 Distribution Centers Management");
        title.getStyleClass().add("page-title");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("➕ Add Center");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> showAddCenterDialog());
        
        titleRow.getChildren().addAll(title, spacer, addBtn);
        root.getChildren().add(titleRow);

        // Search bar
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search centers...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCenters(newVal));
        
        searchRow.getChildren().addAll(new Label("Search:"), searchField);
        root.getChildren().add(searchRow);

        // Table
        centerTable = new TableView<>();
        centerTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        centerTable.getStyleClass().add("management-table");
        
        // Columns
        TableColumn<CenterManagementService.CenterInfo, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCode()));
        codeCol.setPrefWidth(120);
        
        TableColumn<CenterManagementService.CenterInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getName()));
        nameCol.setPrefWidth(200);
        
        TableColumn<CenterManagementService.CenterInfo, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getLocation()));
        locationCol.setPrefWidth(150);
        
        TableColumn<CenterManagementService.CenterInfo, Integer> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getItemCount()).asObject());
        itemsCol.setPrefWidth(80);
        
        TableColumn<CenterManagementService.CenterInfo, Integer> quantityCol = new TableColumn<>("Total Qty (kg)");
        quantityCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getTotalQuantity()).asObject());
        quantityCol.setPrefWidth(120);
        
        TableColumn<CenterManagementService.CenterInfo, String> actionsCol = new TableColumn<>("Actions");
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
                    CenterManagementService.CenterInfo center = getTableView().getItems().get(getIndex());
                    showEditCenterDialog(center);
                });
                
                deleteBtn.setOnAction(e -> {
                    CenterManagementService.CenterInfo center = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(center);
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
        
        centerTable.getColumns().addAll(codeCol, nameCol, locationCol, itemsCol, quantityCol, actionsCol);
        
        VBox.setVgrow(centerTable, Priority.ALWAYS);
        root.getChildren().add(centerTable);
    }


    private void filterCenters(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            refresh();
            return;
        }
        
        String lowerSearch = searchText.toLowerCase();
        centerTable.getItems().removeIf(center -> 
            !center.getCode().toLowerCase().contains(lowerSearch) &&
            !center.getName().toLowerCase().contains(lowerSearch) &&
            !center.getLocation().toLowerCase().contains(lowerSearch)
        );
    }

    private void showAddCenterDialog() {
        CenterDialog dialog = new CenterDialog(stage, null, this::refresh);
        dialog.show();
    }

    private void showEditCenterDialog(CenterManagementService.CenterInfo center) {
        CenterDialog dialog = new CenterDialog(stage, center, this::refresh);
        dialog.show();
    }

    private void showDeleteConfirmation(CenterManagementService.CenterInfo center) {
        String message = "Are you sure you want to delete center:\n\n" +
                "Code: " + center.getCode() + "\n" +
                "Name: " + center.getName() + "\n\n";
        
        if (center.getItemCount() > 0) {
            message += "⚠️ WARNING: This center has " + center.getItemCount() + 
                      " inventory items (" + center.getTotalQuantity() + " kg total) that will also be deleted!";
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Distribution Center");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText(message);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    CenterManagementService.deleteCenter(center.getCode());
                    refresh();
                    showSuccess("Distribution center deleted successfully!");
                } catch (SQLException e) {
                    showError("Failed to delete center: " + e.getMessage());
                }
            }
        });
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
            List<CenterManagementService.CenterInfo> centers = CenterManagementService.getAllCenters();
            centerTable.getItems().setAll(centers);
        } catch (SQLException e) {
            showError("Failed to load centers: " + e.getMessage());
        }
    }

    public Node getRoot() {
        return root;
    }
}

