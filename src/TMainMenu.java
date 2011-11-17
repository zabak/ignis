/**
 * This source code is licensed under GPL v3
 * @author Iva Bydžovská
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Class for handling main menu
 */

public class TMainMenu {
	Menu menuBar, fileMenu, helpMenu;
	  
	MenuItem fileMenuHeader, helpMenuHeader;
	MenuItem fileExitItem, fileOpenItem, fileSaveItem, helpGetHelpItem;
	MenuItem fileSaveAsItem, fileCloseItem, fileNewItem, fileNewConvolut;
	
	final TMainHolder mainHolder;

	public TMainMenu(TMainHolder mc) {
		mainHolder = mc; //store main class
		//create menu bar
		menuBar 			 = new Menu(mc.getShell(), SWT.BAR);
		//create file menu
	  fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
	  fileMenuHeader.setText("&Soubor");
	  fileMenu = new Menu(mc.getShell(), SWT.DROP_DOWN);
    fileMenuHeader.setMenu(fileMenu);
    fileNewItem = new MenuItem(fileMenu, SWT.PUSH);
    fileNewItem.setText("&Nový\tCtrl+N");
    fileNewItem.setAccelerator(SWT.CTRL | 'N');
    fileNewConvolut = new MenuItem(fileMenu, SWT.PUSH);
    fileNewConvolut.setText("&Nový konvolut\tCtrl+Shift+N");
    fileNewConvolut.setAccelerator(SWT.CTRL | SWT.SHIFT | 'N');
    //fileNewConvolut.setEnabled(false);
    fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
    fileOpenItem.setText("&Otevøít ...\tCtrl+O");
    fileOpenItem.setAccelerator(SWT.CTRL | 'O');
    fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveItem.setText("&Uložit ...\tCtrl+S");
    fileSaveItem.setAccelerator(SWT.CTRL | 'S');
    fileSaveAsItem = new MenuItem(fileMenu, SWT.PUSH);
    fileSaveAsItem.setText("&Uložit jako ...\tCtrl+Shift+S");
		fileSaveAsItem.setAccelerator(SWT.CTRL | SWT.SHIFT | 'S');
		fileCloseItem = new MenuItem(fileMenu, SWT.PUSH);
    fileCloseItem.setText("&Zavøít soubor\tCtrl+F3");
		fileCloseItem.setAccelerator(SWT.CTRL | SWT.F3);
		
    new MenuItem(fileMenu, SWT.SEPARATOR);
    
    fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
    fileExitItem.setText("&Zavøít");
    
    //menu for operation with help
    helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    helpMenuHeader.setText("&Nápovìda");
    helpMenu = new Menu(mc.getShell(), SWT.DROP_DOWN);
    helpMenuHeader.setMenu(helpMenu);
    helpGetHelpItem = new MenuItem(helpMenu, SWT.PUSH);
    helpGetHelpItem.setText("&O aplikaci");
    //set menu
    mc.getShell().setMenuBar(menuBar);     
 
    
     
    //add event
    fileCloseItem.addSelectionListener(new fileCloseItemListener(mainHolder));
    //add event
    fileNewItem.addSelectionListener(new fileNewItemListener(mainHolder));        
    //add event
    fileOpenItem.addSelectionListener(new fileOpenItemListener(mainHolder));        
    //add event
    fileNewConvolut.addSelectionListener(new fileNewConvolutListener(mainHolder));
    
    class fileExitItemListener implements Listener {
    	boolean InProgress;
			@Override	public void handleEvent(Event event) {
				if(InProgress) return;
				InProgress = true;
        MessageBox messageBox = new MessageBox(mainHolder.getShell(), SWT.ICON_QUESTION
            | SWT.YES | SWT.NO);
        messageBox.setMessage("Opravdu zavøít aplikaci?");
        messageBox.setText("Zavøení aplikace");
        int response = messageBox.open();
        if (response == SWT.YES) {
        	while(mainHolder.currentXmlDocument != null)	mainHolder.currentXmlDocument.closeDocument();   		      	        	
        	mainHolder.getShell().close();
        	mainHolder.getDisplay().dispose();
        } else {	
        	if(event.type == SWT.Close) event.doit = false;
        }
        InProgress = false;
			}
    }
    
    //add event
    Listener fileExitListener = new fileExitItemListener();
    mainHolder.getShell().addListener(SWT.Close, fileExitListener);
    fileExitItem.addListener(SWT.Selection, fileExitListener);    
    
    //add event
    fileSaveItem.addSelectionListener(new fileSaveItemListener(false, mainHolder));
    //add event
    fileSaveAsItem.addSelectionListener(new fileSaveItemListener(true, mainHolder));    

    class helpGetHelpItemListener implements SelectionListener {
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				MessageBox messageBox = new MessageBox(mainHolder.getShell(), SWT.ICON_INFORMATION
            | SWT.OK);
				messageBox.setMessage("Ignis verze 1.0.2"); //version info
				messageBox.setText("O aplikaci");
				messageBox.open();
			}
			@Override	public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);				
			}
    	
    }
    
    helpGetHelpItem.addSelectionListener(new helpGetHelpItemListener());
    
	}

}
