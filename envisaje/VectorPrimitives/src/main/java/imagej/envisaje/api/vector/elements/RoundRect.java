/*
 * RoundRect.java
 *
 * Created on September 27, 2006, 6:36 PM
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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;


/**
 *
 * @author Tim Boudreau
 */
public class RoundRect implements Vector, Volume, Adjustable, Fillable, Strokable {
    public long serialVersionUID = 39201L;
    public double aw;
    public double ah;
    public double x;
    public double y;
    public double w;
    public double h;
    public boolean fill;

    public RoundRect(double x, double y, double w, double h, double aw, double ah, boolean fill) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.aw = aw;
        this.ah = ah;
        this.fill = fill;
    }

    public String toString() {
        return "RoundRect " + x  + ", " + y + ", " + w
                + ", " + h + " fill: " + fill;
    }

    public Shape toShape() {
        return new RoundRectangle2D.Double (x, y, w, h, aw, ah);
    }

    public void paint(Graphics2D g) {
        if (fill) {
            fill (g);
        } else {
            draw (g);
        }
    }

//    public void getControlPoints(double[] xy) {
//        double halfh = h / 2;
//        double halfw = w / 2;
//        xy[0] = x + halfw;
//        xy[1] = y;
//        xy[2] = x + w;
//        xy[3] = y + halfh;
//        xy[4] = x + halfw;
//        xy[5] = y + h;
//        xy[6] = x;
//        xy[7] = y + halfw;
//    }
//
    public Primitive copy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void getBounds (Rectangle2D.Double r) {
        double wid = w;
        double hi = h;
        double xx, yy, ww, hh;
        if (wid < 0) {
            wid = -wid;
            xx = x + w;
        } else {
            xx = x;
        }
        ww = wid;
        if (hi < 0) {
            hi = -hi;
            yy = y + h;
        } else {
            yy = y;
        }
        hh = hi;
        r.setRect(xx, yy, ww, hh);
    }

    public int getControlPointCount() {
        return 4;
    }

    public void fill(Graphics2D g) {
        g.fill (toShape());
    }

    public boolean isFill() {
        return fill;
    }

    public void draw(Graphics2D g) {
        g.draw (toShape());
    }

    public Pt getLocation() {
        return new Pt (x, y);
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void clearLocation() {
        setLocation (0D, 0D);
    }

    public Vector copy(AffineTransform transform) {
        double[] pts = new double[] {
            x, y, x + w, y + h,
        };
        transform.transform(pts, 0, pts, 0, 2);
        return new RoundRect (pts[0], pts[1], pts[2] - pts[0],
                pts[3] - pts[1], aw, ah, fill);
    }

    public int[] getVirtualControlPointIndices() {
        return EMPTY_INT;
    }

    public boolean equals (Object o) {
        boolean result = o instanceof RoundRect;
        if (result) {
            RoundRect r = (RoundRect) o;
            result = r.h == h && r.w == w && r.x == x && r.y == y;
        }
        return result;
    }

    public int hashCode() {
        return getClass().hashCode() + ((int) ((x * y) + (w * h)));
    }

    public void getControlPoints(double[] xy) {
        xy[0] = x;
        xy[1] = y;
        
        xy[2] = x;
        xy[3] = y + h;
        
        xy[4] = x + w;
        xy[5] = y + h;
        
        xy[6] = x + w;
        xy[7] = y;
    }    

    public void setControlPointLocation(int pointIndex, Pt pt) {
        switch (pointIndex) {
            case 0 :
                h += x - pt.x;
                w += y - pt.y;
                x = pt.x;
                y = pt.y;
                break;
            case 1 :
                w += x - pt.x;
                x = pt.x;
                h = pt.y - y;
                break;
            case 2 :
                w = pt.x - x;
                h = pt.y - y;
                break;
            case 3 :
                w = pt.x - x;
                h += y - pt.y;
                y = pt.y;
                break;
            default :
                throw new IllegalArgumentException (Integer.toString(pointIndex));
        }
        renormalize();
    }
    
    private void renormalize() {
        if (w < 0) {
            double ww = w;
            x += w;
            w *= -1;
        }
        if (h < 0) {
            y += h;
            h *= -1;
        }
    }
    
}
