//
// PivotNumberScrollBarWidget.java
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

package imagej.plugin.gui.pivot;

import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.ScrollBar;
import org.apache.pivot.wtk.ScrollBarValueListener;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;

/**
 * Pivot implementation of number chooser widget, using a scroll bar.
 * 
 * @author Curtis Rueden
 */
public class PivotNumberScrollBarWidget extends PivotNumberWidget
	implements ScrollBarValueListener
{

	private ScrollBar scrollBar;
	private Label label;

	public PivotNumberScrollBarWidget(final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		scrollBar = new ScrollBar();
		scrollBar.setValue(initialValue.intValue());
		scrollBar.setRange(min.intValue(), max.intValue());
		add(scrollBar);
		label = new Label();
		label.setText(initialValue.toString());
		add(label);
		scrollBar.getScrollBarValueListeners().add(this);
	}

	@Override
	public Number getValue() {
		return scrollBar.getValue();
	}

	@Override
	public void valueChanged(ScrollBar s, int previousValue) {
		label.setText("" + scrollBar.getValue());
	}

}
