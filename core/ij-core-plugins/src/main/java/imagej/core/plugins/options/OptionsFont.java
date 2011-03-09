package imagej.core.plugins.options;

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

	@Parameter(label = "Font")  // TODO populate from system fonts
	private String font;
	
	@Parameter(label = "Size", min = "8", max = "72")
	private int fontSize;
	
	@Parameter(label = "Style", choices={"Plain", "Bold", "Italic", "Bold + Italic"})
	private String fontStyle;

	@Parameter(label = "Smooth")
	private boolean fontSmooth;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
