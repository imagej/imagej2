//
// PivotNumberWidget.java
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

package imagej.plugin.ui.pivot;

import imagej.plugin.ui.NumberWidget;
import imagej.plugin.ui.ParamDetails;
import imagej.plugin.ui.WidgetStyle;
import imagej.util.Log;

import org.apache.pivot.wtk.BoxPane;

/**
 * Pivot implementation of number chooser widget.
 *
 * @author Curtis Rueden
 */
public abstract class PivotNumberWidget
	extends BoxPane implements NumberWidget
{

	public static PivotNumberWidget create(final ParamDetails details,
		final Number min, final Number max, final Number stepSize,
		final WidgetStyle style)
	{
		if (style == WidgetStyle.NUMBER_SCROLL_BAR) {
			return new PivotNumberScrollBarWidget(details, min, max, stepSize);
		}
		if (style == WidgetStyle.NUMBER_SLIDER) {
			return new PivotNumberSliderWidget(details, min, max, stepSize);
		}
		if (style != WidgetStyle.DEFAULT && style != WidgetStyle.NUMBER_SPINNER) {
			Log.warn("Ignoring unsupported widget style: " + style);
		}
		return new PivotNumberSpinnerWidget(details, min, max, stepSize);
	}

}
