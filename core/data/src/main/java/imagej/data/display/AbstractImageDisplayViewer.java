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
package imagej.data.display;

import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.event.ZoomEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.display.AbstractDisplayViewer;
import imagej.ext.display.Display;
import imagej.ext.display.DisplayWindow;
import imagej.ext.display.event.window.WinActivatedEvent;
import imagej.ext.tool.ToolService;
import imagej.util.UnitUtils;

/**
 * @author Lee Kamentsky
 *
 * The AbstractImageDisplayViewer implements the gui-independent
 * elements of an image display viewer. It subscribes to the
 * events of its controlled display and distills these into
 * abstract lifecycle actions.
 */
public abstract class AbstractImageDisplayViewer extends AbstractDisplayViewer<DataView> implements ImageDisplayViewer {
	protected enum ZoomScaleOption {
		OPTIONS_PERCENT_SCALE,
		OPTIONS_FRACTIONAL_SCALE
	};
	
	protected ImageCanvas canvas;
	protected EventService eventService;
	protected List<EventSubscriber<?>> subscribers;
	
	public AbstractImageDisplayViewer() {
	}
	@Override
	public boolean canView(Display<?> display) {
		return display instanceof ImageDisplay;
	}
	
	@Override
	public void view(DisplayWindow window, Display<?> display) {
		super.view(window, display);
		this.window = window;
		assert display instanceof ImageDisplay;
		eventService = display.getContext().getService(EventService.class);
		subscribers = eventService.subscribe(this);
	}
	
	@Override
	public ImageDisplay getImageDisplay() {
		assert getDisplay() instanceof ImageDisplay;
		return (ImageDisplay)getDisplay();
	}
	
	//-- Helper methods --//
	/**
	 * Make some informative label text by inspecting the views.
	 * @return
	 */
	public String makeLabel() {
		// CTR TODO - Fix window label to show beyond just the active view.
		final DataView view = getImageDisplay().getActiveView();
		final Dataset dataset = getDataset(view);

		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		final long[] dims = dataset.getDims();
		final AxisType[] axes = dataset.getAxes();
		final Position pos = view.getPlanePosition();

		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (Axes.isXY(axes[i])) continue;
			p++;
			if (dims[i] == 1) continue;
			sb.append(axes[i]);
			sb.append(": ");
			sb.append(pos.getLongPosition(p) + 1);
			sb.append("/");
			sb.append(dims[i]);
			sb.append("; ");
		}

		sb.append(dims[xIndex]);
		sb.append("x");
		sb.append(dims[yIndex]);
		sb.append("; ");

		sb.append(dataset.getTypeLabelLong());
		sb.append("; ");
		
		sb.append(byteInfoString(dataset));
		sb.append("; ");

		final double zoomFactor = getImageDisplay().getCanvas().getZoomFactor();
		if (zoomFactor != 1) {
			sb.append("(");
			sb.append(getScaleConverter().getString(zoomFactor));
			sb.append(")");
		}

		return sb.toString();
	}
	
	protected Dataset getDataset(final DataView view) {
		final Data data = view.getData();
		return data instanceof Dataset ? (Dataset) data : null;
	}
	// -- Helper methods --

	private String byteInfoString(Dataset ds) {
		final double byteCount = ds.getBytesOfInfo();
		return UnitUtils.getAbbreviatedByteLabel(byteCount);
	}

	/**
	 * Implement this in the derived class to get the user's
	 * preference for displaying zoom scale (as a fraction or percent)
	 * 
	 * @return ZoomScaleOption.OPTION_PERCENT_SCALE or ZoomScaleOption.OPTION_FRACTIONAL_SCALE
	 */
	protected abstract ZoomScaleOption getZoomScaleOption();
	
	private ScaleConverter getScaleConverter() {

		if (getZoomScaleOption().equals(ZoomScaleOption.OPTIONS_FRACTIONAL_SCALE))
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
			final FractionalScale scale = new FractionalScale(realScale);
			// is fractional scale invalid?
			if (scale.getDenom() == 0) {
				if (realScale >= 1) return String.format("%.2fX", realScale);
				// else scale < 1
				return String.format("1/%.2fX", (1 / realScale));
			}
			// or do we have a whole number scale?
			else if (scale.getDenom() == 1)
				return String.format("%dX", scale.getNumer());
			// else have valid fraction
			return String.format("%d/%dX", scale.getNumer(), scale.getDenom());
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
			}
			else { // factor < 1
				final double recip = 1.0 / realScale;
				final double floor = Math.floor(recip);
				if ((recip - floor) < 0.0001) {
					numer = 1;
					denom = (int) floor;
				}
			}
			if (denom == 0)
				lookForBestFraction(realScale);
		}

		int getNumer() {
			return numer;
		}

		int getDenom() {
			return denom;
		}

		// This method attempts to find a simple fraction that describes the
		// specified scale. It searches a small set of numbers to minimize
		// time spent. If it fails to find scale it leaves fraction unchanged.

		private void lookForBestFraction(final double scale) {
			final int quickRange = 32;
			for (int n = 1; n <= quickRange; n++) {
				for (int d = 1; d <= quickRange; d++) {
					final double frac = 1.0 * n / d;
					if (Math.abs(scale - frac) < 0.0001) {
						numer = n;
						denom = d;
						return;
					}
				}
			}
		}

	}

	/**
	 * Recalculate the label text and update it on the panel.
	 */
	protected void updateLabel() {
		getPanel().setLabel(makeLabel());
	}
	
	//-- Event handlers --//
	@SuppressWarnings("unused")
	@EventHandler
	protected void onEvent(final WinActivatedEvent event) {
		if (event.getDisplay() != this) return;
		// final UserInterface ui = ImageJ.get(UIService.class).getUI();
		// final ToolService toolMgr = ui.getToolBar().getToolService();
		final ToolService toolService =
			event.getContext().getService(ToolService.class);
		getImageDisplay().getCanvas().setCursor(toolService.getActiveTool().getCursor());
	}

	@SuppressWarnings("unused")
	@EventHandler
	protected void onEvent(final ZoomEvent event) {
		if (event.getCanvas() != getImageDisplay().getCanvas()) return;
		updateLabel();
	}


}
