package guiDeleteUser;

import java.sql.SQLException;

import database.Database;

public class ControllerDeleteUser {
	
	/*-********************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	/**********
	 * <p> Method: performRemoveUser() </p>
	 * 
	 * <p> Description: This method deletes the selected user from the database. </p>
	 * 
	 */
	
	/**********
	 * <p> Method: performRemoveUser() </p>
	 * 
	 * <p> Description: This method tries calling the removeUser method in Database.java on
	 * the specified username. It will print an error to the console if this fails. </p>
	 * 
	 */
	protected static void performRemoveUser() {
		String username = ViewDeleteUser.text_DeleteUser.getText();
		try {
        	// Remove the user of a given username from the database
        	theDatabase.removeUser(username);
			ViewDeleteUser.label_DeletionResult.setText("Successfully deleted user: " + username);
        	}
        catch (SQLException e) {
        	String errorMessage = "*** ERROR *** Database error trying to remove a user: " + e.getMessage();
			ViewDeleteUser.label_DeletionResult.setText(errorMessage);
            System.err.println(errorMessage);
            e.printStackTrace();
            System.exit(0);
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
		guiAdminHome.ViewAdminHome.displayAdminHome(ViewDeleteUser.theStage,
				ViewDeleteUser.theUser);
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
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewDeleteUser.theStage);
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