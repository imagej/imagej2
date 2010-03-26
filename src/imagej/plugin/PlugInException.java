package imagej.plugin;

/**
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 */
public class PlugInException extends Exception {
	public PlugInException() {
		super();
	}

	public PlugInException(String reason) {
		super(reason);
	}
}
