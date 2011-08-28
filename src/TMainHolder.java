/**
 * This source code is licensed under GPL v3
 * @author Iva Bydžovská
 */

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/* main key classes */
class TMainHolder {
	private Display display;
	private Shell shell;
	private TGlobalSettings globalSettings;
	private TabFolder tabFolder;
	private TTopToolBar topToolBar;

	public ArrayList<TXmlDocument> xmlDocuments;
	public TXmlDocument currentXmlDocument;
	public DocumentBuilderFactory dbFactory;
	public DocumentBuilder documentBuilder;

	String SHELL_TEXT = "Ignis * - XML record editor";

	TMainHolder() {
		display = new Display();

		display.addFilter(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (currentXmlDocument != null) {
					Point scOrigin = currentXmlDocument.scrolledComposite.getOrigin();
					scOrigin.y -= 16 * event.count;
					currentXmlDocument.scrolledComposite.setOrigin(scOrigin);
				}
				event.doit = false;
			}
		});

		shell = new Shell(display);
		globalSettings = new TGlobalSettings(this);
		xmlDocuments = new ArrayList<TXmlDocument>();
		dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setValidating(false);
		// dbFactory.setExpandEntityReferences(false);
		try {
			documentBuilder = dbFactory.newDocumentBuilder();
			// throw away any DTD, we don't need it
			documentBuilder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId)
						throws SAXException, IOException {
					if (systemId.contains(".dtd")) {
						return new InputSource(new StringReader(""));
					} else {
						return null;
					}
				}
			});
		} catch (ParserConfigurationException e) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setMessage("Nastala výjimka pøi inicializaci XML parseru:\n"
					+ e.getLocalizedMessage());
			messageBox.setText("Kritická chyba");
			messageBox.open();
		}

		//shell = new Shell(display);
		/*
		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL);
		fillLayout.marginHeight = 4;
		fillLayout.marginWidth = 4;
		shell.setLayout(fillLayout); // set layout to fill whole window
		*/
		
		
		topToolBar = new TTopToolBar(this);
		//form layout for coolbar
		FormLayout shellLayout = new FormLayout ();
		shell.setLayout (shellLayout);
		
		
		shell.setText(SHELL_TEXT); // set title
	  
		tabFolder = new TabFolder(shell, SWT.TOP);		
		tabFolder.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));

		FormData data = new FormData ();
		data.left = new FormAttachment (0, 0);
		data.right = new FormAttachment (100, 0);
		data.top = new FormAttachment (topToolBar.GetCoolBar(), 0, SWT.DEFAULT);
		data.bottom = new FormAttachment (100, 0);		
		tabFolder.setLayoutData(data);
		
		tabFolder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FindSelectedXmlDocument((TabItem) e.item);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		/* 
		 * * / TabItem ti = new TabItem(tabFolder, 0); ti.setText("pokus");
		 * ti.dispose(); /* new TabItem(tabFolder, 0).setText("pokus 2"); new
		 * TabItem(tabFolder, 0).setText("pokus 3");
		 */
	}

	// access functions
	public Shell getShell() {
		return shell;
	}

	public Display getDisplay() {
		return display;
	}

	public TGlobalSettings getGlobalSettings() {
		return globalSettings;
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public void AddXmlDocument(TXmlDocument xd) {
		xmlDocuments.add(xd);
	}

	public void RemoveXmlDocument(TXmlDocument xd) {
		xmlDocuments.remove(xd);
		if (xmlDocuments.isEmpty()) {
			FindSelectedXmlDocument(null); // because missing select event!
		}
	}

	public TXmlDocument FindSelectedXmlDocument(TabItem ti) {
		currentXmlDocument = null;
		for (TXmlDocument xd : xmlDocuments) {
			if (xd.getTabItem() == ti) {
				currentXmlDocument = xd;
			}
		}
		// this is for checking if everything is ok
		if (currentXmlDocument == null)
			shell.setText(SHELL_TEXT);
		else
			shell.setText(SHELL_TEXT + " : " + currentXmlDocument.getName());

		return currentXmlDocument;
	}

	public void openShell() {
		// open main window
		shell.open();
		shell.setBounds(globalSettings.MainWindowParams.getBounds());
		// shell.setMaximized(true);
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				globalSettings.MainWindowParams.setBounds(shell.getBounds());
				globalSettings.SaveSettings();
			}
		});

		// wait for events
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}
}

class fileNewItemListener implements SelectionListener {
	private final TMainHolder mainHolder;

	public fileNewItemListener(TMainHolder mH) {
		mainHolder = mH;
	}
	
  public void widgetSelected(SelectionEvent event) {
		new TXmlDocument(mainHolder, "");
  }

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		widgetSelected(event);				
	}					 
}

class fileNewConvolutListener implements SelectionListener {
	private final TMainHolder mainHolder;

	public fileNewConvolutListener(TMainHolder mH) {
		mainHolder = mH;
	}	
	
  public void widgetSelected(SelectionEvent event) {
  	//TODO: implement new convolut creation
  	new TXmlDocument(mainHolder, "");
  }

  public void widgetDefaultSelected(SelectionEvent event) {
    widgetSelected(event);
  }
}  


class fileCloseItemListener implements SelectionListener {
	private final TMainHolder mainHolder;

	public fileCloseItemListener(TMainHolder mH) {
		mainHolder = mH;
	}		
	
	public void widgetDefaultSelected(SelectionEvent e) {
		if(mainHolder.currentXmlDocument != null) {
		  mainHolder.currentXmlDocument.closeDocument();				  
		}				
	}
	public void widgetSelected(SelectionEvent e) {
		widgetDefaultSelected(e);				
	}
}

class fileOpenItemListener implements SelectionListener {
	private final TMainHolder mainHolder;
	
	public fileOpenItemListener(TMainHolder mH) {
		mainHolder = mH;
	}
	
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

class fileSaveItemListener implements SelectionListener {
	boolean SaveAs;
  private final TMainHolder mainHolder;
	
	public fileSaveItemListener(boolean isSaveAs, TMainHolder mH) {
		mainHolder = mH; 
		SaveAs = isSaveAs;
	}
	
  public void widgetSelected(SelectionEvent event) {
  	if(mainHolder.currentXmlDocument != null) {
  		if(SaveAs || mainHolder.currentXmlDocument.getDocumentUrl().isEmpty()) {
  			mainHolder.currentXmlDocument.saveDocumentAs();       		        			
  		} else mainHolder.currentXmlDocument.saveDocument(null);
  	}
  }

  public void widgetDefaultSelected(SelectionEvent event) {
  	widgetSelected(event);
  }
}