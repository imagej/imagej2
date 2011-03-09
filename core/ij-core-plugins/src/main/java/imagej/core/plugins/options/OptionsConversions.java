package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Conversions... dialog
 * 
 * @author Barry DeZonia
 */

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Conversions...", weight = 10) })
public class OptionsConversions implements ImageJPlugin{

	@Parameter(label = "Scale When Converting")
	private boolean scaleWhenConverting;
	
	@Parameter(label = "Weighted RGB Conversions")
	private boolean weightedRgbConversions;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
