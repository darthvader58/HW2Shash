package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class StudentHomePage {
    private String userName;
    private DatabaseHelper databaseHelper;

    public StudentHomePage(String userName, DatabaseHelper databaseHelper) {
        this.userName = userName;
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Hello, " + userName + "! (Student)");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 1. Ask a new question
        Button askQuestionButton = new Button("Ask a Question");
        askQuestionButton.setOnAction(e -> showAskQuestionUI(primaryStage));

        // 2. View unresolved questions
        Button viewQuestionsButton = new Button("View Unresolved Questions");
        viewQuestionsButton.setOnAction(e -> showUnresolvedQuestionsUI(primaryStage));

        // 3. Update an existing question
        Button updateQuestionButton = new Button("Update My Question");
        updateQuestionButton.setOnAction(e -> selectWhichQuestionToUpdate(primaryStage));

        layout.getChildren().addAll(welcomeLabel, askQuestionButton, viewQuestionsButton, updateQuestionButton);

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Student Home Page");
    }

    // =============================== 1) Ask a Question ===============================
    private void showAskQuestionUI(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label instructionLabel = new Label("Enter your question details:");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField titleField = new TextField();
        titleField.setPromptText("Question Title");
        titleField.setMaxWidth(400);

        TextArea bodyArea = new TextArea();
        bodyArea.setPromptText("Question Body...");
        bodyArea.setWrapText(true);
        bodyArea.setMaxWidth(400);
        bodyArea.setPrefHeight(200);

        Button submitButton = new Button("Submit Question");
        submitButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String body = bodyArea.getText().trim();

            if (title.isEmpty() || body.isEmpty()) {
                showError("Please fill in both title and body.");
                return;
            }

            Question newQ = new Question();
            newQ.setTitle(title);
            newQ.setBody(body);
            newQ.setAskedByUserName(userName);
            newQ.setResolved(false);
            newQ.setUnreadMessagesCount(0);

            StoreQuestions storeQ = new StoreQuestions(databaseHelper);
            boolean success = storeQ.createQuestion(newQ);
            if (success) {
                showInfo("Question submitted successfully!");
                show(primaryStage);
            } else {
                showError("Failed to create question. Check console/logs.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(instructionLabel, titleField, bodyArea, submitButton, backButton);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ask a Question");
    }

    // =============================== 2) View Unresolved Questions ===============================
    /**
     * Lists unresolved questions in a ListView, with a new "View Details" button to see the entire question body.
     */
    private void showUnresolvedQuestionsUI(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Unresolved Questions:");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ListView<String> questionsListView = new ListView<>();
        questionsListView.setMaxWidth(600);
        questionsListView.setMaxHeight(300);

        StoreQuestions storeQ = new StoreQuestions(databaseHelper);
        List<Question> unresolved = storeQ.listUnresolvedQuestions();
        if (unresolved.isEmpty()) {
            questionsListView.getItems().add("No unresolved questions found.");
        } else {
            for (Question q : unresolved) {
                questionsListView.getItems().add("QID=" + q.getQuestionId() + ": " + q.getTitle());
            }
        }

        // Button to view the selected question's full details
        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setOnAction(e -> {
            String selectedItem = questionsListView.getSelectionModel().getSelectedItem();
            if (selectedItem == null || selectedItem.contains("No unresolved questions")) {
                showError("Please select a valid question.");
                return;
            }
            int qId = extractQuestionId(selectedItem);
            showQuestionDetailsUI(primaryStage, qId);
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, questionsListView, viewDetailsButton, backButton);

        Scene scene = new Scene(layout, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Unresolved Questions");
    }

    /**
     * After selecting a question ID from the list, display its entire body, askedByUserName, etc.
     */
    private void showQuestionDetailsUI(Stage primaryStage, int questionId) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label heading = new Label("Question Details (QID=" + questionId + ")");
        heading.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea detailsArea = new TextArea();
        detailsArea.setWrapText(true);
        detailsArea.setEditable(false);
        detailsArea.setMaxWidth(600);
        detailsArea.setPrefHeight(250);

        // Load the question
        StoreQuestions storeQ = new StoreQuestions(databaseHelper);
        Question q = storeQ.readQuestion(questionId);
        if (q == null) {
            detailsArea.setText("Error loading question. Possibly deleted or invalid ID.");
        } else {
            // Display the question details
            StringBuilder sb = new StringBuilder();
            sb.append("Title: ").append(q.getTitle()).append("\n\n");
            sb.append("Body:\n").append(q.getBody()).append("\n\n");
            sb.append("Asked By: ").append(q.getAskedByUserName()).append("\n");
            sb.append("Resolved? ").append(q.isResolved() ? "Yes" : "No").append("\n");
            sb.append("Unread Messages: ").append(q.getUnreadMessagesCount()).append("\n");
            detailsArea.setText(sb.toString());
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showUnresolvedQuestionsUI(primaryStage));

        layout.getChildren().addAll(heading, detailsArea, backButton);

        Scene scene = new Scene(layout, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Details");
    }

    // =============================== 3) Update a Question ===============================
    /**
     * Step 1: Let the student pick which question of theirs to update.
     */
    private void selectWhichQuestionToUpdate(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label instructionLabel = new Label("Select a question you want to update:");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ListView<String> myQuestionsList = new ListView<>();
        myQuestionsList.setMaxWidth(600);
        myQuestionsList.setMaxHeight(300);

        StoreQuestions storeQ = new StoreQuestions(databaseHelper);
        List<Question> myQuestions = storeQ.listQuestionsByUser(userName);
        if (myQuestions.isEmpty()) {
            myQuestionsList.getItems().add("You haven't asked any questions yet!");
        } else {
            for (Question q : myQuestions) {
                myQuestionsList.getItems().add("QID=" + q.getQuestionId() + " Title: " + q.getTitle());
            }
        }

        Button updateButton = new Button("Update Selected Question");
        updateButton.setOnAction(e -> {
            String selected = myQuestionsList.getSelectionModel().getSelectedItem();
            if (selected == null || selected.contains("haven't asked any")) {
                showError("Please select a valid question first.");
                return;
            }
            int qId = extractQuestionId(selected);
            showUpdateQuestionForm(primaryStage, qId);
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(instructionLabel, myQuestionsList, updateButton, backButton);

        Scene scene = new Scene(layout, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Select Question to Update");
    }

    /**
     * Step 2: Show the chosen question's current title/body, let user edit them, then save.
     */
    private void showUpdateQuestionForm(Stage primaryStage, int questionId) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label heading = new Label("Updating Question ID=" + questionId);
        heading.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        StoreQuestions storeQ = new StoreQuestions(databaseHelper);
        Question existingQ = storeQ.readQuestion(questionId);
        if (existingQ == null) {
            showError("Failed to load question. Check console/logs.");
            show(primaryStage);
            return;
        }

        TextField titleField = new TextField(existingQ.getTitle());
        titleField.setMaxWidth(400);

        TextArea bodyArea = new TextArea(existingQ.getBody());
        bodyArea.setWrapText(true);
        bodyArea.setMaxWidth(400);
        bodyArea.setPrefHeight(200);

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            String newTitle = titleField.getText().trim();
            String newBody = bodyArea.getText().trim();
            if (newTitle.isEmpty() || newBody.isEmpty()) {
                showError("Title or body cannot be empty.");
                return;
            }
            existingQ.setTitle(newTitle);
            existingQ.setBody(newBody);

            boolean success = storeQ.updateQuestion(existingQ);
            if (success) {
                showInfo("Question updated successfully!");
                show(primaryStage);
            } else {
                showError("Failed to update question. Check console/logs.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> selectWhichQuestionToUpdate(primaryStage));

        layout.getChildren().addAll(heading, new Label("Title:"), titleField, new Label("Body:"), bodyArea, saveButton, backButton);

        Scene scene = new Scene(layout, 600, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Update Question");
    }

    // =============================== Helper Methods ===============================
    /**
     * Extract questionId from a string like "QID=12: My Title".
     */
    private int extractQuestionId(String item) {
        try {
            // Example: "QID=12: My Title"
            String[] parts = item.split(" "); // "QID=12:", "My", "Title" ...
            // or "QID=12:" could be parts[0].
            // More robust approach: split on ":" or do a small parse
            // We'll do a simpler approach:
            String noSpace = item.split(":")[0]; // "QID=12"
            String[] eqSplit = noSpace.split("=");
            return Integer.parseInt(eqSplit[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }
}
