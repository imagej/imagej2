package imagej.gui.swing;

import imagej.Log;
import imagej.gui.menus.MenuCreator;
import imagej.gui.menus.ShadowMenu;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class JMenuCreator extends SwingMenuCreator
	implements MenuCreator<JMenu>
{

	@Override
	public void createMenus(final ShadowMenu root, final JMenu menu) {
		// create menu items and add to menu bar
		final List<JMenuItem> childMenuItems = createChildMenuItems(root);
		for (final JMenuItem childMenuItem : childMenuItems) {
			if (childMenuItem instanceof JMenu) {
				final JMenu childMenu = (JMenu) childMenuItem;
				menu.add(childMenu);
			}
			else {
				Log.warn("Ignoring unexpected leaf menu item: " + childMenuItem);
			}
		}
	}

}
