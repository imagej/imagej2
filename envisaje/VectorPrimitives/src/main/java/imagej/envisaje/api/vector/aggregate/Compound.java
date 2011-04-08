/*
 * Compound.java
 *
 * Created on October 31, 2006, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.aggregate;

import imagej.envisaje.api.vector.Adjustable;
import imagej.envisaje.api.vector.Aggregate;
import imagej.envisaje.api.vector.Fillable;
import imagej.envisaje.api.vector.Mutable;
import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Strokable;
import imagej.envisaje.api.vector.Vector;
import imagej.envisaje.api.vector.Volume;
import imagej.envisaje.api.vector.util.Pt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;


/**
 * Wrapper for a stack of primitives which are treated as a unit
 *
 * @author Tim Boudreau
 */
public class Compound implements Primitive, Strokable, Vector, Volume, Adjustable, Fillable, Mutable, Aggregate {
    private List <Primitive> contents = new ArrayList <Primitive> (10);
    public double x;
    public double y;
    public Compound(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add (Primitive primitive) {
        contents.add (primitive);
    }

    public void add (int ix, Primitive primitive) {
        contents.add (ix, primitive);
    }

    public void remove (Primitive primitive) {
        contents.remove (primitive);
    }

    public void remove (int ix) {
        contents.remove (ix);
    }

    public void moveUp (int ix) {
        moveUp (ix, contents.get(ix));
    }

    private void moveUp (int ix, Primitive primitive) {
        if (ix > 0) {
            contents.remove (ix);
            contents.add(ix - 1, primitive);
        }
    }

    public void moveDown (int ix) {
        moveDown (ix, contents.get(ix));
    }

    private void moveDown (int ix, Primitive primitive) {
        if (ix < contents.size() - 1) {
            contents.remove (ix);
            contents.add(ix - 1, primitive);
        }
    }

    public void moveUp (Primitive primitive) {
        int ix = indexOf (primitive);
        moveUp (ix, primitive);
    }

    public void moveDown (Primitive primitive) {
        int ix = indexOf (primitive);
        moveDown (ix, primitive);
    }

    public int indexOf (Primitive p) {
        return contents.indexOf(p);
    }

    public java.awt.Rectangle getBounds() {
        java.awt.Rectangle result = null;
        Rectangle2D.Double scratch = new Rectangle2D.Double (0,0,0,0);
        for (Primitive p : contents) {
            if (p instanceof Volume) {
                Volume vp = (Volume) p;
                vp.getBounds(scratch);
                if (result == null) {
                    result = new java.awt.Rectangle (scratch.getBounds());
                } else {
                    java.awt.Rectangle.union(scratch, result, result);
                }
//                System.err.println("Union with " + vp + " gets " + result);
            }
        }
        if (result == null) {
            Pt pt = getLocation();
            result = new java.awt.Rectangle ((int) pt.x, (int) pt.y, 0, 0);
        } else {
            result.translate((int) x, (int) y);
        }
        return result;
    }

    public void paint (Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.getTransform().concatenate(AffineTransform.getTranslateInstance(x, y));
        for (Primitive p : contents) {
            p.paint(g2);
        }
        g2.dispose();
    }

    public Shape toShape() {
        Area a = new Area ();
        for (Primitive p : contents) {
            if (p instanceof Vector) {
                Area b = new Area (((Vector) p).toShape());
                a.add(b);
            }
        }
        a.transform(AffineTransform.getTranslateInstance (x, y));
        return a;
    }

    public void getBounds(Rectangle2D.Double r) {
        Rectangle2D.Double scratch = null;
        for (Primitive p : contents) {
            if (p instanceof Volume) {
                if (scratch == null) {
                    scratch = new Rectangle2D.Double();
                    ((Volume) p).getBounds(r);
                } else {
                    ((Volume) p).getBounds(scratch);
                    r.union(scratch, r, r);
                }
            }
        }
        if (scratch == null) {
            r.width = 0;
            r.height = 0;
            r.x = x;
            r.y = y;
        } else {
            r.x += x;
            r.y += y;
        }
    }

    public Primitive copy() {
        Compound nue = new Compound (x, y);
        List <Primitive> l = new ArrayList <Primitive> (contents.size());
        for (Primitive p : contents) {
            l.add (p.copy());
        }
        nue.contents.addAll (l);
        return nue;
    }

    public void draw(Graphics2D g) {
        AffineTransform oldXform = g.getTransform();
        g.setTransform(AffineTransform.getTranslateInstance(x, y));
        for (Primitive p : contents) {
            if (p instanceof Strokable) {
                ((Strokable)p).draw(g);
            }
        }
        g.setTransform(oldXform);
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
        double[] pts = new double[] { x, y };
        transform.transform (pts, 0, pts, 0 , 1);
        Compound nue = new Compound (pts[0], pts[1]);
        nue.contents.addAll(contents);
        return nue;
    }

    public void fill(Graphics2D g) {
        AffineTransform oldXform = g.getTransform();
        g.setTransform(AffineTransform.getTranslateInstance(x, y));
        for (Primitive p : contents) {
            if (p instanceof Fillable) {
                ((Fillable)p).fill(g);
            }
        }
        g.setTransform(oldXform);
    }

    public boolean isFill() {
        return true; //XXX check primitives?
    }

    public boolean delete(int pointIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean insert(double x, double y, int index, int kind) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getPointIndexNearest(double x, double y) {
        Rectangle2D.Double r = new Rectangle2D.Double();
        getBounds (r);
        Point2D.Double[] d = points (r);
        double bestDistance = Double.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < d.length; i++) {
            Point2D.Double p = d[i];
            double dist = p.distance(x, y);
            if (dist < bestDistance) {
                bestDistance = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private Point2D.Double[] points (Rectangle2D.Double r) {
        return new Point2D.Double[] {
            new Point2D.Double (r.x, r.y),
            new Point2D.Double (r.x + r.width, r.y),
            new Point2D.Double (r.x + r.width, r.y + r.height),
            new Point2D.Double (r.x, r.y + r.height),
        };
    }

    public void setControlPointLocation(int pointIndex, Pt location) {
        int total = 0;
        location = new Pt (location.x - x, location.y - y);
        for (Primitive p : contents) {
            if (p instanceof Adjustable) {
                Adjustable a = (Adjustable) p;
                int count = a.getControlPointCount();
                if (total <= pointIndex && total + count > pointIndex) {
                    a.setControlPointLocation(pointIndex - total, location);
                }
                total += count;
            }
        }
    }
    
    public void getControlPoints(double[] xy) {
        int total = 0;
        List <double[]> l = new ArrayList <double[]> (contents.size());
        List <int[]> v = new ArrayList <int[]> (contents.size());
        for (Primitive p : contents) {
            if (p instanceof Adjustable) {
                Adjustable a = (Adjustable) p;
                int count = a.getControlPointCount();
                double[] d = new double [count * 2];
                a.getControlPoints(d);
                int[] vcp = a.getVirtualControlPointIndices();
                for (int i = 0; i < vcp.length; i++) {
                    vcp[i] += total;
                }
                v.add(vcp);
                l.add (d);
                total += count;
            }
        }
        int ix = 0;
        for (double[] d : l) {
            int len = d.length;
            System.arraycopy (d, 0, xy, ix, d.length);
            ix += len;
        }
    }
    
    public int getControlPointCount() {
        int total = 0;
        List <double[]> l = new ArrayList <double[]> (contents.size());
        for (Primitive p : contents) {
            if (p instanceof Adjustable) {
                Adjustable a = (Adjustable) p;
                int count = a.getControlPointCount();
                total += count;
            }
        }
        return total;
    }

    public int[] getVirtualControlPointIndices() {
        int total = 0;
        List <int[]> v = new ArrayList <int[]> (contents.size());
        for (Primitive p : contents) {
            if (p instanceof Adjustable) {
                Adjustable a = (Adjustable) p;
                int count = a.getControlPointCount();
                double[] d = new double [count * 2];
                a.getControlPoints(d);
                int[] vcp = a.getVirtualControlPointIndices();
                for (int i = 0; i < vcp.length; i++) {
                    vcp[i] += total;
                }
                v.add(vcp);
                total += vcp.length;
            }
        }
        int ix = 0;
        int[] result = new int [total];
        for (int[] i : v) {
            int len = i.length;
            System.arraycopy (i, 0, result, ix, i.length);
            ix += len;
        }
        return result;
    }

    public int getPrimitiveCount() {
        return contents.size();
    }

    public Primitive getPrimitive(int i) {
        return contents.get (i);
    }

    public int getVisualPrimitiveCount() {
        int result = 0;
        int max = getPrimitiveCount();
        for (int i=0; i < max; i++) {
            Primitive p = getPrimitive(i);
            if (p instanceof Vector || p instanceof Strokable || p instanceof Volume) {
                result ++;
            }
        }
        return result;
    }

    public Primitive getVisualPrimitive(int ix) {
        int result = 0;
        int max = getPrimitiveCount();
        for (int i=0; i < max; i++) {
            Primitive p = getPrimitive(i);
            if (p instanceof Vector || p instanceof Strokable || p instanceof Volume) {
                result ++;
                if (result == ix) {
                    return p;
                }
            }
        }
        throw new IndexOutOfBoundsException ("Only " + max + " present but " +
                "requested " + ix);
    }
}
