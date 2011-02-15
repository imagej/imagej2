package imagej.plugin.gui.swing;

import imagej.Log;
import imagej.plugin.RunnablePlugin;
import imagej.plugin.api.MenuEntry;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginUtils;
import imagej.plugin.gui.ShadowMenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public abstract class SwingMenuCreator {

	/**
	 * Generates a list of menu items corresponding
	 * to the child menu nodes, sorted by weight.
	 */
	protected List<JMenuItem> createChildMenuItems(final ShadowMenu shadow) {
		// generate list of ShadowMenu objects, sorted by weight
		final List<ShadowMenu> childMenus =
			new ArrayList<ShadowMenu>(shadow.getChildren().values());
		Collections.sort(childMenus);

		// create JMenuItems corresponding to ShadowMenu objects
		final List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
		double lastWeight = Double.NaN;
		for (final ShadowMenu childMenu : childMenus) {
			final double weight = childMenu.getMenuEntry().getWeight();
			final double difference = Math.abs(weight - lastWeight);
			if (difference > 1) menuItems.add(null); // separator
			lastWeight = weight;
			final JMenuItem item = createMenuItem(childMenu);
			menuItems.add(item);
		}
		return menuItems;
	}

	/** Generates a menu item corresponding to this shadow menu node. */
	private JMenuItem createMenuItem(ShadowMenu shadow) {
		final MenuEntry menuEntry = shadow.getMenuEntry();

		final String name = menuEntry.getName();
		final char mnemonic = menuEntry.getMnemonic();
		final String accelerator = menuEntry.getAccelerator();
		final KeyStroke keyStroke = toKeyStroke(accelerator);
		final String iconPath = menuEntry.getIcon();
		final Icon icon = loadIcon(iconPath);

		final JMenuItem menuItem;
		if (shadow.isLeaf()) {
			// create leaf item
			menuItem = new JMenuItem(name);
			linkAction(shadow.getPluginEntry(), menuItem);
		}
		else {
			// create menu and recursively add children
			final JMenu menu = new JMenu(name);
			final List<JMenuItem> childMenuItems = createChildMenuItems(shadow);
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

	private <T extends RunnablePlugin> void linkAction(final PluginEntry<?> entry,
		final JMenuItem menuItem)
	{
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO - find better solution for typing here
				@SuppressWarnings("unchecked")
				final PluginEntry<T> runnableEntry = (PluginEntry<T>) entry;
				PluginUtils.runPlugin(runnableEntry);
			}
		});
	}

}
