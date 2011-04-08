/*
 * Background.java
 *
 * Created on October 30, 2006, 3:05 PM
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
 * Sets the background of a graphics context via
 * setBackground().
 *
 * @author Tim Boudreau
 */
public final class Background implements Attribute<Color>, Primitive, PaintWrapper {
    public int red;
    public int green;
    public int blue;
    public int alpha;
    public Background(Color c) {
        this.red = c.getRed();
        this.green = c.getGreen();
        this.blue = c.getBlue();
        this.alpha = c.getAlpha();
    }

    private Background (int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public void paint (Graphics2D g) {
        g.setBackground (toColor());
    }

    public Primitive copy() {
        return new Background (red, green, blue, alpha);
    }

    public Paint toPaint() {
        return toColor();
    }

    public Color toColor() {
        return new Color (red, green, blue, alpha);
    }

    public PaintWrapper createScaledInstance(AffineTransform xform) {
        return (PaintWrapper) copy();
    }

    public Color get() {
        return toColor();
    }
}
