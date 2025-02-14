package databasePart1;
import java.sql.*;
import java.util.UUID;
import javafx.scene.control.TextArea;
import application.User;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase;AUTO_SERVER=TRUE";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// Clear the database and restart fresh if needed.
     	    //statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
	
	public Connection getConnection() {
        return connection;
    }

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "role VARCHAR(200), "
				+ "notifications VARCHAR(10000), "
				+ "forgotPassword BOOLEAN DEFAULT FALSE)";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "role VARCHAR(200), "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	    
	    // Questions table
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
                + "questionId INT AUTO_INCREMENT PRIMARY KEY, "
                + "title VARCHAR(255), "
                + "body TEXT, "
                + "askedByUserName VARCHAR(255), "
                + "resolved BOOLEAN DEFAULT FALSE, "
                + "unreadMessagesCount INT DEFAULT 0)";
        statement.execute(questionsTable);
        
        // Answers table
        String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
                + "answerId INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionId INT, "
                + "answerBody TEXT, "
                + "answeredByUserName VARCHAR(255), "
                + "isAccepted BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (questionId) REFERENCES questions(questionId))";
        statement.execute(answersTable);
	}

	// Check if the database is empty.
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
	    String insertUser = "INSERT INTO cse360users (userName, password, role, notifications) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getRole());
	        String notifications = user.getNotifications();
	        pstmt.setString(4, notifications != null ? notifications : "");
	        pstmt.executeUpdate();
	    }
	}

	// Removes a user identified by their userName.
	public boolean removeUser(String userName) {
        String query = "DELETE FROM cse360users WHERE userName = ?";
        try {
			connectToDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Retrieves the role of a user from the database using their userName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	// Generates an invitation code for a given role.
	public String generateInvitationCode(String role) {
	    // Generate a random 4-character code
	    String code = UUID.randomUUID().toString().substring(0, 4);
	    try {
			connectToDatabase();
		} catch (SQLException e) {
			// 
			e.printStackTrace();
		}
	    // SQL query to insert the invitation code and role into the InvitationCodes table
	    String query = "INSERT INTO InvitationCodes (code, role) VALUES (?, ?)";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        // Set the code and role parameters
	        pstmt.setString(1, code);
	        pstmt.setString(2, role);

	        // Execute the query
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code; // Return the generated code
	}
	
	public boolean validateInvitationCode(String code) {
	    // Use the same exact matching approach in both methods
	    String trimmed = code.trim();
	    String query = "SELECT role FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, trimmed);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                // We retrieve the role here, then mark it used
	                String role = rs.getString("role");
	                markInvitationCodeAsUsed(trimmed);
	                return true;
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	public String getInvitationRole(String code) {
	    String trimmed = code.trim();
	    String query = "SELECT role FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, trimmed);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getString("role");
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}


	
	// Marks an invitation code as used.
	public void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	// Displays all users in the given TextArea.
	public void displayAllUsers(TextArea textArea) throws SQLException {
        textArea.clear();
        String query = "SELECT id, userName, role FROM cse360users";
        connectToDatabase();
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String userName = rs.getString("userName");
                String role = rs.getString("role");
                textArea.appendText("ID: " + id + "\n");
                textArea.appendText("Username: " + userName + "\n");
                textArea.appendText("Role: " + role + "\n");
                textArea.appendText("-----------------------------\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.appendText("Error fetching user data from the database.\n");
        }
    }
	
	// Displays notifications for a user identified by userName.
	public void displayNotifications(TextArea textArea, String userName) throws SQLException {
        textArea.clear();
        String query = "SELECT notifications FROM cse360users WHERE userName = ?";
        connectToDatabase();
        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                String notifications = rs.getString("notifications");
	                if (notifications == null) {
	                	notifications = "";
	                }
	                textArea.appendText(notifications);
	            }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.appendText("Error fetching notifications from the database.\n");
        }
	}
	
	// Retrieves the role of a user by userName
	public String getUserRoleByUsername(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Updates a user's role based on their userName.
	public boolean changeUserRole(String userName, String role) {
        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, role);
            pstmt.setString(2, userName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	//Add notification to user for OTP
	public boolean addNotificationToUser(String notification, String userName) {
		String query = "UPDATE cse360users SET notifications = CONCAT(COALESCE(notifications, ''), '\n', ?) WHERE userName = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Set the role and userId parameters
            pstmt.setString(1, notification);
            pstmt.setString(2, userName);

            // Execute the query
            int rowsAffected = pstmt.executeUpdate();

            // Return true if a row was updated, false otherwise
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs
        }
	}
	
	// Retrieves the number of newline characters in the notifications for a user identified by userName.
	public int getNumNotifications(String userName) {
		String query = "SELECT notifications FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return countNewlines(rs.getString("notifications"));
                }
            }
		} 
		catch (SQLException e) {
			e.printStackTrace();
	    }
		return -1;		
	}
	
	// Retrieves the notifications for a user identified by userName.
	public String getNotifications(String userName) {
		String query = "SELECT notifications FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("notifications");
                }
            }
		} 
		catch (SQLException e) {
			e.printStackTrace();
	    }
		return "";		
	}
	
	public static int countNewlines(String text) {
	    if (text == null || text.isEmpty()) {
	        return 0;
	    }
	    return text.split("\n", -1).length - 1;
	}
	
	// Toggles the forgotPassword status for a user identified by userName.
	public boolean setForgetPassword(String userName) {
	    String selectQuery = "SELECT forgotPassword FROM cse360users WHERE userName = ?";
	    String updateQuery = "UPDATE cse360users SET forgotPassword = ? WHERE userName = ?";

	    try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
	         PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
	        selectStmt.setString(1, userName);
	        try (ResultSet rs = selectStmt.executeQuery()) {
	            if (rs.next()) {
	                boolean currentValue = rs.getBoolean("forgotPassword");
	                boolean newValue = !currentValue;
	                updateStmt.setBoolean(1, newValue);
	                updateStmt.setString(2, userName);
	                int rowsUpdated = updateStmt.executeUpdate();
	                return rowsUpdated > 0;
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Updates the password for a user identified by userName.
	public boolean setPassword(String userName, String password) {
	    String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, password);
	        pstmt.setString(2, userName);
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	// Retrieves the forgotPassword status for a user identified by userName.
	public boolean getForgotPasswordStatus(String userName) {
	    String query = "SELECT forgotPassword FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getBoolean("forgotPassword");
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Deletes a specific notification line for a user identified by userName.
	public boolean deleteNotificationLine(String userName, String notification) {
	    String currentNotifications = getNotifications(userName);
	    if (currentNotifications == null || currentNotifications.isEmpty()) {
	        return false;
	    }
	    String[] lines = currentNotifications.split("\n");
	    StringBuilder updatedNotifications = new StringBuilder();
	    boolean lineFound = false;
	    for (String line : lines) {
	        if (!line.contains(notification)) {
	            updatedNotifications.append(line).append("\n");
	        } else {
	            lineFound = true;
	        }
	    }
	    if (!lineFound) {
	        return false;
	    }
	    if (updatedNotifications.length() > 0) {
	        updatedNotifications.setLength(updatedNotifications.length() - 1);
	    }
	    String query = "UPDATE cse360users SET notifications = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, updatedNotifications.toString());
	        pstmt.setString(2, userName);
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	// Create a new question
    public boolean createQuestion(application.Question question) {
        String sql = "INSERT INTO questions (title, body, askedByUserName, resolved, unreadMessagesCount) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getBody());
            pstmt.setString(3, question.getAskedByUserName());
            pstmt.setBoolean(4, question.isResolved());
            pstmt.setInt(5, question.getUnreadMessagesCount());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Retrieve all unresolved questions
    public ResultSet getUnresolvedQuestions() {
        String sql = "SELECT * FROM questions WHERE resolved = FALSE";
        try {
            return connection.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	//Creates answers for unresolved questions
	public boolean createAnswer(application.Answer answer) {
        String sql = "INSERT INTO answers (questionId, answerBody, answeredByUserName, isAccepted) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answer.getQuestionId());
            pstmt.setString(2, answer.getAnswerBody());
            pstmt.setString(3, answer.getAnsweredByUserName());
            pstmt.setBoolean(4, answer.isAccepted());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Retrieve answers for a given question
    public ResultSet getAnswersForQuestion(int questionId) {
        String sql = "SELECT * FROM answers WHERE questionId = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, questionId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Update question resolution status
    public boolean updateQuestionResolved(int questionId, boolean resolved) {
        try {
            connectToDatabase();  
            String sql = "UPDATE questions SET resolved = ? WHERE questionId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setBoolean(1, resolved);
                pstmt.setInt(2, questionId);
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    
    // Update answer acceptance status
    public boolean updateAnswerAccepted(int answerId, boolean isAccepted) {
        String sql = "UPDATE answers SET isAccepted = ? WHERE answerId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, isAccepted);
            pstmt.setInt(2, answerId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    //Delete Questions and Answers
    public boolean deleteQuestionAndAnswers(int questionId) {
        try {
            // 1) Delete all answers for this question
            String deleteAnswersSQL = "DELETE FROM answers WHERE questionId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswersSQL)) {
                pstmt.setInt(1, questionId);
                pstmt.executeUpdate();
            }

            // 2) Now delete the question
            String deleteQuestionSQL = "DELETE FROM questions WHERE questionId = ?";
            try (PreparedStatement pstmt2 = connection.prepareStatement(deleteQuestionSQL)) {
                pstmt2.setInt(1, questionId);
                int rows = pstmt2.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
