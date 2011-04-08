/*
 * Surface.java
 */

package imagej.envisaje.api.image;

import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import imagej.envisaje.Accessor;
import imagej.envisaje.spi.image.SurfaceImplementation;
import imagej.envisaje.spi.tools.Tool;

/**
 * A writable drawing surface belonging to a Layer.
 *
 * @author Timothy Boudreau
 */
public final class Surface {
    final SurfaceImplementation impl;

    static {
        Layer.init();
    }

    Surface (SurfaceImplementation impl) {
        if (Accessor.surfaceFor(impl) != null) {
            throw new IllegalStateException ("Constructing a second " + //NOI18N
                    "SurfaceImplementation for " + impl); //NOI18N
        }
        this.impl = impl;
    }

    /** Get a Graphics object that can be drawn into */
    public Graphics2D getGraphics() {
        return impl.getGraphics();
    }

    /** Apply an effect, in the form of a Composite, to the image */
    public void applyComposite (Composite composite, Shape clip) {
        impl.applyComposite(composite, clip);
    }

    public void setLocation (Point p) {
        impl.setLocation(p);
    }

    public Point getLocation() {
        return impl.getLocation();
    }

    public void beginUndoableOperation(String name) {
        impl.beginUndoableOperation(name);
    }

    public void endUndoableOperation() {
        impl.endUndoableOperation();
    }

    public void cancelUndoableOperation() {
        impl.cancelUndoableOperation();
    }

    public void setCursor (Cursor cursor) {
        impl.setCursor (cursor);
    }

    public void setTool (Tool tool) {
        impl.setTool (tool);
    }
    
    /**
     * Get a BufferedImage of the contents, for tools such as smudge tools
     * which harvest pixel data.
     * <p/>
     * Generally you never want to draw directly into the image, as it may
     * only be a copy.
     * <p/>
     * This method may return null, in which case a tool might offer to convert
     * the layer to a raster layer which can supply an image.  The default
     * implementation returns null.
     * 
     * @return An image
     */
    public BufferedImage getImage() {
        return impl.getImage();
    }

}