
package imagej.ext.ui.pivot;

import imagej.ImageJ;
import imagej.ext.menu.AbstractMenuCreator;
import imagej.ext.menu.ShadowMenu;
import imagej.ext.module.ModuleInfo;
import imagej.ext.plugin.PluginService;
import imagej.util.Log;

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.Menu.SectionSequence;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.PushButton;

/**
 * Populates a {@link BoxPane} with menu items from a {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 */
public class PivotMenuCreator extends AbstractMenuCreator<BoxPane, MenuButton>
{

	@Override
	protected void
		addLeafToMenu(final ShadowMenu shadow, final MenuButton target)
	{
		final Menu.Item item = new Menu.Item(shadow.getMenuEntry().getName());
		linkAction(shadow.getModuleInfo(), item);
		getLastSection(target).add(item);
	}

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final BoxPane target) {
		Log.debug("PivotMenuCreator: Adding leaf item: " + shadow);
		final PushButton button = new PushButton();
		button.setButtonData(shadow.getMenuEntry().getName());
		linkAction(shadow.getModuleInfo(), button);
		target.add(button);
	}

	@Override
	protected MenuButton addNonLeafToMenu(final ShadowMenu shadow,
		final MenuButton target)
	{
		final MenuButton button = createMenuButton(shadow);
		final Menu.Item item = new Menu.Item(shadow.getMenuEntry().getName());
		getLastSection(target).add(item);
		return button;
	}

	@Override
	protected MenuButton addNonLeafToTop(final ShadowMenu shadow,
		final BoxPane target)
	{
		Log.debug("PivotMenuCreator: Adding menu: " + shadow);
		final MenuButton button = createMenuButton(shadow);
		target.add(button);
		return button;
	}

	@Override
	protected void addSeparatorToMenu(final MenuButton target) {
		target.getMenu().getSections().add(new Menu.Section());
	}

	@Override
	protected void addSeparatorToTop(final BoxPane target) {
		Log.debug("PivotMenuCreator: Adding separator");
		target.add(new Label("|"));
	}

	// -- Helper methods --

	private MenuButton createMenuButton(final ShadowMenu shadow) {
		final MenuButton button = new MenuButton();
		button.setButtonData(shadow.getMenuEntry().getName());
		final Menu menu = new Menu();
		button.setMenu(menu);
		menu.getSections().add(new Menu.Section());
		return button;
	}

	private Menu.Section getLastSection(final MenuButton target) {
		final SectionSequence sections = target.getMenu().getSections();
		return sections.get(sections.getLength() - 1);
	}

	private void linkAction(final ModuleInfo info, final Button button) {
		button.setAction(new Action() {

			@Override
			public void perform(final Component c) {
				ImageJ.get(PluginService.class).run(info);
			}
		});
		button.setEnabled(info.isEnabled());
	}

}
