package imagej.gui;

import imagej.Log;
import imagej.plugin.MenuEntry;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MenuBuilder {

	public JMenuBar buildMenuBar(final List<PluginEntry> entries) {
		return buildMenus(new JMenuBar(), entries);
	}

	public JMenuBar buildMenus(final JMenuBar menubar,
		final List<PluginEntry> entries)
	{
		for (final PluginEntry entry : entries) {
			Log.debug("Analyzing plugin: " + entry);
			final List<MenuEntry> menuPath = entry.getMenuPath();
			if (menuPath == null || menuPath.size() <= 1) {
				// skip plugins with no associated menu item
				continue;
			}
			final JMenuItem menuItem = findMenuItem(menubar, menuPath);
			linkAction(entry, menuItem);
		}
		return menubar;
	}

	/**
	 * Retrieves the menu item associated with the given menu path and label,
	 * creating it if necessary.
	 */
	private JMenuItem findMenuItem(final JMenuBar menubar,
		final List<MenuEntry> menuPath)
	{
		assert menuPath.size() > 1;
		final MenuEntry topLevelEntry = menuPath.get(0);

		// dig down through menu structure
		JMenu parent = findTopLevelMenu(menubar, topLevelEntry);
		for (int i = 1; i < menuPath.size() - 1; i++) {
			final MenuEntry entry = menuPath.get(i);
			final JMenuItem item = findSubMenu(parent, entry);
			if (!(item instanceof JMenu)) {
				throw new IllegalArgumentException(
					"Menu path has premature leaf: " + item.getText());
			}
			parent = (JMenu) item;
		}

		// obtain leaf item
		final MenuEntry leafEntry = menuPath.get(menuPath.size() - 1);
		return findLeafItem(parent, leafEntry);
	}

	/** Finds the menu described by the given entry, creating it if needed. */
	private JMenu findTopLevelMenu(final JMenuBar menubar,
		final MenuEntry entry)
	{
		final String name = entry.getName();
		for (int i = 0; i < menubar.getMenuCount(); i++) {
			final JMenu menu = menubar.getMenu(i);
			if (menu.getText().equals(name)) return menu;
		}
		// menu does not exist; create it
		final JMenu menu = new JMenu(name);
		menu.setMnemonic(entry.getMnemonic());
		final KeyStroke keyStroke = getKeyStroke(entry.getAccelerator());
		if (keyStroke != null) menu.setAccelerator(keyStroke);
		final Icon icon = getIcon(entry.getIcon());
		if (icon != null) menu.setIcon(icon);
		menubar.add(menu);
		return menu;
	}

	/**
	 * Finds the menu item described by the given entry,
	 * creating it as a submenu if needed.
	 */
	private JMenuItem findSubMenu(final JMenu menu, final MenuEntry entry) {
		return findMenuItem(menu, entry, true);
	}

	/**
	 * Finds the menu item described by the given entry,
	 * creating it as a leaf if needed.
	 */
	private JMenuItem findLeafItem(final JMenu menu, final MenuEntry leafEntry) {
		return findMenuItem(menu, leafEntry, false);
	}

	/**
	 * Finds the menu item described by the given entry, creating it if needed.
	 */
	private JMenuItem findMenuItem(final JMenu menu, final MenuEntry entry,
		final boolean subMenu)
	{
		final String name = entry.getName();

		for (int i = 0; i < menu.getItemCount(); i++) {
			final JMenuItem item = menu.getItem(i);
			if (item.getText().equals(name)) return item;
		}
		final JMenuItem menuItem;

		// submenu/leaf item does not exist; create it
		if (subMenu) menuItem = new JMenu(name);
		else menuItem = new JMenuItem(name);
		menu.add(menuItem);
		return menuItem;
	}

	private KeyStroke getKeyStroke(final String accelerator) {
		// TODO
		return null;
	}

	private Icon getIcon(String icon) {
		if (icon == null || icon.isEmpty()) return null;
		try {
			return new ImageIcon(new URL(icon));
		}
		catch (MalformedURLException e) {
			Log.warn("No such icon: " + icon);
			return null;
		}
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
