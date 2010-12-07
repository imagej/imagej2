package renderable;


import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import workspace.Workspace;
import workspace.WorkspaceEvent;


import codeblocks.Block;
import codeblocks.JComponentDragHandler;
import codeblockutil.CTracklessScrollPane;
import codeblockutil.CScrollPane.ScrollPolicy;


/**
 * Comment stores and displays user-generated text that
 * can be edited by the user. Comments begin in “editable” state.
 *
 * Comments are associated with a parent source of type JComponent.
 * It should "tag" along with that component.  Note, however, that
 * this feature should be ensured by the parent source.  The
 * parent source can guarantee this by invoking the methods
 * setPosition, translatePosition, and setParent when
 * appropriate.
 *
 * text : String //the text stored in this Comment and edited by the user
 */
public class Comment extends JPanel {
	private static final long serialVersionUID = 328149080425L;
	/**Background color of all comments*/
    private static final Color background = new Color(255,255,150);
    /**border color*/
    private final Color borderColor;
    /**Text field UI*/
    private  JTextArea textArea;//textArea belonging to editingPane
	/**ScrollPane UI*/
    private  CTracklessScrollPane scrollPane;
    /**Dragging handler of this Comment*/
    private JComponentDragHandler jCompDH;
    /**Manager for arrow drawn from this to parent while in editing mode**/
  	private CommentArrow arrow;
  	/**Manages Undo-able Events in this comment's text editor*/
  	private UndoManager undoManager;
  	
  	/** The JComponent this comment and comment label is connected to */
  	private CommentSource commentSource;
  	
  	/** The commentLabel linked to this Comment and placed on the commentSource 	 */
  	private CommentLabel commentLabel;
  	
  	/** true if this comment should not be able to have a location outside of its parent's bounds, false if it may be located outside of its parent's bounds */
  	private boolean constrainComment = true;

  	static int FONT_SIZE = 14;
  	static int MINIMUM_WIDTH = FONT_SIZE*4;
  	static int MINIMUM_HEIGHT = FONT_SIZE*2;
  	static int DEFAULT_WIDTH = 150;
  	static int DEFAULT_HEIGHT = 100;

  	
  	private boolean resizing = false;
  	private int margin = 6;
  	private int width = DEFAULT_WIDTH;
  	private int height = DEFAULT_HEIGHT;
  	private double zoom = 1.0;
  	private String fontname = "Monospaced";
  	private Shape  body, resize, textarea;
  	private boolean pressed = false;
  	private boolean active = false;
  	
