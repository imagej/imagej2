package imagej.module;

import java.util.Map;

/**
 * A Module is an encapsulated piece of functionality.
 * 
 * There are several types of modules, including plugins (e.g.,
 * {@link ImageJPlugin}s) and scripts (see {@link ScriptModule}),
 * as well as {@link Workflow}s, which are direct acyclic graphs 
 * 
 */
public interface Module {

	/** Gets metadata about this module. */
	ModuleInfo getInfo();

	/** Gets the value of the item with the given name. */
	Object getValue(String name);

	/** Gets a table of input values. */
	Map<String, Object> getInputs();

	/** Gets a table of output values. */
	Map<String, Object> getOutputs();

	/** Sets the value of the input with the given name. */
	void setInput(String name, Object value);

	/** Sets input values according to the given table. */
	void setInputs(Map<String, Object> inputs);

}
