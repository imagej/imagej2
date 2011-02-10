package imagej.plugin.api;

import imagej.plugin.IPlugin;
import imagej.plugin.PluginHandler;
import imagej.plugin.spi.PluginHandlerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginEntry {

	private String pluginClass;
	private List<MenuEntry> menuPath;
	private Map<String, Object> presets;
	private PluginHandlerFactory factory;

	public PluginEntry(final String pluginClass) {
		this(pluginClass, null, null);
	}

	public PluginEntry(final String pluginClass, final List<MenuEntry> menuPath) {
		this(pluginClass, menuPath, null);
	}

	public PluginEntry(final String pluginClass, final List<MenuEntry> menuPath,
		final Map<String, Object> presets)
	{
		this(pluginClass, menuPath, presets, null);
	}

	public PluginEntry(final String pluginClass, final List<MenuEntry> menuPath,
			final Map<String, Object> presets, PluginHandlerFactory factory)
	{
		setPluginClass(pluginClass);
		setMenuPath(menuPath);
		setPresets(presets);
		setPluginHandlerFactory(factory);
	}

	public void setPluginClass(final String pluginClass) {
		this.pluginClass = pluginClass;
	}

	public String getPluginClass() {
		return pluginClass;
	}

	public void setMenuPath(final List<MenuEntry> menuPath) {
		if (menuPath == null) {
			this.menuPath = new ArrayList<MenuEntry>();
		}
		else {
			this.menuPath = menuPath;
		}
	}

	public List<MenuEntry> getMenuPath() {
		return menuPath;
	}

	public void setPresets(final Map<String, Object> presets) {
		if (presets == null) {
			this.presets = new HashMap<String, Object>();
		}
		else {
			this.presets = presets;
		}
	}

	public Map<String, Object> getPresets() {
		return presets;
	}

	public void setPluginHandlerFactory(PluginHandlerFactory factory) {
		if (factory == null) {
			this.factory = new DefaultPluginHandlerFactory();
		}
		else {
			this.factory = factory;
		}
	}

	public PluginHandlerFactory getPluginHandlerFactory() {
		return factory;
	}

	public PluginHandler createPluginHandler() throws PluginException {
		return factory.createPluginHandler(createInstance());
	}

	public IPlugin createInstance()
		throws PluginException
	{
		// get Class object for plugin entry
		final Class<?> c;
		try {
			c = Class.forName(getPluginClass());
		}
		catch (ClassNotFoundException e) {
			throw new PluginException(e);
		}
		if (!IPlugin.class.isAssignableFrom(c)) {
			throw new PluginException("Not a plugin");
		}
	
		// instantiate plugin
		final Object pluginInstance;
		try {
			pluginInstance = c.newInstance();
		}
		catch (InstantiationException e) {
			throw new PluginException(e);
		}
		catch (IllegalAccessException e) {
			throw new PluginException(e);
		}
		if (!(pluginInstance instanceof IPlugin)) {
			throw new PluginException("Not a plugin");
		}
		final IPlugin plugin = (IPlugin) pluginInstance;
	
		return plugin;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(pluginClass);
		sb.append(" [");
		boolean firstField = true;

		if (!menuPath.isEmpty()) {
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append("menu = ");
			boolean firstMenu = true;
			for (final MenuEntry menu : menuPath) {
				if (firstMenu) firstMenu = false;
				else sb.append(" > ");
				sb.append(menu);
			}
			sb.append("]");
		}

		for (final String key : presets.keySet()) {
			final Object value = presets.get(key); 
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append(key + " = '" + value + "'");
		}

		return sb.toString();
	}

}
