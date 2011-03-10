package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

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

	@Parameter(label = "JPEG quality (0-100)", persist=true)
	private int jpegQuality;
	
	@Parameter(label = "GIF and PNG transparent index", persist=true)
	private int transparentIndex;

	@Parameter(label = "File extension for tables", persist=true)
	private String tableFileExtension;

	@Parameter(label = "Use JFileChooser to open/save", persist=true)
	private boolean useJFileChooser;

	@Parameter(label = "Save TIFF and raw in Intel byte order", persist=true)
	private boolean saveOrderIntel;
	
	// TODO - in IJ1 these were grouped visually. How is this now done?
	
	@Parameter(label = "Result Table: Copy column headers", persist=true)
	private boolean copyColumnHeaders;
	
	@Parameter(label = "Result Table: Copy row numbers", persist=true)
	private boolean copyRowNumbers;

	@Parameter(label = "Result Table: Save column headers", persist=true)
	private boolean saveColumnHeaders;
	
	@Parameter(label = "Result Table: Save row numbers", persist=true)
	private boolean saveRowNumbers;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
