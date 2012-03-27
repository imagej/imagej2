
package imagej.ext.ui.swt;

import imagej.ImageJ;
import imagej.ext.menu.AbstractMenuCreator;
import imagej.ext.menu.ShadowMenu;
import imagej.ext.module.ModuleInfo;
import imagej.ext.plugin.PluginService;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Populates an SWT {@link Menu} with menu items from a {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 */
public class SWTMenuCreator extends AbstractMenuCreator<Menu, Menu> {

	@Override
	protected void addLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final MenuItem menuItem = new MenuItem(target, 0);
		menuItem.setText(shadow.getMenuEntry().getName());
		linkAction(shadow.getModuleInfo(), menuItem);
	}

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final Menu target) {
		addLeafToMenu(shadow, target);
	}

	@Override
	protected Menu addNonLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final Menu menu = new Menu(target);
		final MenuItem menuItem = new MenuItem(target, SWT.CASCADE);
		menuItem.setText(shadow.getMenuEntry().getName());
		menuItem.setMenu(menu);
		return menu;
	}

	@Override
	protected Menu addNonLeafToTop(final ShadowMenu shadow, final Menu target) {
		return addNonLeafToMenu(shadow, target);
	}

	@Override
	protected void addSeparatorToMenu(final Menu target) {
		new MenuItem(target, SWT.SEPARATOR);
	}

	@Override
	protected void addSeparatorToTop(final Menu target) {
		addSeparatorToMenu(target);
	}

	// -- Helper methods --

	private void linkAction(final ModuleInfo info, final MenuItem menuItem) {
		menuItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				ImageJ.get(PluginService.class).run(info);
			}
		});
	}

}
