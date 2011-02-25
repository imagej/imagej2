package imagej.module;

/**
 * A ModuleInfo object encapsulates metadata about a particular {@link Module}
 * (but not a specific instance of it). In particular, it can report details
 * on the names and types of inputs and outputs.
 * 
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public interface ModuleInfo {

	/** Unique name of this module. */
	String getName();

	/** Name to appear in a UI, if applicable. */
	String getLabel();

	/** Gets a string describing this module. */
	String getDescription();

	/** Gets the input item with the given name. */
	ModuleItem getInput(String name);

	/** Gets the output item with the given name. */
	ModuleItem getOutput(String name);

	/** Gets the list of input items. */
	Iterable<ModuleItem> inputs();

	/** Gets the list of output items. */
	Iterable<ModuleItem> outputs();

}
