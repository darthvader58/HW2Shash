package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.sql.SQLException;
import databasePart1.DatabaseHelper;
import javafx.scene.layout.HBox;

/**
 * The AdminSetupPage class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        Label userError = new Label();
        userError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Label passwordError = new Label();
        passwordError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Setup");
        setupButton.setOnAction(a -> {
            userError.setText("");
            passwordError.setText("");
            
            String userName = userNameField.getText();
            String password = passwordField.getText();

            try {
                if (UserNameRecognizer.checkForValidUserName(userName).length() > 0){
                    userError.setText("Invalid Username:\n" + UserNameRecognizer.checkForValidUserName(userName));
                }
                if (PasswordEvaluator.evaluatePassword(password).length() > 0) {
                    passwordError.setText("Invalid Password:\n" + PasswordEvaluator.evaluatePassword(password));
                }
                if (userError.getText().isEmpty() && passwordError.getText().isEmpty()) { 
                    User user = new User(userName, password, "admin");
                    databaseHelper.register(user);
                    System.out.println("Administrator setup completed.");
               
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Security Notice");
                    alert.setHeaderText("Re-login Required");
                    alert.setContentText("As a security protocol, please log in again.");
                    alert.showAndWait();

                    new UserLoginPage(databaseHelper).show(primaryStage);
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        userError.setMaxWidth(250);
        passwordError.setMaxWidth(250);

        HBox userErrorBox = new HBox(userError);
        userErrorBox.setAlignment(Pos.CENTER_LEFT);
        userErrorBox.setMaxWidth(250);

        HBox passwordErrorBox = new HBox(passwordError);
        passwordErrorBox.setAlignment(Pos.CENTER_LEFT);
        passwordErrorBox.setMaxWidth(250);
        
        VBox.setMargin(userNameField, new Insets(45, 0, 0, 0));

        VBox layout = new VBox(10, 
            userNameField, 
            userErrorBox, 
            passwordField, 
            passwordErrorBox, 
            setupButton
        );
        layout.setAlignment(Pos.TOP_CENTER); 
        layout.setStyle("-fx-padding: 20;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
