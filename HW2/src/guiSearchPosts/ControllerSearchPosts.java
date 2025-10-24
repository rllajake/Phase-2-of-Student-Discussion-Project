package guiSearchPosts;

import database.Database;
import entityClasses.User;
import guiReadPosts.PostItem;
import guiReadPosts.PostCardCell;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import entityClasses.PostItem;
import entityClasses.PostCardCell;

public class ControllerSearchPosts {

    private final Stage stage;
    private final User user;
    private final TextField searchField;
    private final ToggleGroup categoryGroup; // can be "All" => search all threads
    private final ListView<PostItem> resultsList;

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
        if (hasCat) sql += " AND LOWER(t.name) = LOWER(?) ";
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

    public void onOpenSelected() {
        PostItem sel = resultsList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        guiReadPosts.ViewReadPosts.displayReadPosts(stage, user, sel.id);
    }

    public void onBack() {
        try {
            guiStudent.ViewStudentHome.displayStudentHome(stage, user);
        } catch (Throwable t) {
            Platform.runLater(stage::close);
        }
    }

    private static String getToggleText(ToggleGroup g) {
        if (g == null || g.getSelectedToggle() == null) return null;
        return ((ToggleButton) g.getSelectedToggle()).getText();
    }
}
