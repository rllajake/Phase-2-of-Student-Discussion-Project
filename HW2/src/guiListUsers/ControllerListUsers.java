package guiListUsers;

import javafx.stage.Stage;

/**
 * Controller for the List Users page.
 */
public class ControllerListUsers {

	/**
	 * Return to Admin Home.
	 */
	protected static void performReturn() {
		guiAdminHome.ViewAdminHome.displayAdminHome(ViewListUsers.theStage, ViewListUsers.theUser);
	}
}