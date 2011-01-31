package imagej.plugin.ij2;

public interface IPlugin extends Runnable {
	// IPlugin trivially extends Runnable, so that the name of the interface
	// unambiguously identifies an ImageJ plugin, rather than any Runnable object.
}
