package ij.gui;
import ij.*;
import ij.plugin.frame.Recorder;
import ij.plugin.ScreenGrabber;
import ij.plugin.filter.PlugInFilterRunner;
import ij.util.Tools;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.LayoutManager;

import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.*;

/**
 * This class is a customizable modal dialog box. Here is an example
 * GenericDialog with one string field and two numeric fields:
 * <pre>
 *  public class Generic_Dialog_Example implements PlugIn {
 *    static String title="Example";
 *    static int width=512,height=512;
 *    public void run(String arg) {
 *      GenericDialog gd = new GenericDialog("New Image");
 *      gd.addStringField("Title: ", title);
 *      gd.addNumericField("Width: ", width, 0);
 *      gd.addNumericField("Height: ", height, 0);
 *      gd.showDialog();
 *      if (gd.wasCanceled()) return;
 *      title = gd.getNextString();
 *      width = (int)gd.getNextNumber();
 *      height = (int)gd.getNextNumber();
 *      IJ.newImage(title, "8-bit", width, height, 1);
 *   }
 * }
 * </pre>
* To work with macros, the first word of each component label must be 
* unique. If this is not the case, add underscores, which will be converted  
* to spaces when the dialog is displayed. For example, change the checkbox labels
* "Show Quality" and "Show Residue" to "Show_Quality" and "Show_Residue".
*/
public class GenericDialog implements java.awt.event.ActionListener {

	public static final int MAX_SLIDERS = 25;
	protected Vector numberField, stringField, checkbox, choice, slider;
	protected Vector numbers, strings, checkboxes, choices, sliders;
	protected Vector defaultValues,defaultText;
	protected String textArea1, textArea2;
    private boolean wasCanceled;
    private int y;
    private int nfIndex, sfIndex, cbIndex, choiceIndex;
	private boolean firstNumericField=true;
	private boolean firstSlider=true;
	private boolean invalidNumber;
	private String errorMessage;
	private boolean firstPaint = true;
	private Hashtable labels;
	private boolean macro;
	private String macroOptions;
	private int topInset, leftInset, bottomInset;
    private boolean customInsets;
    private int[] sliderIndexes;
	protected String title;

	public java.awt.Label theLabel;

    /** Creates a new GenericDialog with the specified title. Uses the current image
    	image window as the parent frame or the ImageJ frame if no image windows
    	are open. Dialog parameters are recorded by ImageJ's command recorder but
    	this requires that the first word of each label be unique. */
	public GenericDialog(String title) {
		this(title, null);
	}

    /** Creates a new GenericDialog using the specified title and parent frame. */
    public GenericDialog(String title, java.awt.Frame parentUnused) {
		this.title = title;
		numberField = new Vector(5);
		defaultValues = new Vector(5);
		defaultText = new Vector(5);
		macroOptions = Macro.getOptions();
		macro = macroOptions!=null;
    }
    
	//void showFields(String id) {
	//	String s = id+": ";
	//	for (int i=0; i<maxItems; i++)
	//		if (numberField[i]!=null)
	//			s += i+"='"+numberField[i].getText()+"' ";
	//	IJ.write(s);
	//}

	/** Adds a numeric field. The first word of the label must be
		unique or command recording will not work.
	* @param label			the label
	* @param defaultValue	value to be initially displayed
	* @param digits			number of digits to right of decimal point
	*/
	public void addNumericField(String label, double defaultValue, int digits) {
		addNumericField(label, defaultValue, digits, 6, null);
	}

	/** Adds a numeric field. The first word of the label must be
		unique or command recording will not work.
	* @param label			the label
	* @param defaultValue	value to be initially displayed
	* @param digits			number of digits to right of decimal point
	* @param columns		width of field in characters
	* @param units			a string displayed to the right of the field
	*/
   public void addNumericField(String label, double defaultValue, int digits, int columns, String units) {
   		String label2 = label;
   		if (label2.indexOf('_')!=-1)
   			label2 = label2.replace('_', ' ');
		numberField.addElement(label);
		saveLabel("" + defaultValue, label);
    }
    
    private void saveLabel(Object component, String label) {
    	if (labels==null)
    		labels = new Hashtable();
    	if (label.length()>0) {
    		if (label.charAt(0)==' ')
    			label = label.trim();
			labels.put(label, component);
		}
    }
    
	/** Adds an 8 column text field.
	* @param label			the label
	* @param defaultText		the text initially displayed
	*/
	public void addStringField(String label, String defaultText) {
		addStringField(label, defaultText, 8);
	}

	/** Adds a text field.
	* @param label			the label
	* @param defaultText		text initially displayed
	* @param columns			width of the text field
	*/
	public void addStringField(String label, String defaultText, int columns) {
		if (stringField == null)
			stringField = new Vector(4);
		stringField.addElement(label);
		saveLabel(defaultText, label);
		y++;
    }
    
