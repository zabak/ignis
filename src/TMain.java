/**
 * This source code is licensed under GPL v3
 * @author Iva Bydžovská
 */

public class TMain {

	/**
	 * @param args
	 */
	

	static TMainHolder     mH;
	static TMainMenu 			 mainMenu;
	
	public static void main(String[] args) {
		//create main class that holds all important ones
		//that needs to be address from subclasses   
		mH = new TMainHolder(); 
	
  	// create main menu
    mainMenu = new TMainMenu(mH);
  	
    //open the main window
    mH.openShell();
	
	}
}
