//
// SWTMenuCreator.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.plugin.gui.swt;

import imagej.plugin.RunnablePlugin;
import imagej.plugin.api.MenuEntry;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginUtils;
import imagej.plugin.gui.AbstractMenuCreator;
import imagej.plugin.gui.ShadowMenu;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import java.util.List;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public abstract class SWTMenuCreator<M>
	extends AbstractMenuCreator<M, MenuItem>
{

	@Override
	public MenuItem createMenuItem(final ShadowMenu shadow) {
		final MenuEntry menuEntry = shadow.getMenuEntry();

		final String name = menuEntry.getName();

		final MenuItem menuItem;
		if (shadow.isLeaf()) {
			// create leaf item
			menuItem = new MenuItem((Menu) null, 0);
			menuItem.setData(name);
			linkAction(shadow.getPluginEntry(), menuItem);
		}
		else {
			// create menu and recursively add children
			final Menu menu = new Menu((Menu) null);
			menu.setData(name);
			final List<MenuItem> childMenuItems = createChildMenuItems(shadow);
			for (final MenuItem childMenuItem : childMenuItems) {
				if (childMenuItem == null) {
					// TODO: how to add a separator?
					//menu.addSeparator();
				}
				else childMenuItem.setMenu(menu);
			}
			menuItem = null; //menu;
		}

		return menuItem;
	}

	private void linkAction(final PluginEntry<?> entry, final MenuItem menuItem) {
//		menuItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				// TODO - find better solution for typing here
//				@SuppressWarnings("unchecked")
//				final PluginEntry<? extends RunnablePlugin> runnableEntry =
//					(PluginEntry<? extends RunnablePlugin>) entry;
//				PluginUtils.runPlugin(runnableEntry);
//			}
//		});
	}

}
