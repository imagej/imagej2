/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.pivot.widget;

import imagej.plugin.Plugin;
import imagej.widget.InputWidget;
import imagej.widget.TextFieldWidget;
import imagej.widget.WidgetModel;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.TextInput;

/**
 * Pivot implementation of text field widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class PivotTextFieldWidget extends PivotInputWidget<String> implements
	TextFieldWidget<BoxPane>
{

	private TextInput textInput;

	// -- InputWidget methods --

	@Override
	public boolean isCompatible(final WidgetModel model) {
		return super.isCompatible(model) && model.isText() &&
			!model.isMultipleChoice() && !model.isMessage();
	}

	@Override
	public void initialize(final WidgetModel model) {
		super.initialize(model);

		textInput = new TextInput();
		getComponent().add(textInput);

		refreshWidget();
	}

	@Override
	public String getValue() {
		return textInput.getText();
	}

	@Override
	public void refreshWidget() {
		final String text = getModel().getText();
		if (textInput.getText().equals(text)) return; // no change
		textInput.setText(text);
	}

}
