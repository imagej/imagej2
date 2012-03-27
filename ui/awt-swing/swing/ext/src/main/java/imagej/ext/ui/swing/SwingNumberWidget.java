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

import imagej.ext.module.ui.NumberWidget;
import imagej.ext.module.ui.WidgetModel;
import imagej.ext.module.ui.WidgetStyle;
import imagej.util.NumberUtils;

import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParsePosition;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
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
				new JScrollBar(Adjustable.HORIZONTAL, min.intValue(), 1,
					min.intValue(), max.intValue() + 1);
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

		// add spinner widget
		final Class<?> type = model.getItem().getType();
		if (model.getValue() == null) {
			final Number defaultValue = NumberUtils.getDefaultValue(min, max, type);
			model.setValue(defaultValue);
		}
		final Number value = (Number) model.getValue();
		final SpinnerNumberModel spinnerModel =
			new SpinnerNumberModelFactory().createModel(value, min, max, stepSize);
		spinner = new JSpinner(spinnerModel);
		fixSpinner(type);
		setToolTip(spinner);
		add(spinner);
		limitWidth(200);
		spinner.addChangeListener(this);
		refreshWidget();
		syncSliders();
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

	/**
	 * Fixes spinners that display {@link BigDecimal} or {@link BigInteger}
	 * values. This is a HACK to work around the fact that
	 * {@link DecimalFormat#parse(String, ParsePosition)} uses {@link Double}
	 * and/or {@link Long} by default, hence losing precision.
	 */
	private void fixSpinner(final Class<?> type) {
		if (BigDecimal.class.isAssignableFrom(type) ||
			BigInteger.class.isAssignableFrom(type))
		{
			final JComponent editor = spinner.getEditor();
			final JSpinner.NumberEditor numberEditor = (JSpinner.NumberEditor) editor;
			final DecimalFormat decimalFormat = numberEditor.getFormat();
			decimalFormat.setParseBigDecimal(true);
		}
	}

	/** Sets slider values to match the spinner. */
	private void syncSliders() {
		if (slider != null) slider.setValue(getValue().intValue());
		if (scrollBar != null) scrollBar.setValue(getValue().intValue());
	}

}
