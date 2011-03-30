package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.SettingsKeys;

/**
 * Runs the Edit::Options::Input/Output... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Input/Output...", weight = 2) })
public class OptionsInputOutput implements ImageJPlugin{

	@Parameter(label = "JPEG quality (0-100)",
		persistKey = SettingsKeys.OPTIONS_IO_JPEG_QUALITY)
	private int jpegQuality;
	
	@Parameter(label = "GIF and PNG transparent index",
		persistKey = SettingsKeys.OPTIONS_IO_TRANSPARENT_INDEX)
	private int transparentIndex;

	@Parameter(label = "File extension for tables",
		persistKey = SettingsKeys.OPTIONS_IO_FILE_EXT)
	private String tableFileExtension;

	@Parameter(label = "Use JFileChooser to open/save",
		persistKey = SettingsKeys.OPTIONS_IO_USE_JFILECHOOSER)
	private boolean useJFileChooser;

	@Parameter(label = "Save TIFF and raw in Intel byte order",
		persistKey = SettingsKeys.OPTIONS_IO_SAVE_INTEL)
	private boolean saveOrderIntel;
	
	// TODO - in IJ1 these were grouped visually. How is this now done?
	
	@Parameter(label = "Result Table: Copy column headers",
		persistKey = SettingsKeys.OPTIONS_IO_COPY_COLUMNS)
	private boolean copyColumnHeaders;
	
	@Parameter(label = "Result Table: Copy row numbers",
		persistKey = SettingsKeys.OPTIONS_IO_COPY_ROWS)
	private boolean copyRowNumbers;

	@Parameter(label = "Result Table: Save column headers",
		persistKey = SettingsKeys.OPTIONS_IO_SAVE_COLUMNS)
	private boolean saveColumnHeaders;
	
	@Parameter(label = "Result Table: Save row numbers",
		persistKey = SettingsKeys.OPTIONS_IO_SAVE_ROWS)
	private boolean saveRowNumbers;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
