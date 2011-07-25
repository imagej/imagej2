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

package imagej.ext.ui.swing;

import imagej.ImageJ;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.menu.AbstractMenuCreator;
import imagej.ext.module.menu.ShadowMenu;
import imagej.ext.plugin.PluginService;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 * Populates a Swing menu structure with menu items from a {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 */
public abstract class SwingMenuCreator<T> extends
	AbstractMenuCreator<T, JMenu>
{

	/** Table of button groups for radio button menu items. */
	private HashMap<String, ButtonGroup> buttonGroups =
		new HashMap<String, ButtonGroup>();

	@Override
	public void createMenus(final ShadowMenu root, final T target) {
		buttonGroups = new HashMap<String, ButtonGroup>();
		super.createMenus(root, target);
	}

	@Override
	protected void addLeafToMenu(final ShadowMenu shadow, final JMenu target) {
		final JMenuItem menuItem = createLeaf(shadow);
		target.add(menuItem);
	}

	@Override
	protected JMenu
		addNonLeafToMenu(final ShadowMenu shadow, final JMenu target)
	{
		final JMenu menu = createNonLeaf(shadow);
		target.add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToMenu(final JMenu target) {
		target.addSeparator();
	}

	protected JMenuItem createLeaf(final ShadowMenu shadow) {
		final String name = shadow.getMenuEntry().getName();
		final JMenuItem menuItem;
		if (shadow.isCheckBox()) {
			// NB: Call to isSelected(shadow) instantiates the plugin (slow!).
			menuItem = new JCheckBoxMenuItem(name, isSelected(shadow));
		}
		else if (shadow.isRadioButton()) {
			// NB: Call to isSelected(shadow) instantiates the plugin (slow!).
			menuItem = new JRadioButtonMenuItem(name, isSelected(shadow));
			getButtonGroup(shadow).add(menuItem);
		}
		else menuItem = new JMenuItem(name);
		assignProperties(menuItem, shadow);
		linkAction(shadow.getInfo(), menuItem);
		return menuItem;
	}

	protected JMenu createNonLeaf(final ShadowMenu shadow) {
		final JMenu menu = new JMenu(shadow.getMenuEntry().getName());
		assignProperties(menu, shadow);
		return menu;
	}

	// -- Helper methods --

	private boolean isSelected(final ShadowMenu shadow) {
		return shadow.getInfo().isSelected();
	}

	private ButtonGroup getButtonGroup(final ShadowMenu shadow) {
		final String selectionGroup = shadow.getInfo().getSelectionGroup();
		ButtonGroup buttonGroup = buttonGroups.get(selectionGroup);
		if (buttonGroup == null) {
			buttonGroup = new ButtonGroup();
			buttonGroups.put(selectionGroup, buttonGroup);
		}
		return buttonGroup;
	}

	private KeyStroke getKeyStroke(final ShadowMenu shadow) {
		String accelerator = shadow.getMenuEntry().getAccelerator();
		if (accelerator != null) {
			// allow use of ^X to represent control X in keyboard accel parameters
			// NB: extra space REQUIRED
			accelerator = accelerator.replaceAll(Pattern.quote("^"), "control ");
			// on Mac, use Command instead of Control for keyboard shortcuts
			if (isMac() && accelerator.indexOf("meta") < 0) {
				// only if meta not already in use
				accelerator = accelerator.replaceAll("control", "meta");
			}
		}
		return KeyStroke.getKeyStroke(accelerator);
	}

	private Icon loadIcon(final ShadowMenu shadow) {
		final URL iconURL = shadow.getIconURL();
		return iconURL == null ? null : new ImageIcon(iconURL);
	}

	private void assignProperties(final JMenuItem menuItem,
		final ShadowMenu shadow)
	{
		final char mnemonic = shadow.getMenuEntry().getMnemonic();
		if (mnemonic != '\0') menuItem.setMnemonic(mnemonic);

		final KeyStroke keyStroke = getKeyStroke(shadow);
		if (keyStroke != null) menuItem.setAccelerator(keyStroke);

		final Icon icon = loadIcon(shadow);
		if (icon != null) menuItem.setIcon(icon);
	}

	private void linkAction(final ModuleInfo info, final JMenuItem menuItem) {
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				ImageJ.get(PluginService.class).run(info, true);
			}
		});
		menuItem.setEnabled(info.isEnabled());
	}

	private boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac");
	}

}
