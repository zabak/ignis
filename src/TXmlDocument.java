
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This source code is licensed under GPL v3
 * @author Iva Bydžovská
 */


/**
 * Class for handling XML document
 */
public class TXmlDocument {
	private static final String SUPPORTED_DOC_TYPE = "TEI.2";
  private static final String TEI_HEADER = "teiHeader";
  private static final String AUTHOR = "author";
  private static final String DATE   = "date";
  private static final String [] RESPONSIBLE = {"Døívìjší majitel", "Nakladatel", "Knihkupec", "Tiskaø ilustrace"};
  private static final String [] TERM_TYPE = {"person_name_religion", "person_name", "geographical_name", "topic", "genre", "keyword", "date"};
  private static final String [] TERM_TYPE_TEXT = {"Osoba (náboženská)", "Osoba", "Místo", "Téma", "Žánr", "Klíèové slovo", "Datum"};
  private static final String [] LANGS_VAL = {"CZE", "GER", "LAT", "SLO", "POL"};
  private static final String [] LANGS_VAL_LONG = {"èeština", "nìmèina", "latina", "slovenština", "polština"};
  private Color [] SubItemsColors; 
	private TMainHolder MainHolder;
	private String documentUrl;
	private String documentName;
	private TabItem tabItem;
	private Document document;
	private DocumentType documentType;
	final private Composite compositeDoc;
	final public ScrolledComposite scrolledComposite;
	private GridData gridData;
	public 	Label dummyLabel;
	private boolean WasEdited;
	private TXmlMultiLineText lastEditedMultiLineText;
	private int msSubItemIndex;
	private boolean isComposite = false;
	public Node msHeading;
	private final TXmlDocument thisDocument;
	private TXmlDocument masterDocument;
	private Node myMsPart;
	public ArrayList<TXmlDocument> xmlSubDocuments;
	public TXmlText idnoXmlText;	
	
	/**
	 * Returns master node for sub-documents and null for others
	 */
	public Node GetMsPart() {
		return myMsPart;
	}
	
	/**
	 * Returns master document for sub-documents and null for others
	 */
	public TXmlDocument GetMasterDocument() {
		return masterDocument;
	}
		
	/**
	 * 
	 */
	public boolean GetIsComposite() {
		return isComposite;
	}
		
	/**
	 * After changes in form it counts new measures for scrollbars  
	 */
	public void PackAndSetExtends() {
		compositeDoc.pack(true);
		scrolledComposite.setMinSize(compositeDoc.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);		
	}
	
	/**
	 * Finds first child node of desired name
	 * 
	 * @param p		  Parent node of node to be found
	 * @param Name  Name of node to be found 
	 * @return      First child node of desired name or NULL if there is no such node 
	 */
	
	Node GetFirstChildByName(Node p, String Name) {
		NodeList nl = p.getChildNodes();
		//over all child nodes
		Node child;
		for(int i = 0; i < nl.getLength(); i++) {
			child = nl.item(i);
			if(child.getNodeName() == Name) { 
				return child; 
			}
		}	
		return null;
	}
	
  /**
   * Finds first child node by name, attribute and its value
   * 
   * @param parent   Parent node of node to be found
   * @param name     Name of node to be found
   * @param attr     Attribute of node to be found
   * @param attrVal  Attribute value (If it is null, attribute value may be anything
   * @return  			 First child node of desired parameters or NULL if there is no such node
   */
	
	public Node FindChildByNameAndAttribute(Node parent, String name, String attr, String attrVal) {
		//Find node by name, attribute name and attribute value (may be null)
		NodeList nl = parent.getChildNodes();
		Node child;
		for(int i = 0; i < nl.getLength(); i++) {
			child = nl.item(i);
			if(child.getNodeName() == name) {
				Attr a = ((Element)child).getAttributeNode(attr);
				if(a != null) {
					if(attrVal == null || a.getNodeValue().equals(attrVal)) {
						return child;
					}
				} 
			}
		}
		return null;
	}
	
	/**
	 * Finds first child node of desired name or create one if there is no such node.
	 * New node is appended to the end of the list of children of parent node
	 * 
	 * @param p     Parent node of node to be found
	 * @param Name  Name of node to be found
	 * @return      First child node of desired name or newly created one 
	 */
	
	Node CreateOrFindChildByName(Node p, String Name) {
		Node n = GetFirstChildByName(p, Name);
		if(n == null) {
			return p.appendChild(document.createElement(Name));
		}
		return n;
	}

	/**
	 * Finds first child node of desired name or create one if there is no such node.
	 * New node is inserted after child node specified in the parameter "After"
	 * 
	 * @param p     Parent node of node to be found
	 * @param Name  Name of node to be found
	 * @param After Node that should be after newly created one 
	 * @return      First child node of desired name or newly created one 
	 */
	
	Node CreateOrFindChildByName(Node p, String Name, Node After) {
		Node n = GetFirstChildByName(p, Name);
		if(n == null) {
			Node s = After != null ? After.getNextSibling() : null;
			if(s == null) {
				return p.appendChild(document.createElement(Name));
			} else {
				return p.insertBefore(document.createElement(Name), s);
			}
		}
		return n;
	}	
	
	/**
	 * Finds LAST text node or create one if there is no such node.
	 * Text node is created as last child node 
	 * 
	 * @param p    Parent node of text node to be found or created
	 * @return 	   Last text node
	 */
	
	Node CreateOrFindTextChild(Node p) {
		if(p.hasChildNodes()) {
			NodeList nl = p.getChildNodes();
			//over all child nodes	
			Node lastTextChild = null;
			for(int i = 0; i < nl.getLength(); i++) {
				Node child = nl.item(i);
				if(Node.TEXT_NODE == child.getNodeType()) {
					lastTextChild = child; 
				}
			}			
			if(lastTextChild != null) {
				lastTextChild.setNodeValue(lastTextChild.getNodeValue().trim());
				return lastTextChild;
			}			
		} 
		return p.appendChild(document.createTextNode(""));
	}
	
	/**
	 * Finds FIRST text node or create one if there is no such node.
	 * Text node is created as first child node 
	 * 
	 * @param p    Parent node of text node to be found or created
	 * @return 	   First text node
	 */
	 
	Node CreateFirstOrFindTextChild(Node p) {
		if(p.hasChildNodes()) {
			NodeList nl = p.getChildNodes();
			//over all child nodes	
			for(int i = 0; i < nl.getLength(); i++) {
				Node child = nl.item(i);
				if(Node.TEXT_NODE == child.getNodeType()) {
					child.setNodeValue(child.getNodeValue().trim());
					return child;
				}
			}			
		} 
		return p.insertBefore(document.createTextNode(""), p.getFirstChild());
	}	

	/**
	 * Finds if FIRST child node is text or create one if there is no such node.
	 * Text node is created as first child node 
	 * 
	 * @param p    		Parent node of text node to be found or created
	 * @param warning Warning if there are more not empty text nodes
	 * @return 	   		First text node
	 */
	 
	Node CreateFirstOrGetTextChild(Node p, boolean warning) {
		if(p.hasChildNodes()) {
			NodeList nl = p.getChildNodes();
			String text = "";
			Node firstchild = nl.item(0);
  		if(Node.TEXT_NODE == firstchild.getNodeType()) { //found, store
  			firstchild.setNodeValue(firstchild.getNodeValue().trim());
			}	else { //not found created
				firstchild = p.insertBefore(document.createTextNode(""), p.getFirstChild());
			}
			for (int i = 1; i < nl.getLength(); i++) {
				Node child = nl.item(i); 
				if(Node.TEXT_NODE == child.getNodeType()) {
					if(!child.getNodeValue().trim().isEmpty()) { //we found not empty string
						if(text.isEmpty()) {
						  text = child.getNodeValue().trim();
						} else {
							text = text + " " + child.getNodeValue().trim();												
						}
					}
				}
			}
			if(!text.isEmpty()) {
				if(warning) {
					MessageBox messageBox = new MessageBox(MainHolder.getShell(), SWT.ICON_WARNING
		          | SWT.YES | SWT.NO | SWT.CANCEL);
					if(firstchild.getNodeValue().isEmpty()) {
					  messageBox.setMessage("Text '"+text+"' na neoèekávaném místì. Pøesunout?");
					} else {
						messageBox.setMessage("Text '"+text+"' na neoèekávaném místì. Spojit s správnì umístìným textem '"+firstchild.getNodeValue()+"' ?");
					}
		      messageBox.setText("Pøesunout?");
		      int response = messageBox.open();
		      if (response != SWT.CANCEL) {
		      	SetWasEdited();
		      	if(response == SWT.YES) {
		      		firstchild.setNodeValue((firstchild.getNodeValue() + " " + text).trim());  
		      	} 
	      		for (int i = 1; i < nl.getLength(); i++) {
	    				Node child = nl.item(i); 
	    				if(Node.TEXT_NODE == child.getNodeType()) {
	    					child.setNodeValue("");
	    				}
		      	}
		      }
				}  
			}
			return firstchild; 
		} 
		return p.insertBefore(document.createTextNode(""), p.getFirstChild());
	}	
	
	/**
	 * Creates or finds attribute. When attribute is created its value is set by DefaultValue parameter
	 * @param p							Parent node
	 * @param Name 					Name of attribute to be found or created
	 * @param DefaultValue  Default value of newly created attribute
	 * @return              Found or created attribute 
	 */
	
	Attr CreateOrFindAttribute(Element p, String Name, String DefaultValue) {
	  Attr a = p.getAttributeNode(Name);
	  if(a == null) {
	    a = document.createAttribute(Name);
      a.setValue(DefaultValue);
	    p.setAttributeNode(a);
	  } 
	  return a;
	}

	/**
	 * Creates or finds attribute. Value of attribute is set by DefaultValue parameter
	 * @param p							Parent node
	 * @param Name 					Name of attribute to be found or created
	 * @param DefaultValue  Value of attribute
	 * @return              Found or created attribute 
	 */	
	
	Attr CreateOrSetAttribute(Element p, String Name, String DefaultValue) {
	  Attr a = p.getAttributeNode(Name);
	  if(a == null) {
	    a = document.createAttribute(Name);
	    p.setAttributeNode(a);
	  } 	    
    a.setValue(DefaultValue);   
	  return a;
	}	

	/**
	 *  Class keeps single-line text field synchronized with XML text node value 
	 */
	
	class TXmlText {
		private Text text;
		Node textNode;

		public void modifyNodeText(String newText) {
			SetWasEdited();
			text.setText(newText);
			if(textNode != null) {
			  textNode.setNodeValue(newText);
			}
		}		
		
