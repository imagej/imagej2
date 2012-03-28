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

package imagej.ext.ui.pivot;

import imagej.ext.module.ui.WidgetModel;
import imagej.util.NumberUtils;

import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ScrollBar;
import org.apache.pivot.wtk.ScrollBarValueListener;

/**
 * Pivot implementation of number chooser widget, using a scroll bar.
 * 
 * @author Curtis Rueden
 */
public class PivotNumberScrollBarWidget extends PivotNumberWidget
	implements ScrollBarValueListener
{

	private final ScrollBar scrollBar;
	private final Label label;

	public PivotNumberScrollBarWidget(final WidgetModel model,
		final Number min, final Number max, final Number stepSize)
	{
		super(model);

		scrollBar = new ScrollBar();
		scrollBar.setRange(min.intValue(), max.intValue());
		scrollBar.setBlockIncrement(stepSize.intValue());
		add(scrollBar);
		scrollBar.getScrollBarValueListeners().add(this);

		label = new Label();
		add(label);

		refreshWidget();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		final String value = "" + scrollBar.getValue();
		return NumberUtils.toNumber(value, getModel().getItem().getType());
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final Number value = (Number) getModel().getValue();
		scrollBar.setValue(value.intValue());
		label.setText(value.toString());
	}

	// -- ScrollBarValueListener methods --

	@Override
	public void valueChanged(final ScrollBar s, final int previousValue) {
		label.setText("" + scrollBar.getValue());
	}

}
