package imagej.plugin.api;

import imagej.plugin.BasePlugin;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.PluginModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class PluginEntry<T extends BasePlugin> extends BaseEntry<T> {

	/** Type of this entry's plugin; e.g., {@link ImageJPlugin}. */
	private Class<T> pluginType;

	/** Path to this plugin's suggested position in the menu structure. */
	private List<MenuEntry> menuPath;

	/** List of inputs with fixed, preset values. */
	private Map<String, Object> presets;

	/** Factory used to create a module associated with this entry's plugin. */
	private PluginModuleFactory<T> factory;

	public PluginEntry(final String className, final Class<T> pluginType) {
		setClassName(className);
		setPluginType(pluginType);
		setMenuPath(null);
		setPresets(null);
		setPluginModuleFactory(null);
	}

	public void setPluginType(Class<T> pluginType) {
		this.pluginType = pluginType;
	}

	public Class<T> getPluginType() {
		return pluginType;
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		final String className = getClassName();
		sb.append(className);
		boolean firstField = true;

		final String name = getName();
		if (name != null && !name.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("name = " + name);
		}

		final String label = getLabel();
		if (label != null && !label.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("label = " + label);
		}

		final String description = getDescription();
		if (description != null && !description.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("description = " + description);
		}

		if (!menuPath.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("menu = ");
			boolean firstMenu = true;
			for (final MenuEntry menu : menuPath) {
				if (firstMenu) firstMenu = false;
				else sb.append(" > ");
				sb.append(menu);
			}
		}

		final String iconPath = getIconPath();
		if (iconPath != null && !iconPath.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("iconPath = " + iconPath);
		}

		final int priority = getPriority();
		if (priority < Integer.MAX_VALUE) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("priority = " + priority);
		}

		for (final String key : presets.keySet()) {
			final Object value = presets.get(key); 
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append(key + " = '" + value + "'");
		}

		if (!firstField) sb.append("]");

		return sb.toString();
	}

}
