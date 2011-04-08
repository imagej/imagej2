/*
 * Line.java
 *
 * Created on September 27, 2006, 6:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.elements;

import imagej.envisaje.api.vector.Adjustable;
import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Strokable;
import imagej.envisaje.api.vector.Vector;
import imagej.envisaje.api.vector.util.Pt;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;


/**
 *
 * @author Tim Boudreau
 */
public class Line implements Strokable, Adjustable, Vector {
    public double x1;
    public double x2;
    public double y1;
    public double y2;
    public long serialVersionUID = 23923214L;
    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public String toString() {
        return "Line " + x1 + ", " + y1 +
                "->" + x2 + ", " + y2;
    }

    public Shape toShape() {
        return new Line2D.Double(x1, y1, x2, y2);
    }

    public void paint(Graphics2D g) {
//        g.drawLine (x1, y1, x2, y2);
        g.draw (toShape());
    }

    public int getControlPointCount() {
        return 2;
    }

    public void getControlPoints (double[] xy) {
        xy[0] = this.x1;
        xy[1] = this.y1;
        xy[2] = this.x2;
        xy[3] = this.y2;
    }

    public Strokable create(int[] xp, int[] yp) {
        return new Line (xp[0], yp[0], xp[1], yp[1]);
    }

    public void getBounds(Rectangle r) {
        r.x = (int) Math.floor(x1);
        r.y = (int) Math.floor(y1);
        r.width = (int) Math.ceil(x2 - x1);
        r.height = (int) Math.ceil(y2 - y1);
    }

    public void getBounds (Rectangle2D.Double r) {
        double wid = x2 - x1;
        double hi = y2 - y1;
        double x, y, w, h;
        if (wid < 0) {
            wid = -wid;
            x = x2;
        } else {
            x = x1;
        }
        w = wid;
        if (hi < 0) {
            hi = -hi;
            y = y2;
        } else {
            y = y1;
        }
        h = hi;
        r.setRect(x, y, w, h);
    }

    public void draw(Graphics2D g) {
        paint (g);
    }

    public Primitive copy() {
        return new Line (x1, y1, x2, y2);
    }

    public Pt getLocation() {
        return new Pt (x1, y1);
    }

    public void setLocation(double x, double y) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        x1 = x;
        y1 = y;
        x2 = x1 + dx;
        y2 = y1 + dy;
    }

    public void clearLocation() {
        double offx = x2 - x1;
        double offy = y2 - y1;
        x1 = 0;
        y1 = 0;
        x2 = offx;
        y2 = offy;
        if (x2 < 0 || y2 < 0) {
            x2 = 0;
            y2 = 0;
            x1 = -offx;
            y1 = -offy;
        }
    }

    public Vector copy(AffineTransform xform) {
        double[] pts = new double[] {
            x1, y1, x2, y2
        };
        xform.transform(pts, 0, pts, 0, 2);
        x1 = pts[0];
        y1 = pts[1];
        x2 = pts[2];
        y2 = pts[3];
        return new Line (x1, y1, x2, y2);
    }

    public int[] getVirtualControlPointIndices() {
        return EMPTY_INT;
    }

    public boolean equals (Object o) {
        boolean result = o instanceof Line;
        if (result) {
            Line l = (Line) o;
            result = l.x1 == x1 && l.x2 == x2 && l.y1 == y1 && l.y2 == y2;
        }
        return result;
    }

    public int hashCode() {
        return toShape().hashCode() * 17;
    }

    public void setControlPointLocation(int pointIndex, Pt location) {
        switch (pointIndex) {
            case 0 :
                x1 = location.x;
                y1 = location.y;
                break;
            case 1 :
                x2 = location.x;
                y2 = location.y;
                break;
            default :
                throw new IllegalArgumentException (Integer.toString(pointIndex));
        }
    }
}
