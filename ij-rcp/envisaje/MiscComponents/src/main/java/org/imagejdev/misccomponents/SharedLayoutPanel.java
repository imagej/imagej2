/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.misccomponents;

import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A panel which, if it has an ancestor that implements SharedLayoutData, will
 * automatically align its so that all children of that parent have their
 * columns aligned.  Use for a set of components (such as a label for something
 * followed by a control that affects that setting) which will appear in an
 * ancestor component, but may be children of each other, yet should nonetheless
 * have their columns aligned.
 * <p/>
 * Note that the layout manager may not be set at runtime on instances of
 * SharedLayoutPanel.
 * <p/>
 * SharedLayoutPanels also have an &quot;expanded&quot; property - a component
 * may hide some data until a button is clicked, at which point it grows and
 * displays a detail view.  By default any SharedLayoutPanel shares its 
 * expanded setting with all others owned by the same SharedLayoutData ancestor,
 * so if any one is expanded, all others are set to unexpanded - so only one is
 * expanded at a time.
 *
 * @author Tim Boudreau
 */
public class SharedLayoutPanel extends JPanel implements LayoutDataProvider {
    //XXX move this and LDPLayout to their own module
    private boolean initialized;
    public SharedLayoutPanel() {
        super (new LDPLayout());
        initialized = true;
    }
    
    public SharedLayoutPanel(Component c) {
        this();
        add (c);
    }
    
    /**
     * Overridden to throw an exception if called at runtime.
     * @param mgr The layout manager
     */
    @Override
    public final void setLayout (LayoutManager mgr) {
        if (initialized) {
            throw new UnsupportedOperationException ("Cannot set layout on a SharedLayoutPanel");
        }
        super.setLayout (mgr);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        SharedLayoutData p = (SharedLayoutData) SwingUtilities.getAncestorOfClass(SharedLayoutData.class, this);
        if (p != null) {
            p.register(this);
        }
    }
    
    @Override
    public void removeNotify() {
        SharedLayoutData p = (SharedLayoutData) SwingUtilities.getAncestorOfClass(SharedLayoutData.class, this);
        if (p != null) {
            p.unregister(this);
        }
        super.removeNotify();
    }

    /**
     * Gets the column position for this column from the layout manager.
     * @param col The column
     * @return A pixel position in the x axis
     */
    public final int getColumnPosition(int col) {
        LDPLayout layout = (LDPLayout) getLayout();
        return layout.getColumnPosition(this, col);
    }

    /**
     * Returns false by default.  Override if you want to support expansion.
     * @return
     */
    public boolean isExpanded() {
        return false;
    }

    /**
     * Does nothing by default;  override to handle the case that you actually
     * want to change the set of child components when expanded.  You are 
     * responsible for providing a control that actually sets the expanded 
     * state.
     * 
     * @param val To expand or not
     */
    public void doSetExpanded(boolean val) {
        //do nothing by default
    }
}
