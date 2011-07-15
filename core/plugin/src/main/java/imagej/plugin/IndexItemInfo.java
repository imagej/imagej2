//
// IndexItemInfo.java
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

import imagej.module.BasicDetails;
import imagej.module.MenuEntry;
import imagej.module.UserInterfaceDetails;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of metadata corresponding to a particular index item (e.g.,
 * {@link Plugin} or <code>imagej.tool.Tool</code>) discovered using SezPoz.
 * This class is a shared data structure for common elements of such objects.
 * 
 * @author Curtis Rueden
 */
public class IndexItemInfo<T> implements BasicDetails,
	Comparable<IndexItemInfo<?>>, Instantiable<T>, UserInterfaceDetails
{

	/** Fully qualified class name of this item's object. */
	private String className;

	/** Class object for this item's object. Lazily loaded. */
	private Class<T> classObject;

	/** Unique name of the item. */
	private String name;

	/** Human-readable label for describing the item. */
	private String label;

	/** String describing the item in detail. */
	private String description;

	/** Path to this item's suggested position in the menu structure. */
	private List<MenuEntry> menuPath;

	/** Resource path to this item's icon. */
	private String iconPath;

	/** Sort priority of the item. */
	private int priority = Integer.MAX_VALUE;

	/** Whether the item can be selected in the user interface. */
	private boolean selectable;

	/** The name of the selection group to which the item belongs. */
	private String selectionGroup;

	/** Whether the item is selected in the user interface. */
	private boolean selected;

	/** Whether the item is enabled in the user interface. */
	private boolean enabled = true;

	// -- IndexItemInfo methods --

	/** Sets the unique name of the item. */
	public void setName(final String name) {
		this.name = name;
	}

	/** Sets the name to appear in a UI, if applicable. */
	public void setLabel(final String label) {
		this.label = label;
	}

	/** Sets a string describing the item. */
	public void setDescription(final String description) {
		this.description = description;
	}

	/** Sets the fully qualified name of the {@link Class} of the item objects. */
	public void setClassName(final String className) {
		this.className = className;
	}

	/**
	 * Gets the URL corresponding to the icon resource path.
	 * 
	 * @see #getIconPath()
	 */
	public URL getIconURL() throws InstantiableException {
		if (iconPath == null || iconPath.isEmpty()) return null;
		return loadClass().getResource(iconPath);
	}

	// -- UserInterfaceDetails methods --

	@Override
	public void setMenuPath(final List<MenuEntry> menuPath) {
		if (menuPath == null) {
			this.menuPath = new ArrayList<MenuEntry>();
		}
		else {
			this.menuPath = menuPath;
		}
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(className);
		if (name != null && !name.isEmpty()) {
			appendParam(sb, "name", name);
		}
		if (label != null && !label.isEmpty()) {
			appendParam(sb, "label", label);
		}
		if (description != null && !description.isEmpty()) {
			appendParam(sb, "description", description);
		}
		if (menuPath != null && !menuPath.isEmpty()) {
			appendParam(sb, "menu", getMenuString(menuPath));
		}
		if (iconPath != null && !iconPath.isEmpty()) {
			appendParam(sb, "iconPath", iconPath);
		}
		if (priority < Integer.MAX_VALUE) {
			appendParam(sb, "priority", priority);
		}
		appendParam(sb, "selectable", selectable);
		if (selectable) {
			if (selectionGroup != null && !selectionGroup.isEmpty()) {
				appendParam(sb, "selectionGroup", selectionGroup);
			}
			appendParam(sb, "selected", selected);
		}
		appendParam(sb, "enabled", enabled);
		return sb.toString();
	}

	// -- BasicDetails methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final IndexItemInfo<?> entry) {
		return priority - entry.priority;
	}

	// -- Instantiable methods --

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> loadClass() throws InstantiableException {
		if (classObject == null) {
			final Class<?> c;
			try {
				c = Class.forName(className);
			}
			catch (final ClassNotFoundException e) {
				throw new InstantiableException("Class not found: " + className, e);
			}
			classObject = (Class<T>) c;
		}
		return classObject;
	}

	@Override
	public T createInstance() throws InstantiableException {
		final Class<T> c = loadClass();

		// instantiate object
		final T instance;
		try {
			instance = c.newInstance();
		}
		catch (final InstantiationException e) {
			throw new InstantiableException(e);
		}
		catch (final IllegalAccessException e) {
			throw new InstantiableException(e);
		}
		return instance;
	}

	// -- UserInterfaceDetails methods --

	@Override
	public List<MenuEntry> getMenuPath() {
		return menuPath;
	}

	@Override
	public String getIconPath() {
		return iconPath;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isSelectable() {
		return selectable;
	}

	@Override
	public String getSelectionGroup() {
		return selectionGroup;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setIconPath(final String iconPath) {
		this.iconPath = iconPath;
	}

	@Override
	public void setPriority(final int priority) {
		this.priority = priority;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setSelectable(final boolean selectable) {
		this.selectable = selectable;
	}

	@Override
	public void setSelectionGroup(final String selectionGroup) {
		this.selectionGroup = selectionGroup;
	}

	@Override
	public void setSelected(final boolean selected) {
		this.selected = selected;
	}

	// -- Utility methods --

	public static String getMenuString(final List<MenuEntry> menuPath) {
		return getMenuString(menuPath, true);
	}

	public static String getMenuString(final List<MenuEntry> menuPath,
		final boolean includeLeaf)
	{
		final StringBuilder sb = new StringBuilder();
		final int size = menuPath.size();
		final int last = includeLeaf ? size : size - 1;
		for (int i = 0; i < last; i++) {
			final MenuEntry menu = menuPath.get(i);
			if (i > 0) sb.append(" > ");
			sb.append(menu);
		}
		return sb.toString();
	}

	// -- Helper methods --

	protected void appendParam(final StringBuilder sb, final String key,
		final Object value)
	{
		if (sb.charAt(sb.length() - 1) != ']') {
			// first parameter; add bracket prefix
			sb.append(" [");
		}
		else {
			// remove previous closing bracket
			sb.setLength(sb.length() - 1);
		}
		sb.append("; " + key + " = " + value + "]");
	}

}
