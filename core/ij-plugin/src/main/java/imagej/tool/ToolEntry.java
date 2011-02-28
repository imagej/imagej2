package imagej.tool;

import imagej.plugin.api.SezpozEntry;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class ToolEntry extends SezpozEntry<ITool> {

	public ToolEntry(final String className) {
		setClassName(className);
	}

}
