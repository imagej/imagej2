package imagej.module;

/** A ModuleItem represents metadata about one input or output of a module. */
public interface ModuleItem {

	/** Unique name of the item. */
	String getName();

	/** Name to appear in a UI, if applicable. */
	String getLabel();

	/** Type of the item. */
	Class<?> getType();

	/** Default value of the item. */
	Object getDefaultValue();

}
