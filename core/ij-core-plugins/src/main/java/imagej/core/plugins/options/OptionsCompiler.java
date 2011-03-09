package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Compiler... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Compiler...", weight = 13) })
public class OptionsCompiler implements ImageJPlugin{

	@Parameter(label = "Target", choices = {"1.4","1.5","1.6","1.7"})
	private String targetJavaVersion;
	
	@Parameter(label = "Generate debugging ino (javac -g)")
	private boolean generateDebugInfo;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
