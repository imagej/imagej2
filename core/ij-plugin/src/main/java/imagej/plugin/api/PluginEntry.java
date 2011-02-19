package imagej.plugin.api;

import imagej.plugin.BasePlugin;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.PluginModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginEntry<T extends BasePlugin>
	implements Comparable<PluginEntry<?>>
{

	/** Fully qualified class name of this entry's plugin. */
	private String pluginClassName;

	/** Type of this entry's plugin; e.g., {@link ImageJPlugin}. */
	private Class<T> pluginType;

	/** Unique name of the plugin. */
	private String name;

	/** Human-readable label for describing the plugin. */
	private String label;

	/** String describing the plugin in detail. */
	private String description;

	/** Path to this plugin's suggested position in the menu structure. */
	private List<MenuEntry> menuPath;

	/** Resource path to this plugin's icon. */
	private String iconPath;

	/** Sort priority of the plugin. */
	private int priority = Integer.MAX_VALUE;

	/** List of inputs with fixed, preset values. */
	private Map<String, Object> presets;

	/** Factory used to create a module associated with this entry's plugin. */
	private PluginModuleFactory<T> factory;

	/** Class object for this entry's plugin. Lazily loaded. */
	private Class<?> pluginClass;

	public PluginEntry(final String pluginClassName, final Class<T> pluginType) {
		setPluginClassName(pluginClassName);
		setPluginType(pluginType);
		setMenuPath(null);
		setPresets(null);
		setPluginModuleFactory(null);
	}

	public void setPluginClassName(final String pluginClassName) {
		this.pluginClassName = pluginClassName;
	}

	public String getPluginClassName() {
		return pluginClassName;
	}

	public void setPluginType(Class<T> pluginType) {
		this.pluginType = pluginType;
	}
	
	public Class<T> getPluginType() {
		return pluginType;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
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

	public void setIconPath(final String iconPath) {
		this.iconPath = iconPath;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setPriority(final int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
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

	public void setPluginModuleFactory(final PluginModuleFactory<T> factory) {
		if (factory == null) {
			this.factory = new DefaultPluginModuleFactory<T>();
		}
		else {
			this.factory = factory;
		}
	}

	public PluginModuleFactory<T> getPluginModuleFactory() {
		return factory;
	}

	/**
	 * Creates a module to work with this entry,
	 * using the entry's associated {@link PluginModuleFactory}.
	 */
	public PluginModule<T> createModule() throws PluginException {
		return factory.createModule(this);
	}

	/** Gets the class object for this entry's plugin, loading it as needed. */
	public Class<?> getPluginClass() throws PluginException {
		if (pluginClass == null) {
			final Class<?> c;
			try {
				c = Class.forName(pluginClassName);
			}
			catch (ClassNotFoundException e) {
				throw new PluginException("Class not found: " + pluginClassName, e);
			}
			if (!pluginType.isAssignableFrom(c)) {
				throw new PluginException("Not a plugin: " + pluginClassName);
			}
			pluginClass = c;
		}
		return pluginClass;
	}

	/** Creates an instance of this entry's associated plugin. */
	public T createInstance() throws PluginException {
		final Class<?> c = getPluginClass();
	
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
		@SuppressWarnings("unchecked")
		final T plugin = (T) pluginInstance;
		return plugin;
	}

	@Override
	public int compareTo(final PluginEntry<?> entry) {
		return priority - entry.priority;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(pluginClassName);
		sb.append(" [");
		boolean firstField = true;

		if (name != null && !name.isEmpty()) {
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append("name = " + name);
		}

		if (label != null && !label.isEmpty()) {
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append("label = " + label);
		}

		if (description != null && !description.isEmpty()) {
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append("description = " + description);
		}

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
		}

		if (iconPath != null && !iconPath.isEmpty()) {
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append("iconPath = " + iconPath);
		}

		if (priority < Integer.MAX_VALUE) {
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append("priority = " + priority);
		}

		for (final String key : presets.keySet()) {
			final Object value = presets.get(key); 
			if (firstField) firstField = false;
			else sb.append("; ");
			sb.append(key + " = '" + value + "'");
		}

		sb.append("]");

		return sb.toString();
	}

}
