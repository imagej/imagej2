//
// DimensionSliderPanel.java
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

package imagej.display.view;

import java.awt.Adjustable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

//TODO - eliminate dependencies on AWT and Swing

/**
 * TODO
 * 
 * @author Grant Harris
 */
public class DimensionSliderPanel extends JPanel {

	/*
	 * CompositeSliderPanel
	 * If there is a channel dimension is displayed as a composite, 
	 * a slider for that dim should not be added.
	 */

	public DimensionSliderPanel(final DatasetView view) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// setPreferredSize(new Dimension(200, 18));
		// add one slider per dimension beyond the first two
		// System.out.println("Adding sliders, " + view.getImg().numDimensions());
		for (int d = 2; d < view.getImg().numDimensions(); d++) {
			final int dim = d;
			final String label = view.getImg().axis(d).getLabel();
			// System.out.println("d = " + d);
			final int dimLength = (int) view.getImg().dimension(d);

			final JScrollBar bar =
				new JScrollBar(Adjustable.HORIZONTAL, 0, 1, 0, dimLength);
			// bar.setPreferredSize(new Dimension(500,32));
			bar.addAdjustmentListener(new AdjustmentListener() {

				@Override
				public void adjustmentValueChanged(final AdjustmentEvent e) {
					final int value = bar.getValue();
					// System.out.println("dim #" + dim + ": value->" + value);//TEMP
					view.setPosition(value, dim);
					// view.project();
				}
			});
			add(bar);
		}
	}

}
