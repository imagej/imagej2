package ijx.plugin.parameterized;



import ijx.plugin.api.PlugIn;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/*
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 * @author Grant Harris gharris at mbl.edu
 */


public abstract class AbstractPlugIn implements PlugIn, Runnable, Callable<Map<String, Object>> {

	public void run(String arg) {
		PlugInFunctions.runInteractively(this);
	}

	public abstract void run();

	public Map<String, Object> execute(Object... parameters)
			throws PlugInException {
		return PlugInFunctions.execute(this, parameters);
	}

	public void setParameter(String key, Object value) {
		PlugInFunctions.setParameter(this, key, value);
	}

	public Map<String, Object> getOutputMap() {
		return PlugInFunctions.getOutputMap(this);
	}

    public Map<String, Object> call() { // for non-interactive... must set parameters before
        run();
        return getOutputMap();
    }
    public Map<String, Object> call(Map<String, Object> inputMap) { // for non-interactive... must set parameters before
        run();
        PlugInFunctions.runAsFuture(this);
        return getOutputMap();
    }
}
