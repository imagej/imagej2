/*
 * GradientPaintWrapper.java
 *
 * Created on September 28, 2006, 5:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.graphics;

import imagej.envisaje.api.vector.Attribute;
import imagej.envisaje.api.vector.Primitive;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 *
 * @author Tim Boudreau
 */
public final class GradientPaintWrapper implements Primitive, PaintWrapper, Attribute <GradientPaint> {
    public float[] color1;
    public float[] color2;
    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public boolean cyclic;
    public int alpha1;
    public int alpha2;
    public GradientPaintWrapper(GradientPaint gp) {
        color1 = gp.getColor1().getRGBComponents(null);
        color2 = gp.getColor2().getRGBComponents(null);
        alpha1 = gp.getColor1().getAlpha();
        alpha2 = gp.getColor2().getAlpha();
        Point2D p = gp.getPoint1();
        x1 = (float) p.getX();
        y1 = (float) p.getY();
        p = gp.getPoint2();
        x2 = (float) p.getX();
        y2 = (float) p.getY();
        cyclic = gp.isCyclic();
    }
    
    private GradientPaintWrapper (float[] color1, float[] color2,
            float x1, float y1, float x2, float y2, int alpha1, int alpha2, boolean cyclic) {
        this.color1 = new float [color1.length];
        System.arraycopy (color1, 0, this.color1, 0, color1.length);
        this.color2 = new float [color2.length];
        System.arraycopy (color2, 0, this.color2, 0, color2.length);
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
        this.cyclic = cyclic;
    }

    public GradientPaint toGradientPaint() {
        GradientPaint result = new GradientPaint (x1, y1, new Color (color1[0], color1[1], color1[2], color1[3]),
                x2, y2, new Color (color2[0], color2[1], color2[2], color2[3]), cyclic);

        return result;
    }

    public String toString() {
        return "GradientPaintWrapper " + toGradientPaint();
    }

    public void paint (Graphics2D g) {
        g.setPaint (toPaint());
    }

    public Paint toPaint() {
        return toGradientPaint();
    }

    public Color toColor() {
        return new Color (ColorSpace.getInstance(ColorSpace.TYPE_RGB), 
                color1, alpha1);
    }

    public Primitive copy() {
        return new GradientPaintWrapper (color1, color2, x1, 
                y1, x2, y2, alpha1, alpha2, cyclic);
    }

    public PaintWrapper createScaledInstance(AffineTransform xform) {
        float[] pts = new float[] {
            x1, y1, x2, y1,
        };
        xform.transform(pts, 0, pts, 0, pts.length);
        return new GradientPaintWrapper (color1, color2, pts[0],
                pts[1], pts[2], pts[3], alpha1, alpha2, cyclic);
    }

    public GradientPaint get() {
        return toGradientPaint();
    }
}
