/*
 * PaintParticipant.java
 *
 * Created on September 28, 2006, 4:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.spi.tools;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Provided by a tool that wishes to participate in the painting of the 
 * image it is editing without committing what it paints to the image the
 * user is editing.
 * Useful for modal tools in which the user edits and then performs some
 * gesture to commit the edit, such as a tool that draws connected lines
 * by individual clicks and ends a series of lines with a double click
 * or enter keystroke to indicate that the edited image should really
 * be updated;  up to that point the paint() method will be called only
 * to draw on top of the surface of the designer, not to really draw
 * into the backing image.
 *
 * @author Tim Boudreau
 */
public interface PaintParticipant {
    /**
     * Provide a repainter that can be used for callbacks to request repaints
     * and commits and similar.  The Tool that implements this method should
     * cache the result and use it to request repaints (but it should null its
     * reference to the Repainter when Tool.detach() is called).
     * <p>
     * Note it is not specified whether attachRepainter() will be called before
     * or after Tool.attach() and Tool implementations should handle both 
     * cases gracefully.
     */
    public void attachRepainter (Repainter repainter);
    /**
     * Paint whatever content the tool needs to paint.  This method may be 
     * called with a Graphics2D object that allows it to paint on the surface
     * of the designer without altering the image being edited, or if 
     * <code>commit</code> is true, it may be used to do actual painting that
     * <i>does</i> alter the image being edited.  Commit will only be true
     * following a call to Repainter.requestCommit().
     * @param g2d the Graphics context
     * @param layerBounds the rectangle the result should scaled into
     * @param commit Whether or not this call is going to actually alter the 
     *  image being edited.  Implementations may want to use different logic
     *  in that situation (for example, not painting drag control points)
     */
    public void paint (Graphics2D g2d, Rectangle layerBounds, boolean commit);
    
    public interface Repainter {
        /**
         * Request a repaint of all visible UI.  Where possible, prefer 
         * requestRepaint(Rectangle) to this method.  Call this method
         * only if you really don't know the bounds of the area that needs
         * to be repainted - where possible, try to calculate it and pass
         * a rectangle instead.
         */
        public void requestRepaint();
        /**
         * Request a repaint of a given rectangle.  Typically a tool will call
         * this method after it has modified some area of the image being
         * edited.
         * @param bounds A rectangle representing the bounds of the area that
         * should be painted relative to the image being edited (zoom factors
         * will be taken care of by the callee).
         */
        public void requestRepaint (Rectangle bounds);
        /**
         * Set the cursor of the image editor.  If tracking mouse motion, a tool
         * may want to call this to indicate what actions are available.
         * @param cursor The cursor the designer should display
         */
        public void setCursor (Cursor cursor);
        /**
         * Request a callback to paint() that will actually update the edited
         * image.  This is used by tools which implement PaintParticipant to 
         * provide a preview of what will be drawn - for example, anything that
         * involves defining multiple points.  Call this method when the user
         * has performed some gesture that indicates that an edit has been 
         * completed.
         */
        public void requestCommit();
        /**
         * Get a component that dialogs, popup menus, etc. can be parented to.
         * Do not cast the result of this call to any particular type, or attempt
         * to alter that component's state - what is returned by this method
         * is implementation-dependent.
         * @return A component that can be used for parenting.
         */
        public Component getDialogParent();
    }
}
