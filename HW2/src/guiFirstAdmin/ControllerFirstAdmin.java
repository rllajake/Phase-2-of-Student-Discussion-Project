package guiFirstAdmin;

import java.sql.SQLException;
import database.Database;
import entityClasses.User;
import guiNewAccount.ViewNewAccount;
import javafx.stage.Stage;

public class ControllerFirstAdmin {
	/*-********************************************************************************************

	The controller attributes for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	private static String adminUsername = "";
	private static String adminPassword1 = "";
	private static String adminPassword2 = "";		
	protected static Database theDatabase = applicationMain.FoundationsMain.database;		

	/*-********************************************************************************************

	The User Interface Actions for this page
	
	*/
	
	
	/**********
	 * <p> Method: setAdminUsername() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the username field in the
	 * View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminUsername() {
		adminUsername = ViewFirstAdmin.text_AdminUsername.getText();
	}
	
	
	/**********
	 * <p> Method: setAdminPassword1() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the password 1 field in
	 * the View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminPassword1() {
		adminPassword1 = ViewFirstAdmin.text_AdminPassword1.getText();
		ViewFirstAdmin.label_PasswordsDoNotMatch.setText("");
	}
	
	
	/**********
	 * <p> Method: setAdminPassword2() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the password 2 field in
	 * the View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminPassword2() {
		adminPassword2 = ViewFirstAdmin.text_AdminPassword2.getText();		
		ViewFirstAdmin.label_PasswordsDoNotMatch.setText("");
	}
	
	
	/**********
	 * <p> Method: doSetupAdmin() </p>
	 * 
	 * <p> Description: This method is called when the user presses the button to set up the Admin
	 * account.  It start by trying to establish a new user and placing that user into the
	 * database.  If that is successful, we proceed to the UserUpdate page.</p>
	 * 
	 */
	protected static void doSetupAdmin(Stage ps, int r) {
		
		// Validation check for if any of the entries are empty, we display the corresponding error
		if (ViewFirstAdmin.text_AdminUsername.getText().equals("") || ViewFirstAdmin.text_AdminPassword1.getText().equals("") || ViewFirstAdmin.text_AdminPassword2.getText().equals("")) {
			ViewFirstAdmin.text_AdminUsername.setText("");
			ViewFirstAdmin.text_AdminPassword1.setText("");
			ViewFirstAdmin.text_AdminPassword2.setText("");
			ViewFirstAdmin.alertUsernameOrPasswordBlankError.showAndWait();
			return;
		}
		
		/*************
		 * Code Section for Username Validation
		 * Pasted from updated implementation from UserNameRecognizerConsoleTestbedF25
		 */
		
		// local variables for username FSM simulation
		int state = 0;
		String inputline = adminUsername;
		int currentCharIndex = 0;
		char currentChar = adminUsername.charAt(0);
		String usernameRecognizerInput = adminUsername;
		boolean running = true;
		int nextState = -1;
		int usernameSize = 0;
		boolean finalState = false;
		boolean validUser = false;
		
		// FSM continues until EOI is reached or if there is a forbidden character that doesn't lead to any state
		while (running) {
			// The switch statement takes the execution to the code for the current state, where
			// that code sees whether or not the current character is valid to transition to a
			// next state
			switch (state) {
			case 0: 
				// State 0 has 1 valid transition that is addressed by an if statement.
				
				// The current character is checked against A-Z, a-z, 0-9. If any are matched
				// the FSM goes to state 1
				
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||	// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {	// Check for 0-9
					nextState = 1;
					
					// Count the character 
					usernameSize++;
					
					// This only occurs once, so there is no need to check for the size getting
					// too large.
				}
				// If it is none of those characters, the FSM halts
				else 
					running = false;
				
				// The execution of this state is finished
				break;
			
			case 1: 
				// State 1 has two valid transitions, 
				//	1: a A-Z, a-z, 0-9 that transitions back to state 1
				//  2: a period that transitions to state 2 

				
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||	// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {	// Check for 0-9
					nextState = 1;
					
					// Count the character
					usernameSize++;
				}
				// . -> State 2
				// include choices of hyphen and underscore as part of the special characters
				else if (currentChar == '.' || currentChar == '-' || currentChar == '_') {							// Check for /
					nextState = 2;
					
					// Count the .
					usernameSize++;
				}				
				// If it is none of those characters, the FSM halts
				else
					running = false;
				
				// The execution of this state is finished
				// If the size is larger than 16, the loop must stop
				if (usernameSize > 16)
					running = false;
				break;			
				
			case 2: 
				// State 2 deals with a character after a period in the name.
				// inclusive with now period, hyphen, and underscore
				
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||	// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {	// Check for 0-9
					nextState = 1;
					
					// Count the odd digit
					usernameSize++;
					
				}
				// If it is none of those characters, the FSM halts
				else 
					running = false;

