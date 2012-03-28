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

package imagej.ext.ui.swt;

import imagej.ext.Priority;
import imagej.ext.module.Module;
import imagej.ext.module.ui.InputHarvester;
import imagej.ext.module.ui.InputPanel;
import imagej.ext.plugin.AbstractInputHarvesterPlugin;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.process.PreprocessorPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * SWTInputHarvester is an {@link InputHarvester} that collects input
 * parameter values from the user using an {@link SWTInputPanel} dialog box.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_LOW_PRIORITY)
public class SWTInputHarvester extends AbstractInputHarvesterPlugin {

	private static final Display DISPLAY = new Display();

	@Override
	public SWTInputPanel createInputPanel() {
		return new SWTInputPanel(null);
	}

	@Override
	public boolean harvestInputs(final InputPanel inputPanel,
		final Module module)
	{
		// TODO - obtain handle on parent SWTMainFrame somehow
		final Shell dialog = new Shell(DISPLAY, SWT.DIALOG_TRIM);
		final Composite swtInputPanel = ((SWTInputPanel) inputPanel).getPanel();
		swtInputPanel.setParent(dialog);
		// TODO - use scroll bars in case there are many widgets
		final Button okButton = new Button(dialog, SWT.PUSH);
		okButton.setText("&OK");
		final Button cancelButton = new Button(dialog, SWT.PUSH);
		cancelButton.setText("&Cancel");

		final FormLayout form = new FormLayout();
		form.marginWidth = form.marginHeight = 8;
		dialog.setLayout(form);
		final FormData okData = new FormData();
		okData.top = new FormAttachment(swtInputPanel, 8);
		okButton.setLayoutData(okData);
		final FormData cancelData = new FormData();
		cancelData.left = new FormAttachment(okButton, 8);
		cancelData.top = new FormAttachment(okButton, 0, SWT.TOP);
		cancelButton.setLayoutData(cancelData);

		dialog.setDefaultButton(okButton);
		dialog.pack();
		dialog.open();

		// TODO - check whether OK or Cancel was pushed
		return true;
	}

}