    /**
     * Constructs a Comment
     * with belonging to source, with text of initText, and initial zoom
  	 * The comment's borders will have the color borderColor.  
  	 * 
  	 * Note that initializing a comment only constructs
     * all of the necessary structures.  To graphically display a comment,
     * the implementor must then add the comment using the proper
     * Swing methods OR through the convenience method Comment.setParent()
     *
     * @param initText,  initial text of comment
     * @param source, where the comment is linked to.
     * @param borderColor the color that the border of the comment should be
     * @param zoom  initial zoom
     */
    public Comment(String initText, CommentSource source, Color borderColor, double zoom){
    	//set up important fields
    	this.zoom = zoom;
    	this.setLayout(null);
    	this.setOpaque(false);
    	this.setBounds(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
    	this.borderColor=borderColor;
    	this.commentSource = source;
    	
    	//set up editingPanel, labelPanel and their listeners
    	//initialize textArea with autowrap AROUND WORDS not characters
    	textArea = new JTextArea(initText);
    	textArea.setFont(new Font(fontname, Font.PLAIN, (int)(FONT_SIZE*zoom)));
    	textArea.setForeground(Color.BLACK);
    	textArea.setBackground(background);
    	textArea.setCaretColor(Color.BLACK);
    	textArea.setLineWrap(true);
    	textArea.setWrapStyleWord(true);
		undoManager = new UndoManager();
		undoManager.setLimit(1000);
		textArea.getDocument().addUndoableEditListener(undoManager);
		textArea.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
		    	Workspace.getInstance().notifyListeners(new WorkspaceEvent(getCommentSource().getParentWidget(), WorkspaceEvent.BLOCK_COMMENT_CHANGED));

		        if(e.isControlDown() || ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)){	
					if(e.getKeyCode() == KeyEvent.VK_Z){                
			            try{
			                undoManager.undo();
			            }catch(CannotUndoException exception){}
			        }else if(e.getKeyCode() == KeyEvent.VK_Y){                
			            try{
			                undoManager.redo();
			            }catch (CannotRedoException exception){}
			        }
		        }
			}
		});

    	//initialize scrollPane
		scrollPane = new CTracklessScrollPane(textArea,
				ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
				ScrollPolicy.HORIZONTAL_BAR_NEVER,
    			10, this.borderColor, Comment.background);
    	this.add(scrollPane, 0);
    	
    	//set up listeners
    	CommentEventListener eventListener = new CommentEventListener();
    	this.jCompDH = new JComponentDragHandler(this);
    	this.addMouseListener(eventListener);
    	this.addMouseMotionListener(eventListener);
    	textArea.addMouseListener(new MouseAdapter() {
    	    /**
    	     * Implement MouseListener interface
    	     */
    		public void mouseEntered(MouseEvent e) {			
    			Comment comment = Comment.this;
    			comment.setPressed(true);
    			comment.showOnTop();
    		}
    	});
    	textArea.addFocusListener(eventListener);
    	textArea.setEditable(true);
    	this.reformComment();
    	    	
    	this.arrow = new CommentArrow(this);
    	
    	commentLabel = new CommentLabel(source.getBlockID());
    	source.add(commentLabel);
    	commentLabel.setActive(true);

    	this.reformComment();
    	
    	Workspace.getInstance().notifyListeners(new WorkspaceEvent(getCommentSource().getParentWidget(), WorkspaceEvent.BLOCK_COMMENT_ADDED));
    }
    
    /**
     * Handle the removal of this comment from its comment source
     */
    public void delete() {
    	Workspace.getInstance().notifyListeners(new WorkspaceEvent(getCommentSource().getParentWidget(), WorkspaceEvent.BLOCK_COMMENT_REMOVED));
		
    	getParent().remove(arrow.arrow);
		setParent(null);
		
		if (commentSource instanceof RenderableBlock) {
			RenderableBlock rb = (RenderableBlock) commentSource;

			rb.remove(commentLabel);
	    	commentLabel = null;
		}
    }
    
    /**
     * returns the CommentSource for this comment
     * @return
     */
    CommentSource getCommentSource() {
    	return commentSource;
    }
    
    /**
     * returns the commentLabel for this comment
     * @return
     */
    CommentLabel getCommentLabel() {
    	return commentLabel;
    }
    
    /**
     * Returns the width of the comment label for this comment
     * @return
     */
    public int getCommentLabelWidth() {
    	if (commentLabel == null) return 0;
    	return commentLabel.getWidth();
    }

    /**
     * Updates the comment and commentLabel 
     */
    public void update() {
    	if (commentLabel != null) {
    		setVisible(commentLabel.isActive());
    		commentLabel.update();
    		if (arrow.arrow != null) 
    			arrow.setVisible(commentLabel.isActive());
    	}
    }
    
    /**
     * Sets the active state of the commentLabel and updates the comment and commentLabel
     * @param visibleState
     */
    public void update(boolean visibleState) {
    	if (commentLabel != null) {
    		commentLabel.setActive(visibleState);
    	}
    	update();
    }
    
    /**
     * Set a new zoom level, changes font size, label size, location, shape of comment, and arrow for this comment
     * @param newZoom
     */
    public void setZoomLevel(double newZoom) {
		// calculates the new position based on the initial position when zoom is at 1.0
    	this.zoom = newZoom;
    	this.textArea.setFont(new Font(fontname, Font.PLAIN, (int)(12*zoom)));
    	if (commentLabel != null) commentLabel.setZoomLevel(newZoom);

    	this.reformComment();
    	this.getArrow().updateArrow();
    }
    
    /**
     * Recalculate the shape of this comment 
     */
    public void reformComment(){
    	int w = textArea.isEditable() ? (int)(this.width*zoom) : (int)(Comment.MINIMUM_WIDTH*zoom);
    	int h = textArea.isEditable() ? (int)(this.height*zoom) : (int)(Comment.MINIMUM_HEIGHT*zoom);
    	int m = (int)(this.margin*zoom);

    	GeneralPath path2 = new GeneralPath();
    	path2.moveTo(m-1,m-1);
    	path2.lineTo(w-m, m-1);
    	path2.lineTo(w-m, h-m);
    	path2.lineTo(m-1, h-m);
    	path2.closePath();
    	textarea = path2;
  
    	body = new RoundRectangle2D.Double(0,0,w-1,h-1,3*m,3*m);
    	
    	GeneralPath path3 = new GeneralPath();
    	path3.moveTo(w-3*m,h);
    	path3.lineTo(w,h-3*m);
    	path3.curveTo(w,h,w,h,w-3*m,h);
    	resize = path3;
    	
    	scrollPane.setBounds(m,m, w-2*m, h-2*m);
    	scrollPane.setThumbWidth(textArea.isEditable() ? 2*m : 0);
    	this.setBounds(this.getX(), this.getY(), w, h);
    	this.revalidate();
    	this.repaint();
    	
    	if (arrow != null) arrow.updateArrow();
    }
    
	/**
	 * returns the descaled x based on the current zoom
	 * that is given a scaled x it returns what that position would be when zoom == 1
	 * @param x
	 * @return
	 */
	private int descale(double x){
		return (int)(x/zoom);
	}
  
    /**
     * Returns the save String for this comment.
     * @return
     */
    public String getSaveString(){
    	StringBuffer saveString = new StringBuffer();
        saveString.append("<Comment>");
        saveString.append("<Text>");
        saveString.append(Block.escape(this.getText()).replaceAll("`", "'"));
        saveString.append("</Text>");
        saveString.append("<Location>");
        saveString.append("<X>");
        saveString.append(descale(getLocation().getX()));
        saveString.append("</X>");
        saveString.append("<Y>");
        saveString.append(descale(getLocation().getY()));
        saveString.append("</Y>");
        saveString.append("</Location>");
        saveString.append("<BoxSize>");
        saveString.append("<Width>");
        saveString.append(descale(getWidth()));
        saveString.append("</Width>");
        saveString.append("<Height>");
        saveString.append(descale(getHeight()));
        saveString.append("</Height>");
        saveString.append("</BoxSize>");
        if (!commentLabel.isActive()) saveString.append("<Collapsed/>");
        saveString.append("</Comment>");
        return saveString.toString();
    }
    
    /**
     * Loads the comment from a NodeList of comment parts
     * @param commentChildren
     * @param rb
     * @return
     */
    public static Comment loadComment( NodeList commentChildren, RenderableBlock rb) {
        Comment comment = null;
        boolean commentCollapsed = false;
        
        Node commentChild;
        String text = null;
        Point commentLoc = new Point(0,0);
        Dimension boxSize = new Dimension(Comment.DEFAULT_WIDTH, Comment.DEFAULT_HEIGHT);
        
        for(int j=0; j<commentChildren.getLength(); j++){
            commentChild = commentChildren.item(j);
            if(commentChild.getNodeName().equals("Text")){
                text = commentChild.getTextContent();
            }else if(commentChild.getNodeName().equals("Location")){
                RenderableBlock.extractLocationInfo(commentChild, commentLoc);
            }else if(commentChild.getNodeName().equals("BoxSize")){
                RenderableBlock.extractBoxSizeInfo(commentChild, boxSize);
            }else if(commentChild.getNodeName().equals("Collapsed")){
            	commentCollapsed = true;
            }else {
            	System.out.println("Uknown Comment Node: " + commentChild.getNodeName());
            }
        }

        if(text != null){
            comment = new Comment(text, rb, rb.getBlock().getColor(), rb.getZoom());
            comment.setLocation(commentLoc.x, commentLoc.y);
            comment.update(!commentCollapsed);
            comment.setMyWidth((int)boxSize.getWidth());
            comment.setMyHeight((int)boxSize.getHeight());
            comment.reformComment();
        }
        return comment;
    }
    
    /**
     * overrides javax.Swing.JPanel.paint()
     */
 	public void paint(Graphics g){
 		Graphics2D g2 = (Graphics2D)g;
 		g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
 		
 		
 		if (active) {
 	 		g2.setColor(getBorderColor().brighter());
 		} else {
 	 		g2.setColor(getBorderColor());
 		} 		
 		g2.fill(body);
 		if (active) {
 			g2.setColor(Comment.background.brighter());
 		} else {
 			g2.setColor(Comment.background);
 		}
 		g2.fill(textarea);
 		if (active) {
 	 		g2.setColor(Color.white);
 		} else {
 	 		g2.setColor(Color.lightGray);
 		}
 		g2.draw(textarea);
 		if (active) {
 	 		g2.setColor(Color.lightGray.brighter());
 		} else {
 	 		g2.setColor(Color.lightGray);
 		}
 		g2.fill(resize);
 		
 		super.paint(g);
 	}


    /**
     * @return this.text.trim()
     */
    public String getText() {
        return textArea.getText().trim();
    }
    
    /**
     * @modifies editingPane, labelPane
     * @effects modify eiditngPane such that the next call to
     * 			editingPane.getText().trim() equals text.trim() &&
     * 			modify labelPane such that the next call to
     * 			labelPane.getText().trim() equals text.trim()
     * @param text
     */
    public void setText(String text) {
    	textArea.setText(text);
    }

    
     /**
      *  moves this to a new position at (x,y) but not outside of its parent Container
      * @modifies this.location
      * @effect  Set this.location.x to x, if x is within bounds of this.parent.
      * 			if not, then set this.location.x to closest boundary value. 
      *      	Set this.location.y to y, if y is within bounds of this.parent.
      * 			if not, then set this.location.y to closest boundary value. 
      * Override javax.Swing.JComponent.setLocation()
      */
    public void setLocation(int x, int y) {
    	if (isConstrainComment() && this.getParent() != null) {
	        //If x<0, set this.location.x to 0.
	        //If 0<x<this.parent.width, then set this.location.x to x.
	        //If x>this.parent.width, then set this.location.x to this.parent.width.
	        //repeat for y
	        if (y < 0) {
	            y = 0;
	        }else if (y + getHeight()  > this.getParent().getHeight()) {
	            y = Math.max(this.getParent().getHeight() - getHeight(), 0);
	        }
	        
	        if (x < 0) {
	            x = 0;           
	        } else if (x  + getWidth() + 1> this.getParent().getWidth()) {
	            x = Math.max(this.getParent().getWidth() - getWidth() - 1, 0);
	        }
    	}
        super.setLocation(x, y);
    	arrow.updateArrow();
    	Workspace.getInstance().getMiniMap().repaint();
    }
    
    /**
      * moves this to a new position at (x,y) but not outside of its parent Container
      * @modifies this.location
      * @effect  Set this.location.x to x, if x is within bounds of this.parent.
      * 			if not, then set this.location.x to closest boundary value. 
      *      	Set this.location.y to y, if y is within bounds of this.parent.
      * 			if not, then set this.location.y to closest boundary value. 
	  *
      * Override javax.Swing.JComponent.setLocation()
     */
    public void setLocation(Point p){
    	setLocation(p.x, p.y);
    }
    
    
     /**
      * @modifies this
      * @effect translate this.location
      * 		by dx in the x-direction and dy in the y-direction
      * @param dx
      * @param dy
      */
     public void translatePosition(int dx, int dy){
         this.setLocation(this.getX()+dx, this.getY()+dy);
     }
  	
     /**
      * Moves this comment from it's old parent Container to
      * a new Container.  Removal and addition applies only
      * if the Containers are non-null
      * @modifies the current this.parent and newparent
      * @effect First, remove this from current this.parent ONLY if
      * 		current this.parent is non-null.  Second, add this to
      * 		newparent container ONLY if newparent is non-null.
      * 		Third, repaint both modified parent containers.
      * @param newparent
      */
     public void setParent(Container newparent){
         this.setParent(newparent, 0);
     }
     
     /**
      * Over rides the standard setVisible to make sure the arrow's visibility is also set.
      */
     public void setVisible(boolean b) {
    	 super.setVisible(b);
    	 if (arrow.arrow != null) arrow.setVisible(b);
     }
     
     /**
      * Moves this comment from it's old parent Container to
      * a new Container with given constrain.
      * @modifies the current this.parent and newparent
      * @effect First, remove this from current this.parent ONLY if
      * 		current this.parent is non-null.  Second, add this to
      * 		newparent container ONLY if newparent is non-null.
      * 		Third, repaint both modified parent containers.
      * @param newparent
      * @parem constraints
      */
     public void setParent(Container newparent, Object constraints){
    	 //though it's tempting to just write "this.setParent(newparent)"
    	 //we can't do that because we must remove the comment as well
    	 
    	 //remove from the current this.parent Container if non-null
    	 Container oldParent = this.getParent();
         if (oldParent !=null){
        	 oldParent.remove(this);
        	 oldParent.remove(arrow.arrow);
        	 oldParent.validate();
        	 oldParent.repaint();
         }
         //add this to newparent Container if non-null
         if(newparent!=null){
        	 if(constraints == null){
        		 newparent.add(this, 0);
        	 }else{
        		 newparent.add(this, constraints);
        	 }
        	 arrow.updateArrow();
        	 newparent.validate();
             newparent.repaint();
         }
     }
   
    /**
     * String representation of this
     */
     public String toString() {
        return "Comment ID: " + " at " + this.getLocation() + " with text: \"" + getText() + "\"";
    }
     
     /**
      * Bumps the comment to top of ZOrder of parent if parent exists
      */
     public void showOnTop() {
    	 if (getParent() != null) {
    		 getParent().setComponentZOrder(this, 0);
    	 }
     }
      
     /**
      * CommentEventListener is an inner class that
      * responds to the various external events,
      * and provides the requires semantic operations
      * for Comments to be moved/focused correctly.
      * It owns, and sends semantic actions to the
      * outer Comment class.
      */
     private class CommentEventListener implements FocusListener, MouseListener, MouseMotionListener{
       	    
    	 /**When focus lost, force a repaint**/	
    	 public void focusGained(FocusEvent e) {
    		 active = true;
    		 repaint();
    	 }
    	 /**When focuses gained, force a repaint**/
    	 public void focusLost(FocusEvent e) {
    		 active = false;
    		 repaint();
    	 }
    	 /**when clicked upon, switch to editing mode*/
    	 public void mouseClicked(MouseEvent e) {
    		 //prevent users from clicking multiple times and crashing the system
    		 if(e.getClickCount()>1) return;
    	 }

    	 /**highlight this comment when a mouse begins to hover over this*/
    	 public void mouseEntered(MouseEvent e) {
    		 showOnTop();
    		 jCompDH.mouseEntered(e);
    	 }
    	 /**highlight this comment when a mouse hovers over this*/
    	 public void mouseMoved(MouseEvent e) {
    		 if(textArea.isEditable()){
	    		 if(e.getX()>(width-2*margin) && e.getY()>(height-2*margin)){
	    			 Comment.this.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
	    		 }else{
	        		 jCompDH.mouseMoved(e);
	    		 }
    		 }else{
        		 jCompDH.mouseMoved(e);
    		 }
    	 }
    	 /**stop highlighting this comment when a mouse leaves this*/
    	 public void mouseExited(MouseEvent e) {
    		 jCompDH.mouseExited(e);
    	 }
    	 /**prepare for a drag when mouse is pressed down*/
    	 public void mousePressed(MouseEvent e) {
    		 Comment.this.grabFocus();  //atimer.stop();
    		 showOnTop();
    		 jCompDH.mousePressed(e);
    		 if(textArea.isEditable()){
	    		 if(e.getX()>(width-2*margin) && e.getY()>(height-2*margin)){
	    			 setResizing(true);
	    		 }else{
	    			 if(e.getY()<margin){
	        			 setPressed(true);
	        		 }
	    		 }
    		 }else if(e.getY()<margin){
    			 setPressed(true);
    		 }
    		 repaint();
    	 }
    	 
    	 /**when mouse is released*/
    	 public void mouseReleased(MouseEvent e) {
    		 jCompDH.mouseReleased(e);
    		 setResizing(false);
    		 setPressed(false);
    		 repaint();
    	 }
    	 
    	 /**drag this when mouse is dragged*/
    	 public void mouseDragged(MouseEvent e) {	
    		 if(isResizing()){
    			 double ww = e.getX()>MINIMUM_WIDTH*zoom ? e.getX() : MINIMUM_WIDTH*zoom;
    			 double hh = e.getY()>MINIMUM_HEIGHT*zoom ? e.getY() : MINIMUM_HEIGHT*zoom;
    			 width = (int)ww;
    			 height = (int)hh;
    			 reformComment();
    			 Workspace.getInstance().notifyListeners(new WorkspaceEvent(getCommentSource().getParentWidget(), WorkspaceEvent.BLOCK_COMMENT_RESIZED));
    		 }else{
	    		 jCompDH.mouseDragged(e);
	    		 arrow.updateArrow();
    			 Workspace.getInstance().notifyListeners(new WorkspaceEvent(getCommentSource().getParentWidget(), WorkspaceEvent.BLOCK_COMMENT_MOVED));
    		 }
    	 }
     }
	 
	 /**
	  * Returns the comment background color
	  * @return
	  */
	 Color getBackgroundColor() {
		 return background;
	 }
	 
	 /**
	  * Returns the borderColor of this comment
	  * @return
	  */
	 Color getBorderColor() {
		 return borderColor;
	 }

	 /**
	  * access to the comment arrow object
	  * @return
	  */
	public CommentArrow getArrow() {
		return arrow;
	}


	
	/**
	 * @return the width
	 */
	int getMyWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	void setMyWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	int getMyHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	void setMyHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the margin
	 */
	int getMargin() {
		return margin;
	}

	/**
	 * @param margin the margin to set
	 */
	void setMargin(int margin) {
		this.margin = margin;
	}

	/**
	 * Test application for comment.
	 * @param args
	 */
	 public static void main(String[] args) {
		 //Need comment source for comments
		 
		 /**
			JFrame f = new JFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setLayout(new BorderLayout());
			f.setSize(400, 400);
			JPanel p =new JPanel(null);
			p.add(new Comment("", null,  Color.green, 1.0), 0);
			f.add(p);
			p.add(new Comment("", null,  Color.orange, 1.0), 0);
			f.add(p);
			p.add(new Comment("", null,  Color.red, 1.0), 0);
			f.add(p);
			p.add(new Comment("", null,  Color.blue, 1.0), 0);
			f.add(p);
			f.setVisible(true);
		  */
	 }

	/**
	 * @return the pressed true, if this comment has been pressed
	 */
	boolean isPressed() {
		return pressed;
	}

	/**
	 * @param pressed true if this comment has been pressed
	 */
	void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	/**
	 * @return the resizing, true if this comment is being resized
	 */
	boolean isResizing() {
		return resizing;
	}

	/**
	 * @param resizing true if this comment is being resized
	 */
	void setResizing(boolean resizing) {
		this.resizing = resizing;
	}

	/**
	 * returns whether this comment should be constrained to its parent's bounds
	 * @return the constrainComment
	 */
	public boolean isConstrainComment() {
		return constrainComment;
	}

	/**
	 * sets whether this comment should be constrained to its parent's bounds
	 * @param constrainComment the constrainComment to set
	 */
	public void setConstrainComment(boolean constrainComment) {
		this.constrainComment = constrainComment;
	}
} 

