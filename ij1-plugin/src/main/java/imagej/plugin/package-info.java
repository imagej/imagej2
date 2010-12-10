/*
 *  Parameterized or Abstract Plugin
 *
 * A versatile abstract plugin class

As discussed on the ImageJX list.  The idea is to store input/output
parameters as fields of the plugin class.  They must be annotated
(with fiji.plugin.Parameter) so that a dialog can be constructed at
runtime.

Annotated in such a way, the parameters  can also be set from a
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

Supported input parameter types:
 String

Output parameter types:
IjxImagePlus

 Added:
 implements Callable<Map<String, Object>>
 for non-interactive... must set parameters before
 executed by [?]

 *
 *  Unless a label is specified, the field name will be used for the dialog,
    so it starts upcased, and underscores are removed.

 * Examples:

    @Parameter
    public String first_name = "grant";
    @Parameter(label = "Your last name", columns = 15)
    public String last_name = "harris";
    @Parameter(label = "An Integer", required = true)
    public Integer n = 1;
    @Parameter(label = "An int", persist = "example.total")
    public int total = 9;
    @Parameter(label = "A short")
    public short count = 9;
    @Parameter(label = "A float", widget = "slider")
    public float f = 50.0f;
    @Parameter(label = "A Double", widget = "slider")
    public Double x = 50.0;

    @Parameter(label = "A double", digits = 3, columns = 5, units = "microns")
    public double y = 101.2;

    @Parameter
    public boolean yesOrNo = false;
    @Parameter
    public IjxImagePlus impIn;
    //
    @Parameter(output = true)
    public IjxImagePlus impOut;
    @Parameter(output = true)
    public int outputValue = 9;
 *
 */
package imagej.plugin;

