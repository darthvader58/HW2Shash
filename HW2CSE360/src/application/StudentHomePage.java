package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

/**
 * StudentHomePage allows a student to:
 *  1) Ask a question (with a title and body)
 *  2) View unresolved questions
 *  3) View answers for a selected question
 */
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

        // Button to ask a new question
        Button askQuestionButton = new Button("Ask a Question");
        askQuestionButton.setOnAction(e -> showAskQuestionUI(primaryStage));

        // Button to view unresolved questions
        Button viewQuestionsButton = new Button("View Unresolved Questions");
        viewQuestionsButton.setOnAction(e -> showUnresolvedQuestionsUI(primaryStage));

        layout.getChildren().addAll(welcomeLabel, askQuestionButton, viewQuestionsButton);

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Student Home Page");
    }

    /**
     * Shows a scene where the student can type a question title and body.
     * Submits to StoreQuestions, then returns to main StudentHomePage.
     */
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
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in both title and body.");
                alert.showAndWait();
                return;
            }

            // Create a Question object
            Question newQ = new Question();
            newQ.setQuestionId(0); // DB will auto-generate
            newQ.setTitle(title);
            newQ.setBody(body);
            newQ.setAskedByUserName(userName);
            newQ.setResolved(false);
            newQ.setUnreadMessagesCount(0);

            // Use StoreQuestions to save
            StoreQuestions storeQ = new StoreQuestions(databaseHelper);
            boolean success = storeQ.createQuestion(newQ);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Question submitted successfully!");
                alert.showAndWait();
                show(primaryStage); // Return to main page
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create question. Check console/logs.");
                alert.showAndWait();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(instructionLabel, titleField, bodyArea, submitButton, backButton);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ask a Question");
    }

    /**
     * Shows a list of unresolved questions. The student can select one
     * and view its answers.
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

        // Fetch unresolved questions from DB
        StoreQuestions storeQ = new StoreQuestions(databaseHelper);
        List<Question> unresolved = storeQ.listAllQuestions();

        // Populate the ListView with question titles
        for (Question q : unresolved) {
            // e.g. "QID=5: [Title]"
            questionsListView.getItems().add("QID=" + q.getQuestionId() + ": " + q.getTitle());
        }

        // A button to view the answers for the selected question
        Button viewAnswersButton = new Button("View Answers");
        viewAnswersButton.setOnAction(e -> {
            String selectedItem = questionsListView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a question first.");
                alert.showAndWait();
                return;
            }
            // Extract the questionId from the string, e.g. "QID=5: My Title"
            int qId = extractQuestionId(selectedItem);
            showAnswersUI(primaryStage, qId);
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, questionsListView, viewAnswersButton, backButton);

        Scene scene = new Scene(layout, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Unresolved Questions");
    }

    /**
     * Shows all answers for the given questionId.
     */
    private void showAnswersUI(Stage primaryStage, int questionId) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label answersLabel = new Label("Answers for Question ID = " + questionId);
        answersLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea answersArea = new TextArea();
        answersArea.setWrapText(true);
        answersArea.setEditable(false);
        answersArea.setMaxWidth(600);
        answersArea.setMaxHeight(300);

        // Fetch answers from DB
        StoreAnswers storeA = new StoreAnswers(databaseHelper);
        List<Answer> ansList = storeA.listAnswersByQuestion(questionId);
        if (ansList.isEmpty()) {
            answersArea.setText("No answers yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Answer a : ansList) {
                sb.append("Answer ID = ").append(a.getAnswerId())
                  .append("\nPosted by: ").append(a.getAnsweredByUserName())
                  .append("\nAccepted? ").append(a.isAccepted())
                  .append("\n--\n").append(a.getAnswerBody())
                  .append("\n====================\n");
            }
            answersArea.setText(sb.toString());
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showUnresolvedQuestionsUI(primaryStage));

        layout.getChildren().addAll(answersLabel, answersArea, backButton);

        Scene scene = new Scene(layout, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Answers for QID=" + questionId);
    }

    /**
     * Helper to parse the question ID from a string like "QID=5: My Title".
     */
    private int extractQuestionId(String item) {
        // e.g. item = "QID=12: My Title"
        // We can split on '=' then on ':' or do a regex. A simple approach:
        try {
            String[] parts = item.split(":");  // ["QID=12", " My Title"]
            String leftPart = parts[0];        // "QID=12"
            String[] eqSplit = leftPart.split("="); // ["QID", "12"]
            return Integer.parseInt(eqSplit[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // fallback
        }
    }
}
