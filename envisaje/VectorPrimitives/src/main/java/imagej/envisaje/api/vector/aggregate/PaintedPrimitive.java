/*
 * PaintedPrimitive.java
 *
 * Created on October 31, 2006, 8:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.aggregate;

import imagej.envisaje.api.vector.Adjustable;
import imagej.envisaje.api.vector.Aggregate;
import imagej.envisaje.api.vector.Attribute;
import imagej.envisaje.api.vector.Fillable;
import imagej.envisaje.api.vector.Mutable;
import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Proxy;
import imagej.envisaje.api.vector.Strokable;
import imagej.envisaje.api.vector.Vector;
import imagej.envisaje.api.vector.Volume;
import imagej.envisaje.api.vector.graphics.BasicStrokeWrapper;
import imagej.envisaje.api.vector.graphics.FontWrapper;
import imagej.envisaje.api.vector.util.Pt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper for a primitive that can be painted multiple times with different
 * painting attributes.  I.e. if you stroke a rectangle with blue and fill it
 * with red, then you have a PaintedPrimitive for the rectangle with two
 * AttributeSets (which represent 0 or more settings that may be performed on
 * a Graphics2D, such as setting the color).
 * <p>
 * In other words, this represents multiple series of drawing instructions, each
 * of which culminates in stroking or filling the same geometric primitive.
 * <p>
 *
 * @author Tim Boudreau
 */
public abstract class PaintedPrimitive implements Primitive, Proxy, Aggregate {
    protected Primitive p;

    //Note - this class intentionally implements nothing but Primitive, though
    //it implements the methods of all likely interfaces;  inner
    //subclasses add the necessary interfaces to the class signature;  the
    //static create() method selects the correct one for the primitive it has
    //been passed

    List <AttributeSet> attributeSets = new LinkedList <AttributeSet> ();
    PaintedPrimitive (List <Attribute> attributes, Primitive p) {
        this.p = p;
        assert p instanceof Fillable || p instanceof Strokable || p instanceof Vector;
        boolean fill = p instanceof Fillable ? ((Fillable) p).isFill() : false;
        add (attributes, fill);
        assertClass();
    }

    PaintedPrimitive (Primitive p, List <AttributeSet> attributeSets) {
        this.p = p;
        this.attributeSets.addAll (attributeSets);
        assert p instanceof Fillable || p instanceof Strokable || p instanceof Vector;
        assertClass();
    }

    private void assertClass() {
        Class c = getClass();
        assert c == SAV.class || c == SAVF.class || c == SAVFV.class ||
                c == SAVFVM.class || c == SAVVM.class || c == Ve.class ||
                c == Vo.class : "Not to be implemented outside of package"; //NOI18N
    }

    public Shape toShape() {
        Shape result = null;
        if (p instanceof Vector) {
            result = ((Vector) p).toShape();
        } else if (p instanceof Volume) {
            Rectangle2D.Double r = new Rectangle2D.Double();
            ((Volume) p).getBounds(r);
            result = r;
        } else {
            assert false : "Not a Vector or a Volume.  Don't call toShape() on me."; //NOI18N
        }
        return result;
    }

    public Pt getLocation() {
        return p instanceof Vector ? ((Vector) p).getLocation() :
            Pt.ORIGIN;
    }

    public void setLocation(double x, double y) {
        if (p instanceof Vector) {
            ((Vector) p).setLocation(x, y);
        }
    }

    public void clearLocation() {
        if (p instanceof Vector) {
            ((Vector) p).clearLocation();
        }
    }

    public void paint(Graphics2D g) {
        for (AttributeSet set : attributeSets) {
            set.paint(g);
        }
    }

    public void fill(Graphics2D g) {
        throw new UnsupportedOperationException();
    }

    public void draw(Graphics2D g) {
        throw new UnsupportedOperationException();
    }

    public boolean isFill() {
        return p instanceof Fillable ? ((Fillable) p).isFill() : false;
    }

    public void getBounds(Rectangle2D.Double r) {
        if (p instanceof Volume) {
            ((Volume) p).getBounds(r);
        }
        //Now compensate for stroke size so we really get the size right
        double strokeWidth = 0D;
        for (AttributeSet a : attributeSets) {
            int max = a.size();
            for (int i=0; i < max; i++) {
                Attribute at = a.get(i);
                if (at instanceof BasicStrokeWrapper) {
                    strokeWidth = Math.max (strokeWidth, 
                            ((BasicStrokeWrapper) at).lineWidth);
                }
            }
        }
        if (strokeWidth != 0D) {
            double exp = strokeWidth / 2;
            r.x -= exp;
            r.y -= exp;
            r.width += strokeWidth;
            r.height += strokeWidth;
        }
    }

    public int getControlPointCount() {
        return p instanceof Adjustable ?
            ((Adjustable) p).getControlPointCount() : 0;
    }

    public void getControlPoints(double[] xy) {
        ((Adjustable) p).getControlPoints(xy);
    }

