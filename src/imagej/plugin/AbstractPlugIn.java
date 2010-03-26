package imagej.plugin;

import ij.plugin.PlugIn;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 */
public abstract class AbstractPlugIn implements PlugIn, Runnable {
	public void run(String arg) {
		PlugInFunctions.runInteractively(this);
	}

	public abstract void run();

	public Map<String, Object> run(Object... parameters)
			throws PlugInException {
		return PlugInFunctions.run(this, parameters);
	}

	public void setParameter(String key, Object value) {
		PlugInFunctions.setParameter(this, key, value);
	}

	public Map<String, Object> getOutputMap() {
		return PlugInFunctions.getOutputMap(this);
	}
}
