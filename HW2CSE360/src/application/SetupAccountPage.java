package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.DatabaseHelper;

/**
 * The SetupAccountPage class handles the setup process for creating a new user account.
 * It validates username, password, and invitation code before registration.
 */
public class SetupAccountPage {

    private final DatabaseHelper databaseHelper;

    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Create text fields for username, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setMaxWidth(250);

        // Labels to display validation errors
        Label userErrorLabel = new Label();
        userErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Label passwordErrorLabel = new Label();
        passwordErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Label invitationErrorLabel = new Label();
        invitationErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Setup button to validate inputs and register user
        Button setupButton = new Button("Setup");
        setupButton.setOnAction(e -> {
            // Clear old error messages
            userErrorLabel.setText("");
            passwordErrorLabel.setText("");
            invitationErrorLabel.setText("");

            String userName = userNameField.getText();
            String password = passwordField.getText();
            String inviteCode = inviteCodeField.getText();

            // Validate username
            String userNameCheck = UserNameRecognizer.checkForValidUserName(userName);
            if (!userNameCheck.isEmpty()) {
                userErrorLabel.setText("Invalid Username:\n" + userNameCheck);
            }

            // Validate password
            String passwordCheck = PasswordEvaluator.evaluatePassword(password);
            if (!passwordCheck.isEmpty()) {
                passwordErrorLabel.setText("Invalid Password:\n" + passwordCheck);
            }

            // Validate invitation code (check if it exists and not used)
            boolean codeValid = databaseHelper.validateInvitationCode(inviteCode);
            if (!codeValid) {
                invitationErrorLabel.setText("Please enter a valid invitation code.");
            }

            // If no errors so far, proceed to retrieve the role from the invitation code
            if (userErrorLabel.getText().isEmpty() &&
                passwordErrorLabel.getText().isEmpty() &&
                invitationErrorLabel.getText().isEmpty()) {

                // Retrieve the role from the invitation code
                String role = databaseHelper.getInvitationRole(inviteCode);
                if (role == null) {
                    // This means the code wasn't found or was invalid
                    invitationErrorLabel.setText("Please enter a valid invitation code.");
                    return;
                }

                // Register the user in the database
                User newUser = new User(userName, password, role);
                try {
                    databaseHelper.register(newUser);
                    System.out.println("User setup completed with role: " + role);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Security Notice");
                    alert.setHeaderText("Re-login Required");
                    alert.setContentText("As a security protocol, please log in again.");
                    alert.showAndWait();

                    // Redirect to login page
                    new UserLoginPage(databaseHelper).show(primaryStage);
                } catch (SQLException ex) {
                    System.err.println("Database error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // Layout and styling
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20, 50, 20, 50));
        layout.getChildren().addAll(
            userNameField,
            userErrorLabel,
            passwordField,
            passwordErrorLabel,
            inviteCodeField,
            invitationErrorLabel,
            setupButton
        );

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
