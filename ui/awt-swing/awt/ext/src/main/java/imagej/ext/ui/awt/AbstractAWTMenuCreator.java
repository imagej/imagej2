
package imagej.ext.ui.awt;

import imagej.ImageJ;
import imagej.ext.Accelerator;
import imagej.ext.menu.AbstractMenuCreator;
import imagej.ext.menu.ShadowMenu;
import imagej.ext.module.ModuleInfo;
import imagej.ext.plugin.PluginService;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Populates an AWT menu structure with menu items from a {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public abstract class AbstractAWTMenuCreator<T> extends
	AbstractMenuCreator<T, Menu>
{

	@Override
	protected void addLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final MenuItem menuItem = createLeaf(shadow);
		target.add(menuItem);
	}

	@Override
	protected Menu addNonLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final Menu menu = createNonLeaf(shadow);
		target.add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToMenu(final Menu target) {
		target.addSeparator();
	}

	protected MenuItem createLeaf(final ShadowMenu shadow) {
		final MenuItem menuItem = new MenuItem(shadow.getMenuEntry().getName());
		assignProperties(menuItem, shadow);
		linkAction(shadow.getModuleInfo(), menuItem);
		return menuItem;
	}

	protected Menu createNonLeaf(final ShadowMenu shadow) {
		final Menu menu = new Menu(shadow.getMenuEntry().getName());
		assignProperties(menu, shadow);
		return menu;
	}

	// -- Helper methods --

	private void
		assignProperties(final MenuItem menuItem, final ShadowMenu shadow)
	{
		final Accelerator acc = shadow.getMenuEntry().getAccelerator();
		if (acc != null) {
			final int code = acc.getKeyCode().getCode();
			final boolean shift = acc.getModifiers().isShiftDown();
			final MenuShortcut shortcut = new MenuShortcut(code, shift);
			menuItem.setShortcut(shortcut);
		}
	}

	private void linkAction(final ModuleInfo info, final MenuItem menuItem) {
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				ImageJ.get(PluginService.class).run(info);
			}
		});
	}

}
