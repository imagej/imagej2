package imagej.core.plugins.options;

import imagej.SettingsKeys;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Point Tool... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Point Tool...", weight = 6) })
public class OptionsPointTool implements ImageJPlugin{

	@Parameter(label = "Mark Width (pixels)",
		persistKey = SettingsKeys.OPTIONS_POINT_MARK_WIDTH)
	private int markWidth;
	
	@Parameter(label = "Auto-Measure",
		persistKey = SettingsKeys.OPTIONS_POINT_AUTO_MEASURE)
	private boolean autoMeasure;
	
	@Parameter(label = "Auto-Next Slice",
		persistKey = SettingsKeys.OPTIONS_POINT_AUTOSLICE)
	private boolean autoNextSlice;
	
	@Parameter(label = "Add to ROI Manager",
		persistKey = SettingsKeys.OPTIONS_POINT_ADD_ROI)
	private boolean addToRoiMgr;
	
	@Parameter(label = "Label Points",
		persistKey = SettingsKeys.OPTIONS_POINT_LABEL_POINTS)
	private boolean labelPoints;
	
	@Parameter(label = "Selection Color", choices =
	{"red","green","blue","magenta", "cyan", "yellow", "orange", "black", "white"},
		persistKey = SettingsKeys.OPTIONS_POINT_SELECTION_COLOR)
	private String selectionColor;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
