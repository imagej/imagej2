//
// SwingNumberScrollBarWidget.java
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

import imagej.plugin.ui.ParamDetails;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Swing implementation of number chooser widget, using a scroll bar.
 *
 * @author Curtis Rueden
 */
public class SwingNumberScrollBarWidget extends SwingNumberWidget
	implements AdjustmentListener, DocumentListener
{

	private JScrollBar scrollBar;
	private JTextField textField;

	private boolean textFieldUpdating;

	public SwingNumberScrollBarWidget(final ParamDetails details,
		final Number min, final Number max, final Number stepSize)
	{
		super(details);

		scrollBar = new JScrollBar(Adjustable.HORIZONTAL,
			min.intValue(), 1, min.intValue(), max.intValue() + 1);
		scrollBar.setUnitIncrement(stepSize.intValue());
		add(scrollBar, BorderLayout.CENTER);
		scrollBar.addAdjustmentListener(this);

		textField = new JTextField("", 6);
		add(textField, BorderLayout.EAST);
		textField.getDocument().addDocumentListener(this);

		refresh();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		return scrollBar.getValue();
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
 		final Number value = (Number) details.getValue();
		scrollBar.setValue(value.intValue());
		textField.setText(value.toString());
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		if (!textFieldUpdating) textField.setText("" + scrollBar.getValue());
		details.setValue(scrollBar.getValue());
	}

	// -- DocumentListener methods --

	@Override
	public void changedUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	// -- Helper methods --

	private void documentUpdate() {
		textFieldUpdating = true;
		try {
			scrollBar.setValue(Integer.parseInt(textField.getText()));
		}
		catch (NumberFormatException e) {
			// invalid number in text field; do not update scroll bar
		}
		textFieldUpdating = false;
	}

}
