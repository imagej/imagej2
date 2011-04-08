/*
 * VSurfaceImpl.java
 *
 * Created on October 25, 2006, 9:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.vectorlayers;

import imagej.envisaje.api.vector.painting.VectorWrapperGraphics;
import imagej.envisaje.api.vector.util.Pt;
import imagej.envisaje.spi.image.RepaintHandle;
import imagej.envisaje.spi.image.SurfaceImplementation;
import imagej.envisaje.spi.tools.Tool;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Tim Boudreau
 */
class VSurfaceImpl extends SurfaceImplementation implements RepaintHandle {
    private Point location = new Point();
    ShapeStack stack;
    private final VectorWrapperGraphics graphics;
    private final RepaintHandle owner;
    private static Logger logger = Logger.getLogger(VSurfaceImpl.class.getName());
//    static {
//        logger.setLevel(Level.ALL);
//    }
    private static final boolean log = logger.isLoggable(Level.FINE);

    private final Dimension initialSize;
    public VSurfaceImpl(RepaintHandle owner, Dimension d) {
        stack = new ShapeStack(this);
        initialSize = d;
        graphics = new VectorWrapperGraphics (stack,
                new DummyGraphics(),
//                getFakeGraphics(),
                new Point (), 1, 1);
        this.owner = owner;
    }

    public VSurfaceImpl (VSurfaceImpl other) {
        this (other.owner, other.getBounds().getSize());
        stack = new ShapeStack (other.stack);
    }

    public Graphics2D getGraphics() {
        return graphics;
    }

    private void locationChanged (Point old, Point nue) {
        Dimension d = stack.getSize();
        Rectangle oldBds = new Rectangle (old.x, old.y, d.width, d.height);
        Rectangle newBds = new Rectangle (nue.x, nue.y, d.width, d.height);
        if (cachedBounds != null) {
            cachedBounds.setBounds (newBds);
        } else {
            cachedBounds = newBds;
        }
        Rectangle union = oldBds.union(newBds);
        repaintArea (union.x, union.y, union.width, union.height);
    }

    private Rectangle cachedBounds = new Rectangle();
    Rectangle getBounds() {
        if (cachedBounds == null) {
            Dimension d = stack.getSize();
            cachedBounds = new Rectangle(location.x, location.y, d.width, d.height);
            cachedBounds.width = Math.max (cachedBounds.width, initialSize.width);
            cachedBounds.height = Math.max (cachedBounds.height, initialSize.height);
        }
        return cachedBounds;
    }

    public void setLocation(Point p) {
//        System.err.println("xx SET LOCATION " + p + " old is " + location);
        if (!location.equals (p)) {
            Point old = new Point(location);
//            System.err.println("Set location " + p);
            location.setLocation (p);
            locationChanged(old, p);
            cachedBounds = null;
            stack.location = new Pt (p.x, p.y);
//            System.err.println("SET LOC TO " + stack.location);
        }
        Rectangle r = getBounds();
        repaintArea (r.x, r.y, r.width, r.height);
    }

    public Point getLocation() {
        return new Point (location);
    }

    public void beginUndoableOperation(String name) {
        stack.beginDraw();
    }

    public void endUndoableOperation() {
        stack.endDraw();
    }

    public void cancelUndoableOperation() {
        stack.cancelDraw(); //XXX
    }

    public void setCursor(Cursor cursor) {
        owner.setCursor (cursor);
    }

    private Tool tool;
    public void setTool(Tool tool) {
        if (tool != this.tool) {
            this.tool = tool;
            if (tool == null) {
                createCache();
            } else {
                dumpCache();
            }
        }
    }

    private void dumpCache() {
        cache = null;
    }

    private BufferedImage cache;
    private void createCache() {
        Rectangle r = getBounds();
        if (r.width == 0 || r.height == 0) {
            return;
        }
        cache = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().
                createCompatibleImage(r.width - location.x, r.height - location.x, Transparency.TRANSLUCENT);
    }

    private List <AppliedComposite> composites = Collections.synchronizedList (
            new ArrayList <AppliedComposite> ());
    public void applyComposite(Composite composite, Shape clip) {
        composites.clear(); //XXX handle stacking/merging these
        composites.add (new AppliedComposite (composite, clip));
    }

    private static final class AppliedComposite {
        public final Composite composite;
        public final Shape clip;
        public AppliedComposite (Composite composite, Shape clip) {
            this.composite = composite;
            this.clip = clip;
        }

        public boolean hasClip() {
            return clip != null;
        }

        public void apply (Graphics2D g) {
            g.setComposite (composite);
        }
    }

    static boolean NO_CACHE = true;//Boolean.getBoolean("vector.no.cache");
    public boolean paint(Graphics2D g, Rectangle r) {
//        System.err.println("VSurfaceImpl.paint " + r + " loc " + location + " with a " + g);
        if (g instanceof VectorWrapperGraphics) {
            throw new IllegalStateException ("Wrong graphics object");
        }
        if (stack.isEmpty()) {
            return false;
        }

        if (r == null) {
            if (!NO_CACHE) {
                if (cache == null && tool == null) {
                    createCache();
                }
                if (cache != null) {
                    g.drawRenderedImage(cache,
                            AffineTransform.getTranslateInstance(location.x, location.y));
                    return true;
                }
            }
            if (log) logger.log(Level.FINER, "Start paint of stack");
            g.translate(location.x, location.y);
            Composite old = g.getComposite();
            if (!composites.isEmpty()) {
                g.setComposite(composites.iterator().next().composite);
            }
            boolean result = stack.paint(g);
            if (old != null) {
                g.setComposite(old);
            }
            g.translate(-location.x, -location.y);
            return result;
        } else {
            Rectangle bds = getBounds();
            double rw = r.width;
            double rh = r.height;
            double bw = bds.width;
            double bh = bds.height;
            Graphics2D g2 = (Graphics2D) g.create();
            AffineTransform xform = AffineTransform.getScaleInstance(rw / bw,
                    rh / bh);
            
            AffineTransform curr = g2.getTransform();
            curr.concatenate(xform);
            g2.setTransform(curr);
            stack.paint (g2);
            g2.dispose();
            return true;
        }
    }

    public void repaintArea(int x, int y, int w, int h) {
        owner.repaintArea (x, y, w, h);
        if (cachedBounds != null && (x + w > cachedBounds. x + cachedBounds.width ||
                y + h > cachedBounds.height)) {
            cachedBounds = null;
            cache = null;
        }
    }
}
