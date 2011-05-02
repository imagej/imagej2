//
// SwingNumberSpinnerWidget.java
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

import imagej.plugin.ui.ParamModel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Swing implementation of number chooser widget, using a spinner.
 * 
 * @author Curtis Rueden
 */
public class SwingNumberSpinnerWidget extends SwingNumberWidget
	implements ChangeListener
{

	private final JSpinner spinner;

	public SwingNumberSpinnerWidget(final ParamModel model,
		final Number min, final Number max, final Number stepSize)
	{
		super(model);

		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(min,
			(Comparable<?>) min, (Comparable<?>) max, stepSize);
		spinner = new JSpinner(spinnerModel);
		add(spinner, BorderLayout.CENTER);
		limitWidth(250);
		spinner.addChangeListener(this);

		refresh();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		return (Number) spinner.getValue();
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
		final Object value = model.getValue();
		if (value != null) spinner.setValue(value);
	}

	// -- ChangeListener methods --

	@Override
	public void stateChanged(final ChangeEvent e) {
		model.setValue(spinner.getValue());
	}

	// -- Helper methods --

	/**
	 * Limit component width to a certain maximum. This is a HACK to work around
	 * an issue with Double-based spinners that attempt to size themselves very
	 * large (presumably to match Double.MAX_VALUE).
	 */
	private void limitWidth(final int maxWidth) {
		final Dimension spinnerSize = spinner.getPreferredSize();
		if (spinnerSize.width > maxWidth) {
			spinnerSize.width = maxWidth;
			spinner.setPreferredSize(spinnerSize);
			spinner.setMaximumSize(spinnerSize);
			final Dimension widgetSize = getPreferredSize();
			widgetSize.width = spinnerSize.width;
			setPreferredSize(widgetSize);
			setMaximumSize(widgetSize);
		}
	}

}
