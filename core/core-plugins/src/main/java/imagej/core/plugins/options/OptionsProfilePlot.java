package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.SettingsKeys;

/**
 * Runs the Edit::Options::Profile Plot Options... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Profile Plot Options...", weight = 4) })
public class OptionsProfilePlot implements ImageJPlugin{

	@Parameter(label = "Width (pixels)",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_WIDTH)
	private int width;
	
	@Parameter(label = "Height (pixels)",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_HEIGHT)
	private int height;
	
	@Parameter(label = "Minimum Y",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_MIN_Y)
	private double minY;
	
	@Parameter(label = "Maximum Y",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_MAX_Y)
	private double maxY;
	
	@Parameter(label = "Fixed y-axis scale",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_FIXED_YSCALE)
	private boolean yFixedScale;

	@Parameter(label = "Do not save x-values",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_DISCARD_X)
	private boolean noSaveXValues;

	@Parameter(label = "Auto-close",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_AUTOCLOSE)
	private boolean autoClose;

	@Parameter(label = "Vertical profile",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_VERTICAL)
	private boolean vertProfile;

	@Parameter(label = "List values",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_LIST_VALUES)
	private boolean listValues;

	@Parameter(label = "Interpolate line profiles",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_INTERPOLATE)
	private boolean interpLineProf;

	@Parameter(label = "Draw grid lines",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_DRAW_GRID)
	private boolean drawGridLines;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
