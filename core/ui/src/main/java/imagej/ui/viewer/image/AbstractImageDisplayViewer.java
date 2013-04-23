/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.viewer.image;

import imagej.core.options.OptionsAppearance;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.event.DelayedPositionEvent;
import imagej.data.display.event.PanZoomEvent;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.display.Display;
import imagej.display.event.DisplayUpdatedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.options.OptionsService;
import imagej.tool.ToolService;
import imagej.ui.viewer.AbstractDisplayViewer;
import imagej.ui.viewer.DisplayWindow;
import imagej.util.UnitUtils;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.scijava.event.EventHandler;

/**
 * The AbstractImageDisplayViewer implements the UI-independent elements of an
 * image display viewer. It subscribes to the events of its controlled display
 * and distills these into abstract lifecycle actions.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public abstract class AbstractImageDisplayViewer extends
	AbstractDisplayViewer<DataView> implements ImageDisplayViewer
{

	protected enum ZoomScaleOption {
		OPTIONS_PERCENT_SCALE, OPTIONS_FRACTIONAL_SCALE
	}

	private ImageDisplay display;

	// -- ImageDisplayViewer methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	// -- DisplayViewer methods --

	@Override
	public boolean canView(final Display<?> d) {
		return d instanceof ImageDisplay;
	}

	@Override
	public void view(final DisplayWindow w, final Display<?> d) {
		super.view(w, d);
		display = (ImageDisplay) d;
	}

	// -- Internal AbstractImageDisplayViewer methods --

	protected Dataset getDataset(final DataView view) {
		final Data data = view.getData();
		return data instanceof Dataset ? (Dataset) data : null;
	}

	/**
	 * Recalculate the label text and update it on the panel.
	 */
	protected void updateLabel() {
		if (getDisplay().getActiveView() != null) {
			getPanel().setLabel(makeLabel());
		}
	}

	/**
	 * Implement this in the derived class to get the user's preference for
	 * displaying zoom scale (as a fraction or percent)
	 * 
	 * @return {@link ZoomScaleOption#OPTIONS_PERCENT_SCALE} or
	 *         {@link ZoomScaleOption#OPTIONS_FRACTIONAL_SCALE}
	 */
	protected ZoomScaleOption getZoomScaleOption() {
		final OptionsService optionsService =
			getContext().getService(OptionsService.class);
		return optionsService.getOptions(OptionsAppearance.class)
			.isDisplayFractionalScales() ? ZoomScaleOption.OPTIONS_FRACTIONAL_SCALE
			: ZoomScaleOption.OPTIONS_PERCENT_SCALE;
	}

	// -- Helper methods --

	/** Makes some informative label text by inspecting the views. */
	private String makeLabel() {
		// CTR TODO - Fix window label to show beyond just the active view.
		final DataView view = getDisplay().getActiveView();
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

		final double zoomFactor = getDisplay().getCanvas().getZoomFactor();
		if (zoomFactor != 1) {
			sb.append("(");
			sb.append(getScaleConverter().getString(zoomFactor));
			sb.append(")");
		}

		return sb.toString();
	}

	private String byteInfoString(final Dataset ds) {
		final double byteCount = ds.getBytesOfInfo();
		return UnitUtils.getAbbreviatedByteLabel(byteCount);
	}

	private ScaleConverter getScaleConverter() {

		if (getZoomScaleOption().equals(ZoomScaleOption.OPTIONS_FRACTIONAL_SCALE)) {
			return new FractionalScaleConverter();
		}

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
			if (scale.getDenom() == 1) {
				return String.format("%dX", scale.getNumer());
			}
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
			if (denom == 0) lookForBestFraction(realScale);
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

	private boolean isMyDataset(final Dataset ds) {
		if (ds == null) return false;
		final ImageDisplayService service =
			getContext().getService(ImageDisplayService.class);
		final ImageDisplay disp = getDisplay();
		return service.getActiveDataset(disp) == ds;
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final WinActivatedEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		final ToolService toolService = getContext().getService(ToolService.class);
		final ImageDisplay d = getDisplay();
		if (d == null) return;
		d.getCanvas().setCursor(toolService.getActiveTool().getCursor());
	}

	@EventHandler
	protected void onEvent(final PanZoomEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		updateLabel();
	}

	@EventHandler
	protected void onEvent(final DatasetRestructuredEvent event) {
		if (isMyDataset(event.getObject())) updateLabel();
	}

	@EventHandler
	protected void onEvent(final DelayedPositionEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		updateLabel();
	}
	
	// NB - using less restrictive event type here. This captures both dataset
	// data type changes and dataset axis type changes. each affect label.
	
	@EventHandler
	protected void onEvent(final DatasetUpdatedEvent event) {
		if (!isMyDataset(event.getObject())) return;
		updateLabel();
		updateTitle();
	}

	@Override
	@EventHandler
	protected void onEvent(final DisplayUpdatedEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		updateLabel();
		updateTitle();
	}

}
