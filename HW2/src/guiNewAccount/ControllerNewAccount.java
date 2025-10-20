package guiNewAccount;

import java.sql.SQLException;

import database.Database;
import entityClasses.User;

public class ControllerNewAccount {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/


	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.HW2Main.database;
	
	/**********
	 * <p> Method: public doCreateUser() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the User Setup
	 * button.  This method checks the input fields to see that they are valid.  If so, it then
	 * creates the account by adding information to the database.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doCreateUser() {
		
		// Fetch the username and password. (We use the first of the two here, but we will validate
		// that the two password fields are the same before we do anything with it.)
		String username = ViewNewAccount.text_Username.getText();
		String password = ViewNewAccount.text_Password1.getText();
		
		System.out.println("The username is: " + username);
		
		// Display key information to the log
		System.out.println("** Account for Username: " + username + "; theInvitationCode: "+
				ViewNewAccount.theInvitationCode + "; email address: " + 
				ViewNewAccount.emailAddress + "; Role: " + ViewNewAccount.theRole);
		
		// If username or password is blank, then send an error to enter in all the information
		if (ViewNewAccount.text_Username.getText().equals("") || ViewNewAccount.text_Password1.getText().equals("") || ViewNewAccount.text_Password2.getText().equals("")) {
			ViewNewAccount.text_Username.setText("");
			ViewNewAccount.text_Password1.setText("");
			ViewNewAccount.text_Password2.setText("");
			ViewNewAccount.alertUsernameOrPasswordBlankError.showAndWait();
			return;
		}
		
		// Initialize local variables that will be created during this process
		int roleCode = 0;
		User user = null;

		/*************
		 * Code Section for Username Validation
		 * Pasted from updated implementation from UserNameRecognizerConsoleTestbedF25
		 */
		
		// local variables for username FSM simulation
		int state = 0;
		String inputline = username;
		int currentCharIndex = 0;
		char currentChar = username.charAt(0);
		String usernameRecognizerInput = username;
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
			ViewNewAccount.text_Username.setText("");
			ViewNewAccount.text_Password1.setText("");
			ViewNewAccount.text_Password2.setText("");
			ViewNewAccount.alertUsernameValidityLengthError.showAndWait();
			
			guiNewAccount.ViewNewAccount.displayNewAccount(ViewNewAccount.theStage, ViewNewAccount.theInvitationCode);
			return;

		case 1:
			// State 1 is a final state.  Check to see if the UserName length is valid.  If so we
			// we must ensure the whole string has been consumed.

			if (usernameSize < 4) {
				// UserName is too small
				ViewNewAccount.text_Username.setText("");
				ViewNewAccount.text_Password1.setText("");
				ViewNewAccount.text_Password2.setText("");
				ViewNewAccount.alertUsernameValidityLengthError.showAndWait();
				
				guiNewAccount.ViewNewAccount.displayNewAccount(ViewNewAccount.theStage, ViewNewAccount.theInvitationCode);
				return;
			}
			else if (usernameSize > 16) {
				// UserName is too long
				ViewNewAccount.text_Username.setText("");
				ViewNewAccount.text_Password1.setText("");
				ViewNewAccount.text_Password2.setText("");
				ViewNewAccount.alertUsernameValidityLengthError.showAndWait();
				
				guiNewAccount.ViewNewAccount.displayNewAccount(ViewNewAccount.theStage, ViewNewAccount.theInvitationCode);
				return;
			} 
			// Incorporate 3rd else condition for when the first character of the input is an integer
			else if(Character.isDigit(username.charAt(0))) {
				ViewNewAccount.text_Username.setText("");
				ViewNewAccount.text_Password1.setText("");
				ViewNewAccount.text_Password2.setText("");
				ViewNewAccount.alertUsernameValidityLeadingCharacterError.showAndWait();
				
				guiNewAccount.ViewNewAccount.displayNewAccount(ViewNewAccount.theStage, ViewNewAccount.theInvitationCode);
				return;
			}
			else if (currentCharIndex < username.length()) {
				// There are characters remaining in the input, so the input is not valid
				ViewNewAccount.text_Username.setText("");
				ViewNewAccount.text_Password1.setText("");
				ViewNewAccount.text_Password2.setText("");
				ViewNewAccount.alertUsernameValiditySpecialCharacterError.showAndWait();
				
				guiNewAccount.ViewNewAccount.displayNewAccount(ViewNewAccount.theStage, ViewNewAccount.theInvitationCode);
				return;
			}
			
			validUser = true;
			System.out.println("This is a valid username.");
			break;
		
