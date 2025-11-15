package com.slginventory.ui;

import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransportManagementPage {
    private final VBox root = new VBox();
    private final Stage stage;
    private TableView<TransportManagementService.VehicleInfo> vehicleTable;
    private TableView<TransportManagementService.TransportInfo> transportTable;
    private TabPane tabPane;
    private TextField vehicleSearchField;
    private TextField transportSearchField;

    public TransportManagementPage(Stage stage, User user) {
        this.stage = stage;
        build();
        refresh();
    }

    private void build() {
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("management-page");

        // Title
        Label title = new Label("🚚 Transport Management");
        title.getStyleClass().add("page-title");
        root.getChildren().add(title);

        // Tab Pane
        tabPane = new TabPane();
        tabPane.getStyleClass().add("transport-tabs");

        // Vehicles Tab
        Tab vehiclesTab = new Tab("Vehicles");
        vehiclesTab.setClosable(false);
        VBox vehiclesContent = createVehiclesTab();
        vehiclesTab.setContent(vehiclesContent);
        tabPane.getTabs().add(vehiclesTab);

        // Transports Tab
        Tab transportsTab = new Tab("Transports");
        transportsTab.setClosable(false);
        VBox transportsContent = createTransportsTab();
        transportsTab.setContent(transportsContent);
        tabPane.getTabs().add(transportsTab);

        root.getChildren().add(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
    }

    private VBox createVehiclesTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        // Title and Add Button Row
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("➕ Add Vehicle");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> showAddVehicleDialog());
        
        titleRow.getChildren().addAll(spacer, addBtn);
        content.getChildren().add(titleRow);

        // Search bar
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        
        vehicleSearchField = new TextField();
        vehicleSearchField.setPromptText("Search vehicles...");
        vehicleSearchField.setPrefWidth(300);
        vehicleSearchField.getStyleClass().add("search-field");
        vehicleSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterVehicles(newVal));
        
        searchRow.getChildren().addAll(new Label("Search:"), vehicleSearchField);
        content.getChildren().add(searchRow);

        // Table
        vehicleTable = new TableView<>();
        vehicleTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        vehicleTable.getStyleClass().add("management-table");
        
        // Columns
        TableColumn<TransportManagementService.VehicleInfo, String> vehicleNumberCol = new TableColumn<>("Vehicle Number");
        vehicleNumberCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getVehicleNumber()));
        vehicleNumberCol.setPrefWidth(150);
        
        TableColumn<TransportManagementService.VehicleInfo, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getVehicleType()));
        typeCol.setPrefWidth(120);
        
        TableColumn<TransportManagementService.VehicleInfo, String> capacityCol = new TableColumn<>("Capacity (kg)");
        capacityCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getCapacityKg())));
        capacityCol.setPrefWidth(120);
        
        TableColumn<TransportManagementService.VehicleInfo, String> driverNameCol = new TableColumn<>("Driver Name");
        driverNameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDriverName()));
        driverNameCol.setPrefWidth(150);
        
        TableColumn<TransportManagementService.VehicleInfo, String> driverPhoneCol = new TableColumn<>("Driver Phone");
        driverPhoneCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDriverPhone()));
        driverPhoneCol.setPrefWidth(130);
        
        TableColumn<TransportManagementService.VehicleInfo, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
        statusCol.setPrefWidth(120);
        
        TableColumn<TransportManagementService.VehicleInfo, String> actionsCol = new TableColumn<>("Actions");
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
                    TransportManagementService.VehicleInfo vehicle = getTableView().getItems().get(getIndex());
                    showEditVehicleDialog(vehicle);
                });
                
                deleteBtn.setOnAction(e -> {
                    TransportManagementService.VehicleInfo vehicle = getTableView().getItems().get(getIndex());
                    showDeleteVehicleConfirmation(vehicle);
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
        
        vehicleTable.getColumns().addAll(vehicleNumberCol, typeCol, capacityCol, driverNameCol, driverPhoneCol, statusCol, actionsCol);
        
        VBox.setVgrow(vehicleTable, Priority.ALWAYS);
        content.getChildren().add(vehicleTable);
        
        return content;
    }

    private VBox createTransportsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        // Title and Add Button Row
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("➕ Create Transport");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> showAddTransportDialog());
        
        titleRow.getChildren().addAll(spacer, addBtn);
        content.getChildren().add(titleRow);

        // Search bar
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        
        transportSearchField = new TextField();
        transportSearchField.setPromptText("Search transports...");
        transportSearchField.setPrefWidth(300);
        transportSearchField.getStyleClass().add("search-field");
        transportSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterTransports(newVal));
        
        searchRow.getChildren().addAll(new Label("Search:"), transportSearchField);
        content.getChildren().add(searchRow);

        // Table
        transportTable = new TableView<>();
        transportTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        transportTable.getStyleClass().add("management-table");
        
        // Columns
        TableColumn<TransportManagementService.TransportInfo, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(60);
        
        TableColumn<TransportManagementService.TransportInfo, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getVehicleNumber()));
        vehicleCol.setPrefWidth(120);
        
        TableColumn<TransportManagementService.TransportInfo, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getSourceCenter()));
        sourceCol.setPrefWidth(100);
        
        TableColumn<TransportManagementService.TransportInfo, String> destCol = new TableColumn<>("Destination");
        destCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDestinationCenter()));
        destCol.setPrefWidth(100);
        
        TableColumn<TransportManagementService.TransportInfo, String> goodsCol = new TableColumn<>("Goods");
        goodsCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getGoodsName()));
        goodsCol.setPrefWidth(120);
        
        TableColumn<TransportManagementService.TransportInfo, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getQuantity())));
        quantityCol.setPrefWidth(80);
        
        TableColumn<TransportManagementService.TransportInfo, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        
        TableColumn<TransportManagementService.TransportInfo, String> scheduledDateCol = new TableColumn<>("Scheduled Date");
        scheduledDateCol.setCellValueFactory(cell -> {
            if (cell.getValue().getScheduledDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getScheduledDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        scheduledDateCol.setPrefWidth(150);
        
        TableColumn<TransportManagementService.TransportInfo, String> actionsCol = new TableColumn<>("Actions");
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
                    TransportManagementService.TransportInfo transport = getTableView().getItems().get(getIndex());
                    showEditTransportDialog(transport);
                });
                
                deleteBtn.setOnAction(e -> {
                    TransportManagementService.TransportInfo transport = getTableView().getItems().get(getIndex());
                    showDeleteTransportConfirmation(transport);
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
        
        transportTable.getColumns().addAll(idCol, vehicleCol, sourceCol, destCol, goodsCol, quantityCol, statusCol, scheduledDateCol, actionsCol);
        
        VBox.setVgrow(transportTable, Priority.ALWAYS);
        content.getChildren().add(transportTable);
        
        return content;
    }

    private void filterVehicles(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            refreshVehicles();
            return;
        }
        
        String lowerSearch = searchText.toLowerCase();
        vehicleTable.getItems().removeIf(vehicle -> 
            !vehicle.getVehicleNumber().toLowerCase().contains(lowerSearch) &&
            !vehicle.getVehicleType().toLowerCase().contains(lowerSearch) &&
            !vehicle.getStatus().toLowerCase().contains(lowerSearch) &&
            (vehicle.getDriverName() == null || !vehicle.getDriverName().toLowerCase().contains(lowerSearch))
        );
    }

    private void filterTransports(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            refreshTransports();
            return;
        }
        
        String lowerSearch = searchText.toLowerCase();
        transportTable.getItems().removeIf(transport -> 
            !transport.getVehicleNumber().toLowerCase().contains(lowerSearch) &&
            !transport.getSourceCenter().toLowerCase().contains(lowerSearch) &&
            !transport.getDestinationCenter().toLowerCase().contains(lowerSearch) &&
            !transport.getGoodsName().toLowerCase().contains(lowerSearch) &&
            !transport.getStatus().toLowerCase().contains(lowerSearch)
        );
    }

    private void showAddVehicleDialog() {
        VehicleDialog dialog = new VehicleDialog(stage, null, this::refresh);
        dialog.show();
    }

    private void showEditVehicleDialog(TransportManagementService.VehicleInfo vehicle) {
        VehicleDialog dialog = new VehicleDialog(stage, vehicle, this::refresh);
        dialog.show();
    }

    private void showDeleteVehicleConfirmation(TransportManagementService.VehicleInfo vehicle) {
        try {
            boolean hasActiveTransports = TransportManagementService.hasActiveTransports(vehicle.getId());
            String message = "Are you sure you want to delete vehicle:\n\n" +
                    "Vehicle Number: " + vehicle.getVehicleNumber() + "\n" +
                    "Type: " + vehicle.getVehicleType() + "\n\n";
            
            if (hasActiveTransports) {
                message += "⚠️ WARNING: This vehicle has active transports (SCHEDULED or IN_TRANSIT) that will also be deleted!";
            }
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Vehicle");
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText(message);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        TransportManagementService.deleteVehicle(vehicle.getId());
                        refresh();
                        showSuccess("Vehicle deleted successfully!");
                    } catch (SQLException e) {
                        showError("Failed to delete vehicle: " + e.getMessage());
                    }
                }
            });
        } catch (SQLException e) {
            showError("Failed to check vehicle transports: " + e.getMessage());
        }
    }

    private void showAddTransportDialog() {
        TransportDialog dialog = new TransportDialog(stage, null, this::refresh);
        dialog.show();
    }

    private void showEditTransportDialog(TransportManagementService.TransportInfo transport) {
        TransportDialog dialog = new TransportDialog(stage, transport, this::refresh);
        dialog.show();
    }

    private void showDeleteTransportConfirmation(TransportManagementService.TransportInfo transport) {
        String message = "Are you sure you want to delete transport:\n\n" +
                "ID: " + transport.getId() + "\n" +
                "Vehicle: " + transport.getVehicleNumber() + "\n" +
                "Route: " + transport.getSourceCenter() + " → " + transport.getDestinationCenter() + "\n" +
                "Status: " + transport.getStatus() + "\n";
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transport");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText(message);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    TransportManagementService.deleteTransport(transport.getId());
                    refresh();
                    showSuccess("Transport deleted successfully!");
                } catch (SQLException e) {
                    showError("Failed to delete transport: " + e.getMessage());
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
        refreshVehicles();
        refreshTransports();
    }

    private void refreshVehicles() {
        try {
            List<TransportManagementService.VehicleInfo> vehicles = TransportManagementService.getAllVehicles();
            vehicleTable.getItems().setAll(vehicles);
        } catch (SQLException e) {
            showError("Failed to load vehicles: " + e.getMessage());
        }
    }

    private void refreshTransports() {
        try {
            List<TransportManagementService.TransportInfo> transports = TransportManagementService.getAllTransports();
            transportTable.getItems().setAll(transports);
        } catch (SQLException e) {
            showError("Failed to load transports: " + e.getMessage());
        }
    }

    public Node getRoot() {
        return root;
    }
}

