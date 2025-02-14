package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

/*
 * InstructorHomePage: 
 *  - View all questions 
 *  - View answers to a selected question
 *  - Mark question resolved
 *  - Answer selected question
 *  - Delete selected question
 */
public class InstructorHomePage {
    private final DatabaseHelper databaseHelper;
    private final String userName; 

    public InstructorHomePage(DatabaseHelper databaseHelper, String userName) {
        this.databaseHelper = databaseHelper;
        this.userName = userName;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Hello, Instructor " + userName + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button reviewDiscussionsButton = new Button("Review Discussions");
        reviewDiscussionsButton.setOnAction(e -> showReviewDiscussionsUI(primaryStage));

        Button manageScorecardsButton = new Button("Manage Reviewer Scorecards");
        manageScorecardsButton.setOnAction(e -> showManageScorecardsUI(primaryStage));

        Button viewFeedbackButton = new Button("View Private Feedback");
        viewFeedbackButton.setOnAction(e -> showPrivateFeedbackUI(primaryStage));

        layout.getChildren().addAll(welcomeLabel, reviewDiscussionsButton, 
                                    manageScorecardsButton, viewFeedbackButton);

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Instructor Home Page");
    }

    /**
     * Displays all questions in a ListView, with buttons to:
     *  - View answers for the selected question
     *  - Mark it resolved
     *  - Answer the question
     *  - Delete the question
     */
    private void showReviewDiscussionsUI(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("All Questions (Select one to view answers, resolve, answer, or delete):");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ListView<String> questionList = new ListView<>();
        questionList.setMaxWidth(700);
        questionList.setMaxHeight(300);

        // Load all questions
        StoreQuestions storeQ = new StoreQuestions(databaseHelper);
        List<Question> allQs = storeQ.listAllQuestions();
        if (allQs.isEmpty()) {
            questionList.getItems().add("No questions found in the database.");
        } else {
            for (Question q : allQs) {
                // e.g. "QID=3 (Unresolved) Title: How to do X?"
                String display = "QID=" + q.getQuestionId()
                               + " (" + (q.isResolved() ? "Resolved" : "Unresolved") + ") "
                               + "Title: " + q.getTitle();
                questionList.getItems().add(display);
            }
        }

        // Button to view answers for selected question
        Button viewAnswersButton = new Button("View Answers");
        viewAnswersButton.setOnAction(e -> {
            String selectedItem = questionList.getSelectionModel().getSelectedItem();
            if (selectedItem == null || selectedItem.contains("No questions found")) {
                showError("Please select a valid question first.");
                return;
            }
            int qId = extractQuestionId(selectedItem);
            showAnswersUI(primaryStage, qId);
        });

        // Button to mark question resolved
        Button markResolvedButton = new Button("Mark Resolved");
        markResolvedButton.setOnAction(e -> {
            String selectedItem = questionList.getSelectionModel().getSelectedItem();
            if (selectedItem == null || selectedItem.contains("No questions found")) {
                showError("Please select a valid question first.");
                return;
            }
            int qId = extractQuestionId(selectedItem);
            boolean success = databaseHelper.updateQuestionResolved(qId, true);
            if (success) {
                showInfo("Question " + qId + " marked as resolved.");
                // Reload the list
                showReviewDiscussionsUI(primaryStage);
            } else {
                showError("Failed to mark question resolved. Check console/logs.");
            }
        });

        // Button to answer the selected question
        Button answerButton = new Button("Answer Question");
        answerButton.setOnAction(e -> {
            String selectedItem = questionList.getSelectionModel().getSelectedItem();
            if (selectedItem == null || selectedItem.contains("No questions found")) {
                showError("Please select a valid question first.");
                return;
            }
            int qId = extractQuestionId(selectedItem);
            showAnswerUI(primaryStage, qId);
        });

        // Button to delete the question
        Button deleteButton = new Button("Delete Question");
        deleteButton.setOnAction(e -> {
            String selectedItem = questionList.getSelectionModel().getSelectedItem();
            if (selectedItem == null || selectedItem.contains("No questions found")) {
                showError("Please select a valid question first.");
                return;
            }
            int qId = extractQuestionId(selectedItem);
            boolean success = databaseHelper.deleteQuestionAndAnswers(qId);
            if (success) {
                showInfo("Question " + qId + " deleted successfully.");
                // Reload list
                showReviewDiscussionsUI(primaryStage);
            } else {
                showError("Failed to delete question. Check console/logs.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, questionList, 
                                    viewAnswersButton, markResolvedButton, 
                                    answerButton, deleteButton, backButton);

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Review Discussions");
    }

    /**
     * Displays answers for the given questionId in a TextArea.
     */
    private void showAnswersUI(Stage primaryStage, int questionId) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label heading = new Label("Answers for Question ID = " + questionId);
        heading.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea answersArea = new TextArea();
        answersArea.setWrapText(true);
        answersArea.setEditable(false);
        answersArea.setMaxWidth(600);
        answersArea.setMaxHeight(300);

        // Load answers
        StoreAnswers storeA = new StoreAnswers(databaseHelper);
        List<Answer> ansList = storeA.listAnswersByQuestion(questionId);
        if (ansList.isEmpty()) {
            answersArea.setText("No answers for this question.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Answer a : ansList) {
                sb.append("Answer ID=").append(a.getAnswerId())
                  .append(" By: ").append(a.getAnsweredByUserName())
                  .append(" (Accepted=").append(a.isAccepted()).append(")\n")
                  .append(a.getAnswerBody()).append("\n")
                  .append("----------\n");
            }
            answersArea.setText(sb.toString());
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showReviewDiscussionsUI(primaryStage));

        layout.getChildren().addAll(heading, answersArea, backButton);

        Scene scene = new Scene(layout, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Answers for QID=" + questionId);
    }

    /**
     * UI to provide an answer for questionId
     */
    private void showAnswerUI(Stage primaryStage, int questionId) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label heading = new Label("Answer for Question ID=" + questionId);
        heading.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea answerArea = new TextArea();
        answerArea.setPromptText("Type your answer here...");
        answerArea.setWrapText(true);
        answerArea.setMaxWidth(400);
        answerArea.setPrefHeight(200);

        Button submitAnswer = new Button("Submit Answer");
        submitAnswer.setOnAction(e -> {
            String ansText = answerArea.getText().trim();
            if (ansText.isEmpty()) {
                showError("Answer cannot be empty.");
                return;
            }
            // Build an Answer object
            Answer ans = new Answer();
            ans.setAnswerId(0); // auto-generated
            ans.setQuestionId(questionId);
            ans.setAnswerBody(ansText);
            ans.setAnsweredByUserName(userName); // The instructor's username
            ans.setAccepted(false);

            StoreAnswers storeA = new StoreAnswers(databaseHelper);
            boolean success = storeA.createAnswer(ans);
            if (success) {
                showInfo("Answer posted successfully!");
                // Return to question list
                showReviewDiscussionsUI(primaryStage);
            } else {
                showError("Failed to post answer. Check console/logs.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showReviewDiscussionsUI(primaryStage));

        layout.getChildren().addAll(heading, answerArea, submitAnswer, backButton);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Answer a Question");
    }

    /**
     * Helper to parse QID=### from a string like:
     * "QID=12 (Resolved) Title: My Title"
     */
    private int extractQuestionId(String item) {
        try {
            // e.g. "QID=12 (Unresolved) Title: My Title"
            String[] parts = item.split(" "); 
            // parts[0] = "QID=12"
            String qidPart = parts[0]; 
            String[] eqSplit = qidPart.split("=");
            return Integer.parseInt(eqSplit[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return -1; 
        }
    }

    private void showManageScorecardsUI(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label label = new Label("Manage Reviewer Scorecards (Placeholder)");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(label, backButton);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Manage Reviewer Scorecards");
    }

    private void showPrivateFeedbackUI(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label label = new Label("View Private Feedback (Placeholder)");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(label, backButton);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("View Private Feedback");
    }

    // Helper to show an info alert
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    // Helper to show an error alert
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
