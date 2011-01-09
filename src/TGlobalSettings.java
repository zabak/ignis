import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;


/**
 * This source code is licensed under GPL v3
 * @author Iva Bydžovská
 */


/**
 * Class for loading and saving global settings
 */

public class TGlobalSettings {
	TMainHolder MainHolder;
	
	//class holding size of main window
  final class TWindowParams {
    public int x, y, w, h;
    public TWindowParams() {
      w = 800;
      h = 600;
      Monitor primary = MainHolder.getDisplay().getPrimaryMonitor();
      x = (primary.getBounds().width-w)/2;
      y = (primary.getBounds().height-h)/2;
    }
    public Rectangle getBounds() {
    	return new Rectangle(x,y,w,h);
    }
		public void setBounds(Rectangle bounds) {
			x = bounds.x;
			y = bounds.y;
			h = bounds.height;
			w = bounds.width;
		}
  };
  
  TWindowParams MainWindowParams;
  String 				LastPath;
  
  
  public class TSaxParser extends DefaultHandler {
  	
  	public void startElement(String namespace, String localName, String qName, Attributes atts) {
  		String value;
  		if(localName.equals("MainWindow")) {
  			try {
  			  MainWindowParams.x = Integer.parseInt(atts.getValue("Left"));
  			  MainWindowParams.y = Integer.parseInt(atts.getValue("Top"));
  			  MainWindowParams.h = Integer.parseInt(atts.getValue("Height"));
  			  MainWindowParams.w = Integer.parseInt(atts.getValue("Width"));
  			} catch (Exception e) {}
  		} else if(localName.equals("Files")) {		
  			if((value = atts.getValue("Path")) != null) {
  			  LastPath = value;
  			} 
  		}
  	}
  	
    public void ProcessWithSax(String url) {
    	SAXParserFactory spf = SAXParserFactory.newInstance();
    	try {
	      SAXParser 			 sp = spf.newSAXParser();
	      ParserAdapter 	 pa = new ParserAdapter(sp.getParser());
	      pa.setContentHandler(this);   
	      pa.parse(url);
    	} catch (java.io.FileNotFoundException e) {
    		MessageBox messageBox = new MessageBox(MainHolder.getShell(), SWT.ICON_WARNING | SWT.OK);
    		messageBox.setMessage("Konfigurace nenalezena. Byl vytvoøen nový soubor s konfigurací.");
    		messageBox.setText("Chyba naèítání konfigurace");
    		messageBox.open();
    	} catch (Exception e) {
    		MessageBox messageBox = new MessageBox(MainHolder.getShell(), SWT.ICON_WARNING | SWT.OK);
    		messageBox.setMessage("Nastala výjimka pøi naèítání konfigurace, bude vytvoøena nová.\n\nVýjimka:\n"+e.getLocalizedMessage());
    		messageBox.setText("Chyba naèítání konfigurace");
    		messageBox.open();	    		                    
    	}
    }
  }
  
  String ConfigPath;
  String NewFilePath;
 
  public TGlobalSettings(TMainHolder mh) {  	
  	MainHolder = mh;
  	//get default values
  	MainWindowParams = new TWindowParams();
  	LastPath = System.getProperty("user.home");
  	//parse config.xml
  	TSaxParser SaxParser = new TSaxParser();
  	String s = System.getProperty("file.separator");
  	ConfigPath = System.getProperty("user.dir")+s+"config.xml";
  	/*debug* /
  	MessageBox messageBox = new MessageBox(mh.getShell(), SWT.ICON_QUESTION
        | SWT.OK);
    messageBox.setMessage(ConfigPath);
    messageBox.setText("ConfigPath");
    messageBox.open();  	
  	/*debug*/  	
  	NewFilePath = System.getProperty("user.dir")+s+"new.xml";
  	SaxParser.ProcessWithSax("config.xml");
  }
  
  public void SaveSettings() {
  	StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Config>\n");
  	sb.append("\t<MainWindow Left=\"");
  	sb.append(MainWindowParams.x);
  	sb.append("\" Top=\"");
  	sb.append(MainWindowParams.y);
  	sb.append("\" Height=\"");
  	sb.append(MainWindowParams.h);
  	sb.append("\" Width=\"");
  	sb.append(MainWindowParams.w);
  	sb.append("\" />\n");
  	sb.append("\t<Files Path =\"");
  	sb.append(LastPath);		
  	sb.append("\">\n");
  	sb.append("</Files>");
  	sb.append("</Config>");
  	String output = sb.toString();
  	try {
  		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(ConfigPath),"UTF-8");
  		out.write(output);
  		out.flush();
  	} catch(IOException e) {
  		MessageBox messageBox = new MessageBox(MainHolder.getShell(), SWT.ICON_WARNING | SWT.OK);
      messageBox.setMessage("Nastala výjimka pøi ukládání konfigurace:\n"+e.getLocalizedMessage());
      messageBox.setText("Chyba ukládání konfigurace");
      messageBox.open();  		
  	}
  }
}