				// The execution of this state is finished
				// If the size is larger than 16, the loop must stop
				if (usernameSize > 16)
					running = false;
				break;			
			}
			
			if (running) {
				// When the processing of a state has finished, the FSM proceeds to the next
				// character in the input and if there is one, it fetches that character and
				// updates the currentChar.  If there is no next character the currentChar is
				// set to a blank.
				currentCharIndex++;
				if (currentCharIndex < inputline.length())
					currentChar = inputline.charAt(currentCharIndex);
				else {
					currentChar = ' ';
					running = false;
				}

				// Move to the next state
				state = nextState;
				
				// Is the new state a final state?  If so, signal this fact.
				if (state == 1) finalState = true;

				// Ensure that one of the cases sets this to a valid value
				nextState = -1;
			}
			// Should the FSM get here, the loop starts again
	
		}
		
		// prints out the state we are in when the FSM is finished running
		System.out.println(state);
		
		switch (state) {
		case 0:
			// State 0 is not a final state, so we can return a very specific error message
			// if the first character is a forbidden character
			ViewFirstAdmin.text_AdminUsername.setText("");
			ViewFirstAdmin.text_AdminPassword1.setText("");
			ViewFirstAdmin.text_AdminPassword2.setText("");
			ViewFirstAdmin.alertUsernameValidityLengthError.showAndWait();
			
			guiFirstAdmin.ViewFirstAdmin.displayFirstAdmin(ViewFirstAdmin.theStage);
			return;

		case 1:
			// State 1 is a final state.  Check to see if the UserName length is valid.  If so we
			// we must ensure the whole string has been consumed.

			if (usernameSize < 4) {
				// UserName is too small
				ViewFirstAdmin.text_AdminUsername.setText("");
				ViewFirstAdmin.text_AdminPassword1.setText("");
				ViewFirstAdmin.text_AdminPassword2.setText("");
				ViewFirstAdmin.alertUsernameValidityLengthError.showAndWait();
				
				guiFirstAdmin.ViewFirstAdmin.displayFirstAdmin(ViewFirstAdmin.theStage);
				return;
			}
			else if (usernameSize > 16) {
				// UserName is too long
				ViewFirstAdmin.text_AdminUsername.setText("");
				ViewFirstAdmin.text_AdminPassword1.setText("");
				ViewFirstAdmin.text_AdminPassword2.setText("");
				ViewFirstAdmin.alertUsernameValidityLengthError.showAndWait();
				
				guiFirstAdmin.ViewFirstAdmin.displayFirstAdmin(ViewFirstAdmin.theStage);
				return;
			} 
			// Incorporate 3rd else condition for when the first character of the input is an integer
			else if(Character.isDigit(adminUsername.charAt(0))) {
				ViewFirstAdmin.text_AdminUsername.setText("");
				ViewFirstAdmin.text_AdminPassword1.setText("");
				ViewFirstAdmin.text_AdminPassword2.setText("");
				ViewFirstAdmin.alertUsernameValidityLeadingCharacterError.showAndWait();
				
				guiFirstAdmin.ViewFirstAdmin.displayFirstAdmin(ViewFirstAdmin.theStage);
				return;
			}
			else if (currentCharIndex < adminUsername.length()) {
				// There are characters remaining in the input, so the input is not valid
				ViewFirstAdmin.text_AdminUsername.setText("");
				ViewFirstAdmin.text_AdminPassword1.setText("");
				ViewFirstAdmin.text_AdminPassword2.setText("");
				ViewFirstAdmin.alertUsernameValiditySpecialCharacterError.showAndWait();
				
				guiFirstAdmin.ViewFirstAdmin.displayFirstAdmin(ViewFirstAdmin.theStage);
				return;
			}
			
			validUser = true;
			System.out.println("This is a valid username.");
			break;
		
		case 2:
			// State 2 is not a final state, so we can return a very specific error message
			// display corresponding error for when the special character conditions are not met
			ViewFirstAdmin.text_AdminUsername.setText("");
			ViewFirstAdmin.text_AdminPassword1.setText("");
			ViewFirstAdmin.text_AdminPassword2.setText("");
			ViewFirstAdmin.alertUsernameValiditySpecialCharacterError.showAndWait();
			
			guiFirstAdmin.ViewFirstAdmin.displayFirstAdmin(ViewFirstAdmin.theStage);
			return;
			
		default:
			// This is for the case where we have a state that is outside of the valid range.
			// This should not happen
		}
		
		/****************
		 * Code Section for Password Validation
		 * 
		 */
		
		// local variables for password FSM
		String passwordRecognizerInput = adminPassword1;
		int currCharPassIndex = 0;
		char currPassChar = adminPassword1.charAt(0);
		boolean foundUpper = false;
		boolean foundLower = false;
		boolean foundNumber = false;
		boolean foundSpecialChar = false;
		boolean foundLongEnough = false;
		boolean runningPass = true;
		boolean validPass = false;
		boolean notInvalidChar = true;
		
		// FSM continues until EOI is reached or otherwise there is a forbidden character and the password is invalid
		while(runningPass) {
			if (currPassChar >= 'A' && currPassChar <= 'Z') {
				foundUpper = true;
			} else if (currPassChar >= 'a' && currPassChar <= 'z') {
				foundLower = true;
			} else if (currPassChar >= '0' && currPassChar <= '9') {
				foundNumber = true;
			} else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currPassChar) >= 0) {
				foundSpecialChar = true;
			} else {
				notInvalidChar = false;
			}
			// Added another condition making it so that the length doesn't exceed 32 characters
			if (currCharPassIndex > 31) {
				foundLongEnough = false;
			}
			else if (currCharPassIndex >= 7) {
				foundLongEnough = true;
			}
			
			
			// Go to the next character if there is one
			currCharPassIndex++;
			if (currCharPassIndex >= adminPassword1.length())
				runningPass = false;
			else
				currPassChar = adminPassword1.charAt(currCharPassIndex);
		}
		
		// if all password conditions are met, the password is valid, otherwise we give a corresponding error
		if(foundUpper && foundLower && foundNumber && foundSpecialChar && foundLongEnough && notInvalidChar) {
			validPass = true;
			System.out.println("This is a valid password.");
		}
		else {
			ViewFirstAdmin.text_AdminPassword1.setText("");
			ViewFirstAdmin.text_AdminPassword2.setText("");
			ViewFirstAdmin.alertPasswordValidityError.showAndWait();
			return;
		}
		
		// Make sure the two passwords are the same
		if (validUser && validPass && adminPassword1.compareTo(adminPassword2) == 0) {
        	// Create the passwords and proceed to the user home page
        	User user = new User(adminUsername, adminPassword1, "", "", "", "", "", true, false, 
        			false);
            try {
            	// Create a new User object with admin role and register in the database
            	theDatabase.register(user);
            	}
            catch (SQLException e) {
                System.err.println("*** ERROR *** Database error trying to register a user: " + 
                		e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
            
            // User was established in the database, so navigate to the User Update Page
        	guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewFirstAdmin.theStage, user, true);
		}
		else {
			// The two passwords are NOT the same, so clear the passwords, explain the passwords
			// must be the same, and clear the message as soon as the first character is typed.
			ViewFirstAdmin.text_AdminPassword1.setText("");
			ViewFirstAdmin.text_AdminPassword2.setText("");
			ViewFirstAdmin.label_PasswordsDoNotMatch.setText(
					"The two passwords must match. Please try again!");
		}
	}
	
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 */
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	
}

