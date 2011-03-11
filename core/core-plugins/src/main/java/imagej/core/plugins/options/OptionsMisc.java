package imagej.core.plugins.options;

import imagej.SettingsKeys;
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

	@Parameter(label = "Divide by zero value",
		persistKey = SettingsKeys.OPTIONS_MISC_DBZ_VALUE)
	private String divByZeroVal;

	@Parameter(label = "Use pointer cursor",
		persistKey = SettingsKeys.OPTIONS_MISC_POINTER_CURSOR)
	private boolean usePtrCursor;

	@Parameter(label = "Hide \"Process Stack?\" dialog",
		persistKey = SettingsKeys.OPTIONS_MISC_HIDE_STACK_MSG)
	private boolean hideProcessStackDialog;

	@Parameter(label = "Require command key for shortcuts",
		persistKey = SettingsKeys.OPTIONS_MISC_REQUIRE_COMMAND)
	private boolean requireCommandKey;

	@Parameter(label = "Move isolated plugins to Misc. menu",
		persistKey = SettingsKeys.OPTIONS_MISC_MOVE_PLUGINS)
	private boolean moveIsolatedPlugins;

	@Parameter(label = "Run single instance listener",
		persistKey = SettingsKeys.OPTIONS_MISC_SINGLE_INSTANCE)
	private boolean runSingleInstanceListener;

	@Parameter(label = "Debug mode",
		persistKey = SettingsKeys.OPTIONS_MISC_DEBUG_MODE)
	private boolean debugMode;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
