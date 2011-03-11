package imagej.core.plugins.options;

import imagej.SettingsKeys;
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

	@Parameter(label = "Maximum memory (MB)",
		persistKey = SettingsKeys.OPTIONS_MEMORYTHREADS_MAX_MEMORY)
	private int maxMemory;
	
	@Parameter(label = "Parallel threads for stacks",
		persistKey = SettingsKeys.OPTIONS_MEMORYTHREADS_STACK_THREADS)
	private int stackThreads;

	@Parameter(label = "Run garbage collector on status bar click",
		persistKey = SettingsKeys.OPTIONS_MEMORYTHREADS_RUN_GC)
	private boolean runGcOnClick;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
