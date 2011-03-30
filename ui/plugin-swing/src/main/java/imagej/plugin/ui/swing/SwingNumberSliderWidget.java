//
// SwingNumberSliderWidget.java
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

import java.awt.BorderLayout;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Swing implementation of number chooser widget, using a slider.
 *
 * @author Curtis Rueden
 */
public class SwingNumberSliderWidget extends SwingNumberWidget implements ChangeListener {

	private JSlider slider;

	public SwingNumberSliderWidget(final ParamDetails details,
		final Number min, final Number max, final Number stepSize)
	{
		super(details);

		slider = new JSlider(min.intValue(), max.intValue(), min.intValue());
		slider.setMajorTickSpacing((max.intValue() - min.intValue()) / 4);
		slider.setMinorTickSpacing(stepSize.intValue());
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		add(slider, BorderLayout.CENTER);
		slider.addChangeListener(this);

		refresh();
	}

	// -- ChangeListener methods --

	@Override
	public void stateChanged(ChangeEvent e) {
		details.setValue(slider.getValue());
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		return slider.getValue();
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
		final Number value = (Number) details.getValue();
		slider.setValue(value.intValue());
	}

}
