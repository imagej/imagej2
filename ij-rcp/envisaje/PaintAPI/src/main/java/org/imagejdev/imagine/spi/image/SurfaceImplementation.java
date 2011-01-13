/*
 * Surface.java
 *
 * Created on October 14, 2005, 11:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.spi.image;

import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import org.imagejdev.imagine.Accessor;
import org.imagejdev.imagine.api.image.Surface;
import org.imagejdev.imagine.spi.tools.Tool;

/**
 * A writable drawing surface belonging to a Layer.  Layers contain operations
 * to draw themselves onscreen;  all writing into the image should be handled
 * through the layer's Surface.
 *
 * @author Timothy Boudreau
 */
public abstract class SurfaceImplementation {
    protected final Surface surface;

    protected SurfaceImplementation() {
        this.surface = Accessor.DEFAULT.createSurface(this);
    }

    static {
        LayerImplementation.init();
    }

    /** Get a Graphics object that can be drawn into to modify the 
     * picture.  Implementations should automatically update the on-screen
     * image as this Graphics is drawn into, using the layer's RepaintHandle.
     * @return A Graphics2D object
     */
    public abstract Graphics2D getGraphics();
    
    /** Apply an effect, in the form of a Composite, to the image.
     */
    public final void applyComposite (Composite composite) {
        applyComposite (composite, null);
    }

    /**
     * For move operations - set the position of this surface within
     * the overall composite image.
     * //XXX shouldn't this belong to Layer?
     * @param p
     */
    public abstract void setLocation (Point p);
    public abstract Point getLocation();

    /**
     * Begin an undoable operation.  The SurfaceImplementation should 
     * save some sort of checkpoint data at this point, so that the image
     * can be rolled back to its prior state in the event of an undo
     * operation.
     * @param name
     */
    public abstract void beginUndoableOperation (String name);
    /**
     * End an atomic undoable operation.  Throws an exception if no 
     * operation in progress.
     */
    public abstract void endUndoableOperation();
    /**
     * Cancel an undoable operation and roll back to the previous state.
     */
    public abstract void cancelUndoableOperation();
    /**
     * Set the cursor the user should see.
     * @param cursor The cursor
     */
    public abstract void setCursor (Cursor cursor);
    /**
     * Set the tool that is currently operating on the surface.  At any
     * time, only one tool is active against a surface.  If this is called
     * with a different tool than the current one, the current tool should be
     * detached and the new one should take its place.
     * 
     * @param tool The tool or null
     */
    public abstract void setTool (Tool tool);

    protected static SurfaceImplementation implFor (Surface surface) {
        return Accessor.DEFAULT.getSurface(surface);
    }

    public abstract void applyComposite(Composite composite, Shape clip);

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
     * @param g A graphics context
     * @param r A bounding rectangle if painting a thumbnail image, or null
     *  if full quality painting is desired
     */
    public abstract boolean paint (Graphics2D g, Rectangle r);
    
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
        return null;
    }
    
    public final Surface getSurface() {
        return surface;
    }
}
