package imagej.gui.menus;

import imagej.Log;
import imagej.plugin.api.PluginEntry;

import java.util.List;

import javax.swing.JMenuBar;

public class MenuBuilder {

	public JMenuBar buildMenuBar(final List<PluginEntry> entries) {
		final ShadowMenu shadowMenu = new ShadowMenu();
		for (final PluginEntry entry : entries) {
			Log.debug("Analyzing plugin: " + entry);
			shadowMenu.addEntry(entry);
		}
		return shadowMenu.createMenuBar();
	}

}
