package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Arrow Tool... dialog
 * 
 * @author Barry DeZonia
 */

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Arrow Tool...", weight = 5) })
public class OptionsArrowTool implements ImageJPlugin{

	@Parameter(label = "Width", min = "1", max = "50", persist=true)
	private int arrowWidth;
	
	@Parameter(label = "Size", min = "0", max = "30", persist=true)
	private int arrowSize;
	
	@Parameter(label = "Color", persist=true, choices = {"red","green","blue","magenta", "cyan", "yellow", "orange", "black", "white"})
	private String arrowColor;
	
	@Parameter(label = "Style", persist=true, choices = {"Filled", "Notched", "Open", "Headless"})
	private String arrowStyle;
	
	@Parameter(label = "Double headed")
	private boolean arrowDoubleHeaded;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
