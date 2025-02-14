package application;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.*;

import application.StoreQuestions;
import application.StoreAnswers;

public class AdminHomePage {
	
	private String userName;  // Admin is identified by userName only
	private DatabaseHelper databaseHelper; // replaced dbHelper with databaseHelper
	Stage primaryStage = null;

	// Constructor now takes userName only.
    public AdminHomePage(String userName) {
        this.userName = userName;
        this.databaseHelper = new DatabaseHelper();  
        try {
            databaseHelper.connectToDatabase(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Sends a temporary password to a user identified by username.
    private void showSendTempPassword(Stage primaryStage) {
    	VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");
        
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter User Name");
        userNameField.setMaxWidth(200);
        
        Button sendPasswordButton = new Button("Send Temporary Password");
        sendPasswordButton.setStyle("-fx-font-size: 14px;");
        
        sendPasswordButton.setOnAction(event -> {
            String sendUserName = userNameField.getText();
            databaseHelper.setForgetPassword(sendUserName);
            databaseHelper.deleteNotificationLine(userName, "User " + sendUserName + " forgot their password. Send them a temporary one.");
            String password = OTPGenerator.generateOTP();
            
            databaseHelper.addNotificationToUser("Here Is Your Temporary Password: " + password, sendUserName);
            if (databaseHelper.setPassword(sendUserName, password)) {
            	Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Password Sent");
                successAlert.setContentText("The user " + sendUserName + " can now reset their password.");
                successAlert.showAndWait();
            } else {
            	Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("User Not Found");
                errorAlert.setContentText("The username " + sendUserName + " was not found.");
                errorAlert.showAndWait();
            }
            new AdminHomePage(userName).show(primaryStage);
        });
        
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");
        backButton.setOnAction(event -> show(primaryStage));
    	
        layout.getChildren().addAll(userNameField, sendPasswordButton, backButton);
        Scene tempPasswordScene = new Scene(layout, 800, 500);
        primaryStage.setScene(tempPasswordScene);
        primaryStage.setTitle("Send Temporary Password");
    }
    
    // Displays notifications for the admin.
    private void showNotifications(Stage primaryStage) {
    	VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");
        
        TextArea notificationsArea = new TextArea();
        notificationsArea.setEditable(false);
        notificationsArea.setPrefSize(600, 300);
        
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");
        backButton.setOnAction(event -> show(primaryStage));
        
        try {
			databaseHelper.displayNotifications(notificationsArea, userName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        layout.getChildren().addAll(notificationsArea, backButton);
        Scene notificationsScene = new Scene(layout, 800, 500);
        primaryStage.setScene(notificationsScene);
        primaryStage.setTitle("Notifications");
    }
    
    // Displays all user information.
    private void showUserInfoPage(Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        TextArea userInfoArea = new TextArea();
        userInfoArea.setEditable(false);
        userInfoArea.setPrefSize(600, 300); 

        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");
        backButton.setOnAction(event -> show(primaryStage));

        try {
            databaseHelper.displayAllUsers(userInfoArea);
        } catch (Exception e) {
            e.printStackTrace();
            userInfoArea.setText("Error fetching user data.");
        }

        layout.getChildren().addAll(userInfoArea, backButton);
        Scene userInfoScene = new Scene(layout, 800, 500);
        primaryStage.setScene(userInfoScene);
        primaryStage.setTitle("User Information");
    }
    
    // Deletion options for Q&A
    /*private void addDeletionOptions(VBox layout, Stage primaryStage) {
        // Delete Question
        Button deleteQuestionButton = new Button("Delete Question");
        deleteQuestionButton.setStyle("-fx-font-size: 14px;");
        deleteQuestionButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Delete Question");
            dialog.setHeaderText("Enter Question ID to delete:");
            dialog.setContentText("Question ID:");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    int qId = Integer.parseInt(input);
                    boolean success = databaseHelper.deleteQuestion(qId);
                    if(success) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Question deleted successfully.");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR, "Failed to delete question. Check the ID.");
                        alert.showAndWait();
                    }
                } catch(NumberFormatException ex) {
                    Alert alert = new Alert(AlertType.ERROR, "Invalid input. Enter a numeric question ID.");
                    alert.showAndWait();
                }
            });
        });
        
        // Delete Answer
        Button deleteAnswerButton = new Button("Delete Answer");
        deleteAnswerButton.setStyle("-fx-font-size: 14px;");
        deleteAnswerButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Delete Answer");
            dialog.setHeaderText("Enter Answer ID to delete:");
            dialog.setContentText("Answer ID:");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    int aId = Integer.parseInt(input);
                    boolean success = databaseHelper.deleteAnswer(aId);
                    if(success) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Answer deleted successfully.");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR, "Failed to delete answer. Check the ID.");
                        alert.showAndWait();
                    }
                } catch(NumberFormatException ex) {
                    Alert alert = new Alert(AlertType.ERROR, "Invalid input. Enter a numeric answer ID.");
                    alert.showAndWait();
                }
            });
        });
        
        layout.getChildren().addAll(deleteQuestionButton, deleteAnswerButton);
    }*/
    
 // Main home page for admin
    public void show(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label adminLabel = new Label("Hello, " + userName + "!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button viewUsersButton = new Button("View All Users");
        viewUsersButton.setStyle("-fx-font-size: 14px;");
        viewUsersButton.setOnAction(event -> showUserInfoPage(primaryStage));
        
        Button removeUserButton = new Button("Remove User");
        removeUserButton.setStyle("-fx-font-size: 14px;");
        
        Button modifyUserRoleButton = new Button("Modify User Role");
        modifyUserRoleButton.setStyle("-fx-font-size: 14px;");
        
        Button sendTempPassButton = new Button("Send Temporary Password To User");
        sendTempPassButton.setStyle("-fx-font-size: 14px;");
        
        Button checkNotificationButton = new Button("Notifications (" + databaseHelper.getNumNotifications(userName) + ")");
        checkNotificationButton.setStyle("-fx-font-size: 14px;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
        
        viewUsersButton.setOnAction(event -> showUserInfoPage(primaryStage));
        removeUserButton.setOnAction(event -> {
            // Using the deletion UI for users
            new deleteUser(databaseHelper, primaryStage, userName).show();
        });
        modifyUserRoleButton.setOnAction(event -> {
            ModifyUserRole modifyUserPage = new ModifyUserRole(databaseHelper, primaryStage, userName);
            modifyUserPage.show();
        });
        checkNotificationButton.setOnAction(event -> showNotifications(primaryStage));
        sendTempPassButton.setOnAction(event -> showSendTempPassword(primaryStage));
        
        // Add deletion options for questions/answers (only for Admin)
        /*addDeletionOptions(layout, primaryStage);*/
        
        layout.getChildren().addAll(adminLabel, viewUsersButton, removeUserButton, modifyUserRoleButton,
                sendTempPassButton, checkNotificationButton, logoutButton);
        
        Scene adminScene = new Scene(layout, 800, 500);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
    }
}
