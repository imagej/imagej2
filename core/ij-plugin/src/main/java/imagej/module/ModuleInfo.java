package imagej.module;

/**
 * A ModuleInfo object encapsulates metadata about a particular {@link Module}
 * (but not a specific instance of it). In particular, it can report details
 * on the names and types of inputs and outputs.
 */
public interface ModuleInfo {

	/** Unique name of this module. */
	String getName();

	/** Name to appear in a UI, if applicable. */
	String getLabel();

	/** Gets a string describing this module. */
	String getDescription();

	/** Gets the list of input items. */
	Iterable<ModuleItem> inputs();

	/** Gets the list of output items. */
	Iterable<ModuleItem> outputs();

}
