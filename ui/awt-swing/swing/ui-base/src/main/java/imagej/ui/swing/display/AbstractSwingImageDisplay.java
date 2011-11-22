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

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.AbstractImageDisplay;
import imagej.data.display.DataView;
import imagej.data.roi.Overlay;
import imagej.event.EventHandler;
import imagej.ext.display.DisplayService;
import imagej.ext.display.DisplayWindow;
import imagej.options.OptionsService;
import imagej.options.event.OptionsEvent;
import imagej.options.plugins.OptionsAppearance;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTMouseEventDispatcher;

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
	private ScaleConverter scaleConverter;

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

		scaleConverter = getScaleConverter();
	}

	// -- ImageDisplay methods --

	@Override
	public void display(final Dataset dataset) {
		// GBH: Regarding naming/id of the display...
		// For now, we will use the original (first) dataset name
		final String datasetName = dataset.getName();
		createName(datasetName);
		window.setTitle(this.getName());
		add(new SwingDatasetView(this, dataset));
		update();
		initActiveAxis();
	}

	@Override
	public void display(final Overlay overlay) {
		add(new SwingOverlayView(this, overlay));
		update();
		initActiveAxis();
	}

	@Override
	public JHotDrawImageCanvas getCanvas() {
		return imgCanvas;
	}

	@Override
	public String makeLabel() {
		// CTR TODO - Fix window label to show beyond just the active view.
		final DataView view = getActiveView();
		final Dataset dataset = getDataset(view);

		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		final long[] dims = dataset.getDims();
		final Axis[] axes = dataset.getAxes();
		final Position pos = view.getPlanePosition();

		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (Axes.isXY(axes[i])) continue;
			p++;
			if (dims[i] == 1) continue;
			sb.append(axes[i] + ": " + (pos.getLongPosition(p) + 1) + "/" + dims[i] +
				"; ");
		}

		sb.append(dims[xIndex] + "x" + dims[yIndex] + "; ");

		sb.append(dataset.getTypeLabelLong());

		final double zoomFactor = getCanvas().getZoomFactor();
		if (zoomFactor != 1) sb.append(" (" + scaleConverter.getString(zoomFactor) +
			")");

		return sb.toString();
	}

	// -- Display methods --

	@Override
	public SwingDisplayPanel getPanel() {
		return imgPanel;
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onEvent(OptionsEvent e) {
		scaleConverter = getScaleConverter();
	}
	
	// -- Helper methods --


	@SuppressWarnings("synthetic-access")
	private ScaleConverter getScaleConverter() {
		OptionsService service = ImageJ.get(OptionsService.class);
		OptionsAppearance options = service.getOptions(OptionsAppearance.class);
		
		if (options.isDisplayFractionalScales())
			return new FractionalScaleConverter();

		return new PercentScaleConverter();
	}

	// -- Helper classes --

	private interface ScaleConverter {

		String getString(double realScale);
	}

	private class PercentScaleConverter implements ScaleConverter {

		@Override
		public String getString(final double realScale) {
			return String.format("%.2f%%", realScale * 100);
		}

	}

	private class FractionalScaleConverter implements ScaleConverter {

		@Override
		public String getString(final double realScale) {
			final FractionalScale fracScale = new FractionalScale(realScale);
			// is fractional scale invalid?
			if (fracScale.getDenom() == 0) {
				if (realScale >= 1)
					return String.format("%.2fX", realScale);
				// else scale < 1
				return String.format("1/%.2fX", (1 / realScale));
			}
			// or do we have a whole number scale?
			else if (fracScale.getDenom() == 1)
				return String.format("%dX", fracScale.getNumer());
			// else have valid fraction
			return
				String.format("%d/%dX", fracScale.getNumer(), fracScale.getDenom());
		}
	}

	private class FractionalScale {

		private int numer, denom;

		FractionalScale(final double realScale) {
			numer = 0;
			denom = 0;
			if (realScale >= 1) {
				final double floor = Math.floor(realScale);
				if ((realScale - floor) < 0.0001) {
					numer = (int) floor;
					denom = 1;
				}
				else chooseBestFraction(realScale);
			}
			else { // factor < 1
				final double recip = 1.0 / realScale;
				final double floor = Math.floor(recip);
				if ((recip - floor) < 0.0001) {
					numer = 1;
					denom = (int) floor;
				}
				else chooseBestFraction(realScale);
			}
		}

		int getNumer() {
			return numer;
		}

		int getDenom() {
			return denom;
		}
		
		private void chooseBestFraction(final double scale) {
			for (int n = 1; n < 30; n++) {
				for (int d = 1; d < 30; d++) {
					double frac = 1.0 * n / d;
					if (Math.abs(scale-frac) < 0.0001) {
						numer = n;
						denom = d;
						return;
					}
				}
			}
		}
	}

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
