/*
 * ScalingConvolveOp.java
 *
 * Created on August 3, 2006, 2:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.effects;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 *
 * @author Tim Boudreau
 */
public class ScalingConvolveOp implements BufferedImageOp {
    private float factor;
    private ConvolveOp convolve;
    public ScalingConvolveOp(Kernel kernel, float factor) {
        this.factor = factor;
        this.convolve = new ConvolveOp (kernel);
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        return null;
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle2D.Float (0, 0, src.getWidth(), src.getHeight());
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        return null;
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) dstPt = new Point2D.Float();
        dstPt.setLocation(srcPt);
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        return null;
    }
}
