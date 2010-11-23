package ijx.gui;

import ijx.plugin.MacroInstaller;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *
 * @author GBH <imagejdev.org>
 */
public interface IjxToolbar extends ActionListener, ItemListener, MouseListener, MouseMotionListener {
    int NUM_TOOLS = 23;
    int NUM_BUTTONS = 21;
    int SIZE = 26;
    int OFFSET = 5;
    String BRUSH_SIZE = "toolbar.brush.size";
    String ARC_SIZE = "toolbar.arc.size";
    int ANGLE = 14;
    int CROSSHAIR = 7;
    int DOUBLE_CLICK_THRESHOLD = 650;
    int DROPPER = 13;
    int FREELINE = 6;
    int FREEROI = 3;
    int HAND = 12;
    int LINE = 4;
    int MAGNIFIER = 11;
    int OVAL = 1;
    int POINT = 7;
    int POLYGON = 2;
    int POLYLINE = 5;
    int RECTANGLE = 0;
    int SPARE1 = 10;
    int SPARE2 = 15;
    int SPARE3 = 16;
    int SPARE4 = 17;
    int SPARE5 = 18;
    int SPARE6 = 19;
    int SPARE7 = 20;
    int SPARE8 = 21;
    int SPARE9 = 22;
    int TEXT = 9;
    int WAND = 8;

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

    Color getBackgroundColor();

    /**
     * Returns the size of the brush tool, or 0 if the brush tool is not enabled.
     */
    int getBrushSize();

    int getButtonSize();

    /**
     * @deprecated
     * replaced by getForegroundColor()
     */
     Color getColor();

    Color getForegroundColor();

    Dimension getMinimumSize();

    /**
     * Returns 'true' if the multi-point tool is enabled.
     */
    boolean getMultiPointMode();

    Dimension getPreferredSize();

    /**
     * Returns the rounded rectangle arc size, or 0 if the rounded rectangle tool is not enabled.
     */
    int getRoundRectArcSize();

    /**
     * Returns the ID of the current tool (Toolbar.RECTANGLE,
     * Toolbar.OVAL, etc.).
     */
    int getToolId();

    /**
     * Returns the ID of the tool whose name (the description displayed in the status bar)
     * starts with the specified string, or -1 if the tool is not found.
     */
    int getToolId(String name);

    String getToolName();

    void restorePreviousTool();

    void runMacroTool(int id);

    void setBackgroundColor(Color c);

    /**
     * Set the size of the brush tool, which must be greater than 4.
     */
    void setBrushSize(int size);

    /**
     * @deprecated
     * replaced by setForegroundColor()
     */
     void setColor(Color c);

    void setForegroundColor(Color c);

    /**
     * Sets the rounded rectangle arc size (pixels).
     */
    void setRoundRectArcSize(int size);

    boolean setTool(String name);

    void setTool(int tool);
    
    void repaint();
}
