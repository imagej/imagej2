/*
 * Layer.java
 *
 * Created on October 14, 2005, 11:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.spi.image;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;

import imagej.envisaje.Accessor;
import imagej.envisaje.Accessor.InverseAccessor;
import imagej.envisaje.api.editing.LayerFactory;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.image.Picture;
import imagej.envisaje.api.image.Surface;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * A single "layer" - one element in a composite stack of images
 * (represented by a Picture object).  This class is to be implemented by
 * those wishing to create an image editor.  Client that wishes to consume
 * objects such as this (for example, the Layers view) should use
 * Layer, not LayerImplementation.  No client code other than an editor
 * implementation should ever refer to this class.
 *
 * @author Timothy Boudreau
 */
public abstract class LayerImplementation {
    protected static final String PROP_BOUNDS = Layer.PROP_BOUNDS;
    protected static final String PROP_NAME = Layer.PROP_NAME;
    protected static final String PROP_VISIBLE = Layer.PROP_VISIBLE;
    protected static final String PROP_OPACITY = Layer.PROP_OPACITY;
    private final RepaintHandleProxySupport proxy =
            new RepaintHandleProxySupport(toString());

    static {
        init();
    }

    static void init() {
        Class c = Layer.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Accessor.INVERSE == null) {
            Accessor.INVERSE = new IA();
        }
    }

    protected final Layer layer;
    private final LayerFactory factory;
    protected LayerImplementation(LayerFactory factory) {
        layer = Accessor.DEFAULT.createLayer(this);
        this.factory = factory;
    }

    /** Get the bounds of this layer.  This may or may not
     * correspond to the bounds of the image it is part of */
    public abstract Rectangle getBounds();
    /** Get the user supplied name of this layer */
    public abstract String getName();

    public abstract void resize(int width, int height);
    /** Set the display name of this layer */
    public abstract void setName(String name);

    public abstract void addPropertyChangeListener (PropertyChangeListener l);
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
    /** Mark this layer as being included in those layers which are
     * visibly composited into the overall image */
    public abstract void setVisible (boolean visible);
    /** Returns true if setVisible(false) has not been called */
    public abstract boolean isVisible();
    /** Get this layer's opacity, affecting how it is composited */
    public abstract float getOpacity();
    /** Set the opacity of this layer */
    public abstract void setOpacity (float f);

    public abstract void commitLastPropertyChangeToUndoHistory();

    /**
     * Create a Lookup with custom contents.  This method will be called 
     * exactly once.
     */ 
    protected Lookup createLookup() {
        return null;
    }
    
    private Lookup lookup = null;
    /**
     * A lookup which supplies optional functionality for this layer.  For
     * example, if it can be hibernated (switched to a low memory usage
     * dormant state) it may supply an instance of Hibernator in its
     * lookup.  Custom contents are provided by the lookup returned by
     * <code>createLookup()</code>.  The lookup returned by this object
     */
    public final Lookup getLookup() {
        if (this.lookup == null) {
            Lookup lkp = createLookup();
            Lookup fixedContents;
            if (factory == null) {
                //unit tests
                fixedContents = Lookups.fixed (layer, this);
            } else {
                fixedContents = Lookups.fixed (layer, this, factory);
            }
            if (lkp == null) {
                this.lookup = fixedContents;
            } else {
                this.lookup = new ProxyLookup (fixedContents, lkp);
            }
        }
        return lookup;
    }

    /**
     * Paint the current contents of this Surface object to the supplied
     * Graphics2D context.
     * <p>
     * If a bounding rectangle is supplied, this method should assume that the
     * call is to paint a thumbnail, and that low quality rendering settings
     * should be used.  If the rectangle is null, then the image should be
     * rendered at full quality and full size (size will actually be determined
     * by the AffineTransform the Graphics is currently using, which will not
     * be modified if the rectangle is null).
     * <p>
     * If a bounding rectangle is supplied, coordinate information about the
     * location of this layer will be ignored.
     * <p>
     * @param g A graphics context
     * @param r A bounding rectangle if painting a thumbnail image, or null
     *  if full quality painting is desired
     */
    public abstract boolean paint (Graphics2D g, Rectangle bounds, boolean showSelection);

    /** Get a surface for drawing into.  Looks for an instance of
     * SurfaceImplementation in the Lookup returned by getLookup().
     */
    public final SurfaceImplementation getSurface() {
        SurfaceImplementation result = getLookup().lookup(SurfaceImplementation.class);
        if (result == null) {
            Surface surface = getLookup().lookup(Surface.class);
            if (surface != null) {
                result = Accessor.DEFAULT.getSurface(surface);
            }
        }
        return result;
    }

    public final Layer getLayer() {
        return layer;
    }

    public final void addRepaintHandle (RepaintHandle handle) {
        proxy.add (handle);
    }

    public final void removeRepaintHandle (RepaintHandle handle) {
        proxy.remove (handle);
    }

    protected final RepaintHandle getMasterRepaintHandle() {
        return proxy;
    }

    protected static LayerImplementation implFor (Layer layer) {
        return Accessor.DEFAULT.getImpl(layer);
    }

    /**
     * Create a new LayerImplementation identical to this one.
     *
     * @param isUserCopy Whether or not the result should have a name indicating
     *  it is a copy
     * @param deepCopy True if the resulting LayerImplementation should not
     *  share data with this one (editing the new layer will not alter this one
     *  and vice-versa)
     * @return A new LayerImplementation identical to the old one
     */
    public abstract LayerImplementation clone (boolean isUserCopy, boolean deepCopy);

    private static final class IA extends InverseAccessor {
        //Trampoline to allow methods of the API classes to find
        //the implementation instance for another API class
        public Layer layerFor(LayerImplementation impl) {
            return impl == null ? null : impl.layer;
        }

        public Picture pictureFor(PictureImplementation impl) {
            return impl == null ? null : impl.picture;
        }

        public Surface surfaceFor(SurfaceImplementation impl) {
            return impl == null ? null : impl.surface;
        }
    }
}