	/** Adds a checkbox.
	* @param label			the label
	* @param defaultValue	the initial state
	*/
    public void addCheckbox(String label, boolean defaultValue) {
	        if (checkbox==null)
		    checkbox = new Vector(4);
                checkbox.addElement(label);
		saveLabel(new Boolean(defaultValue), label);
		y++;
    }
    
	/** Adds a group of checkboxs using a grid layout.
	* @param rows			the number of rows
	* @param columns		the number of columns
	* @param labels			the labels
	* @param defaultValues	the initial states
	*/
    public void addCheckboxGroup(int rows, int columns, String[] labels, boolean[] defaultValues) {
    	for (int row=0; row<rows; row++) {
			for (int col=0; col<columns; col++) {
				int i2 = col*rows+row;
				if (i2>=labels.length) break;
				checkbox.addElement(labels[i2]);
				saveLabel(new Boolean(defaultValues[i2]),
					labels[i2]);
			}
		}
    }

    /** Adds a popup menu.
   * @param label	the label
   * @param items	the menu items
   * @param defaultItem	the menu item initially selected
   */
   public void addChoice(String label, String[] items, String defaultItem) {
		if (choice==null)
			choice = new Vector(4);
		choice.addElement(label);
		saveLabel(defaultItem, label);
		saveLabel(items, label + ":items");
    }
    
    /** Adds a message consisting of one or more li9nes of text. */
    public void addMessage(String text) {
    }
    
	/** Adds one or two (side by side) text areas.
	* @param text1	initial contents of the first text area
	* @param text2	initial contents of the second text area or null
	* @param rows	the number of rows
	* @param rows	the number of columns
	*/
    public void addTextAreas(String text1, String text2, int rows, int columns) {
	textArea1 = text1;
	textArea2 = text2;
    }
    
   public void addSlider(String label, double minValue, double maxValue, double defaultValue) {
		numberField.addElement(label);
		saveLabel("" + defaultValue, label);
		firstSlider = false;
    }

    /** Adds a Panel to the dialog. */
    public void addPanel(java.awt.Panel panel) {
    }

    /** Adds a Panel to the dialog with custom contraint and insets. The
    	defaults are GridBagConstraints.WEST (left justified) and 
    	"new Insets(5, 0, 0, 0)" (5 pixels of padding at the top). */
    public void addPanel(java.awt.Panel panel, int contraints, java.awt.Insets insets) {
    }
    
    /** Set the insets (margins), in pixels, that will be 
    	used for the next component added to the dialog.
    <pre>
    Default insets:
        addMessage: 0,20,0 (empty string) or 10,20,0
        addCheckbox: 15,20,0 (first checkbox) or 0,20,0
        addCheckboxGroup: 10,0,0 
        addNumericField: 5,0,3 (first field) or 0,0,3
        addStringField: 5,0,5 (first field) or 0,0,5
        addChoice: 5,0,5 (first field) or 0,0,5
     </pre>
    */
    public void setInsets(int top, int left, int bottom) {
    }
    
	java.awt.Insets getInsets(int top, int left, int bottom, int right) {
			return new java.awt.Insets(top, left, bottom, right);
	}

	/** Returns true if the user clicks on "Cancel". */
    public boolean wasCanceled() {
	return false;
    }
    
	/** Returns the contents of the next numeric field. */
   public double getNextNumber() {
		if (numberField==null)
			return -1.0;
		String label = (String)numberField.elementAt(nfIndex);
		String theText = (String)labels.get(label);
		if (macro) {
			theText = Macro.getValue(macroOptions, label, theText);
			//IJ.write("getNextNumber: "+label+"  "+theText);
		}	
		double value;
			Double d = getValue(theText);
			if (d!=null)
				value = d.doubleValue();
			else {
				invalidNumber = true;
				errorMessage = "\""+theText+"\" is an invalid number";
				value = 0.0;
			}
                    nfIndex++;
		return value;
    }
    
	private String trim(String value) {
		if (value.endsWith(".0"))
			value = value.substring(0, value.length()-2);
		if (value.endsWith(".00"))
			value = value.substring(0, value.length()-3);
		return value;
	}

 	protected Double getValue(String theText) {
 		Double d;
 		try {d = new Double(theText);}
		catch (NumberFormatException e){
			d = null;
		}
		return d;
	}

	/** Returns true if one or more of the numeric fields contained an  
		invalid number. Must be called after one or more calls to getNextNumber(). */
   public boolean invalidNumber() {
    	boolean wasInvalid = invalidNumber;
    	invalidNumber = false;
    	return wasInvalid;
    }
    
	/** Returns an error message if getNextNumber was unable to convert a 
		string into a number, otherwise, returns null. */
	public String getErrorMessage() {
		return errorMessage;
   	}

