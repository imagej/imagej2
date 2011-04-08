/*
 * Rect.java
 *
 * Created on October 31, 2006, 1:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.util;

import java.io.Serializable;

/**
 * An immutable rectangle.
 *
 * @author Tim Boudreau
 */
public final class Rect implements Serializable {
    private static long serialVersionUID = 5210124L;
    public final double x, y;
    public final Size size;
    /** Creates a new instance of Rect */
    public Rect(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        size = new Size (w, h);
    }

    public Rect(int x, int y, Size size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public Size getSize() {
        return size;
    }
    
    public Pt getOrigin() {
        return new Pt (x, y);
    }

    public boolean equals (Object o) {
        boolean result = o instanceof Rect;
        if (result) {
            Rect r = (Rect) o;
            result = r.x == x && r.y == y && r.size.equals(size);
        }
        return result;
    }

    public int hashCode() {
        return size.hashCode() + ((int)(x * y));
    }
}
