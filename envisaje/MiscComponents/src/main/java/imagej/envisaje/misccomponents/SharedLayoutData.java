/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.misccomponents;

/**
 * An interface implemented by an ancestor component that allows nested panels
 * to all align their columns.  Usage:  Create a panel, set its layout to 
 * LDPLayout, and add some components to be displayed in a horizontal row.
 * Add them to a component that implements, or has an ancestor that implements,
 * SharedLayoutData.  The LDPLayout instance will access the SharedLayoutData
 * ancestor to find the column positions across all children that implement
 * LayoutDataProvider, even if they are not all children of the same component.
 * <p/>
 * To easily create a component that implements SharedLayoutData, just subclass
 * a JPanel or similar, implement SharedLayoutData and delegate to an instance
 * of DefaultSharedLayoutData, which handles memory management appropriately.
 *
 * @author Tim Boudreua
 */
public interface SharedLayoutData {
    /**
     * The x position for this column, as agreed up on as the max position
     * across all registered components, so columns are aligned.
     * 
     * @param column The index of the column.
     * @return The X position
     */
    public int xPosForColumn (int column);
    /**
     * Register a component that should participate in layout data
     * @param p
     */
    public void register (LayoutDataProvider p);
    /**
     * Unregister a component.
     * @param p
     */
    public void unregister (LayoutDataProvider p);
    /**
     * Called when the expanded method is called 
     * @param p
     * @param state
     */
    public void expanded (LayoutDataProvider p, boolean state);
}
