package imagej.plugin.gui;

import imagej.plugin.api.MenuEntry;
import imagej.plugin.api.PluginEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class ShadowMenu implements Comparable<ShadowMenu> {

	private PluginEntry<?> pluginEntry;

	private int menuDepth;

	private Map<String, ShadowMenu> children;

	/** Constructs an empty root menu node. */
	public ShadowMenu() {
		this(null, -1);
	}

	/** Constructs a root menu node populated with the given plugin entries. */
	public ShadowMenu(final List<PluginEntry<?>> entries) {
		this();
		for (final PluginEntry<?> entry : entries) addEntry(entry);
	}

	private ShadowMenu(final PluginEntry<?> pluginEntry, final int menuDepth) {
		this.pluginEntry = pluginEntry;
		this.menuDepth = menuDepth;
		children = new HashMap<String, ShadowMenu>();
	}

	public PluginEntry<?> getPluginEntry() {
		return pluginEntry;
	}

	public int getMenuDepth() {
		return menuDepth;
	}

	public Map<String, ShadowMenu> getChildren() {
		return children;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public void addEntry(final PluginEntry<?> entry) {
		addChild(entry, 0);
	}

	public MenuEntry getMenuEntry() {
		return pluginEntry.getMenuPath().get(menuDepth);
	}

	@Override
	public int compareTo(ShadowMenu c) {
		final double w1 = getMenuEntry().getWeight();
		final double w2 = c.getMenuEntry().getWeight();
		if (w1 < w2) return -1;
		if (w1 > w2) return 1;
		// if weights are equal, sort alphabetically
		final String n1 = getMenuEntry().getName();
		final String n2 = c.getMenuEntry().getName();
		return n1.compareTo(n2);
	}

	// -- Helper methods --

	private ShadowMenu getChild(final MenuEntry menuEntry) {
		return children.get(menuEntry.getName());
	}

	private void addChild(final PluginEntry<?> entry, final int depth) {
		// retrieve existing child
		final MenuEntry menuEntry = entry.getMenuPath().get(depth);
		final ShadowMenu existingChild = getChild(menuEntry);

		final ShadowMenu childMenu;
		if (existingChild == null) {
			// create new child and add to table
			final String menuName = menuEntry.getName();
			final ShadowMenu newChild = new ShadowMenu(entry, depth);
			children.put(menuName, newChild);
			childMenu = newChild;
		}
		else {
			// fill in any missing menu properties of existing child
			final MenuEntry childMenuEntry = existingChild.getMenuEntry();
			childMenuEntry.assignProperties(menuEntry);
			childMenu = existingChild;
		}

		// recursively add remaining child menus
		if (depth + 1 < entry.getMenuPath().size()) {
			childMenu.addChild(entry, depth + 1);
		}
	}

}