    public int[] getVirtualControlPointIndices() {
        return ((Adjustable) p).getVirtualControlPointIndices();
    }

    public boolean delete(int pointIndex) {
        if (!(p instanceof Mutable)) {
            return false;
        }
        return ((Mutable) p).delete (pointIndex);
    }

    public boolean insert(double x, double y, int index, int kind) {
        if (!(p instanceof Mutable)) {
            return false;
        }
        return ((Mutable) p).insert (x, y, index, kind);
    }

    public int getPointIndexNearest(double x, double y) {
        if (!(p instanceof Mutable)) {
            return -1;
        }
        return ((Mutable) p).getPointIndexNearest(x, y);
    }

    public Vector copy(AffineTransform transform) {
        Vector v = ((Vector) p).copy (transform);
        List <AttributeSet> attrs = copyAttrs();
        if (this instanceof Ve) {
            return new Ve (v, attrs);
        } else if (this instanceof SAV) {
            return new SAV (v, attrs);
        } else if (this instanceof SAVF) {
            return new SAVF (v, attrs);
        } else if (this instanceof SAVFV) {
            return new SAVFV (v, attrs);
        } else if (this instanceof SAVFVM) {
            return new SAVFVM (v, attrs);
        } else if (this instanceof SAVVM) {
            return new SAVVM (v, attrs);
        } else {
            throw new IllegalArgumentException ("What is this? " + this); //NOI18N
        }
    }

    private List <AttributeSet> copyAttrs() {
        List <AttributeSet> result = new ArrayList <AttributeSet> (attributeSets.size());
        for (AttributeSet set : attributeSets) {
            result.add (set.clone());
        }
        return result;
    }

    public Primitive getDrawnObject() {
        return p;
    }

    public boolean matchesDrawnObject (Primitive p) {
        return p.equals(this.p);
    }

    public void add (List <Attribute> attributes, boolean fill) {
        attributeSets.add (new AttributeSet (p, fill, attributes));
    }

    public int getPrimitiveCount() {
        int result = 0;
        for (AttributeSet a : attributeSets) {
            result += a.size();
        }
        return result + 1; //one for the only visual primitive we represent
    }

    public Primitive getPrimitive (int i) {
        int ct = 0;
        for (AttributeSet a : attributeSets) {
            int sz = a.size();
            if (i >= ct && i < ct + sz) {
                return a.get(i - ct);
            }
            ct += sz;
        }
        if (i == ct) {
            return p;
        }
        throw new IndexOutOfBoundsException ("Tried to fetch " + i + " out of " //NOI18N
                + getPrimitiveCount());
    }

    public int getVisualPrimitiveCount() {
        return 1;
    }

    public Primitive getVisualPrimitive (int i) {
        if (i == 0) {
            return getProxiedPrimitive();
        } else {
            throw new IndexOutOfBoundsException ("Tried to fetch primitive " + i //NOI18N
                    + ".  There is only one." ); //NOI18N
        }
    }

    public Primitive getProxiedPrimitive() {
        return p;
    }

    public void setControlPointLocation(int pointIndex, Pt location) {
        ((Adjustable) p).setControlPointLocation(pointIndex, location);
    }

    private static final class AttributeSet {
        private final List <Attribute> attributes = new LinkedList <Attribute> ();
        private final boolean fill;
        private final Primitive shape;
        public AttributeSet (Primitive shape, boolean fill, List <Attribute> attributes) {
            this.attributes.addAll (attributes);
            this.fill = fill;
            this.shape = shape;
        }
        
        public List <Attribute> getAttributes() {
            return Collections.unmodifiableList (attributes);
        }

        public int size() {
            return attributes.size();
        }

        public Attribute get (int i) {
            return attributes.get (i);
        }

        public AttributeSet clone() {
            List <Attribute> nue = new ArrayList <Attribute> (attributes.size());
            for (Attribute a : attributes) {
                nue.add ((Attribute) a.copy());
            }
            return new AttributeSet (shape.copy(), fill, nue);
        }

        public void paint(Graphics2D g) {
            for (Attribute a : attributes) {
                a.paint(g);
            }
            if (fill && shape instanceof Fillable) {
                ((Fillable) shape).fill(g);
            } else if (!fill && shape instanceof Strokable) {
                ((Strokable) shape).draw(g);
            } else {
                shape.paint(g);
            }
        }

        public String toString() {
            return "AttributeSet for one paint of " + shape + " with " + attributes; //NOI18N
        }
    }

    private FontWrapper font;
    public void setFont(FontWrapper f) {
        this.font = font;
    }

    public String toString() {
        return "PaintedPrimitive " + p + " : " + attributeSets; //NOI18N
    }
    
    public List <Attribute> allAttributes() {
        List <Attribute> result = new ArrayList <Attribute> ();
        for (AttributeSet set : attributeSets) {
            result.addAll (set.getAttributes());
        }
        return result;
    }

