package guiTools;


/*******
 * <p> Title: ValidationTest Class. </p>
 * 
 * <p> Description: A Java demonstration for semi-automated tests. It will print output to the console in order to
 * test different input validation used in the program. The types of input validation include username,
 * password, email (for managing account information) as well as input validation for creating, reading,
 * updating, and deleting a post. <br>
 * <br>
 *  In order to validate username, it must consist of 4-16 characters including alphanumeric characters and 
 *  cannot end with a special character. <br>
 *  <br>
 *  For password validation, it must contain at least 8 characters and have
 *  at least one uppercase and one lowercase letter, one digit, one special character.<br>
 *  <br>
 *  Email validation requires the input to have at least one alphanum character followed by an "@" sign that
 *  also is followed by some number of alphanum chars. The end of the email may have special characters such as
 *  "." or "-" but CANNOT end with them. <br>
 *  <br>
 *  Creating post validation requires the post title and contents to be at least 3 alphanum characters. <br>
 *  <br>
 *  Reading post validation requires the post to either not be private, the user must be a reviewer, or the user
 *  is the one who created the post. <br>
 *  <br>
 *  Updating post validation requires the user to be the same as the post author. <br>
 *  <br>
 *  Deleting post validation requires the user to be the same as the post author AND the post hasn't
 *  already been deleted.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 * 
 * @author Team 14
 * 
 * 
 */
public class ValidationTest {
	
	 /*****
     * <p> Method: ValidationTest() </p>
     * 
     * <p> Description: This default constructor is not used in this system. </p>
     */
    public ValidationTest() {
    	
    }

	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	/**********
	 * This mainline displays a header to the console, performs a sequence of
	 * test cases, and then displays a footer with a summary of the results
	 * 
	 * @param args holds command line arguments passed to the program
	 */
	public static void main(String[] args) {
		/************** Test cases semi-automation report header **************/
		System.out.println("______________________________________");
		System.out.println("\nTesting Automation");

		/************** Start of the test cases **************/

		performUsernameTestCase(1, "1Flask", false);

		performUsernameTestCase(2, "Phil", true);

		performUsernameTestCase(3, "Crash12.", false);

		performUsernameTestCase(4, "pilot.Jimmy", true);

		performUsernameTestCase(5, "", false);
		
		System.out.println("\n================ PASSWORD TESTS ================");
		
		performPasswordTestCase(6, "Aa!15678", true);      // valid
		
		performPasswordTestCase(7, "short7!", false);        // too short
		
		performPasswordTestCase(8, "NoSymbol123", false);  // missing special
		
		performPasswordTestCase(9, "valid_P@ssw0rd", true); // valid
		
		performPasswordTestCase(10, "NoDigits!!", false); // missing digit
		
		System.out.println("\n================ EMAIL TESTS ================");
		
		performEmailTestCase(11, "test@example.com", true);
		
        performEmailTestCase(12, "test@example", true);
        
        performEmailTestCase(13, "Amazing@Spid3r-Man", true);
        
        performEmailTestCase(14, "F@F.FF" + "F".repeat(250), false);
        
        performEmailTestCase(15, "", false);
        
        performEmailTestCase(16, "abc", false);
        
        performEmailTestCase(17, "whatsup@", false);
        
        performEmailTestCase(18, "whats-up@example.com", false);
        
        performEmailTestCase(19, "whatsup@example-", false);
		
        System.out.println("\n================ POST TESTS ================");
        
        //                            title body valid
        performCreatePostTestCase(20, "",   "",  false);
        
        performCreatePostTestCase(21, "abc", "", false);
        
        performCreatePostTestCase(22, "a", "bac", false);
        
        performCreatePostTestCase(23, "abc", "bac", true);
        
        //                          private reviewer creator valid
        performReadPostTestCase(24, false,  false,   false,  true);
        
        performReadPostTestCase(25, true,   false,   false,  false);
        
        performReadPostTestCase(26, true,   false,   true,   true);
        
        performReadPostTestCase(27, true,   true,    false,  true);
        
        //                            creator valid
        performUpdatePostTestCase(28, true,   true);
        
        performUpdatePostTestCase(29, false, false);
        
        //                            creator deleted valid
        performDeletePostTestCase(30, true,   false,  true);
        
        performDeletePostTestCase(31, true,   true,   false);
        
        performDeletePostTestCase(32, false,  false,  false);

		/************** End of the test cases **************/

		/************** Test cases semi-automation report footer **************/
		System.out.println("____________________________________________________________________________");
		System.out.println();
		System.out.println("Number of tests passed: "+ numPassed);
		System.out.println("Number of tests failed: "+ numFailed);
	}

