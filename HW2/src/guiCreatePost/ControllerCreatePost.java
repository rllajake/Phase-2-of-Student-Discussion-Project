package guiCreatePost;

import database.Database;
import entityClasses.User;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewCreatePost Class. </p>
 * 
 * <p> Description: The Java/FX-based Create Post Page.  The page is a stub for a particular post needed for
 * the user to create and place into the table for viewing. The widgets on this page are validated and managed in 
 * order for proper post criteria conditions to be met.</p>
 * 
 *  
 */

public class ControllerCreatePost {

    private final Stage stage;
    private final User user;
    private final TextField titleField;
    private final TextArea bodyArea;
    private final ToggleGroup kindGroup;
    private final ToggleGroup categoryGroup;
    private final CheckBox cbPrivate;
    private final CheckBox cbAnonymous;

    
    /*********
     * <p> Title: ControllerCreatePost Constructor </p>
     * 
     * <p>Description: Singleton instance for a new post to be created, being passed 
     * adequate attributes necessary for identifying the created post. </p>
     * 
     * @param stage signifies the given stage displayed on the screen
     * @param user signifies the current logged in user creating the post
     * @param titleField signifies the validated entry for the title
     * @param bodyArea signifies the validated entry for the contents
     * @param kindGroup signifies the type of group the post belongs to
     * @param categoryGroup signifies the type of category the post belongs to
     * @param cbPrivate signifies the checkbox for setting the visibility of the post to private
     * @param cbAnonymous signifies the checkbox for setting the visibility of the user's name to 'Anonymous'
     */
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

    /*********
     * <p> Title: onPost() </p>
     * 
     * <p>Description: Method necessary for validating the post's title and content. 
     * When validated, makes sure to call the database method to place the post entry into the corresponding table
     * for direct reference. Method includes necessary positive and negative feedback for when the post is or is not successfully
     * created. </p>
     * 
     */
    
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

    /*********
     * <p> Title: onBack() </p>
     * 
     * <p>Description: Method signifying the click of the 'Back' button, 
     * necessary for bringing the user back to the Student Home Page. </p>
     * 
     */
    public void onBack() {
        try {
            guiStudent.ViewStudentHome.displayStudentHome(stage, user);
        } catch (Throwable t) {
            Platform.runLater(stage::close);
        }
    }

    /*********
     * <p> Title: safe(String s) </p>
     * 
     * <p>Description: Helper method to validate whether the entries
     * for the title and contents are null or not. </p>
     * 
     * @param s signifies the untrimmed string entry before it is finalized
     * 
     * @return String signifies the trimmed string entry, if not null
     */
    private static String safe(String s) { return s == null ? "" : s.trim(); }

    /*********
     * <p> Title: selectedText(ToggleGroup group, String defaultVal) </p>
     * 
     * <p>Description: Helper method to validate whether the kind or category
     * of post was toggled via ToggleGroup. If not, there is a default value  </p>
     * 
     * @param group signifies the ToggleGroup selected by the user
     * 
     * @param defaultVal signifies the default value if the ToggleGroup selection is null
     * 
     * @return String signifies the trimmed text inside of a ToggleGroup or the default value
     */
    private static String selectedText(ToggleGroup group, String defaultVal) {
        if (group == null || group.getSelectedToggle() == null) return defaultVal;
        Toggle t = group.getSelectedToggle();
        if (t instanceof ToggleButton tb && tb.getText() != null && !tb.getText().isBlank()) {
            return tb.getText().trim();
        }
        return defaultVal;
    }
}
