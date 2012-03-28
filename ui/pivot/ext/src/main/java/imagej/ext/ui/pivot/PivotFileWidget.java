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

package imagej.ext.ui.pivot;

import imagej.ext.module.ui.FileWidget;
import imagej.ext.module.ui.WidgetModel;
import imagej.ext.module.ui.WidgetStyle;

import java.io.File;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FileBrowserSheet.Mode;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;

/**
 * Pivot implementation of file selector widget.
 * 
 * @author Curtis Rueden
 */
public class PivotFileWidget extends PivotInputWidget
	implements FileWidget, ButtonPressListener
{

	private final TextInput path;
	private final PushButton browse;

	public PivotFileWidget(final WidgetModel model) {
		super(model);

		path = new TextInput();
		add(path);

		browse = new PushButton("Browse");
		browse.getButtonPressListeners().add(this);
		add(browse);

		refreshWidget();
	}

	// -- FileWidget methods --

	@Override
	public File getValue() {
		return new File(path.getText());
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final File value = (File) getModel().getValue();
		final String text = value == null ? "" : value.toString();
		if (text.equals(path.getText())) return; // no change
		path.setText(text);
	}

	// -- ButtonPressListener methods --

	@Override
	public void buttonPressed(final Button b) {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}

		// display file chooser in appropriate mode
		final WidgetStyle style = getModel().getItem().getWidgetStyle();
		final FileBrowserSheet browser;
		if (style == WidgetStyle.FILE_SAVE) {
			browser = new FileBrowserSheet(Mode.SAVE_AS);
		}
		else { // default behavior
			browser = new FileBrowserSheet(Mode.OPEN);
		}
		browser.setSelectedFile(file);
		browser.open(path.getWindow());
		final boolean success = browser.getResult();
		if (!success) return;
		file = browser.getSelectedFile();
		if (file == null) return;

		path.setText(file.getAbsolutePath());
	}

}
