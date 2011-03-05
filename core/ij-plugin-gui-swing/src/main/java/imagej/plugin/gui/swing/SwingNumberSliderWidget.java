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

package imagej.plugin.gui.swing;

import java.awt.BorderLayout;

import javax.swing.JSlider;

/**
 * Swing implementation of number chooser widget, using a slider.
 *
 * @author Curtis Rueden
 */
public class SwingNumberSliderWidget extends SwingNumberWidget {

	private JSlider slider;

	public SwingNumberSliderWidget(final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		slider = new JSlider(min.intValue(), max.intValue(),
			initialValue.intValue());
		slider.setMajorTickSpacing((max.intValue() - min.intValue()) / 4);
		slider.setMinorTickSpacing(stepSize.intValue());
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		add(slider, BorderLayout.CENTER);
	}

	@Override
	public Number getValue() {
		return slider.getValue();
	}

}
