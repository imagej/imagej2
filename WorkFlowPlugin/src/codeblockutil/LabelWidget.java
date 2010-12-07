package codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public abstract class LabelWidget extends JComponent{
	public static final int DROP_DOWN_MENU_WIDTH = 7;
	private static final long serialVersionUID = 837647234895L;
    /** Border of textfield*/
	private static final Border textFieldBorder = new CompoundBorder(BorderFactory.createLoweredBevelBorder(), new EmptyBorder(1,2,1,2));
    /** Number formatter for this label */
	private static final NumberFormatter nf = new NumberFormatter(NumberFormatter.MEDIUM_PRECISION);
	
	/** Label that is visable iff editingText is false */
	private final ShadowLabel textLabel = new ShadowLabel();
	/** TextField that is visable iff editingText is true */
	private final BlockLabelTextField textField = new BlockLabelTextField();
   /** drop down menu icon */
	private final LabelMenu menu = new LabelMenu();;
	
	/** The label text before user begins edit (applies only to editable labels)*/
    private String labelBeforeEdit = "";
	/** If this is a number, then only allow nagatvie signs and periods at certain spots */
	private boolean isNumber = false;
    /** Is labelText editable by the user -- default true */
	private boolean isEditable = false;
	/** If focus is true, then show the combo pop up menu */
	private boolean isFocused = false;
	/** Has ComboPopup accessable selections */
	private boolean hasSiblings = false;
	/** True if TEXTFIELD is being edited by user. */
	private boolean editingText;
	/** the background color of the tooltip */
	private Color tooltipBackground = new Color(255,255,225);
	
	private double zoom = 1.0;

	/**
	 * BlockLabel Constructor that takes in BlockID as well.
	 * Unfortunately BlockID is needed, so the label can redirect mouse actions.
	 * @param zoom 
	 */
	public LabelWidget(String initLabelText, Color fieldColor, Color tooltipBackground) {
		if(initLabelText ==null) initLabelText = "";
		this.setFocusTraversalKeysEnabled(false);//MOVE DEFAULT FOCUS TRAVERSAL KEYS SUCH AS TABS
        this.setLayout(new BorderLayout());
        this.tooltipBackground = tooltipBackground;
        this.labelBeforeEdit = initLabelText;

		//set up textfield colors
        textField.setForeground(Color.WHITE);//white text
        textField.setBackground(fieldColor);//background matching block color
        textField.setCaretColor(Color.WHITE);//white caret
        textField.setSelectionColor(Color.BLACK);//black highlight
        textField.setSelectedTextColor(Color.WHITE);//white text when highlighted
        textField.setBorder(textFieldBorder);
        textField.setMargin(textFieldBorder.getBorderInsets(textField));
	}
	protected abstract void fireTextChanged(String value);
	protected abstract void fireGenusChanged(String value);
	protected abstract void fireDimensionsChanged(Dimension value);
	protected abstract boolean isTextValid(String text);
	
	public void addKeyListenerToTextField(KeyListener l){
		textField.addKeyListener(l);
	}
	public void addMouseListenerToLabel(MouseListener l){
		textLabel.addMouseListener(l);
	}
	public void addMouseMotionListenerToLabel(MouseMotionListener l){
		textLabel.addMouseMotionListener(l);
	}
	
	//////////////////////////////
	//// LABEL CONFIGURATION /////
	/////////////////////////////
	
	public void showMenuIcon(boolean show){
		if(this.hasSiblings){
			isFocused = show;
			// repaints the menu and items with the new zoom level
			menu.popupmenu.setZoomLevel(zoom);
			menu.repaint();
		}
	}
	
	/**
	 * setEditingState sets the current editing state of the BlockLabel.
	 * Repaints BlockLabel to reflect the change.
	 */
	public void setEditingState(boolean editing) {
		if (editing) {
			editingText = true;
			textField.setText(textLabel.getText().trim());
    	    labelBeforeEdit = textLabel.getText();
    		this.removeAll();
    		this.add(textField);
			textField.grabFocus();
		} else {
			//update to current textfield.text
			//if text entered was not empty and if it was editing before
            if(editingText){ 
            	//make sure to remove leading and trailing spaces before testing if text is valid
            	//TODO if allow labels to have leading and trailing spaces, will need to modify this if statement
                if(isTextValid(textField.getText().trim()))
                    setText(textField.getText());
                else
                    setText(labelBeforeEdit);
            }
            editingText = false;
		}
	}
	
	/**
	 * editingText returns if BlockLable is being edited
	 * @return editingText
	 */
	public boolean editingText() {
		return editingText;
	}
	
	/**
	 * setEditable state of BlockLabel
	 * @param isEditable specifying editable state of BlockLabel
	 */
	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}
	
	/**
	 * isEditable returns if BlockLable is editable
	 * @return isEditable
	 */
	public boolean isEditable() {
		return isEditable;
	}
	
	public void setNumeric(boolean isNumber) {
		this.isNumber = isNumber;
	}
	
	/**
	 * isEditable returns if BlockLable is editable
	 * @return isEditable
	 */
	public boolean isNumeric() {
		return isNumber;
	}

	public void setSiblings(boolean hasSiblings, String[][] siblings){
		this.hasSiblings = hasSiblings;
		this.menu.setSiblings(siblings);
	}
	
	public boolean hasSiblings(){
		return this.hasSiblings;
	}
	
	/**
	 * set up fonts
	 * @param font
	 */
	public void setFont(Font font){
		super.setFont(font);
		textLabel.setFont(font);
		textField.setFont(font);
		menu.setFont(font);
	}
	
	/**
	 * sets the tool tip of the label
	 */
	public void assignToolTipToLabel(String text){
		this.textLabel.setToolTipText(text);
	}
	
	/**
	 * getText
	 * @return String of the current BlockLabel
	 */
	public String getText() {
		return textLabel.getText().trim();
	}
	
	/**
	 * setText to a NumberFormatted double
	 * @param value
	 */
	public void setText(double value) {
		//check for +/- Infinity
		if (Math.abs(value - Double.MAX_VALUE) < 1) {
			updateLabelText("Infinity");
		} else if  (Math.abs(value + Double.MAX_VALUE) < 1) {
			updateLabelText("-Infinity");
		} else {
			updateLabelText(nf.format(value));
		}
	}
	
	/**
	 * setText to a String (trimmed to remove excess spaces)
	 * @param string
	 */
	public void setText(String string) {
		if(string != null){
			updateLabelText(string.trim());
		}
	}
	
	/**
	 * setText to a boolean
	 * @param bool
	 */
	public void setText(boolean bool) {
		updateLabelText(bool? "True" : "False");
	}
	
	
	/**
	 * updateLabelText updates labelText and sychronizes textField and textLabel to it
	 * @param text
	 */
	public void updateLabelText(String text) {
		//leave some space to click on
		if (text.equals("")) {
			text = "     ";
		}
		
		//update the text everywhere
		textLabel.setText(text);
		textField.setText(text);
				
		//resize to new text
		updateDimensions();
		
		//the blockLabel needs to update the data in Block
		this.fireTextChanged(text);
        
		//show text label and additional ComboPopup if one exists
		this.removeAll();
		this.add(textLabel, BorderLayout.CENTER);
        if (hasSiblings){
            this.add(menu, BorderLayout.EAST);
        }
	}
    
	////////////////////
	//// RENDERING /////
	////////////////////
    
    /**
     * Updates the dimensions of the textRect, textLabel, and textField to the minimum size needed
     * to contain all of the text in the current font.
     */
    private void updateDimensions() {
		Dimension updatedDimension = new Dimension(
				textField.getPreferredSize().width,
				textField.getPreferredSize().height);
		if(this.hasSiblings){
			updatedDimension.width += LabelWidget.DROP_DOWN_MENU_WIDTH;
		}
		textField.setSize(updatedDimension);
		textLabel.setSize(updatedDimension);
		this.setSize(updatedDimension);
		this.fireDimensionsChanged(this.getSize());
    }
    
    /**
     * high lights the text of the editing text field from
     * 0 to the end of textfield
     */
    public void highlightText(){
    	this.textField.setSelectionStart(0);
    }
    
    /**
     * Toggles the visual suggestion that this label may be editable depending on the specified
     * suggest flag and properties of the block and label.  If suggest is true, the visual suggestion will display.  Otherwise, nothing 
     * is shown.  For now, the visual suggestion is a simple white line boder.
     * Other requirements for indicator to show: 
     * - label type must be NAME
     * - label must be editable
     * - block can not be a factory block
     * @param suggest 
     */
    protected void suggestEditable(boolean suggest){
        if(isEditable){
            if(suggest){
                setBorder(BorderFactory.createLineBorder(Color.white));//show white border
            }else{
                setBorder(null);//hide white border
            }
        }
    }
    
    public void setZoomLevel(double newZoom) {
    	this.zoom = newZoom;
    	Font renderingFont;// = new Font(font.getFontName(), font.getStyle(), (int)(font.getSize()*newZoom));
        AffineTransform at = new AffineTransform();
        at.setToScale(newZoom, newZoom);
        renderingFont = this.getFont().deriveFont(at);
    	this.setFont(renderingFont);
    	this.repaint();
    	this.updateDimensions();
    }
	
	public String toString() {
		return "Label at " + this.getLocation() + " with text: \"" + textLabel.getText() + "\"";
	}
	
	/**
	 * returns true if this block should can accept a negative sign
	 */
	public boolean canProcessNegativeSign(){
		if(this.getText() != null && this.getText().contains("-")){
			//if it already has a negative sign, 
			//make sure we're highlighting it
			if(textField.getSelectedText() != null && textField.getSelectedText().contains("-")){
				return true;
			}else{
				return false;
			}
		}else{
			//if it does not have negative sign,
			//make sure our highlight covers index 0
			if(textField.getCaretPosition()==0){
				return true;
			}else{
				if(textField.getSelectionStart()==0){
					return true;
				}
			}
		}
		return false;
	}
	
    /**
     * BlockLabelTextField is a java JtextField that internally handles various events
     * and provides the semantic to interface with the user.  Unliek typical JTextFields,
     * the blockLabelTextField allows clients to only enter certain keys board input.
     * It also reacts to enters and escapse by delegating the KeyEvent to the parent
     * RenderableBlock.
     */
	private class BlockLabelTextField extends JTextField implements MouseListener, DocumentListener, FocusListener, ActionListener{
    	private static final long serialVersionUID = 873847239234L;
    	/** These Key inputs are processed by this text field */
    	private final char[] validNumbers = {'1','2','3','4','5','6','7','8','9','0','.'};
    	/** These Key inputs are processed by this text field if NOT a number block*/
    	private final char[] validChar = {'1','2','3','4','5','6','7','8','9','0',
    			'q','w','e','r','t','y','u','i','o','p','a','s','d','f','g','h','j',
    			'k','l','z','x','c','v','b','n','m',
    			'Q','W','E','R','T','Y','U','I','O','P','A','S','D','F',
    			'G','H','J','K','L','Z','X','C','V','B','N','M',
    			'\'','!','@','#','$','%','^','&','*','(',')','_','+',
    			'-','=','{','}','|','[',']','\\',' ',
    			':','"',';','\'','<','>','?',',','.','/','`','~'};
    	/** These Key inputs are processed by all this text field */
    	private final int[] validMasks = {KeyEvent.VK_BACK_SPACE,
    			KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT,
    			KeyEvent.VK_RIGHT, KeyEvent.VK_END, KeyEvent.VK_HOME,
    			'-',KeyEvent.VK_DELETE, KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL,
    			InputEvent.SHIFT_MASK, InputEvent.SHIFT_DOWN_MASK};
    	
    	/**
    	 * Contructs new block label text field
    	 */
    	private BlockLabelTextField(){
    		this.addActionListener(this);
    		this.getDocument().addDocumentListener(this);
    		this.addFocusListener(this);
    		this.addMouseListener(this);
            /*
             * Sets whether focus traversal keys are enabled 
             * for this Component. Components for which focus 
             * traversal keys are disabled receive key events 
             * for focus traversal keys.
             */
            this.setFocusTraversalKeysEnabled(false);
    	}
    	public void mousePressed(MouseEvent e) {}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
    	public void mouseExited(MouseEvent arg0) {
            //remove the white line border
            //note: make call here since text fields consume mouse events
        	//preventing parent from responding to mouse exited events
            suggestEditable(false);
        }
    	public void actionPerformed(ActionEvent e) {
			setEditingState(false);
		}
		public void changedUpdate(DocumentEvent e) {
			//listens for change in attributes
		}
		public void insertUpdate(DocumentEvent e) {
			updateDimensions();
		}
		public void removeUpdate(DocumentEvent e) {
			updateDimensions();
		}
		public void focusGained(FocusEvent e) {}
		public void focusLost(FocusEvent e) {
			setEditingState(false);
		}
		
		/**
		 * for all user-generated AND/OR system generated key inputs,
		 * either perform some action that should be triggered by
		 * that key or
		 */
	    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,int condition, boolean pressed){
	    	if(isNumber){
		    	if(e.getKeyChar() == '-' && canProcessNegativeSign()){
	    			return super.processKeyBinding(ks, e, condition, pressed);
		    	}
	    		if(this.getText().contains(".") && e.getKeyChar() == '.'){
	    			return false;
	    		}
	    		for(char c : validNumbers){
	    			if(e.getKeyChar() == c){
		    			return super.processKeyBinding(ks, e, condition, pressed);
		    		}
	    		}
	    	}else{
		    	for(char c : validChar){
		    		if(e.getKeyChar() == c){
		    			return super.processKeyBinding(ks, e, condition, pressed);
		    		}
		    	}
	    	}
	    	for(int i : validMasks){
	    		if(e.getKeyCode() == i){
	    			return super.processKeyBinding(ks, e, condition, pressed);
	    		}
	    	}
	    	if((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0){
	    		return super.processKeyBinding(ks, e, condition, pressed);
	    	}
			return false;
	    }
	}
    
	private class LabelMenu extends JPanel implements MouseListener, MouseMotionListener{
		private static final long serialVersionUID = 328149080240L;
		private CPopupMenu popupmenu;
    	private GeneralPath triangle;
    	private LabelMenu(){
    		this.setOpaque(false);
    		this.addMouseListener(this);
    		this.addMouseMotionListener(this);
    		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    		this.popupmenu = new CPopupMenu();
    	}
    	/**
    	 * @param siblings = array of siblin's genus and initial label
    	 *  { {genus, label}, {genus, label}, {genus, label} ....}
    	 */
    	private void setSiblings(String[][] siblings){
    		popupmenu = new CPopupMenu();
    		//if connected to a block, add self and add siblings
    		for(int i=0; i<siblings.length; i++){
    			final String selfGenus = siblings[i][0];
    			CMenuItem selfItem = new CMenuItem(siblings[i][1]);
    			selfItem.addActionListener(new ActionListener(){
    				public void actionPerformed(ActionEvent e){
    					fireGenusChanged(selfGenus);
    					showMenuIcon(false);
    				}
    			});
    			popupmenu.add(selfItem);
    		}
    	}
    	public boolean contains(Point p){
    		return triangle != null && triangle.contains(p);
    	}
    	public boolean contains(int x, int y){
    		return triangle != null && triangle.contains(x, y);
    	}
    	public void paint(Graphics g){
    		super.paint(g);
	    	if(isFocused){
	    		Graphics2D g2 = (Graphics2D)g;
	    		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    		
	    		triangle = new GeneralPath();
	    		triangle.moveTo(0,this.getHeight()/4);
	    		triangle.lineTo(this.getWidth()-1, this.getHeight()/4);
	    		triangle.lineTo(this.getWidth()/2-1, this.getHeight()/4+LabelWidget.DROP_DOWN_MENU_WIDTH);
	    		triangle.lineTo(0, this.getHeight()/4);
	    		triangle.closePath();
				
	    		g2.setColor(new Color(255,255,255,100));
				g2.fill(triangle);
				g2.setColor(Color.BLACK);
				g2.draw(triangle);
    		}
    		
    	}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    	public void mousePressed(MouseEvent e) {
    		if(hasSiblings)
    			popupmenu.show(this, 0, 0);
    	}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
   	 	public void mouseDragged(MouseEvent e) {}
   	 	public void mouseMoved(MouseEvent e) {}
    }
    
    /**
     * Much like a JLabel, only the text is displayed with a shadow like outline
     */
	private class ShadowLabel extends JLabel implements MouseListener, MouseMotionListener{
    	private static final long serialVersionUID = 90123787382L;
    	//To get the shadow effect the text must be displayed multiple times at
		//multiple locations.  x represents the center, white label.
		// o is color values (0,0,0,0.5f) and ¥ is black.
		//			  o o
		//			o x ¥ o
		//			o ¥ o
		//			  o
		//offsetArrays representing the translation movement needed to get from
		// the center location to a specific offset location given in {{x,y},{x,y}....}
		//..........................................grey points.............................................black points
		private final int[][] shadowPositionArray = {{0,-1},{1,-1}, {-1,0}, {2,0},	{-1,1}, {1,1},  {0,2}, 	{1,0},  {0,1}};
		private final float[] shadowColorArray =	{0.5f,	0.5f,	0.5f, 	0.5f, 	0.5f, 	0.5f,	0.5f,	0,		0};
		private double offsetSize = 1;
		private ShadowLabel(){
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
		public void paint(Graphics g){
			Graphics2D g2 = (Graphics2D) g;
			g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

			//DO NOT DRAW SUPER's to prevent drawing of label's string.
			//Implecations: background not automatically drawn
			//super.paint(g);
			
			//draw shadows
			for (int i = 0; i < shadowPositionArray.length; i++) {
				int dx = shadowPositionArray[i][0];
				int dy = shadowPositionArray[i][1];
				g2.setColor(new Color(0,0,0, shadowColorArray[i]));
				g2.drawString(this.getText(), (int)((4+dx)*offsetSize), this.getHeight()+(int)((dy-6)*offsetSize));
			}
			
			//draw main Text
			g2.setColor(Color.white);
			g2.drawString(this.getText(), (int)((4)*offsetSize), this.getHeight()+(int)((-6)*offsetSize));
		}
		public JToolTip createToolTip(){
			return new CToolTip(tooltipBackground);
		}
	    /**
	     * Set to editing state upon mouse click if this block label is editable
	     */
		public void mouseClicked(MouseEvent e) {
			//if clicked and if the label is editable,
			if ( (e.getClickCount() == 1) && isEditable) {	
				//if clicked and if the label is editable,
				//then set it to the editing state when the label is clicked on
				setEditingState(true);
	    		textField.setSelectionStart(0);
			}
		}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {
	        suggestEditable(true);
		}
		public void mouseExited(MouseEvent e) {
	        suggestEditable(false);
		}	
		public void mouseDragged(MouseEvent e) {
			suggestEditable(false);
		}
		public void mouseMoved(MouseEvent e) {
	        suggestEditable(true);
		}
	}
}