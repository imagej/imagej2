/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package imagej.envisaje.api.vector.elements;

import imagej.envisaje.api.vector.Adjustable;
import imagej.envisaje.api.vector.Fillable;
import imagej.envisaje.api.vector.Mutable;
import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Strokable;
import imagej.envisaje.api.vector.Vector;
import imagej.envisaje.api.vector.Volume;
import imagej.envisaje.api.vector.util.Pt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Wrapper for a PathIterator - i.e. any shape.
 *
 * @author Tim Boudreau
 */
public final class PathIteratorWrapper implements Strokable, Fillable, Volume, Adjustable, Vector, Mutable {
    private Segment[] segments;
    /** Creates a new instance of PathIteratorWrapper2 */

    public PathIteratorWrapper(PathIterator it) {
        this (it, false);
    }
    
    private final boolean fill;
    public PathIteratorWrapper(PathIterator it, boolean fill) {
        unpack (it);
        this.fill = fill;
    }
    
    public PathIteratorWrapper (GlyphVector gv, float x, float y) {
        this (gv.getOutline(x, y).getPathIterator(AffineTransform.getTranslateInstance(0, 0)));
    }
    
    PathIteratorWrapper (Segment[] segments, boolean fill) {
        this.segments = segments;
        this.fill = fill;
    }
    
    private void unpack (PathIterator it) {
        double[] d = new double [6];
        List <Segment> segments = new ArrayList <Segment> ();
        while (!it.isDone()) {
            Arrays.fill (d, 0D);
            byte type = (byte) it.currentSegment(d);
            segments.add (new Segment (d, type));
            it.next();
        }
        this.segments = segments.toArray (new Segment[segments.size()]);
    }
    
    private static final class Segment implements Serializable {
        final double[] data;
        final byte type;
        Segment (Segment other) {
            data = other.data == null ? null : new double [other.data.length];
            if (data != null) {
                System.arraycopy (other.data, 0, data, 0, other.data.length);
            }
            this.type = other.type;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            switch (type) {
                case PathIterator.SEG_MOVETO :
                    sb.append ("MoveTo " + data[0] +","+ data[1]);
                    break;
                case PathIterator.SEG_LINETO :
                    sb.append ("LineTo " + data[0] +","+ data[1]);
                    break;
                case PathIterator.SEG_QUADTO :
                    sb.append ("QuadTo " + data[0] +","+ data[1] +","+ data[2] +","+ data[3]);
                    break;
                case PathIterator.SEG_CUBICTO :
                    sb.append ("CubicTo " + data[0] +","+ data[1] +","+ data[2] +","+ data[3] +","+ data[4] +","+ data[5]);
                    break;
                case PathIterator.SEG_CLOSE :
                    sb.append ("Close");
                    break;
                default :
                    sb.append ("Unknown type " + type);
            }
            return sb.toString();
        }
        
        Segment (double[] data, byte type) {
            this.type = type;
            switch (type) {
                case PathIterator.SEG_MOVETO :
                    this.data = new double[] {
                        data[0], data[1],
                    };
                    break;
                case PathIterator.SEG_LINETO :
                    this.data = new double[] {
                        data[0], data[1],
                    };
                    break;
                case PathIterator.SEG_QUADTO :
                    this.data = new double[] {
                        data[0], data[1], data[2], data[3],
                    };
                    break;
                case PathIterator.SEG_CUBICTO :
                    this.data = new double[data.length];
                    System.arraycopy(data, 0, this.data, 0, data.length);
                    break;
                case PathIterator.SEG_CLOSE :
                    this.data = null;
                    break;
                default :
                    throw new AssertionError ("PathIterator provided unknown " +
                            "segment type " + type);
            }
        }
        
        int getPointCount() {
            switch (type) {
                case PathIterator.SEG_MOVETO :
                    return 1;
                case PathIterator.SEG_LINETO :
                    return 1;
                case PathIterator.SEG_QUADTO :
                    return 2;
                case PathIterator.SEG_CUBICTO :
                    return 3;
                case PathIterator.SEG_CLOSE :
                    return 0;
                default :
                    throw new AssertionError ("PathIterator provided unknown " +
                            "segment type " + type);
            }
        }
        
