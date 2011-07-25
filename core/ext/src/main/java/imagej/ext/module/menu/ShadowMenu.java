//
// ShadowMenu.java
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

package imagej.ext.module.menu;

import imagej.ext.MenuEntry;
import imagej.ext.module.ModuleInfo;
import imagej.util.ClassUtils;
import imagej.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tree representing the menu structure independent of any particular user
 * interface. Each menu item is linked to a corresponding {@link ModuleInfo}.
 * 
 * @author Curtis Rueden
 * @see MenuCreator
 */
public class ShadowMenu implements Comparable<ShadowMenu> {

	private static final String DEFAULT_ICON_PATH = "/icons/plugin.png";

	private final ModuleInfo info;

	private final int menuDepth;

	private final Map<String, ShadowMenu> children;

	/** Constructs an empty root menu node. */
	public ShadowMenu() {
		this(null, -1);
	}

	/** Constructs a root menu node populated with the given module entries. */
	public ShadowMenu(final List<ModuleInfo> entries) {
		this();
		for (final ModuleInfo entry : entries)
			addChild(entry);
	}

	private ShadowMenu(final ModuleInfo info, final int menuDepth) {
		this.info = info;
		this.menuDepth = menuDepth;
		children = new HashMap<String, ShadowMenu>();
	}

	public ModuleInfo getModuleInfo() {
		return info;
	}

	public int getMenuDepth() {
		return menuDepth;
	}

	/** Gets this menu's children, sorted by weight. */
	public List<ShadowMenu> getChildren() {
		final List<ShadowMenu> childMenus =
			new ArrayList<ShadowMenu>(children.values());
		Collections.sort(childMenus);
		return childMenus;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public boolean isToggle() {
		if (info == null) return false;
		return info.isSelectable();
	}

	public boolean isCheckBox() {
		if (!isToggle()) return false;
		final String selectionGroup = info.getSelectionGroup();
		return selectionGroup == null || selectionGroup.isEmpty();
	}

	public boolean isRadioButton() {
		if (!isToggle()) return false;
		final String selectionGroup = info.getSelectionGroup();
		return selectionGroup != null && !selectionGroup.isEmpty();
	}

	public void addChild(final ModuleInfo child) {
		if (child.getMenuPath().isEmpty()) return; // no menu
		addChild(child, 0);
	}

	public MenuEntry getMenuEntry() {
		return info.getMenuPath().get(menuDepth);
	}

	public URL getIconURL() {
		String iconPath = getMenuEntry().getIconPath();
		if (iconPath == null || iconPath.isEmpty()) {
			if (isLeaf()) iconPath = DEFAULT_ICON_PATH;
			else return null;
		}
		final String className = info.getDelegateClassName();
		final Class<?> c = ClassUtils.loadClass(className);
		if (c == null) return null;
		final URL iconURL = c.getResource(iconPath);
		if (iconURL == null) {
			Log.error("Could not load icon: " + iconPath);
		}
		return iconURL;
	}

	// -- Object methods --

	@Override
	public int compareTo(final ShadowMenu c) {
		final double w1 = getMenuEntry().getWeight();
		final double w2 = c.getMenuEntry().getWeight();
		if (w1 < w2) return -1;
		if (w1 > w2) return 1;
		// if weights are equal, sort alphabetically
		final String n1 = getMenuEntry().getName();
		final String n2 = c.getMenuEntry().getName();
		return n1.compareTo(n2);
	}

	@Override
	public String toString() {
		return getMenuEntry().getName();
	}

	// -- Helper methods --

	private ShadowMenu getChild(final MenuEntry menuEntry) {
		return children.get(menuEntry.getName());
	}

	private void addChild(final ModuleInfo child, final int depth) {
		// retrieve existing child
		final MenuEntry menuEntry = child.getMenuPath().get(depth);
		final ShadowMenu existingChild = getChild(menuEntry);

		final ShadowMenu childMenu;
		if (existingChild == null) {
			// create new child and add to table
			final String menuName = menuEntry.getName();
			final ShadowMenu newChild = new ShadowMenu(child, depth);
			children.put(menuName, newChild);
			childMenu = newChild;
		}
		else {
			// fill in any missing menu properties of existing child
			final MenuEntry childMenuEntry = existingChild.getMenuEntry();
			childMenuEntry.assignProperties(menuEntry);
			childMenu = existingChild;
		}

		// recursively add remaining child menus
		if (depth + 1 < child.getMenuPath().size()) {
			childMenu.addChild(child, depth + 1);
		}
		else if (existingChild != null) {
			Log.warn("ShadowMenu: leaf item already exists: " + existingChild);
		}
	}

	// -- Utility methods --

	/**
	 * Gets the label to use for the given module's menu item. The result is
	 * prioritized as follows:
	 * <ol>
	 * <li>Item label</li>
	 * <li>Menu path's leaf entry name</li>
	 * <li>Item name</li>
	 * <li>Item's class name, without package prefix</li>
	 * </ol>
	 */
	public static String getMenuLabel(final ModuleInfo info) {
		// use object label, if available
		final String label = info.getLabel();
		if (label != null && !label.isEmpty()) return label;

		// use name of leaf menu item, if available
		final List<MenuEntry> menuPath = info.getMenuPath();
		if (menuPath != null && menuPath.size() > 0) {
			final MenuEntry menuEntry = menuPath.get(menuPath.size() - 1);
			final String menuName = menuEntry.getName();
			if (menuName != null && !menuName.isEmpty()) return menuName;
		}

		// use object name, if available
		final String name = info.getName();
		if (name != null && !name.isEmpty()) return name;

		// use object class name
		final String className = info.getDelegateClassName();
		final int dot = className.lastIndexOf(".");
		return dot < 0 ? className : className.substring(dot + 1);
	}

}
