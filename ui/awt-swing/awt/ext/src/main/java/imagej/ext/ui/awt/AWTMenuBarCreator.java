
package imagej.ext.ui.awt;

import imagej.ext.menu.ShadowMenu;
import imagej.util.Log;

import java.awt.Menu;
import java.awt.MenuBar;

/**
 * Populates an AWT {@link MenuBar} with menu items from a {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 */
public class AWTMenuBarCreator extends AbstractAWTMenuCreator<MenuBar> {

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final MenuBar target) {
		Log.warn("MenuBarCreator: Ignoring top-level leaf: " + shadow);
	}

	@Override
	protected Menu
		addNonLeafToTop(final ShadowMenu shadow, final MenuBar target)
	{
		final Menu menu = createNonLeaf(shadow);
		target.add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToTop(final MenuBar target) {
		Log.warn("MenuBarCreator: Ignoring top-level separator");
	}

}
