//
// AbstractSwingImageDisplay.java
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

package imagej.ui.swing.display;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.AbstractImageDisplay;
import imagej.data.roi.Overlay;
import imagej.ext.display.DisplayService;
import imagej.ext.display.DisplayWindow;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTMouseEventDispatcher;
import net.imglib2.img.Axis;

/**
 * A Swing image display plugin, which displays 2D planes in grayscale or
 * composite color. Intended to be subclassed by a concrete implementation that
 * provides a {@link DisplayWindow} in which the display should be housed.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
public abstract class AbstractSwingImageDisplay extends AbstractImageDisplay {

	protected final DisplayWindow window;
	private final JHotDrawImageCanvas imgCanvas;
	private final SwingDisplayPanel imgPanel;

	public AbstractSwingImageDisplay(final DisplayWindow window) {
		super();
		this.window = window;

		imgCanvas = new JHotDrawImageCanvas(this);
		imgCanvas.addEventDispatcher(new AWTKeyEventDispatcher(this, eventService));
		imgCanvas.addEventDispatcher(new AWTMouseEventDispatcher(this,
			eventService, false));
		setCanvas(imgCanvas);

		imgPanel = new SwingDisplayPanel(this, window);
		setPanel(imgPanel);
	}

	// -- ImageDisplay methods --

	@Override
	public long getAxisPosition(final Axis axis) {
		// FIXME
		return imgPanel.getAxisPosition(axis);
	}

	@Override
	public void setAxisPosition(final Axis axis, final long position) {
		// FIXME
		imgPanel.setAxisPosition(axis, position);
	}

	@Override
	public void display(final Dataset dataset) {
		// GBH: Regarding naming/id of the display...
		// For now, we will use the original (first) dataset name
		final String datasetName = dataset.getName();
		createName(datasetName);
		window.setTitle(this.getName());
		add(new SwingDatasetView(this, dataset));
		initActiveAxis();
		redoWindowLayout();
		update();
	}

	@Override
	public void display(final Overlay overlay) {
		add(new SwingOverlayView(this, overlay));
		initActiveAxis();
		redoWindowLayout();
		update();
	}

	@Override
	public JHotDrawImageCanvas getCanvas() {
		return imgCanvas;
	}

	// -- Display methods --

	@Override
	public SwingDisplayPanel getPanel() {
		return imgPanel;
	}

	// -- Helper methods --

	/** Name this display with unique id. */
	private void createName(final String baseName) {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		String theName = baseName;
		int n = 0;
		while (!displayService.isUniqueName(theName)) {
			n++;
			theName = baseName + "-" + n;
		}
		this.setName(theName);
	}

}