		class TXmlTextModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
				if(textNode != null) {
				  textNode.setNodeValue(text.getText().trim());
				}
			}		
		}
		public TXmlText (Node n, Text t){
			text = t;
			textNode = n;
			if(textNode != null) {
			  text.setText(textNode.getNodeValue().trim());
			}
			text.addModifyListener(new TXmlTextModifyListener());
		}
	}

	/**
	 *  Class keeps single-line text field synchronized with XML node attribute value 
	 */
	
	class TXmlAttrText {
		private Text text;
		Attr textAttr;		
		class TXmlTextModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
				if(textAttr != null) {
					textAttr.setNodeValue(text.getText().trim());					
				}
			}		
		}
		public TXmlAttrText (Attr n, Text t){
			text = t;
			textAttr = n;
			if(textAttr != null) {
			  text.setText(textAttr.getNodeValue().trim());
			}
			text.addModifyListener(new TXmlTextModifyListener());
		}
	}	
	
	
	
	/**
	 *  Class keeps two single-line text field synchronized with XML text node value.
	 *  First text field represents year of birth/creation, second text field represents year of death/destruction  
	 */
	
	class TXmlTextYearRange {
		private Text textBeg, textEnd;
		Node textNode;
		Node parent;
		class TXmlTextYearRangeModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
				if(textBeg.getText().trim().isEmpty() && textEnd.getText().trim().isEmpty()) {
					//if text is empty, delete
					if(textNode != null) {
						parent.removeChild(textNode.getParentNode());
						textNode = null;
					} 
				} else {
					textNode = CreateOrFindTextChild(CreateOrFindChildByName(parent, DATE)); 
					textNode.setNodeValue(textBeg.getText().trim()+" - "+textEnd.getText().trim());
				}
			}		
		}
		public TXmlTextYearRange (Node n, Composite group){
			
			new Label(group, SWT.NONE).setText("Rok narození/vzniku");
			Composite compoDates = new Composite(group, SWT.NONE);
			GridLayout l = new GridLayout(3,true);
			l.marginHeight = l.marginWidth = 0;
			compoDates.setLayout(l);
			GridData lgridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			lgridData.minimumWidth = 75;
			textBeg = new Text(compoDates, SWT.BORDER);
			textBeg.setLayoutData(lgridData);
			new Label(compoDates, SWT.NONE).setText("Rok úmrtí/zániku");
			textEnd = new Text(compoDates, SWT.BORDER);
			textEnd.setLayoutData(lgridData);
					
			parent   = n;
			textNode = GetFirstChildByName(parent, DATE);
			if(textNode != null) {
				textNode = CreateOrFindTextChild(textNode);
				String [] s = textNode.getNodeValue().split("-");
				if(s.length > 0) textBeg.setText(s[0].trim());
				if(s.length > 1) textEnd.setText(s[1].trim());
			}
			TXmlTextYearRangeModifyListener tyrml = new TXmlTextYearRangeModifyListener();
			textBeg.addModifyListener(tyrml);
			textEnd.addModifyListener(tyrml);
		}
	}	
	
	/**
	 * Class keeps three single-line text field synchronized with XML text node value.
	 * First one is number of first page, second one is number of second page, last one contains human readable notation.
	 * Last one can be automatically generated.
	 *  
	 * <br />e.g:<br />
	 * <code>&lt;locus from="1" to="16"&gt;s. 1-16&lt;/locus&gt;</code>
	 */
	
	class TXmlLocus {
		
		private Text textFrom;
		private Text textTo;
		private Text textAlternative;
		private Button btnAutoGenerate;		

		class TPagesModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
				if(btnAutoGenerate.getSelection()) {
				  textAlternative.setText("s. "+textFrom.getText()+"-"+textTo.getText());
				  btnAutoGenerate.setSelection(true);
				}
			}		
		}
		
		private void Construct(Node locus, Composite parent) {
			new Label(parent, SWT.NONE).setText("Stránky");
			
			Attr from = CreateOrFindAttribute((Element) locus, "from", "1");
			Attr to = CreateOrFindAttribute((Element) locus, "to", "1");
			
			Composite compoPages = new Composite(parent, SWT.NONE);
			GridLayout l = new GridLayout(5,false);
			l.marginHeight = l.marginWidth = 0;
			compoPages.setLayout(l);
			GridData lgridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			lgridData.minimumWidth = 75;
			new Label(compoPages, SWT.NONE).setText("od");
			textFrom = new Text(compoPages, SWT.BORDER);			
			textFrom.setLayoutData(lgridData);			
			new TXmlAttrText(from, textFrom);
			textFrom.addModifyListener(new TPagesModifyListener());
			new Label(compoPages, SWT.NONE).setText("do");
			textTo = new Text(compoPages, SWT.BORDER);
			textTo.setLayoutData(lgridData);			
			new TXmlAttrText(to, textTo);
			textTo.addModifyListener(new TPagesModifyListener());
			btnAutoGenerate = new Button(compoPages,SWT.CHECK);
			btnAutoGenerate.setText("Automaticky generovat zobrazovaný text");
			btnAutoGenerate.setSelection(true);
			new Label(parent, SWT.NONE).setText("Zobrazovaný zápis");
			textAlternative = new Text(parent, SWT.BORDER);
			textAlternative.setLayoutData(gridData);
			new TXmlText(CreateOrFindTextChild(locus), textAlternative);
			textAlternative.addModifyListener(new ModifyListener() {				
				@Override public void modifyText(ModifyEvent e) {
					btnAutoGenerate.setSelection(false);					
				}
			});
		}
		
		public TXmlLocus(Node locus, Composite parent) {
			Construct(locus, parent);
		}

		public TXmlLocus(Node locus, Composite parent, Color defaultColor) {
			Construct(locus, parent);
			textFrom.setBackground(defaultColor);
			textTo.setBackground(defaultColor);			
			textAlternative.setBackground(defaultColor);			
		}
	}
	
	/**
	 *  Class keeps <code>combobox</code> synchronized with XML text node value 
	 */
	
	class TXmlCombo {
		private Combo text;
		Node textNode;	
		
		public void modifyNodeText(String newText) {
			SetWasEdited();
			text.setText(newText);
			if(textNode != null) {
			  textNode.setNodeValue(newText);
			}
		}
		
		class TXmlTextModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
				if(textNode != null) {
				  textNode.setNodeValue(text.getText());
				}
			}		
		}
		public TXmlCombo (Node n, Combo t){
			text = t;
			textNode = n;
			if(textNode != null) {
			  text.setText(textNode.getNodeValue().trim());
			}
			text.addModifyListener(new TXmlTextModifyListener());
		}
	}

	/**
	 *  Class keeps <code>combobox</code> synchronized with XML node and attribute value.
	 *  Node value can be set by <code>combobox</code> text or by alternative strings.
	 *  Attribute value can be set by <code>combobox</code> text or by alternative strings.
	 */
	
	class TXmlNodeAttrCombo {
		Combo text;
		Attr textAttr;
		Node textNode;		
		String [] AttrValues;
		String [] NodeValues;
		class TXmlTextModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
			  if(AttrValues != null) {			  
				  textAttr.setNodeValue(AttrValues[text.getSelectionIndex()]);
				} else {
				  textAttr.setNodeValue(text.getText());
				}
				if(NodeValues != null) {			  
				  textNode.setNodeValue(NodeValues[text.getSelectionIndex()]);
				} else {
				  textNode.setNodeValue(text.getText());
				}				
			}		
		}
		
		public TXmlNodeAttrCombo(Node n, Attr a, Combo t, String [] vattr, String [] vnode){
			text = t;
			textAttr = a;
			textNode = n;
			AttrValues = vattr;
			NodeValues = vnode;
			text.addModifyListener(new TXmlTextModifyListener());
		}
	}
	
	/**
	 *  Class keeps <code>combobox</code> synchronized with XML node and multiple attributes value.
	 *  Node value can be set by <code>combobox</code> text or by alternative strings.
	 *  Multiple attributes value can be set by <code>combobox</code> text or by alternative strings.
	 */
	
	class TXmlNodeAttrCombo2 {
		Combo text;
		Attr [] textAttr;
		Node textNode;		
		String [] AttrValues;
		String [] NodeValues;
		class TXmlTextModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
			  if(AttrValues != null) {			  
			  	for(Attr a : textAttr) {
			  		if(a != null) {
				      a.setNodeValue(AttrValues[text.getSelectionIndex()]);
			  		}
			  	}
				} else {
					for(Attr a : textAttr) {
						if(a != null) {
				      a.setNodeValue(text.getText());
						}
					}
				}
				if(NodeValues != null) {			  
				  textNode.setNodeValue(NodeValues[text.getSelectionIndex()]);
				} else {
				  textNode.setNodeValue(text.getText());
				}				
			}		
		}
		
		public TXmlNodeAttrCombo2(Node n, Attr [] a, Combo t, String [] vattr, String [] vnode){
			text = t;
			textAttr = a;
			textNode = n;
			AttrValues = vattr;
			NodeValues = vnode;
			text.addModifyListener(new TXmlTextModifyListener());
		}
	}
	
	/**
	 *  Class keeps <code>combobox</code> synchronized with XML attribute value.
	 *  Attribute value can be set by <code>combobox</code> text or by alternative strings.
	 */
	
	class TXmlAttrCombo {
		Combo text;
		Attr textAttr;		
		String [] Values;
		class TXmlTextModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
			  if(Values != null) {			  
				  textAttr.setNodeValue(Values[text.getSelectionIndex()]);
				} else {
				  textAttr.setNodeValue(text.getText());
				}				
			}		
		}
		public TXmlAttrCombo(Attr a, Combo t, String [] v){
			text = t;
			textAttr = a;
			Values = v;
			if(Values != null) {
				String sa = a.getNodeValue();
				for(int i = 0; i < v.length; i++) {
					if(v[i].equals(sa)) {
						text.select(i);
						break;
					}
				}
			}			
			text.addModifyListener(new TXmlTextModifyListener());
		}
	}	

			
	final class TPrinter {
		private Node Master;
		private Node Printer;
		private Composite parent;
		private Group group;
		private Node Person;
		private Node Place;
		private Node PlaceText;
		private Node PersonText;
		private Combo comboPerson;
		private Text textPerson;
		private Text textPlace;

		public TPrinter(Composite p, Node n, Node m) {
			Printer = n;
			Master = m;
			parent = p;		
			group = null;	
					
			if(Printer == null) {
				CreateLatentPrinter();
			} else {
				CreatePrinter();
			}
		}
		
		private void CreateGroup() {
			if(group != null) {
				group.dispose();
			}
			group = new Group(parent,SWT.NONE);
			group.setLayout(new GridLayout(2, false));
			//group.setText("Tiskaø");
			/*
			Control [] control = parent.getChildren();
			if(control.length > 0) {
			  group.moveAbove(control[0]);
			}
			*/
			new Label (group, SWT.NONE).setText("Tiskaø");
			Button button = new Button(group, SWT.PUSH);
			button.setText(Printer == null ? "Doplnit ... (povinnì pro jednotlivý tisk)" : "Smazat ... (pro konvolut)");
			button.addSelectionListener(new SelectionListener() {			
				@Override
				public void widgetSelected(SelectionEvent e) {
					SetWasEdited();
					if(Printer == null) {
						CreatePrinter();					
					} else {
						CreateLatentPrinter();
					}
				}				
				@Override	public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);	
				}
			});
		}
		
		private void CreatePrinter() {
			if(Printer == null) {
				Printer = document.createElement("respStmt");				
				Master.appendChild(Printer);				
				Node resp = document.createElement("resp");
				Printer.appendChild(resp);
				CreateOrFindTextChild(resp).setNodeValue("Tiskaø");
			}
			
			CreateGroup();
			
		  //look for place and person
			Person = Place = PlaceText = null;
			Node child = Printer.getFirstChild();
			//there I find first two nodes than suits for place and person
			while(child != null && (Place == null || Person == null)) {
				if(child.getNodeName().equals("name")) {
					Attr type = CreateOrFindAttribute((Element) child, "type", "");
					if(type.getValue().equals("place")) {
						Place = child;
						PlaceText = CreateOrFindTextChild(Place);
					} else if(Person == null && (type.getValue().equals("person") || type.getValue().equals("org"))){ 
						Person = child;
						PersonText = CreateFirstOrGetTextChild(Person, true);  //20110426
				  }
				}
				child = child.getNextSibling();
			}
			//create missing person
			if(Person == null) {
				Person = Printer.appendChild(document.createElement("name"));
				PersonText = CreateFirstOrFindTextChild(Person); //20110421
				PersonText.setNodeValue("s.n.");
				CreateOrFindAttribute((Element) Person, "type", "person");
				CreateOrFindAttribute((Element) Person, "role", "printer");
			}
			//create missing place
			if(Place == null) {
				Place = Printer.appendChild(document.createElement("name"));
				PlaceText = CreateOrFindTextChild(Place);
				PlaceText.setNodeValue("s.l.");
				CreateOrFindAttribute((Element) Place, "type", "place");
				CreateOrFindAttribute((Element) Place, "role", "printer");
			}
			
			comboPerson = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY); 
			comboPerson.setItems(new String[] {"Osoba", "Korporace"});
			Attr personType = CreateOrFindAttribute((Element) Person, "type", "person");
			/*
			if(personType.getValue().equals("person")) {
				comboPerson.select(0);
			} else {
				comboPerson.select(1);
			}
			*/
			new TXmlAttrCombo(personType, comboPerson, new String[] {"person", "org"});			
			//create text for person
			textPerson = new Text(group, SWT.BORDER);			
			textPerson.setLayoutData(gridData);
			new TXmlText(PersonText, textPerson);
			
			//Create fields for dates
			new TXmlTextYearRange(Person, group);
  		
			//create text for place
			new Label(group, SWT.NONE).setText("Místo");
			textPlace = new Text(group, SWT.BORDER);
			textPlace.setLayoutData(gridData);
			new TXmlText(PlaceText, textPlace);
			
			
			PackAndSetExtends();
		}
		private void CreateLatentPrinter() {	
			if(Printer != null) {
				Printer.getParentNode().removeChild(Printer);
				Printer = null;
			}
			CreateGroup();			
			PackAndSetExtends();
		}
	}
	
	final class TResponsibilitiesStatement {
		private Group     group;
		private Composite compRole;
		private Combo     comboRole;
		private Combo 		comboPerson;
		private Text      textPerson;
		private Text 		  textPlace;
		private TXmlText  textXmlPlace;
		private Composite compPlace;
		private Node 			Person;
		private Node 			PersonText;	
		private Node 			Place;
		private Node 			PlaceText;
		private TXmlNodeAttrCombo2 xmlComboRole;
		private Button checkBox;		
		public TResponsibilitiesStatement(final Composite parent, final Node n) {
			if(dummyLabel != null) {
				dummyLabel.dispose();
				dummyLabel = null;
			}
			group = new Group(parent,SWT.NONE);
			group.setLayout(new GridLayout(2, false));
			compRole = new Composite(group, SWT.NONE);
			GridLayout l = new GridLayout(2,true);
			l.marginHeight = l.marginWidth = 0;
			compRole.setLayout(l);
			new Label (compRole, SWT.NONE).setText("Role");
			Button button = new Button(compRole, SWT.PUSH);
			button.setText("x");
			button.setToolTipText("Vymazat tuto odpovìdnost");
			//look for reps
			Node resp = CreateOrFindChildByName(n, "resp");
			resp = CreateOrFindTextChild(resp);
			//look for place and person
			Person = Place = PlaceText = null;
			Node child = n.getFirstChild();
			//there I find first two nodes than suits for place and person
			while(child != null && (Place == null || Person == null)) {
				if(child.getNodeName().equals("name")) {
					Attr type = CreateOrFindAttribute((Element) child, "type", "");
					if(type.getValue().equals("place")) {
						Place = child;
						PlaceText = CreateOrFindTextChild(Place);
					} else if(Person == null && (type.getValue().equals("person") || type.getValue().equals("org"))){ 
						Person = child;
				  }
				}
				child = child.getNextSibling();
			}
			//create missing person
			if(Person == null) {
				Person = n.appendChild(document.createElement("name"));
				CreateOrFindAttribute((Element) Person, "type", "person");
			}
			//create combo for choosing role
			comboRole = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
			comboRole.setItems(RESPONSIBLE);			
			comboRole.setText(resp.getNodeValue());
			//comboRole.setLayoutData(gridData);
			//create array of attributes
			Attr [] attrs = new Attr [2];
			attrs[0] = CreateOrFindAttribute((Element) Person, "role", "");
			attrs[1] = Place == null ? null : CreateOrFindAttribute((Element) Place, "role", "");
			//create link between node, attributes and combo
			xmlComboRole = new TXmlNodeAttrCombo2(resp, attrs, comboRole, new String[] {"former_owner", "publisher", "book_seller", "illustration_printer"},RESPONSIBLE);
			
			//create combo for person
			comboPerson = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY); 
			comboPerson.setItems(new String[] {"Osoba", "Korporace"});			
			Attr personType = CreateOrFindAttribute((Element) Person, "type", "person");
			/*
			if(personType.getValue().equals("person")) {
				comboPerson.select(0);
			} else {
				comboPerson.select(1);
			}
			*/
			new TXmlAttrCombo(personType, comboPerson, new String[] {"person", "org"});			
			//create text for person
			textPerson = new Text(group, SWT.BORDER);
			PersonText = CreateFirstOrGetTextChild(Person, true); //20110421
			textPerson.setLayoutData(gridData);
			new TXmlText(PersonText, textPerson);
			
			//Create fields for dates
			new TXmlTextYearRange(Person, group);
			
			//create fields for place
			compPlace = new Composite(group, SWT.NONE);
			compPlace.setLayout(l);
			new Label (compPlace, SWT.NONE).setText("Místo");
			checkBox = new Button(compPlace, SWT.CHECK);
			checkBox.setSelection(Place != null);
			//create text for place
			textPlace = new Text(group, SWT.BORDER);			
			textPlace.setEnabled(Place != null);
			textPlace.setLayoutData(gridData);
			textXmlPlace = new TXmlText(PlaceText, textPlace);
			
			PackAndSetExtends();
			
			checkBox.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SetWasEdited();
					if(checkBox.getSelection()) {
						//create place
						Place = n.appendChild(document.createElement("name"));
						CreateOrFindAttribute((Element) Place, "type", "place");
						xmlComboRole.textAttr[1] = CreateOrFindAttribute((Element) Place, "role", xmlComboRole.textAttr[0].getNodeValue());
						textXmlPlace.textNode = PlaceText = CreateOrFindTextChild(Place);
						PlaceText.setNodeValue(textPlace.getText());
						textPlace.setEnabled(true);						
					} else {
						//delete place
						xmlComboRole.textAttr[1] = null;
						textXmlPlace.textNode = null;
						textPlace.setEnabled(false);
						Place.getParentNode().removeChild(Place);
					}					
				}				
				@Override	public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
			
			button.addSelectionListener(new SelectionListener() {						
				@Override
				public void widgetSelected(SelectionEvent e) {
					SetWasEdited();
					group.dispose();
					/*
					compRole.dispose();
					comboRole.dispose();
					comboPerson.dispose();
					textPerson.dispose();
					compPlace.dispose();
					textPlace.dispose();
					*/
					if(parent.getChildren().length == 0) {
						dummyLabel = new Label(parent, SWT.NONE);
					}
					PackAndSetExtends();
					n.getParentNode().removeChild(n);
				}						
				@Override	public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
		}
	}
	
	final class TOtherAuthor {
		Composite c;
		Text textOtherAutor;	 		   
		public TOtherAuthor(final Composite parent, final Label sibling, final Node n) {
			c = new Composite(parent, SWT.NONE); 
			c.moveAbove(sibling);
			GridLayout l = new GridLayout(2,true);
			l.marginHeight = l.marginWidth = 0;
			c.setLayout(l);
			new Label (c, SWT.NONE).setText ("Autor");
			Button b = new Button(c, SWT.PUSH);
			b.setText("x");
			b.setToolTipText("Vymazat tohoto autora");
			textOtherAutor = new Text (parent, SWT.BORDER);
			textOtherAutor.moveBelow(c);			    			    
			textOtherAutor.setLayoutData(gridData);		
			Node msHeadingAutorText = CreateOrFindTextChild(n);
			new TXmlText(msHeadingAutorText, textOtherAutor);
			PackAndSetExtends();
			b.addSelectionListener(new SelectionListener() {						
				@Override
				public void widgetSelected(SelectionEvent e) {
					SetWasEdited();
					textOtherAutor.dispose();
					c.dispose();
					PackAndSetExtends();
					n.getParentNode().removeChild(n);
				}						
				@Override	public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});			
		}
	}

	class AddAutorButtonListener implements SelectionListener {
		Node AuthorParent;
		Composite parent;
		Label sibling;
		public AddAutorButtonListener(final Composite p, final Label s, Node n) {
			parent = p;
			sibling = s;
			AuthorParent = n;
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			SetWasEdited();
			Node child = AuthorParent.getFirstChild();
			Node lastAuthor = null;
			while(child != null) {
				if(child.getNodeName().equals(AUTHOR)) {
					lastAuthor = child;
				}
				child = child.getNextSibling();
			}
			Node newAuthor = document.createElement(AUTHOR);
			AuthorParent.insertBefore(newAuthor, lastAuthor.getNextSibling());
			newAuthor.appendChild(document.createTextNode(""));
			new TOtherAuthor(parent, sibling, newAuthor);
		}
		@Override	public void widgetSelected(SelectionEvent e) {
			widgetDefaultSelected(e);
		}			
	}
	
	class AddRespButtonListener implements SelectionListener {
		Node respParent;
		Composite parent;
		AddRespButtonListener(final Composite p,  Node n) {
			parent = p;
			respParent = n;
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			SetWasEdited();
			Node respStmt = respParent.getFirstChild();
			Node lastRespStmt = null;
			while(respStmt != null) {
				if(respStmt.getNodeName().equals("respStmt")) {
				  lastRespStmt = respStmt;				  
				}
				respStmt = respStmt.getNextSibling();
			}			
			respStmt = document.createElement("respStmt");
			respParent.insertBefore(respStmt, lastRespStmt);
			
			new TResponsibilitiesStatement(parent, respStmt);
		}

		@Override	public void widgetSelected(SelectionEvent e) {
			widgetDefaultSelected(e);
		}				
	}
	
	class TAdditional {
		Node additional;
		Group gpAdditional;
		//GridData gridData240;
		public TAdditional(Node parent, Composite sibling) {
			additional = CreateOrFindChildByName(parent, "additional");
			
			GridLayout gridLayoutCompact = new GridLayout(3,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;
			GridData lgridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			lgridData.minimumWidth = 75;
			/*			
			gridData240 = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			gridData240.minimumWidth = 240;
			*/
			
			gpAdditional = new Group(sibling.getParent(), SWT.NONE); 
			FormData formData = new FormData();
			formData.top = new FormAttachment(sibling);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			gpAdditional.setLayoutData(formData);
			gpAdditional.setLayout(new FormLayout());			
		
			Node bibl;
			boolean createKnihopis;
			Text textIdno = null;
			Composite compositeOtherExemplars = null;
			Button buttonAddExemplar = null;
			//listBibl
			Node listBibl = CreateOrFindChildByName(additional, "listBibl");			
			
			Composite composite = null;
			
			if(!isComposite) {  //Knihopis and other exemplars
				composite = new Composite(gpAdditional, SWT.NONE);			
				composite.setLayout(new GridLayout(2,false));
				formData = new FormData();
				formData.left = new FormAttachment(0,0);
				formData.right = new FormAttachment(100,0);
				composite.setLayoutData(formData);			
				
				createKnihopis = true;
					
				//bibl
				bibl = listBibl.getFirstChild();
				
				//create visual part for knihopis
				new Label (composite, SWT.NONE).setText("Knihopis");
				Composite compositeIdno = new Composite(composite, SWT.NONE);
				compositeIdno.setLayout(gridLayoutCompact);
				textIdno = new Text (compositeIdno, SWT.BORDER);
				textIdno.setLayoutData(lgridData);				
				new Label (compositeIdno, SWT.NONE).setText("forma dle èísla ve formátu Knihopisu Digital (K13172)");
				
				//create visual for other exemplars
				compositeOtherExemplars = new Composite(gpAdditional, SWT.NONE);
				compositeOtherExemplars.setLayout(new GridLayout(1,false));
				formData = new FormData();
				formData.top = new FormAttachment(composite);
				formData.left = new FormAttachment(0,0);
				formData.right = new FormAttachment(100,0);
				compositeOtherExemplars.setLayoutData(formData);
				
				composite = new Composite(gpAdditional, SWT.NONE);			
				composite.setLayout(new GridLayout(2,false));
				formData = new FormData();
				formData.top = new FormAttachment(compositeOtherExemplars);
				formData.left = new FormAttachment(0,0);
				formData.right = new FormAttachment(100,0);
				composite.setLayoutData(formData);				
				buttonAddExemplar = new Button(composite, SWT.PUSH);			
				buttonAddExemplar.setText("Pøidat další exempláø");
			} else {
				createKnihopis = false;			
				//bibl
				bibl = listBibl.getFirstChild();				
			}
			
			//create visual for literature
			
			Composite compositeLiterature = new Composite(gpAdditional, SWT.NONE);
			compositeLiterature.setLayout(new GridLayout(1,false));
			formData = new FormData();
			if(composite != null) formData.top = new FormAttachment(composite);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			compositeLiterature.setLayoutData(formData);
			
			composite = new Composite(gpAdditional, SWT.NONE);			
			composite.setLayout(new GridLayout(2,false));
			formData = new FormData();
			formData.top = new FormAttachment(compositeLiterature);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);				
			Button buttonAddLiterature = new Button(composite, SWT.PUSH);			
			buttonAddLiterature.setText("Pøidat další literaturu");
			
			//go trough all nodes looking for bibl
			while (bibl != null) {
				if (bibl.getNodeName() == "bibl") { //bilb was found
					Node idno = GetFirstChildByName(bibl, "idno");
					if(idno != null) { //knihopis or other exemplar
						if(!isComposite) { 
							Attr idnoType = CreateOrFindAttribute((Element) idno, "type", "other_exemplar");
							if(idnoType.getNodeValue().equals("knihopis")) {
								createKnihopis = false;
								new TXmlText(CreateOrFindTextChild(idno), textIdno); //bind node with text field
							} else if(idnoType.getNodeValue().equals("other_exemplar")) { //other_exemplar
								new TOtherExemplar(idno, compositeOtherExemplars);
							} else { //unknown
								//TODO: deal with unknown
								
							}
						} else {
							//TODO: deal with unexpected bibl
						}
					} else { //literature - get text node
						new TLiterature(bibl, compositeLiterature);
					}					
				}
				bibl = bibl.getNextSibling();
			}
			
			if (createKnihopis) { //create knihopis if it failed to find one
				bibl = listBibl.insertBefore(document.createElement("bibl"), listBibl.getFirstChild());
				Node idno = bibl.appendChild(document.createElement("idno"));
				CreateOrFindAttribute((Element) idno, "type", "knihopis");	
				new TXmlText(CreateOrFindTextChild(idno), textIdno); //bind node with text field
			}		
			
					
			class AddOtherExemplarListener implements SelectionListener {
				private Node listBibl;
				private Composite compositeOtherExemplars;
				
				public AddOtherExemplarListener(Node n, Composite c) {
					listBibl = n;
					compositeOtherExemplars = c;
				}
				@Override	public void widgetSelected(SelectionEvent e) {
					SetWasEdited();
					Node OtherExemplar;
					OtherExemplar = listBibl.appendChild(document.createElement("bibl"));
					OtherExemplar = OtherExemplar.appendChild(document.createElement("idno"));					
					CreateOrFindAttribute((Element) OtherExemplar, "type", "other_exemplar");		
					new TOtherExemplar(OtherExemplar, compositeOtherExemplars);
					PackAndSetExtends();
				}				
				@Override	public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);					
				}				
			}

			class AddLiteratureListener implements SelectionListener {
				private Node listBibl;
				private Composite compositeLiterature;
				
				public AddLiteratureListener(Node n, Composite c) {
					listBibl = n;
					compositeLiterature = c;
				}
				@Override	public void widgetSelected(SelectionEvent e) {
					SetWasEdited();
					Node Literature;
					Literature = listBibl.appendChild(document.createElement("bibl"));		
					new TLiterature(Literature, compositeLiterature);
					PackAndSetExtends();
				}				
				@Override	public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);					
				}				
			}			
			
			if(buttonAddExemplar != null) buttonAddExemplar.addSelectionListener(new AddOtherExemplarListener(listBibl, compositeOtherExemplars));
			buttonAddLiterature.addSelectionListener(new AddLiteratureListener(listBibl, compositeLiterature));
		}
		
	}

	class TOtherExemplar {
		public TOtherExemplar(final Node t, Composite c) {
			final Composite composite = new Composite(c, SWT.NONE);
			GridData lgridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			lgridData.minimumWidth = 75;
			  
			GridLayout gridLayout = new GridLayout(3, false); //add more columns to hold years
			gridLayout.marginHeight = gridLayout.marginWidth = 0;
			composite.setLayout(gridLayout);
				
			new Label (composite, SWT.NONE).setText("Další exempláø");
			Text textIdno = new Text (composite, SWT.BORDER);
			textIdno.setLayoutData(lgridData);	
			new TXmlText(CreateOrFindTextChild(t), textIdno);
				
			Button removeExemplar = new Button(composite, SWT.PUSH);
			removeExemplar.setText("x");					
			removeExemplar.addSelectionListener(new SelectionListener() {						
			@Override
			public void widgetSelected(SelectionEvent e) {
				SetWasEdited();
				composite.dispose();
				PackAndSetExtends();
				Node bibl = t.getParentNode(); 
				bibl.getParentNode().removeChild(bibl);
			}						
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			});	
		}
	}	

	class TLiterature {
		public TLiterature(final Node t, Composite c) {
			final Composite composite = new Composite(c, SWT.NONE);
			GridData lgridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			lgridData.minimumWidth = 75;
			lgridData.heightHint = 16*3;
			lgridData.widthHint = 480;
			  
			GridLayout gridLayout = new GridLayout(3, false); //add more columns to hold years
			gridLayout.marginHeight = gridLayout.marginWidth = 0;
			composite.setLayout(gridLayout);
				
			new Label (composite, SWT.NONE).setText("Literatura");
			Text textIdno = new Text (composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);	
			textIdno.setLayoutData(lgridData);	
			new TXmlText(CreateOrFindTextChild(t), textIdno);
				
			Button removeExemplar = new Button(composite, SWT.PUSH);
			removeExemplar.setText("x");					
			removeExemplar.addSelectionListener(new SelectionListener() {						
			@Override
			public void widgetSelected(SelectionEvent e) {
				SetWasEdited();
				composite.dispose();
				PackAndSetExtends(); 
				t.getParentNode().removeChild(t);
			}						
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			});	
		}
	}	
	
	class THistory {
		Node history;
		Group gpHistory;
		GridData gridData240;
		public THistory(Node parent, Composite sibling) {
			history = CreateOrFindChildByName(parent, "history");
			
			GridLayout gridLayoutCompact = new GridLayout(4,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;			
						
			gridData240 = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			gridData240.minimumWidth = 240;
			
			gpHistory = new Group(sibling.getParent(), SWT.NONE); 
			FormData formData = new FormData();
			formData.top = new FormAttachment(sibling);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			gpHistory.setLayoutData(formData);
			gpHistory.setLayout(new FormLayout());			
		
			Composite composite = new Composite(gpHistory, SWT.NONE);			
			composite.setLayout(new GridLayout(2,false));
			formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);			
			
			//origin
			Node origin = CreateOrFindChildByName(history, "origin");
			Node originP = CreateOrFindChildByName(origin, "p");
			
			//place name
			
			Node placeName = FindChildByNameAndAttribute(originP, "placeName", "type", "country");
			if(placeName == null) {
				placeName = originP.appendChild(document.createElement("placeName"));
				CreateOrFindAttribute((Element) placeName, "type", "country");				
			}
			
			new Label (composite, SWT.NONE).setText("Zemì pùvodu");
			
			Combo comboPlaceName = new Combo (composite, SWT.NONE);
			comboPlaceName.setItems (new String [] {"Èesko"});
			comboPlaceName.setLayoutData(gridData240);			 
			new TXmlCombo(CreateOrFindTextChild(placeName), comboPlaceName);
			
			//provenance
			if(masterDocument == null) {
			  Node provenance = CreateOrFindChildByName(history, "provenance", origin);			
			  Node provenanceP = CreateOrFindChildByName(provenance, "p");

			
				new Label (composite, SWT.NONE).setText("Provenience");
				Text textProvenance = new Text (composite, SWT.BORDER);
				textProvenance.setLayoutData(gridData);
				new TXmlText(CreateOrFindTextChild(provenanceP), textProvenance);
			}
			
			//origDate
			
			Node origDate;
			
			if (isComposite) { //different location of origin date
				origDate = CreateOrFindChildByName(msHeading, "origDate");
			} else {
        origDate = CreateOrFindChildByName(originP, "origDate"); 
			}	
						
			new Label (composite, SWT.NONE).setText("Datace (slovnì)");
			Text textOrigDate = new Text (composite, SWT.BORDER);
			textOrigDate.setLayoutData(gridData);
			new TXmlText(CreateOrFindTextChild(origDate), textOrigDate);
					
			new Label (composite, SWT.NONE).setText("Datace (meze)");
			
			Composite compositeOrigDate = new Composite(composite, SWT.NONE);
			compositeOrigDate.setLayout(gridLayoutCompact);
			
			Attr notBefore = CreateOrFindAttribute((Element) origDate, "notBefore", "1900");
			Attr notAfter  = CreateOrFindAttribute((Element) origDate, "notAfter",  "1900");

			GridData lgridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			lgridData.minimumWidth = 75;
			
			new Label(compositeOrigDate, SWT.NONE).setText("ne pøed");
			Text textNotBefore = new Text(compositeOrigDate, SWT.BORDER);			
			textNotBefore.setLayoutData(lgridData);
			
			new TXmlAttrText(notBefore, textNotBefore);
					
			new Label(compositeOrigDate, SWT.NONE).setText("ne po");
			Text textNotAfter = new Text(compositeOrigDate, SWT.BORDER);			
			textNotAfter.setLayoutData(lgridData);
			
			new TXmlAttrText(notAfter, textNotAfter);
			
			//acquisition
			
			Node acquisition = CreateOrFindChildByName(history, "acquisition");
			Node acquisitionP = CreateOrFindChildByName(acquisition, "p");
			
      //if(masterDocument == null) {
				new Label (composite, SWT.NONE).setText("Pøírùstkové èíslo");
				Text textAcquisition = new Text (composite, SWT.BORDER);
				textAcquisition.setLayoutData(gridData);
				new TXmlText(CreateOrFindTextChild(acquisitionP), textAcquisition);
      //}	
			/*
			<origin><p>
			<!-- Zemì pùvodu -->
						<placeName type="country">Èesko</placeName>
						
			<!-- DATACE TISKU (rukopisu) -->
						<origDate notBefore="1780" notAfter="1820">pøelom 18. a 19. století(</origDate>
						</p></origin>
			*/			
			
		}
	}
	
	class TPhysDesc {
		Node msDescription;
		GridData physGridData;
		Group gpPhysDesc;
		Composite compositeTerms;
		Composite compositeAddNext;
		private GridData summGridData, summGridData2, summGridData3;
	
		class TTermIllustration {
			Composite composite;
			
			public TTermIllustration(final Node t) {
				composite = new Composite(compositeTerms, SWT.NONE);
				GridLayout gridLayout = new GridLayout(3, false); 
				gridLayout.marginHeight = gridLayout.marginWidth = 0;
				composite.setLayout(gridLayout);
				Label label = new Label (composite, SWT.NONE);
				label.setText("Odkaz na ilustraci");
				label.setLayoutData(summGridData);
				//Attr termType = CreateOrFindAttribute((Element) t, "type", "illustration");						
				Text text = new Text(composite, SWT.BORDER);
//				text.setBackground(bgColor);
				text.setLayoutData(summGridData2);
				new TXmlText(CreateOrFindTextChild(t), text);					
				Button removeTerm = new Button(composite, SWT.PUSH);
				removeTerm.setText("x");					
				removeTerm.addSelectionListener(new SelectionListener() {						
				@Override
				public void widgetSelected(SelectionEvent e) {
					SetWasEdited();
					composite.dispose();
					PackAndSetExtends();
					t.getParentNode().removeChild(t);
				}						
				@Override	public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
				});	
			}
		}		
		
		class AddIllustrationRefListener implements SelectionListener {
			Node decorationDecoNoteP;
			public AddIllustrationRefListener(Node n) {
				decorationDecoNoteP = n;
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				SetWasEdited();
				Node termIllustration = decorationDecoNoteP.appendChild(document.createElement("term"));				
				CreateOrFindAttribute((Element) termIllustration, "type", "illustration");
				new TTermIllustration(termIllustration);
				PackAndSetExtends();
			}
			@Override	public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		}				
		
		public TPhysDesc(Node parent, Composite sibling) {

	  	summGridData = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
	  	summGridData.widthHint = 100;
	  	summGridData2 = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
	  	summGridData2.widthHint = 240;
	  	summGridData3 = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
	  	summGridData3.widthHint = 600;
			
			GridLayout gridLayoutCompact = new GridLayout(2,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;			
			
			msDescription = parent;
			
			physGridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			physGridData.minimumWidth = 240;
			
			Node physDesc	= CreateOrFindChildByName(msDescription, "physDesc");
			gpPhysDesc = new Group(sibling.getParent(), SWT.NONE); 
			FormData formData = new FormData();
			formData.top = new FormAttachment(sibling);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			gpPhysDesc.setLayoutData(formData);
			gpPhysDesc.setLayout(new FormLayout());
			
			Composite composite = new Composite(gpPhysDesc, SWT.NONE);			
			composite.setLayout(new GridLayout(2,false));
			formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);
			 
			if(!isComposite) {  //physical description
				//form
				Node form = CreateOrFindChildByName(physDesc, "form");
				form =  CreateOrFindChildByName(form, "p");
					
				new Label (composite, SWT.NONE).setText ("Typ pøedmìtu");
				
				Combo comboForm = new Combo (composite, SWT.NONE);
			  comboForm.setItems (new String [] {"Kniha - tisk", "Rukopis", "Strojopis"});
				comboForm.setLayoutData(physGridData);			 
				new TXmlCombo(CreateOrFindTextChild(form), comboForm);
				
				//support
				Node support = CreateOrFindChildByName(physDesc, "support");
				support =  CreateOrFindChildByName(support, "p");
					
				new Label (composite, SWT.NONE).setText ("Materiál");
				
				Combo comboSupport = new Combo (composite, SWT.NONE);
				comboSupport.setItems (new String [] {"Papír"});
				comboSupport.setLayoutData(physGridData);			 
				new TXmlCombo(CreateOrFindTextChild(support), comboSupport);
			}
			//extent
			
			new Label (composite, SWT.NONE).setText ("Poèet stran");
			
			Node extent = CreateOrFindChildByName(physDesc, "extent");
						
			Composite compositeExtent = new Composite(composite, SWT.NONE);
			compositeExtent.setLayout(gridLayoutCompact);
			
			Combo comboExtent = new Combo (compositeExtent, SWT.NONE);
			comboExtent.setItems (new String [] {"[x] s.", "x s."});
			comboExtent.setLayoutData(physGridData);
			new Label (compositeExtent, SWT.NONE).setText ("(neèíslované v hranatých závorkách)");
			new TXmlCombo(CreateFirstOrFindTextChild(extent), comboExtent);
			
			//extent - dimensions			
			new Label (composite, SWT.NONE).setText ("Formát");
			
			Node extentDim = CreateOrFindChildByName(extent, "dimensions");
			if (isComposite) {  //dimensions
				CreateOrSetAttribute((Element)extentDim, "scope", "most");
			} else {
				CreateOrSetAttribute((Element)extentDim, "scope", "all");
			}			
			CreateOrSetAttribute((Element)extentDim, "type", "leaf");
			CreateOrSetAttribute((Element)extentDim, "units", "mm");
			
			Combo comboExtentDim = new Combo (composite, SWT.NONE);
			comboExtentDim.setItems (new String [] {"Jednolist", "8°.", "16°.", "24°."});
			comboExtentDim.setLayoutData(physGridData);			 			
			new TXmlCombo(CreateFirstOrFindTextChild(extentDim), comboExtentDim);
			
			if(!isComposite) { //physical description
				//layout
				new Label (composite, SWT.NONE).setText ("Dispozice");
				
				Node layout = CreateOrFindChildByName(physDesc, "layout"); 
				Node layoutP = CreateOrFindChildByName(layout, "p");
				
				Combo combolayoutP = new Combo (composite, SWT.NONE);
				combolayoutP.setItems (new String [] {"Tištìno per extensum.", "Tištìno ve dvou sloupcích.", "Tištìno ve tøech sloupcích."});
				combolayoutP.setLayoutData(physGridData);			 
				new TXmlCombo(CreateFirstOrFindTextChild(layoutP), combolayoutP);

			
  			//dimension
	  		new Label (composite, SWT.NONE).setText ("Zrcadlo tisku");
		  	new TDimensions(composite, CreateOrFindChildByName(layoutP, "dimensions"));

		  	//msWriting
				new Label (composite, SWT.NONE).setText ("Písmo");
				Node msWriting = CreateOrFindChildByName(physDesc, "msWriting");
				Node msWritingP = CreateOrFindChildByName(msWriting, "p");
				
				Combo comboWritingP = new Combo (composite, SWT.NONE);
				comboWritingP.setItems (new String [] {"Gotika", "Latinka"});
				comboWritingP.setLayoutData(physGridData);			 
				new TXmlCombo(CreateFirstOrFindTextChild(msWritingP), comboWritingP);
				
				//decoration
				
				new Label (composite, SWT.NONE).setText ("Výzdoba");
				Node decoration = CreateOrFindChildByName(physDesc, "decoration");
				Node decorationDecoNote = CreateOrFindChildByName(decoration, "decoNote");
				Node decorationDecoNoteP = CreateOrFindChildByName(decorationDecoNote, "p");
				
				Text textDecoNoteP = new Text (composite, SWT.BORDER);
				textDecoNoteP.setLayoutData(gridData);
				new TXmlText(CreateFirstOrFindTextChild(decorationDecoNoteP), textDecoNoteP); //corrected 0.9.1
				
			  //dimension
				new Label (composite, SWT.NONE).setText ("Rozmìr výzdoby");
				new TDimensions(composite, CreateOrFindChildByName(decorationDecoNoteP, "dimensions"));
				
				//button for adding illustration references 
				
		  	compositeTerms = new Composite(gpPhysDesc, SWT.NONE);
				formData = new FormData();			
				formData.top = new FormAttachment(composite);
				formData.left = new FormAttachment(0,0);
				formData.right = new FormAttachment(100,0);
				compositeTerms.setLayoutData(formData);
	
				RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
				rowLayout.spacing = 5;
				rowLayout.marginHeight = rowLayout.marginWidth = 2; 
				compositeTerms.setLayout(rowLayout);			
				
				Node termIllustration = FindChildByNameAndAttribute(decorationDecoNoteP, "term", "type", "illustration");						
				while(termIllustration != null) {							
				  if(termIllustration.getNodeName().equals("term")) {			  	
				  	Attr a = ((Element)termIllustration).getAttributeNode("type");
						if(a != null) {
							if(a.getNodeValue().equals("illustration")) {
								new TTermIllustration(termIllustration);
							}
						} 			  				  				  	
				  }
				  termIllustration = termIllustration.getNextSibling();
				}			
				
				
				
				compositeAddNext = new Composite(gpPhysDesc, SWT.NONE);
				formData = new FormData();			
				formData.top = new FormAttachment(compositeTerms);
				formData.left = new FormAttachment(0,0);
				formData.right = new FormAttachment(100,0);
				compositeAddNext.setLayoutData(formData);			
				compositeAddNext.setLayout(new GridLayout(2, false));
				
				Label labelAddIllustrationRef = new Label (compositeAddNext, SWT.NONE);
				labelAddIllustrationRef.setText ("Odkaz na ilustraci");
				Button buttonAddIllustrationRef = new Button(compositeAddNext, SWT.PUSH);
				buttonAddIllustrationRef.setText("pøidat ...");
				buttonAddIllustrationRef.addSelectionListener(new AddIllustrationRefListener(decorationDecoNoteP));
			}
			
			
			//condition
			new Label (composite, SWT.NONE).setText ("Stav pøedmìtu");
			Node condition = CreateOrFindChildByName(physDesc, "condition");
			Node conditionP = CreateOrFindChildByName(condition, "p");
			
			Combo comboConditionP = new Combo (composite, SWT.NONE);
			comboConditionP.setItems (new String [] {"Stav dobrý", "Restaurováno", "Poškozeno", "Velmi poškozeno"});
		  comboConditionP.setLayoutData(summGridData3);
		  SetWidth(comboConditionP, 600);
		  new TXmlCombo(CreateFirstOrFindTextChild(conditionP), comboConditionP);
			
			/*
			if(isComposite) {
				Text textConditionP = new Text (composite, SWT.BORDER);
				textConditionP.setLayoutData(summGridData3);
				SetWidth(textConditionP, 600);
				new TXmlText(CreateFirstOrFindTextChild(conditionP), textConditionP);
			} else {
				Combo comboConditionP = new Combo (composite, SWT.NONE);
  			comboConditionP.setItems (new String [] {"Stav dobrý", "Restaurováno", "Poškozeno", "Velmi poškozeno"});
			  comboConditionP.setLayoutData(physGridData);			 
			  new TXmlCombo(CreateFirstOrFindTextChild(conditionP), comboConditionP);				
			}
			*/
			PackAndSetExtends();
		}
	}
	
	class TDimensions {
		public TDimensions(Composite parent, Node dimensions) {	
			
			GridData gridDataDim = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			gridDataDim.minimumWidth = 50;
			
			CreateOrSetAttribute((Element)dimensions, "units", "mm");
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout gridLayoutCompact = new GridLayout(5,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;
			composite.setLayout(gridLayoutCompact);
			new Label (composite, SWT.NONE).setText ("výška");
			Node height = CreateOrFindChildByName(dimensions, "height");			
			
			Text textHeight = new Text (composite, SWT.BORDER);
			textHeight.setLayoutData(gridDataDim);
			new TXmlText(CreateOrFindTextChild(height), textHeight);
			
			new Label (composite, SWT.NONE).setText ("mm \t šíøka");
			Node width = CreateOrFindChildByName(dimensions, "width");
			
			Text textWidth = new Text (composite, SWT.BORDER);
			textWidth.setLayoutData(gridDataDim);
			new TXmlText(CreateOrFindTextChild(width), textWidth);
			
			new Label (composite, SWT.NONE).setText ("mm");
		}
	}
	
	class AddSubItem implements SelectionListener {
		Node mParent;
		Composite mComposite;
		TContent Content;
		public AddSubItem(Node parent, Composite p, TContent c) {
			mParent = parent; mComposite = p; Content = c;
		}
		@Override public void widgetSelected(SelectionEvent e) {
			SetWasEdited();
			msSubItemIndex++;
			Node msSubItem = mParent.appendChild(document.createElement("msItem"));
			CreateOrSetAttribute((Element)msSubItem,"n","1."+msSubItemIndex);
			new TmsSubItem(msSubItem, mComposite, Content);
			if(Content.incipit != null) { //if not deleted delete now
				Content.incipit.Dispose();
				Content.incipit = null;				
			}
			if(Content.Melody != null) { //if not deleted delete now
				Content.Melody.Dispose();
				Content.Melody = null;				
			}
		}				
		@Override	public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);					
		}				
	};
	
	class TmsSubItem {
		Node msItem1;
		private Group gpMsItem;
		GridData subGridData;		
		TMelody Melody;
		TContent Content; 
		int Index;
		Color defaultColor;
		TMultiLineNode incipit;
		
		class DeleteSubItem implements SelectionListener {
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				MessageBox messageBox = new MessageBox(MainHolder.getShell(), SWT.ICON_WARNING
	          | SWT.YES | SWT.NO);
	      messageBox.setMessage("'"+gpMsItem.getText()+"' bude smazán. Pokraèovat?");
	      messageBox.setText("Odebrat?");
	      int response = messageBox.open();
	      if (response == SWT.YES) {
	      	SetWasEdited(); 
	      	for(int i = Index; i < msSubItemIndex; i++) {
						TmsSubItem si = Content.msSubItems.get(i);
						si.UpdateIndex(si.Index-1);
					}									
					Content.msSubItems.remove(this);
					msSubItemIndex--;			
					if(msSubItemIndex == 0) {
					  Content.CreateMelody();			
					  Content.Melody.CopyText(Melody);
					  Content.CreateIncipit();
					  Content.incipit.CopyText(incipit); //if there was incipit copy it to new incipit
					}
					gpMsItem.dispose();
					msItem1.getParentNode().removeChild(msItem1);
					PackAndSetExtends();	      	
	      }				
			}
			@Override	public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);					
			}				
		}		
		
		
		public void UpdateIndex(int i) {
			Index = i;
			CreateOrSetAttribute((Element)msItem1,"n","1."+Index);
			SetTextByIndex();
		}
		
		public void SetTextByIndex() {
			if(Index == 1) {
				gpMsItem.setText("První text");
			} else {
			  gpMsItem.setText("Pøítisk "+(Index-1));
			}
		}
		
		public TmsSubItem(Node parent, Composite p, TContent c) {
			msItem1 = parent;
			Content = c;
			Content.msSubItems.add(this);
			subGridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			subGridData.minimumWidth = 240;			
			
		
			Index = msSubItemIndex;
			defaultColor = SubItemsColors[Index % 6];
			
			gpMsItem = new Group(p, SWT.NONE); //gpMsDescription
			gpMsItem.setLayout(new FormLayout());
			//gpMsItem.setBackground(new Color(null,255,128,128));
			
			Composite composite = new Composite(gpMsItem, SWT.NONE);			
			composite.setLayout(new GridLayout(2,false));
			FormData formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);			
			
			SetTextByIndex();
			
			/*
			new Label (composite, SWT.NONE).setText("Pøítisk");
			new Label (composite, SWT.NONE).setText(new Integer(msSubItemIndex).toString());
			*/	
	    //locus
			Node locus	= CreateOrFindChildByName(msItem1, "locus");			
			new TXmlLocus(locus, composite, defaultColor);

			//title
			Node Title	= CreateOrFindChildByName(msItem1, "title");
			if(((Element)Title).getAttributeNode("type") != null) { //its another type of title
				Title = msItem1.insertBefore(document.createElement("title"), Title);				
			}
			Title = CreateOrFindTextChild(Title);
			new Label (composite, SWT.NONE).setText("Titul v pøepisu");
			Text textHeadingTitle = new Text (composite, SWT.BORDER);
			textHeadingTitle.setBackground(defaultColor);
			textHeadingTitle.setLayoutData(gridData);
			new TXmlText(Title, textHeadingTitle);			
			//title incipit						
			Node titleIncipit = FindChildByNameAndAttribute(msItem1, "title", "type", "incipit");
			if(titleIncipit == null) { //if doesn't exist create
				titleIncipit = msItem1.appendChild(document.createElement("title"));
				CreateOrFindAttribute((Element) titleIncipit, "type", "incipit");				
			}			
			new Label (composite, SWT.NONE).setText ("Incipit v pøepisu");
			Text textTitleIncipit = new Text (composite, SWT.BORDER);
			textTitleIncipit.setBackground(defaultColor);
			textTitleIncipit.setLayoutData(gridData);
			new TXmlText(CreateOrFindTextChild(titleIncipit), textTitleIncipit);			
  		//summary
			TSummary summary = new TSummary(msItem1, composite, defaultColor);
		  
			//multiline composite
			Composite multiLineComposite = new Composite(gpMsItem, SWT.NONE);
			formData = new FormData();			
			formData.top = new FormAttachment(summary.compositeAddNext);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			multiLineComposite.setLayoutData(formData);
			multiLineComposite.setLayout(new GridLayout(2, false));
			//incipit
			incipit = new TMultiLineNode("Incipit", "incipit", msItem1, multiLineComposite, defaultColor);
			incipit.CopyText(Content.incipit); //if there was incipit copy it to new incipit
			
  		//melody
			Melody = new TMelody(gpMsItem, multiLineComposite, msItem1, defaultColor);
			Melody.CopyText(Content.Melody);
			
			Button btDeleteSubItem = new Button(Melody.composite, SWT.PUSH);			
			btDeleteSubItem.setText("Odebrat tento pøítisk ...");
			btDeleteSubItem.addSelectionListener(new DeleteSubItem());		
			PackAndSetExtends();
		}		
	}
	
	class TMelody {
		Composite composite;
		FormData formData;
		Node qMelody;
		Node noteStrophes;
		TXmlCombo xmlComboMelodyNote; 
		TXmlText xmlTextMelody;
		TXmlCombo xmlComboNoteStrophes;
		
		private Combo comboMelodyNote;
		private Text textMelody;
		private Combo comboNoteStrophes;
		
		public void Dispose () {
			composite.dispose();
			PackAndSetExtends();
			Node parent = qMelody.getParentNode();
			parent.removeChild(qMelody);
			parent.removeChild(noteStrophes);
		}		
		
		public void CopyText(TMelody Source) {
			if(Source != null) {
			  xmlComboMelodyNote.modifyNodeText(Source.xmlComboMelodyNote.text.getText()); 
			  xmlTextMelody.modifyNodeText(Source.xmlTextMelody.text.getText());
			  xmlComboNoteStrophes.modifyNodeText(Source.xmlComboNoteStrophes.text.getText());
			}
		}
		
		private void Construct(Group gpParent, Composite sibling, Node parent) {
			GridLayout gridLayoutCompact = new GridLayout(2,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;
			GridData melodyGridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			melodyGridData.minimumWidth = 240;			
			//melody			
			composite = new Composite(gpParent, SWT.NONE); //gpMsContent
			formData = new FormData();			
			formData.top = new FormAttachment(sibling); //compositeAddSubItem
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);
			composite.setLayout(new GridLayout(2, false));
			
			qMelody = FindChildByNameAndAttribute(parent, "q", "type", "melody"); //msItem
			if(qMelody == null) { //if doesn't exist create
				qMelody = parent.appendChild(document.createElement("q"));
				CreateOrFindAttribute((Element) qMelody, "type", "melody");
			}
			
			Node qMelodyNote = CreateOrFindChildByName(qMelody, "note");
			
			new Label (composite, SWT.NONE).setText ("Poznámka k nápìvu");
			
			comboMelodyNote = new Combo (composite, SWT.NONE);
			comboMelodyNote.setItems (new String [] {"Bez nápìvu", "Zpívá se jako:", "Jako:"});
			comboMelodyNote.setLayoutData(melodyGridData);			 
			xmlComboMelodyNote = new TXmlCombo(CreateOrFindTextChild(qMelodyNote), comboMelodyNote);
						
			new Label (composite, SWT.NONE).setText ("Vlastní nápìv");
			
			textMelody = new Text (composite, SWT.BORDER);
			textMelody.setLayoutData(gridData);
			xmlTextMelody = new TXmlText(CreateOrFindTextChild(qMelody), textMelody);					
						
			//strophes
			new Label (composite, SWT.NONE).setText ("Sloky");			
			
			noteStrophes = FindChildByNameAndAttribute(parent, "note", "type", "strophes");
			if(noteStrophes == null) { //if doesn't exist create
				noteStrophes = parent.appendChild(document.createElement("note"));
				CreateOrFindAttribute((Element) noteStrophes, "type", "strophes");
				Node noteStrophesText = CreateOrFindTextChild(noteStrophes);
				noteStrophesText.setNodeValue("[x] slok");		
			}	
			
			Composite compositeNoteStrophes = new Composite(composite, SWT.NONE);
			compositeNoteStrophes.setLayout(gridLayoutCompact);						
			comboNoteStrophes = new Combo (compositeNoteStrophes, SWT.NONE);
			comboNoteStrophes.setItems (new String [] {"[x] slok", "x slok"});
			comboNoteStrophes.setLayoutData(melodyGridData);	
			new Label (compositeNoteStrophes, SWT.NONE).setText ("(neèíslované v hranatých závorkách)");
			xmlComboNoteStrophes = new TXmlCombo(CreateOrFindTextChild(noteStrophes), comboNoteStrophes);
			/* I don't remember what I suppose to do with this commented code
			Text textNoteStrophes = new Text (multiLineComposite, SWT.BORDER);
			textNoteStrophes.setLayoutData(gridData);
			new TXmlText(CreateOrFindTextChild(noteStrophes), textNoteStrophes);			
			*/			
		}
		
		public TMelody (Group gpParent, Composite sibling, Node parent) { 
			Construct(gpParent, sibling, parent);
		}

		public TMelody(Group gpParent, Composite sibling, Node parent,
				Color defaultColor) {
			Construct(gpParent, sibling, parent);
			comboMelodyNote.setBackground(defaultColor);
			textMelody.setBackground(defaultColor);
			comboNoteStrophes.setBackground(defaultColor);
		}
	};
	

	
	
	class TmsParts {
		Group gpTmsParts;
		Node msDescription;
		private int msPartIndex;
		Button btAddFirst;
		GridData gd;
		ArrayList<TOtherMsPart> msParts;
		Composite lParent;
		private Composite composite;
		
		class OpenMsPart implements SelectionListener {
			final Node msPartToOpen;
			public OpenMsPart(Node msPart) {
				msPartToOpen = msPart;		
			}
			@Override public void widgetSelected(SelectionEvent e) {
	      //look for open sub items		
				for (TXmlDocument xd : xmlSubDocuments) {
					if(xd.GetMsPart() == msPartToOpen) {
						TabItem lTabItem = xd.getTabItem();
						//show sub item
						MainHolder.getTabFolder().setSelection(lTabItem); //show new tab, no event!		
						MainHolder.FindSelectedXmlDocument(lTabItem);     //because missing select event!
	    			return;									
					}
				}
				//open new sub item
				new TXmlDocument(MainHolder, thisDocument, msPartToOpen);
			}				
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);					
			}				
		};		
		
		class DeleteMsPart implements SelectionListener {
			final TOtherMsPart thisToDelete;
			public DeleteMsPart(TOtherMsPart toDelete) {
				thisToDelete = toDelete;
			}
			@Override public void widgetSelected(SelectionEvent e) {
				
				MessageBox messageBox = new MessageBox(MainHolder.getShell(), SWT.ICON_WARNING
	          | SWT.YES | SWT.NO);
	      messageBox.setMessage("Pøívazek bude smazán. Pokraèovat?");
	      messageBox.setText("Odebrat?");
	      int response = messageBox.open();
	      if (response == SWT.YES) {
				
		      //look for sub items to close		
					for (TXmlDocument xd : xmlSubDocuments) {
						if(xd.GetMsPart() == thisToDelete.thisMsPart) {
							//close
							xd.closeDocument();
							break;
						}
					}
					//re-index and rearrange
					boolean found = false;
					Control prev = null;
					for (TOtherMsPart oms : msParts) {
						if (found) {
							oms.ChangeMyIndex(-1, true);
							oms.AttachTo(prev);
							prev = oms.container;
						} else {
							if(oms == thisToDelete) {
								found = true;
							} else {
							  prev = oms.container;
							}
						}	
					}		
										
  				FormData formData = new FormData();
					if(prev != null) formData.top = new FormAttachment(prev);
					formData.left = new FormAttachment(0,0);
					btAddFirst.setLayoutData(formData);
					
					//delete all				
					thisToDelete.dispose();
					PackAndSetExtends();
	      }
			}				
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);					
			}				
		};		
		
		class AddMsPart implements SelectionListener {
			final TOtherMsPart beforeThisAdd;
			public AddMsPart(TOtherMsPart toAdd) {
				beforeThisAdd = toAdd;
			}
			@Override public void widgetSelected(SelectionEvent e) {
				//find msPart
				
				Node msPart = document.createElement("msPart");	
				Node msIdno = CreateOrFindChildByName(msPart, "idno");
				msIdno = CreateOrFindTextChild(msIdno);				
				
				TOtherMsPart newMsPart;
				
				if(beforeThisAdd == null) { //last item
					int newIdx = msParts.size()+1; 
					msDescription.appendChild(msPart);
					msIdno.setTextContent(idnoXmlText.text.getText() + "/" + Integer.toString(newIdx));
					newMsPart = new TOtherMsPart(msPart, msParts.size() > 0 ? msParts.get(msParts.size()-1).container : null, newIdx);
					msParts.add(newMsPart);
				} else {
					int newIdx = msParts.indexOf(beforeThisAdd);
					msDescription.insertBefore(msPart, beforeThisAdd.thisMsPart);
					msIdno.setTextContent(idnoXmlText.text.getText() + "/" + Integer.toString(beforeThisAdd.myIndex));
					newMsPart = new TOtherMsPart(msPart, newIdx > 0 ? msParts.get(newIdx-1).container : null, beforeThisAdd.myIndex);
					msParts.add(msParts.indexOf(beforeThisAdd), newMsPart);
				}	
				//re-index and rearrange
				boolean found = false;
				Control prev = null;
				for (TOtherMsPart oms : msParts) {
					if (found) {
						oms.ChangeMyIndex(1, true);
						oms.AttachTo(prev);
						prev = oms.container;
					} else {
						found = oms == newMsPart;
					}					
					prev = oms.container;
				}
				if(prev != null) {					
					FormData formData = new FormData();
					formData.top = new FormAttachment(prev);
					formData.left = new FormAttachment(0,0);
					btAddFirst.setLayoutData(formData);
				}
				PackAndSetExtends();		
				SetWasEdited();
			}				
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);					
			}				
		}
		
		class TOtherMsPart {
			Composite container;
			final Node thisMsPart;
			int myIndex; 
			
			void ChangeMyIndex(int value, boolean relative) {
				if(relative) {
					myIndex = myIndex + value;
				} else {
					myIndex = value;
				}
				CreateOrSetAttribute((Element)thisMsPart,"n",Integer.toString(myIndex));
			}
			
			public void dispose() {
				msParts.remove(this);
				container.dispose();				
				thisMsPart.getParentNode().removeChild(thisMsPart);
				SetWasEdited();
			}

			void AttachTo(Control toAttach) {
				FormData formData = new FormData();
				if(toAttach != null) formData.top = new FormAttachment(toAttach);
				formData.left = new FormAttachment(0,0);
				formData.right = new FormAttachment(100,0);
				container.setLayoutData(formData);
			}
			
			public TOtherMsPart(Node msPart, Control sibling, int index) {
				thisMsPart = msPart;
				container = new Composite(composite , SWT.NONE);				
				AttachTo(sibling);
				container.setLayout(new GridLayout(4,false));
				
				Node msIdno = CreateOrFindChildByName(msPart, "idno");
				Node msIdnoText = CreateOrFindTextChild(msIdno);
				
				Text textIdno = new Text (container, SWT.BORDER);
				textIdno.setLayoutData(gd);
				new TXmlText(msIdnoText, textIdno); //TODO: Change tab name 					

				ChangeMyIndex(index, false);

				Button btOpen = new Button(container, SWT.PUSH);			
				btOpen.setText("Otevøít ...");
				btOpen.addSelectionListener(new OpenMsPart(msPart));
				
				Button btDelete = new Button(container, SWT.PUSH);			
				btDelete.setText("Smazat ...");
				btDelete.addSelectionListener(new DeleteMsPart(this));

				Button btAdd = new Button(container, SWT.PUSH);			
				btAdd.setText("Pøidat pøed ...");
				btAdd.addSelectionListener(new AddMsPart(this));
			}		
		}		
			
		
		
		public TmsParts(Node parent, Composite sibling) {
			msDescription = parent;
			msParts = new ArrayList<TOtherMsPart>();
			
			gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gd.widthHint = 240;
			
			lParent = sibling.getParent();
			
			//create visual part
			gpTmsParts = new Group(lParent, SWT.NONE); //gpMsDescription
			FormData formData = new FormData();
			formData.top = new FormAttachment(sibling);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			gpTmsParts.setLayoutData(formData);
			gpTmsParts.setLayout(new FormLayout());
			gpTmsParts.setText("Pøívazky");
			
			composite = new Composite(gpTmsParts, SWT.NONE);			
			formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);						
			composite.setLayout(new FormLayout());
			/*
			Composite compositeButton = new Composite(composite, SWT.NONE);
			formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			compositeButton.setLayoutData(formData);						
			compositeButton.setLayout(new GridLayout(1, false));
			*/			
			
			Control prevControl = null;
			
			//find all msParts
			Node msPart = GetFirstChildByName(msDescription, "msPart");
			while(msPart != null) {  			
				if(msPart.getNodeName().equals("msPart")) {
					msPartIndex++;
					
					TOtherMsPart Part = new TOtherMsPart(msPart, prevControl, msPartIndex); 
					msParts.add(Part); 
					prevControl = Part.container;
					
					/*
					Text textIdno = new Text (composite, SWT.BORDER);
					new TXmlText(msIdno, textIdno); //TODO: Change tab name 					
					
					CreateOrSetAttribute((Element)msPart,"n",Integer.toString(msPartIndex));
					Button btOpen = new Button(composite, SWT.PUSH);			
					btOpen.setText("Otevøít ...");
					btOpen.addSelectionListener(new OpenMsPart(msPart));
					
					Button btDelete = new Button(composite, SWT.PUSH);			
					btDelete.setText("Smazat ...");

					Button btAdd = new Button(composite, SWT.PUSH);			
					btAdd.setText("Pøidat za ...");
					*/
				}
				msPart = msPart.getNextSibling();
			}

			btAddFirst = new Button(composite, SWT.PUSH);
			btAddFirst.setText("Pøidat pøívazek ...");
			btAddFirst.addSelectionListener(new AddMsPart(null));

			formData = new FormData();
			formData.top = new FormAttachment(prevControl);
			formData.left = new FormAttachment(0,0);
			//formData.right = new FormAttachment(100,0);
			btAddFirst.setLayoutData(formData);			
			
			/**/
		}
	}
	
	class TContent {
		Node msDescription;
		GridData contGridData;
		Group gpMsContent;
		TMultiLineNode incipit;
		ArrayList<TmsSubItem> msSubItems;
		public TMelody Melody;
		Node msItem;
		Composite compositeAddSubItem;
		Composite multiLineComposite;
		//GridLayout gridLayoutCompact;
		public TContent(Node parent, Composite sibling) {
			msSubItems = new ArrayList<TmsSubItem>();
			msDescription = parent;
			
			GridLayout gridLayoutCompact = new GridLayout(2,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;			
			
			contGridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			contGridData.minimumWidth = 240; 
			
			/*gridLayoutCompact = new GridLayout(2,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;*/
			
			Node msContent	= CreateOrFindChildByName(msDescription, "msContents");
			gpMsContent = new Group(sibling.getParent(), SWT.NONE); //gpMsDescription
			FormData formData = new FormData();
			formData.top = new FormAttachment(sibling);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			gpMsContent.setLayoutData(formData);
			gpMsContent.setLayout(new FormLayout());
			
			Composite composite = new Composite(gpMsContent, SWT.NONE);			
			composite.setLayout(new GridLayout(2,false));
			formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);
			//msItem
			msItem = CreateOrFindChildByName(msContent, "msItem");
			CreateOrFindAttribute((Element) msItem, "n", "1").setNodeValue("1"); //must be 1 
	    //locus
			Node locus	= CreateOrFindChildByName(msItem, "locus");			
			new TXmlLocus(locus, composite);
			//title???
			//title incipit
			Node titleIncipit = FindChildByNameAndAttribute(msItem, "title", "type", "incipit");
			if(titleIncipit == null) { //if doesn't exist create
				titleIncipit = msItem.appendChild(document.createElement("title"));
				CreateOrFindAttribute((Element) titleIncipit, "type", "incipit");				
			}
			new Label (composite, SWT.NONE).setText ("Incipit první písnì");
			Text textTitleIncipit = new Text (composite, SWT.BORDER);
			textTitleIncipit.setLayoutData(gridData);
			new TXmlText(CreateOrFindTextChild(titleIncipit), textTitleIncipit);
			//summary
			TSummary summary = new TSummary(msItem, composite);
			
			//multiline composite
			multiLineComposite = new Composite(gpMsContent, SWT.NONE);
			formData = new FormData();			
			formData.top = new FormAttachment(summary.compositeAddNext);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			multiLineComposite.setLayoutData(formData);
			multiLineComposite.setLayout(new GridLayout(2, false));
			//rubric
			/*TMultiLineNode rubric = */new TMultiLineNode("Pøepis titulní strany", "rubric", msItem, multiLineComposite);
			//colofon
			/*TMultiLineNode colophon = */new TMultiLineNode("Kolofon", "colophon", msItem, multiLineComposite);
			
			/*
			 * There can be msItem 1.x here 
			 * */
			
			Node msSubItem = GetFirstChildByName(msItem, "msItem");
			
			//if there is msItem there should not be incipit 
			if(msSubItem == null) {
  			//incipit
				CreateIncipit(); // incipit = new TMultiLineNode("Incipit", "incipit", msItem, multiLineComposite);
			} else {
				incipit = null;
			}
			//msItems additional prints
			Composite msItemAdditions = new Composite(gpMsContent, SWT.NONE);
			formData = new FormData();			
			formData.top = new FormAttachment(multiLineComposite);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			msItemAdditions.setLayoutData(formData);
			RowLayout rowLayoutSubItems = new RowLayout ();
			rowLayoutSubItems.type = SWT.VERTICAL;			
			msItemAdditions.setLayout(rowLayoutSubItems);
						
			//msItems 
			compositeAddSubItem = new Composite(gpMsContent, SWT.NONE);
			formData = new FormData();			
			formData.top = new FormAttachment(msItemAdditions);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			compositeAddSubItem.setLayoutData(formData);
			compositeAddSubItem.setLayout(new GridLayout(1,false));
						
			while(msSubItem != null) {
				if(msSubItem.getNodeName().equals("msItem")) {
					msSubItemIndex++;
					CreateOrSetAttribute((Element)msSubItem,"n","1."+msSubItemIndex);
					new TmsSubItem(msSubItem, msItemAdditions, this);					
				}
				msSubItem = msSubItem.getNextSibling(); 
			}
			
			//Button to add next msItem
			Button btAddAddition = new Button(compositeAddSubItem, SWT.PUSH);			
			btAddAddition.setText("Pøidat pøítisk ...");
			btAddAddition.addSelectionListener(new AddSubItem(msItem, msItemAdditions, this));			
			
			//melody
			if(msSubItemIndex == 0) { 
				CreateMelody() ;//Melody = new TMelody(gpMsContent, compositeAddSubItem, msItem);
			} else { //if there is msItems there should not be melody, strophes ...
				Melody = null;				
			}
		}
		public void CreateIncipit() {
			incipit = new TMultiLineNode("Incipit", "incipit", msItem, multiLineComposite);  			
		}
		public void CreateMelody() {
			Melody = new TMelody(gpMsContent, compositeAddSubItem, msItem);	
		}
	}	
	
	class TXmlTextYearRangeExtension implements ModifyListener {
    Combo Caller;
    Composite ParentComp;
    Composite Envelop;
    Node parent;
    Label labelBeg, labelEnd;
    Text textBeg, textEnd;
    Node textNode;
    Button ButtonDel;
    
    class TXmlTextYearRangeModifyListener implements ModifyListener {
			@Override
			public void modifyText(ModifyEvent e) {
				SetWasEdited();
	      if(textBeg == null || textEnd == null) return;		
				if(textBeg.getText().trim().isEmpty() && textEnd.getText().trim().isEmpty()) {
					//if text is empty, delete
					if(textNode != null) {
						parent.removeChild(textNode.getParentNode());
						textNode = null;
					} 
				} else {
					textNode = CreateOrFindTextChild(CreateOrFindChildByName(parent, DATE)); 
					textNode.setNodeValue(textBeg.getText().trim()+" - "+textEnd.getText().trim());
				}
			}		
		}
    
    TXmlTextYearRangeModifyListener tyrml;
    
    void CreateYearRange() {
    	Envelop = new Composite(ParentComp, SWT.NONE);
    	Envelop.moveAbove(ButtonDel);
    	GridLayout gridLayout = new GridLayout(4, false); //add more columns to hold years
			gridLayout.marginHeight = gridLayout.marginWidth = 0;
			Envelop.setLayout(gridLayout);
    	labelBeg = new Label(Envelop, SWT.NONE);
    	labelBeg.setText("Od roku");
			textBeg = new Text(Envelop, SWT.BORDER);
			labelEnd = new Label(Envelop, SWT.NONE);
			labelEnd.setText("Do roku");
			textEnd = new Text(Envelop, SWT.BORDER);
			
			textNode = GetFirstChildByName(parent, DATE);
			if(textNode != null) {
				textNode = CreateOrFindTextChild(textNode);
				String [] s = textNode.getNodeValue().split("-");
				if(s.length > 0) textBeg.setText(s[0].trim());
				if(s.length > 1) textEnd.setText(s[1].trim());
			}
			textBeg.addModifyListener(tyrml);
			textEnd.addModifyListener(tyrml);			
			
			ParentComp.pack();
    }
    
    void DeleteYearRange() {
    	if(Envelop != null)  {
    	  Envelop.dispose();
    	}
    	Envelop = null;
    	ParentComp.pack();    	
    }
    
		public TXmlTextYearRangeExtension(Composite p, Combo c, Node n, Button b) {
			ParentComp = p;
			Caller = c;
			parent = n;
			ButtonDel = b;
			labelBeg = labelEnd = null; 
			textBeg = textEnd = null;
			tyrml = new TXmlTextYearRangeModifyListener();			
			modifyText(null);
		}
		
		@Override	public void modifyText(ModifyEvent e) {
			if(e != null) SetWasEdited();
			if(Caller.getText().equals("Datum")) {
				CreateYearRange();
			} else {
				DeleteYearRange();
			}
		}
		
	}
	
	class TXmlMultiLineText {
		Text text;
		Node masterNode;
		
		void LoadText() {
			String string = "";			
			Node child = masterNode.getFirstChild();
			while(child != null) {
				if(Node.TEXT_NODE == child.getNodeType()) {
					string += child.getNodeValue().trim();
				} else if(child.getNodeName() == "lb") {
					string += "\r\n";
				} else if(child.getNodeName() == "supl") {										
					Node suplChild = CreateOrFindTextChild(child);
					string += "["+suplChild.getNodeValue()+"]";
				}
			  child = child.getNextSibling();
			}
			text.setText(string.trim());
		}
		
		void SaveText() {
			DeleteOld();
			String [] byNewLine = text.getText().split("\r\n");
			Node lb = null;
			Boolean noSpace = true;
			for(String line : byNewLine) {
				String [] byBracket = line.split("[\\[\\]]");
				Boolean b = false; 
				for(String sub : byBracket) {
					if(b) {
						Node supl =  masterNode.appendChild(document.createElement("supl"));
						supl.appendChild(document.createTextNode(sub));
					} else {
						if(!noSpace) {
							sub = " " + sub;
						}
						masterNode.appendChild(document.createTextNode(sub));
						noSpace = sub.endsWith("=");
					}
					b = !b;					
				}
				lb = masterNode.appendChild(document.createElement("lb")); 
			}
			if(lb != null) {
			  masterNode.removeChild(lb); //remove last lb
			}	
			if(lastEditedMultiLineText == this) { //remove reference to self if it's already saved
				lastEditedMultiLineText = null;
			}
		}
		
		void DeleteOld() {
			Node child = masterNode.getFirstChild();
			while(child != null) {
				Node nextchild = child.getNextSibling();
				masterNode.removeChild(child);
				child = nextchild;
			}
		}
		
		void RegisterLastEdited() {
			lastEditedMultiLineText = this;
		}
		
		public TXmlMultiLineText(Node n, Text t) {
			text = t;
			masterNode = n;
			LoadText();
			text.addFocusListener(new FocusListener() {				
				@Override	public void focusLost(FocusEvent e) {
					SaveText();					
				}
				
				@Override	public void focusGained(FocusEvent e) {
					RegisterLastEdited(); 
				}
			});
			text.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					SetWasEdited();					
				}				
			});
		}		
	}
	
	class TMultiLineNode {
		TXmlMultiLineText xmlt;
		Color textColor;
		Text multiLineText;
		Node multiNode;
		Label l;
		public void Dispose() {
			multiLineText.dispose();
			l.dispose();
			PackAndSetExtends();
			Node parent = multiNode.getParentNode();
			parent.removeChild(multiNode);
		}
		//copy and save text from another MultiLineNode
		public void CopyText(TMultiLineNode Source) {
			if(Source != null) {
				xmlt.text.setText(Source.xmlt.text.getText());
				xmlt.SaveText();				
			}
		}
		
		private void Construct(String labelText, String nodeName, Node parent, Composite composite) {
			multiNode = CreateOrFindChildByName(parent, nodeName);
			/*
			GridData gridDataLabel = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
			gridDataLabel.widthHint = 120;
			*/
			l = new Label(composite, SWT.NONE);
			l.setText(labelText);
			//l.setLayoutData(gridDataLabel);
			multiLineText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
			GridData gridData = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
			gridData.widthHint = 480;
			gridData.heightHint = 120;
			multiLineText.setLayoutData(gridData);
			xmlt = new TXmlMultiLineText(multiNode, multiLineText);
		}		
		
		public TMultiLineNode(String labelText, String nodeName, Node parent, Composite composite, Color defaultColor) {
			Construct(labelText, nodeName, parent, composite);
			multiLineText.setBackground(defaultColor);
		}
		
		public TMultiLineNode(String labelText, String nodeName, Node parent, Composite composite) {
			Construct(labelText, nodeName, parent, composite);
		}
	}
	
	class TSummary {
		Node summary;
		Composite compositeParent;
		Composite compositeTerms;
		Composite compositeAddNext;
		GridData summGridData;
		GridData summGridData2;
		GridData summGridData3;
		GridData summGridData4;
		Boolean searchSummary = true;		
		Color bgColor;
		
		class TTerm {
			Composite composite;			
			public TTerm(final Node t, Boolean genre) {
				composite = new Composite(compositeTerms, SWT.NONE);
				if(genre) {					
					GridLayout gridLayout = new GridLayout(2, false);
					gridLayout.marginHeight = gridLayout.marginWidth = 0;					
					composite.setLayout(gridLayout);
					Label label = new Label(composite, SWT.NONE);
					label.setText("Žánr");
					label.setLayoutData(summGridData);
					Combo combo = new Combo (composite, SWT.NONE);
					combo.setBackground(bgColor);
					combo.setItems (new String [] {"Kramáøská píseò", "Kramáøský tisk", "Modlitba"});
					combo.setLayoutData(summGridData3);			 
					new TXmlCombo(CreateOrFindTextChild(t), combo);
				} else {
					GridLayout gridLayout = new GridLayout(4, false); //add more columns to hold years
					gridLayout.marginHeight = gridLayout.marginWidth = 0;
					composite.setLayout(gridLayout);
					Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
					combo.setBackground(bgColor);
					combo.setItems(TERM_TYPE_TEXT);
					combo.setLayoutData(summGridData);
					combo.setVisibleItemCount(TERM_TYPE_TEXT.length);
					Attr termType = CreateOrFindAttribute((Element) t, "type", "person_name_religion");	
					new TXmlAttrCombo(termType, combo, TERM_TYPE);					
					Text text = new Text(composite, SWT.BORDER);
					text.setBackground(bgColor);
					text.setLayoutData(summGridData2);
					new TXmlText(CreateFirstOrGetTextChild(t,true), text);	//110516
					Button removeTerm = new Button(composite, SWT.PUSH);
					removeTerm.setText("x");					
					combo.addModifyListener(new TXmlTextYearRangeExtension(composite,combo, t, removeTerm));
					removeTerm.addSelectionListener(new SelectionListener() {						
					@Override
					public void widgetSelected(SelectionEvent e) {
						SetWasEdited();
						composite.dispose();
						PackAndSetExtends();
						t.getParentNode().removeChild(t);
					}						
					@Override	public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}
					});	
				}
			}
		}
		
		public void CreateSummaryText(Node t) {
			Composite composite = new Composite(compositeTerms, SWT.NONE);
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginHeight = gridLayout.marginWidth = 0;					
			composite.setLayout(gridLayout);					
			Label label = new Label(composite, SWT.NONE);
			label.setText("Shrnutí");
			label.setLayoutData(summGridData);					
			Text summaryText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
			summaryText.setBackground(bgColor);
			summaryText.setLayoutData(summGridData4);
			new TXmlText(t, summaryText);
			searchSummary = false;
		}
		
		class AddTermButtonListener implements SelectionListener {
			Node summary;
			public AddTermButtonListener(Node n) {
				summary = n;
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				SetWasEdited();
				Node termGenre = summary.appendChild(document.createElement("term"));				
				CreateOrFindAttribute((Element) termGenre, "type", "genre");
				new TTerm(termGenre, false);
				PackAndSetExtends();
			}
			@Override	public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}			
		}
		
	  private void Construct(Node parent, Composite sibling) {
	  	compositeParent = sibling.getParent();
	  	summGridData = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
	  	summGridData.widthHint = 140;
	  	summGridData2 = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
	  	summGridData2.widthHint = 240;
	  	summGridData3 = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
	  	summGridData3.widthHint = 240;
	  	summGridData4 = new GridData (SWT.BEGINNING, SWT.CENTER, false, false);
	  	summGridData4.widthHint = 480;
	  	summGridData4.heightHint = 120;
	  	summary = CreateOrFindChildByName(parent, "summary");	  	
	  	compositeTerms = new Composite(compositeParent, SWT.NONE);
			FormData formData = new FormData();			
			formData.top = new FormAttachment(sibling);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			compositeTerms.setLayoutData(formData);
			/*
			FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
			fillLayout.spacing = 5;
			fillLayout.marginHeight = fillLayout.marginWidth = 5; 
			compositeTerms.setLayout(fillLayout);
			*/
			RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
			rowLayout.spacing = 5;
			rowLayout.marginHeight = rowLayout.marginWidth = 5; 
			compositeTerms.setLayout(rowLayout);
			
			Node termGenre = FindChildByNameAndAttribute(summary, "term", "type", "genre");
			if(termGenre == null) { //if doesn't exist create
				termGenre = summary.appendChild(document.createElement("term"));				
				CreateOrFindAttribute((Element) termGenre, "type", "genre");				
			}
			new TTerm(termGenre, true);
  	  // get more terms	
			
	  	Node term = termGenre.getNextSibling();				
			while(term != null) {
				if(searchSummary && term.getNodeType() == Node.TEXT_NODE) {
					CreateSummaryText(term); //this sets searchSummary = false
				}								
			  if(term.getNodeName().equals("term")) {
			  	if(searchSummary) { //if there was no text node after first term insert summary text here
			  		Node t = document.createTextNode("");
			  		summary.insertBefore(t, term);
			  		CreateSummaryText(t); //this sets searchSummary = false 
			  	}
			  	new TTerm(term, false);
			  }
			  term = term.getNextSibling();
			}
			if(searchSummary) { //if there was no other term insert summary text behind genre
	  		Node t = document.createTextNode("");
	  		summary.insertBefore(t, termGenre.getNextSibling());
	  		CreateSummaryText(t); //this sets searchSummary = false 				
			}
			
			//button for adding terms
	  	compositeAddNext = new Composite(compositeParent, SWT.NONE);
			formData = new FormData();			
			formData.top = new FormAttachment(compositeTerms);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			compositeAddNext.setLayoutData(formData);			
			compositeAddNext.setLayout(new GridLayout(2, false));
			
			Label labelAddTerm = new Label (compositeAddNext, SWT.NONE);
			labelAddTerm.setText ("Další klíèové slovo");
			Button buttonAddTerm = new Button(compositeAddNext, SWT.PUSH);
			buttonAddTerm.setText("pøidat ...");
			buttonAddTerm.addSelectionListener(new AddTermButtonListener(summary));
				  		  	
	  }

	  public TSummary(Node parent, Composite sibling) {
	  	bgColor = MainHolder.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	  	Construct(parent, sibling);
	  }
	  
		public TSummary(Node parent, Composite sibling, Color defaultColor) {
			bgColor = defaultColor;
			Construct(parent, sibling);
		}
	}
	
	class TResponsibilities {
		private Composite compositeResp;
		public TResponsibilities(Node parent, Composite sibling) {
			Node printer = null; //this is exception
			
			Composite compositePrinter = new Composite(sibling.getParent(), SWT.NONE); //gpMsHeading
			FormData formData = new FormData();			
			formData.top = new FormAttachment(sibling);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			compositePrinter.setLayoutData(formData);
			compositePrinter.setLayout(new FillLayout());
				
  		compositeResp = new Composite(sibling.getParent(), SWT.NONE);
			formData = new FormData();			
			formData.top = new FormAttachment(compositePrinter);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			compositeResp.setLayoutData(formData);
			compositeResp.setLayout(new FillLayout(SWT.VERTICAL));
			
			dummyLabel = new Label(compositeResp, SWT.NONE);		
			
			Node respStmt = parent.getFirstChild();
			while(respStmt != null) {
				if(respStmt.getNodeName().equals("respStmt")) {
				  Node respStmtName = GetFirstChildByName(respStmt, "name");
				  if(respStmtName != null) {
					  Attr attrRole = ((Element) respStmtName).getAttributeNode("role");
					  if(attrRole != null && attrRole.getValue().equals("printer")) {
					  	printer = respStmt; 
					  } else {
					  	//create special role
					  	new TResponsibilitiesStatement(compositeResp, respStmt);
					  }
				  }
				}
				respStmt = respStmt.getNextSibling();
			}
						
			new TPrinter(compositePrinter, printer, parent);
			
			GridLayout gridLayoutCompact = new GridLayout(3,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;
			GridData lgridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			lgridData.minimumWidth = 75;		
			
			Composite composite = new Composite(sibling.getParent(),SWT.NONE);
			composite.setLayout(gridLayoutCompact);
			formData = new FormData();
			formData.top = new FormAttachment(compositeResp);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);
  		//button for adding responsibilities
			Label labelAddResp = new Label (composite, SWT.NONE);
			labelAddResp.setText ("Další odpovìdnost");
			Button buttonAddResp = new Button(composite, SWT.PUSH);
			buttonAddResp.setText("pøidat ...");
			buttonAddResp.addSelectionListener(new AddRespButtonListener(compositeResp, parent));			
		}
	}
	
	void CreateTitle(Node parent, Composite compParent) {
		Node msHeadingTitle	= CreateOrFindChildByName(parent, "title");
		msHeadingTitle = CreateOrFindTextChild(msHeadingTitle);
		new Label (compParent, SWT.NONE).setText (isComposite ? "Název konvolutu" : "Titul v pøepisu");
		Text textHeadingTitle = new Text (compParent, SWT.BORDER);
		textHeadingTitle.setLayoutData(gridData);
		new TXmlText(msHeadingTitle, textHeadingTitle);
	}
	
	class TAuthors {
		private Composite compositeAuth;
		public TAuthors(Node parent, Composite compParent) {
			compositeAuth = compParent;
			if(!isComposite) { //author
				Node msHeadingAutor	= CreateOrFindChildByName(parent, AUTHOR);
				Node msHeadingAutorText = CreateOrFindTextChild(msHeadingAutor);
				new Label (compositeAuth, SWT.NONE).setText ("Autor");
				Text textHeadingAutor = new Text (compositeAuth, SWT.BORDER);
				textHeadingAutor.setLayoutData(gridData);
				new TXmlText(msHeadingAutorText, textHeadingAutor);
				//button for adding authors
				Label labelAddAuthor = new Label (compositeAuth, SWT.NONE);
				labelAddAuthor.setText ("Další autor");
				Button buttonAddAuthor = new Button(compositeAuth, SWT.PUSH);
				buttonAddAuthor.setText("pøidat ...");
				buttonAddAuthor.addSelectionListener(new AddAutorButtonListener(compositeAuth, labelAddAuthor, parent));			
				//get more authors	
				msHeadingAutor = msHeadingAutor.getNextSibling();				
				while(msHeadingAutor != null) {
				  if(msHeadingAutor.getNodeName().equals(AUTHOR)) {
				    new TOtherAuthor(compositeAuth, labelAddAuthor, msHeadingAutor);	
				  }
			    msHeadingAutor = msHeadingAutor.getNextSibling();
				}			
			}			
		}
	}
	
	private void AddLanguage(Node Parent, Composite composite) {
		Node textLang = CreateOrFindChildByName(Parent, "textLang"); //msHeading
		new Label(composite, SWT.NONE).setText("Jazyk dokumentu");
		Combo comboLang = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboLang.setItems(new String[] {"èesky", "nìmecky", "latinsky", "slovensky", "polsky"});
		Attr langAttr = CreateOrFindAttribute((Element) textLang, "langKey", "CZE");
		textLang = CreateOrFindTextChild(textLang);
		comboLang.setText(textLang.getNodeValue());
		new TXmlNodeAttrCombo(textLang, langAttr, comboLang, LANGS_VAL, null);			
	}	
	
	class TTeiHeaderFrame {	  
	  Node  parentNode;
		public TTeiHeaderFrame(Node p) {
	    parentNode = p;
	    FormData formData;
			GridLayout gridLayout = new GridLayout (2, false);
			GridLayout gridLayoutCompact = new GridLayout(2,false);
			gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;
	    //file description
			Node fileDesc = CreateOrFindChildByName(parentNode, "fileDesc");
			//title stmt
			Node titleStmt = CreateOrFindChildByName(fileDesc, "titleStmt");    
	    Group gpTitleStmt = new Group(compositeDoc, SWT.NONE);
	    gpTitleStmt.setText("Popis záznamu");
	    gpTitleStmt.setLayout(gridLayout);
	    formData = new FormData();
	    formData.left = new FormAttachment(0,0);
	    formData.right = new FormAttachment(100,0);
	    gpTitleStmt.setLayoutData(formData);
			Node title = CreateOrFindChildByName(titleStmt, "title");
			title = CreateOrFindTextChild(title);
			new Label (gpTitleStmt, SWT.NONE).setText ("Název dokumentu");
			Text textTitle = new Text (gpTitleStmt, SWT.BORDER);
			gridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
			gridData.minimumWidth = 600; 
			textTitle.setLayoutData(gridData);
			new TXmlText(title, textTitle);
			
			Node author = CreateOrFindChildByName(titleStmt, AUTHOR);
			author = CreateOrFindTextChild(author);
			new Label (gpTitleStmt, SWT.NONE).setText ("Autor dokumentu");
			Text textAuthor = new Text (gpTitleStmt, SWT.BORDER);
			textAuthor.setLayoutData(gridData);			
			new TXmlText(author, textAuthor);
			/*
			Combo comboAuthor = new Combo (gpTitleStmt, SWT.NONE);
			comboAuthor.setItems (new String [] {"Item 1", "Item 2", "Item 2"});
			comboAuthor.setLayoutData(gridData);			
			new TXmlCombo(author, comboAuthor);			
			*/
			
			//publication stmt
			Node publicationStmt = CreateOrFindChildByName(fileDesc, "publicationStmt");
			publicationStmt = CreateOrFindChildByName(publicationStmt, "p");
			publicationStmt = CreateOrFindTextChild(publicationStmt);
			new Label (gpTitleStmt, SWT.NONE).setText ("Instituce zpracovávající");
			Text textPublicationStmt = new Text (gpTitleStmt, SWT.BORDER);
			textPublicationStmt.setLayoutData(gridData);
			new TXmlText(publicationStmt, textPublicationStmt);   	  	
						
			//---------------------------------------------------------------------------------------
			//source description
			Node sourceDesc = CreateOrFindChildByName(fileDesc, "sourceDesc");		
			Group gpSourceDesc = new Group(compositeDoc, SWT.NONE);
			FormLayout formLayout = new FormLayout();
			formLayout.marginHeight = formLayout.marginWidth = 4; 
			formLayout.spacing = 4;
			gpSourceDesc.setText("Popis zdroje");			
			gpSourceDesc.setLayout(formLayout);
	    formData = new FormData();
	    formData.left = new FormAttachment(0,0);
	    formData.right = new FormAttachment(100,0);
	    formData.top = new FormAttachment(gpTitleStmt,5);
	    gpSourceDesc.setLayoutData(formData);			
			/*FormData data = new FormData ();
			data.top = new FormAttachment (gpTitleStmt, 0, SWT.DEFAULT);
			gpSourceDesc.setLayoutData(data);*/
		
			
			Node recordDesc = CreateOrFindChildByName(sourceDesc, "p");
			recordDesc = CreateOrFindTextChild(recordDesc);
			Composite composite = new Composite(gpSourceDesc, SWT.NONE);
			composite.setLayout(gridLayoutCompact);
			formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);
			Label label = new Label (composite, SWT.NONE);
			label.setText ("Typ záznamu");
			Text textRecordDesc = new Text (composite, SWT.BORDER);
			textRecordDesc.setLayoutData(gridData);
			new TXmlText(recordDesc, textRecordDesc);			
			
		  //---------------------------------------------------------------------------------------
			//msDescription			
			Node msDescription = CreateOrFindChildByName(sourceDesc, "msDescription");
			
			Group gpMsDescription = new Group(gpSourceDesc, SWT.NONE);
			formData = new FormData();
			formData.top = new FormAttachment(composite);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);			
			gpMsDescription.setLayoutData(formData);
			gpMsDescription.setLayout(formLayout);
			
			composite = new Composite(gpMsDescription, SWT.NONE);
			composite.setLayout(gridLayoutCompact);
			formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);
			
			new Label (composite, SWT.NONE).setText ("Typ dokumentu");
			Combo comboDocType = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			comboDocType.setItems(new String[] {"Jednotlivý tisk", "Konvolut"});
			Attr docStatus = CreateOrFindAttribute((Element) msDescription, "status", "uni");
			/*
			if(docStatus.getValue().equals("uni")) {
			  comboDocType.select(0);
			} else {
			  comboDocType.select(1);
			}
			*/
			new TXmlAttrCombo(docStatus, comboDocType, new String [] {"uni", "compo"});
			isComposite = comboDocType.getSelectionIndex() > 0;
			comboDocType.setEnabled(false); //it can't be changed!!
			
			//--------------------------------------------------------------------------
			//msIdentifier
			Node msIdentifier = CreateOrFindChildByName(msDescription, "msIdentifier");
			
			Group gpMsIdentifier = new Group(gpMsDescription, SWT.NONE);
			formData = new FormData();
			formData.top = new FormAttachment(composite);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			gpMsIdentifier.setLayoutData(formData);
			gpMsIdentifier.setLayout(gridLayout);
			
			//country
			Node country = CreateOrFindChildByName(msIdentifier, "country");
			new Label (gpMsIdentifier, SWT.NONE).setText ("Stát uložení");
			Combo comboCountry = new Combo(gpMsIdentifier, SWT.DROP_DOWN | SWT.READ_ONLY);
			comboCountry.setItems(new String[] {"Èesko", "Slovensko"});
			Attr countryAttr = CreateOrFindAttribute((Element) country, "reg", "CZ");
			country = CreateOrFindTextChild(country);
			comboCountry.setText(country.getNodeValue());
			new TXmlNodeAttrCombo(country, countryAttr, comboCountry, new String [] {"CZ", "SK"}, null);
			//settlement
			Node settlement = CreateOrFindChildByName(msIdentifier, "settlement");
			settlement = CreateOrFindTextChild(settlement);
			new Label (gpMsIdentifier, SWT.NONE).setText ("Mìsto uložení");
			Text textSettlement = new Text (gpMsIdentifier, SWT.BORDER);
			textSettlement.setLayoutData(gridData);
			new TXmlText(settlement, textSettlement);
			//repository
			Node repository = CreateOrFindChildByName(msIdentifier, "repository");
			repository = CreateOrFindTextChild(repository);
			new Label (gpMsIdentifier, SWT.NONE).setText ("Instituce uložení");
			Text textRepository = new Text (gpMsIdentifier, SWT.BORDER);
			textRepository.setLayoutData(gridData);
			new TXmlText(repository, textRepository);			
			//idno
			Node idno = CreateOrFindChildByName(msIdentifier, "idno");
			idno = CreateOrFindTextChild(idno);
			new Label (gpMsIdentifier, SWT.NONE).setText ("Identifikaèní èíslo");
			Text textIdno = new Text (gpMsIdentifier, SWT.BORDER);
			textIdno.setLayoutData(gridData);
			idnoXmlText = new TXmlText(idno, textIdno);	

			//msHeading
			msHeading	= CreateOrFindChildByName(msDescription, "msHeading"); //needed before History for composite			
			//-------------------------------------------------------------------			
			THistory History = new THistory(msDescription, gpMsIdentifier);			
			//-------------------------------------------------------------------

			Group gpMsHeading = new Group(gpMsDescription, SWT.NONE);
			formData = new FormData();
			formData.top = new FormAttachment(History.gpHistory);//(gpMsIdentifier);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			gpMsHeading.setLayoutData(formData);
			gpMsHeading.setLayout(formLayout);
			
			composite = new Composite(gpMsHeading, SWT.NONE);
			composite.setLayout(gridLayoutCompact);
			formData = new FormData();
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);
			composite.setLayoutData(formData);
			
			//title
			CreateTitle(msHeading, composite);
			/*
			Node msHeadingTitle	= CreateOrFindChildByName(msHeading, "title");
			msHeadingTitle = CreateOrFindTextChild(msHeadingTitle);
			new Label (composite, SWT.NONE).setText (isComposite ? "Název konvolutu" : "Titul v pøepisu");
			Text textHeadingTitle = new Text (composite, SWT.BORDER);
			textHeadingTitle.setLayoutData(gridData);
			new TXmlText(msHeadingTitle, textHeadingTitle);
			*/
			//author
			new TAuthors(msHeading, composite);
			/*
			if(!isComposite) { //author
				Node msHeadingAutor	= CreateOrFindChildByName(msHeading, AUTHOR);
				Node msHeadingAutorText = CreateOrFindTextChild(msHeadingAutor);
				new Label (composite, SWT.NONE).setText ("Autor");
				Text textHeadingAutor = new Text (composite, SWT.BORDER);
				textHeadingAutor.setLayoutData(gridData);
				new TXmlText(msHeadingAutorText, textHeadingAutor);
				//button for adding authors
				Label labelAddAuthor = new Label (composite, SWT.NONE);
				labelAddAuthor.setText ("Další autor");
				Button buttonAddAuthor = new Button(composite, SWT.PUSH);
				buttonAddAuthor.setText("pøidat ...");
				buttonAddAuthor.addSelectionListener(new AddAutorButtonListener(composite, labelAddAuthor, msHeading));			
				//get more authors	
				msHeadingAutor = msHeadingAutor.getNextSibling();				
				while(msHeadingAutor != null) {
				  if(msHeadingAutor.getNodeName().equals(AUTHOR)) {
				    new TOtherAuthor(composite, labelAddAuthor, msHeadingAutor);	
				  }
			    msHeadingAutor = msHeadingAutor.getNextSibling();
				}			
			}
			*/
			//responsibilities
			
			new TResponsibilities(msHeading, composite);
			
			/*
			composite = new Composite(gpMsHeading, SWT.NONE);
			composite.setLayout(gridLayout);
			formData = new FormData();			
			formData.top = new FormAttachment(composite);
			formData.left = new FormAttachment(0,0);
			formData.right = new FormAttachment(100,0);	
			composite.setLayoutData(formData);					
			*/
			
			AddLanguage(msHeading, composite);
			/*
			Node textLang = CreateOrFindChildByName(msHeading, "textLang");
			new Label(composite, SWT.NONE).setText("Jazyk dokumentu");
			Combo comboLang = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			comboLang.setItems(new String[] {"èesky", "nìmecky", "latinsky", "slovensky", "polsky"});
			Attr langAttr = CreateOrFindAttribute((Element) textLang, "langKey", "CZE");
			textLang = CreateOrFindTextChild(textLang);
			comboLang.setText(textLang.getNodeValue());
			new TXmlNodeAttrCombo(textLang, langAttr, comboLang, LANGS_VAL, null);	
			*/		
		  //-------------------------------------------------------------------
			//msContent
			TContent Content = null;
			TmsParts msParts = null;
			if(isComposite) { //each msPart has own msContent
				//process msPart
				msParts = new TmsParts(msDescription, gpMsHeading);				
			} else {
			  Content = new TContent(msDescription, gpMsHeading);
			}
			//-------------------------------------------------------------------
			//physDesc 
			TPhysDesc PhysDesc = new TPhysDesc(msDescription, isComposite ? msParts.gpTmsParts : Content.gpMsContent);
			//-------------------------------------------------------------------
			//additional			
			/*TAdditional Additional = */new TAdditional(msDescription, PhysDesc.gpPhysDesc);
			//-------------------------------------------------------------------
			//profileDesc
			// This part only adds 5 langs by LANGS_VAL
			Node profileDesc = CreateOrFindChildByName(parentNode, "profileDesc");
			Node langUsage = CreateOrFindChildByName(profileDesc, "langUsage");
			//LANGS_VAL_LONG
			for(int i = 0; i < LANGS_VAL.length; i++) {
			  Node language = FindChildByNameAndAttribute(langUsage, "language", "id", LANGS_VAL[i]);
			  if(language == null) {
			  	language = langUsage.appendChild(document.createElement("language"));
					CreateOrFindAttribute((Element) language, "id", LANGS_VAL[i]);
					language = CreateFirstOrFindTextChild(language);
					language.setNodeValue(LANGS_VAL_LONG[i]);					
			  }
			}				
	  }
	}
	
	TTeiHeaderFrame TeiHeaderFrame;
	
	protected void finalize() {
		//dispose colors
    for(int i = 0; i < 6; i++) {
    	SubItemsColors[i].dispose();
    }
	}
	
  public void SetWidth(Control c, int i) {
		org.eclipse.swt.graphics.Rectangle temp = c.getBounds();
		temp.width = i;
		c.setBounds(temp);	
	}

  private void Init() {
  	msSubItemIndex = 0; 
  	WasEdited = false;
  	lastEditedMultiLineText = null;
  	myMsPart = null;
  	masterDocument = null;
  	
  	SubItemsColors = new Color [6];
		SubItemsColors[0] = new Color(MainHolder.getDisplay(),255,224,224);
		SubItemsColors[1] = new Color(MainHolder.getDisplay(),255,255,224);
		SubItemsColors[2] = new Color(MainHolder.getDisplay(),224,255,224);
		SubItemsColors[3] = new Color(MainHolder.getDisplay(),224,255,255);
		SubItemsColors[4] = new Color(MainHolder.getDisplay(),224,224,255);
		SubItemsColors[5] = new Color(MainHolder.getDisplay(),255,224,255);
  }
  
  private void AfterInit() {
	  FormLayout formLayout = new FormLayout (); 
	  formLayout.marginHeight = formLayout.marginWidth = 8;
	  compositeDoc.setLayout(formLayout);
	  
	  MainHolder.getTabFolder().setSelection(tabItem); //show new tab, no event!		
	  MainHolder.FindSelectedXmlDocument(tabItem);     //because missing select event!	   	
  }
  
  
  public TXmlDocument(TMainHolder mh, TXmlDocument master, Node yourMsPart) {
  	MainHolder = mh;  	
  	thisDocument = this;
  	document = master.document;
  	Init();
  	
  	mh.AddXmlDocument(this); //register to holder
  	
  	xmlSubDocuments = null;
  	masterDocument = master;
  	masterDocument.xmlSubDocuments.add(this); //register at master
  	myMsPart = yourMsPart;
		msHeading = myMsPart;  	
  	
  	tabItem = new TabItem(mh.getTabFolder(), 0); 
  	
		Node msIdno = GetFirstChildByName(myMsPart, "idno");
		msIdno = CreateOrFindTextChild(msIdno);
  	
		tabItem.setText(msIdno != null ? msIdno.getNodeValue() : "Nedefinováno");
		tabItem.setToolTipText(masterDocument.documentUrl);	
  	
  	scrolledComposite = new ScrolledComposite(MainHolder.getTabFolder(), SWT.H_SCROLL | SWT.V_SCROLL ); //| SWT.BORDER
	  compositeDoc = new Composite(scrolledComposite, SWT.NONE);
	  
	  AfterInit();
	 	  
	  FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = formLayout.marginWidth = 4; 
		formLayout.spacing = 4;
	  
	  Group gpMsPart= new Group(compositeDoc, SWT.NONE);
	  //gpMsPart.setText("test");
		gpMsPart.setLayout(new FillLayout(SWT.HORIZONTAL));	  
		FormData formData = new FormData();
		//formData.top = new FormAttachment(0,0);
		formData.left = new FormAttachment(0,0);
		formData.right = new FormAttachment(100,0);			
		gpMsPart.setLayoutData(formData);
		
	  Button btBack = new Button(gpMsPart, SWT.PUSH);
	  btBack.setText("Návrat do konvolutu ...");
	  btBack.addSelectionListener(new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
        TabItem lTabItem = masterDocument.getTabItem();
				//show master document
				MainHolder.getTabFolder().setSelection(lTabItem); //show new tab, no event!		
				MainHolder.FindSelectedXmlDocument(lTabItem);     //because missing select event!
			}				
			@Override	public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);	
			}
		});

	  
	  
		//new Label(gpMsPart, SWT.NONE).setText("pokus");
		
  	//-------------------------------------------------------------------
		//history
		THistory History = new THistory(myMsPart, gpMsPart);	  
	  
		GridLayout gridLayoutCompact = new GridLayout(2,false);
		gridLayoutCompact.marginHeight = gridLayoutCompact.marginWidth = 0;
		GridData lgridData = new GridData (SWT.BEGINNING, SWT.CENTER, true, false);
		lgridData.minimumWidth = 75;
		
    Group gpResponsibilities = new Group(compositeDoc, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(History.gpHistory);
		formData.left = new FormAttachment(0,0);
		formData.right = new FormAttachment(100,0);
		gpResponsibilities.setLayoutData(formData);
		gpResponsibilities.setLayout(formLayout);
		
		Composite composite = new Composite(gpResponsibilities, SWT.NONE);
		composite.setLayout(gridLayoutCompact);
		formData = new FormData();
		formData.left = new FormAttachment(0,0);
		formData.right = new FormAttachment(100,0);
		composite.setLayoutData(formData);
		
		Node msContent	= CreateOrFindChildByName(myMsPart, "msContents");
		Node msItem = CreateOrFindChildByName(msContent, "msItem");

		
  	//title
		CreateTitle(msItem, composite);
		
		//-------------------------------------------------------------------
		//authors		
		new TAuthors(msItem, composite);		
		
		//lang
		AddLanguage(msItem, composite);		
		
		//-------------------------------------------------------------------
		//responsibilities
		new TResponsibilities(msItem, composite);		
		
  	//---------------------------------------------------------------------------------------
		//Content							
		TContent Content = new TContent(myMsPart, gpResponsibilities);
	  
  	//-------------------------------------------------------------------
		//physDesc 
		TPhysDesc PhysDesc = new TPhysDesc(myMsPart, Content.gpMsContent);
		
  	//-------------------------------------------------------------------
  	//additional			
		TAdditional Additional = new TAdditional(myMsPart, PhysDesc.gpPhysDesc);		  	
		
	  //...dopsat!!!
		PackAndSetExtends();
		scrolledComposite.setContent(compositeDoc);			
		tabItem.setControl(scrolledComposite);
  }
    
	public TXmlDocument(TMainHolder mh, String url) {
		MainHolder = mh;
  	thisDocument = this;		
		Init();
		
		xmlSubDocuments = new ArrayList<TXmlDocument>(); //array for msParts
						
		mh.AddXmlDocument(this); //register to holder		
		//create path
		tabItem = new TabItem(mh.getTabFolder(), 0); 
		
		url = SetDocumentUrl(url);
		
  	scrolledComposite = new ScrolledComposite(MainHolder.getTabFolder(), SWT.H_SCROLL | SWT.V_SCROLL ); //| SWT.BORDER
	  compositeDoc = new Composite(scrolledComposite, SWT.NONE);

	  AfterInit();
		
	  try {
	  	File f = new File(url);
			document = mh.documentBuilder.parse(f);
			if(document != null) {
				documentType = document.getDoctype();
				if(documentType == null || documentType.getName() != SUPPORTED_DOC_TYPE) {
					MessageBox messageBox = new MessageBox(mh.getShell(), SWT.ICON_WARNING | SWT.OK);
		      messageBox.setMessage("Dokument není správného typu.");
		      messageBox.setText("Varování");
		      messageBox.open();
				} 
				Node node = document.getElementsByTagName(SUPPORTED_DOC_TYPE).item(0);
				if(node != null) { //we have the basic element, now lets rock
					Node teiHeader = CreateOrFindChildByName(node, TEI_HEADER);
					new TTeiHeaderFrame(teiHeader);
				} 
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PackAndSetExtends();
		scrolledComposite.setContent(compositeDoc);			
		tabItem.setControl(scrolledComposite);
	}
	
  public void SetWasEdited() {
  	if(masterDocument == null) {
  	  tabItem.setText(documentName+"*");
    	WasEdited = true;  	  
  	} else {
  		masterDocument.SetWasEdited();
  	}
  }
  
  public void ResetWasEdited() {
  	if(masterDocument == null) {  	
  	  tabItem.setText(documentName);
  	  WasEdited = false;
  	} else {
  		masterDocument.ResetWasEdited();
  	}
  }
  
  public boolean GetWasEdited() {
  	return WasEdited;
  }
  
	public String SetDocumentUrl(String url) {
		boolean New = url.isEmpty();   
				
		if(New) {
			url = MainHolder.getGlobalSettings().NewFilePath;
		} 
		documentUrl = url;
	  //get file name
		File documentFile = new File(url); 
		documentName = documentFile.getName();
  	//save last used path	
		tabItem.setText(documentName);
		tabItem.setToolTipText(documentUrl);		
		if(New) {
			documentUrl = "";
		} else {
			MainHolder.getGlobalSettings().LastPath = documentFile.getParent();
		}
		return url;
	}  
  
	public Control GetFirstControl(Composite parent) {
		Control [] c = parent.getChildren();
		if(c.length > 0) {
			return c[0];
		}
		return null;
	}

	public void saveDocument(String newUrl) {
		if(lastEditedMultiLineText != null) {
		  lastEditedMultiLineText.SaveText();
		}
		
		if(newUrl == null) { 
			newUrl = documentUrl;
		} else {
			SetDocumentUrl(newUrl);
		}
		 
		//delete backup file
		File fBackup = new File(newUrl+".bak");
		if(fBackup.exists()) {
			fBackup.delete();
		}
		
		//create backup file
		File fOriginal = new File(newUrl);
		fOriginal.renameTo(fBackup);		
		
		//transform to output file
		//TODO: Indents, nice XML, ...
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "masterx.dtd"); //TEI.2 "masterx.dtd"
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			Result result = new StreamResult(new File(newUrl));
			Source source = new DOMSource(document);
			transformer.transform(source, result);
			ResetWasEdited();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}		
	}
	
	public void closeDocument() {
		//save file
		if(WasEdited) {
      MessageBox messageBox = new MessageBox(MainHolder.getShell(), SWT.ICON_QUESTION
          | SWT.YES | SWT.NO);
      messageBox.setMessage("Záznam '"+documentName+"' není uložen. Uložit?");
      messageBox.setText("Uložit?");
      int response = messageBox.open();
      if (response == SWT.YES) {
      	if(getDocumentUrl().isEmpty()) {
      		saveDocumentAs(); 
      	} else {
			    saveDocument(null);
      	}
      }	
		}
		//close all sub documents
		if(masterDocument == null) {
			if(xmlSubDocuments != null) for (TXmlDocument xd : xmlSubDocuments) {
				xd.closeDocument();
			}
		} else {
			masterDocument.xmlSubDocuments.remove(this);
		}
		//remove reference
		MainHolder.RemoveXmlDocument(this);
		//remove tabItem
		tabItem.dispose();
	}
	
	public TabItem getTabItem() {
	  return tabItem;
	}
	
	public String getName() {
		return documentName;
	}

	public String getDocumentUrl() {
		return documentUrl;
	}
  
	//save document as
	public void saveDocumentAs() {
		FileDialog fd = new FileDialog(MainHolder.getShell(), SWT.SAVE);
    fd.setText("Uložit jako");
    fd.setFilterPath(MainHolder.getGlobalSettings().LastPath);//"C:/");
    fd.setOverwrite(true);
    String[] filterExt = { "*.xml"};
    fd.setFilterExtensions(filterExt);
    String selected = fd.open();
    if(selected != null) {      		     		
		  saveDocument(selected);
		  MainHolder.getShell().setText(MainHolder.SHELL_TEXT + " : " + getName());
    }		
	}

}
