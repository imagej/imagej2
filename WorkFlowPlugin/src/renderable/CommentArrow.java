package renderable;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * Draws an arrow (triangle to make the comment look like a speech bubble) between a comment and its source.
 * 
 * 
 * @author joshua
 *
 */
public class CommentArrow {
	Comment comment;

	/**Internal Timer*/
	Arrow arrow;
	private boolean active = false;
	
	/**Constructs this*/
	public CommentArrow(Comment comment) {
		this.comment = comment;
	}

	/**
     * Set new location for arrow
     */
   public void setLocation(int x, int y){
   	arrow.setLocation(x, y);
   	updateArrow();
   }
   
   /** 
    * set new location for arrow
    * @param p
    */
   public void setLocation(Point p){
	   setLocation(p.x, p.y);
   }
	
   /**
    * update arrow properties according to current location of comment and commentSource
    */
	public void updateArrow() {
		Container parentContainer = comment.getParent();
		
		if(comment.getCommentSource() != null && parentContainer != null){
			//Point start = SwingUtilities.convertPoint(commentSource, commentSource.getLocation(), parentContainer);
			Point start = (Point) (comment.getCommentSource().getCommentLocation());
			Point end = (Point) (comment.getLocation());
			end.translate(comment.getWidth()/2,comment.getHeight()/2);
			
			if (arrow == null) arrow = new Arrow();
			if (arrow != null && arrow.getParent() != null) {
				arrow.getParent().remove(arrow);
			}

    		double dx = (end.x-start.x);
    		double dy = (end.y-start.y);
    		double length = Math.sqrt(dx*dx + dy*dy);
    		
    		if (length>0) {
    			dx = dx/length;
    			dy = dy/length;
    		}

    		arrow.setXpoints((int) (end.x - dy*10), start.x, (int) (end.x + dy*10));
			arrow.setYpoints((int) (end.y + dx*10), start.y, (int) (end.y - dx*10));
			arrow.updateArrow();
			
			if (arrow.getParent() == null) {
				parentContainer.add(arrow);
			}
			parentContainer.validate();
			parentContainer.repaint();
    		active = true;
		}
	}
    	    	
	/**
	 * Returns whether this  animation is active
	 * @return the active
	 */
	boolean isActive() {
		return active;
	}
	
	/**
	 * Sets the visibility of the arrow component
	 *
	 */
	public void setVisible(boolean b) {
		arrow.setVisible(b);
	}
	
	/**
	 * Class in charge of drawing actual triangular arrow between the comment and the CommentSource
	 * @author joshua
	 *
	 */
	private class Arrow extends JComponent {
		public int[] xpoints;
		public int[] ypoints;
		int minx, miny;
			
		private static final long serialVersionUID = 328149080427L;
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	 		g2.setColor(comment.getBorderColor().darker());
			g2.fillPolygon(xpoints, ypoints, 3);
		}

		/**
		 * Set the three x coordinates of the triangle (in the coordinate system containing the comment and commentSource)
		 * @param x1
		 * @param x2
		 * @param x3
		 */
		void setXpoints(int x1, int x2, int x3) { 
			xpoints = new int[] {x1, x2, x3}; 
			minx = Math.min(xpoints[0], Math.min(xpoints[1], xpoints[2]));
			xpoints[0] = xpoints[0] - minx;
			xpoints[1] = xpoints[1] - minx;
			xpoints[2] = xpoints[2] - minx;
		}

		/**
		 * Set the three y coordinates of the triangle (in the coordinate system containing the comment and commentSource)
		 * @param y1
		 * @param y2
		 * @param y3
		 */
		void setYpoints(int y1, int y2, int y3) { 
			ypoints = new int[] {y1, y2, y3}; 
			miny = Math.min(ypoints[0], Math.min(ypoints[1], ypoints[2]));
			ypoints[0] = ypoints[0] - miny;
			ypoints[1] = ypoints[1] - miny;
			ypoints[2] = ypoints[2] - miny;
		}

		/**
		 * should be called after points are changed
		 */
		void updateArrow() {
			int w = Math.max(xpoints[0], Math.max(xpoints[1],xpoints[2]));
			int h = Math.max(ypoints[0], Math.max(ypoints[1],ypoints[2]));
    		setBounds(minx, miny, w, h);
		}
	}

	/**
	 * This parent for this arrow
	 * @return
	 */
	public Container getParent() {
		return arrow.getParent();
	}	
	
}
