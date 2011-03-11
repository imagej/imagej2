package imagej.core.plugins.options;

import imagej.SettingsKeys;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Fonts... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Fonts...", weight = 3) })
public class OptionsFont implements ImageJPlugin{

	@Parameter(label = "Font",  // TODO populate from system fonts
		persistKey = SettingsKeys.OPTIONS_FONT_NAME)
	private String font;
	
	@Parameter(label = "Size", min = "8", max = "72",
		persistKey = SettingsKeys.OPTIONS_FONT_SIZE)
	private int fontSize;
	
	@Parameter(label = "Style", choices={"Plain", "Bold", "Italic", "Bold + Italic"},
		persistKey = SettingsKeys.OPTIONS_FONT_STYLE)
	private String fontStyle;

	@Parameter(label = "Smooth",
		persistKey = SettingsKeys.OPTIONS_FONT_SMOOTHING)
	private boolean fontSmooth;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
