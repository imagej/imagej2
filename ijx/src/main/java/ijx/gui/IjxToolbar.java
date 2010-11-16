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

    public static final int RECTANGLE = 0;
    public static final int OVAL = 1;
    public static final int POLYGON = 2;
    public static final int FREEROI = 3;
    public static final int LINE = 4;
    public static final int POLYLINE = 5;
    public static final int FREELINE = 6;
    public static final int POINT = 7, CROSSHAIR = 7;
    public static final int WAND = 8;
    public static final int TEXT = 9;
    public static final int SPARE1 = 10;
    public static final int MAGNIFIER = 11;
    public static final int HAND = 12;
    public static final int DROPPER = 13;
    public static final int ANGLE = 14;
    public static final int SPARE2 = 15;
    public static final int SPARE3 = 16;
    public static final int SPARE4 = 17;
    public static final int SPARE5 = 18;
    public static final int SPARE6 = 19;
    public static final int SPARE7 = 20;
    public static final int SPARE8 = 21;
    public static final int SPARE9 = 22;
    public static final int DOUBLE_CLICK_THRESHOLD = 650;
    static final int NUM_TOOLS = 23;
    static final int NUM_BUTTONS = 21;
    static final int SIZE = 26;
    static final int OFFSET = 5;
    static final String BRUSH_SIZE = "toolbar.brush.size";
    static final String ARC_SIZE = "toolbar.arc.size";

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
