package application;

/**
 * Represents a Question posted by a user.
 */
public class Question {
    private int questionId;            // Primary key in the DB
    private String title;              // Short title or summary
    private String body;               // Full text of the question
    private String askedByUserName;    // Who asked it (student userName)
    private boolean resolved;          // Has the question been resolved?
    private int unreadMessagesCount;   // For potential private feedback or messages

    public Question() {
        // Default constructor
    }

    public Question(int questionId, String title, String body, String askedByUserName, boolean resolved, int unreadMessagesCount) {
        this.questionId = questionId;
        this.title = title;
        this.body = body;
        this.askedByUserName = askedByUserName;
        this.resolved = resolved;
        this.unreadMessagesCount = unreadMessagesCount;
    }

    // Getters and setters
    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAskedByUserName() {
        return askedByUserName;
    }

    public void setAskedByUserName(String askedByUserName) {
        this.askedByUserName = askedByUserName;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public void setUnreadMessagesCount(int unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
    }
}
