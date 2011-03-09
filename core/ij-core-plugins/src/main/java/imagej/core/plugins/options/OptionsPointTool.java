package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Point Tool...", weight = 6) })
public class OptionsPointTool implements ImageJPlugin{

	@Parameter(label = "Mark Width (pixels)")
	private int markWidth;
	
	@Parameter(label = "Auto-Measure")
	private boolean autoMeasure;
	
	@Parameter(label = "Auto-Next Slice")
	private boolean autoNextSlice;
	
	@Parameter(label = "Add to ROI Manager")
	private boolean addToRoiMgr;
	
	@Parameter(label = "Label Points")
	private boolean labelPoints;
	
	@Parameter(label = "Selection Color", choices =
	{"red","green","blue","magenta", "cyan", "yellow", "orange", "black", "white"})
	private String selectionColor;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
