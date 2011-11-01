
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;




public class TTopToolBar {
	
	final TMainHolder mainHolder;
	private CoolBar topCoolBar;
	private ToolBar coolToolBar;
	public CoolBar GetCoolBar() {
		return topCoolBar;
	}
	
	private ToolItem CreateNewToolButton(String hint, String ico, SelectionListener sl) {
		Image img = null;
		ToolItem coolToolItem = new ToolItem (coolToolBar, SWT.NONE);
		coolToolItem.setToolTipText (hint);
		try {
			img = new Image(mainHolder.getDisplay(), getClass().getResourceAsStream("icons/"+ico));
			coolToolItem.setImage(img);
		} catch (Exception e) {
			
		}		
		if(sl != null) coolToolItem.addSelectionListener(sl);
		return coolToolItem;
	}
	
	private ToolItem CreateNewSeparator() {
		ToolItem coolToolItem = new ToolItem (coolToolBar, SWT.SEPARATOR);
		//coolToolItem.setWidth(16);
		return coolToolItem;
	}
	
	public TTopToolBar (TMainHolder mc) {
		mainHolder = mc; //store main class
		
		
		
		
		topCoolBar = new CoolBar (mainHolder.getShell(), SWT.NONE);
		coolToolBar = new ToolBar (topCoolBar, SWT.FLAT);
		
		ToolItem coolToolItem = CreateNewToolButton("Nový tisk", "filenew.png", new fileNewItemListener(mainHolder));
		coolToolItem = CreateNewToolButton("Nový konvolut", "filenew2.png", new fileNewConvolutListener(mainHolder) );		
		CreateNewSeparator();
		coolToolItem = CreateNewToolButton("Otevøít", "document_open_folder.png", new fileOpenItemListener(mainHolder));
		CreateNewSeparator();
		coolToolItem = CreateNewToolButton("Uložit", "document_save.png", new fileSaveItemListener(false, mainHolder));
		coolToolItem = CreateNewToolButton("Uložit jako", "document_save_as.png", new fileSaveItemListener(true, mainHolder));
		CreateNewSeparator();
		coolToolItem = CreateNewToolButton("Zavøít aktuální", "window_close.png", new fileCloseItemListener(mainHolder));

		
		
		
		CoolItem coolItem1 = new CoolItem (topCoolBar, SWT.NONE);
		coolItem1.setControl (coolToolBar);
		Point size = coolToolBar.computeSize (SWT.DEFAULT, SWT.DEFAULT);
		coolItem1.setSize (coolItem1.computeSize (size.x, size.y));
		
		/*
		coolToolBar = new ToolBar (topCoolBar, SWT.BORDER);
		coolToolItem = new ToolItem (coolToolBar, SWT.NONE);
		coolToolItem.setText ("Item 3");
		coolToolItem = new ToolItem (coolToolBar, SWT.NONE);
		coolToolItem.setText ("Item 4");
		CoolItem coolItem2 = new CoolItem (topCoolBar, SWT.NONE);
		coolItem2.setControl (coolToolBar);
		size = coolToolBar.computeSize (SWT.DEFAULT, SWT.DEFAULT);
		coolItem2.setSize (coolItem2.computeSize (size.x, size.y));
		topCoolBar.setSize (topCoolBar.computeSize (SWT.DEFAULT, SWT.DEFAULT));
		*/		
		
		FormData data = new FormData ();
		data.left = new FormAttachment (0, 0);
		data.right = new FormAttachment (100, 0);
		topCoolBar.setLayoutData (data);

		
	}
}
