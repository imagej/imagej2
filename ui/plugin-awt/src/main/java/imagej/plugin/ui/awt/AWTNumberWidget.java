//
// AWTNumberWidget.java
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

import imagej.plugin.ui.NumberWidget;
import imagej.plugin.ui.ParamModel;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

/**
 * AWT implementation of number chooser widget.
 *
 * @author Curtis Rueden
 */
public class AWTNumberWidget extends AWTInputWidget
	implements NumberWidget, AdjustmentListener, TextListener
{

	private Scrollbar scrollBar;
	private TextField textField;

	public AWTNumberWidget(final ParamModel model,
		final Number min, final Number max, final Number stepSize)
	{
		super(model);

		scrollBar = new Scrollbar(Adjustable.HORIZONTAL,
			min.intValue(), 1, min.intValue(), max.intValue() + 1);
		scrollBar.setUnitIncrement(stepSize.intValue());
		scrollBar.addAdjustmentListener(this);
		add(scrollBar, BorderLayout.CENTER);

		textField = new TextField(6);
		textField.addTextListener(this);
		add(textField, BorderLayout.EAST);

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
 		final Number value = (Number) model.getValue();
		scrollBar.setValue(value.intValue());
		textField.setText(value.toString());
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		textField.setText("" + scrollBar.getValue());
	}

	// -- TextListener methods --

	@Override
	public void textValueChanged(TextEvent e) {
		try {
			scrollBar.setValue(Integer.parseInt(textField.getText()));
		}
		catch (NumberFormatException exc) {
			// invalid number in text field; do not update scroll bar
		}
	}

}
