/*
 * Volume.java
 *
 * Created on November 1, 2006, 6:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * A graphical element which has an interior.
 *
 * @author Tim Boudreau
 */
public interface Volume extends Primitive {
    public void getBounds (Rectangle2D.Double dest);
}
