package imagej.plugin;

import imagej.module.Module;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Module class for working with a {@link BasePlugin} instance,
 * particularly its {@link Parameter}s.
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Johannes Schindelin johannes.schindelin at gmx.de
 * @author Grant Harris gharris at mbl.edu
 */
public class PluginModule<T extends BasePlugin> implements Module {

	/** Metadata about this plugin. */
	private PluginModuleInfo<T> info;

	/** The plugin instance handled by this module. */
	private final T plugin;

	/** Creates a plugin module for a new instance of the given plugin entry. */
	public PluginModule(final PluginEntry<T> entry) throws PluginException {
		this.info = new PluginModuleInfo<T>(entry);
		this.plugin = entry.createInstance();
		setInputs(info.getPluginEntry().getPresets());
	}

	/** Gets the plugin instance handled by this module. */
	public T getPlugin() {
		return plugin;
	}

	public Object getValue(final Field field) {
		try {
			return field.get(plugin);
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot access field: " + field, e);
		}
	}

	// -- Module methods --

	@Override
	public PluginModuleInfo<T> getInfo() {
		return info;
	}

	@Override
	public Object getValue(final String name) {
		final Field field = info.getField(name);
		if (field == null) return null;
		return getValue(field);
	}

	@Override
	public Map<String, Object> getInputs() {
		return getMap(info.getInputFields());
	}

	@Override
	public Map<String, Object> getOutputs() {
		return getMap(info.getOutputFields());
	}

	@Override
	public void setInput(final String name, final Object value) {
		try {
			final Field field = plugin.getClass().getDeclaredField(name);
			field.setAccessible(true); // expose non-public fields
			final Parameter annotation = field.getAnnotation(Parameter.class);
			if (annotation == null) {
				throw new IllegalArgumentException("field \'" +
					name + "\' is not a plugin parameter");
			}
			if (annotation.output()) {
				throw new IllegalArgumentException("field \'" +
					name + "\' is an output field");
			}
			field.set(plugin, value);
		}
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Invalid name: " + name);
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Field is not accessible: " + name);
		}
	}

	@Override
	public void setInputs(final Map<String, Object> inputs) {
		for (final String name : inputs.keySet()) {
			setInput(name, inputs.get(name));
		}
	}

	// -- Helper methods --

	private Map<String, Object> getMap(final Iterable<Field> fields) {
		final Map<String, Object> result = new HashMap<String, Object>();
		for (final Field field : fields) {
			final String name = field.getName();
			try {
				result.put(name, field.get(plugin));
			}
			catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Cannot access field: " + name, e);
			}
		}
		return result;
	}

}
