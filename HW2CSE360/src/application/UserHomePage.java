package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * UserHomePage displays a simple interface showing the user's role(s) and
 * a button to navigate to the correct home page. If multiple roles exist,
 * we show a combo box to select which role to proceed with.
 */
public class UserHomePage {
    private String userName;
    private DatabaseHelper databaseHelper;

    public UserHomePage(String userName, DatabaseHelper databaseHelper) {
        this.userName = userName;
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Retrieve the user's role(s) from the database
        String role = databaseHelper.getUserRoleByUsername(userName);
        System.out.println("DB says role is: [" + role + "]");

        // A label showing the userName and the retrieved role(s)
        Label infoLabel = new Label("Hello, " + userName + "!\nRole(s): " + role);
        infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        if (role == null || role.isEmpty()) {
            // If no role is found, display an error message
            Label errorLabel = new Label("No valid role found for user: " + userName);
            layout.getChildren().addAll(infoLabel, errorLabel);
        }
        else if (role.contains(",")) {
            // If multiple roles are found, let the user select which one to use
            Label selectLabel = new Label("Select a Role:");
            ComboBox<String> roleComboBox = new ComboBox<>();

            String[] roles = role.split(",");
            for (String r : roles) {
                roleComboBox.getItems().add(r.trim());
            }
            roleComboBox.setPromptText("Select Role");

            Button goButton = new Button("Proceed to Selected Role");
            goButton.setOnAction(event -> {
                String selectedRole = roleComboBox.getValue();
                if (selectedRole == null || selectedRole.isEmpty()) {
                    return; // no role selected
                }
                navigateToRoleHomePage(selectedRole, primaryStage);
            });

            layout.getChildren().addAll(infoLabel, selectLabel, roleComboBox, goButton);
        }
        else {
            // Single role: we show a button to proceed to that role's page
            Button proceedButton = new Button("Proceed to Your Role Home Page");
            proceedButton.setOnAction(e -> {
                navigateToRoleHomePage(role, primaryStage);
            });

            layout.getChildren().addAll(infoLabel, proceedButton);
        }

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("User Home Page");
    }

    /**
     * A helper method to navigate to the correct home page based on the role.
     * Using case-insensitive matching to avoid mismatch issues (e.g., "Reviewer" vs. "reviewer").
     */
    private void navigateToRoleHomePage(String role, Stage primaryStage) {
        String normalizedRole = role.trim().toLowerCase();
        System.out.println("Navigating with role: [" + normalizedRole + "]");

        switch (normalizedRole) {
            case "student":
                new StudentHomePage(userName, databaseHelper).show(primaryStage);
                break;
            case "staff":
                new StaffHomePage(userName, databaseHelper).show(primaryStage);
                break;
            case "reviewer":
                new ReviewerHomePage(databaseHelper).show(primaryStage);
                break;
            case "instructor":
                new InstructorHomePage(databaseHelper, userName).show(primaryStage);
                break;
            default:
                // Fallback or error handling
                Label errorLabel = new Label("Unrecognized role: [" + role + "]");
                VBox layout = new VBox(errorLabel);
                layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
                Scene scene = new Scene(layout, 800, 500);
                primaryStage.setScene(scene);
                primaryStage.setTitle("Error");
                break;
        }
    }
}
