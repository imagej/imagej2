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

package imagej.plugin.gui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Swing implementation of number chooser widget, using a spinner.
 *
 * @author Curtis Rueden
 */
public class SwingNumberSpinnerWidget extends SwingNumberWidget {

	private JSpinner spinner;

	public SwingNumberSpinnerWidget(final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
			initialValue, (Comparable<?>) min, (Comparable<?>) max, stepSize);
		spinner = new JSpinner(spinnerModel);
		limitWidth(spinner, 300);
		add(spinner, BorderLayout.CENTER);
	}

	@Override
	public Number getValue() {
		return (Number) spinner.getValue();
	}

	/**
	 * Limit component width to a certain maximum.
	 *
	 * This is a HACK to work around an issue with Double-based spinners
	 * that attempt to size themselves very large (presumably to match
	 * Double.MAX_VALUE).
	 */
	private void limitWidth(final JComponent c, final int maxWidth) {
		final Dimension prefSize = c.getPreferredSize();
		if (prefSize.width > maxWidth) prefSize.width = maxWidth;
		c.setMaximumSize(prefSize);
	}

}
