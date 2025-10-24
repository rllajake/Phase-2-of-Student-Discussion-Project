package guiMyPosts;

import entityClasses.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Objects;

import entityClasses.PostItem;
import entityClasses.PostCardCell;

public class ViewMyPosts {

    // keep old entry point
    public static void displayMyPosts(Stage stage, User user) {
        displayMyPosts(stage, user, null);   // no preselect
    }

    // allow optional initial selection by post id
    public static void displayMyPosts(Stage stage, User user, Long initialPostId) {
        // === Left: Categories rail ===
        Label catHdr = new Label("CATEGORIES");
        catHdr.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
        ToggleButton allBtn = new ToggleButton("All");
        ToggleButton generalBtn = new ToggleButton("General");
        ToggleButton lecturesBtn = new ToggleButton("Lectures");
        ToggleButton teamNormsBtn = new ToggleButton("Team Norms");
        ToggleButton assignmentsBtn = new ToggleButton("Assignments");
        ToggleButton socialBtn = new ToggleButton("Social");
        ToggleGroup catGroup = new ToggleGroup();
        for (ToggleButton b : new ToggleButton[]{allBtn, generalBtn, lecturesBtn, teamNormsBtn, assignmentsBtn, socialBtn}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setToggleGroup(catGroup);
        }
        allBtn.setSelected(true);
        VBox catRail = new VBox(6, catHdr, allBtn, generalBtn, lecturesBtn, teamNormsBtn, assignmentsBtn, socialBtn);
        catRail.setPrefWidth(220);
        catRail.setPadding(new Insets(12));

        // === Center: Posts list ===
        Label listHdr = new Label("Posts");
        listHdr.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        ListView<PostItem> postList = new ListView<>();
        postList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        postList.setFocusTraversable(false);
        postList.setCellFactory(lv -> new PostCardCell());
        VBox centerCol = new VBox(10, listHdr, postList);
        centerCol.setPadding(new Insets(12));
        VBox.setVgrow(postList, Priority.ALWAYS);

        // === Right: Details + Placeholder ===
        Label detailsHdr = new Label("Details");
        detailsHdr.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label titleLbl = new Label("");
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label metaLbl  = new Label("");
        metaLbl.setStyle("-fx-text-fill: #666;");
        TextArea bodyArea = new TextArea();
        bodyArea.setWrapText(true);
        bodyArea.setEditable(false);
        bodyArea.setPrefRowCount(12);
        bodyArea.setStyle("-fx-control-inner-background: #fff;");

        // Actions row: Reply (always), Edit/Delete (only when owning the post; controller will toggle)
        Button replyBtn  = new Button("Reply");
        Button editPostBtn   = new Button("Edit");
        Button deletePostBtn = new Button("Delete");
        HBox actionsRow = new HBox(8, replyBtn, editPostBtn, deletePostBtn);

        // Replies container (NO header; hidden when empty)
        VBox repliesBox = new VBox(8);
        repliesBox.setPadding(new Insets(6, 0, 0, 0));

        VBox detailsBox = new VBox(8, detailsHdr, titleLbl, metaLbl, bodyArea, actionsRow, repliesBox);
        detailsBox.setPadding(new Insets(12));
        detailsBox.setVisible(false);
        detailsBox.setManaged(false);

        // Placeholder when nothing selected
        Label pickLbl = new Label("Select a post to see details");
        VBox placeholder = new VBox(pickLbl);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(12));

        StackPane rightStack = new StackPane(placeholder, detailsBox);

        // === Top bar with filter & back ===
        Label pageTitle = new Label("");
        pageTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        ToggleButton fAll = new ToggleButton("All");
        ToggleButton fQ   = new ToggleButton("Questions");
        ToggleButton fP   = new ToggleButton("Posts");
        ToggleGroup kindGroup = new ToggleGroup();
        for (ToggleButton b : new ToggleButton[]{fAll, fQ, fP}) {
            b.setToggleGroup(kindGroup);
        }
        fAll.setSelected(true);

        HBox filterBar = new HBox(6, pageTitle, new HBox(), fAll, fQ, fP);
        HBox.setHgrow(filterBar.getChildren().get(1), Priority.ALWAYS);
        Button backBtn = new Button("Back");

        HBox topBar = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(spacer, filterBar, backBtn);
        topBar.setPadding(new Insets(12, 12, 0, 12));

        // === Main layout ===
        BorderPane root = new BorderPane();
        
        root.getStylesheets().add(
        	    Objects.requireNonNull(applicationMain.HW2Main.class.getResource("application.css"),
        	        "application.css not found in applicationMain").toExternalForm()
        	);
        	root.getStylesheets().add(
        	    Objects.requireNonNull(applicationMain.HW2Main.class.getResource("student.css"),
        	        "student.css not found in applicationMain").toExternalForm()
        	);
        
        root.setTop(topBar);
        root.setLeft(catRail);
        root.setCenter(centerCol);
        root.setRight(rightStack);

        // === Controller wiring ===
        ControllerMyPosts controller = new ControllerMyPosts(
                stage, user, catGroup, kindGroup,
                postList, titleLbl, metaLbl, bodyArea,
                repliesBox, detailsBox, placeholder,   // pass the actual details & placeholder VBOXes
                replyBtn, editPostBtn, deletePostBtn, initialPostId
        );

        // category & kind filters
        catGroup.selectedToggleProperty().addListener((obs, o, n) -> controller.applyFilters());
        kindGroup.selectedToggleProperty().addListener((obs, o, n) -> controller.applyFilters());

        // list selection
        postList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> controller.onPostSelected(n));

        // guard: ignore clicks on empty cells
        postList.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            javafx.scene.Node node = e.getPickResult().getIntersectedNode();
            while (node != null && node != postList && !(node instanceof javafx.scene.control.ListCell)) {
                node = node.getParent();
            }
            if (node instanceof javafx.scene.control.ListCell<?> cell && cell.isEmpty()) {
                postList.getSelectionModel().clearSelection();
                e.consume();
            }
        });

        backBtn.setOnAction(e -> controller.onBack());

        controller.loadMyPosts(user);

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Read Posts");
        stage.show();
    }

}
