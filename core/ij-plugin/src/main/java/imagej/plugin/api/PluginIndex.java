package imagej.plugin.api;

import imagej.plugin.BasePlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * An efficient index of available plugins.
 *
 * @author Curtis Rueden
 */
public class PluginIndex {

	/** SezPoz index of available {@link BasePlugin}s. */
	private Index<Plugin, BasePlugin> pluginIndex;

	/** ClassLoader to use when querying SezPoz. */
	private ClassLoader classLoader;

	/** Table of plugin lists, organized by plugin type. */
	private HashMap<Class<?>, ArrayList<PluginEntry<?>>> pluginLists =
		new HashMap<Class<?>, ArrayList<PluginEntry<?>>>();

	private PluginIndex(final ClassLoader classLoader) {
		this.classLoader = classLoader;
		reloadPlugins();
	}

	// TODO - decide if singleton pattern is really best here

	private static PluginIndex instance;

	public static PluginIndex getIndex() {
		return getIndex(null);
	}

	public static PluginIndex getIndex(final ClassLoader classLoader) {
		if (instance == null) instance = new PluginIndex(classLoader);
		return instance;
	}

	public void reloadPlugins() {
		if (classLoader == null) {
			pluginIndex = Index.load(Plugin.class, BasePlugin.class);
		}
		else {
			pluginIndex = Index.load(Plugin.class, BasePlugin.class, classLoader);
		}

		// classify plugins into types
		pluginLists.clear();
		for (final IndexItem<Plugin, BasePlugin> item : pluginIndex) {
			final PluginEntry<?> entry = createEntry(item);
			final Class<?> type = item.annotation().type();
			registerType(entry, type);
		}

		// sort plugin lists by priority
		for (final ArrayList<PluginEntry<?>> pluginList : pluginLists.values()) {
			Collections.sort(pluginList);
		}
	}

	/** Gets a copy of the list of plugins labeled with the given type. */
	public <T extends BasePlugin> ArrayList<PluginEntry<T>>
		getPlugins(final Class<T> type)
	{
		// TODO - find a way to avoid making a copy of the list here?
		final ArrayList<PluginEntry<T>> outputList =
			new ArrayList<PluginEntry<T>>();
		final ArrayList<PluginEntry<?>> cachedList = pluginLists.get(type);
		if (cachedList != null) {
			for (PluginEntry<?> entry : cachedList) {
				@SuppressWarnings("unchecked")
				final PluginEntry<T> typedEntry = (PluginEntry<T>) entry;
				outputList.add(typedEntry);
			}
		}
		return outputList;
	}

	private <T extends BasePlugin> PluginEntry<T> createEntry(
		final IndexItem<Plugin, BasePlugin> item)
	{
		final String className = item.className();
		final Plugin plugin = item.annotation();

		@SuppressWarnings("unchecked")
		final Class<T> pluginType = (Class<T>) plugin.type();

		final PluginEntry<T> entry = new PluginEntry<T>(className, pluginType);
		entry.setName(plugin.name());
		entry.setLabel(plugin.label());
		entry.setDescription(plugin.description());
		entry.setIconPath(plugin.iconPath());
		entry.setPriority(plugin.priority());

		final List<MenuEntry> menuPath = new ArrayList<MenuEntry>();
		final Menu[] menu = plugin.menu();
		if (menu.length > 0) {
			parseMenuPath(menuPath, menu);
		}
		else {
			// parse menuPath attribute
			final String path = plugin.menuPath();
			if (!path.isEmpty()) parseMenuPath(menuPath, path);
		}
		entry.setMenuPath(menuPath);

		return entry;
	}

	private void registerType(PluginEntry<?> entry, Class<?> type) {
		ArrayList<PluginEntry<?>> pluginList = pluginLists.get(type);
		if (pluginList == null) {
			pluginList = new ArrayList<PluginEntry<?>>();
			pluginLists.put(type, pluginList);
		}
		pluginList.add(entry);
	}

	private void parseMenuPath(final List<MenuEntry> menuPath,
			final Menu[] menu)
	{
		for (int i = 0; i < menu.length; i++) {
			final String name = menu[i].label();
			final double weight = menu[i].weight();
			final char mnemonic = menu[i].mnemonic();
			final String accelerator = menu[i].accelerator();
			final String icon = menu[i].icon();				
			menuPath.add(new MenuEntry(name, weight, mnemonic, accelerator, icon));
		}
	}

	private void parseMenuPath(final List<MenuEntry> menuPath,
			final String path)
	{
		final String[] menuPathTokens = path.split(">");
		for (String token : menuPathTokens) menuPath.add(new MenuEntry(token));
	}

}
