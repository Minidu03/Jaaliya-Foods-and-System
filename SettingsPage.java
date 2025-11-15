package com.slginventory.ui;

import com.slginventory.auth.PasswordHasher;
import com.slginventory.db.Database;
import com.slginventory.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SettingsPage {
    private final VBox root = new VBox();
    private final Stage stage;
    private final User user;

    public SettingsPage(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        build();
    }

    private void build() {
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("settings-page");

        Label title = new Label("⚙️ User Account Settings");
        title.getStyleClass().add("page-title");
        root.getChildren().add(title);

        // Account Information Card
        VBox accountCard = createCard("Account Information");
        GridPane accountGrid = new GridPane();
        accountGrid.setHgap(15);
        accountGrid.setVgap(15);
        accountGrid.setPadding(new Insets(15));

        accountGrid.add(new Label("Username:"), 0, 0);
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.getStyleClass().add("settings-value");
        accountGrid.add(usernameLabel, 1, 0);

        accountGrid.add(new Label("Company:"), 0, 1);
        Label companyLabel = new Label(user.getCompany());
        companyLabel.getStyleClass().add("settings-value");
        accountGrid.add(companyLabel, 1, 1);

        accountCard.getChildren().add(accountGrid);
        root.getChildren().add(accountCard);

        // Change Password Card
        VBox passwordCard = createCard("Change Password");
        VBox passwordBox = new VBox(15);
        passwordBox.setPadding(new Insets(15));

        Label passwordInfo = new Label("Update your account password. Password must be at least 6 characters.");
        passwordInfo.getStyleClass().add("info-text");
        passwordInfo.setWrapText(true);

        Button changePasswordBtn = new Button("🔒 Change Password");
        changePasswordBtn.getStyleClass().add("button");
        changePasswordBtn.setOnAction(e -> showChangePasswordDialog());

        passwordBox.getChildren().addAll(passwordInfo, changePasswordBtn);
        passwordCard.getChildren().add(passwordBox);
        root.getChildren().add(passwordCard);

        // Preferences Card
        VBox preferencesCard = createCard("Preferences");
        VBox prefsBox = new VBox(15);
        prefsBox.setPadding(new Insets(15));

        Label themeLabel = new Label("Theme:");
        themeLabel.getStyleClass().add("settings-label");

        ToggleGroup themeGroup = new ToggleGroup();
        RadioButton darkTheme = new RadioButton("Dark Theme");
        RadioButton lightTheme = new RadioButton("Light Theme");
        darkTheme.setToggleGroup(themeGroup);
        lightTheme.setToggleGroup(themeGroup);
        darkTheme.setSelected(true); // Default

        HBox themeBox = new HBox(15, darkTheme, lightTheme);
        themeBox.setAlignment(Pos.CENTER_LEFT);

        Label langLabel = new Label("Language:");
        langLabel.getStyleClass().add("settings-label");

        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("English", "Sinhala", "Tamil");
        langCombo.setValue("English");

        prefsBox.getChildren().addAll(themeLabel, themeBox, langLabel, langCombo);
        preferencesCard.getChildren().add(prefsBox);
        root.getChildren().add(preferencesCard);

        // Danger Zone
        VBox dangerCard = createCard("Danger Zone");
        VBox dangerBox = new VBox(15);
        dangerBox.setPadding(new Insets(15));

        Label dangerInfo = new Label("⚠️ These actions cannot be undone. Please proceed with caution.");
        dangerInfo.getStyleClass().add("danger-text");
        dangerInfo.setWrapText(true);

        Button deleteAccountBtn = new Button("🗑️ Delete Account");
        deleteAccountBtn.getStyleClass().add("button-danger");
        deleteAccountBtn.setOnAction(e -> showDeleteAccountConfirmation());

        dangerBox.getChildren().addAll(dangerInfo, deleteAccountBtn);
        dangerCard.getChildren().add(dangerBox);
        root.getChildren().add(dangerCard);
    }

    private VBox createCard(String title) {
        VBox card = new VBox(10);
        card.getStyleClass().add("info-box");
        card.setPadding(new Insets(20));

        Label cardTitle = new Label(title);
        cardTitle.getStyleClass().add("info-box-title");
        card.getChildren().add(cardTitle);

        return card;
    }

    private void showChangePasswordDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Change Password");

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(400);

        Label title = new Label("🔒 Change Password");
        title.getStyleClass().add("dialog-title");
        root.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 20, 0));

        Label currentLabel = new Label("Current Password:");
        currentLabel.getStyleClass().add("dialog-label");
        PasswordField currentField = new PasswordField();
        currentField.setPromptText("Enter current password");

        Label newLabel = new Label("New Password:");
        newLabel.getStyleClass().add("dialog-label");
        PasswordField newField = new PasswordField();
        newField.setPromptText("Enter new password (min 6 characters)");

        Label confirmLabel = new Label("Confirm Password:");
        confirmLabel.getStyleClass().add("dialog-label");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm new password");

        grid.add(currentLabel, 0, 0);
        grid.add(currentField, 1, 0);
        grid.add(newLabel, 0, 1);
        grid.add(newField, 1, 1);
        grid.add(confirmLabel, 0, 2);
        grid.add(confirmField, 1, 2);

        root.getChildren().add(grid);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("dialog-status");
        root.getChildren().add(statusLabel);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Change Password");
        saveBtn.getStyleClass().add("button");
        saveBtn.setOnAction(e -> {
            String current = currentField.getText();
            String newPass = newField.getText();
            String confirm = confirmField.getText();

            statusLabel.getStyleClass().removeAll("status-error", "status-success");

            // Validate current password
            try (Connection conn = Database.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT password_hash FROM users WHERE id = ?")) {
                ps.setInt(1, user.getId());
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String hash = rs.getString("password_hash");
                        if (!PasswordHasher.verify(current, hash)) {
                            statusLabel.setText("❌ Current password is incorrect");
                            statusLabel.getStyleClass().add("status-error");
                            return;
                        }
                    }
                }
            } catch (SQLException ex) {
                statusLabel.setText("❌ Error: " + ex.getMessage());
                statusLabel.getStyleClass().add("status-error");
                return;
            }

            if (newPass.length() < 6) {
                statusLabel.setText("❌ New password must be at least 6 characters");
                statusLabel.getStyleClass().add("status-error");
                return;
            }

            if (!newPass.equals(confirm)) {
                statusLabel.setText("❌ New passwords do not match");
                statusLabel.getStyleClass().add("status-error");
                return;
            }

            // Update password
            try (Connection conn = Database.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
                ps.setString(1, PasswordHasher.hash(newPass));
                ps.setInt(2, user.getId());
                ps.executeUpdate();

                statusLabel.setText("✅ Password changed successfully!");
                statusLabel.getStyleClass().add("status-success");

                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(evt -> dialog.close());
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
        dialog.showAndWait();
    }

    private void showDeleteAccountConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Confirm Account Deletion");
        alert.setContentText("⚠️ WARNING: This will permanently delete your account and all associated data including:\n\n" +
                "• Your vendor profile\n" +
                "• All vendor inventory\n" +
                "• All account settings\n\n" +
                "This action CANNOT be undone!\n\n" +
                "Are you absolutely sure you want to delete your account?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Additional confirmation
                Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
                confirmAlert.setTitle("Final Confirmation");
                confirmAlert.setHeaderText("Last Chance!");
                confirmAlert.setContentText("This is your last chance to cancel. Your account will be permanently deleted. Continue?");
                confirmAlert.showAndWait().ifPresent(confirmResponse -> {
                    if (confirmResponse == ButtonType.OK) {
                        try (Connection conn = Database.getInstance().getConnection();
                             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                            ps.setInt(1, user.getId());
                            ps.executeUpdate();

                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Account Deleted");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Your account has been deleted. The application will now close.");
                            successAlert.showAndWait();

                            // Return to login
                            com.slginventory.ui.LoginView loginView = new com.slginventory.ui.LoginView(stage);
                            Scene loginScene = new Scene(loginView.getRoot(), 1100, 720);
                            loginScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                            stage.setScene(loginScene);
                        } catch (SQLException e) {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Error");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Failed to delete account: " + e.getMessage());
                            errorAlert.showAndWait();
                        }
                    }
                });
            }
        });
    }

    public Node getRoot() {
        return root;
    }
}

