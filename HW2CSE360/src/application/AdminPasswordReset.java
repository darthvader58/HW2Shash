package application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class AdminPasswordReset {
    private Stage stage;
    private DatabaseHelper databaseHelper;
    private String userName;  // using userName instead of userId
    private Runnable onPasswordReset; // Callback for returning to login page

    public AdminPasswordReset(DatabaseHelper databaseHelper, String userName) {
        this.databaseHelper = databaseHelper;
        this.userName = userName;
        this.stage = new Stage();
    }

    public void show(Stage primaryStage) {
        Label instructionLabel = new Label("Enter your new password:");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        Button resetButton = new Button("Reset Password");
        Label messageLabel = new Label();

        resetButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();

            if (newPassword.isEmpty()) {
                messageLabel.setText("Password cannot be empty.");
                return;
            }

            boolean success = databaseHelper.setPassword(userName, newPassword);
            if (success) {
                messageLabel.setText("Password reset successfully!");
                databaseHelper.setForgetPassword(userName);
                if (onPasswordReset != null) {
                    onPasswordReset.run();
                    stage.close();
                }
            } else {
                messageLabel.setText("Failed to reset password.");
            }
        });

        VBox layout = new VBox(10, instructionLabel, newPasswordField, resetButton, messageLabel);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 300, 200);
        stage.setScene(scene);
        stage.setTitle("Reset Password");
        stage.show();
    }

    public void setOnPasswordReset(Runnable onPasswordReset) {
        this.onPasswordReset = onPasswordReset;
    }
}
