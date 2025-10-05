package guiManageInvitations;

import database.Database;

public class ControllerManageInvitations {

	/*-********************************************************************************************
	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	public static String emailAddressErrorMessage = "";	// The error message text
	public static String emailAddressInput = "";		// The input being processed
	public static int emailAddressIndexofError = -1;	// The index where the error was located
	private static int state = 0;						// The current state value
	private static int nextState = 0;					// The next state value
	private static boolean finalState = false;			// Is this state a final state?
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running
	private static int domainPartCounter = 0;			// A domain name may not exceed 63 characters
	
	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewManageInvitations.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			ViewManageInvitations.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewManageInvitations.alertEmailError.showAndWait();
			return;
		}

		// Verify that the current user cannot send an invitation to themselves, display an error message
			if (emailAddress.equals(theDatabase.getCurrentEmailAddress())) {
				System.out.println("Please choose another user that isn't you.");
				// need to add an alert here
				ViewManageInvitations.alertCurrentError.setContentText("Please choose another user that isn't you.");
				ViewManageInvitations.alertCurrentError.showAndWait();
				return;
			}

		// Otherwise, clear, if any, old invitations associated with the email address entered
		theDatabase.clearOldInvitationCode(emailAddress);

		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewManageInvitations.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewManageInvitations.alertEmailError.showAndWait();
			return;
		}

		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewManageInvitations.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewManageInvitations.alertDeadline.setContentText(msg);
		ViewManageInvitations.alertDeadline.showAndWait();

		// Update the Admin Home pages status
		ViewManageInvitations.text_InvitationEmailAddress.setText("");
		guiManageInvitations.ViewManageInvitations.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}


	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code currently only checks to see that the email address is not
	 * empty.  In the future, a syntactic check must be performed and maybe there is a way to check
	 * if a properly email address is active.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0) {
			return true;
		}
		
		// The following are the local variable used to perform the Finite State Machine simulation
				state = 0;							// This is the FSM state number
				inputLine = emailAddress;					// Save the reference to the input line as a global
				currentCharNdx = 0;					// The index of the current character

				// The Finite State Machines continues until the end of the input is reached or at some 
				// state the current character does not match any valid transition to a next state

				emailAddressInput = emailAddress;			// Save a copy of the input

				// Let's ensure there is input
				currentChar = emailAddress.charAt(0);		// The current character from the above indexed position

				// Let's ensure the address is not too long
				if (emailAddress.length() > 255) {
					emailAddressErrorMessage = "A valid email address must be no more than 255 characters.\n";
					return true;
				}
				running = true;						// Start the loop
				System.out.println("\nCurrent Final Input  Next  DomainName\nState   State Char  State  Size");

				// The Finite State Machines continues until the end of the input is reached or at some 
				// state the current character does not match any valid transition to a next state
				while (running) {
					// The switch statement takes the execution to the code for the current state, where
					// that code sees whether or not the current character is valid to transition to a
					// next state
					nextState = -1;						// Default to there is no next state		
					
					switch (state) {
					case 0: 
						// State 0 has just 1 valid transition.
						// The current character is must be checked against 62 options. If any are matched
						// the FSM must go to state 1
						// The first and the second check for an alphabet character the third a numeric
						if ((currentChar >= 'A' && currentChar <= 'Z')|| 		// Upper case
								(currentChar >= 'a' && currentChar <= 'z') ||	// Lower case
								(currentChar >= '0' && currentChar <= '9')) {	// Digit
							nextState = 1;
						}
										
						// If it is none of those characters, the FSM halts
						else { 
							running = false;
						}
						
						break;				
						// The execution of this state is finished
					
					case 1: 
						// State 1 has three valid transitions.  
						
						// Replace this with the required code
						if ((currentChar >= 'A' && currentChar <= 'Z')|| 		// Upper case
								(currentChar >= 'a' && currentChar <= 'z') ||	// Lower case
								(currentChar >= '0' && currentChar <= '9')) {	// Digit
							nextState = 1;
						}
						
						// If the next character is a period, FSM transitions to state 0
						else if (currentChar == '.') {
							nextState = 0;
						}
						
						// If the next character is @, FSM transitions to state 2
						else if (currentChar == '@') {
							nextState = 2;
						}
						
						// If it is none of those characters, the FSM halts
						else {
							running = false;
						}
						
						break;
						// The execution of this state is finished
									
					case 2: 
						// State 2 has one valid transition.
						
						// Replace this with the required code
						if ((currentChar >= 'A' && currentChar <= 'Z')|| 		// Upper case
								(currentChar >= 'a' && currentChar <= 'z') ||	// Lower case
								(currentChar >= '0' && currentChar <= '9')) {	// Digit
							nextState = 3;
						}
						
						// If it is none of those characters, the FSM halts
						else {
							running = false;
						}
						
						// The execution of this state is finished
						break;
			
					case 3:
						// State 3 has three valid transition.
						
						// Replace this with the required code
						if ((currentChar >= 'A' && currentChar <= 'Z')|| 		// Upper case
								(currentChar >= 'a' && currentChar <= 'z') ||	// Lower case
								(currentChar >= '0' && currentChar <= '9')) {	// Digit
							nextState = 3;
						}
						
						// If the next character is a period, the FSM transitions to state 2
						else if (currentChar == '.') {
							nextState = 2;
						}
						
						// If the next character is a hyphen, the FSM transitions to state 4
						else if (currentChar == '-') {
							nextState = 4;
						}
						
						// If it is none of those characters, the FSM halts
						else {
							running = false;
						}
						
						// The execution of this state is finished
						break;

					case 4: 
						// State 4 has one valid transition.

						// Replace this with the required code
						if ((currentChar >= 'A' && currentChar <= 'Z')|| 		// Upper case
								(currentChar >= 'a' && currentChar <= 'z') ||	// Lower case
								(currentChar >= '0' && currentChar <= '9')) {	// Digit
							nextState = 3;
						}
						
						// If it is none of those characters, the FSM halts
						else {
							running = false;
						}
						// The execution of this state is finished
						break;

					}
					
					if (running) {
						//displayDebuggingInfo();
						// When the processing of a state has finished, the FSM proceeds to the next character
						// in the input and if there is one, it fetches that character and updates the 
						// currentChar.  If there is no next character the currentChar is set to a blank.
						
						moveToNextCharacter();
						
						// Move to the next state
						state = nextState;
						nextState = -1;
					}
					// Should the FSM get here, the loop starts again

				}
				// displayDebuggingInfo();
				
				System.out.println("The loop has ended.");

				emailAddressIndexofError = currentCharNdx;		// Copy the index of the current character;
				
				// When the FSM halts, we must determine if the situation is an error or not.  That depends
				// of the current state of the FSM and whether or not the whole string has been consumed.
				// This switch directs the execution to separate code for each of the FSM states and that
				// makes it possible for this code to display a very specific error message to improve the
				// user experience.
				switch (state) {
				case 0:
					// State 0 is not a final state, so we can return a very specific error message
					emailAddressIndexofError = currentCharNdx;		// Copy the index of the current character;
					emailAddressErrorMessage = "May only be alphanumberic.\n";
					return true;

				case 1:
					// State 1 is not a final state, so we can return a very specific error message

					// Replace this with the required code
					emailAddressIndexofError = currentCharNdx;
					emailAddressErrorMessage = "May only use alphanumeric, an '@' symbol, or a period.\n";
					return true;
					
				case 2:
					// State 2 is not a final state, so we can return a very specific error message
					
					// Replace this with the required code
					emailAddressIndexofError = currentCharNdx;
					emailAddressErrorMessage = "May only use alphanumeric.\n";
					return true;
					
				case 3:
					// State 3 is a Final State, so this is not an error if the input is empty, otherwise
					// we can return a very specific error message.

					if (currentCharNdx<emailAddress.length()) {
						// If not all of the string has been consumed, we point to the current character
						// in the input line and specify what that character must be in order to move
						// forward.
						emailAddressIndexofError = currentCharNdx;		// Copy the index of the current character;
						emailAddressErrorMessage = "This must be the end of the input.\n";
						return true;
					}
					else 
					{
						emailAddressIndexofError = -1;
						emailAddressErrorMessage = "";
						return false;
					}

				case 4:
					// State 4 is not a final state, so we can return a very specific error message. 

					// Replace this with the required code
					emailAddressIndexofError = currentCharNdx;
					emailAddressErrorMessage = "May only use alphanumeric.\n";
					return true;
				default:
					return false;
				}
	}

	/*********
	 * <p> Method: moveToNextCharacter() </p>
	 * 
	 * <p> Description: This method iterates over each character in an email address until
	 * the end of input is reached. </p>
	 */
	private static void moveToNextCharacter() {
			currentCharNdx++;
			if (currentCharNdx < inputLine.length())
						currentChar = inputLine.charAt(currentCharNdx);
			else {
				System.out.println("End of input was found!");
				currentChar = ' ';
				running = false;
			}
		}

	/**********
	 * <p> Method: performReturn() </p>
	 * 
	 * <p> Description: This method returns the user (who must be an Admin as only admins are the
	 * only users who have access to this page) to the Admin Home page. </p>
	 * 
	 */
	protected static void performReturn() {
		guiAdminHome.ViewAdminHome.displayAdminHome(ViewManageInvitations.theStage,
				ViewManageInvitations.theUser);
	}


	/**********
	 * <p> Method: performLogout() </p>
	 * 
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * 
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewManageInvitations.theStage);
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
		System.exit(0);
	}
}