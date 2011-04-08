/*
 * RepaintHandle.java
 *
 * Created on October 15, 2005, 8:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.painting;

import imagej.envisaje.api.vector.Primitive;


/**
 * Handle for communication between a WrapperGraphics and its owner.
 * Allows the graphics object to request a repaint of any area that has
 * been modified (so a draw to a backing image can trigger a repaint of
 * a UI representing that image, and only update the changed area).
 *
 * @author Timothy Boudreau
 */
public interface VectorRepaintHandle {
    /**
     * Notify that the area bounded by the passed bounding box has been
     * painted into.
     */
    public void repaintArea (int x, int y, int w, int h);
    /**
     * After a drawing operation has been completed, provides a vector
     * primitive describing what has been done.
     */
    public void drawn (Primitive shape);
    /**
     * Pass a vector primitive to this graphics object, causing it to be
     * painted.  Note that this will not generate a subsequent call to 
     * drawn().
     */ 
    public void draw (Primitive shape);
}
