/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.uis.awt.widget;

import imagej.widget.InputWidget;
import imagej.widget.NumberWidget;
import imagej.widget.WidgetModel;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import org.scijava.plugin.Plugin;

/**
 * AWT implementation of number chooser widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class AWTNumberWidget extends AWTInputWidget<Number> implements
	NumberWidget<Panel>, AdjustmentListener, TextListener
{

	// CTR FIXME - Update the model properly, and handle non-integer values.

	private Scrollbar scrollBar;
	private TextField textField;

	// -- InputWidget methods --

	@Override
	public Number getValue() {
		return scrollBar.getValue();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		final Number min = model.getMin();
		final Number max = model.getMax();
		final Number stepSize = model.getStepSize();

		scrollBar =
			new Scrollbar(Adjustable.HORIZONTAL, min.intValue(), 1, min.intValue(),
				max.intValue() + 1);
		scrollBar.setUnitIncrement(stepSize.intValue());
		scrollBar.addAdjustmentListener(this);
		getComponent().add(scrollBar, BorderLayout.CENTER);

		textField = new TextField(6);
		textField.addTextListener(this);
		getComponent().add(textField, BorderLayout.EAST);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isNumber();
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		textField.setText("" + scrollBar.getValue());
	}

	// -- TextListener methods --

	@Override
	public void textValueChanged(final TextEvent e) {
		try {
			scrollBar.setValue(Integer.parseInt(textField.getText()));
		}
		catch (final NumberFormatException exc) {
			// invalid number in text field; do not update scroll bar
		}
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final String value = get().getValue().toString();
		if (textField.getText().equals(value)) return; // no change
		textField.setText(value);
	}
}
