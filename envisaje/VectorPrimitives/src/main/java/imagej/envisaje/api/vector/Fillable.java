/*
 * Fillable.java
 *
 * Created on October 30, 2006, 9:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector;

import java.awt.Graphics2D;

/**
 * Represents a graphical element with an interior that can be
 * painted into
 *
 * @author Tim Boudreau
 */
public interface Fillable extends Volume {
    /**
     * Fill this graphical element according to its current
     * coordinates, using the Paint object currently returned
     * by the passed Graphics object's getPaint() method.
     * This method should function even if isFill() returns false.
     */
    public void fill(Graphics2D g);
    /**
     * Determine if this graphical element is to be filled.
     */
    public boolean isFill();
}
