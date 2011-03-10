package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Misc... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Misc...", weight = 15) })
public class OptionsMisc implements ImageJPlugin{

	@Parameter(label = "Divide by zero value", persist=true)
	private String divByZeroVal;

	@Parameter(label = "Use pointer cursor", persist=true)
	private boolean usePtrCursor;

	@Parameter(label = "Hide \"Process Stack?\" dialog", persist=true)
	private boolean hideProcessStackDialog;

	@Parameter(label = "Require command key for shortcuts", persist=true)
	private boolean requireCommandKey;

	@Parameter(label = "Move isolated plugins to Misc. menu", persist=true)
	private boolean moveIsolatedPlugins;

	@Parameter(label = "Run single instance listener", persist=true)
	private boolean runSingleInstanceListener;

	@Parameter(label = "Debug mode", persist=true)
	private boolean debugMode;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
