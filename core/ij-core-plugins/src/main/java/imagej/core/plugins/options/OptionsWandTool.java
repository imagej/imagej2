package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Wand Tool...", weight = 7) })
public class OptionsWandTool implements ImageJPlugin{

	@Parameter(label = "Mode", choices = {"Legacy", "4-connected", "8-connected"})
	private String mode;
	
	@Parameter(label = "Tolerance")
	private double tolerance;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
