/*
 * Hibernator.java
 *
 * Created on October 25, 2006, 12:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.image;

/**
 * Optional object which may be available through a Layer implementation's
 * Lookup.  Enables a Layer to enter a dormant, reduced memory state when it
 * is not likely to be painted for some time.  Implementations may do such
 * things as store raster data in slower but more memory-efficient forms or
 * off the Java heap.
 * <p>
 * This is typically done for Layer objects which are being placed on an 
 * undo stack and will most likely never be touched again (they have been
 * deleted;  if an undo operation is slow, that's better than running out
 * of memory).  It may also be done for images which are not currently visible
 * on screen if the system is low on memory.
 *
 * @author Tim Boudreau
 */
public interface Hibernator {
    /**
     * Cause the Layer providing this instance of Hibernator to enter a
     * low-memory, dormant state.
     */ 
    void hibernate();
    
    /**
     * Cause the Layer providing this instance of Hibernator to return to
     *    a fully-loaded, responsive state.
     * @param immediately If true, the system should try to return to an 
     *    active state in the current thread, possibly blocking for I/O.
     *    If false, the operation may be done lazily.
     */ 
    void wakeup(boolean immediately, Runnable notify);
}
