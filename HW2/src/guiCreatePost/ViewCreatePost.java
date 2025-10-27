package guiCreatePost;

import entityClasses.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewCreatePost Class. </p>
 * 
 * <p> Description: The Java/FX-based page for creating a validated post.</p>
 *
 *  
 */

public class ViewCreatePost {

	/*********
	 * <p> Title: displayCreatePost(Stage stage, User user) </p>
	 * 
	 * <p> Description: Method responsible for displaying the Create Post Page to the student
	 * user whenever they click the 'Create Post' button on the Student Home Page. All necessary components 
	 * for this screen are displayed so the user can input them however they would like, while being validated. </p>
	 * 
	 * @param stage signifies the set stage to be displayed for the student
	 * @param user signifies the current logged in student user
	 */
    public static void displayCreatePost(Stage stage, User user) {
        Label titleHdr = new Label("Create Post");
        titleHdr.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Question vs Post (segmented)
        ToggleButton rbQuestion = new ToggleButton("Question");
        ToggleButton rbPost     = new ToggleButton("Post");
        ToggleGroup kindGroup = new ToggleGroup();
        rbQuestion.setToggleGroup(kindGroup);
        rbPost.setToggleGroup(kindGroup);
        rbQuestion.setSelected(true); // DEFAULT to avoid null
        HBox kindBar = new HBox(8, rbQuestion, rbPost);

        // Title
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        // Category chips
        ToggleButton tbGeneral     = new ToggleButton("General");
        ToggleButton tbLectures    = new ToggleButton("Lectures");
        ToggleButton tbTeamNorms   = new ToggleButton("Team Norms");
        ToggleButton tbAssignments = new ToggleButton("Assignments");
        ToggleButton tbSocial      = new ToggleButton("Social");
        ToggleGroup catGroup = new ToggleGroup();
        for (ToggleButton t : new ToggleButton[]{tbGeneral, tbLectures, tbTeamNorms, tbAssignments, tbSocial}) {
            t.setToggleGroup(catGroup);
        }
        tbGeneral.setSelected(true); // DEFAULT to avoid null
        FlowPane catPane = new FlowPane(8, 8, tbGeneral, tbLectures, tbTeamNorms, tbAssignments, tbSocial);
        VBox catBox = new VBox(6, new Label("Category"), catPane);

        // Body
        TextArea body = new TextArea();
        body.setPromptText("Write your statement or question...");
        body.setWrapText(true);
        body.setPrefRowCount(12);

        // Privacy options
        CheckBox cbPrivate   = new CheckBox("Private");
        Label    privateHelp = new Label("Visible to you and staff only");
        privateHelp.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        CheckBox cbAnonymous   = new CheckBox("Anonymous");
        Label    anonHelp      = new Label("Hide your name from students");
        anonHelp.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        GridPane privacyRow = new GridPane();
        privacyRow.setHgap(20);
        privacyRow.add(new VBox(2, cbPrivate, privateHelp), 0, 0);
        privacyRow.add(new VBox(2, cbAnonymous, anonHelp), 1, 0);

        // Actions
        Button postBtn = new Button("Post");
        Button backBtn = new Button("Back");
        HBox actions = new HBox(10, postBtn, backBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox root = new VBox(12,
                titleHdr,
                new Label("Type"), kindBar,
                new Label("Title"), titleField,
                catBox,
                new Label("Body"), body,
                privacyRow,
                actions
        );
        root.setPadding(new Insets(16));
        root.setPrefWidth(800);

        // --- Controller wiring ---
        ControllerCreatePost controller = new ControllerCreatePost(
                stage, user, titleField, body, kindGroup, catGroup, cbPrivate, cbAnonymous
        );

        // disable Post until mandatory fields provided
        postBtn.disableProperty().bind(
                titleField.textProperty().isEmpty()
                        .or(body.textProperty().isEmpty())
        );

        postBtn.setOnAction(e -> controller.onPost());
        backBtn.setOnAction(e -> controller.onBack());

        stage.setScene(new Scene(root));
        stage.setTitle("Create Post");
        stage.show();
    }
    
    /******
     * <p> Title: ViewCreatePost() Constructor </p>
     * 
     * <p> Description: Constructor for the ViewCreatePost class, not used here in this variation. </p>
     */
    public ViewCreatePost() {
    	
    }
}
