package application;

import databasePart1.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StoreQuestions handles Create, Read, Update, and Delete operations for Question objects in the DB.
 */
public class StoreQuestions {

    private DatabaseHelper databaseHelper;

    public StoreQuestions(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Create a new question in the DB
    public boolean createQuestion(Question question) {
        String insertSQL = "INSERT INTO questions (title, body, askedByUserName, resolved, unreadMessagesCount) "
                         + "VALUES (?, ?, ?, ?, ?)";
        try {
            databaseHelper.connectToDatabase(); // ensure DB connection
            try (PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
                pstmt.setString(1, question.getTitle());
                pstmt.setString(2, question.getBody());
                pstmt.setString(3, question.getAskedByUserName());
                pstmt.setBoolean(4, question.isResolved());
                pstmt.setInt(5, question.getUnreadMessagesCount());
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Read a question by its ID
    public Question readQuestion(int questionId) {
        String selectSQL = "SELECT * FROM questions WHERE questionId = ?";
        try {
            databaseHelper.connectToDatabase();
            try (PreparedStatement pstmt = getConnection().prepareStatement(selectSQL)) {
                pstmt.setInt(1, questionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Question q = new Question();
                        q.setQuestionId(rs.getInt("questionId"));
                        q.setTitle(rs.getString("title"));
                        q.setBody(rs.getString("body"));
                        q.setAskedByUserName(rs.getString("askedByUserName"));
                        q.setResolved(rs.getBoolean("resolved"));
                        q.setUnreadMessagesCount(rs.getInt("unreadMessagesCount"));
                        return q;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update an existing question
    public boolean updateQuestion(Question question) {
        String updateSQL = "UPDATE questions SET title = ?, body = ?, askedByUserName = ?, "
                         + "resolved = ?, unreadMessagesCount = ? WHERE questionId = ?";
        try {
            databaseHelper.connectToDatabase();
            try (PreparedStatement pstmt = getConnection().prepareStatement(updateSQL)) {
                pstmt.setString(1, question.getTitle());
                pstmt.setString(2, question.getBody());
                pstmt.setString(3, question.getAskedByUserName());
                pstmt.setBoolean(4, question.isResolved());
                pstmt.setInt(5, question.getUnreadMessagesCount());
                pstmt.setInt(6, question.getQuestionId());
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    

    // List all unresolved questions
    public List<Question> listAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String querySQL = "SELECT * FROM questions"; // no WHERE clause => all
        try {
            databaseHelper.connectToDatabase();
            try (PreparedStatement pstmt = getConnection().prepareStatement(querySQL);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setQuestionId(rs.getInt("questionId"));
                    q.setTitle(rs.getString("title"));
                    q.setBody(rs.getString("body"));
                    q.setAskedByUserName(rs.getString("askedByUserName"));
                    q.setResolved(rs.getBoolean("resolved"));
                    q.setUnreadMessagesCount(rs.getInt("unreadMessagesCount"));
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }


    // Helper to get the actual DB connection
    private Connection getConnection() {
        // Instead of using reflection, simply use the public getter from DatabaseHelper.
        return databaseHelper.getConnection();
    }
}
