package imagej.gui;

import imagej.Log;
import imagej.plugin.MenuEntry;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MenuBuilder {

	public JMenuBar buildMenuBar(final List<PluginEntry> entries) {
		final ShadowMenu shadowMenu = new ShadowMenu();
		for (final PluginEntry entry : entries) {
			Log.debug("Analyzing plugin: " + entry);
			shadowMenu.addEntry(entry);
		}
		return shadowMenu.createMenuBar();
	}

	/** Helper class for generating sorted list of menus. */
	private class ShadowMenu implements Comparable<ShadowMenu> {
		private PluginEntry pluginEntry;
		private int menuDepth;
		private Map<String, ShadowMenu> children =
			new HashMap<String, ShadowMenu>();

		/** Constructor for root menu node. */
		public ShadowMenu() {
			this(null, -1);
		}

		private ShadowMenu(final PluginEntry pluginEntry, final int menuDepth) {
			this.pluginEntry = pluginEntry;
			this.menuDepth = menuDepth;
		}

		public void addEntry(final PluginEntry entry) {
			addChild(entry, 0);
		}

		public JMenuBar createMenuBar() {
			assert pluginEntry == null && menuDepth == -1;
			final JMenuBar menuBar = new JMenuBar();

			// create menu items and add to menu bar
			final List<JMenuItem> childMenuItems = createChildMenuItems();
			for (final JMenuItem childMenuItem : childMenuItems) {
				if (childMenuItem instanceof JMenu) {
					final JMenu childMenu = (JMenu) childMenuItem;
					menuBar.add(childMenu);
				}
				else {
					Log.warn("Unexpected leaf menu item: " + childMenuItem);
				}
			}

			return menuBar;
		}

		private MenuEntry getMenuEntry() {
			return pluginEntry.getMenuPath().get(menuDepth);
		}

		private ShadowMenu getChild(final MenuEntry menuEntry) {
			return children.get(menuEntry.getName());
		}

		private void addChild(final PluginEntry entry, final int depth) {
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
		}

		/**
		 * Generates a list of menu items corresponding
		 * to the child menu nodes, sorted by weight.
		 */
		private List<JMenuItem> createChildMenuItems() {
			// generate list of ShadowMenu objects, sorted by weight
			final List<ShadowMenu> childMenus =
				new ArrayList<ShadowMenu>(children.values());
			Collections.sort(childMenus);

			// create JMenuItems corresponding to ShadowMenu objects
			final List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
			double lastWeight = Double.NaN;
			for (final ShadowMenu childMenu : childMenus) {
				final double weight = childMenu.getMenuEntry().getWeight();
				final double difference = Math.abs(weight - lastWeight);
				if (difference > 1) menuItems.add(null); // separator
				lastWeight = weight;
				final JMenuItem item = childMenu.createMenuItem();
				menuItems.add(item);
			}
			return menuItems;
		}

		/** Generates a menu item corresponding to this menu node. */
		private JMenuItem createMenuItem() {
			final MenuEntry menuEntry = getMenuEntry();

			final String name = menuEntry.getName();
			final char mnemonic = menuEntry.getMnemonic();
			final String accelerator = menuEntry.getAccelerator();
			final KeyStroke keyStroke = toKeyStroke(accelerator);
			final String iconPath = menuEntry.getIcon();
			final Icon icon = loadIcon(iconPath);

			final JMenuItem menuItem;
			if (children.isEmpty()) {
				// create leaf item
				menuItem = new JMenuItem(name);
				linkAction(pluginEntry, menuItem);
			}
			else {
				// create menu and recursively add children
				final JMenu menu = new JMenu(name);
				final List<JMenuItem> childMenuItems = createChildMenuItems();
				for (final JMenuItem childMenuItem : childMenuItems) {
					if (childMenuItem == null) menu.addSeparator();
					else menu.add(childMenuItem);
				}
				menuItem = menu;
			}
			if (mnemonic != '\0') menuItem.setMnemonic(mnemonic);
			if (keyStroke != null) menuItem.setAccelerator(keyStroke);
			if (icon != null) menuItem.setIcon(icon);

			return menuItem;
		}

		@Override
		public int compareTo(ShadowMenu c) {
			final double w1 = getMenuEntry().getWeight();
			final double w2 = c.getMenuEntry().getWeight();
			if (w1 < w2) return -1;
			if (w1 > w2) return 1;
			// if weights are equal, sort alphabetically
			final String n1 = getMenuEntry().getName();
			final String n2 = c.getMenuEntry().getName();
			return n1.compareTo(n2);
		}

		private KeyStroke toKeyStroke(final String accelerator) {
			return KeyStroke.getKeyStroke(accelerator);
		}

		private Icon loadIcon(String icon) {
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

}
