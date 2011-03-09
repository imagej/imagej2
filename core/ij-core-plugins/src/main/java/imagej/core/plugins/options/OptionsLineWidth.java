package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Line Width...", weight = 1) })
public class OptionsLineWidth implements ImageJPlugin{

	@Parameter(label = "Line Width")
	private int lineWidth;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
