//
// AbstractUIDetails.java
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

package imagej.ext;

/**
 * Abstract superclass of {@link UIDetails} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractUIDetails implements UIDetails {

	/** Unique name of the object. */
	private String name;

	/** Human-readable label for describing the object. */
	private String label;

	/** String describing the object in detail. */
	private String description;

	/** Path to this object's suggested position in the menu structure. */
	private MenuPath menuPath;

	/** Resource path to this object's icon. */
	private String iconPath;

	/** Sort priority of the object. */
	private int priority = Integer.MAX_VALUE;

	/** Whether the object can be selected in the user interface. */
	private boolean selectable;

	/** The name of the selection group to which the object belongs. */
	private String selectionGroup;

	/** Whether the object is selected in the user interface. */
	private boolean selected;

	/** Whether the object is enabled in the user interface. */
	private boolean enabled = true;

	// -- AbstractUIDetails methods --

	/** Sets the unique name of the object. */
	public void setName(final String name) {
		this.name = name;
	}

	/** Sets the name to appear in a UI, if applicable. */
	public void setLabel(final String label) {
		this.label = label;
	}

	/** Sets a string describing the object. */
	public void setDescription(final String description) {
		this.description = description;
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
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
			appendParam(sb, "menu", menuPath.getMenuString());
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

	// -- Comparable methods --

	@Override
	public int compareTo(final UIDetails obj) {
		return priority - obj.getPriority();
	}

	// -- UIDetails methods --

	@Override
	public String getTitle() {
		// use object label, if available
		if (label != null && !label.isEmpty()) return label;

		// use name of leaf menu item, if available
		if (menuPath != null && menuPath.size() > 0) {
			final MenuEntry menuLeaf = menuPath.getLeaf();
			final String menuName = menuLeaf.getName();
			if (menuName != null && !menuName.isEmpty()) return menuName;
		}

		// use object name, if available
		if (name != null && !name.isEmpty()) return name;

		// use class name as a last resort
		return getClass().getSimpleName();
	}

	@Override
	public MenuPath getMenuPath() {
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
	public void setMenuPath(final MenuPath menuPath) {
		if (menuPath == null) {
			this.menuPath = new MenuPath();
		}
		else {
			this.menuPath = menuPath;
		}
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

	// -- Internal AbstractUIDetails methods --

	protected void appendParam(final StringBuilder sb, final String key,
		final Object value)
	{
		final int len = sb.length();
		if (len == 0 || sb.charAt(len - 1) != ']') {
			// first parameter; add bracket prefix
			sb.append(" [");
		}
		else {
			// remove previous closing bracket
			sb.setLength(len - 1);
			sb.append("; ");
		}
		sb.append(key + " = " + value + "]");
	}

}
