package guiReviewer;

public class ControllerReviewerHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	**********************************************************************************************/
	
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewReviewerHome.theStage, ViewReviewerHome.theUser, false);
	}	

	
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewReviewerHome.theStage);
	}
	
	protected static void performQuit() {
		System.exit(0);
	}

}
