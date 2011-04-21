/**
 * This source code is licensed under GPL v3
 * @author Iva Bydžovská
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabItem;

/**
 * Class for handling main menu
 */

public class TMainMenu {
	Menu menuBar, fileMenu, helpMenu;
	  
	MenuItem fileMenuHeader, helpMenuHeader;
	MenuItem fileExitItem, fileOpenItem, fileSaveItem, helpGetHelpItem;
	MenuItem fileSaveAsItem, fileCloseItem, fileNewItem;
	
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
    
    
    
    class fileCloseItemListener implements SelectionListener {
			public void widgetDefaultSelected(SelectionEvent e) {
				if(mainHolder.currentXmlDocument != null) {
				  mainHolder.currentXmlDocument.closeDocument();				  
				}				
			}
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);				
			}
    }
   
    //add event
    fileCloseItem.addSelectionListener(new fileCloseItemListener());
   
    class fileNewItemListener implements SelectionListener {
      public void widgetSelected(SelectionEvent event) {
      	new TXmlDocument(mainHolder, "");
	        /*
	        TXmlDocument xd = new TXmlDocument(mainHolder, selected); 
	        mainHolder.AddXmlDocument(xd); //register to holder
	        mainHolder.getTabFolder().setSelection(xd.getTabItem()); //show new tab
					*/
      }

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);				
			}
    }
    
    //add event
    fileNewItem.addSelectionListener(new fileNewItemListener());
    
    class fileOpenItemListener implements SelectionListener {
      public void widgetSelected(SelectionEvent event) {
  		  FileDialog fd = new FileDialog(mainHolder.getShell(), SWT.OPEN);
	      fd.setText("Otevøít");
	      fd.setFilterPath(mainHolder.getGlobalSettings().LastPath);//"C:/");
	      String[] filterExt = { "*.xml"};
	      fd.setFilterExtensions(filterExt);
	      String selected = fd.open();
	      if(selected != null) {
	      	for(TXmlDocument xd : mainHolder.xmlDocuments) {
	      		if(xd.getDocumentUrl().equals(selected) && !xd.getDocumentUrl().isEmpty()) {
	      			TabItem tabItem = xd.getTabItem();
	      			mainHolder.getTabFolder().setSelection(tabItem); //show new tab, no event!		
	      			mainHolder.FindSelectedXmlDocument(tabItem);     //because missing select event!
	      			return;
	      		}
	      	}
	      	new TXmlDocument(mainHolder, selected);
	      }
      }

      public void widgetDefaultSelected(SelectionEvent event) {
        widgetSelected(event);
      }
    }    
    
    //add event
    fileOpenItem.addSelectionListener(new fileOpenItemListener());
    
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
    
    class fileSaveItemListener implements SelectionListener {
    	boolean SaveAs;
    	public fileSaveItemListener(boolean isSaveAs) {
    		SaveAs = isSaveAs;
    	}
    	
      public void widgetSelected(SelectionEvent event) {
      	if(mainHolder.currentXmlDocument != null) {
      		if(SaveAs || mainHolder.currentXmlDocument.getDocumentUrl().isEmpty()) {
      		  FileDialog fd = new FileDialog(mainHolder.getShell(), SWT.SAVE);
    	      fd.setText("Uložit jako");
    	      fd.setFilterPath(mainHolder.getGlobalSettings().LastPath);//"C:/");
    	      fd.setOverwrite(true);
    	      String[] filterExt = { "*.xml"};
    	      fd.setFilterExtensions(filterExt);
    	      String selected = fd.open();
    	      if(selected != null) {      		     		
        		  mainHolder.currentXmlDocument.saveDocument(selected);
        		  mainHolder.getShell().setText(mainHolder.SHELL_TEXT + " : " + mainHolder.currentXmlDocument.getName());
    	      }      			
      		} else mainHolder.currentXmlDocument.saveDocument(null);
      	}
      }

      public void widgetDefaultSelected(SelectionEvent event) {
      	widgetSelected(event);
      }
    }
    //add event
    fileSaveItem.addSelectionListener(new fileSaveItemListener(false));
    //add event
    fileSaveAsItem.addSelectionListener(new fileSaveItemListener(true));    

    class helpGetHelpItemListener implements SelectionListener {
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				MessageBox messageBox = new MessageBox(mainHolder.getShell(), SWT.ICON_INFORMATION
            | SWT.OK);
				messageBox.setMessage("Ignis verze 0.9.3");
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
