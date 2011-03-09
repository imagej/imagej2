package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Profile Plot Options...", weight = 4) })
public class OptionsProfilePlot implements ImageJPlugin{

	@Parameter(label = "Width (pixels)")
	private int width;
	
	@Parameter(label = "Height (pixels)")
	private int height;
	
	@Parameter(label = "Minimum Y")
	private double minY;
	
	@Parameter(label = "Maximum Y")
	private double maxY;
	
	@Parameter(label = "Fixed y-axis scale")
	private boolean yFixedScale;

	@Parameter(label = "Do not save x-values")
	private boolean noSaveXValues;

	@Parameter(label = "Auto-close")
	private boolean autoClose;

	@Parameter(label = "Vertical profile")
	private boolean vertProfile;

	@Parameter(label = "List values")
	private boolean listValues;

	@Parameter(label = "Interpolate line profiles")
	private boolean interpLineProf;

	@Parameter(label = "Draw grid lines")
	private boolean drawGridLines;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
