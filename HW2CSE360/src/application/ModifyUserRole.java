package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import databasePart1.DatabaseHelper;

/**
 * ModifyUserRole class allows an admin to modify a user's role.
 * It now identifies users using their unique username rather than an ID.
 */
class ModifyUserRole {
	DatabaseHelper databaseHelper = null;
	Stage primaryStage = null;
	private String adminUserName;  // using adminUserName only

	public ModifyUserRole(DatabaseHelper databaseHelper, Stage primaryStage, String adminUserName) {
		this.databaseHelper = databaseHelper;
		this.primaryStage = primaryStage;
		this.adminUserName = adminUserName;
	}
	
	public void show() {
	    VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

	    Label titleLabel = new Label("Modify User Role");
	    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    TextField userNameField = new TextField();
	    userNameField.setPromptText("Enter User Name");
	    userNameField.setMaxWidth(200);
	    
	    CheckBox studentCheckBox = new CheckBox("Student");
	    CheckBox reviewerCheckBox = new CheckBox("Reviewer");
	    CheckBox instructorCheckBox = new CheckBox("Instructor");
	    CheckBox staffCheckBox = new CheckBox("Staff");
	    
	    VBox checkBoxContainer = new VBox(5, studentCheckBox, reviewerCheckBox, instructorCheckBox, staffCheckBox);
	    checkBoxContainer.setStyle("-fx-alignment: center;");

	    Button modifyButton = new Button("Modify User Role");
	    modifyButton.setStyle("-fx-font-size: 14px;");
	    
	    modifyButton.setOnAction(event -> {
	        try {
	            String userName = userNameField.getText();
	            
	            if (databaseHelper.getUserRoleByUsername(userName).equalsIgnoreCase("admin")) {
	                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
	                errorAlert.setTitle("Error");
	                errorAlert.setHeaderText("Cannot Change Admin Role");
	                errorAlert.setContentText("You cannot change the role of an Admin.");
	                errorAlert.showAndWait();
	                return;
	            } else {
	                List<String> selectedRoles = new ArrayList<>();
	                if (studentCheckBox.isSelected()) selectedRoles.add("Student");
	                if (reviewerCheckBox.isSelected()) selectedRoles.add("Reviewer");
	                if (instructorCheckBox.isSelected()) selectedRoles.add("Instructor");
	                if (staffCheckBox.isSelected()) selectedRoles.add("Staff");
	                
	                if (selectedRoles.isEmpty()) {
	                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
	                    errorAlert.setTitle("Error");
	                    errorAlert.setHeaderText("No Role Selected");
	                    errorAlert.setContentText("Please select at least one role.");
	                    errorAlert.showAndWait();
	                    return;
	                }
	                
	                String roles = String.join(",", selectedRoles);
	                
	                boolean isMod = databaseHelper.changeUserRole(userName, roles);
	                if (!isMod) {
	                    Alert notFoundAlert = new Alert(Alert.AlertType.ERROR);
	                    notFoundAlert.setTitle("Error");
	                    notFoundAlert.setHeaderText("User Not Found");
	                    notFoundAlert.setContentText("Username: " + userName + " not found");
	                    notFoundAlert.showAndWait();
	                    return;
	                } else {
	                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
	                    successAlert.setTitle("Success");
	                    successAlert.setHeaderText("User Role Modified");
	                    successAlert.setContentText("The user " + userName + " has been modified with role(s): " + roles);
	                    successAlert.showAndWait();
	                }
	            } 
	        } catch (Exception e) {
	            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
	            errorAlert.setTitle("Error");
	            errorAlert.setHeaderText("Invalid Input");
	            errorAlert.setContentText("Please enter a valid username.");
	            errorAlert.showAndWait();
	        }
	    });
	    
	    Button backButton = new Button("Back to Home");
	    backButton.setStyle("-fx-font-size: 14px;");
	    backButton.setOnAction(event -> {
	        new AdminHomePage(adminUserName).show(primaryStage);
	    });

	    layout.getChildren().addAll(titleLabel, userNameField, checkBoxContainer, modifyButton, backButton);
	    Scene modifyUserScene = new Scene(layout, 800, 500);
	    primaryStage.setScene(modifyUserScene);
	    primaryStage.setTitle("Modify User Role");
	}
}
