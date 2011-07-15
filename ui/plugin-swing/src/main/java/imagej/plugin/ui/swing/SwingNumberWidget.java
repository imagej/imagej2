//
// SwingNumberWidget.java
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

import imagej.module.ui.NumberWidget;
import imagej.module.ui.WidgetModel;
import imagej.module.ui.WidgetStyle;

import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Swing implementation of number chooser widget.
 * 
 * @author Curtis Rueden
 */
public class SwingNumberWidget extends SwingInputWidget implements
	NumberWidget, AdjustmentListener, ChangeListener
{

	private JScrollBar scrollBar;
	private JSlider slider;
	private final JSpinner spinner;

	public SwingNumberWidget(final WidgetModel model, final Number min,
		final Number max, final Number stepSize)
	{
		super(model);

		// add optional widgets, if specified
		final WidgetStyle style = model.getItem().getWidgetStyle();
		if (style == WidgetStyle.NUMBER_SCROLL_BAR) {
			scrollBar =
				new JScrollBar(Adjustable.HORIZONTAL, min.intValue(), 1, min
					.intValue(), max.intValue() + 1);
			scrollBar.setUnitIncrement(stepSize.intValue());
			setToolTip(scrollBar);
			add(scrollBar);
			scrollBar.addAdjustmentListener(this);
		}
		else if (style == WidgetStyle.NUMBER_SLIDER) {
			slider = new JSlider(min.intValue(), max.intValue(), min.intValue());
			slider.setMajorTickSpacing((max.intValue() - min.intValue()) / 4);
			slider.setMinorTickSpacing(stepSize.intValue());
			slider.setPaintLabels(true);
			slider.setPaintTicks(true);
			setToolTip(slider);
			add(slider);
			slider.addChangeListener(this);
		}

		final SpinnerNumberModel spinnerModel =
			new SpinnerNumberModel(min, (Comparable<?>) min, (Comparable<?>) max,
				stepSize);
		spinner = new JSpinner(spinnerModel);
		setToolTip(spinner);
		add(spinner);
		limitWidth(200);
		spinner.addChangeListener(this);

		refreshWidget();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		return (Number) spinner.getValue();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final Object value = getModel().getValue();
		if (spinner.getValue().equals(value)) return; // no change
		spinner.setValue(value);
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		// sync spinner with scroll bar value
		final int value = scrollBar.getValue();
		spinner.setValue(value);
	}

	// -- ChangeListener methods --

	@Override
	public void stateChanged(final ChangeEvent e) {
		final Object source = e.getSource();
		if (source == slider) {
			// sync spinner with slider value
			final int value = slider.getValue();
			spinner.setValue(value);
		}
		else if (source == spinner) {
			// sync slider and/or scroll bar with spinner value
			syncSliders();
		}
		updateModel();
	}

	// -- Helper methods --

	/**
	 * Limit component width to a certain maximum. This is a HACK to work around
	 * an issue with Double-based spinners that attempt to size themselves very
	 * large (presumably to match Double.MAX_VALUE).
	 */
	private void limitWidth(final int maxWidth) {
		final Dimension minSize = spinner.getMinimumSize();
		if (minSize.width > maxWidth) {
			minSize.width = maxWidth;
			spinner.setMinimumSize(minSize);
		}
		final Dimension prefSize = spinner.getPreferredSize();
		if (prefSize.width > maxWidth) {
			prefSize.width = maxWidth;
			spinner.setPreferredSize(prefSize);
		}
	}

	/** Sets slider values to match the spinner. */
	private void syncSliders() {
		if (slider != null) slider.setValue(getValue().intValue());
		if (scrollBar != null) scrollBar.setValue(getValue().intValue());
	}

}
