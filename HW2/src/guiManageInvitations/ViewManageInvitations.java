package guiManageInvitations;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;

import java.util.ArrayList;
import java.util.List;

/*******
 * <p> Title: GUIManageInvitations Class. </p>
 * 
 * <p> Description: The Java/FX-based page for managing invitations.</p> 
 */

public class ViewManageInvitations {

	/*-*******************************************************************************************
	Attributes
	
	*/

	// These are the application values required by the user interface

	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.

	// GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
	// and a button to allow this user to update the account settings.
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");

	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator1 = new Line(20, 95, width-20, 95);
	
	// GUI Area 2: This area is used to provide status of the system.  This basic foundational code
		// does not have much current status information to display.
		public static Label label_NumberOfInvitations = 
				new Label("Number of Oustanding Invitations: x");
		protected static Label label_NumberOfUsers = new Label("Number of Users: x");

	// GUI Area 2: This is the first of two areas provided the admin with a set of action buttons
	// that can be used to perform the tasks allocated to the admin role.  This part is about
	// inviting potential new users to establish an account and what role that user will have.
	protected static Label label_Invitations = new Label("Send An Invitation");
	protected static Label label_InvitationEmailAddress = new Label("Email Address");
	protected static TextField text_InvitationEmailAddress = new TextField();
	protected static ComboBox <String> combobox_SelectRole = new ComboBox <String>();
	protected static String [] roles = {"Admin", "Student", "Reviewer"};
	protected static Button button_SendInvitation = new Button("Send Invitation");
	protected static Alert alertEmailError = new Alert(AlertType.INFORMATION);
	protected static Alert alertEmailSent = new Alert(AlertType.INFORMATION);
	protected static Alert alertCurrentError = new Alert(AlertType.INFORMATION);
	
	protected static Alert alertDeadline = new Alert(AlertType.INFORMATION);


	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator4 = new Line(20, 525, width-20,525);

	// GUI Area 3: This is last of the GUI areas.  It is used for quitting the application, logging
	// out, and on other pages a return is provided so the user can return to a previous page when
	// the actions on that page are complete.  Be advised that in most cases in this code, the 
	// return is to a fixed page as opposed to the actual page that invoked the pages.
	protected static Button button_Return = new Button("Return");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.

	// These attributes are used to configure the page and populate it with this user's information
	private static ViewManageInvitations theView;	// Used to determine if instantiation of the class
												// is needed
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	protected static Stage theStage;			// The Stage that JavaFX has established for us
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets 
	protected static User theUser;				// The current user of the application

	public static Scene theManageInvitationsScene = null;	// The Scene each invocation populates



	/*-*******************************************************************************************
	Constructors
	
	*/

	public static void displayManageInvitations(Stage ps, User user) {

		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;

		// If not yet established, populate the static aspects of the GUI by creating the 
		// singleton instance of this class
		if (theView == null) theView = new ViewManageInvitations();	

		// Set the role for potential users to the default (No role selected)
		combobox_SelectRole.getSelectionModel().select(0);

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundation Code: Admin Manage Invitations Page");
		theStage.setScene(theManageInvitationsScene);						// Set this page onto the stage
		theStage.show();
		label_NumberOfUsers.setText("Number of users: " + 
				theDatabase.getNumberOfUsers());
		
		// Display it to the user
		label_UserDetails.setText("Admin: " + theUser.getUserName());
	}


	/**********
	 * <p> Method: ViewManageInvitationsPage() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object. </p>
	 * 
	 * This is a singleton, so this is performed just once.  Subsequent uses fill in the changeable
	 * fields using the displayManageInvitations method.</p>
	 * 
	 */
	public ViewManageInvitations() {

		// This page is used by all roles, so we do not specify the role being used		

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theManageInvitationsScene = new Scene(theRootPane, width, height);

		// Populate the window with the title and other common widgets and set their static state

		// GUI Area 1
		label_PageTitle.setText("Manage Invitations Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

		// GUI Area 3
		setupLabelUI(label_Invitations, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 175);

		setupLabelUI(label_InvitationEmailAddress, "Arial", 16, width, Pos.BASELINE_LEFT,
		20, 210);

		setupTextUI(text_InvitationEmailAddress, "Arial", 16, 360, Pos.BASELINE_LEFT,
		130, 205, true);
		
		setupLabelUI(label_NumberOfInvitations, "Arial", 20, 200, Pos.BASELINE_LEFT, 20, 105);
		label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
						theDatabase.getNumberOfInvitations());
			
		setupLabelUI(label_NumberOfUsers, "Arial", 20, 200, Pos.BASELINE_LEFT, 20, 135);

		setupComboBoxUI(combobox_SelectRole, "Dialog", 16, 90, 500, 205);

		List<String> list = new ArrayList<String>();	// Create a new list empty list of the
		for (int i = 0; i < roles.length; i++) {		// roles this code currently supports
			list.add(roles[i]);
		}
		combobox_SelectRole.setItems(FXCollections.observableArrayList(list));
		combobox_SelectRole.getSelectionModel().select(0);
		alertEmailSent.setTitle("Invitation");
		alertEmailSent.setHeaderText("Invitation was sent");

		alertCurrentError.setTitle("Current User Error");
		alertCurrentError.setHeaderText("You are the current user.");
		
		alertDeadline.setTitle("Code Deadline");
		alertDeadline.setHeaderText("10-minute code was sent.");

		setupButtonUI(button_SendInvitation, "Dialog", 16, 150, Pos.CENTER, 630, 205);
		button_SendInvitation.setOnAction((event) -> {ControllerManageInvitations.performInvitation(); });

		// GUI Area 3		
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
		button_Return.setOnAction((event) -> {ControllerManageInvitations.performReturn(); });

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
		button_Logout.setOnAction((event) -> {ControllerManageInvitations.performLogout(); });

		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
		button_Quit.setOnAction((event) -> {ControllerManageInvitations.performQuit(); });

		// This is the end of the GUI Widgets for the page

		theRootPane.getChildren().addAll(
				label_NumberOfInvitations, label_NumberOfUsers, label_PageTitle, label_UserDetails, line_Separator1,
				label_Invitations, label_InvitationEmailAddress,
				text_InvitationEmailAddress, combobox_SelectRole,
				button_SendInvitation, line_Separator4, 
				button_Return, button_Logout, 
				button_Quit
				);
	}	

	/*-*******************************************************************************************
	Helper methods used to minimizes the number of lines of code needed above
	
	*/



	/**********
	 * Private local method to initialize the standard fields for a label
	 * 
	 * @param l		The Label object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x,
			double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}


	/**********
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b		The Button object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	protected static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x,
			double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}

	/**********
	 * Private local method to initialize the standard fields for a text field
	 */
	private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);		
		t.setEditable(e);
	}


	/**********
	 * Private local method to initialize the standard fields for a ComboBox
	 * 
	 * @param c		The ComboBox object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the ComboBox
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private void setupComboBoxUI(ComboBox <String> c, String ff, double f, double w, double x, double y){
		c.setStyle("-fx-font: " + f + " " + ff + ";");
		c.setMinWidth(w);
		c.setLayoutX(x);
		c.setLayoutY(y);
	}
}