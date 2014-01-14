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

package imagej.plugins.uis.swing.widget;

import imagej.widget.InputWidget;
import imagej.widget.NumberWidget;
import imagej.widget.WidgetModel;

import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParsePosition;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.plugin.Plugin;
import org.scijava.util.NumberUtils;

/**
 * Swing implementation of number chooser widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingNumberWidget extends SwingInputWidget<Number> implements
	NumberWidget<JPanel>, AdjustmentListener, ChangeListener
{

	private JScrollBar scrollBar;
	private JSlider slider;
	private JSpinner spinner;

	// -- InputWidget methods --

	@Override
	public Number getValue() {
		return (Number) spinner.getValue();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		final Number min = model.getMin();
		final Number max = model.getMax();
		final Number softMin = model.getSoftMin();
		final Number softMax = model.getSoftMax();
		final Number stepSize = model.getStepSize();

		// add optional widgets, if specified
		final String style = model.getItem().getWidgetStyle();
		if (NumberWidget.SCROLL_BAR_STYLE.equals(style)) {
			int smx = softMax.intValue();
			if (smx < Integer.MAX_VALUE) smx++;
			scrollBar =
				new JScrollBar(Adjustable.HORIZONTAL, softMin.intValue(), 1, softMin
					.intValue(), smx);
			scrollBar.setUnitIncrement(stepSize.intValue());
			setToolTip(scrollBar);
			getComponent().add(scrollBar);
			scrollBar.addAdjustmentListener(this);
		}
		else if (NumberWidget.SLIDER_STYLE.equals(style)) {
			slider =
				new JSlider(softMin.intValue(), softMax.intValue(), softMin.intValue());
			slider.setMajorTickSpacing((softMax.intValue() - softMin.intValue()) / 4);
			slider.setMinorTickSpacing(stepSize.intValue());
			slider.setPaintLabels(true);
			slider.setPaintTicks(true);
			setToolTip(slider);
			getComponent().add(slider);
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
		getComponent().add(spinner);
		limitWidth(200);
		spinner.addChangeListener(this);
		refreshWidget();
		syncSliders();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isNumber();
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
		if (slider != null) {
			// clamp value within slider bounds
			int value = getValue().intValue();
			if (value < slider.getMinimum()) value = slider.getMinimum();
			else if (value > slider.getMaximum()) value = slider.getMaximum();
			slider.removeChangeListener(this);
			slider.setValue(value);
			slider.addChangeListener(this);
		}
		if (scrollBar != null) {
			// clamp value within scroll bar bounds
			int value = getValue().intValue();
			if (value < scrollBar.getMinimum()) value = scrollBar.getMinimum();
			else if (value > scrollBar.getMaximum()) value = scrollBar.getMaximum();
			scrollBar.removeAdjustmentListener(this);
			scrollBar.setValue(getValue().intValue());
			scrollBar.addAdjustmentListener(this);
		}
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final Object value = get().getValue();
		if (spinner.getValue().equals(value)) return; // no change
		spinner.setValue(value);
	}
}
