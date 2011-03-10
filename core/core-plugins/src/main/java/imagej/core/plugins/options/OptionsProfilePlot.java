package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

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

	@Parameter(label = "Width (pixels)", persist=true)
	private int width;
	
	@Parameter(label = "Height (pixels)", persist=true)
	private int height;
	
	@Parameter(label = "Minimum Y", persist=true)
	private double minY;
	
	@Parameter(label = "Maximum Y", persist=true)
	private double maxY;
	
	@Parameter(label = "Fixed y-axis scale", persist=true)
	private boolean yFixedScale;

	@Parameter(label = "Do not save x-values", persist=true)
	private boolean noSaveXValues;

	@Parameter(label = "Auto-close", persist=true)
	private boolean autoClose;

	@Parameter(label = "Vertical profile", persist=true)
	private boolean vertProfile;

	@Parameter(label = "List values", persist=true)
	private boolean listValues;

	@Parameter(label = "Interpolate line profiles", persist=true)
	private boolean interpLineProf;

	@Parameter(label = "Draw grid lines", persist=true)
	private boolean drawGridLines;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
