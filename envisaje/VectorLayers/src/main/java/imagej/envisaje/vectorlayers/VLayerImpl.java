/*
 * VLayerImpl.java
 *
 * Created on October 25, 2006, 3:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.vectorlayers;

import imagej.envisaje.api.editing.LayerFactory;
import imagej.envisaje.spi.image.LayerImplementation;
import imagej.envisaje.spi.image.RepaintHandle;
import imagej.envisaje.vectorlayers.tools.MoveShapeTool;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Vector layer implementation.
 *
 * @author Tim Boudreau
 */
class VLayerImpl extends LayerImplementation {
    private final VSurfaceImpl surface;
    private String name = "Fred"; //NOI18N
    public VLayerImpl(LayerFactory factory, RepaintHandle handle, String name, Dimension d) {
        super (factory);
        addRepaintHandle (handle);
        surface = new VSurfaceImpl (getMasterRepaintHandle(), d);
        this.name = name;
    }

    VLayerImpl (VLayerImpl other) {
        super (other.getLookup().lookup (LayerFactory.class));
        addRepaintHandle(other.getMasterRepaintHandle());
        this.name = other.name;
        this.visible = other.visible;
        this.opacity = other.opacity;
        this.surface = new VSurfaceImpl (other.surface);
    }

    public Object clone() {
        return new VLayerImpl (this);
    }

    public Rectangle getBounds() {
        return surface.getBounds();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!this.name.equals(name)) {
            String old = this.name;
            this.name = name;
            change.firePropertyChange(PROP_NAME, old, name);
        }
    }

    SwingPropertyChangeSupport change = new SwingPropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener l) {
        change.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        change.removePropertyChangeListener(l);
    }

    private boolean visible = true;
    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            change.firePropertyChange(PROP_VISIBLE, !visible, visible);
            repaint();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public float getOpacity() {
        return 1.0F;
    }

    private float opacity = 1.0F;
    public void setOpacity(float f) {
        if (opacity != f) {
            float old = opacity;
            opacity = f;
            change.firePropertyChange(PROP_OPACITY, old, f);
            repaint();
        }
    }

    private void repaint() {
        Rectangle bounds = getBounds();
        getMasterRepaintHandle().repaintArea(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void commitLastPropertyChangeToUndoHistory() {
    }

    public boolean paint(Graphics2D g, Rectangle bounds, boolean showSelection) {
        if (bounds == null && !visible) {
            return false;
        }
        Composite old = g.getComposite();
        if (opacity != 1.0F) {
            g.setComposite (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        }
        
        boolean result = surface.paint(g, bounds);
        if (old != null) {
            g.setComposite(old);
        }
//        System.out.println(surface.stack.dump());
        return result;
    }

    public LayerImplementation clone(boolean isUserCopy, boolean deepCopy) {
        VLayerImpl result = new VLayerImpl(getLookup().lookup(LayerFactory.class),
                getMasterRepaintHandle(),
                getName(), getBounds().getSize());
        return result;
    }

    @Override
    protected Lookup createLookup() {
        return Lookups.fixed (layer, surface, 
                surface.getSurface(), 
                surface.stack, new MoveShapeTool());
    }

    @Override
    public void resize(int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
