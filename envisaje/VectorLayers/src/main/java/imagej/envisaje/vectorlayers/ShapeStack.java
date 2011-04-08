/*
 * ShapeStack.java
 *
 * Created on October 25, 2006, 9:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.vectorlayers;

import imagej.envisaje.api.vector.Aggregate;
import imagej.envisaje.api.vector.Attribute;
import imagej.envisaje.api.vector.Fillable;
import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.Proxy;
import imagej.envisaje.api.vector.Vector;
import imagej.envisaje.api.vector.Volume;
import imagej.envisaje.api.vector.aggregate.PaintedPrimitive;
import imagej.envisaje.api.vector.aggregate.TransformedPrimitive;
import imagej.envisaje.api.vector.painting.VectorRepaintHandle;
import imagej.envisaje.api.vector.util.Pt;
import imagej.envisaje.spi.image.RepaintHandle;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holder for the stack of vector shapes making up an image
 *
 * @author Tim Boudreau
 */
public final class ShapeStack implements VectorRepaintHandle {
    List <Primitive> primitives = new ArrayList <Primitive> ();
    private List <Attribute> currentAttributes = new ArrayList <Attribute> ();
    private List <Primitive> unknown = new ArrayList <Primitive> ();

    private static Logger logger = Logger.getLogger(ShapeStack.class.getName());
    private static final boolean log = logger.isLoggable(Level.FINE);
    static {
        logger.setLevel(Level.ALL);
    }
    private final RepaintHandle owner;
    public ShapeStack(RepaintHandle owner) {
        this.owner = owner;
    }

    public ShapeStack (ShapeStack other) {
        this.owner = other.owner;
        for (Primitive entry : other.primitives) {
            primitives.add (entry);
        }
    }

    public ShapeStack (ShapeStack other, RepaintHandle handle) {
        this.owner = handle;
        for (Primitive entry : other.primitives) {
            primitives.add (entry);
        }
    }

    public void repaintArea(int x, int y, int w, int h) {
        owner.repaintArea (x, y, w, h);
    }

    public Dimension getSize() {
        Rectangle2D.Double r = new Rectangle2D.Double();
        Rectangle2D.Double scratch = new Rectangle2D.Double();
        for (Primitive primitive : primitives) {
            if (primitive instanceof Volume) {
                ((Volume) primitive).getBounds (scratch);
            }
            r.union(r, scratch, r);
        }
        return new Dimension ((int) (r.x + r.width), (int)
            (r.y + r.height));
    }

    public List <Primitive> getPrimitives() {
        return primitives; //XXX defensive copy if needed
    }
    Pt location = Pt.ORIGIN;
    private final Rectangle2D.Double scratch = new
            Rectangle2D.Double();

    PaintedPrimitive currentPrimitive = null;

    public void drawn(Primitive p) {
        if (log) logger.log(Level.FINE, "Drawn: " + p);
//        System.err.println("DRAWN: " + p);
        if (p instanceof Vector) {
            Pt loc = ((Vector)p).getLocation();
            double x = loc.x - location.x;
            double y = loc.y - location.y;
            ((Vector)p).setLocation(x, y);
        }

        if (p instanceof Attribute) {
            currentAttributes.add ((Attribute)p);
        } else {
            if (currentPrimitive == null) {
                currentPrimitive = PaintedPrimitive.create(p, currentAttributes);
            } else if (currentPrimitive.matchesDrawnObject(p)) {
                boolean fill = p instanceof Fillable ? ((Fillable) p).isFill() : false;
                currentPrimitive.add (currentAttributes, fill);
            } else { //if currentPrimitive != null && !currentPrimitive.matchesDrawnObject (p)
//                System.err.println("Go to a new primitive");
                primitives.add (currentPrimitive);
                currentPrimitive = PaintedPrimitive.create (p, currentAttributes);
            }
            currentAttributes.clear();
        }

        if (p instanceof Volume) {
            ((Volume) p).getBounds(scratch);
            java.awt.Rectangle r = scratch.getBounds();
            owner.repaintArea(r.x, r.y, r.width, r.height);
        }
    }

    void beginDraw() {
        if (currentPrimitive != null) {
            saveCurrentPrimitive();
        }
    }

    void endDraw() {
        if (currentPrimitive != null) {
            saveCurrentPrimitive();
        }
    }

    private void saveCurrentPrimitive() {
        Primitive p = currentPrimitive;
        if (p != null) {
            primitives.add (TransformedPrimitive.create(p));
        }
        currentPrimitive = null;
        currentAttributes.clear();
        if (p != null && p instanceof Volume) {
            ((Volume) p).getBounds(scratch);
            repaintArea ((int) Math.floor(scratch.x), (int) Math.floor(scratch.y),
                    (int) Math.ceil(scratch.width), (int) Math.ceil(scratch.height));
        }
    }

    void cancelDraw() {
        currentAttributes.clear();
    }

    boolean isEmpty() {
        return primitives.isEmpty();
    }
    
    public void toFront (Primitive primitive) {
        assert primitives.contains (primitive);
        primitives.remove (primitive);
        primitives.add (primitive);
    }
    
    public void toBack (Primitive primitive) {
        assert primitives.contains (primitive);
        primitives.remove (primitive);
        primitives.add (0, primitive);
    }
    
    public void add (Primitive primitive) {
        primitives.add (primitive);
    }
    
    public void replace (List <? extends Primitive> toRemove, Primitive replaceWith) {
        int ix = Integer.MIN_VALUE;
        for (Primitive p : toRemove) {
            ix = Math.max (primitives.indexOf(p), ix);
        }
        primitives.removeAll (toRemove);
//        System.err.println("Replace " + toRemove + " with " + replaceWith);
        int newIdx = Math.max (0, ix - toRemove.size());
        primitives.add (newIdx, replaceWith);
    }
    
    public void draw(Primitive shape) {
        throw new UnsupportedOperationException();
    }

    public boolean paint (Graphics2D g) {
        Primitive[] shapes = (Primitive[])
                primitives.toArray(new Primitive[primitives.size()]);
        for (int i=0; i < shapes.length; i++) {
//            System.err.println("PAINT " + shapes[i]);
            shapes[i].paint(g);
        }
        return shapes.length > 0;
    }

    public String toString() {
        return super.toString() + primitives;
    }
    
    public String dump() {
        StringBuilder sb = new StringBuilder("\n----------\n");
        for (Primitive p : primitives) {
            dumpPrimitive (p, 0, sb);
        }
        sb.append ("----------\n");
        return sb.toString();
    }
    
    private void dumpPrimitive (Primitive p, int depth, StringBuilder sb) {
        char[] c = new char [depth * 2];
        Arrays.fill (c, ' ');
        sb.append (new String (c) + " ITEM:");
        sb.append (p);
        sb.append ('\n');
        if (p instanceof Aggregate) {
            int max = ((Aggregate) p).getPrimitiveCount();
            for (int i = 0; i < max; i++) {
                Primitive pp = ((Aggregate) p).getPrimitive (i);
                dumpPrimitive (pp, depth + 1, sb);
            }
        } else if (p instanceof Proxy) {
            dumpPrimitive (((Proxy) p).getProxiedPrimitive(), depth + 1, sb);
        }
    }
}
