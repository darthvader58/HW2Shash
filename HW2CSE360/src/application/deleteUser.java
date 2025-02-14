package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * deleteUser class represents the page where an admin can remove a user by entering their username.
 */
public class deleteUser {

    private DatabaseHelper databaseHelper;
    private Stage primaryStage;
    private String adminUserName;  // using adminUserName only

    // Constructor name must match the class name
    public deleteUser(DatabaseHelper databaseHelper, Stage primaryStage, String adminUserName) {
        this.databaseHelper = databaseHelper;
        this.primaryStage = primaryStage;
        this.adminUserName = adminUserName;
    }

    public void show() {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        Label titleLabel = new Label("Remove User");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(200);

        Button removeButton = new Button("Remove User");
        removeButton.setStyle("-fx-font-size: 14px;");

        removeButton.setOnAction(event -> {
            String userName = userNameField.getText();
            String userRole = databaseHelper.getUserRole(userName);
            if ("admin".equalsIgnoreCase(userRole)) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Cannot Remove Admin");
                errorAlert.setContentText("You cannot remove a user with the Admin role.");
                errorAlert.showAndWait();
                return;
            }

            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Confirm Removal");
            confirmationDialog.setHeaderText("Are you sure you want to remove this user?");
            confirmationDialog.setContentText("Username: " + userName);

            confirmationDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean isRemoved = databaseHelper.removeUser(userName);
                    if (isRemoved) {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("User Removed");
                        successAlert.setContentText("The user " + userName + " has been removed.");
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("User Not Found");
                        errorAlert.setContentText("The username " + userName + " was not found.");
                        errorAlert.showAndWait();
                    }
                    new AdminHomePage(adminUserName).show(primaryStage);
                }
            });
        });

        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");
        backButton.setOnAction(event -> new AdminHomePage(adminUserName).show(primaryStage));

        layout.getChildren().addAll(titleLabel, userNameField, removeButton, backButton);
        Scene removeUserScene = new Scene(layout, 800, 500);
        primaryStage.setScene(removeUserScene);
        primaryStage.setTitle("Remove User");
    }
}
