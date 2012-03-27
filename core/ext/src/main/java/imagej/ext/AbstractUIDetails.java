/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ext;

import imagej.util.StringMaker;

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

	/** Name of the menu structure to which the object belongs. */
	private String menuRoot;

	/** Resource path to this object's icon. */
	private String iconPath;

	/** Sort priority of the object. */
	private double priority = Priority.LAST_PRIORITY;

	/** Whether the object can be selected in the user interface. */
	private boolean selectable;

	/** The name of the selection group to which the object belongs. */
	private String selectionGroup;

	/** Whether the object is selected in the user interface. */
	private boolean selected;

	/** Whether the object is enabled in the user interface. */
	private boolean enabled = true;

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker();
		sm.append("name", name);
		sm.append("label", label);
		sm.append("description", description);
		sm.append("menu", menuPath.getMenuString());
		sm.append("iconPath", iconPath);
		sm.append("priority", priority, Integer.MAX_VALUE);
		if (selectable) {
			sm.append("selectionGroup", selectionGroup);
			sm.append("selected", selected);
		}
		sm.append("enabled", enabled);
		return sm.toString();
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final UIDetails obj) {
		if (priority == obj.getPriority()) return 0;
		return priority > obj.getPriority() ? -1 : 1;
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
	public String getMenuRoot() {
		return menuRoot;
	}

	@Override
	public String getIconPath() {
		return iconPath;
	}

	@Override
	public double getPriority() {
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
	public void setMenuRoot(final String menuRoot) {
		this.menuRoot = menuRoot;
	}

	@Override
	public void setIconPath(final String iconPath) {
		this.iconPath = iconPath;
	}

	@Override
	public void setPriority(final double priority) {
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

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

}
