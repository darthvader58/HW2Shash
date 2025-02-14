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
 * ReviewerHomePage:
 *  - "Review Potential Answers" -> lists answers from the DB (placeholder).
 *  - "Write a Review" -> minimal UI to simulate writing a review.
 */
public class ReviewerHomePage {
    private final DatabaseHelper databaseHelper;

    public ReviewerHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Hello, Reviewer!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Button to review potential answers
        Button reviewAnswersButton = new Button("Review Potential Answers");
        reviewAnswersButton.setOnAction(e -> showAnswersNeedingReviewUI(primaryStage));

        // Button to write a review (placeholder UI)
        Button writeReviewButton = new Button("Write a Review");
        writeReviewButton.setOnAction(e -> showWriteReviewUI(primaryStage, -1)); 
        // -1 indicates no specific answerId

        layout.getChildren().addAll(welcomeLabel, reviewAnswersButton, writeReviewButton);

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Home Page");
    }

    /**
     * Shows a list of answers from the DB. In a real scenario, you'd filter answers 
     * that actually need review (e.g., not accepted or missing a review). 
     * For demonstration, we just list all answers.
     */
    private void showAnswersNeedingReviewUI(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Answers Needing Review (Placeholder)");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ListView<String> answersList = new ListView<>();
        answersList.setMaxWidth(600);
        answersList.setMaxHeight(300);

        // Use StoreAnswers to fetch all answers
        StoreAnswers storeA = new StoreAnswers(databaseHelper);
        List<Answer> allAnswers = storeA.listAnswersByQuestion(-1); 
        // If you had a "listAllAnswers()" method, you’d call that. 
        // For now, let's pretend listAnswersByQuestion(-1) returns everything or you create a new method.

        // Populate the ListView
        if (allAnswers.isEmpty()) {
            answersList.getItems().add("No answers found in DB (placeholder).");
        } else {
            for (Answer a : allAnswers) {
                String display = "AnsID=" + a.getAnswerId() 
                               + " QID=" + a.getQuestionId() 
                               + " (By: " + a.getAnsweredByUserName() + ")";
                answersList.getItems().add(display);
            }
        }

        // "Write Review" button to let user pick an answer
        Button writeReviewButton = new Button("Write Review for Selected");
        writeReviewButton.setOnAction(e -> {
            String selected = answersList.getSelectionModel().getSelectedItem();
            if (selected == null || selected.contains("No answers found")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an answer first.");
                alert.showAndWait();
                return;
            }
            int answerId = extractAnswerId(selected);
            showWriteReviewUI(primaryStage, answerId);
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, answersList, writeReviewButton, backButton);

        Scene scene = new Scene(layout, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Answers Needing Review");
    }

    /**
     * Minimal UI for writing a review. In a real app, you'd store the review in a "reviews" table.
     */
    private void showWriteReviewUI(Stage primaryStage, int answerId) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label heading = new Label("Write a Review (Answer ID=" + answerId + ")");
        heading.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Write your review here...");
        reviewArea.setWrapText(true);
        reviewArea.setMaxWidth(400);
        reviewArea.setPrefHeight(200);

        Button submitReview = new Button("Submit Review");
        submitReview.setOnAction(e -> {
            String reviewText = reviewArea.getText().trim();
            if (reviewText.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Review cannot be empty.");
                alert.showAndWait();
                return;
            }
            // In a real scenario, you'd store the review in the DB.
            Alert alert = new Alert(Alert.AlertType.INFORMATION, 
                    "Review submitted successfully!\n(Placeholder, not stored in DB)");
            alert.showAndWait();
            show(primaryStage);
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(heading, reviewArea, submitReview, backButton);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Write a Review");
    }

    /**
     * Helper method to parse the answerId from a string like:
     * "AnsID=5 QID=3 (By: studentUser)"
     */
    private int extractAnswerId(String item) {
        try {
            // item = "AnsID=5 QID=3 (By: studentUser)"
            String[] parts = item.split(" ");
            // parts[0] = "AnsID=5"
            String ansIdPart = parts[0]; // "AnsID=5"
            String[] eqSplit = ansIdPart.split("=");
            return Integer.parseInt(eqSplit[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
