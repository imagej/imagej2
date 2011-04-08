/*
 * Clear.java
 *
 * Created on September 27, 2006, 8:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.elements;

import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Volume;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Tim Boudreau
 */
public final class Clear implements Volume, Primitive {
    private static long serialVersionUID = 101034L;
    public int x;
    public int y;
    public int width;
    public int height;
    public Clear(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = height;
    }

    public String toString() {
        return "Clear " + x + ',' + y + ',' + width + ',' + height;
    }

    public void paint(Graphics2D g) {
        g.clearRect(x, y, width, height);
    }

    public void getBounds (Rectangle2D.Double r) {
        r.setRect(x, y, width, height);
    }

    public Primitive copy() {
        return new Clear (x, y, width, height);
    }

    public boolean equals (Object o) {
        boolean result = o instanceof Clear;
        if (result) {
            Clear c = (Clear) o;
            result = c.x == x && c.y == y && c.width == width &&
                    c.height == height;
        }
        return result;
    }

    public int hashCode() {
        return ((x * y) + (width * height)) ^ 17;
    }

    public void setLocation(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }
}
