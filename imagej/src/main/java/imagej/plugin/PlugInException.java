package imagej.plugin;


public class PlugInException extends Exception {
	public PlugInException() {
		super();
	}

	public PlugInException(String reason) {
		super(reason);
	}
}
