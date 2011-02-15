package imagej.plugin;

import imagej.module.ModuleInfo;
import imagej.module.ModuleItem;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link ModuleInfo} class for querying metadata of a {@link BasePlugin}.
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Johannes Schindelin johannes.schindelin at gmx.de
 * @author Grant Harris gharris at mbl.edu
 */
public class PluginModuleInfo<T extends BasePlugin> implements ModuleInfo {

	/** The plugin entry associated with this module info. */
	private final PluginEntry<T> pluginEntry;

	/** Class object of this plugin. */
	private final Class<?> pluginClass;

	/** Creates module info for the given plugin entry. 
	 * @throws PluginException */
	public PluginModuleInfo(final PluginEntry<T> pluginEntry)
		throws PluginException
	{
		this.pluginEntry = pluginEntry;
		pluginClass = pluginEntry.getPluginClass();
	}

	public PluginEntry<T> getPluginEntry() {
		return pluginEntry;
	}

	public Field getField(final String name) {
		try {
			final Field field = pluginClass.getDeclaredField(name);
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

	public Iterable<Field> getFields() {
		return getFields(PluginModuleInfo.ALL);
	}

	public Iterable<Field> getInputFields() {
		return getFields(inputFilter(pluginEntry.getPresets().keySet()));
	}

	public Iterable<Field> getOutputFields() {
		return getFields(PluginModuleInfo.OUTPUTS);
	}

	@Deprecated
	public Parameter getParameter(final Field field) {
		return field.getAnnotation(Parameter.class);
	}

	// -- ModuleInfo methods --

	@Override
	public String getName() {
		return pluginEntry.getName();
	}

	@Override
	public String getLabel() {
		return pluginEntry.getLabel();
	}

	@Override
	public String getDescription() {
		return pluginEntry.getDescription();
	}

	@Override
	public Iterable<ModuleItem> inputs() {
		// TODO - avoid recomputing this list with every call?
		final ArrayList<ModuleItem> inputs = new ArrayList<ModuleItem>();
		for (final Field f: getInputFields()) {
			final ModuleItem item = new PluginModuleItem(f);
			inputs.add(item);
		}
		return inputs;
	}

	@Override
	public Iterable<ModuleItem> outputs() {
		// TODO - avoid recomputing this list with every call?
		final ArrayList<ModuleItem> outputs = new ArrayList<ModuleItem>();
		for (final Field f: getOutputFields()) {
			final ModuleItem item = new PluginModuleItem(f);
			outputs.add(item);
		}
		return outputs;
	}

	// -- Helper methods --

	private Iterable<Field> getFields(final ParameterFilter filter) {
		final Field[] fields = pluginClass.getDeclaredFields();
		return new ParameterIterable(fields, filter);
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

		ParameterIterable(final Field[] fields, final ParameterFilter filter) {
			this.fields = fields;
			this.filter = filter;
			AccessibleObject.setAccessible(fields, true); // expose non-public fields
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
