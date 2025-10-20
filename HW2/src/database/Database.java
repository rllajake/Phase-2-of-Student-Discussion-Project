package database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import entityClasses.User;

/*******
 * <p> Title: Database Class. </p>
 *
 * <p> Description: H2 file DB at ~/FoundationDatabase. Creates schema and offers
 * legacy admin features plus student posts/replies.</p>
 *
 * @version 2.03  2025-10-14
 *  - Make isDatabaseEmpty() robust and not depend on instance state
 *  - Ensure all schema is created in ensureSchema(...)
 *  - Seed threads safely (omit id for IDENTITY)
 *  - getConnection() now throws SQLException (no generic Exception)
 *  - Add missing final returns to boolean methods
 */
public class Database {

    // JDBC + DB location
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL      = "jdbc:h2:~/FoundationDatabase";

    // Credentials
    static final String USER = "sa";
    static final String PASS = "";

    // Legacy/instance connection for admin features
    private Connection connection = null;
    private Statement  statement  = null;

    // Current user snapshot for UI convenience
    private String  currentUsername;
    private String  currentPassword;
    private String  currentFirstName;
    private String  currentMiddleName;
    private String  currentLastName;
    private String  currentPreferredFirstName;
    private String  currentEmailAddress;
    private boolean currentAdminRole;
    private boolean currentStudentRole;
    private boolean currentReviewerRole;

    public Database() {}

