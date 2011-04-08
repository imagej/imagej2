/*
 * StrokedPrimitive.java
 *
 * Created on October 25, 2006, 10:05 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector;

import java.awt.Graphics2D;

/**
 * A graphics primitive which may have an outline that can be
 * drawn to a Graphics2D
 *
 * @author Tim Boudreau
 */
public interface Strokable extends Vector {
    /**
     * Draw the outline of the shape this object represents
     */
    public void draw(Graphics2D g);
}
