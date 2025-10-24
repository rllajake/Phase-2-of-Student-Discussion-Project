package guiMyPosts;

import database.Database;
import entityClasses.User;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import entityClasses.PostItem;
import entityClasses.PostCardCell;

public class ControllerMyPosts {

    private final Stage stage;
    private final User user;

    private final ToggleGroup catGroup;   // category rail (left)
    private final ToggleGroup kindGroup;  // All / Questions / Posts
    private final ToggleGroup seenGroup = new ToggleGroup(); // NEW: Unseen / Seen

    private final ListView<PostItem> postList;
    private final Label titleLbl;
    private final Label metaLbl;
    private final TextArea bodyArea;

    // Replies are rendered into a VBox; hidden when empty
    private final VBox repliesBox;

    private final Node detailsBox;      // the details content node (wrapped in a ScrollPane at runtime)
    private final Node placeholderBox;  // “Select a post...” message

    private final Button replyBtn;
    private final Button editPostBtn;
    private final Button deletePostBtn;

    private final Long initialPostId;

    private final List<PostItem> allPosts = new ArrayList<>();

    public ControllerMyPosts(
            Stage stage,
            User user,
            ToggleGroup catGroup,
            ToggleGroup kindGroup,
            ListView<PostItem> postList,
            Label titleLbl,
            Label metaLbl,
            TextArea bodyArea,
            VBox repliesBox,
            Node detailsBox,
            Node placeholderBox,
            Button replyBtn,
            Button editPostBtn,
            Button deletePostBtn,
            Long initialPostId
    ) {
        this.stage = stage;
        this.user = user;
        this.catGroup = catGroup;
        this.kindGroup = kindGroup;
        this.postList = postList;
        this.titleLbl = titleLbl;
        this.metaLbl = metaLbl;
        this.bodyArea = bodyArea;
        this.repliesBox = repliesBox;
        this.detailsBox = detailsBox;
        this.placeholderBox = placeholderBox;
        this.replyBtn = replyBtn;
        this.editPostBtn = editPostBtn;
        this.deletePostBtn = deletePostBtn;
        this.initialPostId = initialPostId;

        // style hook for CSS
        if (detailsBox instanceof javafx.scene.layout.Region r) {
            r.getStyleClass().add("details-root");
        }

        // Wire post-level checkbox handlers into the ListCell template (unread tracking)
        PostCardCell.setReadProviders(
                id -> {
                    try { return Database.isPostRead(user.getUserName(), id); }
                    catch (Exception e) { return false; }
                },
                (id, checked) -> {
                    try { Database.setPostRead(user.getUserName(), id, checked); } catch (Exception ignored) {}
                    try {
                        PostItem it = postList.getItems().stream().filter(p -> p.id == id).findFirst().orElse(null);
                        if (it != null) {
                            it.unreadReplyCount = Database.getUnreadReplyCount(user.getUserName(), id);
                            postList.refresh();
                        }
                    } catch (Exception ignored) {}
                    // If we’re filtering by Seen/Unseen, reflect it immediately
                    applyFilters();
                }
        );

        this.replyBtn.setOnAction(e -> onReply());
        this.editPostBtn.setOnAction(e -> onEditPost());
        this.deletePostBtn.setOnAction(e -> onDeletePost());

        postList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> onPostSelected(n));

        // wrap long bodies instead of stretching horizontally
        bodyArea.setWrapText(true);

        // make the entire details pane scroll vertically
        Platform.runLater(() -> {
            if (detailsBox.getParent() == null) return;
            if (detailsBox.getParent() instanceof ScrollPane) return;

            ScrollPane detailsScroll = new ScrollPane();
            detailsScroll.setFitToWidth(true);
            detailsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            detailsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            detailsScroll.setPannable(true);

            var parent = detailsBox.getParent();
            if (parent instanceof javafx.scene.layout.BorderPane bp) {
                if (bp.getCenter() == detailsBox) { bp.setCenter(null); detailsScroll.setContent(detailsBox); bp.setCenter(detailsScroll); }
                else if (bp.getRight() == detailsBox) { bp.setRight(null); detailsScroll.setContent(detailsBox); bp.setRight(detailsScroll); }
                else if (bp.getLeft() == detailsBox) { bp.setLeft(null); detailsScroll.setContent(detailsBox); bp.setLeft(detailsScroll); }
                else if (bp.getTop() == detailsBox) { bp.setTop(null); detailsScroll.setContent(detailsBox); bp.setTop(detailsScroll); }
                else if (bp.getBottom() == detailsBox) { bp.setBottom(null); detailsScroll.setContent(detailsBox); bp.setBottom(detailsScroll); }
            } else if (parent instanceof javafx.scene.layout.Pane pane) {
                int idx = pane.getChildren().indexOf(detailsBox);
                if (idx >= 0) {
                    pane.getChildren().remove(idx);
                    detailsScroll.setContent(detailsBox);
                    pane.getChildren().add(idx, detailsScroll);
                }
            }
        });
        
