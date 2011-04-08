/*
 * Pt.java
 *
 * Created on October 31, 2006, 1:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.util;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Immutable point.
 *
 * @author Tim Boudreau
 */
public final class Pt implements Serializable{
    public final double x;
    public final double y;
    public static final Pt ORIGIN = new Pt (0,0);
    public Pt(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Pt(Point2D point) {
        this (point.getX(), point.getY());
    }

    public Rect center (final Size size) {
        double halfx = size.w / 2;
        double halfy = size.h / 2;
        return new Rect (x - halfx, y - halfy, size.w, size.h);
    }

    public boolean equals (Object o) {
        boolean result = o instanceof Pt;
        if (result) {
            Pt p = (Pt) o;
            result = p.x == x && p.y == y;
        }
        return result;
    }

    public int hashCode() {
        return (int) ((x + 1) * (y + 1)) * -100;
    }

    public String toString() {
        return "Pt<" + x + ',' + y + '>';
     }
}
