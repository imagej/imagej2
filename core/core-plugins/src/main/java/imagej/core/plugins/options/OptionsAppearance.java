package imagej.core.plugins.options;

import imagej.SettingsKeys;
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

	@Parameter(label = "Interpolate zoomed images", persistKey = SettingsKeys.OPTIONS_APPEARANCE_INTERPOLATE_ZOOMED_IMAGES)
	private boolean interpZoomedImages;
	
	@Parameter(label = "Open images at 100%", persistKey = SettingsKeys.OPTIONS_APPEARANCE_FULL_ZOOMED_IMAGES)
	private boolean fullZoomImages;
	
	@Parameter(label = "Black canvas", persistKey = SettingsKeys.OPTIONS_APPEARANCE_BLACK_CANVAS)
	private boolean blackCanvas;
	
	@Parameter(label = "No image border", persistKey = SettingsKeys.OPTIONS_APPEARANCE_NO_IMAGE_BORDER)
	private boolean noImageBorder;
	
	@Parameter(label = "Use inverting lookup table", persistKey = SettingsKeys.OPTIONS_APPEARANCE_USE_INVERTING_LUT)
	private boolean useInvertingLUT;
	
	@Parameter(label = "Antialiased tool icons", persistKey = SettingsKeys.OPTIONS_APPEARANCE_ANTIALIASED_TOOL_ICONS)
	private boolean antialiasedToolIcons;
	
	@Parameter(label = "Menu font size (points)", persistKey = SettingsKeys.OPTIONS_APPEARANCE_MENU_FONT_SIZE)
	private int menuFontSize;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
