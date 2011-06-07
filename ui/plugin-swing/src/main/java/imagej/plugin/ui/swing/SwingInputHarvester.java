//
// SwingInputHarvester.java
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

package imagej.plugin.ui.swing;

import imagej.plugin.MenuEntry;
import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.PluginModuleInfo;
import imagej.plugin.process.PluginPreprocessor;
import imagej.plugin.ui.AbstractInputHarvester;
import imagej.plugin.ui.InputPanel;
import imagej.util.awt.AWTWindows;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * SwingInputHarvester is a {@link PluginPreprocessor} that collects input
 * parameter values from the user using a {@link SwingInputPanel} dialog box.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = PluginPreprocessor.class)
public class SwingInputHarvester extends AbstractInputHarvester {

	// -- InputHarvester methods --

	@Override
	public SwingInputPanel createInputPanel() {
		return new SwingInputPanel();
	}

	@Override
	public boolean harvestInputs(final InputPanel inputPanel,
		final PluginModule<?> module)
	{
		final JOptionPane optionPane = new JOptionPane(null);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		final JDialog dialog =
			optionPane.createDialog(module.getInfo().getLabel());
		final String title = getTitle(module);
		dialog.setTitle(title);
		final JPanel mainPane = (JPanel) optionPane.getComponent(0);
		final JPanel widgetPane = (JPanel) mainPane.getComponent(0);
		// TODO - use JScrollPane in case there are many widgets
		widgetPane.add(((SwingInputPanel) inputPanel).getPanel());
		dialog.setModal(true);
		dialog.pack();
		AWTWindows.centerWindow(dialog);
		ensureDialogSizeReasonable(dialog);
		// TODO - open IJ2 from within Eclipse. Give any app focus. Then close
		// IJ2 app via red close box in its left corner. Quit dialog comes
		// up behind the other app. Is there a window event we should track
		// in IJ2 that could pull dialog in front of the other app?
		// #1 attempted fix of visibility/focus issue
		// dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		// #2 attempted fix of visibility/focus issue
		// if (!dialog.hasFocus())
		// dialog.requestFocus();
		final Integer rval = (Integer) optionPane.getValue();
		return rval != null && rval == JOptionPane.OK_OPTION;
	}

	// -- Helper methods --

	private String getTitle(final PluginModule<?> module) {
		final PluginModuleInfo<?> moduleInfo = module.getInfo();
		final String label = moduleInfo.getLabel();
		if (label != null && !label.isEmpty()) return label;
		final List<MenuEntry> menuPath = moduleInfo.getPluginEntry().getMenuPath();
		if (menuPath != null && menuPath.size() > 0) {
			final MenuEntry menuEntry = menuPath.get(menuPath.size() - 1);
			final String menuName = menuEntry.getName();
			if (menuName != null && !menuName.isEmpty()) return menuName;
		}
		final String name = moduleInfo.getName();
		if (name != null && !name.isEmpty()) return name;
		return "Choose Parameters";
	}

	private void ensureDialogSizeReasonable(final JDialog dialog) {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension dialogSize = dialog.getSize();

		int newWidth = dialogSize.width;
		int newHeight = dialogSize.height;

		final int maxWidth = 3 * screenSize.width / 4;
		final int maxHeight = 3 * screenSize.height / 4;

		if (newWidth > maxWidth) newWidth = maxWidth;
		if (newHeight > maxHeight) newHeight = maxHeight;

		dialog.setSize(newWidth, newHeight);
	}

}
