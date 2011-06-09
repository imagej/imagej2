//
// BaseEntry.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.plugin;

import java.net.URL;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public abstract class BaseEntry<T> implements Comparable<BaseEntry<?>> {

	/** Fully qualified class name of this entry's object. */
	private String className;

	/** Unique name of the object. */
	private String name;

	/** Human-readable label for describing the object. */
	private String label;

	/** String describing the object in detail. */
	private String description;

	/** Resource path to this object's icon. */
	private String iconPath;

	/** Sort priority of the object. */
	private int priority = Integer.MAX_VALUE;

	/** Whether the entry is enabled in the user interface. */
	private boolean enabled = true;

	/** Class object for this entry's object. Lazily loaded. */
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

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
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

	/** Creates an instance of this entry's associated object. */
	public T createInstance() throws PluginException {
		final Class<T> c = loadClass();

		// instantiate object
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
	public int compareTo(final BaseEntry<?> entry) {
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
