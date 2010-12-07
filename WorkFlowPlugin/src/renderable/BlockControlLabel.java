package renderable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import codeblocks.BlockConnectorShape;

/**
 * BlockControlLabel is a basic Label that exists on a given block that is used to control a property that may be toggled between two states.  This class should be extended to handle the particular states that are toggled.
 * @author joshua
 *
 */
class BlockControlLabel extends JLabel implements MouseListener {
	private static final long serialVersionUID = 1L;
	private boolean active = false;
    private long blockID;
	
    /**
     * Creates a new BlockControlLabel that controls the given block that corresponds to blockID
     * this must be added to the block by the method creating this Label.
     * @param blockID
     */
	 BlockControlLabel(long blockID) {
		this.blockID = blockID;
		this.setFont(new Font("Courier", Font.BOLD, (int)(14)));
		this.setForeground(new Color(255,255,255));
		this.setBorder(BorderFactory.createLineBorder(Color.gray));//show white border
		this.setOpaque(false);
		this.setHorizontalAlignment(SwingConstants.CENTER );
		this.setVerticalAlignment(SwingConstants.CENTER );
		this.addMouseListener(this);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * setup current visual state of button 
	 */
	public void update() {
		RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);
		
		if (rb != null) {
	        int x = 0;
	        int y = 0;
	        
	        y += rb.getBlockHeight()/rb.getZoom() - 22 + (isActive()?BlockConnectorShape.CONTROL_PLUG_HEIGHT:0);
	        y -= 20;
	        x += 12;
	        x=rb.rescale(x);
	        y=rb.rescale(y);
	        
			setLocation(x, y);
			setSize(rb.rescale(14), rb.rescale(14));
	
			if (isActive()) {
				setText("?");
			} else {
				setText("-");
			}
		}
	}

	/**
	 * updates BlockControlLbabel before painting
	 */
	public void paint(Graphics g) {
		update();
		super.paint(g);
	}
			

	/**
	 * Sets the active property
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * gets the active property
	 */
	public boolean isActive() {
		return active;
	}
	
	 /**
	  * toggles the active state of this label
	  * @return
	  */
	void toggle() {
		setActive(!isActive());
	}
	 
	 /**
	  * Sets the block ID for this BlockControlLabel
	  * @param blockID
	  */
	 void setBlockID(long blockID) {
		 this.blockID = blockID;
	 }
	
	 /**
	  * Returns the block ID for this BlockControlLabel
	  * @return
	  */
	 long getBlockID() {
		 return blockID;
	 }
        
    /**
     * update zoom for this button
     * @param newZoom
     */
    public void setZoomLevel(double newZoom) {
    	Font renderingFont;
        AffineTransform at = new AffineTransform();
        at.setToScale(newZoom, newZoom);
        renderingFont = this.getFont().deriveFont(at);
    	this.setFont(renderingFont);
    	this.repaint();
    }

    /**
     * Implement MouseListener interface
     * toggle collapse state of block if button pressed
     */
	public void mouseClicked(MouseEvent e) {
		//Nothing to do on mouseClicked
	}
	
    /**
     * Implement MouseListener interface
     * highlight button state
	 */
	public void mouseEntered(MouseEvent e) {			
		this.setBorder(BorderFactory.createLineBorder(Color.white));
	}

    /**
     * Implement MouseListener interface
     * de-highlight button state
	 */
	public void mouseExited(MouseEvent e) {
		this.setBorder(BorderFactory.createLineBorder(Color.gray));
	}

    /**
     * Implement MouseListener interface
	 */
	public void mousePressed(MouseEvent e) {
		//Nothing to do on mousePressed
	}

    /**
     * Implement MouseListener interface
	 */
	public void mouseReleased(MouseEvent e) {
		//Nothing to do on mouseReleased
	}
}