		case 2:
			// State 2 is not a final state, so we can return a very specific error message
			// display corresponding error for when the special character conditions are not met
			ViewNewAccount.text_Username.setText("");
			ViewNewAccount.text_Password1.setText("");
			ViewNewAccount.text_Password2.setText("");
			ViewNewAccount.alertUsernameValiditySpecialCharacterError.showAndWait();
			
			guiNewAccount.ViewNewAccount.displayNewAccount(ViewNewAccount.theStage, ViewNewAccount.theInvitationCode);
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
		String passwordRecognizerInput = password;
		int currCharPassIndex = 0;
		char currPassChar = password.charAt(0);
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
				System.out.println("Upper case letter found");
				foundUpper = true;
			} else if (currPassChar >= 'a' && currPassChar <= 'z') {
				System.out.println("Lower case letter found");
				foundLower = true;
			} else if (currPassChar >= '0' && currPassChar <= '9') {
				System.out.println("Digit found");
				foundNumber = true;
			} else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currPassChar) >= 0) {
				System.out.println("Special character found");
				foundSpecialChar = true;
			} else {
				notInvalidChar = false;
			}
			// Added another condition making it so that the length doesn't exceed 32 characters
			if (currCharPassIndex > 31) {
				System.out.println("Now more than 32 characters have been found. Too long of a password!");
				foundLongEnough = false;
			}
			else if (currCharPassIndex >= 7) {
				System.out.println("At least 8 characters found");
				foundLongEnough = true;
			}
			
			
			// Go to the next character if there is one
			currCharPassIndex++;
			if (currCharPassIndex >= password.length())
				runningPass = false;
			else
				currPassChar = password.charAt(currCharPassIndex);
		}
		
		// if all password conditions are met, the password is valid, otherwise we give a corresponding error
		if(foundUpper && foundLower && foundNumber && foundSpecialChar && foundLongEnough && notInvalidChar) {
			validPass = true;
		}
		else {
			ViewNewAccount.text_Password1.setText("");
			ViewNewAccount.text_Password2.setText("");
			ViewNewAccount.alertPasswordValidityError.showAndWait();
			return;
		}
		
		// Make sure the two passwords are the same.
		// added valid username and valid password conditions alongside the checking if both passwords are the same
			if (validUser && validPass && ViewNewAccount.text_Password1.getText().
					compareTo(ViewNewAccount.text_Password2.getText()) == 0) {
				
				// The passwords match so we will set up the role and the User object base on the 
				// information provided in the invitation
				if (ViewNewAccount.theRole.compareTo("Admin") == 0) {
					roleCode = 1;
					user = new User(username, password, "", "", "", "", "", true, false, false);
				} else if (ViewNewAccount.theRole.compareTo("Student") == 0) {
					roleCode = 2;
					user = new User(username, password, "", "", "", "", "", false, true, false);
				} else if (ViewNewAccount.theRole.compareTo("Reviewer") == 0) {
					roleCode = 3;
					user = new User(username, password, "", "", "", "", "", false, false, true);
				} else {
					System.out.println(
							"**** Trying to create a New Account for a role that does not exist!");
					System.exit(0);
				}
				
				// Unlike the FirstAdmin, we know the email address, so set that into the user as well.
	        	user.setEmailAddress(ViewNewAccount.emailAddress);
	
	        	// Inform the system about which role will be played
				applicationMain.HW2Main.activeHomePage = roleCode;
				
	        	// Create the account based on user and proceed to the user account update page
	            try {
	            	// Create a new User object with the pre-set role and register in the database
	            	theDatabase.register(user);
	            } catch (SQLException e) {
	                System.err.println("*** ERROR *** Database error: " + e.getMessage());
	                e.printStackTrace();
	                System.exit(0);
	            }
	            
	            // The account has been set, so remove the invitation from the system
	            theDatabase.removeInvitationAfterUse(
	            		ViewNewAccount.text_Invitation.getText());
	            
	            // Set the database so it has this user and the current user
	            theDatabase.getUserAccountDetails(username);
	
	            // Navigate to the Welcome Login Page
	            guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewNewAccount.theStage, user, false);
			}
			else {
				// The two passwords are NOT the same, so clear the passwords, explain the passwords
				// must be the same, and clear the message as soon as the first character is typed.
				ViewNewAccount.text_Password1.setText("");
				ViewNewAccount.text_Password2.setText("");
				ViewNewAccount.alertUsernamePasswordError.showAndWait();
			}
		}

	
	/**********
	 * <p> Method: public performQuit() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so there is no cleanup required.  (This is important so we can minimize the impact
	 * of crashed.)
	 * 
	 */	
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	
}
