package guiSearchPosts;

import entityClasses.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Objects;

import entityClasses.PostItem;

public class ViewSearchPosts {

    public static void displaySearchPosts(Stage stage, User user) {
        Label hdr = new Label("Search Posts");
        hdr.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField q = new TextField();
        q.setPromptText("Search keyword…");

        // Categories rail mimic
        ToggleButton allBtn = new ToggleButton("All");
        ToggleButton generalBtn = new ToggleButton("General");
        ToggleButton lecturesBtn = new ToggleButton("Lectures");
        ToggleButton teamNormsBtn = new ToggleButton("Team Norms");
        ToggleButton assignmentsBtn = new ToggleButton("Assignments");
        ToggleButton socialBtn = new ToggleButton("Social");
        ToggleGroup catGroup = new ToggleGroup();
        for (ToggleButton b : new ToggleButton[]{allBtn,generalBtn,lecturesBtn,teamNormsBtn,assignmentsBtn,socialBtn}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setToggleGroup(catGroup);
        }
        allBtn.setSelected(true);
        VBox catRail = new VBox(6, new Label("CATEGORIES"), allBtn, generalBtn, lecturesBtn, teamNormsBtn, assignmentsBtn, socialBtn);
        catRail.setPrefWidth(220);
        catRail.setPadding(new Insets(12));

        ListView<PostItem> results = new ListView<>();

        Button searchBtn = new Button("Search");
        HBox searchBar = new HBox(8, q, searchBtn);
        HBox.setHgrow(q, Priority.ALWAYS);

        Button openBtn = new Button("Open");
        Button backBtn = new Button("Back");
        HBox actions = new HBox(10, new Region(), openBtn, backBtn);
        HBox.setHgrow(actions.getChildren().get(0), Priority.ALWAYS);

        VBox center = new VBox(10, hdr, searchBar, results, actions);
        center.setPadding(new Insets(12));
        VBox.setVgrow(results, Priority.ALWAYS);

        BorderPane root = new BorderPane(center, null, null, null, catRail);
        
        root.getStylesheets().add(
        	    Objects.requireNonNull(applicationMain.HW2Main.class.getResource("application.css"),
        	        "application.css not found in applicationMain").toExternalForm()
        	);
        	root.getStylesheets().add(
        	    Objects.requireNonNull(applicationMain.HW2Main.class.getResource("student.css"),
        	        "student.css not found in applicationMain").toExternalForm()
        	);

        ControllerSearchPosts controller = new ControllerSearchPosts(stage, user, q, catGroup, results);
        searchBtn.setOnAction(e -> controller.onSearch());
        openBtn.setOnAction(e -> controller.onOpenSelected());
        backBtn.setOnAction(e -> controller.onBack());
        q.setOnAction(e -> controller.onSearch());

        stage.setScene(new Scene(root, 1100, 700));
        stage.setTitle("Search Posts");
        stage.show();
        
     // Live-refresh when category changes (non-invasive)
        catGroup.selectedToggleProperty().addListener((obs, o, n) -> controller.onSearch());
        
    }
}
