package imagej.plugin.gui.swing;

import imagej.Log;
import imagej.plugin.gui.MenuCreator;
import imagej.plugin.gui.ShadowMenu;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class JMenuBarCreator extends SwingMenuCreator
	implements MenuCreator<JMenuBar>
{

	@Override
	public void createMenus(final ShadowMenu root, final JMenuBar menuBar) {
		// create menu items and add to menu bar
		final List<JMenuItem> childMenuItems = createChildMenuItems(root);
		for (final JMenuItem childMenuItem : childMenuItems) {
			if (childMenuItem instanceof JMenu) {
				final JMenu childMenu = (JMenu) childMenuItem;
				menuBar.add(childMenu);
			}
			else {
				Log.warn("Ignoring unexpected leaf menu item: " + childMenuItem);
			}
		}
	}

}
