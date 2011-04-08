/*
 * PathModel.java
 *
 * Created on July 19, 2006, 3:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.splines;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Tim Boudreau
 */
public interface PathModel <T extends Entry> extends List <T>, Shape, Serializable {
    Hit hit (Point2D p, int areaSize);
    Hit hit (Point p, int areaSize);
    Set <Hit> hit (Rectangle r, boolean includeControlPoints);
    void addChangeListener (ChangeListener cl);
    void removeChangeListener (ChangeListener cl);
    void setPoint (Node node, Point2D where);
}
