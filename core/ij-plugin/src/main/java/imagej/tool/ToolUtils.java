package imagej.tool;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Utility class for discovering and querying tools.
 *
 * @author Curtis Rueden
 */
public final class ToolUtils {

	private ToolUtils() {
		// prohibit instantiation of utility class
	}

	public static List<ToolEntry> findTools() {
		final Index<Tool, ITool> toolIndex = Index.load(Tool.class, ITool.class);
		final List<ToolEntry> tools = new ArrayList<ToolEntry>();
		for (final IndexItem<Tool, ITool> item : toolIndex) {
			tools.add(createEntry(item));
		}
		return tools;
	}

	private static ToolEntry createEntry(final IndexItem<Tool, ITool> item) {
		final String className = item.className();
		final Tool tool = item.annotation();

		final ToolEntry entry = new ToolEntry(className);
		entry.setName(tool.name());
		entry.setLabel(tool.label());
		entry.setDescription(tool.description());
		entry.setIconPath(tool.iconPath());
		entry.setPriority(tool.priority());

		return entry;
	}

}
