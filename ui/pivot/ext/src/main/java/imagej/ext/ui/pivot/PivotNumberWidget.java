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

import imagej.ext.module.ui.NumberWidget;
import imagej.ext.module.ui.WidgetModel;
import imagej.ext.module.ui.WidgetStyle;
import imagej.util.Log;

/**
 * Pivot implementation of number chooser widget.
 *
 * @author Curtis Rueden
 */
public abstract class PivotNumberWidget
	extends PivotInputWidget implements NumberWidget
{

	public PivotNumberWidget(final WidgetModel model) {
		super(model);
	}

	public static PivotNumberWidget create(final WidgetModel model,
		final Number min, final Number max, final Number stepSize,
		final WidgetStyle style)
	{
		if (style == WidgetStyle.NUMBER_SCROLL_BAR) {
			return new PivotNumberScrollBarWidget(model, min, max, stepSize);
		}
		if (style == WidgetStyle.NUMBER_SLIDER) {
			return new PivotNumberSliderWidget(model, min, max);
		}
		if (style != WidgetStyle.DEFAULT && style != WidgetStyle.NUMBER_SPINNER) {
			Log.warn("Ignoring unsupported widget style: " + style);
		}
		return new PivotNumberSpinnerWidget(model, min, max, stepSize);
	}

}
