package imagej.plugin;

public interface ImageJPlugin extends IPlugin {
	// ImageJPlugin trivially extends IPlugin, so that the name of the interface
	// unambiguously identifies an ImageJ plugin, for discovery by SezPoz.
}
