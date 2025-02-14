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
 * StaffHomePage:
 *  - "Monitor Discussions" -> display Q&A
 *  - "Assist Instructors" -> placeholder
 *  - Also can delete Q/A if you previously implemented that
 */
public class StaffHomePage {
    private String userName;
    private final DatabaseHelper databaseHelper;

    public StaffHomePage(String userName, DatabaseHelper databaseHelper) {
        this.userName = userName;
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Hello, " + userName + " (Staff)!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button monitorButton = new Button("Monitor Discussions");
        monitorButton.setOnAction(e -> showMonitorDiscussionsUI(primaryStage));

        Button assistButton = new Button("Assist Instructors");
        assistButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Assist Instructors (placeholder)...");
            alert.showAndWait();
        });

        // If you want to keep question/answer deletion for staff, add the same logic:
        Button deleteQuestionButton = new Button("Delete Question");
        deleteQuestionButton.setOnAction(e -> showDeleteQuestionDialog(primaryStage));

        Button deleteAnswerButton = new Button("Delete Answer");
        deleteAnswerButton.setOnAction(e -> showDeleteAnswerDialog(primaryStage));

        layout.getChildren().addAll(welcomeLabel, monitorButton, assistButton, 
                                    deleteQuestionButton, deleteAnswerButton);

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Home Page");
    }

    /**
     * Show all Q&A in a text area, similar to instructor's "Review Discussions."
     */
    private void showMonitorDiscussionsUI(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label label = new Label("Monitor Discussions (All Q&A)");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea discussionArea = new TextArea();
        discussionArea.setWrapText(true);
        discussionArea.setEditable(false);
        discussionArea.setMaxWidth(700);
        discussionArea.setPrefHeight(300);

        // List all questions & answers
        StoreQuestions storeQ = new StoreQuestions(databaseHelper);
        List<Question> allQs = storeQ.listAllQuestions(); // or create "listAllQuestions()"
        StringBuilder sb = new StringBuilder();
        if (allQs.isEmpty()) {
            sb.append("No questions found.\n");
        } else {
            for (Question q : allQs) {
                sb.append("QID=").append(q.getQuestionId())
                  .append(" Title=").append(q.getTitle())
                  .append("\nAskedBy=").append(q.getAskedByUserName())
                  .append(" Resolved=").append(q.isResolved())
                  .append("\nBody: ").append(q.getBody())
                  .append("\n---------------------------------\n");
                // Show answers
                StoreAnswers storeA = new StoreAnswers(databaseHelper);
                List<Answer> ansList = storeA.listAnswersByQuestion(q.getQuestionId());
                if (ansList.isEmpty()) {
                    sb.append("No answers.\n");
                } else {
                    for (Answer a : ansList) {
                        sb.append("   AnsID=").append(a.getAnswerId())
                          .append(" (By: ").append(a.getAnsweredByUserName())
                          .append(") Accepted=").append(a.isAccepted())
                          .append("\n   ").append(a.getAnswerBody())
                          .append("\n---------------------\n");
                    }
                }
                sb.append("\n====================\n");
            }
        }
        discussionArea.setText(sb.toString());

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(label, discussionArea, backButton);

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Monitor Discussions");
    }

    /**
     * Show a dialog to delete a question by ID.
     */
    private void showDeleteQuestionDialog(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Question");
        dialog.setHeaderText("Enter Question ID to delete:");
        dialog.setContentText("Question ID:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int qId = Integer.parseInt(input);
                StoreQuestions storeQ = new StoreQuestions(databaseHelper);
                boolean success = databaseHelper.deleteQuestionAndAnswers(qId);
                if(success) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION, "Question deleted successfully.");
                    info.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete question. Check the ID.");
                    err.showAndWait();
                }
            } catch(NumberFormatException ex) {
                Alert err = new Alert(Alert.AlertType.ERROR, "Invalid input. Must be a numeric question ID.");
                err.showAndWait();
            }
        });
    }

    /**
     * Show a dialog to delete an answer by ID.
     */
    private void showDeleteAnswerDialog(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Answer");
        dialog.setHeaderText("Enter Answer ID to delete:");
        dialog.setContentText("Answer ID:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int aId = Integer.parseInt(input);
                StoreAnswers storeA = new StoreAnswers(databaseHelper);
                boolean success = storeA.deleteAnswer(aId);
                if(success) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION, "Answer deleted successfully.");
                    info.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete answer. Check the ID.");
                    err.showAndWait();
                }
            } catch(NumberFormatException ex) {
                Alert err = new Alert(Alert.AlertType.ERROR, "Invalid input. Must be a numeric answer ID.");
                err.showAndWait();
            }
        });
    }
}
