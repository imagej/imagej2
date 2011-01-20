/*
 * LayerDropSupport.java
 *
 * Created on November 7, 2006, 4:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.spi;
import java.awt.geom.Point2D;

/**
 *
 * @author Tim Boudreau
 */
public interface LayerDropSupport {
    public boolean canDrop (Object o, Point2D point);
    public boolean drop (Object o, Point2D point);
}
