/*
 * Rectangle.java
 *
 * Created on September 27, 2006, 6:22 PM
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author Tim Boudreau
 */
public final class Rectangle implements Strokable, Fillable, Volume, Adjustable {
    public long serialVersionUID = 2354354L;
    public double h;
    public double x;
    public double y;
    public double w;
    public boolean fill;
    public Rectangle(double x, double y, double w, double h, boolean fill) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.fill = fill;
    }

    public String toString() {
        return "Rectangle " + x  + ", " + y + ", " + w
                + ", " + h + " fill: " + fill;
    }

    public Shape toShape() {
        return new Rectangle2D.Double(x, y, w, h);
    }

    public String toSvgFragment(Map <String, String> otherAttributes) {
        //PENDING:  Do this for other primitives
        if (!otherAttributes.keySet().containsAll(requiredAttributes())) {
            HashSet <String> set = new HashSet <String> (otherAttributes.keySet());
            set.removeAll (requiredAttributes());
            throw new IllegalArgumentException ("Missing attributes " + set);
        }
        StringBuilder bld = new StringBuilder("<");
        bld.append (getSvgName() + " x=\"");
        bld.append (x);
        bld.append ("\" y=\"");
        bld.append (y);
        bld.append ("\" width=\"");
        bld.append (w);
        bld.append ("\" height=\"");
        bld.append (h);
        bld.append ("\" fill=\"");
        bld.append (otherAttributes.get("fill"));
        bld.append ("\" stroke=\"");
        bld.append (otherAttributes.get("stroke"));
        bld.append ("\"/>");
        return bld.toString();
    }

    protected String getSvgName() {
        return "rect";
    }

    public Set <String> requiredAttributes() {
        return new HashSet <String> (Arrays.asList ("fill", "stroke"));
    }

    public boolean isFill() {
        return fill;
    }

    public boolean equals (Object o) {
        boolean result = o instanceof Rectangle;
        if (result) {
            Rectangle r = (Rectangle) o;
            result = r.h == h && r.w == w && r.x == x && r.y == y;
        }
        return result;
    }

    public int hashCode() {
        return getClass().hashCode() + ((int) ((x * y) + (w * h)));
    }

    public void paint(Graphics2D g) {
        if (fill) {
            fill (g);
        } else {
            draw (g);
        }
    }

    public int getControlPointCount() {
        return 4;
    }

    public void getBounds (Rectangle2D.Double r) {
        r.setRect(x, y, w, h);
    }

    public Strokable createInverseFilledInstance() {
        return new Rectangle (x, y, w, h, !fill);
    }

    public void draw(Graphics2D g) {
        g.draw (toShape());
    }

    public Primitive copy() {
        return new Rectangle (x, y, w, h, fill);
    }

    public void fill(Graphics2D g) {
        g.fill (toShape());
    }

    public Pt getLocation() {
        return new Pt (x, y);
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void clearLocation() {
        setLocation (0, 0);
    }

    public Vector copy(AffineTransform transform) {
        double[] pts = new double[] {
            x, y, x + w, y + h,
        };
        transform.transform (pts, 0, pts, 0, 2);
        return new Rectangle (pts[0], pts[1], 
                pts[2] - pts[0], pts[3] - pts[1], fill);
    }

    public int[] getVirtualControlPointIndices() {
        return EMPTY_INT;
    }
    
    public void getControlPoints(double[] xy) {
        assert xy.length >= 8 : "Array too small";
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
