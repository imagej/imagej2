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

package imagej.ui.swing.widget;

import imagej.widget.InputWidget;
import imagej.widget.TextWidget;
import imagej.widget.WidgetModel;

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;

/**
 * Swing implementation of text field widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingTextWidget extends SwingInputWidget<String> implements
	DocumentListener, TextWidget<JPanel>
{

	private LogService log;

	private JTextComponent textComponent;

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

	// -- InputWidget methods --

	@Override
	public String getValue() {
		return textComponent.getText();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		log = model.getContext().getService(LogService.class);

		final int columns = model.getItem().getColumnCount();

		// construct text widget of the appropriate style, if specified
		final String style = model.getItem().getWidgetStyle();
		if (TextWidget.AREA_STYLE.equals(style)) {
			textComponent = new JTextArea("", 5, columns);
		}
		else if (TextWidget.PASSWORD_STYLE.equals(style)) {
			textComponent = new JPasswordField("", columns);
		}
		else {
			textComponent = new JTextField("", columns);
		}
		setToolTip(textComponent);
		getComponent().add(textComponent);
		limitLength();
		textComponent.getDocument().addDocumentListener(this);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isText() &&
			!model.isMultipleChoice() && !model.isMessage();
	}

	// -- Helper methods --

	private void limitLength() {
		// only limit length for single-character inputs
		if (!get().isCharacter()) return;

		// limit text field to a single character
		final int maxChars = 1;
		final Document doc = textComponent.getDocument();
		if (doc instanceof AbstractDocument) {
			final DocumentFilter docFilter = new DocumentSizeFilter(maxChars);
			((AbstractDocument) doc).setDocumentFilter(docFilter);
		}
		else if (log != null) {
			log.warn("Unknown document type: " + doc.getClass().getName());
		}
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final String text = get().getText();
		if (textComponent.getText().equals(text)) return; // no change
		textComponent.setText(text);
	}

}
