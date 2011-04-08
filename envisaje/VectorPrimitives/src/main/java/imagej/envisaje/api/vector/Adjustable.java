/*
 * Adjustable.java
 *
 * Created on November 1, 2006, 6:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector;

import imagej.envisaje.api.vector.util.Pt;

/**
 * Represents a graphical element whose size, rotation or
 * position may be adjusted.
 *
 * @author Tim Boudreau
 */
public interface Adjustable extends Primitive {
    /**
     * Get the number of user-manipulable control points
     * for this object
     */
    public int getControlPointCount();
    /**
     * Get the control points of this object in an array
     * of coordinates x,y,x,y...
     * @param xy An array of doubles to write into, which
     *  must be at least as large as the value returned by
     *  getControlPointCount()
     */
    public void getControlPoints(double[] xy);

    public int[] getVirtualControlPointIndices();

    public void setControlPointLocation (int pointIndex, Pt location);
}
