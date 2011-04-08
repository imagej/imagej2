/*
 * Primitive.java
 *
 * Created on October 23, 2006, 8:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector;

import java.awt.Graphics2D;
import java.io.Serializable;

/**
 * Parent interface for all vector primitives.  A primitive
 * is an object that represents the data passed to one call
 * on a Graphics2D object.  This includes both shapes that
 * are painted, and colors and strokes that are set.  These
 * are used to create a stream of events that can be later
 * replayed to another Graphics context to recreate the elements
 * painted.
 *
 * @see net.java.dev.imagine.api.vector.painting.
 *       VectorWrapperGraphics
 * @author Tim Boudreau
 */
public interface Primitive extends Serializable {
    public static final int[] EMPTY_INT = new int[0];
    /**
     * Paint this primitive (this may mean not painting anything,
     * but setting the stroke or color represented by this object) -
     * apply this object's data to the passed graphics context.
     */
    public void paint (Graphics2D g);
    /**
     * Create a duplicate of this Primitive, of the same type as
     * this primitive (or functionally and visually identical to
     * it), whose data is independent from this one, and which can
     * be altered without affecting the original.
     * @return An independent duplicate of this primitive
     */
    public Primitive copy();
}
