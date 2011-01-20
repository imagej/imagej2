/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.api.selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Abstract definition of selection within a Layer.  Layer implementations
 * should provide an instance of Selection in their Lookup;  tools which
 * operate on selection will then interact with the selection instance.
 * <p/>
 * For a concrete implementation, see the ShapeSelection class, which operates
 * on adding/removing shapes.  Implementations with non-raster backing storage
 * will want to implement their own way of translating shapes provided by
 * selection tools into an actual selection of a type of their choosing.
 *
 * @author Tim Boudreau
 */
public abstract class Selection<T> {
    private final Class<T> type;
    public enum Op {
        ADD, SUBTRACT, XOR, INTERSECT, REPLACE;
        @Override
        public String toString() {
            return NbBundle.getMessage(Selection.class, name());
        }
        
    };
    
    /**
     * Create a new selection object that operates on the given type.
     * @param type
     */
    protected Selection (Class<T> type) {
        this.type = type;
    }
        
    /**
     * Get the type of object this selection accepts.  Used by tools to determine if
     * they can operate on this selection.
     * 
     * @return
     */
    public final Class<T> type() {
        return type;
    }
    
    /**
     * Get a representation of the selection as an instance of T.
     * @return The selection
     */
    public abstract T get();
    /**
     * Add something into the selection.
     * @param content The thing being added
     * @param op How it should affect the existing selection, if any
     */
    public abstract void add (T content, Op op);
    /**
     * Clear the contents of the selection.
     */
    public abstract void clear();
    /**
     * Paint the selection.
     * @param g The graphics context
     */
    public abstract void paint(Graphics2D g);
    /**
     * Hook method for a concrete implementation to shuffle an edit into the
     * undo stack for the application.  Implementations should create an
     * undoable edit in their add(), selectAll() and clear() methods and pass it to this
     * method before returning.
     * 
     * @param edit
     */
    protected final void changed(UndoableEdit edit) {
        UndoManager mgr = (UndoManager) 
            Utilities.actionsGlobalContext().lookup(UndoManager.class);
        if (mgr != null) {
            mgr.addEdit(edit);
        }
        for (ChangeListener l : listeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * Convenience method so implementations can be consistent in the colors
     * and outlining they use to paint the selection.
     * 
     * @param g The graphics context
     * @param content A shape
     */
    public static void paintSelectionAsShape (Graphics2D g, Shape shape) {
        Color fill = new Color (255, 255, 255, 120);
        Graphics2D g1 = (Graphics2D) g.create();
        g1.setColor (fill);
        g1.fill(shape);
        g1.setXORMode(Color.BLUE);
        g1.draw(shape);
        g1.dispose();
    }

    /**
     * Copy the passed selection into this selection.  Implementations should
     * call selection.clearNoUndo() on the passed selection.  This method is
     * called when the selected layer the user is editing changes.  Since 
     * selection is owned by the layer, the old layer's selection should be 
     * cleared and this selection should become the selection from the passed
     * instance, without generating any undo events.
     * @param selection
     */
    public abstract void translateFrom(Selection selection);
    /**
     * Get this selection as a shape.  This is a fallback for non-shape-based
     * selections, so that selection can be shared across layers of different
     * types.  If this method returns null, the selection will be cleared
     * when the active layer changes.
     * @return
     */
    public abstract Shape asShape();
    /**
     * Clear this selection without generating an undo event to be passed to
     * the changed() method.
     */
    public abstract void clearNoUndo();
    
    public abstract void invert(Rectangle bds);
    
    private Set<ChangeListener> listeners = Collections.synchronizedSet(new HashSet<ChangeListener>());
    public final void addChangeListener (ChangeListener l) {
        listeners.add (l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        listeners.remove (l);
    }
}
