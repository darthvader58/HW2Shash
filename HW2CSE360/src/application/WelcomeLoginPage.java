package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.DatabaseHelper;

public class WelcomeLoginPage {
	
    private final DatabaseHelper databaseHelper;
    private String userName;

    public WelcomeLoginPage(DatabaseHelper databaseHelper, String userName) {
        this.databaseHelper = databaseHelper;
        this.userName = userName;
    }
    
    public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(5);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label welcomeLabel = new Label("Welcome, " + userName + "!!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // "Continue" button to proceed to the user's page
        Button continueButton = new Button("Continue to your Page");
        continueButton.setOnAction(a -> {
            String role = user.getRole();
            System.out.println("User's role from 'User' object: " + role);

            // If user is admin, go to AdminHomePage
            if (role.equalsIgnoreCase("admin")) {
                AdminHomePage adminHomePage = new AdminHomePage(userName);
                adminHomePage.show(primaryStage);
            } 
            // Otherwise, go to UserHomePage, which routes single-role users to their specific page
            else {
                UserHomePage userHomePage = new UserHomePage(userName, databaseHelper);
                userHomePage.show(primaryStage);
            }
        });
        
        // Logout button returns to SetupLoginSelectionPage
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            System.out.println("Logging out and returning to SetupLoginSelectionPage.");
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });
        
        // Quit button exits the application
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(a -> {
            databaseHelper.closeConnection();
            Platform.exit();
        });
        
        // If user is admin, show an "Invite" button for generating invitation codes
        if ("admin".equalsIgnoreCase(user.getRole())) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage);
            });
            layout.getChildren().add(inviteButton);
        }
        
        layout.getChildren().addAll(welcomeLabel, continueButton, quitButton, logoutButton);
        Scene welcomeScene = new Scene(layout, 800, 400);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
    }
}
