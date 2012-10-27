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

package imagej.ui.awt.widget;

import imagej.plugin.Plugin;
import imagej.widget.FileWidget;
import imagej.widget.InputWidget;
import imagej.widget.WidgetModel;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.File;

/**
 * AWT implementation of file selector widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class AWTFileWidget extends AWTInputWidget<File> implements
	FileWidget<Panel>, ActionListener, TextListener
{

	private TextField path;
	private Button browse;

	// -- InputWidget methods --

	@Override
	public boolean isCompatible(final WidgetModel model) {
		return super.isCompatible(model) && model.isType(File.class);
	}

	@Override
	public void initialize(final WidgetModel model) {
		super.initialize(model);

		getComponent().setLayout(new BorderLayout());

		path = new TextField(20);
		path.addTextListener(this);
		getComponent().add(path, BorderLayout.CENTER);

		browse = new Button("Browse");
		browse.addActionListener(this);
		getComponent().add(browse, BorderLayout.EAST);

		refreshWidget();
	}

	@Override
	public File getValue() {
		final String text = path.getText();
		return text.isEmpty() ? null : new File(text);
	}

	@Override
	public void refreshWidget() {
		final String text = getModel().getText();
		if (text.equals(path.getText())) return; // no change
		path.setText(text);
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}

		// display file chooser in appropriate mode
		final String style = getModel().getItem().getWidgetStyle();
		final FileDialog fileDialog = new FileDialog((Frame) null);
		if (FileWidget.SAVE_STYLE.equals(style)) {
			fileDialog.setMode(FileDialog.SAVE);
		}
		else { // default behavior
			fileDialog.setMode(FileDialog.LOAD);
		}
		fileDialog.setDirectory(file.getAbsolutePath());
		fileDialog.setVisible(true);
		final String filename = fileDialog.getFile();
		fileDialog.dispose();
		if (filename == null) return;

		path.setText(filename);
	}

	// -- TextListener methods --

	@Override
	public void textValueChanged(final TextEvent e) {
		updateModel();
	}

}
