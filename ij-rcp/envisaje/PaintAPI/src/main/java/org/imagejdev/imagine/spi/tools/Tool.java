package org.imagejdev.imagine.spi.tools;

import javax.swing.Icon;
import org.imagejdev.imagine.api.image.Layer;
import org.openide.util.Lookup;

/**
 * Interface for a tool which can modify an image.  Tools may provide
 * MouseListener, MouseMotionListener, MouseWheelListener and/or KeyListener
 * and receive events from the editing component, with the coordinates
 * translated and scaled if necessary into the component's layer's
 * coordinate space.
 * <p>
 * There are two typical models for implementing tools:  Non-modal tools
 * directly paint into the image being drawn.  To do this, simply track, 
 * e.g. mouse events, call layer.getSurface().getGraphics() and draw what
 * you want into it.
 * <p>
 * <i>Modal</i> tools generally have a two-phase operation cycle - the user
 * draws something on the screen and then indicates drawing is completed by
 * double-clicking or pressing enter, or aborts by pressing Escape.  An 
 * example of this is a tool that lets you draw polylines by clicking successive
 * points of the polyline, showing the drawn polyline on screen, but not 
 * actually writing it to the underlying image until the user performs some
 * gesture such as pressing enter, to indicate that the edit should be
 * <i>committed</i>.  Modal tools should provide an instance of 
 * PaintParticipant from their Lookup.  This will provide the necessary 
 * hooks to repaint and draw on the editing canvas without writing into the
 * underlying image.
 * <p>
 * Tools do not generally need to be directly concerned with either the
 * relative coordinate space of the layer they are editing, or the bounds
 * of the selection in that layer.  This will be handled transparently by
 * the infrastructure providing the layer;  to paint, simply get the
 * Surface of the Layer object passed to activate() and use the Graphics
 * instance returned by its getGraphics method to paint.
 * <p>
 * Scaling for painting into zoomed images is similarly handled - a tool
 * will always receive coordinates in the coordinate space of the
 * image it is editing.
 *
 * @author Timothy Boudreau
 */
public interface Tool {
    /** Get an icon to represent the tool */
    public Icon getIcon();
    /** Get a human readable, localized name for the tool */
    public String getName();
    /** Called when a tool is activated to modify a layer */
    public void activate (Layer layer);
    /** Called when a tool is deactivated and no longer should affect anything
     * (it has been removed as a listener on the current editor component).
     */
    public void deactivate ();

    /** For looking up of optional tool attributes that apply to some but not
     * all tools.  Rather than is-a relationships with regards to the
     * capabilities of a tool, the tool is queried whether it <i>has</i> an
     * instance of a particular known interface.  This also makes it possible
     * to have private communication between a specific Layer implementation
     * and a specific Tool implementation without the infrastructure needing
     * to know about the interfaces in question.
     * <p>
     * The following API classes can be found in the Lookups of
     * some (but not all) tools:
     * <ul>
     * <li>PaintParticipant - tools that paint in-progress content before
     *  the content has been written out to the image will need to provide an
     * instance of PaintParticipant, which will be queried to paint updated
     * content.</li>
     * <li>ToolState - Persistable state - if a tool is stateful and the user
     * switches from editing one to another image, the current state of the
     * tool will be stored using a ToolState object which an write out the
     * state and read it back in.</li>
     * <li>CustomizerProvider - If the tool has a GUI customizer, this is how
     * to provide it.</li>
     * <li>MouseListener, KeyListener, MouseMotionListener, MouseWheelListener - if any of these
     * interfaces are found in the lookup, the editor will attach them if
     * appropriate</li>
     * </ul>
     * Typically the contents of a Tool's lookup do not change over its
     * lifetime.  For simple cases, it is enough to implement the desired
     * interfaces directly on the Tool implementation, and return
     * Lookups.singleton (this) from this method.
     */
    public Lookup getLookup();
    
    /**
     * Determine if a tool can work with an individual instance of Layer.  It
     * is possible to plug in custom layer implementations, and it is possible
     * that some tools will only work with certain types of layers - for 
     * example, a vector bezier spline editor might not want to work against
     * a raster layer implementation.
     */ 
    public boolean canAttach (Layer layer);

}