    /*========================= Legacy Admin Connection =========================*/

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement  = connection.createStatement();
            // statement.execute("DROP ALL OBJECTS"); // (optional) clean reset
            ensureSchema(connection);                // make sure all tables exist
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found", e);
        }
    }

    /** Legacy method (kept for compatibility) */
    private void createTables() throws SQLException {
        // userDB
        String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "firstName VARCHAR(255), "
                + "middleName VARCHAR(255), "
                + "lastName VARCHAR(255), "
                + "preferredFirstName VARCHAR(255), "
                + "emailAddress VARCHAR(255), "
                + "adminRole BOOL DEFAULT FALSE, "
                + "studentRole BOOL DEFAULT FALSE, "
                + "reviewerRole BOOL DEFAULT FALSE)";
        statement.execute(userTable);

        // InvitationCodes
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "emailAddress VARCHAR(255), "
                + "deadline TIMESTAMP, "
                + "role VARCHAR(10))";
        statement.execute(invitationCodesTable);

        // OneTimePass
        String oneTimePassTable = "CREATE TABLE IF NOT EXISTS OneTimePass ("
                + "emailAddress VARCHAR(255), "
                + "password VARCHAR(32) PRIMARY KEY, "
                + "used BOOL DEFAULT FALSE)";
        statement.execute(oneTimePassTable);
    }

    /*========================= First-Run / Counts =========================*/

    /** Robust first-run check: if anything goes wrong, treat DB as empty. */
    public boolean isDatabaseEmpty() {
        final String sql = "SELECT COUNT(*) AS count FROM userDB";
        try {
            if (statement != null) {
                ResultSet rs = statement.executeQuery(sql);
                if (rs.next()) return rs.getInt("count") == 0;
                return true;
            } else {
                // no instance connection yet → use shared connection that ensures schema
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement(sql)) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) return rs.getInt(1) == 0;
                    return true;
                }
            }
        } catch (SQLException e) {
            // Table missing or any SQL issue → consider it empty to trigger Admin Sign-Up
            return true;
        }
    }

    public int getNumberOfUsers() {
        final String sql = "SELECT COUNT(*) AS count FROM userDB";
        try {
            if (statement != null) {
                ResultSet rs = statement.executeQuery(sql);
                if (rs.next()) return rs.getInt("count");
                return 0;
            } else {
                try (Connection c = getConnection();
                     PreparedStatement ps = c.prepareStatement(sql)) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) return rs.getInt(1);
                    return 0;
                }
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    /*========================= Admin / Users =========================*/

    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
                + "lastName, preferredFirstName, emailAddress, adminRole, studentRole, reviewerRole) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            currentUsername = user.getUserName();        pstmt.setString(1, currentUsername);
            currentPassword = user.getPassword();        pstmt.setString(2, currentPassword);
            currentFirstName = user.getFirstName();      pstmt.setString(3, currentFirstName);
            currentMiddleName = user.getMiddleName();    pstmt.setString(4, currentMiddleName);
            currentLastName = user.getLastName();        pstmt.setString(5, currentLastName);
            currentPreferredFirstName = user.getPreferredFirstName();
                                                        pstmt.setString(6, currentPreferredFirstName);
            currentEmailAddress = user.getEmailAddress();pstmt.setString(7, currentEmailAddress);
            currentAdminRole = user.getAdminRole();      pstmt.setBoolean(8, currentAdminRole);
            currentStudentRole = user.getStudentRole();  pstmt.setBoolean(9, currentStudentRole);
            currentReviewerRole = user.getReviewerRole();pstmt.setBoolean(10, currentReviewerRole);
            pstmt.executeUpdate();
        }
    }

    public void removeUser(String username) throws SQLException {
        try (PreparedStatement pstmt =
                     connection.prepareStatement("DELETE from userDB WHERE userName = ?")) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    public List<String> getUserList () {
        List<String> userList = new ArrayList<>();
        userList.add("<Select a User>");
        String query = "SELECT userName FROM userDB";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) userList.add(rs.getString("userName"));
        } catch (SQLException e) { return null; }
        return userList;
    }

    public boolean loginAdmin(User user){
        String q = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND adminRole = TRUE";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            return ps.executeQuery().next();
        } catch  (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean loginStudent(User user) {
        String q = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND studentRole = TRUE";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            return ps.executeQuery().next();
        } catch  (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean loginReviewer(User user) {
        String q = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND reviewerRole = TRUE";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            return ps.executeQuery().next();
        } catch  (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean doesUserExist(String userName) {
        String q = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int getNumberOfRoles (User user) {
        int n = 0;
        if (user.getAdminRole()) n++;
        if (user.getStudentRole()) n++;
        if (user.getReviewerRole()) n++;
        return n;
    }

    public String generateInvitationCode(String emailAddress, String role) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        String q = "INSERT INTO InvitationCodes (code, emailaddress, deadline, role) VALUES (?, ?, ?, ?)";
        Timestamp deadline = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, code);
            ps.setString(2, emailAddress);
            ps.setTimestamp(3, deadline);
            ps.setString(4, role);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        currentPassword = code;
        return code;
    }

    public boolean checkInvitationCode(String code) {
        String q = "SELECT deadline from InvitationCodes WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp deadline = rs.getTimestamp("deadline");
                return !LocalDateTime.now().isAfter(deadline.toLocalDateTime());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }

    public void clearOldInvitationCode(String emailAddress) {
        try (PreparedStatement p1 =
                     connection.prepareStatement("DELETE from InvitationCodes where emailAddress = ?")) {
            p1.setString(1, emailAddress);
            p1.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement p2 =
                     connection.prepareStatement("DELETE from userDB where emailAddress = ?")) {
            p2.setString(1, emailAddress);
            p2.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String generateOneTimePass(String emailAddress) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        String q = "INSERT INTO OneTimePass (emailAddress, password, used) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, emailAddress);
            ps.setString(2, code);
            ps.setBoolean(3, false);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        currentPassword = code;
        return code;
    }

    public void updatePassword(String password, String emailAddress) {
        try (PreparedStatement ps =
                     connection.prepareStatement("UPDATE userDB SET password = ? WHERE emailAddress = ?")) {
            ps.setString(1, password);
            ps.setString(2, emailAddress);
            ps.executeUpdate();
            currentEmailAddress = emailAddress;
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int getNumberOfInvitations() {
        try {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) AS count FROM InvitationCodes");
            if (rs.next()) return rs.getInt("count");
        } catch  (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean emailaddressHasBeenUsed(String emailAddress) {
        String q = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ? AND code IS NOT NULL";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, emailAddress);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("count") > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public String getRoleGivenAnInvitationCode(String code) {
        String q = "SELECT * FROM InvitationCodes WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    public String getEmailAddressUsingCode (String code ) {
        String q = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("emailAddress");
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    public void removeInvitationAfterUse(String code) {
        String q = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                try (PreparedStatement del =
                             connection.prepareStatement("DELETE FROM InvitationCodes WHERE code = ?")) {
                    del.setString(1, code);
                    del.executeUpdate();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updatePassword2(String password, String username) {
        try (PreparedStatement ps =
                     connection.prepareStatement("UPDATE userDB SET password = ? WHERE userName = ?")) {
            ps.setString(1, password);
            ps.setString(2, username);
            ps.executeUpdate();
            currentPassword = password;
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getFirstName(String username) {
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT firstName FROM userDB WHERE userName = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("firstName");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateFirstName(String username, String firstName) {
        try (PreparedStatement ps =
                     connection.prepareStatement("UPDATE userDB SET firstName = ? WHERE username = ?")) {
            ps.setString(1, firstName);
            ps.setString(2, username);
            ps.executeUpdate();
            currentFirstName = firstName;
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getMiddleName(String username) {
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT middleName FROM userDB WHERE userName = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("middleName");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateMiddleName(String username, String middleName) {
        try (PreparedStatement ps =
                     connection.prepareStatement("UPDATE userDB SET middleName = ? WHERE username = ?")) {
            ps.setString(1, middleName);
            ps.setString(2, username);
            ps.executeUpdate();
            currentMiddleName = middleName;
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getLastName(String username) {
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT lastName FROM userDB WHERE userName = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("lastName");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateLastName(String username, String lastName) {
        try (PreparedStatement ps =
                     connection.prepareStatement("UPDATE userDB SET lastName = ? WHERE username = ?")) {
            ps.setString(1, lastName);
            ps.setString(2, username);
            ps.executeUpdate();
            currentLastName = lastName;
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getPreferredFirstName(String username) {
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT preferredFirstName FROM userDB WHERE userName = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("preferredFirstName");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updatePreferredFirstName(String username, String preferredFirstName) {
        try (PreparedStatement ps =
                     connection.prepareStatement("UPDATE userDB SET preferredFirstName = ? WHERE username = ?")) {
            ps.setString(1, preferredFirstName);
            ps.setString(2, username);
            ps.executeUpdate();
            currentPreferredFirstName = preferredFirstName;
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getEmailAddress(String username) {
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT emailAddress FROM userDB WHERE userName = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("emailAddress");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateEmailAddress(String username, String emailAddress) {
        try (PreparedStatement ps =
                     connection.prepareStatement("UPDATE userDB SET emailAddress = ? WHERE username = ?")) {
            ps.setString(1, emailAddress);
            ps.setString(2, username);
            ps.executeUpdate();
            currentEmailAddress = emailAddress;
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean getUserAccountDetails(String username) {
        String q = "SELECT * FROM userDB WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            rs.next();
            currentUsername           = rs.getString(2);
            currentPassword           = rs.getString(3);
            currentFirstName          = rs.getString(4);
            currentMiddleName         = rs.getString(5);
            currentLastName           = rs.getString(6);
            currentPreferredFirstName = rs.getString(7);
            currentEmailAddress       = rs.getString(8);
            currentAdminRole          = rs.getBoolean(9);
            currentStudentRole        = rs.getBoolean(10);
            currentReviewerRole       = rs.getBoolean(11);
            return true;
        } catch (SQLException e) { return false; }
    }

    public boolean updateUserRole(String username, String role, String value) {
        if (role.compareTo("Admin") == 0) {
            try (PreparedStatement ps =
                         connection.prepareStatement("UPDATE userDB SET adminRole = ? WHERE username = ?")) {
                ps.setString(1, value);
                ps.setString(2, username);
                ps.executeUpdate();
                currentAdminRole = "true".equals(value);
                return true;
            } catch (SQLException e) { return false; }
        }
        if (role.compareTo("Student") == 0) {
            try (PreparedStatement ps =
                         connection.prepareStatement("UPDATE userDB SET studentRole = ? WHERE username = ?")) {
                ps.setString(1, value);
                ps.setString(2, username);
                ps.executeUpdate();
                currentStudentRole = "true".equals(value);
                return true;
            } catch (SQLException e) { return false; }
        }
        if (role.compareTo("Reviewer") == 0) {
            try (PreparedStatement ps =
                         connection.prepareStatement("UPDATE userDB SET reviewerRole = ? WHERE username = ?")) {
                ps.setString(1, value);
                ps.setString(2, username);
                ps.executeUpdate();
                currentReviewerRole = "true".equals(value);
                return true;
            } catch (SQLException e) { return false; }
        }
        return false;
    }

    /*======================== Student Posts / Replies ========================*/

    public static long upsertThread(String name) throws Exception {
        if (name == null || name.isBlank()) name = "General";
        try (Connection c = getConnection()) {
            ensureSchema(c);
            try (PreparedStatement sel = c.prepareStatement(
                    "SELECT id FROM threads WHERE LOWER(name)=LOWER(?)")) {
                sel.setString(1, name);
                try (ResultSet rs = sel.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO threads(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ins.setString(1, name);
                ins.executeUpdate();
                try (ResultSet rs = ins.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            try (PreparedStatement sel2 = c.prepareStatement(
                    "SELECT id FROM threads WHERE LOWER(name)=LOWER(?)")) {
                sel2.setString(1, name);
                try (ResultSet rs2 = sel2.executeQuery()) {
                    if (rs2.next()) return rs2.getLong(1);
                }
            }
            throw new IllegalStateException("upsertThread failed for: " + name);
        }
    }
    
    // Return thread id by exact name (case-insensitive); null if not found
    public static Long fetchThreadIdByName(Connection c, String name) throws SQLException {
        if (name == null || name.isBlank()) return null;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id FROM threads WHERE LOWER(name)=LOWER(?)")) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return null;
    }
    
 // Return "General" id (exists by ensureSchema)
    public static long getGeneralThreadId(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id FROM threads WHERE name='General'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        // fallback (shouldn't happen)
        try (PreparedStatement ins = c.prepareStatement(
                "INSERT INTO threads(name) VALUES('General')",
                Statement.RETURN_GENERATED_KEYS)) {
            ins.executeUpdate();
            try (ResultSet rs = ins.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Failed to ensure 'General' thread.");
    }
    
    public static long createPostStudent(
            String author,
            String thread,        // e.g., "General"
            String title,
            String kind,          // e.g., "Post" or "Question"
            String content,
            boolean isPrivate,
            boolean isAnonymous
    ) throws SQLException {
        // Normalize thread to default "General" when null/blank
        thread = (thread == null || thread.isBlank()) ? "General" : thread.trim();

        final String sql = """
            INSERT INTO posts(author, thread, title, kind, content, is_private, is_anonymous, created_at)
            VALUES (?,?,?,?,?,?,?, CURRENT_TIMESTAMP)
            """;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ensureSchema(c);
            ps.setString(1, author);
            ps.setString(2, thread);
            ps.setString(3, title);
            ps.setString(4, kind);
            ps.setString(5, content);
            ps.setBoolean(6, isPrivate);
            ps.setBoolean(7, isAnonymous);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return -1L;
        }
    }


    
    public static long addReply(String author, long postId, String content) throws SQLException {
        final String sql = """
            INSERT INTO replies (post_id, author, content, created_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
        """;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ensureSchema(c);
            ps.setLong(1, postId);
            ps.setString(2, author);
            ps.setString(3, content);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return -1L; // fallback if the driver doesn’t return keys
        }
    }

    // tiny helper so static ops don't depend on instance state
    private static Connection open() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);         // keep your existing constants
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /** Ownership enforcing helpers (UI should use these) */
    
    /** Soft-delete a post, only if authored by 'author'. */
    public static void softDeletePostByAuthor(long postId, String author) throws SQLException {
        if (author == null || author.isBlank()) throw new SQLException("Missing author");
        final String PLACEHOLDER = "[This post has been deleted]";
        final String sql = """
            UPDATE posts
               SET deleted = TRUE,
                   content = ?,
                   deleted_at = CURRENT_TIMESTAMP
             WHERE id = ? AND author = ?
               AND COALESCE(deleted, FALSE) = FALSE
        """;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, PLACEHOLDER);
            ps.setLong(2, postId);
            ps.setString(3, author);
            int n = ps.executeUpdate();
            if (n == 0) throw new SQLException("You can only delete your own non-deleted post.");
        }
    }

    /** Update a post's title/body, only if authored by 'author'. */
    public static void updatePostByAuthor(long postId, String author, String newTitle, String newContent) throws SQLException {
        if (author == null || author.isBlank()) throw new SQLException("Missing author");
        final String sql = """
            UPDATE posts
               SET title = ?,
                   content = ?,
                   updated_at = CURRENT_TIMESTAMP
             WHERE id = ? AND author = ?
               AND COALESCE(deleted, FALSE) = FALSE
        """;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newTitle == null ? "" : newTitle.trim());
            ps.setString(2, newContent == null ? "" : newContent.trim());
            ps.setLong(3, postId);
            ps.setString(4, author);
            int n = ps.executeUpdate();
            if (n == 0) throw new SQLException("You can only edit your own non-deleted post.");
        }
    }

    /** Update a reply's content, only if 'author' wrote it. */
    public static void updateReplyByAuthor(long replyId, String author, String newContent) throws SQLException {
        if (author == null || author.isBlank()) throw new SQLException("Missing author");
        final String sql = "UPDATE replies SET content=? WHERE id=? AND author=?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newContent == null ? "" : newContent.trim());
            ps.setLong(2, replyId);
            ps.setString(3, author);
            int n = ps.executeUpdate();
            if (n == 0) throw new SQLException("You can only edit your own reply.");
        }
    }

    /** Delete a reply, only if 'author' wrote it. */
    public static void deleteReplyByAuthor(long replyId, String author) throws SQLException {
        if (author == null || author.isBlank()) throw new SQLException("Missing author");
        final String sql = "DELETE FROM replies WHERE id=? AND author=?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, replyId);
            ps.setString(2, author);
            int n = ps.executeUpdate();
            if (n == 0) throw new SQLException("You can only delete your own reply.");
        }
    }

    
    public static java.util.List<java.util.Map<String,Object>> findPosts(
            String keyword, String threadName, String author) throws Exception {

        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.author, p.thread AS thread,
                   p.title, p.kind, p.content,
                   COALESCE(p.deleted, FALSE) AS deleted,
                   p.created_at,
                   (SELECT COUNT(*) FROM replies r WHERE r.post_id = p.id) AS reply_count
              FROM posts p
             WHERE 1=1
        """);

        java.util.List<Object> params = new java.util.ArrayList<>();

        if (threadName != null && !threadName.isBlank()) {
            sql.append(" AND LOWER(p.thread) = LOWER(?)");
            params.add(threadName.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase().trim() + "%";
            sql.append(" AND (LOWER(p.title) LIKE ? OR LOWER(p.content) LIKE ?)");
            params.add(kw);
            params.add(kw);
        }
        if (author != null && !author.isBlank()) {
            sql.append(" AND p.author = ?");
            params.add(author.trim());
        }

        sql.append(" ORDER BY p.created_at DESC, p.id DESC");

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ensureSchema(c);
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s)      ps.setString(i + 1, s);
                else if (p instanceof Long l)   ps.setLong(i + 1, l);
                else                            ps.setObject(i + 1, p);
            }

            java.util.List<java.util.Map<String,Object>> out = new java.util.ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String,Object> row = new java.util.HashMap<>();
                    row.put("id",          rs.getLong("id"));
                    row.put("author",      rs.getString("author"));
                    row.put("thread",      rs.getString("thread"));
                    row.put("title",       rs.getString("title"));
                    row.put("kind",        rs.getString("kind"));
                    row.put("content",     rs.getString("content"));
                    row.put("deleted",     rs.getBoolean("deleted"));
                    row.put("created_at",  rs.getTimestamp("created_at"));
                    row.put("reply_count", rs.getInt("reply_count"));
                    out.add(row);
                }
            }
            return out;
        }
    }


    public static java.util.List<java.util.Map<String,Object>> findReplies(long postId) throws Exception {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id, author, content, created_at FROM replies WHERE post_id=? ORDER BY created_at ASC, id ASC")) {
            ensureSchema(c);
            ps.setLong(1, postId);
            java.util.List<java.util.Map<String,Object>> out = new java.util.ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String,Object> row = new java.util.HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("author", rs.getString("author"));
                    row.put("content", rs.getString("content"));
                    row.put("created_at", rs.getTimestamp("created_at"));
                    out.add(row);
                }
            }
            return out;
        }
    }

    /*======================== Connection + Schema ========================*/

 // Optional: track schema init exactly once
    private static volatile boolean SCHEMA_READY = false;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            Connection c = DriverManager.getConnection(DB_URL, USER, PASS);
            if (!SCHEMA_READY) {
                // ensureSchema is idempotent; this just makes it cheaper after first call
                ensureSchema(c);
                SCHEMA_READY = true;
            }
            return c; // callers can safely close this in try-with-resources
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found", e);
        }
    }

    /** Global schema creation (idempotent). Safe no matter who calls it first. */
    private static void ensureSchema(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            // --- Core admin/user tables (so first-run checks always succeed) ---
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS userDB (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  userName VARCHAR(255) UNIQUE,
                  password VARCHAR(255),
                  firstName VARCHAR(255),
                  middleName VARCHAR(255),
                  lastName VARCHAR(255),
                  preferredFirstName VARCHAR(255),
                  emailAddress VARCHAR(255),
                  adminRole BOOL DEFAULT FALSE,
                  studentRole BOOL DEFAULT FALSE,
                  reviewerRole BOOL DEFAULT FALSE
                )
            """);

            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS InvitationCodes(
                  code VARCHAR(10) PRIMARY KEY,
                  emailAddress VARCHAR(255),
                  deadline TIMESTAMP,
                  role VARCHAR(10)
                )
            """);

            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS OneTimePass(
                  emailAddress VARCHAR(255),
                  password VARCHAR(32) PRIMARY KEY,
                  used BOOL DEFAULT FALSE
                )
            """);

            // --- Threads (must exist before posts) ---
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS threads(
                  id IDENTITY PRIMARY KEY,
                  name VARCHAR(120) NOT NULL UNIQUE
                )
            """);

            // --- Posts ---
            
            try (Statement st = c.createStatement()) {
                // Base tables if missing
                st.execute("""
                    CREATE TABLE IF NOT EXISTS posts (
                      id IDENTITY PRIMARY KEY,
                      author VARCHAR(100) NOT NULL,
                      title VARCHAR(255),
                      content CLOB,
                      thread VARCHAR(100) DEFAULT 'General',
                      kind VARCHAR(50) DEFAULT 'Post',
                      is_private BOOLEAN DEFAULT FALSE,
                      is_anonymous BOOLEAN DEFAULT FALSE,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP,
                      deleted BOOLEAN DEFAULT FALSE,
                      deleted_at TIMESTAMP
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS replies (
                      id IDENTITY PRIMARY KEY,
                      post_id BIGINT NOT NULL,
                      author VARCHAR(100) NOT NULL,
                      content CLOB,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP,
                      FOREIGN KEY (post_id) REFERENCES posts(id)
                    )
                """);

                // If your DB was created earlier without these columns, add them:
                st.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS thread VARCHAR(100) DEFAULT 'General'");
                st.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS kind VARCHAR(50) DEFAULT 'Post'");
                st.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS is_private BOOLEAN DEFAULT FALSE");
                st.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS is_anonymous BOOLEAN DEFAULT FALSE");
                // Ensure soft-delete columns exist (H2 supports IF NOT EXISTS)
                st.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE");
                st.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP");

                // Optional: a real threads table (staff manages this). We’ll seed “General”.
                st.execute("""
                    CREATE TABLE IF NOT EXISTS threads (
                      id IDENTITY PRIMARY KEY,
                      name VARCHAR(100) UNIQUE NOT NULL
                    )
                """);
                st.execute("""
                    INSERT INTO threads(name)
                    SELECT 'General'
                    WHERE NOT EXISTS (SELECT 1 FROM threads WHERE name = 'General')
                """);
             
            }

            // --- Read tracking ---
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS post_reads(
                  user_name VARCHAR(120) NOT NULL,
                  post_id BIGINT NOT NULL,
                  last_read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY(user_name, post_id),
                  CONSTRAINT fk_post_reads_post FOREIGN KEY (post_id) REFERENCES posts(id)
                )
            """);

            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS reply_reads(
                  user_name VARCHAR(120) NOT NULL,
                  reply_id BIGINT NOT NULL,
                  read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY(user_name, reply_id),
                  CONSTRAINT fk_reply_reads_reply FOREIGN KEY (reply_id) REFERENCES replies(id)
                )
            """);

            // --- Seed default thread (omit id so IDENTITY auto-generates) ---
            s.executeUpdate("""
                MERGE INTO threads (name)
                KEY(name)
                VALUES ('General')
            """);
        }
    }

    /*======================== Read/Unread helpers ========================*/

    // Keep your existing helpers (markPostViewed, getUnreadReplyCount, listReplies, etc.) as-is.
    // Below are the explicit checkbox-driven helpers.

    public static boolean isPostRead(String userName, long postId) throws Exception {
        if (userName == null) return false;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM post_reads WHERE user_name=? AND post_id=?")) {
            ps.setString(1, userName);
            ps.setLong(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void setPostRead(String userName, long postId, boolean read) throws Exception {
        if (userName == null) return;
        try (Connection c = getConnection()) {
            if (read) {
                try (PreparedStatement ps = c.prepareStatement("""
                    MERGE INTO post_reads(user_name, post_id, last_read_at)
                    KEY(user_name, post_id)
                    VALUES(?, ?, CURRENT_TIMESTAMP)
                """)) {
                    ps.setString(1, userName);
                    ps.setLong(2, postId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM post_reads WHERE user_name=? AND post_id=?")) {
                    ps.setString(1, userName);
                    ps.setLong(2, postId);
                    ps.executeUpdate();
                }
            }
        }
    }

    public static boolean isReplyRead(String userName, long replyId) throws Exception {
        if (userName == null) return false;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM reply_reads WHERE user_name=? AND reply_id=?")) {
            ps.setString(1, userName);
            ps.setLong(2, replyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void setReplyRead(String userName, long replyId, boolean read) throws Exception {
        if (userName == null) return;
        try (Connection c = getConnection()) {
            if (read) {
                try (PreparedStatement ps = c.prepareStatement("""
                    MERGE INTO reply_reads(user_name, reply_id, read_at)
                    KEY(user_name, reply_id)
                    VALUES(?, ?, CURRENT_TIMESTAMP)
                """)) {
                    ps.setString(1, userName);
                    ps.setLong(2, replyId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM reply_reads WHERE user_name=? AND reply_id=?")) {
                    ps.setString(1, userName);
                    ps.setLong(2, replyId);
                    ps.executeUpdate();
                }
            }
        }
    }

    public static void markPostViewed(String userName, long postId) throws Exception {
        if (userName == null) return;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 MERGE INTO post_reads(user_name, post_id, last_read_at)
                 KEY(user_name, post_id)
                 VALUES(?, ?, CURRENT_TIMESTAMP)
             """)) {
            ps.setString(1, userName);
            ps.setLong(2, postId);
            ps.executeUpdate();
        }
    }

    public static int getUnreadReplyCount(String userName, long postId) throws Exception {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT COUNT(*)
                 FROM replies r
                 LEFT JOIN reply_reads rr
                   ON rr.reply_id = r.id AND rr.user_name = ?
                 WHERE r.post_id = ?
                   AND rr.reply_id IS NULL
             """)) {
            ps.setString(1, userName);
            ps.setLong(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public static List<String> listReplies(long postId, String filter, String userName) throws Exception {
        String base  = "SELECT r.id, r.author, r.content, r.created_at FROM replies r";
        String where = " WHERE r.post_id = ? ";
        String order = " ORDER BY r.created_at ASC, r.id ASC ";
        boolean unreadOnly = "UNREAD".equalsIgnoreCase(filter);
        String sql = unreadOnly
                ? base + " LEFT JOIN reply_reads rr ON rr.reply_id = r.id AND rr.user_name = ? " + where + " AND rr.reply_id IS NULL " + order
                : base + where + order;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (unreadOnly) ps.setString(idx++, userName);
            ps.setLong(idx, postId);
            List<String> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String a = rs.getString("author");
                    String content = rs.getString("content");
                    out.add((a == null ? "Anonymous" : a) + ": " + (content == null ? "" : content));
                }
            }
            return out;
        }
    }

    /*======================== Getters + Cleanup ========================*/

    public String  getCurrentUsername()           { return currentUsername; }
    public String  getCurrentPassword()           { return currentPassword; }
    public String  getCurrentFirstName()          { return currentFirstName; }
    public String  getCurrentMiddleName()         { return currentMiddleName; }
    public String  getCurrentLastName()           { return currentLastName; }
    public String  getCurrentPreferredFirstName() { return currentPreferredFirstName; }
    public String  getCurrentEmailAddress()       { return currentEmailAddress; }
    public boolean getCurrentAdminRole()          { return currentAdminRole; }
    public boolean getCurrentStudentRole()        { return currentStudentRole; }
    public boolean getCurrentReviewerRole()       { return currentReviewerRole; }

    public void dump() throws SQLException {
        String query = "SELECT * FROM userDB";
        ResultSet rs = statement.executeQuery(query);
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            for (int i = 0; i < meta.getColumnCount(); i++) {
                System.out.println(meta.getColumnLabel(i + 1) + ": " + rs.getString(i + 1));
            }
            System.out.println();
        }
        rs.close();
    }

    public void closeConnection() {
        try { if (statement  != null) statement.close();  } catch (SQLException ignored) {}
        try { if (connection != null) connection.close(); } catch (SQLException ignored) {}
    }
   
}