     // kill the old header filter buttons but keep title/back
        removeLegacyHeaderFilterButtons();

        // build & mount the NEW segmented filter above the list (adds Unseen/Seen pair)
        mountFilterBarAbovePostList();
    }

    /* ====================== Data loading & filtering ====================== */

    public void loadMyPosts(User user) {
        allPosts.clear();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     """
                     SELECT p.id, p.author, p.thread, p.title, p.kind, p.content, p.is_anonymous, p.is_private,
                            p.created_at, p.deleted,
                            (SELECT COUNT(*) FROM replies r WHERE r.post_id = p.id) AS reply_count
                     FROM posts p WHERE p.author = ?
                     ORDER BY p.created_at DESC, p.id DESC
                     """)) {
        	ps.setString(1, user.getUserName());
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
                            rs.getInt("reply_count"),
                            Database.getUnreadReplyCount(user.getUserName(), rs.getLong("id"))
                    );
                    it.isDeleted = rs.getBoolean("deleted");
                    allPosts.add(it);
                }
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load posts: " + e.getMessage()).showAndWait();
        }

        applyFilters();

        // auto-select first or a specific id if provided
        if (!postList.getItems().isEmpty()) {
            if (initialPostId != null) {
                for (int i = 0; i < postList.getItems().size(); i++) {
                    if (postList.getItems().get(i).id == initialPostId) {
                        postList.getSelectionModel().select(i);
                        break;
                    }
                }
            } else {
                postList.getSelectionModel().select(0);
            }
        }
    }

    public void applyFilters() {
        final String selectedCat  = getToggleText(catGroup);
        final String selectedKind = getToggleText(kindGroup);
        final String normKind     = normalizeKind(selectedKind);
        final String seenChoice   = getToggleText(seenGroup); // "Unseen" / "Seen" / null (both)

        List<PostItem> filtered = new ArrayList<>(allPosts);

        if (selectedCat != null && !"All".equalsIgnoreCase(selectedCat)) {
            filtered = filtered.stream()
                    .filter(p -> selectedCat.equalsIgnoreCase(p.thread))
                    .collect(Collectors.toList());
        }
        if (normKind != null && !"All".equalsIgnoreCase(normKind)) {
            final String fKind = normKind;
            filtered = filtered.stream()
                    .filter(p -> fKind.equalsIgnoreCase(p.kind))
                    .collect(Collectors.toList());
        }
        if (seenChoice != null) {
            final boolean wantSeen = "Seen".equalsIgnoreCase(seenChoice);
            filtered = filtered.stream().filter(p -> {
                try {
                    boolean isSeen = Database.isPostRead(user.getUserName(), p.id);
                    return wantSeen == isSeen;
                } catch (Exception e) {
                    return false; // on error, hide
                }
            }).collect(Collectors.toList());
        }

        postList.getItems().setAll(filtered);
        postList.getSelectionModel().clearSelection();
        showPlaceholder(true);
    }

    /* ====================== Selection & rendering ====================== */

    public void onPostSelected(PostItem cur) {
        if (cur == null) {
            showPlaceholder(true);
            return;
        }
        showPlaceholder(false);

        titleLbl.setText(cur.safeTitle());
        metaLbl.setText(String.format("%s • %s • %s", cur.thread, cur.kind, humanize(cur.createdAt)));

        boolean deleted = cur.isDeleted || "[This post has been deleted]".equals(cur.content);

        if (deleted) {
            bodyArea.setText("[This post has been deleted]");
            titleLbl.setStyle("-fx-opacity: 0.6;");
            metaLbl.setStyle("-fx-opacity: 0.6;");
        } else {
            bodyArea.setText(cur.content == null ? "" : cur.content);
            titleLbl.setStyle("-fx-opacity: 1.0;");
            metaLbl.setStyle("-fx-opacity: 1.0;");
        }

        // actions: only for owner and only when not deleted
        boolean isMine = (user != null && user.getUserName() != null
                && cur.author != null && user.getUserName().equalsIgnoreCase(cur.author));
        editPostBtn.setDisable(deleted || !isMine);
        deletePostBtn.setVisible(isMine && !deleted);
        deletePostBtn.setManaged(isMine && !deleted);

        renderReplies(cur);
    }

    private void renderReplies(PostItem cur) {
        repliesBox.setFillWidth(true);
        repliesBox.getChildren().clear();

        try {
            List<Map<String,Object>> rows = Database.findReplies(cur.id);
            if (rows.isEmpty()) {
                repliesBox.setManaged(false);
                repliesBox.setVisible(false);
                return;
            }
            repliesBox.setManaged(true);
            repliesBox.setVisible(true);

            for (Map<String, Object> r : rows) {
                long rid = ((Number) r.getOrDefault("id", 0L)).longValue();
                String a = (String) r.getOrDefault("author", null);
                String content = (String) r.getOrDefault("content", "");

                HBox row = new HBox(8);
                row.setFillHeight(true);
                row.setMaxWidth(Double.MAX_VALUE);

                CheckBox cb = new CheckBox();
                boolean isRead = false;
                try { isRead = Database.isReplyRead(user.getUserName(), rid); } catch (Exception ignored) {}
                cb.setSelected(isRead);
                cb.setOnAction(e -> {
                    try { Database.setReplyRead(user.getUserName(), rid, cb.isSelected()); } catch (Exception ignored2) {}
                    try {
                        cur.unreadReplyCount = Database.getUnreadReplyCount(user.getUserName(), cur.id);
                        postList.refresh();
                    } catch (Exception ignored3) {}
                });

                Label la = new Label(a == null || a.isBlank() ? "Anonymous" : a);
                Label sep = new Label("•");

                Label lc = makeWrappingLabel(content);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                boolean mine = (user != null && user.getUserName() != null
                        && a != null && user.getUserName().equalsIgnoreCase(a));

                if (mine) {
                    Button edit = new Button("Edit");
                    edit.setOnAction(ev -> {
                        TextInputDialog d = new TextInputDialog(content == null ? "" : content);
                        d.setTitle("Edit Reply");
                        d.setHeaderText(null);
                        d.setContentText("Update your reply:");
                        Optional<String> rr = d.showAndWait();
                        if (rr.isPresent()) {
                            String nv = rr.get().trim();

                            // BASIC VALIDATION (alphanumeric + spaces only, and >= 3 letters/digits)
                            String tAlnum = nv.replaceAll("[^A-Za-z0-9]", "");
                            if (nv.isBlank()
                                    || nv.matches(".*[^A-Za-z0-9\\s].*")
                                    || tAlnum.length() < 3) {
                                new Alert(Alert.AlertType.WARNING,
                                        "Reply must be alphanumeric (letters/digits and spaces only) and at least 3 letters/digits long.")
                                        .showAndWait();
                                return;
                            }

                            try {
                                Database.updateReplyByAuthor(rid, user.getUserName(), nv);
                                renderReplies(cur);
                            } catch (Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Failed to update reply: " + ex.getMessage()).showAndWait();
                            }
                        }
                    });

                    Button del = new Button("Delete");
                    del.setOnAction(ev -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this reply?", ButtonType.OK, ButtonType.CANCEL);
                        Optional<ButtonType> ares = confirm.showAndWait();
                        if (ares.isPresent() && ares.get() == ButtonType.OK) {
                            try {
                                Database.deleteReplyByAuthor(rid, user.getUserName());
                                cur.replyCount = Math.max(0, cur.replyCount - 1);
                                renderReplies(cur);
                                postList.refresh();
                            } catch (Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Failed to delete reply: " + ex.getMessage()).showAndWait();
                            }
                        }
                    });

                    row.getChildren().addAll(cb, la, sep, lc, spacer, edit, del);
                } else {
                    row.getChildren().addAll(cb, la, sep, lc, spacer);
                }

                repliesBox.getChildren().add(row);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load replies: " + e.getMessage()).showAndWait();
        }
    }


    /* ====================== Actions ====================== */

    private void onReply() {
        PostItem cur = postList.getSelectionModel().getSelectedItem();
        if (cur == null) return;

        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Reply");
        dlg.setHeaderText("Add a reply");
        dlg.setContentText("Reply:");
        Optional<String> res = dlg.showAndWait();
        if (res.isEmpty()) return;

        String text = res.get().trim();

     // BASIC VALIDATION (allow punctuation; require not-blank and >= 3 letters/digits)
        int alnum = text.replaceAll("[^A-Za-z0-9]", "").length();
        if (text.isBlank() || alnum < 3) {
            new Alert(Alert.AlertType.WARNING,
                    "Reply must not be empty and must contain at least 3 letters/digits (spaces don’t count).")
                    .showAndWait();
            return;
        }

        try {
            Database.addReply(user == null ? "Anonymous" : user.getUserName(), cur.id, text);
            renderReplies(cur);
            cur.replyCount += 1;
            onPostSelected(cur);
            postList.refresh();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to add reply: " + ex.getMessage()).showAndWait();
        }
    }


    private void onEditPost() {
        PostItem cur = postList.getSelectionModel().getSelectedItem();
        if (cur == null) return;

        boolean deleted = cur.isDeleted || "[This post has been deleted]".equals(cur.content);
        boolean isMine = (user != null && user.getUserName() != null
                && cur.author != null && user.getUserName().equalsIgnoreCase(cur.author));
        if (deleted || !isMine) {
            new Alert(Alert.AlertType.INFORMATION, "You can only edit your own, non-deleted posts.").showAndWait();
            return;
        }

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Edit Post");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(cur.title == null ? "" : cur.title);
        titleField.setPromptText("Title (optional)");
        TextArea contentArea = new TextArea(cur.content == null ? "" : cur.content);
        contentArea.setPrefRowCount(10);

        VBox box = new VBox(8, new Label("Title:"), titleField, new Label("Content:"), contentArea);
        dlg.getDialogPane().setContent(box);

        Optional<ButtonType> res = dlg.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        String newTitle = titleField.getText() == null ? "" : titleField.getText().trim();
        String newBody  = contentArea.getText() == null ? "" : contentArea.getText().trim();


     // BASIC VALIDATION (allow punctuation; require not-blank and >= 3 letters/digits)
        int tAl = newTitle.replaceAll("[^A-Za-z0-9]", "").length();
        int bAl = newBody.replaceAll("[^A-Za-z0-9]", "").length();
        if (newTitle.isBlank() || newBody.isBlank() || tAl < 3 || bAl < 3) {
            new Alert(Alert.AlertType.WARNING,
                    "Title and body must not be empty and must contain at least 3 letters/digits (spaces don’t count).")
                    .showAndWait();
            return;
        }

        try {
            Database.updatePostByAuthor(cur.id, user.getUserName(), newTitle, newBody);
            cur.title = newTitle;
            cur.content = newBody;
            onPostSelected(cur);
            postList.refresh();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Edit failed: " + e.getMessage()).showAndWait();
        }
    }


    private void onDeletePost() {
        PostItem cur = postList.getSelectionModel().getSelectedItem();
        if (cur == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this post? (Replies will remain, but viewers will see that the original post was deleted.)",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm Delete");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        try {
            Database.softDeletePostByAuthor(cur.id, user.getUserName());
            cur.isDeleted = true;
            onPostSelected(cur);   // hides Delete button
            postList.refresh();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage()).showAndWait();
        }
    }

    /* ====================== Helpers ====================== */

    private void showPlaceholder(boolean show) {
        detailsBox.setVisible(!show);
        detailsBox.setManaged(!show);
        placeholderBox.setVisible(show);
        placeholderBox.setManaged(show);
    }

    private static String humanize(java.sql.Timestamp ts) {
        if (ts == null) return "";
        Duration d = Duration.between(ts.toInstant(), Instant.now());
        long m = Math.max(0, d.toMinutes());
        if (m < 1) return "just now";
        if (m < 60) return m + "m ago";
        long h = m / 60;
        if (h < 24) return h + "h ago";
        long days = h / 24;
        return days + "d ago";
    }

    private static String getToggleText(ToggleGroup g) {
        if (g == null || g.getSelectedToggle() == null) return null;
        if (g.getSelectedToggle() instanceof ToggleButton tb) return tb.getText();
        if (g.getSelectedToggle() instanceof Labeled l) return l.getText();
        return null;
    }

    private static String normalizeKind(String k) {
        if (k == null) return null;
        if ("Posts".equalsIgnoreCase(k)) return "Post";
        if ("Questions".equalsIgnoreCase(k)) return "Question";
        return k;
    }

    public void onBack() {
        try {
            guiStudent.ViewStudentHome.displayStudentHome(stage, user);
        } catch (Throwable t) {
            Platform.runLater(stage::close);
        }
    }

    private Label makeWrappingLabel(String text) {
        Label l = new Label(text == null ? "" : text);
        l.setWrapText(true);
        l.setMinWidth(0);
        l.setPrefWidth(0);
        l.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(l, Priority.ALWAYS);
        l.maxWidthProperty().bind(repliesBox.widthProperty().subtract(260));
        return l;
    }

    // Build "Filter" + segmented All/Questions/Posts + segmented Unseen/Seen
    private HBox buildFilterBar() {
        // Detach any existing toggles to prevent ghost toggles if re-mounted
        for (Toggle t : new ArrayList<>(kindGroup.getToggles())) t.setToggleGroup(null);
        for (Toggle t : new ArrayList<>(seenGroup.getToggles())) t.setToggleGroup(null);

        Label title = new Label("Filter");
        title.getStyleClass().add("section-title");

        // Kind segment
        ToggleButton all = new ToggleButton("All");
        ToggleButton q   = new ToggleButton("Questions");
        ToggleButton p   = new ToggleButton("Posts");
        all.getStyleClass().addAll("seg-toggle", "seg-left");
        q.getStyleClass().addAll("seg-toggle", "seg-mid");
        p.getStyleClass().addAll("seg-toggle", "seg-right");
        all.setUserData("All");
        q.setUserData("Question");
        p.setUserData("Post");
        all.setToggleGroup(kindGroup);
        q.setToggleGroup(kindGroup);
        p.setToggleGroup(kindGroup);
        if (kindGroup.getSelectedToggle() == null) all.setSelected(true);

        kindGroup.selectedToggleProperty().addListener((obs, o, n) -> applyFilters());

        // NEW: Seen segment (two buttons, no “All”; leaving both unselected == show both)
        ToggleButton unseen = new ToggleButton("Unseen");
        ToggleButton seen   = new ToggleButton("Seen");
        unseen.getStyleClass().addAll("seg-toggle", "seg-left");
        seen.getStyleClass().addAll("seg-toggle", "seg-right");
        unseen.setUserData("Unseen");
        seen.setUserData("Seen");
        unseen.setToggleGroup(seenGroup);
        seen.setToggleGroup(seenGroup);

        // apply on change
        seenGroup.selectedToggleProperty().addListener((obs, o, n) -> applyFilters());

        HBox kindBox = new HBox(6, all, q, p);
        HBox seenBox = new HBox(6, unseen, seen);

        HBox bar = new HBox(16, title, kindBox, seenBox);
        bar.getStyleClass().add("kindbar");
        return bar;
    }

    // Mount the filter bar directly ABOVE the postList in the left column
    private void mountFilterBarAbovePostList() {
        Platform.runLater(() -> {
            var parent = postList.getParent();
            if (!(parent instanceof VBox vb)) return;

            boolean already = vb.getChildren().stream()
                    .anyMatch(n -> n.getStyleClass().contains("kindbar"));
            if (already) return;

            vb.getChildren().add(0, buildFilterBar());
        });
    }
    
 // Remove legacy header toggle buttons "All", "Questions", "Posts" (NOT our new Filter bar)
    private void removeLegacyHeaderFilterButtons() {
        Platform.runLater(() -> {
            if (stage == null || stage.getScene() == null) return;
            var root = stage.getScene().getRoot();
            if (!(root instanceof javafx.scene.Parent p)) return;

            List<ToggleButton> targets = new ArrayList<>();
            collectLegacyHeaderToggles(p, targets);

            // detach from group to avoid ghost selection, then remove from their parents
            for (ToggleButton tb : targets) {
                if (kindGroup.getToggles().contains(tb)) tb.setToggleGroup(null);
                if (tb.getParent() instanceof javafx.scene.layout.Pane pane) {
                    pane.getChildren().remove(tb);
                }
            }
        });
    }

    // DFS: find ToggleButtons labeled All/Questions/Posts that are NOT inside our new "kindbar"
    private void collectLegacyHeaderToggles(javafx.scene.Parent node, List<ToggleButton> out) {
        for (javafx.scene.Node ch : node.getChildrenUnmodifiable()) {
            if (ch instanceof ToggleButton tb) {
                String t = tb.getText();
                boolean looksLikeLegacy =
                        t != null && (t.equalsIgnoreCase("All")
                                   || t.equalsIgnoreCase("Questions")
                                   || t.equalsIgnoreCase("Posts"));
                boolean insideOurNewBar =
                        tb.getParent() != null && tb.getParent().getStyleClass().contains("kindbar");

                if (looksLikeLegacy && !insideOurNewBar) {
                    out.add(tb);
                }
            }
            if (ch instanceof javafx.scene.Parent p) collectLegacyHeaderToggles(p, out);
        }
    }

}
