/*
 * ColorWrapper.java
 *
 * Created on October 23, 2006, 9:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.graphics;

import imagej.envisaje.api.vector.Attribute;
import imagej.envisaje.api.vector.Primitive;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Tim Boudreau
 */
public class ColorWrapper implements Primitive, PaintWrapper, Attribute <Color> {
    public final int r, g, b, a;
    public ColorWrapper(int r, int g, int b, int a) {
        this.r = r >= 0 ? r :  255-r;
        this.g = g >= 0 ? g :  255-g;
        this.b = b >= 0 ? b :  255-b;
        this.a = a >= 0 ? a :  255-a;
    }

    public ColorWrapper (Color color) {
        this (color.getRed(), color.getGreen(),
                 color.getBlue(), color.getAlpha());
    }

    public Color toColor() {
        return new Color (r, g, b, a);
    }

    public void paint(Graphics2D g) {
        g.setPaint (toPaint());
    }

    public boolean equals (Object o) {
        boolean result = o != null && o.getClass() == getClass();
        if (result) {
            ColorWrapper c = (ColorWrapper) o;
            result = c.r == r && c.b == b && c.g == g && c.a == a;
        }
        return result;
    }

    public int hashCode() {
        return getClass().hashCode() + toColor().hashCode();
    }

    public Paint toPaint() {
        return toColor();
    }

    public Primitive copy() {
        return new ColorWrapper (r, g, b, a);
    }

    public PaintWrapper createScaledInstance(AffineTransform xform) {
        return (PaintWrapper) copy();
    }

    public Color get() {
        return toColor();
    }

    public String toString() {
        return super.toString() + ":" + get();
    }
}
