package guiCreatePost;

import database.Database;
import entityClasses.User;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ControllerCreatePost {

    private final Stage stage;
    private final User user;
    private final TextField titleField;
    private final TextArea bodyArea;
    private final ToggleGroup kindGroup;
    private final ToggleGroup categoryGroup;
    private final CheckBox cbPrivate;
    private final CheckBox cbAnonymous;

    public ControllerCreatePost(Stage stage,
                                User user,
                                TextField titleField,
                                TextArea bodyArea,
                                ToggleGroup kindGroup, ToggleGroup categoryGroup,
                                CheckBox cbPrivate, CheckBox cbAnonymous) {
        this.stage = stage;
        this.user = user;
        this.titleField = titleField;
        this.bodyArea = bodyArea;
        this.kindGroup = kindGroup;
        this.categoryGroup = categoryGroup;
        this.cbPrivate = cbPrivate;
        this.cbAnonymous = cbAnonymous;
    }

    public void onPost() {
        String title = safe(titleField.getText());
        String body = safe(bodyArea.getText());
        String kind = selectedText(kindGroup, "Post");
        String category = selectedText(categoryGroup, "General");
        boolean isPrivate = cbPrivate.isSelected();
        boolean isAnonymous = cbAnonymous.isSelected();

        // BASIC VALIDATION (allow punctuation; require not-blank and >= 3 letters/digits)
        String t = title;
        String b = body;
        int tAlnum = t.replaceAll("[^A-Za-z0-9]", "").length();
        int bAlnum = b.replaceAll("[^A-Za-z0-9]", "").length();
        if (t.isBlank() || b.isBlank() || tAlnum < 3 || bAlnum < 3) {
            new Alert(Alert.AlertType.WARNING,
                    "Title and body must not be empty and must contain at least 3 letters/digits (spaces don’t count).")
                    .showAndWait();
            return;
        }

        try {
            Database.createPostStudent(user.getUserName(), category, title, kind, body, isPrivate, isAnonymous);
            new Alert(Alert.AlertType.INFORMATION, "Post created.").showAndWait();
            onBack();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to create post: " + e.getMessage()).showAndWait();
        }
    }

    public void onBack() {
        try {
            guiStudent.ViewStudentHome.displayStudentHome(stage, user);
        } catch (Throwable t) {
            Platform.runLater(stage::close);
        }
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static String selectedText(ToggleGroup group, String defaultVal) {
        if (group == null || group.getSelectedToggle() == null) return defaultVal;
        Toggle t = group.getSelectedToggle();
        if (t instanceof ToggleButton tb && tb.getText() != null && !tb.getText().isBlank()) {
            return tb.getText().trim();
        }
        return defaultVal;
    }
}
