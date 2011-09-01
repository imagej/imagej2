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

package imagej.ext.menu;

import imagej.ext.MenuEntry;
import imagej.ext.MenuPath;
import imagej.ext.menu.event.MenusAddedEvent;
import imagej.ext.menu.event.MenusRemovedEvent;
import imagej.ext.menu.event.MenusUpdatedEvent;
import imagej.ext.module.ModuleInfo;
import imagej.util.ClassUtils;
import imagej.util.Log;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A tree representing the menu structure independent of any particular user
 * interface. Each menu item is linked to a corresponding {@link ModuleInfo}.
 * 
 * @author Curtis Rueden
 * @see MenuCreator
 */
public class ShadowMenu implements Comparable<ShadowMenu>,
	Collection<ModuleInfo>
{

	private static final String DEFAULT_ICON_PATH = "/icons/plugin.png";

	private final MenuService menuService;

	private final ModuleInfo moduleInfo;

	private final int menuDepth;

	private final Map<String, ShadowMenu> children;

	/** Constructs a root menu node populated with all available modules. */
	public ShadowMenu(final MenuService menuService) {
		this(menuService, menuService.getPluginService().getModuleService()
			.getModules());
	}

	/** Constructs a root menu node populated with the given modules. */
	public ShadowMenu(final MenuService menuService,
		final Collection<? extends ModuleInfo> modules)
	{
		this(menuService, null, -1);
		addAll(modules);
	}

	private ShadowMenu(final MenuService menuService, final ModuleInfo info,
		final int menuDepth)
	{
		this.menuService = menuService;
		this.moduleInfo = info;
		this.menuDepth = menuDepth;
		children = new HashMap<String, ShadowMenu>();
	}

	// -- ShadowMenu methods --

	/** Gets the {@link MenuService} associated with this menu. */
	public MenuService getMenuService() {
		return menuService;
	}

	/** Gets the module linked to this menu. */
	public ModuleInfo getModuleInfo() {
		return moduleInfo;
	}

	/** Gets this menu's index into its linked module's {@link MenuEntry}. */
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

	/** Returns true if this menu has no children. */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/** Returns true if this menu is selectable (checkbox or radio button). */
	public boolean isToggle() {
		if (moduleInfo == null) return false;
		return moduleInfo.isSelectable();
	}

	/** Returns true if this menu is a checkbox. */
	public boolean isCheckBox() {
		if (!isToggle()) return false;
		final String selectionGroup = moduleInfo.getSelectionGroup();
		return selectionGroup == null || selectionGroup.isEmpty();
	}

	/** Returns true if this menu is a radio button. */
	public boolean isRadioButton() {
		if (!isToggle()) return false;
		final String selectionGroup = moduleInfo.getSelectionGroup();
		return selectionGroup != null && !selectionGroup.isEmpty();
	}

	/** Gets the {@link MenuEntry} associated with this menu. */
	public MenuEntry getMenuEntry() {
		if (moduleInfo == null) return null;
		final MenuPath menuPath = moduleInfo.getMenuPath();
		if (menuPath == null) return null;
		return menuPath.get(menuDepth);
	}

	/** Gets the URL of the icon associated with this menu's {@link MenuEntry}. */
	public URL getIconURL() {
		final MenuEntry menuEntry = getMenuEntry();
		if (menuEntry == null) return null;
		String iconPath = menuEntry.getIconPath();
		if (iconPath == null || iconPath.isEmpty()) {
			if (isLeaf()) iconPath = DEFAULT_ICON_PATH;
			else return null;
		}
		final String className = moduleInfo.getDelegateClassName();
		final Class<?> c = ClassUtils.loadClass(className);
		if (c == null) return null;
		final URL iconURL = c.getResource(iconPath);
		if (iconURL == null) {
			Log.error("Could not load icon: " + iconPath);
		}
		return iconURL;
	}

	/**
	 * Updates the menu structure to reflect changes in the given module. Does
	 * nothing unless the module is already in the menu structure.
	 * 
	 * @return true if the module was successfully updated
	 */
	public boolean update(final ModuleInfo module) {
		final ShadowMenu removed = removeInternal(module);
		if (removed == null) return false; // was not in menu structure
		final ShadowMenu menu = addInternal(module);
		if (menu == null) return false;
		menuService.getEventService().publish(new MenusUpdatedEvent(menu));
		return true;
	}

	/**
	 * Updates the menu structure to reflect changes in the given modules. Does
	 * nothing unless at least one of the modules is already in the menu
	 * structure.
	 * 
	 * @return true if at least one module was successfully updated
	 */
	public boolean updateAll(final Collection<? extends ModuleInfo> c) {
		final HashSet<ShadowMenu> menus = new HashSet<ShadowMenu>();
		for (final ModuleInfo info : c) {
			System.out.println("==> ShadowMenu.updateAll: info=" + info);//TEMP
			final ShadowMenu removed = removeInternal(info);
			if (removed == null) continue; // was not in menu structure
			System.out.println("==> ShadowMenu.updateAll: removed item: " + removed);//TEMP
			final ShadowMenu menu = addInternal(info);
			System.out.println("==> ShadowMenu.updateAll: added item: " + menu);//TEMP
			if (menu != null) menus.add(menu);
		}
		if (menus.isEmpty()) return false;
		menuService.getEventService().publish(new MenusUpdatedEvent(menus));
		return true;
	}

	/** Executes the module linked to this menu. */
	public void run() {
		menuService.getPluginService().run(moduleInfo);
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

	// -- Collection methods --

	@Override
	public boolean add(final ModuleInfo o) {
		final ShadowMenu menu = addInternal(o);
		if (menu == null) return false;
		menuService.getEventService().publish(new MenusAddedEvent(menu));
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends ModuleInfo> c) {
		final HashSet<ShadowMenu> menus = new HashSet<ShadowMenu>();
		for (final ModuleInfo info : c) {
			final ShadowMenu menu = addInternal(info);
			if (menu != null) menus.add(menu);
		}
		if (menus.isEmpty()) return false;
		menuService.getEventService().publish(new MenusAddedEvent(menus));
		return true;
	}

	@Override
	public void clear() {
		children.clear();
	}

	@Override
	public boolean contains(final Object o) {
		if (o == moduleInfo) return true;
		for (final ShadowMenu node : children.values()) {
			if (node.contains(o)) return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return children.isEmpty();
	}

	@Override
	public ShadowMenuIterator iterator() {
		return new ShadowMenuIterator(this);
	}

	@Override
	public boolean remove(final Object o) {
		if (!(o instanceof ModuleInfo)) return false;
		final ModuleInfo info = (ModuleInfo) o;
		final ShadowMenu menu = removeInternal(info);
		if (menu == null) return false;
		menuService.getEventService().publish(new MenusRemovedEvent(menu));
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		final HashSet<ShadowMenu> menus = new HashSet<ShadowMenu>();
		for (final Object o : c) {
			if (!(o instanceof ModuleInfo)) continue;
			final ModuleInfo info = (ModuleInfo) o;
			final ShadowMenu menu = removeInternal(info);
			if (menu != null) menus.add(menu);
		}
		if (menus.isEmpty()) return false;
		menuService.getEventService().publish(new MenusRemovedEvent(menus));
		return true;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		int sum = 1;
		for (final ShadowMenu child : children.values()) {
			sum += child.size();
		}
		return sum;
	}

	@Override
	public Object[] toArray() {
		final Object[] a = new Object[size()];
		return toArray(a);
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		// ensure that the type of the specified array is valid
		final Class<?> componentType = a.getClass().getComponentType();
		if (!ModuleInfo.class.isAssignableFrom(componentType)) {
			throw new ArrayStoreException();
		}

		// ensure that the destination array is of sufficient size
		final int size = size();
		final T[] result;
		if (a.length >= size) {
			// given array is big enough; use it
			result = a;
		}
		else {
			// allocate new array of sufficient size
			final Object newArray = Array.newInstance(componentType, size);
			@SuppressWarnings("unchecked")
			final T[] typedArray = (T[]) newArray;
			result = typedArray;
		}

		// copy elements into destination array
		int index = 0;
		final ShadowMenuIterator iter = iterator();
		while (iter.hasNext()) {
			@SuppressWarnings("unchecked")
			final T element = (T) iter.next();
			result[index++] = element;
		}

		return result;
	}

	// -- Helper methods --

	private ShadowMenu addInternal(final ModuleInfo o) {
		if (o.getMenuPath().isEmpty()) return null; // no menu
		return addChild(o, 0);
	}

	private ShadowMenu removeInternal(final ModuleInfo o) {
		for (final String menuName : children.keySet()) {
			final ShadowMenu menu = children.get(menuName);
			// remove directly from children
			if (menu.getModuleInfo() == o) {
				children.remove(menuName);
				return menu;
			}

			// recursively remove from descendants
			final ShadowMenu removed = menu.removeInternal(o);
			if (removed != null) return removed;
		}

		return null;
	}

	private ShadowMenu addChild(final ModuleInfo child, final int depth) {
		// retrieve existing child
		final MenuEntry menuEntry = child.getMenuPath().get(depth);
		final ShadowMenu existingChild = getChild(menuEntry);

		final ShadowMenu childMenu;
		if (existingChild == null) {
			// create new child and add to table
			final String menuName = menuEntry.getName();
			final ShadowMenu newChild = new ShadowMenu(menuService, child, depth);
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
		return childMenu;
	}

	private ShadowMenu getChild(final MenuEntry menuEntry) {
		return children.get(menuEntry.getName());
	}

}
