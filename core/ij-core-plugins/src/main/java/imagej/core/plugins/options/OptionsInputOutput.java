package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Input/Output...", weight = 2) })
public class OptionsInputOutput implements ImageJPlugin{

	@Parameter(label = "JPEG quality (0-100)")
	private int jpegQuality;
	
	@Parameter(label = "GIF and PNG transparent index")
	private int transparentIndex;

	@Parameter(label = "File extension for tables")
	private String tableFileExtension;

	@Parameter(label = "Use JFileChooser to open/save")
	private boolean useJFileChooser;

	@Parameter(label = "Save TIFF and raw in Intel byte order")
	private boolean saveOrderIntel;
	
	// TODO - in IJ1 these were grouped visually. How is this now done?
	
	@Parameter(label = "Result Table: Copy column headers")
	private boolean copyColumnHeaders;
	
	@Parameter(label = "Result Table: Copy row numbers")
	private boolean copyRowNumbers;

	@Parameter(label = "Result Table: Save column headers")
	private boolean saveColumnHeaders;
	
	@Parameter(label = "Result Table: Save row numbers")
	private boolean saveRowNumbers;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
