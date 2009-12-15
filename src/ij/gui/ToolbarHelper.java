package ij.gui;

import ijx.gui.IjxToolbar;
import ij.Prefs;
import java.awt.Color;

/**
 *
 * @author GBH
 */
public class ToolbarHelper {

    public static Color foregroundColor = Prefs.getColor(Prefs.FCOLOR, Color.black);
    public static Color backgroundColor = Prefs.getColor(Prefs.BCOLOR, Color.white);
    public static boolean brushEnabled;
    public static int brushSize = (int) Prefs.get(Toolbar.BRUSH_SIZE, 15);
    private static IjxToolbar instance;
    
    	/** Returns a reference to the ImageJ toolbar. */
	public static IjxToolbar getInstance() {
		return instance;
	}
}
