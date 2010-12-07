package workspace;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

/**
 * The PageDivider is a JComponent graphically 
 * marking the boundary between the two pages.  
 * It holds a reference to the left page and invokes 
 * mutations to that page upon user-generated 
 * interactions with the PageDivider. Specifically, 
 * when a user presses the PageDivider, it forces the 
 * left page to recalculate the minimum width by invoking 
 * Page.reformMinimumPixelWidth().  When a user drags a 
 * PageDivider, it resizes the left pages’s abstract width 
 * by invoking Page.addPixelWidth().  Note that this only 
 * changes the page’s abstract width but does not re-render 
 * the left to reflect the change in the abstract width.  
 * This is done by various page listeners handled when 
 * the PageDivider informs the PageChangedEventListener 
 * to notify various PageChangeListeners.
 * 
 * We observe the following use cases:
 * 	1) 	User mouses over PageDivider
 * 		PageDivider grows in width
 * 	2) 	User moves mouse outside of PageDivider
 * 		PageDivider shrinks in width
 * 	3)	User presses on the PageDivider
 * 		Page.reformMinimumPixelWidth() is invoked
 * 	4) 	User drags the PageDivider
 * 		Page.addPixelWidth() is invoked
 * 		PageChangeListeners are invoked
 * 
 * @specfield leftPage : Page //the page on the left side of this PageDivider
 * @Specfield color : Color //the color of this pageDivider
 * @specfield mouseIn : boolean flag //true if and only if mouse is over this PageDivider
 */
public class PageDivider extends JComponent implements MouseListener, MouseMotionListener{
	private static final long serialVersionUID = 328149080272L;
	/** The color of all PageDividers */
	private static final Color DIVIDER_COLOR = Color.GRAY;
	/** A pointer to the left page of this PageDivider */
	private final Page leftPage;
	/** mouseIn Flag: true if and only if mouse is over this PageDivider */
	private boolean mouseIn = false;
	/** Drag Flag: true if and only if mosue is dragging this PageDivider */
	private boolean dragDone = false;
	/** The x corrdinate in pixel of the last mousePressed on this PageDivider */
	private int mPressedX;

	/**
	 * @param left - the left page belonging to the this PageDivider
	 * 
	 * @requires left != null
	 * @effects Constructs a new PageDivider that point to
	 * 			"left" as the this PageDivier's left page.
	 * 			Any user-generated triggers will mutate the
	 * 			"left" page.  This.color is set to Page.DIVIDER_COLOR.
	 */
	public PageDivider(Page left) {
		leftPage = left;
		setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	/**
	 * @return this.leftPage.  May NOT return null.
	 */
	public Page getLeftPage() {
		return leftPage;
	}

	/**
	 * renders this PageDivider to nornally be a line,
	 * or a thick line with a width of 3 if mouseIn flag
	 * is true.
	 */
	public void paintComponent(Graphics g) {
		g.setColor(DIVIDER_COLOR);
		g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
		if (mouseIn) {
			g.fillRect(getWidth() / 2 - 1, 0, 3, getHeight());
		}
	}
	
	/**
	 * @modifies this.leftPage
	 * @effects reforms the minimum width of this.leftPage
	 */
	public void mousePressed(MouseEvent e) {
		mPressedX = e.getX();
		leftPage.reformMinimumPixelWidth();
	}
	
	/**
	 * @effects all WorkspaceListeners
	 * @modies fires and notifys all WorkspaceListener of a Page_Resize event
	 */
	public void mouseReleased(MouseEvent e){
		if(dragDone){
			Workspace.getInstance().notifyListeners(new WorkspaceEvent(leftPage, WorkspaceEvent.PAGE_RESIZED, true));
			dragDone = false;
		}
	}
	
	/**
	 * @modifies mouseIn flag
	 * @effects sets mouseIn boolean flag to true
	 */
	public void mouseEntered(MouseEvent e) {
		mouseIn = true;
		repaint();
	}
	
	/**
	 * @modifies mouseIn flag
	 * @effects sets mouseIn boolean flag to false
	 */
	public void mouseExited(MouseEvent e) {
		mouseIn = false;
		repaint();
	}
	
	/**
	 * @modifies this.leftPage and all PageChangeListeners
	 * @effects adds the delta x change to the elft page's abstract width
	 * 			and informs all PageChangeListeners of this change
	 */
	public void mouseDragged(MouseEvent e) {
		leftPage.addPixelWidth(e.getX() - mPressedX);
		dragDone = true;
		PageChangeEventManager.notifyListeners();
	}
	
	public void mouseMoved(MouseEvent e) {}	    
	public void mouseClicked(MouseEvent e){}

}
