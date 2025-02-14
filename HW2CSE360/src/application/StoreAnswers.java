package application;

import databasePart1.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StoreAnswers handles CRUD for Answer objects.
 */
public class StoreAnswers {

    private DatabaseHelper databaseHelper;

    public StoreAnswers(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Create a new answer
    public boolean createAnswer(Answer ans) {
        String insertSQL = "INSERT INTO answers (questionId, answerBody, answeredByUserName, isAccepted) "
                         + "VALUES (?, ?, ?, ?)";
        try {
            databaseHelper.connectToDatabase();
            try (PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
                pstmt.setInt(1, ans.getQuestionId());
                pstmt.setString(2, ans.getAnswerBody());
                pstmt.setString(3, ans.getAnsweredByUserName());
                pstmt.setBoolean(4, ans.isAccepted());
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Read an answer by its ID
    public Answer readAnswer(int answerId) {
        String selectSQL = "SELECT * FROM answers WHERE answerId = ?";
        try {
            databaseHelper.connectToDatabase();
            try (PreparedStatement pstmt = getConnection().prepareStatement(selectSQL)) {
                pstmt.setInt(1, answerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Answer a = new Answer();
                        a.setAnswerId(rs.getInt("answerId"));
                        a.setQuestionId(rs.getInt("questionId"));
                        a.setAnswerBody(rs.getString("answerBody"));
                        a.setAnsweredByUserName(rs.getString("answeredByUserName"));
                        a.setAccepted(rs.getBoolean("isAccepted"));
                        return a;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update an existing answer
    public boolean updateAnswer(Answer ans) {
        String updateSQL = "UPDATE answers SET questionId = ?, answerBody = ?, answeredByUserName = ?, isAccepted = ? "
                         + "WHERE answerId = ?";
        try {
            databaseHelper.connectToDatabase();
            try (PreparedStatement pstmt = getConnection().prepareStatement(updateSQL)) {
                pstmt.setInt(1, ans.getQuestionId());
                pstmt.setString(2, ans.getAnswerBody());
                pstmt.setString(3, ans.getAnsweredByUserName());
                pstmt.setBoolean(4, ans.isAccepted());
                pstmt.setInt(5, ans.getAnswerId());
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete an answer by ID
    public boolean deleteAnswer(int answerId) {
        String deleteSQL = "DELETE FROM answers WHERE answerId = ?";
        try {
            databaseHelper.connectToDatabase();
            try (PreparedStatement pstmt = getConnection().prepareStatement(deleteSQL)) {
                pstmt.setInt(1, answerId);
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // List all answers for a given question
    public List<Answer> listAnswersByQuestion(int questionId) {
        List<Answer> ansList = new ArrayList<>();
        String querySQL = "SELECT * FROM answers WHERE questionId = ?";
        try {
            databaseHelper.connectToDatabase();
            try (PreparedStatement pstmt = getConnection().prepareStatement(querySQL)) {
                pstmt.setInt(1, questionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Answer a = new Answer();
                        a.setAnswerId(rs.getInt("answerId"));
                        a.setQuestionId(rs.getInt("questionId"));
                        a.setAnswerBody(rs.getString("answerBody"));
                        a.setAnsweredByUserName(rs.getString("answeredByUserName"));
                        a.setAccepted(rs.getBoolean("isAccepted"));
                        ansList.add(a);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ansList;
    }

    // Helper to get the DB connection from DatabaseHelper
    private Connection getConnection() {
        return databaseHelper.getConnection();
    }
}
