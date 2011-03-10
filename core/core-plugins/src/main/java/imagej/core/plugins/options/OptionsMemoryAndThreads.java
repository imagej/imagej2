package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Memory & Threads... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Memory & Threads...", weight = 11) })
public class OptionsMemoryAndThreads implements ImageJPlugin{

	@Parameter(label = "Maximum memory (MB)")
	private int maxMemory;
	
	@Parameter(label = "Parallel threads for stacks")
	private int stackThreads;

	@Parameter(label = "Run garbage collector on status bar click")
	private boolean runGcOnClick;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
