/*
 * Oval.java
 *
 * Created on September 27, 2006, 6:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.elements;

import imagej.envisaje.api.vector.Adjustable;
import imagej.envisaje.api.vector.Fillable;
import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Strokable;
import imagej.envisaje.api.vector.Vector;
import imagej.envisaje.api.vector.Volume;
import imagej.envisaje.api.vector.util.Pt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Tim Boudreau
 */
public class Oval implements Strokable, Fillable, Adjustable, Volume, Vector {
    public long serialVersionUID = 232354194L;
    public double x, y, width, height;
    public boolean fill;
    public Oval(double x, double y, double width, double height, boolean fill) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fill = fill;
    }

    public String toString() {
        return "Oval " + x  + ", " + y + ", " + width
                + ", " + height + " fill: " + fill;
    }

    public Shape toShape() {
        return new Ellipse2D.Double (x, y, width, height);
    }

    public void paint(Graphics2D g) {
        if (fill) {
            fill (g);
        } else {
            draw (g);
        }
    }

    public void getBounds (Rectangle2D.Double r) {
        double wid = width;
        double hi = height;
        double xx, yy, ww, hh;
        if (wid < 0) {
            wid = -wid;
            xx = x + width;
        } else {
            xx = x;
        }
        ww = wid;
        if (hi < 0) {
            hi = -hi;
            yy = y + height;
        } else {
            yy = y;
        }
        hh = hi;
        r.setRect(xx, yy, ww, hh);
    }

    public boolean isFill() {
        return fill;
    }

    public int getControlPointCount() {
        return 4;
    }

    public void draw(Graphics2D g) {
        g.draw (toShape());
    }

    public void fill(Graphics2D g) {
        g.fill (toShape());
    }

    public Primitive copy() {
        return new Oval (x, y, width, height, fill);
    }

//    public void getControlPoints(double[] xp) {
//        double halfh = height / 2;
//        double halfw = width / 2;
//        xp[0] = x + halfw;
//        xp[1] = y;
//        xp[2] = x + width;
//        xp[3] = y + halfh;
//        xp[4] = x + halfw;
//        xp[5] = y + height;
//        xp[6] = x;
//        xp[7] = y + halfh;
//    }

    public Pt getLocation() {
        return new Pt (x, y);
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void clearLocation() {
        x = 0D;
        y = 0D;
    }

    public Vector copy(AffineTransform xform) {
        double[] pts = new double[] {
            x, y, x + width, y + height,
        };
        xform.transform(pts, 0, pts, 0, 2);
        return new Oval (pts[0], pts[1],
                pts[2] - pts[0], pts[3] - pts[1], fill);
    }

    public int[] getVirtualControlPointIndices() {
        return EMPTY_INT;
    }

    public boolean equals (Object o) {
        boolean result = o instanceof Oval;
        if (result) {
            Oval v = (Oval) o;
            result = v.x == x && v.y == y && v.width == width && v.height == height;
        }
        return result;
    }

    public int hashCode() {
        return (int) ((width * height) + x + (y * width));
    }
    

    public void getControlPoints(double[] xy) {
        assert xy.length >= 8 : "Array too small";
        xy[0] = x;
        xy[1] = y;
        
        xy[2] = x;
        xy[3] = y + height;
        
        xy[4] = x + width;
        xy[5] = y + height;
        
        xy[6] = x + width;
        xy[7] = y;
    }    

    public void setControlPointLocation(int pointIndex, Pt pt) {
        switch (pointIndex) {
            case 0 :
                height += x - pt.x;
                width += y - pt.y;
                x = pt.x;
                y = pt.y;
                break;
            case 1 :
                width += x - pt.x;
                x = pt.x;
                height = pt.y - y;
                break;
            case 2 :
                width = pt.x - x;
                height = pt.y - y;
                break;
            case 3 :
                width = pt.x - x;
                height += y - pt.y;
                y = pt.y;
                break;
            default :
                throw new IllegalArgumentException (Integer.toString(pointIndex));
        }
        renormalize();
    }
    
    private void renormalize() {
        if (width < 0) {
            double ww = width;
            x += width;
            width *= -1;
        }
        if (height < 0) {
            y += height;
            height *= -1;
        }
    }    

//    public void setControlPointLocation(int pointIndex, Pt pt) {
//        switch (pointIndex) {
//            case 0 :
//                y = pt.y;
//                break;
//            case 1 :
//                x = pt.x;
//                break;
//            case 3 :
//                double oldy = y;
//                height = pt.y - y;
//                if (height < 0) {
//                    y = -height;
//                    height = oldy - y;
//                }
//                break;
//            case 2 :
//                double oldx = x;
//                width = pt.x - x;
//                if (width < 0) {
//                    x = -width;
//                    width = oldx - x;
//                }
//                break;
//                
//        }
//    }
}
