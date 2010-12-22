package imagej.plugin.ij2;

import imagej.plugin.PluginException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Johannes Schindelin johannes.schindelin at gmx.de
 * @author Grant Harris gharris at mbl.edu
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public final class ParameterHandler {

	private static final boolean DEBUG = false;

	private ParameterHandler() {
		// forbid instantiation of utility class
	}

	public static Iterable<Field> getInputParameters(IPlugin plugin) {
		return getParameters(plugin, ParameterHandler.inputs);
	}

	public static void setParameter(IPlugin plugin, String key, Object value) {
		try {
			Class<?> clazz = plugin.getClass();
			Field field = clazz.getField(key);
			Parameter annotation = field.getAnnotation(Parameter.class);
			if (annotation == null) {
				throw new IllegalArgumentException("field \'" + key + "\' is not a plugin parameter");
			}
			if (annotation.output()) {
				throw new IllegalArgumentException("field \'" + key + "\' is an output field");
			}
			field.set(plugin, value);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Invalid key: " + key);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Field is not public: " + key);
		}
	}

	public static Iterable<Field> getOutputParameters(IPlugin plugin) {
		return getParameters(plugin, ParameterHandler.outputs);
	}

	public static Iterable<Field> getParameters(IPlugin plugin, ParameterFilter filter) {
		return new ParameterIterable(plugin, filter);
	}

	public static Iterable<Field> getParameters(IPlugin plugin) {
		return getParameters(plugin, ParameterHandler.all);
	}

	public static Map<String, Object> getInputMap(IPlugin plugin)
		throws PluginException
	{
		return createMap(getInputParameters(plugin), plugin);
	}

	public static Map<String, Object> getOutputMap(IPlugin plugin)
		throws PluginException
	{
		return createMap(getOutputParameters(plugin), plugin);
	}

	private static Map<String, Object> createMap(Iterable<Field> fields,
		Object instance) throws PluginException
	{
		final Map<String, Object> result = new HashMap<String, Object>();
		for (Field field : fields) {
			try {
				result.put(field.getName(), field.get(instance));
			}
			catch (IllegalArgumentException e) {
				throw new PluginException(e);
			}
			catch (IllegalAccessException e) {
				throw new PluginException(e);
			}
		}
		return result;		
	}
	
	// Parameter Filters -----------------------------------------------------------------
	public interface ParameterFilter {

		public boolean matches(Parameter parameter);
	}

	protected final static ParameterFilter all = new ParameterFilter() {

		public boolean matches(Parameter parameter) {
			return true;
		}
	};

	protected final static ParameterFilter inputs = new ParameterFilter() {

		public boolean matches(Parameter parameter) {
			return !parameter.output();
		}
	};

	protected final static ParameterFilter outputs = new ParameterFilter() {

		public boolean matches(Parameter parameter) {
			return parameter.output();
		}
	};

	protected static class ParameterIterable implements Iterable<Field> {

		Field[] fields;
		ParameterFilter filter;

		ParameterIterable(Field[] fields, ParameterFilter filter) {
			this.fields = fields;
			this.filter = filter;
		}

		ParameterIterable(IPlugin plugin, ParameterFilter filter) {
			this(plugin.getClass().getFields(), filter);
		}

		public Iterator<Field> iterator() {
			return new ParameterIterator(fields, filter);
		}
	}

	protected static class ParameterIterator implements Iterator<Field> {
		int counter;
		Field[] fields;
		ParameterFilter filter;

		ParameterIterator( Field[] fields, ParameterFilter filter ) {
			if (DEBUG) System.out.println("There are " + fields.length + " fields.");
			this.fields = fields;

			this.filter = filter;
			counter = -1;
			findNext();
		}

		void findNext() {
			while (++counter < fields.length) {
				Parameter parameter = fields[counter].getAnnotation(Parameter.class);
				if (parameter == null) {
					continue;
				}
				if (filter.matches(parameter)) {
					return;
				}
			}
		}

		public boolean hasNext() {
			return counter < fields.length;
		}

		public Field next() {
			Field result = fields[counter];
			findNext();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
