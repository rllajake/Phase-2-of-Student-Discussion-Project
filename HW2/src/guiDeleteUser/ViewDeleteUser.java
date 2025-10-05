package guiDeleteUser;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;
import java.util.Optional;

/*******
 * <p> Title: GUIDeleteUser Class. </p>
 * 
 * <p> Description: The Java/FX-based page for deleting a specified user.</p> 
 */

public class ViewDeleteUser {
	
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
	
	// GUI Area 2: It allows the admin to input a username of the user they want to delete,
	// and a button for submitting this username.
	protected static TextField text_DeleteUser = new TextField();
	protected static Button button_DeleteUser = new Button("Delete user");
	
	//Error alerts for deleting a user that doesn't exist, or the admin trying to delete themself.
	protected static Alert alertUsernameError = new Alert(AlertType.INFORMATION);
	protected static Alert alertDeleteSameUserError = new Alert(AlertType.INFORMATION);
	
	// Confirmation alert for the admin to confirm (Clicking Yes or No) whether
	// they wish to delete the user they input.
	protected static Alert confirmDeleteUser = new Alert(AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.NO);
	
	// A text label that will say if the user was successfully deleted
	protected static Label label_DeletionResult = new Label();
	
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
	private static ViewDeleteUser theView;	// Used to determine if instantiation of the class
												// is needed
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	protected static Stage theStage;			// The Stage that JavaFX has established for us
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets 
	protected static User theUser;				// The current user of the application
	
	public static Scene theDeleteUserScene = null;	// The Scene each invocation populates



	/*-*******************************************************************************************

	Constructors
	
	*/

	/**********
	 * <p> Method: displayDeleteUser(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the AddRevove page to be displayed.
	 * 
	 * It first sets up very shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User whose roles will be updated
	 *
	 */
	public static void displayDeleteUser(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI by creating the 
		// singleton instance of this class
		if (theView == null) theView = new ViewDeleteUser();
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.		
		text_DeleteUser.setText(""); // Reset the delete user input from the last use
		
		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundation Code: Admin Delete User Page");
		theStage.setScene(theDeleteUserScene);						// Set this page onto the stage
		theStage.show();											// Display it to the user
	}

	
	/**********
	 * <p> Method: ViewDeleteUserPage() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object. </p>
	 * 
	 * This is a singleton, so this is performed just once.  Subsequent uses fill in the changeable
	 * fields using the displayDeleteUser method.</p>
	 * 
	 */
	public ViewDeleteUser() {
		
		// This page is used by all roles, so we do not specify the role being used		
			
		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theDeleteUserScene = new Scene(theRootPane, width, height);
		
		// Populate the window with the title and other common widgets and set their static state
		
		// GUI Area 1
		label_PageTitle.setText("Delete User Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((event) -> 
			{guiUserUpdate.ViewUserUpdate.displayUserUpdate(theStage, theUser, false); });
		
		// GUI Area 2
		// Establish the text input operand field for the username of the user to delete
		setupTextUI(text_DeleteUser, "Arial", 18, 300, Pos.BASELINE_LEFT, 50, 200, true);
		text_DeleteUser.setPromptText("Enter User to Delete");
		
		//Button for deleting a user. Will have popups if inputted username is invalid,
		//or the same as the current user. Otherwise an "are you sure" prompt comes up.
		setupButtonUI(button_DeleteUser, "Dialog", 18, 200, Pos.CENTER, 475, 200);
		button_DeleteUser.setOnAction((event) -> {
			String username = text_DeleteUser.getText();
			deleteUserButtonLogic(username);
			});
		
		//Text label showing deletion results
		label_DeletionResult.setText("");
		setupLabelUI(label_DeletionResult, "Arial", 22, width, Pos.BASELINE_LEFT, 20, 485);
		
		//Set text for the error alert that appears when you input a username that doesn't exist
		alertUsernameError.setTitle("Invalid username!");
		alertUsernameError.setHeaderText(null);
		
		//Set text for the error alert that appears when the admin tries to delete their own account
		alertDeleteSameUserError.setTitle("Invalid username!");
		alertDeleteSameUserError.setHeaderText(null);
		
		// GUI Area 3		
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
		button_Return.setOnAction((event) -> {ControllerDeleteUser.performReturn(); });

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
		button_Logout.setOnAction((event) -> {ControllerDeleteUser.performLogout(); });
    
		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
		button_Quit.setOnAction((event) -> {ControllerDeleteUser.performQuit(); });
		
		// This is the end of the GUI Widgets for the page
		
		theRootPane.getChildren().addAll(
				label_PageTitle, label_UserDetails, 
				button_UpdateThisUser, line_Separator1,
				text_DeleteUser, button_DeleteUser,
				label_DeletionResult, line_Separator4, 
				button_Return, button_Logout, 
				button_Quit
				);
	}	

	/*-*******************************************************************************************

	Helper methods used to minimizes the number of lines of code needed above
	
	*/

	//Used in the Delete User Button to show different alerts
	// The alerts can either be for errors or for confirming the user's choice to delete
	private static void deleteUserButtonLogic(String username) {
		if (theDatabase.getUserAccountDetails(username) == false) {
     		// Inform the admin that they input an invalid username
			label_DeletionResult.setText("");
    		alertUsernameError.setContentText(
    				"No such user with that username exists. Try again!");
    		alertUsernameError.showAndWait();
    		return;
    	} else if (username.equals(ViewDeleteUser.theUser.getUserName())) {
    		// Inform the admin that they can't delete themself!
    		label_DeletionResult.setText("");
    		alertDeleteSameUserError.setContentText(
    				"You can't delete your own account!");
    		alertDeleteSameUserError.showAndWait();
    		return;
    	}
		
		//Show confirmation alert (Yes/No?)
		confirmDeleteUser.setTitle("Are you sure?");
		confirmDeleteUser.setHeaderText(null);
		confirmDeleteUser.setContentText("Do you really want to remove user: " + username + "?");
		
		Optional<ButtonType> result = confirmDeleteUser.showAndWait();
		
		//If admin pressed yes, delete the user with that username
		if (result.isPresent() && result.get() == ButtonType.YES) {
			ControllerDeleteUser.performRemoveUser();
		}
	}
	
	
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
}