  	/** Returns the contents of the next text field. */
   public String getNextString() {
		String label = (String)stringField.elementAt(nfIndex);
		String theText = (String)labels.get(label);
		theText = Macro.getValue(macroOptions, label, theText);
		sfIndex++;
		return theText;
    }
    
  	/** Returns the state of the next checkbox. */
    public boolean getNextBoolean() {
		if (checkbox==null)
			return false;
		String label = (String)checkbox.get(cbIndex);
		boolean state = isMatch(macroOptions, label+" ");
		cbIndex++;
		return state;
    }
    
    // Returns true if s2 is in s1 and not in a bracketed literal (e.g., "[literal]")
    boolean isMatch(String s1, String s2) {
    	int len1 = s1.length();
    	int len2 = s2.length();
    	boolean match, inLiteral=false;
    	char c;
    	for (int i=0; i<len1-len2+1; i++) {
    		c = s1.charAt(i);
     		if (inLiteral && c==']')
    			inLiteral = false;
    		else if (c=='[')
    			inLiteral = true;
    		if (c!=s2.charAt(0) || inLiteral || (i>1&&s1.charAt(i-1)=='='))
    			continue;
    		match = true;
			for (int j=0; j<len2; j++) {
				if (s2.charAt(j)!=s1.charAt(i+j))
					{match=false; break;}
			}
			if (match) return true;
    	}
    	return false;
    }
    
  	/** Returns the selected item in the next popup menu. */
    public String getNextChoice() {
		if (choice==null)
			return "";
		String label = (String)choice.get(choiceIndex);
		String item = (String)labels.get(label);
		item = Macro.getValue(macroOptions, label, item);
		choiceIndex++;
		return item;
    }
    
  	/** Returns the index of the selected item in the next popup menu. */
    public int getNextChoiceIndex() {
		String label = (String)choice.get(choiceIndex);
		String[] items = (String[])labels.get(label + ":items");
		String item = getNextChoice();
		int selected = 0;
		for (; selected < items.length; selected++)
			if (items[selected].equals(item))
				return selected;
		return -1;
    }
    
  	/** Returns the contents of the next text area. */
	public String getNextText() {
		if (textArea1 != null) {
			String result = textArea1;
			textArea1 = null;
			return result;
		}
		String result = textArea2;
		textArea2 = null;
		return result;
	}

  	/** Displays this dialog box. */
    public void showDialog() {
		if (!macro)
			throw new RuntimeException("Cannot run dialog headlessly");
		nfIndex = 0;
		sfIndex = 0;
		cbIndex = 0;
		choiceIndex = 0;
  	}
  	
  	/** Returns the Vector containing the numeric TextFields. */
  	public Vector getNumericFields() {
  		return numberField;
  	}
    
  	/** Returns the Vector containing the string TextFields. */
  	public Vector getStringFields() {
  		return stringField;
  	}

  	/** Returns the Vector containing the Checkboxes. */
  	public Vector getCheckboxes() {
  		return checkbox;
  	}

  	/** Returns the Vector containing the Choices. */
  	public Vector getChoices() {
  		return choice;
  	}

  	/** Returns the sliders (Scrollbars). */
  	public Vector getSliders() {
  		return slider;
  	}

  	/** Returns a reference to textArea1. */
  	public java.awt.TextArea getTextArea1() {
  		return null;
  	}

  	/** Returns a reference to textArea2. */
  	public java.awt.TextArea getTextArea2() {
  		return null;
  	}
  	
  	/** Returns a reference to the Label or MultiLineLabel created
  		by addMessage(), or null if addMessage() was not called. */
  	public java.awt.Component getMessage() {
  		return null;
  	}

	protected void setup() {
	}

	public java.awt.Insets getInsets() {
    	return new java.awt.Insets(0, 0, 1, 1);
	}

	public void actionPerformed(java.awt.event.ActionEvent ev) {}
	public void setEchoChar(char echoChar) {}
	public void addPreviewCheckbox(PlugInFilterRunner pfr) {}
	public void addPreviewCheckbox(PlugInFilterRunner pfr, String label) {}
	public void setOKLabel(String label) {}
	public void enableYesNoCancel() {}
	public void enableYesNoCancel(String yesLabel, String noLabel) {}
	public void hideCancelButton() {}
	public void addDialogListener(DialogListener dl) {}
	public boolean wasOKed() { return true; }
	public void previewRunning(boolean isRunning) {}
	public void centerDialog(boolean b) {}
	public void addHelp(String url) {}
	public void setModal(boolean modal) {}
	public void dispose() {}
	public void addWindowListener(WindowListener listener) {}
	public void setCursor(Cursor cursor) {}
	public void add(Component component) {}
	public void add(Component component, int index) {}
	public void add(Component component, Object constraints) {}
	public void remove(Component component) {}
	public void repaint() {}
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public void windowClosing(WindowEvent event) {}
	public void itemStateChanged(ItemEvent event) {}
	public LayoutManager getLayout() { return null; }
}
