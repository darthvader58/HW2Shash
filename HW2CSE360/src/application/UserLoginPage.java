package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class UserLoginPage {
    private DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Main vertical layout
        VBox mainLayout = new VBox(15);
        // Adjust margins for a centered look
        mainLayout.setPadding(new Insets(20, 80, 20, 80)); 
        mainLayout.setAlignment(Pos.CENTER);

        // Horizontal layout for username + password
        HBox fieldsBox = new HBox(30);
        fieldsBox.setAlignment(Pos.CENTER);

        // Username label/field
        Label userNameLabel = new Label("Username:");
        userNameLabel.setWrapText(false);
        userNameLabel.setPrefWidth(80);
        userNameLabel.setAlignment(Pos.CENTER_RIGHT);

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setPrefWidth(200);

        // Password label/field
        Label passwordLabel = new Label("Password:");
        passwordLabel.setWrapText(false);
        passwordLabel.setPrefWidth(80);
        passwordLabel.setAlignment(Pos.CENTER_RIGHT);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setPrefWidth(200);

        // Add both label/field pairs to the horizontal box
        fieldsBox.getChildren().addAll(
            userNameLabel, userNameField,
            passwordLabel, passwordField
        );

        // Message label for status or errors
        Label messageLabel = new Label();

        // Login button
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String role = databaseHelper.getUserRole(userName);

            if (role == null) {
                messageLabel.setText("Invalid credentials.");
                return;
            }

            User user = new User(userName, password, role);
            try {
                if (databaseHelper.login(user)) {
                    new WelcomeLoginPage(databaseHelper, userName).show(primaryStage, user);
                } else {
                    messageLabel.setText("Invalid credentials.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Error logging in.");
            }
        });

        // Forgot Password button
        Button forgotPasswordButton = new Button("Forgot Password");
        forgotPasswordButton.setOnAction(e -> {
            String userName = userNameField.getText();
            if (userName == null || userName.isEmpty()) {
                messageLabel.setText("Please enter your username first.");
            } else {
                new AdminPasswordReset(databaseHelper, userName).show(primaryStage);
            }
        });

        // Quit button to return to setup/login selection page
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        // Add everything to the main vertical layout
        mainLayout.getChildren().addAll(
            fieldsBox,
            loginButton,
            forgotPasswordButton,
            quitButton,
            messageLabel
        );

        // Create and set the scene
        Scene scene = new Scene(mainLayout, 900, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
