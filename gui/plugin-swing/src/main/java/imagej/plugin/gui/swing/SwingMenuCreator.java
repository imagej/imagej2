//
// SwingMenuCreator.java
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

package imagej.plugin.gui.swing;

import imagej.Log;
import imagej.plugin.RunnablePlugin;
import imagej.plugin.api.MenuEntry;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;
import imagej.plugin.api.PluginUtils;
import imagej.plugin.gui.AbstractMenuCreator;
import imagej.plugin.gui.ShadowMenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public abstract class SwingMenuCreator<M>
	extends AbstractMenuCreator<M, JMenuItem>
{

	@Override
	protected JMenuItem createMenuItem(final ShadowMenu shadow) {
		final MenuEntry menuEntry = shadow.getMenuEntry();

		final String name = menuEntry.getName();
		final char mnemonic = menuEntry.getMnemonic();
		final KeyStroke keyStroke = getKeyStroke(shadow);
		final Icon icon = loadIcon(shadow);

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

	private KeyStroke getKeyStroke(final ShadowMenu shadow) {
		String accelerator = shadow.getMenuEntry().getAccelerator();
		if (accelerator != null){
			System.out.print("accelerator specified : original = ("+accelerator+")");
			// allow use of ^X to represent control X in plugin keyboard accel parameters
			accelerator = accelerator.replaceAll(Pattern.quote("^"), "control ");  // NB: extra space REQUIRED
			// on Mac, use Command instead of Control for keyboard shortcuts
			if (isMac())
				if (accelerator.indexOf("meta") == -1)  // only if meta not already in use
					accelerator = accelerator.replaceAll("control", "meta");
		}
		return KeyStroke.getKeyStroke(accelerator);
	}

	private Icon loadIcon(final ShadowMenu shadow) {
		final String iconPath = shadow.getMenuEntry().getIcon();
		if (iconPath == null || iconPath.isEmpty()) return null;
		try {
			final Class<?> c = shadow.getPluginEntry().loadClass();
			final URL iconURL = c.getResource(iconPath);
			if (iconURL == null) return null;
			return new ImageIcon(iconURL);
		}
		catch (PluginException e) {
			Log.error("Could not load icon: " + iconPath, e);
		}
		return null;
	}

	private void linkAction(final PluginEntry<?> entry,
		final JMenuItem menuItem)
	{
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO - find better solution for typing here
				@SuppressWarnings("unchecked")
				final PluginEntry<? extends RunnablePlugin> runnableEntry =
					(PluginEntry<? extends RunnablePlugin>) entry;
				PluginUtils.runPlugin(runnableEntry);
			}
		});
	}

	private boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac");
	}

}
