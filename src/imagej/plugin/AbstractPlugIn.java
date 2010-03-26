package imagej.plugin;



import ij.plugin.PlugIn;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/*
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 * @author Grant Harris gharris at mbl.edu
 */

/*
 * A versatile abstract plugin class

As discussed on the ImageJX list.  The idea is to store input/output
parameters as fields of the plugin class.  They must be annotated
(with fiji.plugin.Parameter) so that a dialog can be constructed at
runtime.

Annotated in such a way, the parameters can also be set from a
map containing key/value pairs; this is only recommended if you need
the flexibility, or if you need to test quickly, as it moves the
compile time validation to a runtime validation.

Using this to make a plugin is easy: just implement the run()
method of a Runnable, and extend AbstractPlugIn:

	import fiji.plugin.AbstractPlugIn;

	public class MyPlugIn extends AbstractPlugIn {
		public void run() {
			IJ.log("Hello, World");
		}
	}

To use a parameter, just add a field:

	import fiji.plugin.Parameter;
	...
		@Parameter public String Name;

This will make a proper ImageJ plugin which shows a dialog constructed from
the annotated input parameters at runtime before it runs the run() method.

As this is only a proof of concept at this point, the only supported
input parameter type is a String, and the only supported output
parameter is an ImagePlus.
 */

public abstract class AbstractPlugIn implements PlugIn, Runnable, Callable<Map<String, Object> > {

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

    public Map<String, Object> call() { // for non-interactive... must set parameters before
        run();
        return getOutputMap();
    }
}