    //Below are logic-free subclasses of PaintedPrimitive which
    //implement the various combinations of interfaces needed.  All
    //methods of all interfaces are implemented by the parent class,
    //so the subclass simply marks which interfaces it provides so
    //that it will be equivalent to the object it wraps.
    private static final class Vo extends PaintedPrimitive implements Volume {
        public Vo (Primitive p, List <AttributeSet> attrs) {
            super (p, attrs);
            assert p instanceof Volume;
        }

        public Vo (List <Attribute> attrs, Primitive p) {
            super (attrs, p);
            assert p instanceof Volume;
        }

        public Primitive copy() {
            return new Vo (p, super.copyAttrs());
        }
    }

    private static final class Ve extends PaintedPrimitive implements Vector {
        public Ve (Primitive p, List <AttributeSet> attrs) {
            super (p, attrs);
            assert p instanceof Vector;
        }

        public Ve (List <Attribute> attrs, Primitive p) {
            super (attrs, p);
            assert p instanceof Vector;
        }

        public Primitive copy() {
            return new Ve (p, super.copyAttrs());
        }
    }

    private static final class SAV extends PaintedPrimitive implements Strokable, Adjustable, Vector {
        public SAV (Primitive p, List <AttributeSet> attrs) {
            super (p, attrs);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Vector;
        }

        public SAV (List <Attribute> attrs, Primitive p) {
            super (attrs, p);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Vector;
        }

        public Primitive copy() {
            return new SAV (p, super.copyAttrs());
        }
    }

    private static final class SAVVM extends PaintedPrimitive implements Strokable, Adjustable, Vector, Volume, Mutable {
        public SAVVM (Primitive p, List <AttributeSet> attrs) {
            super (p, attrs);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Vector;
            assert p instanceof Volume;
            assert p instanceof Mutable;
        }

        public SAVVM (List <Attribute> attrs, Primitive p) {
            super (attrs, p);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Vector;
            assert p instanceof Volume;
            assert p instanceof Mutable;
        }

        public Primitive copy() {
            return new SAVVM (p, super.copyAttrs());
        }
    }

    private static final class SAVF extends PaintedPrimitive implements Strokable, Adjustable, Volume, Fillable {
        public SAVF (Primitive p, List <AttributeSet> attrs) {
            super (p, attrs);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Volume;
            assert p instanceof Fillable;
        }

        public SAVF (List <Attribute> attrs, Primitive p) {
            super (attrs, p);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Volume;
            assert p instanceof Fillable;
        }

        public Primitive copy() {
            return new SAVF (p, super.copyAttrs());
        }
    }

    private static final class SAVFV extends PaintedPrimitive implements Strokable, Adjustable, Volume, Fillable, Vector {
        public SAVFV (Primitive p, List <AttributeSet> attrs) {
            super (p, attrs);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Volume;
            assert p instanceof Fillable;
            assert p instanceof Vector;
        }

        public SAVFV (List <Attribute> attrs, Primitive p) {
            super (attrs, p);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Volume;
            assert p instanceof Fillable;
            assert p instanceof Vector;
        }

        public Primitive copy() {
            return new SAVFV (p, super.copyAttrs());
        }
    }

    private static final class SAVFVM extends PaintedPrimitive implements Strokable, Adjustable, Volume, Fillable, Vector, Mutable {
        public SAVFVM (Primitive p, List <AttributeSet> attrs) {
            super (p, attrs);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Volume;
            assert p instanceof Fillable;
            assert p instanceof Vector;
            assert p instanceof Mutable;
        }

        public SAVFVM (List <Attribute> attrs, Primitive p) {
            super (attrs, p);
            assert p instanceof Strokable;
            assert p instanceof Adjustable;
            assert p instanceof Volume;
            assert p instanceof Fillable;
            assert p instanceof Vector;
            assert p instanceof Mutable;
        }

        public Primitive copy() {
            return new SAVFVM (p, super.copyAttrs());
        }
    }

    public static PaintedPrimitive create(Primitive p, List <Attribute> attributes) {
        if (p instanceof Strokable && p instanceof Fillable && p instanceof Volume && p instanceof Adjustable && p instanceof Vector && p instanceof Mutable) {
            return new SAVFVM (attributes, p);
        } else if (p instanceof Strokable && p instanceof Fillable && p instanceof Volume && p instanceof Adjustable && p instanceof Vector) {
            return new SAVFV (attributes, p);
        } else if (p instanceof Strokable && p instanceof Adjustable && p instanceof Volume && p instanceof Fillable) {
            return new SAVF (attributes, p);
        } else if (p instanceof Strokable && p instanceof Adjustable && p instanceof Volume && p instanceof Vector) {
            return new SAVVM (attributes, p);
        } else if (p instanceof Strokable && p instanceof Adjustable && p instanceof Vector) {
            return new SAV (attributes, p);
        } else if (p instanceof Volume) {
            return new Vo(attributes, p);
        } else if (p instanceof Vector) {
            return new Ve(attributes, p);
        } else {
            throw new IllegalArgumentException ("Unknown type combination:" + p); //NOI18N
        }
    }
}
