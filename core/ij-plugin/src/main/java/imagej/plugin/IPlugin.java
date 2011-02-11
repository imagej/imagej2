package imagej.plugin;

public interface IPlugin extends Runnable {
	// IPlugin trivially extends Runnable, so that the name of the interface
	// unambiguously identifies a plugin, rather than any Runnable object.
}
