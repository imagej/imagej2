/*
 * StringWrapper.java
 *
 * Created on September 27, 2006, 6:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.elements;

import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Vector;
import imagej.envisaje.api.vector.util.Pt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;


/**
 * Represents a string painted to a graphics context
 *
 * @author Tim Boudreau
 */
public class StringWrapper implements Vector {
    public long serialVersionUID = 72305123414L;
    public String string;
    public double x;
    public double y;
    public StringWrapper(String string, double x, double y) {
        this.x = x;
        this.y = y;
        this.string = string;
        assert string != null;
    }

    public String toString() {
        return "StringWrapper '" + string + "' @ " +
                x + ", " + y;
    }

    public Shape toShape() {
       return new java.awt.Rectangle (0,0,0,0); //XXX use GlyphVector?
    }

    public boolean equals(Object o) {
        boolean result = o instanceof StringWrapper;
        if (result) {
            StringWrapper sw = (StringWrapper) o;
            result = string.equals (sw.string);
        }
        return result;
    }

    public int hashCode() {
        return string.hashCode() * 31;
    }

    public void paint(Graphics2D g) {
        g.drawString (string, (float) x, (float) y);
    }

    public void getBounds(Rectangle2D.Double r) {
        //XXX fixme
        r.setRect (x, y, 1000, 20);
    }

    public Primitive copy() {
        return new StringWrapper (string, x, y);
    }

    public Pt getLocation() {
        return new Pt (x, y);
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void clearLocation() {
        setLocation (0, 0);
    }

    public Vector copy(AffineTransform transform) {
        double[] pts = new double [] { x, y };
        transform.transform (pts, 0, pts, 0, 1);
        return new StringWrapper (string, pts[0], pts[1]);
    }
}
