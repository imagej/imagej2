/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.menu;

import imagej.command.CommandService;
import imagej.menu.event.MenusAddedEvent;
import imagej.menu.event.MenusRemovedEvent;
import imagej.menu.event.MenusUpdatedEvent;
import imagej.module.ModuleInfo;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.util.ClassUtils;

/**
 * A tree representing a menu structure independent of any particular user
 * interface.
 * <p>
 * A {@code ShadowMenu} is a tree node with links to other tree nodes. It is
 * possible to traverse the entire menu structure from any given node, though by
 * convention the root node is used to represent the menu as a whole.
 * </p>
 * <p>
 * The class is implemented as a {@link Collection} of modules (i.e.,
 * {@link ModuleInfo} objects), with the tree structure generated from the
 * modules' menu paths (see {@link ModuleInfo#getMenuPath()}). The class also
 * implements {@link Runnable}, with each leaf node retaining a link to its
 * corresponding {@link ModuleInfo}, and executing that module when
 * {@link #run()} is called.
 * </p>
 * 
 * @author Curtis Rueden
 * @see MenuCreator
 * @see MenuPath
 * @see MenuEntry
 */
public class ShadowMenu extends AbstractContextual implements
	Comparable<ShadowMenu>, Collection<ModuleInfo>, Runnable
{

	/** Icon to use for leaf entries by default, if no icon is specified. */
	private static final String DEFAULT_ICON_PATH = "/icons/plugin.png";

	/** The module linked to this node. Always null for non-leaf nodes. */
	private final ModuleInfo moduleInfo;

	/** The menu entry corresponding to this node. */
	private final MenuEntry menuEntry;

	/** How deep into the menu structure this node is. */
	private final int menuDepth;

	/** Reference to parent node. */
	private final ShadowMenu parent;

	/** Table of child nodes, keyed by name. */
	private final Map<String, ShadowMenu> children;

	/** Constructs a root menu node populated with the given modules. */
	public ShadowMenu(final Context context,
		final Collection<? extends ModuleInfo> modules)
	{
		this(context, null, -1, null);
		addAll(modules);
	}

	private ShadowMenu(final Context context, final ModuleInfo moduleInfo,
		final int menuDepth, final ShadowMenu parent)
	{
		setContext(context);
		if (moduleInfo == null) {
			this.moduleInfo = null;
			menuEntry = null;
		}
		else {
			final MenuPath menuPath = moduleInfo.getMenuPath();
			// preserve moduleInfo reference only for leaf items
			final boolean leaf = menuDepth == menuPath.size() - 1;
			this.moduleInfo = leaf ? moduleInfo : null;
			menuEntry = menuPath.get(menuDepth);
		}
		this.menuDepth = menuDepth;
		this.parent = parent;
		children = new HashMap<String, ShadowMenu>();
	}

	// -- ShadowMenu methods --

	/** Gets the module linked to this node, or null if node is not a leaf. */
	public ModuleInfo getModuleInfo() {
		return moduleInfo;
	}

	/**
	 * Gets the node with the given menu path (relative to this node), or null if
	 * no such menu node.
	 * <p>
	 * For example, asking for "File &gt; New &gt; Image..." from the root
	 * application menu node would retrieve the node for "Image...", as would
	 * asking for "New &gt; Image..." from the "File" node.
	 * </p>
	 */
	public ShadowMenu getMenu(final MenuPath menuPath) {
		return getMenu(menuPath, 0);
	}

	public ShadowMenu getMenu(final String path) {
		return getMenu(new MenuPath(path), 0);
	}

	/**
	 * Gets the menu entry corresponding to this node. May be a non-leaf menu
	 * (e.g., "File") or a leaf item (e.g., "Exit").
	 */
	public MenuEntry getMenuEntry() {
		return menuEntry;
	}

	/**
	 * Gets how deep into the menu structure this node is. For example, "File"
	 * would be at depth 1, whereas "Exit" (of "File>Exit") would be at depth 2.
	 */
	public int getMenuDepth() {
		return menuDepth;
	}

	/** Gets the name of the menu. */
	public String getName() {
		return menuEntry == null ? null : menuEntry.getName();
	}

	/** Gets this node's parent, or null if it is a root node. */
	public ShadowMenu getParent() {
		return parent;
	}

	/** Gets this node's children, sorted by weight. */
	public List<ShadowMenu> getChildren() {
		// copy the children table into an ordered list
		final List<ShadowMenu> childList =
			new ArrayList<ShadowMenu>(children.values());
		// sort the list by weight then alphabetically
		Collections.sort(childList);
		return childList;
	}

	/** Returns true if this node has no children. */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/** Returns true if this node is selectable (checkbox or radio button). */
	public boolean isToggle() {
		if (moduleInfo == null) return false;
		return moduleInfo.isSelectable();
	}

	/** Returns true if this node is a checkbox. */
	public boolean isCheckBox() {
		if (!isToggle()) return false;
		final String selectionGroup = moduleInfo.getSelectionGroup();
		return selectionGroup == null || selectionGroup.isEmpty();
	}

	/** Returns true if this node is a radio button. */
	public boolean isRadioButton() {
		if (!isToggle()) return false;
		final String selectionGroup = moduleInfo.getSelectionGroup();
		return selectionGroup != null && !selectionGroup.isEmpty();
	}

	/**
	 * Gets the URL of the icon associated with this node's {@link MenuEntry}.
	 * 
	 * @see org.scijava.plugin.PluginInfo#getIconURL()
	 */
	public URL getIconURL() {
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
			final LogService log = getContext().getService(LogService.class);
			if (log != null) log.error("Could not load icon: " + iconPath);
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
		final ShadowMenu node = addInternal(module);
		if (node == null) return false;
		final EventService es = getContext().getService(EventService.class);
		if (es != null) es.publish(new MenusUpdatedEvent(node));
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
		final HashSet<ShadowMenu> nodes = new HashSet<ShadowMenu>();
		for (final ModuleInfo info : c) {
			final ShadowMenu removed = removeInternal(info);
			if (removed == null) continue; // was not in menu structure
			final ShadowMenu node = addInternal(info);
			if (node != null) nodes.add(node);
		}
		if (nodes.isEmpty()) return false;
		final EventService es = getContext().getService(EventService.class);
		if (es != null) es.publish(new MenusUpdatedEvent(nodes));
		return true;
	}

	// -- Object methods --

	@Override
	public int compareTo(final ShadowMenu c) {
		if (menuEntry == null || c.menuEntry == null) return 0;
		// compare weights
		final double w1 = menuEntry.getWeight();
		final double w2 = c.menuEntry.getWeight();
		if (w1 < w2) return -1;
		if (w1 > w2) return 1;
		// if weights are equal, sort alphabetically
		final String n1 = menuEntry.getName();
		final String n2 = c.menuEntry.getName();
		final String s1 = n1 == null ? "" : n1;
		final String s2 = n2 == null ? "" : n2;
		return s1.compareTo(s2);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= menuDepth; i++)
			sb.append("\t");
		final String name = getName();
		sb.append(name == null ? "[-]" : name);
		for (final ShadowMenu child : getChildren()) {
			sb.append("\n" + child.toString());
		}
		return sb.toString();
	}

	// -- Runnable methods --

	/**
	 * Executes the module linked to this node. Does nothing for non-leaf nodes.
	 */
	@Override
	public void run() {
		if (moduleInfo == null) return; // no module to run
		final CommandService cs = getContext().getService(CommandService.class);
		if (cs != null) cs.run(moduleInfo);
	}

	// -- Collection methods --

	/**
	 * Adds the given module to the menu structure. If the module is not visible
	 * (i.e., {@link ModuleInfo#isVisible()} returns false), it is ignored.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(final ModuleInfo o) {
		if (!o.isVisible()) return false;

		final ShadowMenu node = addInternal(o);
		if (node == null) return false;
		final EventService es = getContext().getService(EventService.class);
		if (es != null) es.publish(new MenusAddedEvent(node));
		return true;
	}

	/**
	 * Adds the given modules to the menu structure. If a module is not visible
	 * (i.e., {@link ModuleInfo#isVisible()} returns false), it is ignored.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(final Collection<? extends ModuleInfo> c) {
		final HashSet<ShadowMenu> nodes = new HashSet<ShadowMenu>();
		for (final ModuleInfo info : c) {
			if (!info.isVisible()) continue;
			final ShadowMenu node = addInternal(info);
			if (node != null) nodes.add(node);
		}
		if (nodes.isEmpty()) return false;
		final EventService es = getContext().getService(EventService.class);
		if (es != null) es.publish(new MenusAddedEvent(nodes));
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
		final ShadowMenu node = removeInternal(info);
		if (node == null) return false;
		final EventService es = getContext().getService(EventService.class);
		if (es != null) es.publish(new MenusRemovedEvent(node));
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		final HashSet<ShadowMenu> nodes = new HashSet<ShadowMenu>();
		for (final Object o : c) {
			if (!(o instanceof ModuleInfo)) continue;
			final ModuleInfo info = (ModuleInfo) o;
			final ShadowMenu node = removeInternal(info);
			if (node != null) nodes.add(node);
		}
		if (nodes.isEmpty()) return false;
		final EventService es = getContext().getService(EventService.class);
		if (es != null) es.publish(new MenusRemovedEvent(nodes));
		return true;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		final ArrayList<Object> toRemove = new ArrayList<Object>();
		for (final ModuleInfo info : this) {
			if (!c.contains(info)) toRemove.add(info);
		}
		return removeAll(toRemove);
	}

	@Override
	public int size() {
		int sum = moduleInfo == null ? 0 : 1;
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
			final ShadowMenu child = children.get(menuName);
			// remove directly from children
			if (child.getModuleInfo() == o) {
				children.remove(menuName);
				return child;
			}

			// recursively remove from descendants
			final ShadowMenu removed = child.removeInternal(o);
			if (removed != null) {
				if (child.isLeaf() && child.getModuleInfo() == null) {
					// prune empty non-leaf node
					children.remove(menuName);
				}
				return removed;
			}
		}

		return null;
	}

	private ShadowMenu addChild(final ModuleInfo info, final int depth) {
		final MenuPath menuPath = info.getMenuPath();
		final MenuEntry entry = menuPath.get(depth);
		final boolean leaf = isLeaf(depth, menuPath);

		// retrieve existing child
		final ShadowMenu existingChild = children.get(entry.getName());

		final ShadowMenu child;
		if (existingChild == null) {
			// create new child and add to table
			final String menuName = entry.getName();
			final ShadowMenu newChild =
				new ShadowMenu(getContext(), info, depth, this);
			children.put(menuName, newChild);
			child = newChild;
		}
		else {
			// fill in any missing menu properties of existing child
			final MenuEntry childMenuEntry = existingChild.getMenuEntry();
			childMenuEntry.assignProperties(entry);
			child = existingChild;
		}

		// recursively add remaining child menus
		if (!leaf) child.addChild(info, depth + 1);
		else if (existingChild != null) {
			final LogService log = getContext().getService(LogService.class);
			if (log != null) {
				log.warn("ShadowMenu: menu item already exists:\n\texisting: " +
					existingChild.getModuleInfo() + "\n\t ignored: " + info);
			}
		}
		return child;
	}

	private boolean isLeaf(final int depth, final MenuPath path) {
		return depth == path.size() - 1;
	}

	private ShadowMenu getMenu(final MenuPath menuPath, final int index) {
		final MenuEntry entry = menuPath.get(index);

		// search for a child with matching menu entry
		for (final ShadowMenu child : children.values()) {
			if (entry.getName().equals(child.getMenuEntry().getName())) {
				// found matching child
				if (isLeaf(index, menuPath)) {
					// return child directly
					return child;
				}
				// recurse downward
				return child.getMenu(menuPath, index + 1);
			}
		}
		return null;
	}

}