        void translate (double dx, double dy) {
            if (data != null) {
                for (int i=0; i < data.length; i += 2) {
                    data[i] += dx;
                    data[i + 1] += dy;
                }
            }
        }
        
        int copyData (double[] dest, int offset) {
            int count = (getPointCount() * 2);
            if (data != null) {
                System.arraycopy(data, 0, dest, offset, data.length);
            }
            return count;
        }
        
        int ixNearest (double x, double y, double[] dist) {
            int result = -1;
            double shortestLength = Integer.MAX_VALUE;
            if (data != null) {
                for (int i = 0; i < data.length; i+=2) {
                   double rx = x - data[i];
                   double ry = y - data[i+1];
                   dist[0] = Math.sqrt((rx * rx) + (ry * ry));
                   if (dist[0] < shortestLength) {
                       result = i / 2;
                   }
                }
            }
            return result;
        }
        
        public boolean equals (Object o) {
            boolean result = o instanceof Segment;
            if (result) {
                Segment s = (Segment) o;
                result = s.type == type;
                if (result) {
                    if (type != PathIterator.SEG_CLOSE) {
                        result = Arrays.equals(data, s.data);
                    }
                }
            }
            return result;
        }
        
        public int hashCode() {
            return data == null ? 1 : (Arrays.hashCode(data) * (type + 1) * 31);
        }
    }
    
    public void draw(Graphics2D g) {
        g.draw (toShape());
    }

    public Shape toShape() {
        GeneralPath path = new GeneralPath();
        for (int i = 0; i < segments.length; i++) {
            Segment seg = segments[i];
            switch (seg.type) {
                case PathIterator.SEG_MOVETO :
                    path.moveTo (seg.data[0], seg.data[1]);
                    break;
                case PathIterator.SEG_LINETO :
                    path.lineTo(seg.data[0], seg.data[1]);
                    break;
                case PathIterator.SEG_QUADTO :
                    path.quadTo(seg.data[0], seg.data[1], seg.data[2], seg.data[3]);
                    break;
                case PathIterator.SEG_CUBICTO :
                    path.curveTo(seg.data[0], seg.data[1], seg.data[2], seg.data[3],
                            seg.data[4], seg.data[5]);
                    break;
                case PathIterator.SEG_CLOSE :
                    path.closePath();
                    break;
                default :
                    throw new AssertionError ("PathIterator provided unknown " +
                            "segment type " + seg.type);
                    
            }
        }
        return path;
    }

    public Pt getLocation() {
        //XXX optimize
        java.awt.Rectangle r = toShape().getBounds();
        return new Pt (r.getLocation());
    }

    public void setLocation(double x, double y) {
        Pt old = getLocation();
        double offx = x - old.x;
        double offy = y - old.y;
        for (int i = 0; i < segments.length; i++) {
            Segment seg = segments[i];
            seg.translate(offx, offy);
        }
    }

    public void clearLocation() {
        Pt pt = getLocation();
        setLocation (0 - pt.x, 0 - pt.y);
    }

    public Vector copy(AffineTransform xform) {
        if (xform == null || AffineTransform.getTranslateInstance(0, 0).equals (xform)) {
            Segment[] nue = new Segment[segments.length];
            for (int i = 0; i < segments.length; i++) {
                Segment seg = segments[i];
                Segment s = new Segment (seg);
                nue[i] = s;
            }
            return new PathIteratorWrapper (nue, fill);
        } else {
            Shape s = toShape();
            return new PathIteratorWrapper (s.getPathIterator(xform));
        }
    }

    public void paint(Graphics2D g) {
        if (isFill()) {
            fill (g);
        } else {
            draw (g);
        }
    }

    public Primitive copy() {
        return copy (null);
    }

    public void fill(Graphics2D g) {
        g.fill (toShape());
    }

    public boolean isFill() {
        return fill;
    }

    public void getBounds(Double dest) {
        //XXX optimize
        dest.setRect(toShape().getBounds2D());
    }

    public int getControlPointCount() {
        int result = 0;
        for (int i = 0; i < segments.length; i++) {
            int ct = segments[i].getPointCount();
//            System.err.println(segments[i] + ": " + ct);
            result += ct;
        }
        return result;
    }

