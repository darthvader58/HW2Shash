package application;

/**
 * Represents a potential answer to a question.
 */
public class Answer {
    private int answerId;            // Primary key in the DB
    private int questionId;          // Foreign key to Question
    private String answerBody;       // Full text of the answer
    private String answeredByUserName; // The userName of who posted the answer
    private boolean isAccepted;      // Did the question-asker accept this as correct?

    public Answer() {
        // Default constructor
    }

    public Answer(int answerId, int questionId, String answerBody, String answeredByUserName, boolean isAccepted) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.answerBody = answerBody;
        this.answeredByUserName = answeredByUserName;
        this.isAccepted = isAccepted;
    }

    // Getters and setters
    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getAnswerBody() {
        return answerBody;
    }

    public void setAnswerBody(String answerBody) {
        this.answerBody = answerBody;
    }

    public String getAnsweredByUserName() {
        return answeredByUserName;
    }

    public void setAnsweredByUserName(String answeredByUserName) {
        this.answeredByUserName = answeredByUserName;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
}
