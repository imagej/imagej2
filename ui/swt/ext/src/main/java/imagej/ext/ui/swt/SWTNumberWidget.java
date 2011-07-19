//
// SWTNumberWidget.java
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

package imagej.ext.ui.swt;

import imagej.ext.module.ui.NumberWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;

/**
 * SWT implementation of number chooser widget.
 * 
 * @author Curtis Rueden
 */
public class SWTNumberWidget extends SWTInputWidget implements NumberWidget {

	private final Slider slider;

	public SWTNumberWidget(final Composite parent, final WidgetModel model,
		final Number min, final Number max, final Number stepSize)
	{
		super(parent, model);

		slider = new Slider(this, SWT.HORIZONTAL);
		slider.setValues(min.intValue(), min.intValue(), max.intValue(),
			stepSize.intValue(), stepSize.intValue(), 10 * stepSize.intValue());

		refreshWidget();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		return slider.getSelection();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final int value = ((Number) getModel().getValue()).intValue();
		if (slider.getSelection() == value) return; // no change
		slider.setSelection(value);
	}

}