    public void getControlPoints(double[] xy) {
        int ix = 0;
        for (int i = 0; i < segments.length; i++) {
            ix += segments[i].copyData(xy, ix);
        }
    }

    public int[] getVirtualControlPointIndices() {
        List <Integer> ints = null;
        int ct = 0;
        for (int i = 0; i < segments.length; i++) {
            Segment seg = segments[i];
            if (seg.type == PathIterator.SEG_CUBICTO) {
                if (ints == null) {
                    ints = new ArrayList <Integer> (8);
                }
                ints.add (ct);
                ints.add (ct+1);
            }
            ct += seg.getPointCount();
        }
        if (ints == null) {
            return new int[0];
        } else {
            int[] result = new int [ints.size()];
            int ix = 0;
            for (Integer in : ints) {
                result[ix] = in.intValue();
                ix ++;
            }
            return result;
        }
    }

    public void setControlPointLocation(int pointIndex, Pt location) {
        int ct = 0;
        for (int i = 0; i < segments.length; i++) {
            Segment seg = segments[i];
            int pc = seg.getPointCount();
            if (pointIndex >= ct && pointIndex < pc + ct) {
                int offset = (pointIndex - ct) * 2;
                seg.data[offset] = location.x;
                seg.data[offset + 1] = location.y;
            }
            ct += pc;
        }
    }
    
    
    private int entryIndexFor (int pointIndex) {
        int ct = 0;
        for (int i = 0; i < segments.length; i++) {
            Segment seg = segments[i];
            int pc = seg.getPointCount();
            if (pointIndex >= ct && pointIndex < pc + ct) {
                return i;
            }
            ct += pc;
        }
        return -1;
    }

    public boolean delete(int pointIndex) {
        int ix = entryIndexFor (pointIndex);
        boolean result = ix >= 0;
        if (result) {
            Segment[] nue = new Segment[segments.length - 1];
            for (int i = 0; i < nue.length; i++) {
                if (i < ix) {
                    nue[i] = segments[i];
                } else if (i >= ix) {
                    nue[i] = segments[i + 1];
                }
            }
            segments = nue;
        }
        return result; 
    }

    public boolean insert(double x, double y, int index, int kind) {
        double[] data;
        switch (kind) {
            case PathIterator.SEG_MOVETO :
            case PathIterator.SEG_LINETO :
                data = new double[] { x, y };
                break;
            case PathIterator.SEG_QUADTO :
                data = new double[] { x - 10, y - 10, x, y };
                break;
            case PathIterator.SEG_CUBICTO :
                data = new double[] { x - 10, y, x, y + 10, x, y };
                break;
            case PathIterator.SEG_CLOSE :
                data = new double[0];
                break;
            default :
                throw new AssertionError (Integer.toString(kind));
        }
        int eix = entryIndexFor (index);
        Segment s = new Segment (data, (byte) kind);
        Segment[] nue = new Segment [segments.length + 1];
        for (int i = 0; i < nue.length; i++) {
            Segment seg = i < eix ? segments[i] : i > eix ? segments[i-1] : s;
            nue[i] = seg;
        }
        segments = nue;
        return true;
    }

    public int getPointIndexNearest(double x, double y) {
        double[] dist = new double[1];
        double bestDist = java.lang.Double.MAX_VALUE;
        int ct = 0;
        int result = -1;
        for (int i = 0; i < segments.length; i++) {
            Segment seg = segments[i];
            int ix = seg.ixNearest(x, y, dist);
            int realIndex = ct + ix;
            if (dist[0] < bestDist) {
                bestDist = dist[0];
                result = realIndex;
            }
            ct += seg.getPointCount();
        }
        return result;
    }
    
    public boolean equals (Object o) {
        boolean result = o instanceof PathIteratorWrapper;
        if (result) {
            result = Arrays.equals(segments, ((PathIteratorWrapper) o).segments);
        }
        return result;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            Segment seg = segments[i];
            sb.append (seg);
            if (i != segments.length - 1) {
                sb.append (", ");
            }
        }
        return sb.toString();
    }
}
