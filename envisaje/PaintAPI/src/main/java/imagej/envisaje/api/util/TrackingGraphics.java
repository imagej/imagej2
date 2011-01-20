/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.api.util;

import java.awt.Graphics2D;

/**
 * A Graphics object which can be notified of modified areas.
 * 
 * Implementations of the object are expected not to require a call to
 * areaModified() subsequent to normal painting operations;  the areaModified()
 * method exists for tools which operate directly on the backing raster
 * of a Surface object, so the Surface object cannot know on its own what
 * has been modified.
 *
 * @author Tim Boudreau
 */
public abstract class TrackingGraphics extends Graphics2D {
    public abstract void areaModified (int x, int y, int w, int h);
}
