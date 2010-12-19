package imagej.gui;

import imagej.plugin.PluginEntry;
import imagej.plugin.PluginUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBuilder {

	public JMenuBar buildMenuBar(final List<PluginEntry> entries) {
		return buildMenus(new JMenuBar(), entries);
	}

	public JMenuBar buildMenus(final JMenuBar menubar,
		final List<PluginEntry> entries)
	{
		for (final PluginEntry entry : entries) {
			final List<String> menuPath = entry.getMenuPath();
			if (menuPath == null || menuPath.size() == 0) {
				// skip plugins with no associated menu item
				continue;
			}
			final String label = entry.getLabel();
			final JMenuItem menuItem = findMenuItem(menubar, menuPath, label);
			linkAction(entry, menuItem);
		}
		return menubar;
	}

	/**
	 * Retrieves the menu item associated with the given menu path and label,
	 * creating it if necessary.
	 */
	private JMenuItem findMenuItem(final JMenuBar menubar,
		final List<String> menuPath, final String label)
	{
		JMenu parent = findTopLevelMenu(menubar, menuPath.get(0));
		for (int i = 1; i < menuPath.size(); i++) {
			final String name = menuPath.get(i);
			final JMenuItem item = findSubMenu(parent, name);
			if (!(item instanceof JMenu)) {
				throw new IllegalArgumentException(
					"Menu path has premature leaf: " + item.getText());
			}
			parent = (JMenu) item;
		}
		return findLeafItem(parent, label);
	}

	/** Finds the menu with the given label, creating it if needed. */
	private JMenu findTopLevelMenu(final JMenuBar menubar, final String name) {
		for (int i = 0; i < menubar.getMenuCount(); i++) {
			final JMenu menu = menubar.getMenu(i);
			if (menu.getText().equals(name)) return menu;
		}
		// menu does not exist; create it
		final JMenu menu = new JMenu(name);
		menubar.add(menu);
		return menu;
	}

	/**
	 * Finds the menu item with the given label,
	 * creating it as a submenu if needed.
	 */
	private JMenuItem findSubMenu(final JMenu menu, final String name) {
		return findMenuItem(menu, name, true);
	}

	/**
	 * Finds the menu item with the given label,
	 * creating it as a leaf if needed.
	 */
	private JMenuItem findLeafItem(final JMenu menu, final String name) {
		return findMenuItem(menu, name, false);
	}

	/** Finds the menu item with the given label, creating it if needed. */
	private JMenuItem findMenuItem(final JMenu menu, final String name,
		final boolean subMenu)
	{
		for (int i = 0; i < menu.getItemCount(); i++) {
			final JMenuItem item = menu.getItem(i);
			if (item.getText().equals(name)) return item;
		}
		final JMenuItem menuItem;
		if (subMenu) {
			// submenu does not exist; create it
			menuItem = new JMenu(name);
		}
		else {
			// leaf item does not exist; create it
			menuItem = new JMenuItem(name);
		}
		menu.add(menuItem);
		return menuItem;
	}

	private void linkAction(final PluginEntry entry, final JMenuItem menuItem) {
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PluginUtils.runPlugin(entry);
			}
		});
	}

}
