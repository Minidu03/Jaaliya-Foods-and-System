package com.slginventory.ui;

import com.slginventory.algorithms.Dijkstra;
import com.slginventory.algorithms.Graph;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapPane {
    private final BorderPane root = new BorderPane();
    private final StackPane mapContainer = new StackPane();
    private final Pane canvas = new Pane();
    private final Map<String, double[]> nodePos = new HashMap<>(); // Normalized positions (0-1) relative to map image
    private Image mapImage;
    private ImageView mapImageView;
    private ImageView truckImageView;
    private Circle fallbackTruckCircle;
    private String currentTruckNode = "PETTAH";
    private final Label routeLabel = new Label();
    private final Graph graph;

    public MapPane() {
        this.graph = Graph.buildSriLankaCenterGraph();
        initializeCoordinates();
        build();
        drawGraph();
    }
    
    private void initializeCoordinates() {
        // Store normalized positions (0-1) for distribution centers on the map image
        // Format: [normalizedX, normalizedY] where 0,0 is top-left and 1,1 is bottom-right
        // Positions calibrated to match exact locations on the Sri Lanka map image
        
        // JAFFNA - Very top, northernmost point of the island (within dark green stripe)
        nodePos.put("JAFFNA", new double[]{0.50, 0.05});
        
        // DAMBULLA - Upper-middle section, slightly to the right of center (within orange stripe)
        nodePos.put("DAMBULLA", new double[]{0.58, 0.32});
        
        // KANDY - Central part of the island, below Dambulla (within orange stripe)
        nodePos.put("KANDY", new double[]{0.52, 0.42});
        
        // PELIYAGODA - Western side, below Kandy, slightly to the left (within dark green stripe)
        nodePos.put("PELIYAGODA", new double[]{0.22, 0.48});
        
        // PETTAH - Western side, below Peliyagoda (within dark green stripe)
        nodePos.put("PETTAH", new double[]{0.20, 0.52});
        
        // GALLE - Bottom, southern part, slightly to the right of center (within orange stripe)
        nodePos.put("GALLE", new double[]{0.48, 0.82});
    }
    
    
    private void build() {
        // Setup map container - transparent background with border
        mapContainer.getStyleClass().add("map-container");
        mapContainer.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        // Set fixed preferred size to prevent scrolling - map should fit exactly in viewport
        mapContainer.setPrefSize(900, 650);
        mapContainer.setMinSize(900, 650);
        mapContainer.setMaxSize(900, 650);
        
        // Try to load and add Sri Lanka map background
        try {
            mapImage = new Image(getClass().getResourceAsStream("/images/sri-lanka-map.png"));
            mapImageView = new ImageView(mapImage);
            
            // Bind map image to container size - fill container exactly to prevent scrolling
            mapImageView.fitWidthProperty().bind(mapContainer.widthProperty());
            mapImageView.fitHeightProperty().bind(mapContainer.heightProperty());
            mapImageView.setPreserveRatio(false); // Fill container exactly
            mapImageView.setOpacity(1.0); // Full opacity to show transparency of PNG
            mapImageView.setSmooth(true);
            mapContainer.getChildren().add(mapImageView);
            
            // Wait for image to load, then draw graph
            mapImage.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0) {
                    // Image loaded, draw graph with normalized positions
                    drawGraph();
                }
            });
            
            // If image already loaded, draw graph immediately
            if (mapImage.getProgress() >= 1.0) {
                drawGraph();
            }
            
            // Redraw graph when image view size changes (for responsive scaling)
            mapImageView.fitWidthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && mapImage.getProgress() >= 1.0) {
                    drawGraph();
                }
            });
            mapImageView.fitHeightProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && mapImage.getProgress() >= 1.0) {
                    drawGraph();
                }
            });
        } catch (Exception e) {
            // Map image not found, draw graph anyway with normalized positions
            System.err.println("Map image not found: " + e.getMessage());
            drawGraph();
        }
        
        // Setup canvas for drawing nodes, edges, and truck - bind to container size
        canvas.setStyle("-fx-background-color: transparent;");
        canvas.prefWidthProperty().bind(mapContainer.widthProperty());
        canvas.prefHeightProperty().bind(mapContainer.heightProperty());
        canvas.minWidthProperty().bind(mapContainer.widthProperty());
        canvas.minHeightProperty().bind(mapContainer.heightProperty());
        
        // Redraw graph when canvas size changes (for responsive scaling)
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                drawGraph();
            }
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                drawGraph();
            }
        });
        mapContainer.getChildren().add(canvas);
        
        // Load and setup truck icon
        try {
            Image truckImage = new Image(getClass().getResourceAsStream("/images/truck-icon.png"));
            truckImageView = new ImageView(truckImage);
            truckImageView.setFitWidth(24);
            truckImageView.setFitHeight(24);
            truckImageView.setPreserveRatio(true);
            truckImageView.setManaged(false);
            canvas.getChildren().add(truckImageView);
            // Initially position truck at PETTAH (default location)
            setTruckAt("PETTAH");
        } catch (Exception e) {
            // Truck icon not found, create a red circle as fallback
            fallbackTruckCircle = new Circle(12, Color.RED);
            fallbackTruckCircle.setStroke(Color.DARKRED);
            fallbackTruckCircle.setStrokeWidth(2);
            fallbackTruckCircle.setManaged(false);
            canvas.getChildren().add(fallbackTruckCircle);
            truckImageView = null;
            setTruckAt("PETTAH");
        }

        // Setup controls
        ComboBox<String> sourceCombo = new ComboBox<>();
        ComboBox<String> destCombo = new ComboBox<>();
        sourceCombo.getItems().addAll(nodePos.keySet());
        destCombo.getItems().addAll(nodePos.keySet());
        sourceCombo.setValue("PETTAH");
        destCombo.setValue("DAMBULLA");

        Label title = new Label("Sri Lanka Distribution Centers Map");
        title.getStyleClass().add("panel-title");

        routeLabel.getStyleClass().add("route-label");
        routeLabel.setStyle("-fx-font-size: 14px;");

        javafx.scene.control.Button findRouteBtn = new javafx.scene.control.Button("Find Shortest Path");
        findRouteBtn.setOnAction(e -> {
            String source = sourceCombo.getValue();
            String dest = destCombo.getValue();
            if (source != null && dest != null && !source.equals(dest)) {
                findAndAnimatePath(source, dest);
            }
        });

        javafx.scene.layout.HBox controls = new javafx.scene.layout.HBox(10,
                new Label("From:"), sourceCombo,
                new Label("To:"), destCombo,
                findRouteBtn);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        root.setTop(wrapCenter(title));
        root.setCenter(mapContainer);
        root.setBottom(new javafx.scene.layout.VBox(5, controls, wrapCenter(routeLabel)));
    }

    private Node wrapCenter(Node n) {
        javafx.scene.layout.HBox hb = new javafx.scene.layout.HBox(n);
        hb.setAlignment(Pos.CENTER);
        hb.setPadding(new Insets(10));
        return hb;
    }

    private void drawGraph() {
        // Get current canvas dimensions
        double canvasWidth = canvas.getWidth() > 0 ? canvas.getWidth() : 900;
        double canvasHeight = canvas.getHeight() > 0 ? canvas.getHeight() : 650;
        
        // Convert normalized positions to pixel coordinates
        Map<String, double[]> pixelPositions = new HashMap<>();
        for (Map.Entry<String, double[]> entry : nodePos.entrySet()) {
            String node = entry.getKey();
            double[] normalized = entry.getValue();
            double pixelX = normalized[0] * canvasWidth;
            double pixelY = normalized[1] * canvasHeight;
            pixelPositions.put(node, new double[]{pixelX, pixelY});
        }
        
        // Preserve truck when clearing
        Node truckNode = null;
        if (truckImageView != null && canvas.getChildren().contains(truckImageView)) {
            truckNode = truckImageView;
        } else if (fallbackTruckCircle != null && canvas.getChildren().contains(fallbackTruckCircle)) {
            truckNode = fallbackTruckCircle;
        }
        
        canvas.getChildren().clear();
        
        // Re-add truck if it existed
        if (truckNode != null) {
            canvas.getChildren().add(truckNode);
        }

        // Draw edges (avoid duplicates for undirected graph)
        Set<String> drawnEdges = new HashSet<>();
        for (String from : graph.getAdjacency().keySet()) {
            for (Graph.Edge edge : graph.getAdjacency().get(from)) {
                String to = edge.to;
                // Create a canonical edge identifier to avoid drawing the same edge twice
                String edgeKey = from.compareTo(to) < 0 ? from + ":" + to : to + ":" + from;
                if (!drawnEdges.contains(edgeKey) && pixelPositions.containsKey(from) && pixelPositions.containsKey(to)) {
                    drawnEdges.add(edgeKey);
                    double[] start = pixelPositions.get(from);
                    double[] end = pixelPositions.get(to);
                    // Use positions directly (already scaled)
                    Line line = new Line(start[0], start[1], end[0], end[1]);
                    line.setStroke(Color.GRAY);
                    line.setStrokeWidth(2);
                    line.getStyleClass().add("map-edge");
                    canvas.getChildren().add(line);

                    // Distance label
                    double midX = (start[0] + end[0]) / 2;
                    double midY = (start[1] + end[1]) / 2;
                    Text distText = new Text(midX, midY, String.valueOf((int)edge.weightKm + "km"));
                    distText.setStyle("-fx-font-size: 10px; -fx-fill: #666;");
                    canvas.getChildren().add(distText);
                }
            }
        }

        // Draw nodes
        for (Map.Entry<String, double[]> entry : pixelPositions.entrySet()) {
            String node = entry.getKey();
            double[] pos = entry.getValue();
            
            // Calculate node size based on canvas size (responsive)
            double nodeSize = Math.max(8, Math.min(15, Math.min(canvasWidth, canvasHeight) / 50));
            Circle circle = new Circle(nodeSize, Color.LIGHTBLUE);
            circle.setStroke(Color.DARKBLUE);
            circle.setStrokeWidth(2);
            circle.setCenterX(pos[0]);
            circle.setCenterY(pos[1]);
            circle.getStyleClass().add("map-node");
            
            // Add tooltip with center information
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                "Distribution Center: " + node + "\nClick to view details"
            );
            tooltip.setStyle("-fx-font-size: 12px;");
            javafx.scene.control.Tooltip.install(circle, tooltip);
            
            // Add click handler
            circle.setOnMouseClicked(e -> {
                // Could show center details dialog here
                System.out.println("Clicked on: " + node);
            });
            
            // Add hover effect
            circle.setOnMouseEntered(e -> {
                circle.setFill(Color.web("#2980b9"));
                circle.setStroke(Color.web("#1f5f8b"));
                circle.setStrokeWidth(3);
            });
            
            circle.setOnMouseExited(e -> {
                circle.setFill(Color.LIGHTBLUE);
                circle.setStroke(Color.DARKBLUE);
                circle.setStrokeWidth(2);
            });
            
            // Position label below the node
            double labelFontSize = Math.max(10, Math.min(14, Math.min(canvasWidth, canvasHeight) / 60));
            Text label = new Text(pos[0] - 20, pos[1] + nodeSize + 15, node);
            label.setStyle("-fx-font-weight: bold; -fx-font-size: " + labelFontSize + "px; -fx-fill: #2c3e50;");
            
            canvas.getChildren().addAll(circle, label);
        }
        
        // Reposition truck according to current scale and ensure it stays on top
        if (currentTruckNode != null) {
            setTruckAt(currentTruckNode);
        }
        if (truckNode != null) {
            canvas.getChildren().remove(truckNode);
            canvas.getChildren().add(truckNode);
            truckNode.toFront();
        }
    }

    private void findAndAnimatePath(String source, String dest) {
        var result = Dijkstra.shortestPaths(graph, source);
        List<String> path = result.reconstructPath(dest);
        
        if (path.size() > 1) {
            Double distance = result.distanceKm.get(dest);
            if (distance != null && distance != Double.POSITIVE_INFINITY) {
                routeLabel.setText(String.format("Shortest route: %s (%.1f km)", 
                    String.join(" → ", path), distance));
                animateTruck(path, graph);
            } else {
                routeLabel.setText("No path found!");
            }
        } else {
            routeLabel.setText("No path found!");
        }
    }

    public void animateTruck(List<String> path, Graph g) {
        if (path == null || path.size() < 2) return;

        // Ensure truck is visible
        if (truckImageView != null && !canvas.getChildren().contains(truckImageView)) {
            canvas.getChildren().add(truckImageView);
        } else if (fallbackTruckCircle != null && !canvas.getChildren().contains(fallbackTruckCircle)) {
            canvas.getChildren().add(fallbackTruckCircle);
        }
        setTruckAt(path.get(0));

        Timeline timeline = new Timeline();
        Duration time = Duration.ZERO;

        for (int i = 0; i < path.size() - 1; i++) {
            String start = path.get(i);
            String end = path.get(i + 1);

            Duration segment = Duration.millis(50 * g.getWeight(start, end));
            time = time.add(segment);

            // Get current canvas dimensions for pixel conversion
            double canvasWidth = canvas.getWidth() > 0 ? canvas.getWidth() : 900;
            double canvasHeight = canvas.getHeight() > 0 ? canvas.getHeight() : 650;
            double[] normalizedPos = nodePos.get(end);
            double[] endPos = new double[]{normalizedPos[0] * canvasWidth, normalizedPos[1] * canvasHeight};
            double scaleFactor = Math.min(canvasWidth, canvasHeight) / 600.0;
            double truckPixelSize = Math.max(18, 24 * scaleFactor);
            double truckOffset = truckPixelSize / 2.0;
            
            if (truckImageView != null) {
                KeyValue kvX = new KeyValue(truckImageView.layoutXProperty(), endPos[0] - truckOffset);
                KeyValue kvY = new KeyValue(truckImageView.layoutYProperty(), endPos[1] - truckOffset);
                KeyFrame kf = new KeyFrame(time, kvX, kvY);
                timeline.getKeyFrames().add(kf);
            } else if (fallbackTruckCircle != null) {
                double radius = Math.max(6, 12 * scaleFactor);
                KeyValue kvRadius = new KeyValue(fallbackTruckCircle.radiusProperty(), radius);
                KeyValue kvX = new KeyValue(fallbackTruckCircle.centerXProperty(), endPos[0]);
                KeyValue kvY = new KeyValue(fallbackTruckCircle.centerYProperty(), endPos[1]);
                KeyFrame kf = new KeyFrame(time, kvRadius, kvX, kvY);
                timeline.getKeyFrames().add(kf);
            }
        }
        String finalNode = path.get(path.size() - 1);
        timeline.setOnFinished(evt -> setTruckAt(finalNode));
        
        timeline.play();
    }

    private void setTruckAt(String node) {
        if (!nodePos.containsKey(node)) {
            return;
        }
        currentTruckNode = node;

        // Get current canvas dimensions
        double canvasWidth = canvas.getWidth() > 0 ? canvas.getWidth() : 900;
        double canvasHeight = canvas.getHeight() > 0 ? canvas.getHeight() : 650;
        // Convert normalized position to pixel coordinates
        double[] normalizedPos = nodePos.get(node);
        double[] pos = new double[]{normalizedPos[0] * canvasWidth, normalizedPos[1] * canvasHeight};
        double scaleFactor = Math.min(canvasWidth, canvasHeight) / 600.0;

        if (truckImageView != null) {
            double truckPixelSize = Math.max(18, 24 * scaleFactor);
            double offset = truckPixelSize / 2.0;
            truckImageView.setFitWidth(truckPixelSize);
            truckImageView.setFitHeight(truckPixelSize);
            truckImageView.setLayoutX(pos[0] - offset);
            truckImageView.setLayoutY(pos[1] - offset);
        } else if (fallbackTruckCircle != null) {
            double radius = Math.max(6, 12 * scaleFactor);
            fallbackTruckCircle.setRadius(radius);
            fallbackTruckCircle.setCenterX(pos[0]);
            fallbackTruckCircle.setCenterY(pos[1]);
        }
    }

    public Node getRoot() {
        return root;
    }
}
