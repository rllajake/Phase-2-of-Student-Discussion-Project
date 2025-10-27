package guiSearchPosts;

import entityClasses.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Objects;

import entityClasses.PostItem;

/**
 * <p> ViewSearchPosts Class </p>
 * 
 * <p> Description: This class defines the user interface for the Search Posts page.
 * It creates and arranges all UI components including the search bar, category filters,
 * results list, and action buttons. The view establishes the visual layout and connects
 * UI components to the controller for event handling. The interface provides live search
 * functionality that automatically refreshes results when the category filter changes. </p>
 * 
 * <p> Copyright: Arizona State University © 2025 </p>
 * 
 * @author Group 14
 * 
 * @version 1.00    2025-10-24 Initial implementation
 */
public class ViewSearchPosts {

    /**
     * <p> Method: displaySearchPosts() </p>
     * 
     * <p> Description: Creates and displays the Search Posts interface. This method builds
     * the complete UI layout including a header, search input field, category filter rail,
     * results list view, and action buttons. It applies appropriate stylesheets, creates
     * the controller instance, and connects all UI event handlers to controller methods.
     * The interface allows users to search for posts by keyword and filter by category,
     * with automatic live refresh when category selections change. </p>
     * 
     * @param stage The JavaFX stage on which to display the search interface
     * @param user  The currently logged-in user
     */
    public static void displaySearchPosts(Stage stage, User user) {
        // Create page header
        Label hdr = new Label("Search Posts");
        hdr.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Create search input field
        TextField q = new TextField();
        q.setPromptText("Search keyword…");

        // Create category filter toggle buttons
        ToggleButton allBtn = new ToggleButton("All");
        ToggleButton generalBtn = new ToggleButton("General");
        ToggleButton lecturesBtn = new ToggleButton("Lectures");
        ToggleButton teamNormsBtn = new ToggleButton("Team Norms");
        ToggleButton assignmentsBtn = new ToggleButton("Assignments");
        ToggleButton socialBtn = new ToggleButton("Social");

        // Group category buttons and configure layout
        ToggleGroup catGroup = new ToggleGroup();
        for (ToggleButton b : new ToggleButton[]{allBtn,generalBtn,lecturesBtn,teamNormsBtn,assignmentsBtn,socialBtn}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setToggleGroup(catGroup);
        }
        allBtn.setSelected(true);
        
        // Create category rail sidebar
        VBox catRail = new VBox(6, new Label("CATEGORIES"), allBtn, generalBtn, lecturesBtn, teamNormsBtn, assignmentsBtn, socialBtn);
        catRail.setPrefWidth(220);
        catRail.setPadding(new Insets(12));

        // Create list view for displaying search results
        ListView<PostItem> results = new ListView<>();

        // Create search bar with search button
        Button searchBtn = new Button("Search");
        HBox searchBar = new HBox(8, q, searchBtn);
        HBox.setHgrow(q, Priority.ALWAYS);

        // Create action buttons
        Button openBtn = new Button("Open");
        Button backBtn = new Button("Back");
        HBox actions = new HBox(10, new Region(), openBtn, backBtn);
        HBox.setHgrow(actions.getChildren().get(0), Priority.ALWAYS);

        // Arrange main content vertically
        VBox center = new VBox(10, hdr, searchBar, results, actions);
        center.setPadding(new Insets(12));
        VBox.setVgrow(results, Priority.ALWAYS);

        // Create root layout with category rail on left
        BorderPane root = new BorderPane(center, null, null, null, catRail);

        // Apply stylesheets for consistent theming
        root.getStylesheets().add(
        	    Objects.requireNonNull(applicationMain.HW2Main.class.getResource("application.css"),
        	        "application.css not found in applicationMain").toExternalForm()
        	);
        	root.getStylesheets().add(
        	    Objects.requireNonNull(applicationMain.HW2Main.class.getResource("student.css"),
        	        "student.css not found in applicationMain").toExternalForm()
        	);

        // Create controller and connect event handlers
        ControllerSearchPosts controller = new ControllerSearchPosts(stage, user, q, catGroup, results);
        searchBtn.setOnAction(e -> controller.onSearch());
        openBtn.setOnAction(e -> controller.onOpenSelected());
        backBtn.setOnAction(e -> controller.onBack());
        q.setOnAction(e -> controller.onSearch());

        // Display the scene
        stage.setScene(new Scene(root, 1100, 700));
        stage.setTitle("Search Posts");
        stage.show();
        
     // Live-refresh when category changes (non-invasive)
        catGroup.selectedToggleProperty().addListener((obs, o, n) -> controller.onSearch());
        
    }
}
