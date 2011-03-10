package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Appearance... dialog
 * 
 * @author Barry DeZonia
 */

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Appearance...", weight = 9) })
public class OptionsAppearance implements ImageJPlugin{

	@Parameter(label = "Interpolate zoomed images")
	private boolean interpZoomedImages;
	
	@Parameter(label = "Open images at 100%")
	private boolean fullZoomImages;
	
	@Parameter(label = "Black canvas")
	private boolean blackCanvas;
	
	@Parameter(label = "No image border")
	private boolean noImageBorder;
	
	@Parameter(label = "Use inverting lookup table")
	private boolean useInvertingLUT;
	
	@Parameter(label = "Antialiased tool icons")
	private boolean antialiasedToolIcons;
	
	@Parameter(label = "Menu font size (points)")
	private int menuFontSize;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
