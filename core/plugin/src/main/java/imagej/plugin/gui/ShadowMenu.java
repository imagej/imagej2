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

package imagej.plugin.gui;

import imagej.Log;
import imagej.plugin.api.MenuEntry;
import imagej.plugin.api.PluginEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class ShadowMenu implements Comparable<ShadowMenu> {

	private PluginEntry<?> pluginEntry;

	private int menuDepth;

	private Map<String, ShadowMenu> children;

	/** Constructs an empty root menu node. */
	public ShadowMenu() {
		this(null, -1);
	}

	/** Constructs a root menu node populated with the given plugin entries. */
	public ShadowMenu(final List<PluginEntry<?>> entries) {
		this();
		for (final PluginEntry<?> entry : entries) addEntry(entry);
	}

	private ShadowMenu(final PluginEntry<?> pluginEntry, final int menuDepth) {
		this.pluginEntry = pluginEntry;
		this.menuDepth = menuDepth;
		children = new HashMap<String, ShadowMenu>();
	}

	public PluginEntry<?> getPluginEntry() {
		return pluginEntry;
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

	public void addEntry(final PluginEntry<?> entry) {
		if (entry.getMenuPath().isEmpty()) return; // no menu
		addChild(entry, 0);
	}

	public MenuEntry getMenuEntry() {
		return pluginEntry.getMenuPath().get(menuDepth);
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

	private void addChild(final PluginEntry<?> entry, final int depth) {
		// retrieve existing child
		final MenuEntry menuEntry = entry.getMenuPath().get(depth);
		final ShadowMenu existingChild = getChild(menuEntry);

		final ShadowMenu childMenu;
		if (existingChild == null) {
			// create new child and add to table
			final String menuName = menuEntry.getName();
			final ShadowMenu newChild = new ShadowMenu(entry, depth);
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
		if (depth + 1 < entry.getMenuPath().size()) {
			childMenu.addChild(entry, depth + 1);
		}
		else if (existingChild != null) {
			Log.warn("ShadowMenu: leaf item already exists: " + existingChild);
		}
	}

}
