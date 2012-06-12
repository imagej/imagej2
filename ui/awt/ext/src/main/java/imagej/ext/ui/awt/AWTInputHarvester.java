/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ext.ui.awt;

import imagej.ext.Priority;
import imagej.ext.module.Module;
import imagej.ext.module.ui.InputHarvester;
import imagej.ext.module.ui.InputPanel;
import imagej.ext.plugin.AbstractInputHarvesterPlugin;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.process.PreprocessorPlugin;

import java.awt.Dimension;
import java.awt.Panel;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * AWTInputHarvester is an {@link InputHarvester} that collects input parameter
 * values from the user using an {@link AWTInputPanel} dialog box.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_LOW_PRIORITY)
public class AWTInputHarvester extends AbstractInputHarvesterPlugin {

	@Override
	public AWTInputPanel createInputPanel() {
		return new AWTInputPanel();
	}

	@Override
	public boolean
		harvestInputs(final InputPanel inputPanel, final Module module)
	{
		// TODO - use pure AWT instead of Swing here

		// convert input panel to Swing component
		final Panel pane = ((AWTInputPanel) inputPanel).getPanel();

		// display input panel in a dialog
		final String title = module.getInfo().getTitle();
		final boolean allowCancel = module.getInfo().canCancel();
		final int optionType, messageType;
		if (allowCancel) optionType = JOptionPane.OK_CANCEL_OPTION;
		else optionType = JOptionPane.DEFAULT_OPTION;
		if (inputPanel.isMessageOnly()) {
			if (allowCancel) messageType = JOptionPane.QUESTION_MESSAGE;
			else messageType = JOptionPane.INFORMATION_MESSAGE;
		}
		else messageType = JOptionPane.PLAIN_MESSAGE;
		final JOptionPane optionPane =
			new JOptionPane(pane, messageType, optionType);
		final JDialog dialog = optionPane.createDialog(title);
		dialog.setModal(true);
		dialog.setResizable(true);
		dialog.pack();
		ensureDialogSizeReasonable(dialog);
		dialog.setVisible(true);
		final Integer rval = (Integer) optionPane.getValue();
		dialog.dispose();

		// verify return value of dialog
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
