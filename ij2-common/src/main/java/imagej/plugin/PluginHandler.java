package imagej.plugin;

import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for working with a {@link IPlugin} instance,
 * particularly its {@link Parameter}s.
 *
 * @author Johannes Schindelin johannes.schindelin at gmx.de
 * @author Grant Harris gharris at mbl.edu
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class PluginHandler {

	/** The plugin instance handled by this parameter handler. */
	private final IPlugin plugin;

	/**
	 * Table of preset values.
	 *
	 * Fields in this table are excluded from the list of input parameters.
	 */
	private final Map<String, Object> presets;

	/** Creates a plugin handler for a new instance of the given plugin entry. */
	public PluginHandler(final PluginEntry entry) throws PluginException {
		this(entry.createInstance(), entry.getPresets());
	}

	/** Creates a plugin handler for the given plugin. */
	public PluginHandler(final IPlugin plugin) {
		this(plugin, new HashMap<String, Object>());
	}

	public PluginHandler(final IPlugin plugin,
		final Map<String, Object> presets)
	{
		this.plugin = plugin;
		this.presets = presets;
		setValues(presets);
	}

	public IPlugin getPlugin() {
		return plugin;
	}

	public Map<String, Object> getPresets() {
		return presets;
	}

	public Parameter get(final Field field) {
		return field.getAnnotation(Parameter.class);
	}

	public Object getValue(final Field field) {
		try {
			return field.get(plugin);
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot access field: " + field, e);
		}
	}

	public Object getValue(final String name) {
		final Field field = getField(name);
		if (field == null) return null;
		return getValue(field);
	}

	public void setValue(final String key, final Object value) {
		try {
			final Field field = plugin.getClass().getDeclaredField(key);
			field.setAccessible(true); // expose non-public fields
			final Parameter annotation = field.getAnnotation(Parameter.class);
			if (annotation == null) {
				throw new IllegalArgumentException("field \'" +
					key + "\' is not a plugin parameter");
			}
			if (annotation.output()) {
				throw new IllegalArgumentException("field \'" +
					key + "\' is an output field");
			}
			field.set(plugin, value);
		}
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Invalid key: " + key);
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Field is not accessible: " + key);
		}
	}

	public void setValues(final Map<String, Object> inputMap) {
		for (final String key : inputMap.keySet()) {
			setValue(key, inputMap.get(key));
		}
	}

	public Iterable<Field> getFields() {
		return getFields(PluginHandler.ALL);
	}

	public Iterable<Field> getInputFields() {
		return getFields(inputFilter(presets.keySet()));
	}

	public Iterable<Field> getOutputFields() {
		return getFields(PluginHandler.OUTPUTS);
	}

	public Map<String, Object> getInputMap() {
		return getMap(getInputFields());
	}

	public Map<String, Object> getOutputMap() {
		return getMap(getOutputFields());
	}

	// -- Helper methods --

	private Field getField(final String name) {
		try {
			final Field field = plugin.getClass().getDeclaredField(name);
			field.setAccessible(true); // expose non-public fields
			return field;
		}
		catch (SecurityException e) {
			throw new IllegalArgumentException("Cannot access field: " + name, e);
		}
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("No such field: " + name, e);
		}
	}

	private Iterable<Field> getFields(final ParameterFilter filter) {
		return new ParameterIterable(plugin, filter);
	}

	private Map<String, Object> getMap(final Iterable<Field> fields) {
		final Map<String, Object> result = new HashMap<String, Object>();
		for (Field field : fields) {
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

	// -- Parameter filtering --

	private interface ParameterFilter {
		public boolean matches(Field field, Parameter parameter);
	}

	private static final ParameterFilter ALL = new ParameterFilter() {
		@Override
		public boolean matches(Field field, Parameter parameter) {
			return true;
		}
	};

	private static final ParameterFilter OUTPUTS = new ParameterFilter() {
		@Override
		public boolean matches(Field field, Parameter parameter) {
			return parameter.output();
		}
	};

	private static ParameterFilter inputFilter(final Set<String> excluded) {
		return new ParameterFilter() {
			@Override
			public boolean matches(Field field, Parameter parameter) {
				return !parameter.output() && !excluded.contains(field.getName());
			}
		};
	}

	// -- Parameter iteration --

	private static class ParameterIterable implements Iterable<Field> {
		private Field[] fields;
		private ParameterFilter filter;

		ParameterIterable(Field[] fields, ParameterFilter filter) {
			this.fields = fields;
			this.filter = filter;
			AccessibleObject.setAccessible(fields, true); // expose non-public fields
		}

		ParameterIterable(IPlugin plugin, ParameterFilter filter) {
			this(plugin.getClass().getDeclaredFields(), filter);
		}

		@Override
		public Iterator<Field> iterator() {
			return new ParameterIterator(fields, filter);
		}
	}

	private static class ParameterIterator implements Iterator<Field> {
		private int counter;
		private Field[] fields;
		private ParameterFilter filter;

		ParameterIterator(Field[] fields, ParameterFilter filter) {
			this.fields = fields;

			this.filter = filter;
			counter = -1;
			findNext();
		}

		private void findNext() {
			while (++counter < fields.length) {
				Parameter parameter = fields[counter].getAnnotation(Parameter.class);
				if (parameter == null) continue;
				if (filter.matches(fields[counter], parameter)) return;
			}
		}

		@Override
		public boolean hasNext() {
			return counter < fields.length;
		}

		@Override
		public Field next() {
			Field result = fields[counter];
			findNext();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
