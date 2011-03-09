package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Misc...", weight = 15) })
public class OptionsMisc implements ImageJPlugin{

	@Parameter(label = "Divide by zero value")
	private String divByZeroVal;

	@Parameter(label = "Use pointer cursor")
	private boolean usePtrCursor;

	@Parameter(label = "Hide \"Process Stack?\" dialog")
	private boolean hideProcessStackDialog;

	@Parameter(label = "Require command key for shortcuts")
	private boolean requireCommandKey;

	@Parameter(label = "Move isolated plugins to Misc. menu")
	private boolean moveIsolatedPlugins;

	@Parameter(label = "Run single instance listener")
	private boolean runSingleInstanceListener;

	@Parameter(label = "Debug mode")
	private boolean debugMode;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
