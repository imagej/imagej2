
package ijx.gui;

import ij.plugin.MacroInstaller;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *
 * @author GBH
 */
public interface IjxToolbar extends ActionListener, ItemListener, MouseListener, MouseMotionListener {
    
    /**
     * Used by the MacroInstaller class to install macro tools.
     */
    void addMacroTool(String name, MacroInstaller macroInstaller, int id);

    /**
     * Adds a tool to the toolbar. The 'toolTip' string is displayed in the status bar
     * when the mouse is over the tool icon. The 'toolTip' string may include icon
     * (http://rsb.info.nih.gov/ij/developer/macro/macros.html#tools).
     * Returns the tool ID, or -1 if all tools are in use.
     */
    int addTool(String toolTip);

    /**
     * Returns the ID of the tool whose name (the description displayed in the status bar)
     * starts with the specified string, or -1 if the tool is not found.
     */
    int getToolId(String name);

    void restorePreviousTool();

    /**
     * Obsolete. Use setForegroundColor().
     */
    void setColor(Color c);

    boolean setTool(String name);

    void setTool(int tool);

}
