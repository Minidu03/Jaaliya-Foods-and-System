package com.slginventory.ui;

import com.slginventory.auth.AuthService;
import com.slginventory.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginView {
    private final BorderPane root = new BorderPane();
    private final AuthService authService = new AuthService();
    private final Stage stage;
    private final StackPane backgroundPane = new StackPane();
    private Font lexendRegular;
    private Font lexendSemiBold;
    private Font lexendBold;

    public LoginView(Stage stage) {
        this.stage = stage;
        build();
    }

    private void build() {
        // Prefer Lexend font if available
        loadLexendFonts();

        // Create animated background with GIF image
        setupAnimatedBackground();
        
        // Create content pane with semi-transparent overlay
        VBox contentPane = new VBox(20);
        contentPane.setAlignment(Pos.CENTER);
        contentPane.setPadding(new Insets(45));
        contentPane.setStyle(String.join("",
            "-fx-background-color: linear-gradient(to bottom right, rgba(255,255,255,0.85), rgba(200, 255, 210, 0.78));",
            "-fx-background-radius: 24;",
            "-fx-border-radius: 24;",
            "-fx-border-color: rgba(46, 125, 50, 0.7);",
            "-fx-border-width: 2.5;"
        ));
        contentPane.setEffect(new DropShadow(30, Color.rgb(27, 94, 32, 0.45)));
        contentPane.setMaxWidth(500);
        contentPane.setMaxHeight(600);
        
        // Add logo if available
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/images/app-logo.png"));
            ImageView logoView = new ImageView(logoImage);
            logoView.setFitWidth(120);
            logoView.setFitHeight(120);
            logoView.setPreserveRatio(true);
            
            // Add rotation animation to logo
            RotateTransition rotate = new RotateTransition(Duration.seconds(3), logoView);
            rotate.setByAngle(360);
            rotate.setCycleCount(RotateTransition.INDEFINITE);
            rotate.setAutoReverse(false);
            rotate.play();
            
            HBox logoBox = new HBox(logoView);
            logoBox.setAlignment(Pos.CENTER);
            contentPane.getChildren().add(logoBox);
        } catch (Exception e) {
            // Logo not found, continue without it
        }
        
        Label title = new Label("Jaaliya Foods");
        title.setFont(lexendBold != null ? Font.font(lexendBold.getFamily(), 38) : Font.font("Arial", FontWeight.BOLD, 38));
        title.setStyle("-fx-text-fill: #1b5e20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 3, 0, 0, 1);");
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        contentPane.getChildren().add(titleBox);

        Label subtitle = new Label("Anuradhapura District • Sri Lanka");
        subtitle.setFont(lexendSemiBold != null ? Font.font(lexendSemiBold.getFamily(), 16) : Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        subtitle.setStyle("-fx-text-fill: #33691e;");
        HBox subtitleBox = new HBox(subtitle);
        subtitleBox.setAlignment(Pos.CENTER);
        contentPane.getChildren().add(subtitleBox);

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: transparent; -fx-tab-min-height: 38; -fx-tab-min-width: 140; -fx-text-base-color: #1b5e20;");
        Tab loginTab = new Tab("Login", buildLoginPane());
        loginTab.setClosable(false);
        Tab registerTab = new Tab("Register", buildRegisterPane());
        registerTab.setClosable(false);
        tabs.getTabs().addAll(loginTab, registerTab);
        contentPane.getChildren().add(tabs);
        
        // Center the content pane
        StackPane centerPane = new StackPane();
        centerPane.getChildren().add(contentPane);
        centerPane.setAlignment(Pos.CENTER);
        
        backgroundPane.getChildren().add(centerPane);
        // Make background pane fill available space
        backgroundPane.prefWidthProperty().bind(root.widthProperty());
        backgroundPane.prefHeightProperty().bind(root.heightProperty());
        root.setCenter(backgroundPane);
        root.setPadding(new Insets(0));
        
        // Add copyright notice at the bottom - opacity matches background
        Label copyrightLabel = new Label("@ Copyrights reserved CMSD Solutions");
        if (lexendRegular != null) {
            copyrightLabel.setFont(Font.font(lexendRegular.getFamily(), 12));
        }
        copyrightLabel.setStyle("-fx-text-fill: #000000; -fx-font-size: 12.5px;");
        HBox copyrightBox = new HBox(copyrightLabel);
        copyrightBox.setAlignment(Pos.CENTER);
        copyrightBox.setPadding(new Insets(10));
        // Match the content pane's translucent look
        copyrightBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.72);");
        copyrightBox.setPrefHeight(36);
        root.setBottom(copyrightBox);
    }
    
    private void setupAnimatedBackground() {
        // Try to load agricultural GIF/image as background (supports multiple common names)
        ImageView gifImageView = null;
        String[] possibleGifNames = {
            "/images/agriculture.gif.gif",
            "/images/agriculture.gif",
            "/images/background.gif",
            "/images/login-bg.gif",
            "/images/farming.gif",
            "/images/agricultural.gif"
        };
        
        for (String gifPath : possibleGifNames) {
            try {
                Image gifImage = new Image(getClass().getResourceAsStream(gifPath));
                gifImageView = new ImageView(gifImage);
                // Bind to parent size for responsive scaling
                gifImageView.fitWidthProperty().bind(backgroundPane.widthProperty());
                gifImageView.fitHeightProperty().bind(backgroundPane.heightProperty());
                gifImageView.setPreserveRatio(false);
                gifImageView.setSmooth(true);
                // Add semi-transparent overlay for better text readability
                gifImageView.setOpacity(0.7);
                backgroundPane.getChildren().add(0, gifImageView);
                break; // Successfully loaded, exit loop
            } catch (Exception e) {
                // Try next image name
                continue;
            }
        }
        
        // If no GIF found, use gradient background as fallback
        if (gifImageView == null) {
            BackgroundFill bgFill = new BackgroundFill(
                new javafx.scene.paint.LinearGradient(0, 0, 1, 1, true, null,
                    new javafx.scene.paint.Stop(0, Color.web("#E8F5E9")),
                    new javafx.scene.paint.Stop(0.5, Color.web("#C8E6C9")),
                    new javafx.scene.paint.Stop(1, Color.web("#A5D6A7"))
                ),
                null, null
            );
            backgroundPane.setBackground(new Background(bgFill));
        } else {
            // Set a subtle background color behind the GIF
            BackgroundFill bgFill = new BackgroundFill(
                Color.web("#F1F8E9"),
                null, null
            );
            backgroundPane.setBackground(new Background(bgFill));
        }
        
        // Add floating animated circles for extra visual appeal
        for (int i = 0; i < 6; i++) {
            Circle circle = new Circle(20 + Math.random() * 40);
            circle.setFill(Color.web("rgba(76, 175, 80, 0.15)"));
            circle.setStroke(Color.web("rgba(76, 175, 80, 0.25)"));
            circle.setStrokeWidth(1.5);
            circle.setManaged(false);

            final double factorX = Math.random();
            final double factorY = Math.random();

            backgroundPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                circle.setLayoutX(newVal.doubleValue() * factorX);
            });
            backgroundPane.heightProperty().addListener((obs, oldVal, newVal) -> {
                circle.setLayoutY(newVal.doubleValue() * factorY);
            });

            // Set initial position once layout bounds are available
            circle.setLayoutX(backgroundPane.getWidth() * factorX);
            circle.setLayoutY(backgroundPane.getHeight() * factorY);
            
            // Animate circles floating
            TranslateTransition translate = new TranslateTransition(Duration.seconds(4 + Math.random() * 3), circle);
            translate.setByX(Math.random() * 80 - 40);
            translate.setByY(Math.random() * 80 - 40);
            translate.setCycleCount(javafx.animation.Animation.INDEFINITE);
            translate.setAutoReverse(true);
            translate.play();
            
            // Fade animation
            FadeTransition fade = new FadeTransition(Duration.seconds(2 + Math.random() * 2), circle);
            fade.setFromValue(0.2);
            fade.setToValue(0.6);
            fade.setCycleCount(javafx.animation.Animation.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();
            
            backgroundPane.getChildren().add(circle);
        }
    }

    private GridPane buildLoginPane() {
        TextField username = new TextField();
        username.setPromptText("Enter your username");
        username.setPrefWidth(250);
        username.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
        PasswordField password = new PasswordField();
        password.setPromptText("Enter your password");
        password.setPrefWidth(250);
        password.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
        Label status = new Label();
        status.setStyle("-fx-text-fill: #aa0000; -fx-font-size: 12px;");

        Button login = new Button("Login");
        login.setDefaultButton(true);
        login.setPrefWidth(250);
        if (lexendSemiBold != null) {
            login.setStyle("-fx-font-family: '" + lexendSemiBold.getFamily() + "';");
        }
        login.setOnAction(e -> {
            String usernameText = username.getText().trim();
            String passwordText = password.getText();
            if (usernameText.isEmpty() || passwordText.isEmpty()) {
                status.setText("Please enter username and password");
                return;
            }
            User user = authService.login(usernameText, passwordText);
            if (user != null) {
                DashboardView dashboardView = new DashboardView(stage, user);
                Scene dashboardScene = new Scene(dashboardView.getRoot(), 1280, 800);
                dashboardScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(dashboardScene);
            } else {
                status.setText("Invalid credentials");
            }
        });

        GridPane gp = new GridPane();
        gp.setVgap(15);
        gp.setHgap(15);
        gp.setPadding(new Insets(30, 20, 20, 20));
        gp.setAlignment(Pos.CENTER);
        int r = 0;
        
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1b5e20;");
        if (lexendSemiBold != null) {
            userLabel.setFont(Font.font(lexendSemiBold.getFamily(), 14));
        }
        gp.add(userLabel, 0, r); 
        gp.add(username, 1, r++);
        
        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1b5e20;");
        if (lexendSemiBold != null) {
            passLabel.setFont(Font.font(lexendSemiBold.getFamily(), 14));
        }
        gp.add(passLabel, 0, r); 
        gp.add(password, 1, r++);
        
        gp.add(login, 1, r++);
        gp.add(status, 1, r);
        return gp;
    }

    private ScrollPane buildRegisterPane() {
        TextField username = new TextField();
        username.setPromptText("Choose a username");
        username.setPrefWidth(250);
        username.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
        PasswordField password = new PasswordField();
        password.setPromptText("At least 6 characters");
        password.setPrefWidth(250);
        password.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
        TextField company = new TextField();
        company.setPromptText("Your company name");
        company.setPrefWidth(250);
        company.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
        TextField vendorName = new TextField();
        vendorName.setPromptText("Vendor name");
        vendorName.setPrefWidth(250);
        vendorName.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
        TextField phone = new TextField();
        phone.setPromptText("Phone number");
        phone.setPrefWidth(250);
        phone.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
        Label status = new Label();
        status.setStyle("-fx-font-size: 12px;");

        Button register = new Button("Create account");
        register.setPrefWidth(250);
        register.setOnAction(e -> {
            String usernameText = username.getText().trim();
            String passwordText = password.getText();
            String companyText = company.getText().trim();
            String vendorNameText = vendorName.getText().trim();
            String phoneText = phone.getText().trim();
            
            if (usernameText.isEmpty() || passwordText.isEmpty() || 
                companyText.isEmpty() || vendorNameText.isEmpty() || phoneText.isEmpty()) {
                status.setStyle("-fx-text-fill: #aa0000;");
                status.setText("Please fill all fields");
                return;
            }
            
            if (passwordText.length() < 6) {
                status.setStyle("-fx-text-fill: #aa0000;");
                status.setText("Password must be at least 6 characters");
                return;
            }
            
            boolean ok = authService.register(usernameText, passwordText, companyText, vendorNameText, phoneText);
            if (ok) {
                status.setStyle("-fx-text-fill: #006b00;");
                status.setText("Registered. Please login.");
                // Clear form
                username.clear();
                password.clear();
                company.clear();
                vendorName.clear();
                phone.clear();
            } else {
                status.setStyle("-fx-text-fill: #aa0000;");
                status.setText("Username already exists.");
            }
        });

        GridPane gp = new GridPane();
        gp.setVgap(12);
        gp.setHgap(15);
        gp.setPadding(new Insets(20));
        gp.setAlignment(Pos.CENTER);
        int r = 0;
        
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1b5e20;");
        if (lexendSemiBold != null) {
            userLabel.setFont(Font.font(lexendSemiBold.getFamily(), 14));
        }
        gp.add(userLabel, 0, r); gp.add(username, 1, r++);
        
        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1b5e20;");
        if (lexendSemiBold != null) {
            passLabel.setFont(Font.font(lexendSemiBold.getFamily(), 14));
        }
        gp.add(passLabel, 0, r); gp.add(password, 1, r++);
        
        Label compLabel = new Label("Company:");
        compLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1b5e20;");
        if (lexendSemiBold != null) {
            compLabel.setFont(Font.font(lexendSemiBold.getFamily(), 14));
        }
        gp.add(compLabel, 0, r); gp.add(company, 1, r++);
        
        Label vendorLabel = new Label("Vendor Name:");
        vendorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1b5e20;");
        if (lexendSemiBold != null) {
            vendorLabel.setFont(Font.font(lexendSemiBold.getFamily(), 14));
        }
        gp.add(vendorLabel, 0, r); gp.add(vendorName, 1, r++);
        
        Label phoneLabel = new Label("Phone:");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1b5e20;");
        if (lexendSemiBold != null) {
            phoneLabel.setFont(Font.font(lexendSemiBold.getFamily(), 14));
        }
        gp.add(phoneLabel, 0, r); gp.add(phone, 1, r++);
        
        gp.add(register, 1, r++);
        gp.add(status, 1, r);
        
        // Wrap GridPane in ScrollPane to enable scrolling
        ScrollPane scrollPane = new ScrollPane(gp);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false); // Allow content to be taller than viewport for scrolling
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0));
        // Ensure GridPane can expand vertically
        gp.setMinHeight(Region.USE_PREF_SIZE);
        return scrollPane;
    }

    public BorderPane getRoot() {
        return root;
    }

    private void loadLexendFonts() {
        try {
            // Try load from resources if provided by user: src/main/resources/fonts
            lexendRegular = Font.loadFont(getClass().getResourceAsStream("/fonts/Lexend-Regular.ttf"), 14);
            lexendSemiBold = Font.loadFont(getClass().getResourceAsStream("/fonts/Lexend-SemiBold.ttf"), 14);
            lexendBold = Font.loadFont(getClass().getResourceAsStream("/fonts/Lexend-Bold.ttf"), 14);
        } catch (Exception ignore) { }

        // If not bundled, try remote Google Fonts sources (runtime download)
        try {
            if (lexendRegular == null) {
                lexendRegular = Font.loadFont("https://raw.githubusercontent.com/google/fonts/main/ofl/lexend/static/Lexend-Regular.ttf", 14);
            }
        } catch (Exception ignore) { }
        try {
            if (lexendSemiBold == null) {
                lexendSemiBold = Font.loadFont("https://raw.githubusercontent.com/google/fonts/main/ofl/lexend/static/Lexend-SemiBold.ttf", 14);
            }
        } catch (Exception ignore) { }
        try {
            if (lexendBold == null) {
                lexendBold = Font.loadFont("https://raw.githubusercontent.com/google/fonts/main/ofl/lexend/static/Lexend-Bold.ttf", 14);
            }
        } catch (Exception ignore) { }
    }
}
