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

package imagej.plugins.uis.pivot.widget;

import imagej.widget.InputWidget;
import imagej.widget.NumberWidget;
import imagej.widget.WidgetModel;

import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ScrollBar;
import org.apache.pivot.wtk.ScrollBarValueListener;
import org.scijava.plugin.Plugin;
import org.scijava.util.NumberUtils;

/**
 * Pivot implementation of number chooser widget, using a scroll bar.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class PivotNumberScrollBarWidget extends PivotNumberWidget implements
	ScrollBarValueListener
{

	private ScrollBar scrollBar;
	private Label label;

	// -- InputWidget methods --

	@Override
	public Number getValue() {
		final String value = "" + scrollBar.getValue();
		return NumberUtils.toNumber(value, get().getItem().getType());
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		final Number min = model.getMin();
		final Number max = model.getMax();
		final Number stepSize = model.getStepSize();

		scrollBar = new ScrollBar();
		scrollBar.setRange(min.intValue(), max.intValue());
		scrollBar.setBlockIncrement(stepSize.intValue());
		getComponent().add(scrollBar);
		scrollBar.getScrollBarValueListeners().add(this);

		label = new Label();
		getComponent().add(label);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		final String style = model.getItem().getWidgetStyle();
		if (!NumberWidget.SCROLL_BAR_STYLE.equals(style)) return false;
		return super.supports(model);
	}

	// -- ScrollBarValueListener methods --

	@Override
	public void valueChanged(final ScrollBar s, final int previousValue) {
		label.setText("" + scrollBar.getValue());
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final Number value = (Number) get().getValue();
		scrollBar.setValue(value.intValue());
		label.setText(value.toString());
	}
}
