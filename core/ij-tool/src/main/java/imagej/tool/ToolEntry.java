package imagej.tool;

import imagej.plugin.api.BaseEntry;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class ToolEntry extends BaseEntry<ITool> {

	public ToolEntry(final String className) {
		setClassName(className);
	}

}