	/**
	 * This method sets up the input value for the test from the input parameters,
	 * displays test execution information, invokes precisely the same recognizer
	 * that the interactive JavaFX mainline uses, interprets the returned value,
	 * and displays the interpreted result. This method in particular is used to test for a valid username.
	 * 
	 * @param testCase is the test case number that gets displayed in the console output
	 * 
	 * @param inputText specifies the input string we are testing is a valid username
	 * 
	 * @param expectedPass specifies if we expect the input to be a valid username
	 */
	public static void performUsernameTestCase(int testCase, String inputText, boolean expectedPass) {

		/************** Display an individual test case header **************/
		System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + inputText + "\"");
		System.out.println("______________");
		System.out.println("\nFinite state machine execution trace:");

		/************** Call the recognizer to process the input **************/
		String resultText= usernameValidator(inputText);

		/************** Interpret the result and display that interpreted information **************/
		System.out.println();

		// If the resulting text is empty, the recognizer accepted the input
		if (resultText != "") {
			 // If the test case expected the test to pass then this is a failure
			if (expectedPass) {
				System.out.println("***Failure*** The username <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			}
			// If the test case expected the test to fail then this is a success
			else {			
				System.out.println("***Success*** The username <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			}
		}

		// If the resulting text is empty, the recognizer accepted the input
		else {	
			// If the test case expected the test to pass then this is a success
			if (expectedPass) {	
				System.out.println("***Success*** The username <" + inputText + 
						"> is valid, so this is a pass!");
				numPassed++;
			}
			// If the test case expected the test to fail then this is a failure
			else {
				System.out.println("***Failure*** The username <" + inputText + 
						"> was judged as valid" + 
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
	}
	
	/**
	 * This method sets up the input value for the test from the input parameters,
	 * displays test execution information, invokes precisely the same recognizer
	 * that the interactive JavaFX mainline uses, interprets the returned value,
	 * and displays the interpreted result. This method in particular is used to test for a valid email.
	 * 
	 * @param testCase is the test case number that gets displayed in the console output
	 * 
	 * @param inputText specifies the input string we are testing is a valid password
	 * 
	 * @param expectedPass specifies if we expect the input to be a valid password
	 */
	public static void performPasswordTestCase(int testCase, String inputText, boolean expectedPass) {
	    System.out.println("__\n\nTest case: " + testCase);
	    System.out.println("Input: \"" + inputText + "\"");
	    System.out.println("__");
	    System.out.println("\nPassword validation execution trace:");

	    String resultText = passwordValidator(inputText);
	    boolean passed = resultText.isEmpty();

	    System.out.println();
	    if (passed && expectedPass) {
	        System.out.println("Success The password <" + inputText + "> is valid.");
	        numPassed++;
	    } else if (passed) {
	        System.out.println("Failure The password <" + inputText + "> was judged valid, but it was supposed to be invalid!");
	        numFailed++;
	    } else if (expectedPass) {
	        System.out.println("Failure The password <" + inputText + "> is invalid, but it was supposed to be valid!");
	        System.out.println("Error message: " + resultText);
	        numFailed++;
	    } else {
	        System.out.println("Success The password <" + inputText + "> is invalid (as expected).");
	        System.out.println("Error message: " + resultText);
	        numPassed++;
	    }
	}
	
	/**
	 * This method sets up the input value for the test from the input parameters,
	 * displays test execution information, invokes precisely the same recognizer
	 * that the interactive JavaFX mainline uses, interprets the returned value,
	 * and displays the interpreted result. This method in particular is used to test for a valid email.
	 * 
	 * @param testCase is the test case number that gets displayed in the console output
	 * 
	 * @param inputText specifies the input string we are testing is a valid email
	 * 
	 * @param expectedPass specifies if we expect the email to be a valid one
	 */
	public static void performEmailTestCase(int testCase, String inputText, boolean expectedPass) {
		/************** Display an individual test case header **************/
		System.out.println("______________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + inputText + "\"");
		System.out.println("_______");
		System.out.println("\nFinite state machine execution trace:");
		
		/************** Call the recognizer to process the input **************/
		String resultText = emailValidator(inputText);
		
		/************** Interpret the result and display that interpreted information **************/
		System.out.println();
		
		// If the resulting text is empty, the recognizer accepted the input
		if (resultText != "") {
			 // If the test case expected the test to pass then this is a failure
			if (expectedPass) {
				System.out.println("***Failure*** The email address <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			}
			// If the test case expected the test to fail then this is a success
			else {			
				System.out.println("***Success*** The email address <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			}
		}
		
		// If the resulting text is empty, the recognizer accepted the input
		else {	
			// If the test case expected the test to pass then this is a success
			if (expectedPass) {	
				System.out.println("***Success*** The email address <" + inputText + 
						"> is valid, so this is a pass!");
				numPassed++;
			}
			// If the test case expected the test to fail then this is a failure
			else {
				System.out.println("***Failure*** The email address <" + inputText + 
						"> was judged as valid" + 
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
	}

	// Helper method for validating usernames
	protected static String usernameValidator(String username) {
		// check if the size of the string is 0. Return if so
		if (username.length() < 1) {
			return "Error: String of length 0";
		}

		// local variables for username FSM simulation
				int state = 0;
				String inputline = username;
				int currentCharIndex = 0;
				char currentChar = username.charAt(0);
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
					return "Error: Forbidden character at state 0";

				case 1:
					// State 1 is a final state.  Check to see if the UserName length is valid.  If so we
					// we must ensure the whole string has been consumed.

					if (usernameSize < 4) {
						// UserName is too small
						return "Error: Username too small";
					}
					else if (usernameSize > 16) {
						// UserName is too long
						return "Error: username too long";
					} 
					// Incorporate 3rd else condition for when the first character of the input is an integer
					else if(Character.isDigit(username.charAt(0))) {
						return "Error: Username starts with integer";
					}
					else if (currentCharIndex < username.length()) {
						// There are characters remaining in the input, so the input is not valid
						return "Error: Characters remaining in the input";
					}

					validUser = true;
					return "";

				case 2:
					// State 2 is not a final state, so we can return a very specific error message
					// display corresponding error for when the special character conditions are not met
					return "Error: Special character conditions not met";

				default:
					// This is for the case where we have a state that is outside of the valid range.
					// This should not happen
					return "Error: Default";
				}
	}
	
	// Password rules from the provided Password Evaluator (FSM):
		// - must contain: at least one UPPERCASE, one lowercase, one digit, one special
		// - specials allowed exactly: ~`!@#$%^&*()_-+={}[]|\:;\"'<>,.?/
		// - length >= 8
		// - only letters, digits, and the specials above are permitted (else immediate error)
		protected static String passwordValidator(String password) {
		    if (password == null) password = "";

		    boolean foundUpperCase = false;
		    boolean foundLowerCase = false;
		    boolean foundNumericDigit = false;
		    boolean foundSpecialChar = false;
		    boolean foundLongEnough = false;

		    final String SPECIALS = "~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/";

		    // simple trace (optional)
		    System.out.println("Scanning password characters…");

		    for (int i = 0; i < password.length(); i++) {
		        char ch = password.charAt(i);

		        if (ch >= 'A' && ch <= 'Z') {
		            if (!foundUpperCase) System.out.println("Upper case letter found");
		            foundUpperCase = true;
		        } else if (ch >= 'a' && ch <= 'z') {
		            if (!foundLowerCase) System.out.println("Lower case letter found");
		            foundLowerCase = true;
		        } else if (ch >= '0' && ch <= '9') {
		            if (!foundNumericDigit) System.out.println("Digit found");
		            foundNumericDigit = true;
		        } else if (SPECIALS.indexOf(ch) >= 0) {
		            if (!foundSpecialChar) System.out.println("Special character found");
		            foundSpecialChar = true;
		        } else {
		            // exact wording from the evaluator
		            return "*** Error *** An invalid character has been found! "
		                 + "Allowed: letters, digits, and these specials " + SPECIALS;
		        }

		        // “Long enough” flips true once the 8th character is reached (index 7)
		        if (i >= 7 && !foundLongEnough) {
		            System.out.println("At least 8 characters found");
		            foundLongEnough = true;
		        }
		    }

		    // Aggregate missing conditions exactly like the evaluator
		    String errMessage = "";
		    if (!foundUpperCase)   errMessage += "Upper case; ";
		    if (!foundLowerCase)   errMessage += "Lower case; ";
		    if (!foundNumericDigit) errMessage += "Numeric digit; ";
		    if (!foundSpecialChar) errMessage += "Special character; ";
		    if (!foundLongEnough)  errMessage += "Long Enough; ";

		    if (errMessage.isEmpty()) return "";
		    return errMessage + "conditions were not satisfied";
		}
		
		/**
		 *  The error message text 
		 */
		public static String emailAddressErrorMessage = "";
		
		/** 
		 * The input being processed 
		 */
	    public static String emailAddressInput = "";     
	    
	    /**
	     * The index where the error was located 
	     */
	    public static int emailAddressIndexofError = -1;    
	    
	    private static int state = 0;                        // The current state value
	    private static int nextState = 0;                    // The next state value
	    private static boolean finalState = false;            // Is this state a final state?
	    private static String inputLine = "";                // The input line
	    private static char currentChar;                    // The current character in the line
	    private static int currentCharNdx;                    // The index of the current character
	    private static boolean running;                        // The flag that specifies if the FSM is 
	                                                        // running
	    
	    /**********
		 * This method is a mechanical transformation of a Finite State Machine diagram into a Java
		 * method.
		 * 
		 * @param email		The input string for the Finite State Machine
		 * @return			An output string that is empty if every things is okay or it will be
		 * 						a string with a help description of the error follow by two lines
		 * 						that shows the input line follow by a line with an up arrow at the
		 *						point where the error was found.
		 */
		public static String emailValidator(String email) {
			// The following are the local variable used to perform the Finite State Machine simulation
			state = 0;							// This is the FSM state number
			inputLine = email;					// Save the reference to the input line as a global
			currentCharNdx = 0;					// The index of the current character


			// The Finite State Machines continues until the end of the input is reached or at some 
			// state the current character does not match any valid transition to a next state

			emailAddressInput = email;			// Save a copy of the input

			// Let's ensure there is input
			if (email.length() <= 0) {
				emailAddressErrorMessage = "There was no email address found.\n";
				return emailAddressErrorMessage + displayInput(email, 0);
			}
			currentChar = email.charAt(0);		// The current character from the above indexed position

			// Let's ensure the address is not too long
			if (email.length() > 255) {
				emailAddressErrorMessage = "A valid email address must be no more than 255 characters.\n";
				return emailAddressErrorMessage + displayInput(email, 255);
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

					// The current character is must be checked against 62 options. If any are matched
					// the FSM must go to state 1
					// The first and the second check for an alphabet character the third a numeric
					if ((currentChar >= 'A' && currentChar <= 'Z')|| 		// Upper case
							(currentChar >= 'a' && currentChar <= 'z') ||	// Lower case
							(currentChar >= '0' && currentChar <= '9')) {	// Digit
						nextState = 1;
					}
					
					else if (currentChar == '.'){ 
						nextState = 0;
					}
					
					else if (currentChar == '@'){ 
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
					
					// The current character is must be checked against 62 options. If any are matched
					// the FSM must go to state 1
					// The first and the second check for an alphabet character the third a numeric
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
					
					if ((currentChar >= 'A' && currentChar <= 'Z')|| 		// Upper case
							(currentChar >= 'a' && currentChar <= 'z') ||	// Lower case
							(currentChar >= '0' && currentChar <= '9')) {	// Digit
						nextState = 3;
					}
					
					else if (currentChar == '.'){ 
						nextState = 2;
					}
					
					else if (currentChar == '-'){ 
						nextState = 4;
					}
					
					else { 
						running = false;
					}

					// The execution of this state is finished
					break;

				case 4: 
					// State 4 has one valid transition.

					if ((currentChar >= 'A' && currentChar <= 'Z')|| 		// Upper case
							(currentChar >= 'a' && currentChar <= 'z') ||	// Lower case
							(currentChar >= '0' && currentChar <= '9')) {	// Digit
						nextState = 3;
					}
					
					else { 
						running = false;
					}

					// The execution of this state is finished
					break;

				}
				
				if (running) {
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
				return emailAddressErrorMessage;

			case 1:
				// State 1 is not a final state, so we can return a very specific error message
				
				return "You didn't put an @ sign!";

			case 2:
				// State 2 is not a final state, so we can return a very specific error message
							
				return "Need another alphanum char after @!";

			case 3:
				// State 3 is a Final State, so this is not an error if the input is empty, otherwise
				// we can return a very specific error message.

				if (currentCharNdx<email.length()) {
					// If not all of the string has been consumed, we point to the current character
					// in the input line and specify what that character must be in order to move
					// forward.
					emailAddressIndexofError = currentCharNdx;		// Copy the index of the current character;
					emailAddressErrorMessage = "This must be the end of the input.\n";
					return emailAddressErrorMessage + displayInput(email, currentCharNdx);
				}
				else 
				{
					emailAddressIndexofError = -1;
					emailAddressErrorMessage = "";
					return emailAddressErrorMessage;
				}

			case 4:
				// State 4 is not a final state, so we can return a very specific error message. 

				return "You need something after the dash!";

			default:
				return "";
			}
		}

		/**********
		 * This private method display the input line and then on a line under it displays an up arrow
		 * at the point where an error should one be detected.  This method is designed to be used to 
		 * display the error message on the console terminal.
		 * 
		 * @param input				The input string
		 * @param currentCharNdx	The location where an error was found
		 * @return					Two lines, the entire input line followed by a line with an up arrow
		 */
		private static String displayInput(String input, int currentCharNdx) {
			// Display the entire input line
			String result = input.substring(0,currentCharNdx) + "?\n";

			return result;
		}
		
		
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
		
		
		/*******
		 * <p> Method: void performCreatePostTestCase(int testCase, String postTitle, String postContents,
		 * boolean expectedPass) </p>
		 * 
		 * <p> Description: Runs test case for if the user can create a particular post, they should only be able
		 * to create the post if the post title and body have at least 3 alphanumeric characters.</p>
		 * 
		 * @param testCase is the test case number that gets displayed in the console output
		 * 
		 * @param postTitle specifies what the title of the post will be
		 * 
		 * @param postContents specifies what the contents of the post will be
		 * 
		 * @param expectedPass specifies if we expect the new post to be a valid one
		 *  
		 */
		// CreatePost Test Case Method
		public static void performCreatePostTestCase(int testCase, String postTitle, String postContents, boolean expectedPass) {
			/************** Display an individual test case header **************/
			System.out.println("______________________________________\n\nTest case: " + testCase);
			System.out.printf("Input: post with title <%s> and contents <%s>\n", postTitle, postContents);
			

			int titleAlnum = postTitle.replaceAll("[^A-Za-z0-9]", "").length();
	        int contentsAlnum = postContents.replaceAll("[^A-Za-z0-9]", "").length();
	        // If the post Title or Contents is empty or less than 3 alphanum characters, the recognizer rejects the input
			if (postTitle.isBlank() || postContents.isBlank() || titleAlnum < 3 || contentsAlnum < 3) {
				// If the test case expected the test to pass then this is a failure
				if (expectedPass) {
					System.out.printf("***Failure*** The post with title <%s> and contents <%s> is invalid."
							+ "\nBut it was supposed to be valid, so this is a failure!\n", postTitle, postContents);
					numFailed++;
				}
				// If the test case expected the test to fail then this is a success
				else {
					System.out.printf("***Success*** The post with title <%s> and contents <%s> is invalid."
							+ "\nBut it was supposed to be invalid, so this is a pass!\n", postTitle, postContents);
					numPassed++;
				}
				
			} else {
				// If the test case expected the test to pass then this is a success
				if (expectedPass) {
					System.out.printf("***Success*** The post with title <%s> and contents <%s> is valid, so this is a pass!\n", postTitle, postContents);
					numPassed++;
				}
				// If the test case expected the test to fail then this is a failure
				else {
					System.out.printf("***Failure*** The post with title <%s> and contents <%s> is valid."
							+ "\nBut it was supposed to be invalid, so this is a failure!\n", postTitle, postContents);
					numFailed++;
				}
			}
		}
		
		
		/*******
		 * <p> Method: void performReadPostTestCase(int testCase, boolean privatePost, boolean userIsReviewer,
		 * boolean userIsPostAuthor, boolean expectedPass) </p>
		 * 
		 * <p> Description: Runs test case for if the user can read a particular post, they should only be able
		 * to read the post if they are either the post author, a reviewer, or if the post isn't private.</p>
		 * 
		 * @param testCase is the test case number that gets displayed in the console output
		 * 
		 * @param privatePost specifies if post is private or not
		 * 
		 * @param userIsReviewer specifies if user has the role of reviewer
		 * 
		 * @param userIsPostAuthor specifies if the user actually created the post
		 * 
		 * @param expectedPass specifies if we expect the post to be readable by the user
		 *  
		 */
		// ReadPost Test Case Method
		public static void performReadPostTestCase(int testCase, boolean privatePost, boolean userIsReviewer, boolean userIsPostAuthor, boolean expectedPass) {
			/************** Display an individual test case header **************/
			System.out.println("______________________________________\n\nTest case: " + testCase);
			System.out.printf("Input: privatePost %b, userIsReviewer %b, userIsPostAuthor %b\n", privatePost, userIsReviewer, userIsPostAuthor);
			
			// If the post is not private or the user is a reviewer or the post author, they should be able to read the post
			if (!privatePost || userIsReviewer || userIsPostAuthor) {
				// If the test case expected the test to pass then this is a success
				if (expectedPass) {
					System.out.printf("***Success*** The post with details privatePost %b, userIsReviewer %b,"
							+ " userIsPostAuthor %b can be read, so this is a pass!\n", privatePost, userIsReviewer, userIsPostAuthor);
					numPassed++;
				}
				// If the test case expected the test to fail then this is a failure
				else {
					System.out.printf("***Failure*** The post with details privatePost %b, userIsReviewer %b,"
							+ " userIsPostAuthor %b can be read,"
							+ "\nBut it wasn't supposed to be readable, so this is a failure!\n", privatePost, userIsReviewer, userIsPostAuthor);
					numFailed++;
				}
				
			} else {
				// If the test case expected the test to pass then this is a failure
				if (expectedPass) {
					System.out.printf("***Failure*** The post with details privatePost %b, userIsReviewer %b,"
							+ " userIsPostAuthor %b cannot be read,"
							+ "\nBut it was supposed to be readable, so this is a failure!\n", privatePost, userIsReviewer, userIsPostAuthor);
					numFailed++;
				}
				// If the test case expected the test to fail then this is a success
				else {
					System.out.printf("***Success*** The post with details privatePost %b, userIsReviewer %b,"
							+ " userIsPostAuthor %b cannot be read,"
							+ "\nBut it wasn't supposed to be readable, so this is a pass!\n", privatePost, userIsReviewer, userIsPostAuthor);
					numPassed++;
				}
			}
		}
		
		
		/*******
		 * <p> Method: void performUpdatePostTestCase(int testCase, boolean userIsPostAuthor, boolean expectedPass) </p>
		 * 
		 * <p> Description: Runs test case for if the user should be able to update a post given
		 * whether they are the post author.</p>
		 * 
		 * @param testCase is the test case number that gets displayed in the console output
		 * 
		 * @param userIsPostAuthor specifies if the user actually created the post
		 * 
		 * @param expectedPass specifies if we expect the post to be updatable by the user
		 *  
		 */
		// UpdatePost Test Case Method
		public static void performUpdatePostTestCase(int testCase, boolean userIsPostAuthor, boolean expectedPass) {
			/************** Display an individual test case header **************/
			System.out.println("______________________________________\n\nTest case: " + testCase);
			System.out.printf("Input: userIsPostAuthor %b\n", userIsPostAuthor);
			
			// If the user is the author of the post they should be able to update it
			if (userIsPostAuthor) {
				// If the test case expected the test to pass then this is a success
				if (expectedPass) {
					System.out.println("***Success*** The post can be updated, so this is a pass!");
					numPassed++;
				}
				// If the test case expected the test to fail then this is a failure
				else {
					System.out.println("***Failure*** The post can be updated,"
							+ "\nBut it wasn't supposed to be updatable, so this is a failure!");
					numFailed++;
				}
				
			} else {
				// If the test case expected the test to pass then this is a failure
				if (expectedPass) {
					System.out.println("***Failure*** The post cannot be updated,"
							+ "\nBut it was supposed to be updatable, so this is a failure!");
					numFailed++;
				}
				// If the test case expected the test to fail then this is a success
				else {
					System.out.println("***Success*** The post cannot be updated,"
							+ "\nBut it wasn't supposed to be updatable, so this is a pass!");
					numPassed++;
				}
			}
		}
		
		
		/*******
		 * <p> Method: void performDeletePostTestCase(int testCase, boolean userIsPostAuthor, boolean postAlreadyDeleted, boolean expectedPass) </p>
		 * 
		 * <p> Description: Runs test case for if the user should be able to delete a post given
		 * whether they are the post author and if the post hasn't already been deleted.</p>
		 * 
		 * @param testCase is the test case number that gets displayed in the console output
		 * 
		 * @param userIsPostAuthor specifies if the user actually created the post
		 * 
		 * @param postAlreadyDeleted specifies if the post has already been deleted before
		 * 
		 * @param expectedPass specifies if we expect the post to be deletable by the user
		 *  
		 */
		// DeletePost Test Case Method
		public static void performDeletePostTestCase(int testCase, boolean userIsPostAuthor, boolean postAlreadyDeleted, boolean expectedPass) {
			/************** Display an individual test case header **************/
			System.out.println("______________________________________\n\nTest case: " + testCase);
			System.out.printf("Input: userIsPostAuthor %b, postAlreadyDeleted %b\n", userIsPostAuthor, postAlreadyDeleted);
			
			// If the user is the author of the post and it's not already deleted they should be able to delete it
			if (userIsPostAuthor && !postAlreadyDeleted) {
				// If the test case expected the test to pass then this is a success
				if (expectedPass) {
					System.out.println("***Success*** The post can be deleted, so this is a pass!");
					numPassed++;
				}
				// If the test case expected the test to fail then this is a failure
				else {
					System.out.println("***Failure*** The post can be deleted,"
							+ "\nBut it wasn't supposed to be deletable, so this is a failure!");
					numFailed++;
				}
				
			} else {
				// If the test case expected the test to pass then this is a failure
				if (expectedPass) {
					System.out.println("***Failure*** The post cannot be deleted,"
							+ "\nBut it was supposed to be deletable, so this is a failure!");
					numFailed++;
				}
				// If the test case expected the test to fail then this is a success
				else {
					System.out.println("***Success*** The post cannot be deleted,"
							+ "\nBut it wasn't supposed to be deletable, so this is a pass!");
					numPassed++;
				}
			}
		}		
}
