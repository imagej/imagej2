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

package imagej.ext.ui.swing;

import imagej.ext.module.ui.TextFieldWidget;
import imagej.ext.module.ui.WidgetModel;
import imagej.util.ClassUtils;
import imagej.util.Log;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Swing implementation of text field widget.
 * 
 * @author Curtis Rueden
 */
public class SwingTextFieldWidget extends SwingInputWidget implements
	DocumentListener, TextFieldWidget
{

	private final JTextField textField;

	public SwingTextFieldWidget(final WidgetModel model, final int columns) {
		super(model);

		textField = new JTextField("", columns);
		setToolTip(textField);
		add(textField);
		limitLength();
		textField.getDocument().addDocumentListener(this);

		refreshWidget();
	}

	// -- DocumentListener methods --

	@Override
	public void changedUpdate(final DocumentEvent e) {
		updateModel();
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		updateModel();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		updateModel();
	}

	// -- TextFieldWidget methods --

	@Override
	public String getValue() {
		return textField.getText();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final Object value = getModel().getValue();
		String text = value == null ? "" : value.toString();
		if (text.equals("\0")) text = ""; // render null character as empty
		if (textField.getText().equals(text)) return; // no change
		textField.setText(text);
	}

	// -- Helper methods --

	private void limitLength() {
		// only limit length for single-character inputs
		if (!ClassUtils.isCharacter(getModel().getItem().getType())) return;

		// limit text field to a single character
		final int maxChars = 1;
		final Document doc = textField.getDocument();
		if (doc instanceof AbstractDocument) {
			final DocumentFilter docFilter = new DocumentSizeFilter(maxChars);
			((AbstractDocument) doc).setDocumentFilter(docFilter);
		}
		else Log.warn("Unknown document type: " + doc.getClass().getName());
	}

}
