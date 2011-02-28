package imagej.plugin.api;

import java.net.URL;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public abstract class SezpozEntry<T> implements Comparable<SezpozEntry<?>> {

	/** Fully qualified class name of this entry's object. */
	private String className;

	/** Unique name of the object. */
	private String name;

	/** Human-readable label for describing the object. */
	private String label;

	/** String describing the object in detail. */
	private String description;

	/** Resource path to this plugin's icon. */
	private String iconPath;

	/** Sort priority of the plugin. */
	private int priority = Integer.MAX_VALUE;

	/** Class object for this entry's plugin. Lazily loaded. */
	private Class<T> classObject;

	public void setClassName(final String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
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

	// TODO - consider using a different exception type than "PluginException"

	/** Gets the class object for this entry, loading it as needed. */
	@SuppressWarnings("unchecked")
	public Class<T> loadClass() throws PluginException {
		if (classObject == null) {
			final Class<?> c;
			try {
				c = Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				throw new PluginException("Class not found: " + className, e);
			}
			classObject = (Class<T>) c;
		}
		return classObject;
	}

	/** Creates an instance of this entry's associated plugin. */
	public T createInstance() throws PluginException {
		final Class<T> c = loadClass();

		// instantiate plugin
		final T instance;
		try {
			instance = c.newInstance();
		}
		catch (InstantiationException e) {
			throw new PluginException(e);
		}
		catch (IllegalAccessException e) {
			throw new PluginException(e);
		}
		return instance;
	}

	public URL getIconURL() throws PluginException {
		if (iconPath.isEmpty()) return null;
		return loadClass().getResource(iconPath);
	}

	@Override
	public int compareTo(final SezpozEntry<?> entry) {
		return priority - entry.priority;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		sb.append(className);
		boolean firstField = true;

		if (name != null && !name.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("name = " + name);
		}

		if (label != null && !label.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("label = " + label);
		}

		if (description != null && !description.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("description = " + description);
		}

		if (iconPath != null && !iconPath.isEmpty()) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("iconPath = " + iconPath);
		}

		if (priority < Integer.MAX_VALUE) {
			if (firstField) {
				sb.append(" [");
				firstField = false;
			}
			else sb.append("; ");
			sb.append("priority = " + priority);
		}

		return sb.toString();
	}

}
