package guiStudent;

/*******
 * <p> Title: GUIStudentHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Student Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have been implemented.  When those buttons
 * are pressed, a GUI pops up for the function associated with the button that was
 * been implemented. 
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 *  
 */


public class ControllerStudentHome {

	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */
	
	/**********
	 * <p> 
	 * 
	 * Title: createPost() Method. </p>
	 * 
	 * <p> Description: Protected method that allows the user to create a discussion post. </p>
	 */
	protected static void createPost () {
		guiCreatePost.ViewCreatePost.displayCreatePost(ViewStudentHome.theStage, 
				ViewStudentHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: readPost() Method. </p>
	 * 
	 * <p> Description: Protected method that allows the user to read all created posts. </p>
	 */
	protected static void readPosts () {
		guiReadPosts.ViewReadPosts.displayReadPosts(ViewStudentHome.theStage, 
				ViewStudentHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: searchPosts() Method. </p>
	 * 
	 * <p> Description: Protected method that allows the user to search for any posts that have been made. </p>
	 */
	protected static void searchPosts() {
		guiSearchPosts.ViewSearchPosts.displaySearchPosts(ViewStudentHome.theStage, 
				ViewStudentHome.theUser);
	}
	

	/**********
	 * <p> 
	 * 
	 * Title: readPost() Method. </p>
	 * 
	 * <p> Description: Protected method that allows the user to read posts only created by the user. </p>
	 */

	protected static void readMyPosts() {
		guiMyPosts.ViewMyPosts.displayMyPosts(ViewStudentHome.theStage, 
				ViewStudentHome.theUser);
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
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewStudentHome.theStage);
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
