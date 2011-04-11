//
// AWTInputHarvester.java
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

package imagej.plugin.ui.awt;

import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.ui.AbstractInputHarvester;
import imagej.plugin.ui.InputPanel;
import imagej.plugin.process.PluginPreprocessor;

import java.awt.Dimension;
import java.awt.Toolkit;

// TODO - eliminate Swing dependency
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * AWTInputHarvester is a plugin preprocessor that collects input parameter
 * values from the user using an {@link AWTInputPanel} dialog box.
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = PluginPreprocessor.class)
public class AWTInputHarvester extends AbstractInputHarvester {

	@Override
	public AWTInputPanel createInputPanel() {
		return new AWTInputPanel();
	}

	@Override
	public boolean harvestInputs(final InputPanel inputPanel,
		final PluginModule<?> module)
	{
		final JOptionPane optionPane = new JOptionPane(null);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		final JDialog dialog = optionPane.createDialog(module.getInfo().getLabel());
		final JPanel mainPane = (JPanel) optionPane.getComponent(0);
		final JPanel widgetPane = (JPanel) mainPane.getComponent(0);
		// TODO - use ScrollPane in case there are many widgets
		widgetPane.add(((AWTInputPanel) inputPanel).getPanel());
		dialog.setModal(true);
		dialog.pack();
		ensureDialogSizeReasonable(dialog);
		dialog.setVisible(true);
		final Integer rval = (Integer) optionPane.getValue();
		return rval != null && rval == JOptionPane.OK_OPTION;
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
