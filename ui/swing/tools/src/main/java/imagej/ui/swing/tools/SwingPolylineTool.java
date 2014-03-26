package imagej.ui.swing.tools;

import imagej.tool.AbstractTool;
import imagej.tool.Tool;
import org.scijava.plugin.Plugin;

/**
 * Swing/JHotDraw implementation of multi-segmented line tool.
 * 
 * @author Benjamin Nanes
 */
@Plugin(type = Tool.class, name = "Polyline", description = "Polyline overlays",
	iconPath = "/icons/tools/polyline.png", priority = SwingPolylineTool.PRIORITY)
public class SwingPolylineTool  extends AbstractTool {

	public static final double PRIORITY = SwingLineTool.PRIORITY - 1;

}
