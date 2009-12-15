package ij.plugin;

/** Plugins that acquire images or display windows should
	implement this interface. Plugins that process images 
	should implement the PlugInFilter interface. */
public interface PlugIn {

	/** This method is called when the plugin is loaded.
		'arg', which may be blank, is the argument specified
		for this plugin in IJ_Props.txt. */ 
	public void run(String arg);
	
}