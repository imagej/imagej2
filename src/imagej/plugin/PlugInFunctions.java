package imagej.plugin;

import ij.ImagePlus;

import ij.gui.GenericDialog;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 */
public class PlugInFunctions {
	public static Map<String, Object> run(Runnable plugin,
			Object... parameters) throws PlugInException {
		if ((parameters.length % 2) != 0)
			throw new IllegalArgumentException("incomplete key/value pair");
		Class clazz = plugin.getClass();
		for (int i = 0; i < parameters.length; i += 2)
			setParameter(plugin,
				(String)parameters[i], parameters[i + 1]);
		plugin.run();
		return getOutputMap(plugin);
	}

	public static void setParameter(Runnable plugin,
			String key, Object value) {
		try {
			Class clazz = plugin.getClass();
			Field field = clazz.getField(key);
			Parameter annotation =
				field.getAnnotation(Parameter.class);
			if (annotation == null)
				throw new IllegalArgumentException("field '"
					+ key + "' is not a plugin parameter");
			if (annotation.isOutput())
				throw new IllegalArgumentException("field '"
					+ key + "' is an output field");
			field.set(plugin, value);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Invalid key: " + key);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Field is not public: " + key);
		}
	}

	public static Map<String, Object> getOutputMap(Runnable plugin) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Field field : getOutputParameters(plugin)) try {
			result.put(field.getName(), field.get(plugin));
		} catch (Exception e) { e.printStackTrace(); }
		return result;
	}

	public static void runInteractively(Runnable plugin) {
		if (!showDialog(plugin))
			return;
		plugin.run();
		for (ImagePlus image : PlugInFunctions.getOutputImages(plugin))
			image.show();
	}

	public static String getLabel(Field field) {
		Parameter parameter = field.getAnnotation(Parameter.class);
		if (parameter != null) {
			String label = parameter.label();
			if (label != null && !label.equals(""))
				return label;
		}
		return field.getName();
	}

	public static Object getDefault(Field field) {
		// TODO
		return "";
	}

	public static boolean showDialog(Runnable plugin) {
		// TODO: Should plugin have a getName() method, defaulting
		// to the class name?
		GenericDialog dialog = new GenericDialog("Parameters");
		for (Field field : getInputParameters(plugin)) {
			if (field.getType() == String.class)
				dialog.addStringField(getLabel(field),
						(String)getDefault(field));
			else
				throw new RuntimeException("TODO!");
		}
		dialog.showDialog();
		if (dialog.wasCanceled())
			return false;
		for (Field field : getInputParameters(plugin)) try {
			if (field.getType() == String.class)
				field.set(plugin, dialog.getNextString());
			else
				throw new RuntimeException("TODO!");
		} catch (Exception e) { e.printStackTrace(); }
		return true;
	}

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
			return !parameter.isOutput();
		}
	};

	protected final static ParameterFilter outputs = new ParameterFilter() {
		public boolean matches(Parameter parameter) {
			return parameter.isOutput();
		}
	};

	protected static class ParameterIterable implements Iterable<Field> {
		Field[] fields;
		ParameterFilter filter;

		ParameterIterable(Field[] fields, ParameterFilter filter) {
			this.fields = fields;
			this.filter = filter;
		}

		ParameterIterable(Runnable plugin,
				ParameterFilter filter) {
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

		ParameterIterator(Field[] fields, ParameterFilter filter) {
			this.fields = fields;
			this.filter = filter;
			counter = -1;
			findNext();
		}

		void findNext() {
			while (++counter < fields.length) {
				Parameter parameter = fields[counter]
					.getAnnotation(Parameter.class);
				if (parameter == null)
					continue;
				if (filter.matches(parameter))
					return;
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

	public static Iterable<Field> getParameters(Runnable plugin,
			ParameterFilter filter) {
		return new ParameterIterable(plugin, filter);
	}

	public static Iterable<Field> getParameters(Runnable plugin) {
		return getParameters(plugin, all);
	}

	public static Iterable<Field> getInputParameters(Runnable plugin) {
		return getParameters(plugin, inputs);
	}

	public static Iterable<Field> getOutputParameters(Runnable plugin) {
		return getParameters(plugin, outputs);
	}

	public static Iterable<ImagePlus> getOutputImages(Runnable plugin) {
		List<ImagePlus> result = new ArrayList<ImagePlus>();
		for (Field field : getOutputParameters(plugin)) {
			if (field.getType() == ImagePlus.class) try {
				result.add((ImagePlus)field.get(plugin));
			} catch (Exception e) { e.printStackTrace(); }
		}
		return result;
	}
}
