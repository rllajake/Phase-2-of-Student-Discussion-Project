package guiListUsers;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import entityClasses.User;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * <p> Title: ViewListUsers Class. </p>
 *
 * <p> Description: JavaFX view that lists all users in the system with basic account details
 * and role flags. </p>
 */
public class ViewListUsers {

    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    private static Label label_Title = new Label("All Users");
    private static Label label_Subtitle = new Label("Read-only listing of user accounts and roles");
    private static Button button_Return = new Button("Return");

    private static TableView<UserRow> table = new TableView<>();

    // Keep track of the current admin user to return to Admin Home
    protected static User theUser;
    protected static Stage theStage;
    
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    
    /**********
	 * <p> Method: public displayListUsers(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is called when the application first starts. It create an
	 * an instance of the View class.  
	 * 
	 * NOTE: As described below, this code does not implement MVC using the singleton pattern used
	 * by most of the pages as the code is written this way because we know with certainty that it
	 * will only be called once.  For this reason, we directly call the private class constructor.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user is the current user
	 */
    public static void displayListUsers(Stage ps, User user) {
        theUser = user;
        theStage = ps;

        Pane root = new Pane();

        setupLabel(label_Title, "Arial", 32, Pos.CENTER, 0, 20, width);
        setupLabel(label_Subtitle, "Arial", 16, Pos.CENTER, 0, 64, width);

        setupTable();

        table.setItems(loadUsers());

        button_Return.setLayoutX(width - 140);
        button_Return.setLayoutY(height - 60);
        button_Return.setPrefWidth(120);
        button_Return.setOnAction(e -> ControllerListUsers.performReturn());

        root.getChildren().addAll(label_Title, label_Subtitle, table, button_Return);

        Scene scene = new Scene(root, width, height);
        ps.setTitle("List Users");
        ps.setScene(scene);
        ps.show();
    }

    private static void setupTable() {
        table.getColumns().clear();
        table.setLayoutX(20);
        table.setLayoutY(100);
        table.setPrefWidth(width - 40);
        table.setPrefHeight(height - 180);

        TableColumn<UserRow, String> cUsername = new TableColumn<>("Username");
        cUsername.setPrefWidth(160);
        cUsername.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUsername()));

        TableColumn<UserRow, String> cFirst = new TableColumn<>("First");
        cFirst.setPrefWidth(120);
        cFirst.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFirstName()));

        TableColumn<UserRow, String> cMiddle = new TableColumn<>("Middle");
        cMiddle.setPrefWidth(120);
        cMiddle.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMiddleName()));

        TableColumn<UserRow, String> cLast = new TableColumn<>("Last");
        cLast.setPrefWidth(140);
        cLast.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getLastName()));

        TableColumn<UserRow, String> cPref = new TableColumn<>("Preferred");
        cPref.setPrefWidth(140);
        cPref.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPreferredFirstName()));

        TableColumn<UserRow, String> cEmail = new TableColumn<>("Email");
        cEmail.setPrefWidth(220);
        cEmail.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEmailAddress()));

        TableColumn<UserRow, Boolean> cAdmin = new TableColumn<>("Admin");
        cAdmin.setPrefWidth(80);
        cAdmin.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().getAdminRole()));

        TableColumn<UserRow, Boolean> cStudent = new TableColumn<>("Student");
        cStudent.setPrefWidth(80);
        cStudent.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().getStudent()));

        TableColumn<UserRow, Boolean> cReviewer = new TableColumn<>("Reviewer");
        cReviewer.setPrefWidth(80);
        cReviewer.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().getReviewer()));
        
        // Make columns share the table width
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Tiny fudge factor for borders/insets so columns dont exceed table width
        double pad = 12;
        var usableWidth = table.widthProperty().subtract(pad);
        
        // Small mins so they can squeeze if needed
        cUsername.setMinWidth(70);
        cFirst.setMinWidth(70);
        cMiddle.setMinWidth(70);
        cLast.setMinWidth(80);
        cPref.setMinWidth(90);
        cEmail.setMinWidth(120);
        cAdmin.setMinWidth(60);
        cStudent.setMinWidth(60);
        cReviewer.setMinWidth(60);
        
        // Proportional widths that sum to around 1.0
        cUsername.prefWidthProperty().bind(usableWidth.multiply(0.14));
        cFirst.prefWidthProperty().bind(usableWidth.multiply(0.10));
        cMiddle.prefWidthProperty().bind(usableWidth.multiply(0.10));
        cLast.prefWidthProperty().bind(usableWidth.multiply(0.12));
        cPref.prefWidthProperty().bind(usableWidth.multiply(0.12));
        cEmail.prefWidthProperty().bind(usableWidth.multiply(0.24));
        cAdmin.prefWidthProperty().bind(usableWidth.multiply(0.06));
        cStudent.prefWidthProperty().bind(usableWidth.multiply(0.06));
        cReviewer.prefWidthProperty().bind(usableWidth.multiply(0.06));

        table.getColumns().addAll(Arrays.asList(
        		cUsername, cFirst, cMiddle, cLast, cPref, cEmail, cAdmin, cStudent, cReviewer
        		));
    }

    private static void setupLabel(Label l, String ff, double f, Pos align, double x, double y, double w) {
        l.setFont(new Font(ff, f));
        l.setAlignment(align);
        l.setLayoutX(x);
        l.setLayoutY(y);
        l.setPrefWidth(w);
    }

    private static ObservableList<UserRow> loadUsers() {
        List<UserRow> rows = new ArrayList<>();
        try {
            List<String> usernames = theDatabase.getUserList();
            if (usernames != null) {
                for (String u : usernames) {
                    if ("<Select a User>".equals(u)) continue;
                    if (theDatabase.getUserAccountDetails(u)) {
                        UserRow r = new UserRow(
                        		theDatabase.getCurrentUsername(),
                        		theDatabase.getCurrentFirstName(),
                        		theDatabase.getCurrentMiddleName(),
                        		theDatabase.getCurrentLastName(),
                        		theDatabase.getCurrentPreferredFirstName(),
                        		theDatabase.getCurrentEmailAddress(),
                        		theDatabase.getCurrentAdminRole(),
                        		theDatabase.getCurrentStudentRole(),
                        		theDatabase.getCurrentReviewerRole()
                        );
                        rows.add(r);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return FXCollections.observableArrayList(rows);
    }

    public static class UserRow {
        private String username;
        private String firstName;
        private String middleName;
        private String lastName;
        private String preferredFirstName;
        private String emailAddress;
        private boolean adminRole;
        private boolean studentRole;
        private boolean reviewerRole;

        public UserRow(String username, String firstName, String middleName, String lastName,
                String preferredFirstName, String emailAddress, boolean adminRole, boolean studentRole, boolean reviewerRole) {
            this.username = username;
            this.firstName = nvl(firstName);
            this.middleName = nvl(middleName);
            this.lastName = nvl(lastName);
            this.preferredFirstName = nvl(preferredFirstName);
            this.emailAddress = nvl(emailAddress);
            this.adminRole = adminRole;
            this.studentRole = studentRole;
            this.reviewerRole = reviewerRole;
        }

        private String nvl(String s) { return s == null ? "" : s; }

        public String getUsername() { return username; }
        public String getFirstName() { return firstName; }
        public String getMiddleName() { return middleName; }
        public String getLastName() { return lastName; }
        public String getPreferredFirstName() { return preferredFirstName; }
        public String getEmailAddress() { return emailAddress; }
        public boolean getAdminRole() { return adminRole; }
        public boolean getStudent() { return studentRole; }
        public boolean getReviewer() { return reviewerRole; }
    }
}