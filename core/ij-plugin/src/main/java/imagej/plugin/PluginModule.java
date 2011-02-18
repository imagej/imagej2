package imagej.plugin;

import imagej.module.Module;
import imagej.module.ModuleItem;
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

	/** The plugin instance handled by this module. */
	private final T plugin;

	/** Metadata about this plugin. */
	private PluginModuleInfo<T> info;

	/** Creates a plugin module for a new instance of the given plugin entry. */
	public PluginModule(final PluginEntry<T> entry) throws PluginException {
		plugin = entry.createInstance();
		info = new PluginModuleInfo<T>(entry, plugin);
	}

	/** Gets the plugin instance handled by this module. */
	public T getPlugin() {
		return plugin;
	}

	// -- Module methods --

	@Override
	public PluginModuleInfo<T> getInfo() {
		return info;
	}

	@Override
	public Object getInput(final String name) {
		final PluginModuleItem item = info.getInput(name);
		return getValue(item.getField(), plugin);
	}

	@Override
	public Object getOutput(final String name) {
		final PluginModuleItem item = info.getOutput(name);
		return getValue(item.getField(), plugin);
	}

	@Override
	public Map<String, Object> getInputs() {
		return getMap(info.inputs());
	}

	@Override
	public Map<String, Object> getOutputs() {
		return getMap(info.outputs());
	}

	@Override
	public void setInput(final String name, final Object value) {
		final PluginModuleItem item = info.getInput(name);
		setValue(item.getField(), plugin, value);
	}

	@Override
	public void setOutput(final String name, final Object value) {
		final PluginModuleItem item = info.getOutput(name);
		setValue(item.getField(), plugin, value);
	}

	@Override
	public void setInputs(final Map<String, Object> inputs) {
		for (final String name : inputs.keySet()) {
			setInput(name, inputs.get(name));
		}
	}

	@Override
	public void setOutputs(final Map<String, Object> outputs) {
		for (final String name : outputs.keySet()) {
			setOutput(name, outputs.get(name));
		}
	}

	// -- Helper methods --

	private Map<String, Object> getMap(final Iterable<ModuleItem> items) {
		final Map<String, Object> map = new HashMap<String, Object>();
		for (final ModuleItem item : items) {
			final PluginModuleItem pmi = (PluginModuleItem) item;
			final String name = item.getName();
			final Object value = getValue(pmi.getField(), plugin);
			map.put(name, value);
		}
		return map;
	}

	// -- Utility methods --

	public static void setValue(final Field field,
		final Object instance, final Object value)
	{
		if (instance == null) return;
		try {
			field.set(instance, value);
		}
		catch (IllegalArgumentException e) {
			assert false;
		}
		catch (IllegalAccessException e) {
			assert false;
		}
	}

	public static Object getValue(final Field field, final Object instance) {
		if (instance == null) return null;
		try {
			return field.get(instance);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
		catch (IllegalAccessException e) {
			return null;
		}
	}

}
