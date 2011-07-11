//
// IndexEntry.java
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
 * A collection of metadata corresponding to a particular item discovered using
 * SezPoz. This class is a shared data structure for objects annotated with
 * {@link Plugin}, imagej.tool.Tool, or potentially other similar annotations.
 * 
 * @author Curtis Rueden
 * @see PluginEntry
 */
public abstract class IndexEntry<T> implements Comparable<IndexEntry<?>> {

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

	// -- IndexEntry methods --

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

	/** Gets the class object for this entry, loading it as needed. */
	@SuppressWarnings("unchecked")
	public Class<T> loadClass() throws IndexException {
		if (classObject == null) {
			final Class<?> c;
			try {
				c = Class.forName(className);
			}
			catch (final ClassNotFoundException e) {
				throw new IndexException("Class not found: " + className, e);
			}
			classObject = (Class<T>) c;
		}
		return classObject;
	}

	/** Creates an instance of this entry's associated object. */
	public T createInstance() throws IndexException {
		final Class<T> c = loadClass();

		// instantiate object
		final T instance;
		try {
			instance = c.newInstance();
		}
		catch (final InstantiationException e) {
			throw new IndexException(e);
		}
		catch (final IllegalAccessException e) {
			throw new IndexException(e);
		}
		return instance;
	}

	public URL getIconURL() throws IndexException {
		if (iconPath.isEmpty()) return null;
		return loadClass().getResource(iconPath);
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final IndexEntry<?> entry) {
		return priority - entry.priority;
	}

	// -- Object methods --

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
