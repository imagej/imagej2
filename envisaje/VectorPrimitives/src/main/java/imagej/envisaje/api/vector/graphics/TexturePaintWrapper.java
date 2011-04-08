/*
 * TexturePaintWrapper.java
 *
 * Created on October 31, 2006, 9:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.graphics;

import imagej.envisaje.api.vector.Attribute;
import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.elements.ImageWrapper;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Tim Boudreau
 */
public class TexturePaintWrapper implements Primitive, PaintWrapper, Attribute <TexturePaint> {
    public ImageWrapper image;
    public double x;
    public double y;
    public double w;
    public double h;
    public TexturePaintWrapper(TexturePaint p) {
        Rectangle2D anchorRect = p.getAnchorRect();
        BufferedImage img = p.getImage();
        x = anchorRect.getX();
        y = anchorRect.getY();
        w = anchorRect.getWidth();
        h = anchorRect.getHeight();
        image = new ImageWrapper (img);
    }

    private TexturePaintWrapper(double x, double y, double w, double h, ImageWrapper i) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.image= (ImageWrapper) i.copy();
    }

    public void paint(Graphics2D g) {
        g.setPaint (toPaint());
    }

    public Paint toPaint() {
        return get();
    }

    public Color toColor() {
        return Color.BLACK;
    }

    public Primitive copy() {
        return new TexturePaintWrapper (x, y, w, h, (ImageWrapper) 
            image.copy());
    }

    public PaintWrapper createScaledInstance(AffineTransform xform) {
        double[] pts = new double[] { 
            x, y, x + w, y + h,
        };
        xform.transform(pts, 0, pts, 0, 2);
        return new TexturePaintWrapper (pts[0], pts[1], pts[2] - pts[0],
                pts[3] - pts[1], image);
    }

    public TexturePaint get() {
       BufferedImage img = image.img;
       Rectangle2D bds = new Rectangle2D.Double (x, y, w, h);
       return new TexturePaint (img, bds);
    }
}
