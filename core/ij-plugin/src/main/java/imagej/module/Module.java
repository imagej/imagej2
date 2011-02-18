package imagej.module;

import java.util.Map;

/**
 * A Module is an encapsulated piece of functionality with inputs and outputs.
 * 
 * There are several types of modules, including plugins (see
 * {@link PluginModule}) and scripts (see {@link ScriptModule}),
 * as well as {@link Workflow}s, which are directed acyclic graphs
 * consisting of modules whose inputs and outputs are connected.
 *
 * The Module interface represents a specific instance of a module,
 * while the corresponding {@link ModuleInfo} represents metadata
 * about that module, particularly its input and output names and types.
 */
public interface Module {

	/** Gets metadata about this module. */
	ModuleInfo getInfo();

	/** Gets the value of the input with the given name. */
	Object getInput(String name);

	/** Gets the value of the output with the given name. */
	Object getOutput(String name);

	/** Gets a table of input values. */
	Map<String, Object> getInputs();

	/** Gets a table of output values. */
	Map<String, Object> getOutputs();

	/** Sets the value of the input with the given name. */
	void setInput(String name, Object value);

	/** Sets the value of the output with the given name. */
	void setOutput(String name, Object value);

	/** Sets input values according to the given table. */
	void setInputs(Map<String, Object> inputs);

	/** Sets output values according to the given table. */
	void setOutputs(Map<String, Object> outputs);

}
