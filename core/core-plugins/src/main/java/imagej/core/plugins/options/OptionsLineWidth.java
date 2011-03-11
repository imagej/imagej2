package imagej.core.plugins.options;

import imagej.SettingsKeys;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Line Width... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Line Width...", weight = 1) })
public class OptionsLineWidth implements ImageJPlugin{

	@Parameter(label = "Line Width",
		persistKey = SettingsKeys.OPTIONS_LINEWIDTH_WIDTH)
	private int lineWidth;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
