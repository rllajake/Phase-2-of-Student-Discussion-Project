package guiSearchPosts;

import database.Database;
import entityClasses.User;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import entityClasses.PostItem;
import entityClasses.PostCardCell;

/**
 * <p> ControllerSearchPosts Class </p>
 * 
 * <p> Description: This controller handles user interactions for the Search Posts page.
 * It processes search queries, filters by category, and displays matching posts in a 
 * list view. The controller communicates with the database to retrieve posts and manages
 * navigation to post details and back to the student home page. </p>
 * 
 * <p> Copyright: Arizona State University © 2025 </p>
 * 
 * @author Group 14
 * 
 * @version 1.00    2025-10-27 Initial implementation
 */
public class ControllerSearchPosts {

    private final Stage stage;
    private final User user;
    private final TextField searchField;
    private final ToggleGroup categoryGroup; // can be "All" => search all threads
    private final ListView<PostItem> resultsList;

    /**
     * <p> Constructor: ControllerSearchPosts() </p>
     * 
     * <p> Description: Initializes the search posts controller with references to the UI
     * components and user context. Sets up the custom cell factory for displaying search
     * results in the list view. </p>
     * 
     * @param stage         The JavaFX stage for displaying the search interface
     * @param user          The currently logged-in user
     * @param searchField   The text field for entering search queries
     * @param categoryGroup The toggle group for selecting post categories
     * @param resultsList   The list view for displaying search results
     */
    public ControllerSearchPosts(Stage stage, User user,
                                 TextField searchField,
                                 ToggleGroup categoryGroup,
                                 ListView<PostItem> resultsList) {
        this.stage = stage;
        this.user = user;
        this.searchField = searchField;
        this.categoryGroup = categoryGroup;
        this.resultsList = resultsList;
        this.resultsList.setCellFactory(lv -> new PostCardCell());
    }

    /**
     * <p> Method: onSearch() </p>
     * 
     * <p> Description: Executes a search query based on the current search text and selected
     * category filter. Queries the database for matching posts, calculates unread reply counts
     * for each post, and updates the results list view with the matching posts sorted by 
     * creation date. </p>
     * 
     */
    public void onSearch() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String cat = getToggleText(categoryGroup); // may be "All" => search all

        List<PostItem> out = new ArrayList<>();
        
        String sql = """
        	    SELECT p.id, p.author, p.thread AS thread,
        	           COALESCE(p.title,'') AS title, COALESCE(p.kind,'Post') AS kind,
        	           p.content, p.is_anonymous, p.is_private, p.created_at,
        	           (SELECT COUNT(*) FROM replies r WHERE r.post_id = p.id) AS reply_count
        	      FROM posts p
        	     WHERE COALESCE(p.deleted, FALSE) = FALSE
        	""";

        boolean hasCat = (cat != null && !"All".equalsIgnoreCase(cat));
        boolean hasQ = (q != null && !q.isBlank());
        if (hasQ) sql += " AND (LOWER(p.title) LIKE ? OR LOWER(p.content) LIKE ?) ";
        if (hasCat) sql += " AND LOWER(p.thread) = LOWER(?) ";
        sql += " ORDER BY p.created_at DESC, p.id DESC ";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (hasQ) {
                ps.setString(idx++, "%" + q + "%");
                ps.setString(idx++, "%" + q + "%");
            }
            if (hasCat) {
                ps.setString(idx++, cat);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PostItem it = new PostItem(
                            rs.getLong("id"),
                            rs.getString("author"),
                            rs.getString("thread"),
                            rs.getString("title"),
                            rs.getString("kind"),
                            rs.getString("content"),
                            rs.getBoolean("is_anonymous"),
                            rs.getBoolean("is_private"),
                            rs.getTimestamp("created_at"),
                            rs.getInt("reply_count")
                    );
                    out.add(it);
                }
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
        }

        // fill unread counts
        if (user != null && user.getUserName() != null) {
            for (int i = 0; i < out.size(); i++) {
                PostItem p = out.get(i);
                try {
                    int unread = Database.getUnreadReplyCount(user.getUserName(), p.id);
                    out.set(i, new PostItem(p.id, p.author, p.thread, p.title, p.kind,
                            p.content, p.isAnonymous, p.isPrivate, p.createdAt, p.replyCount, unread));
                } catch (Exception ignored) {}
            }
        }

        resultsList.getItems().setAll(out);
    }

    /**
     * <p> Method: onOpenSelected() </p>
     * 
     * <p> Description: Opens the detailed view for the currently selected post in the search
     * results list. If no post is selected, no action is taken. </p>
     * 
     */
    public void onOpenSelected() {
        PostItem sel = resultsList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            // open reader with the post preselected
            guiReadPosts.ViewReadPosts.displayReadPosts(stage, user, sel.id);
        } catch (Throwable t) {
            Platform.runLater(stage::close);
        } 
    }

    /**
     * <p> Method: onBack() </p>
     * 
     * <p> Description: Returns the user to the student home page. If navigation fails, 
     * closes the current stage. </p>
     * 
     */
    public void onBack() {
        try {
            guiStudent.ViewStudentHome.displayStudentHome(stage, user);
        } catch (Throwable t) {
            Platform.runLater(stage::close);
        }
    }

    /**
     * <p> Method: getToggleText() </p>
     * 
     * <p> Description: Helper method to extract the text label from the currently selected
     * toggle button in a toggle group. </p>
     * 
     * @param g The toggle group to query
     * @return  The text of the selected toggle button, or null if nothing is selected
     */
    private static String getToggleText(ToggleGroup g) {
        if (g == null || g.getSelectedToggle() == null) return null;
        return ((ToggleButton) g.getSelectedToggle()).getText();
    }
}
