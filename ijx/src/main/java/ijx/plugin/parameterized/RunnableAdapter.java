package ijx.plugin.parameterized;

import java.util.Map;
/*
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 */

/*
 Add convenience classes to wrap Runnable instances into PlugIns

With the new concept of input/output parameters being encoded as annotated
fields, all that is left of the generic plugin interface is a public run()
method, i.e. the Runnable interface.

Provide ways (time will tell which ones are the most useful) to wrap
Runnables as plugins:

	PlugIn plugin = new RunnableAdapter(new MyPlugIn());

or

	public class Wrapped_PlugIn extends RunnableAdapter {
		public Wrapped_PlugIn() {
			super(new MyPlugIn());
		}
	}

or

	public class Wrapped_PlugIn extends PlugInWrapper {
		public Wrapped_PlugIn() {
			super("MyPlugIn");
		}
	}

or in a plugins.config:

	Bla>Blub, "Blizz", fiji.plugin.PlugInWrapper("MyPlugIn")

Unfortunately, it is not possible to do this with generics
(Bla>Blub, "Blizz", fiji.plugin.PlugInWrapper<MyPlugIn>), because
Java generics work by erasure.  Therefore, a PlugInWrapper<T>
cannot instantiate T.

But a possible option is to teach ImageJA to fall back to using the
PlugInWrapper if it finds that class and the plugin is an instance
of a Runnable.

*/

 public class RunnableAdapter extends AbstractPlugIn {

    Runnable plugin;

    public RunnableAdapter(Runnable plugin) {
        this.plugin = plugin;
    }

    public void run() {
        plugin.run();
    }

    public void runInteractively() {
        PlugInFunctions.runInteractively(plugin);
    }

    public Map<String, Object> execute(Object... parameters)
            throws PlugInException {
        return PlugInFunctions.execute(plugin, parameters);
    }

    public Map<String, Object> execute(Map<String, Object> inputMap)
            throws PlugInException {
        return PlugInFunctions.execute(plugin, inputMap);
    }

    @Override
    public void setParameter(String key, Object value) {
        PlugInFunctions.setParameter(plugin, key, value);
    }

    @Override
    public Map<String, Object> getOutputMap() {
        return PlugInFunctions.getOutputMap(plugin);
    }
}

