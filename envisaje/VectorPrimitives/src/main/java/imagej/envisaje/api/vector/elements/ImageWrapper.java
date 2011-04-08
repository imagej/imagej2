/*
 * ImageWrapper.java
 *
 * Created on September 27, 2006, 6:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.elements;

import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Vector;
import imagej.envisaje.api.vector.util.Pt;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import javax.imageio.ImageIO;


/**
 *
 * @author Tim Boudreau
 */
public class ImageWrapper implements Primitive, Vector {
    public transient BufferedImage img;
    public double x;
    public double y;

    public ImageWrapper(RenderedImage img) {
        this.img = getBufferedImage(img, false);
        x = 0;
        y = 0;
    }

    public ImageWrapper(RenderableImage img) {
        this.img = getBufferedImage(img);
        x = 0;
        y = 0;
    }

    public ImageWrapper(RenderedImage img, double x, double y) {
        this.img = getBufferedImage(img, false);
        this.x = x;
        this.y = y;
    }

    public ImageWrapper(RenderableImage img, double x, double y) {
        this.img = getBufferedImage(img);
        this.x = x;
        this.y = y;
    }

    public ImageWrapper (double x, double y, Image img) {
        this.img = getBufferedImage (img);
        this.x = x;
        this.y = y;
    }

    private static BufferedImage getBufferedImage (Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        } else {
            BufferedImage result = new BufferedImage (img.getWidth(null), img.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = result.createGraphics();
            g2d.drawImage(img, AffineTransform.getTranslateInstance(0, 0), null);
            g2d.dispose();
            return result;
        }
    }

    private static BufferedImage getBufferedImage (RenderedImage img, boolean force) {
        if (img instanceof BufferedImage && !force) {
            return (BufferedImage) img;
        } else {
            BufferedImage result = new BufferedImage (img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = result.createGraphics();
            g2d.drawRenderedImage(img, AffineTransform.getTranslateInstance(0,0));
            g2d.dispose();
            return result;
        }
    }

    private static BufferedImage getBufferedImage (RenderableImage img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        } else {
            BufferedImage result = new BufferedImage ((int) img.getWidth(), (int) img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = result.createGraphics();
            g2d.drawRenderableImage(img, AffineTransform.getTranslateInstance(0,0));
            g2d.dispose();
            return result;
        }
    }

     private void writeObject(ObjectOutputStream out) throws IOException {
            ImageIO.write(img, "jpg", out);
            out.writeDouble(x);
            out.writeDouble (y);
     }

     private void readObject(ObjectInputStream in)   throws IOException, ClassNotFoundException {
            img = ImageIO.read(in);
            x = in.readDouble();
            y = in.readDouble();
     }

     private void readObjectNoData() throws ObjectStreamException {
            img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            x = 0;
            y = 0;
     }

     public String toString() {
         return "ImageWrapper " + img;
     }

     public void paint (Graphics2D g) {
         g.drawRenderedImage(img, AffineTransform.getTranslateInstance (x, y));
     }

    public void getBounds (java.awt.Rectangle r) {
        r.setBounds((int)Math.floor(x), (int) Math.floor(y), 
                img.getWidth(), img.getHeight());
    }

    public Primitive copy() {
        BufferedImage nue = getBufferedImage (img, true);
        return new ImageWrapper (x, y, nue);
    }

    public Shape toShape() {
        return new Rectangle2D.Double (x, y, img.getWidth(), img.getHeight());
    }

    public Pt getLocation() {
        return new Pt ((int)x, (int)y);
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void clearLocation() {
        this.x = 0;
        this.y = 0;
    }

    public Vector copy(AffineTransform xform) {
        double[] pts = new double[] {
            x, y, x + img.getWidth(), y + img.getHeight(),
        };
        xform.transform (pts, 0, pts, 0, 2);
        int nw = (int)(pts[2] - pts[0]);
        int nh = (int)(pts[3] - pts[1]);
        BufferedImage nue = 
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(nw, nh);
        Graphics2D g = nue.createGraphics();
        g.drawRenderedImage(img, xform);
        g.dispose();
        double nx = pts[0];
        double ny = pts[1];
        return new ImageWrapper (nue, nx, ny);
    }
}
