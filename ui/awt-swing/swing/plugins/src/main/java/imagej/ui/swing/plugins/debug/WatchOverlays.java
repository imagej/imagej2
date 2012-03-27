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

package imagej.ui.swing.plugins.debug;

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.RectangleOverlay;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.object.ObjectService;
import imagej.object.event.ObjectsListEvent;
import imagej.ui.swing.SwingOutputWindow;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.roi.RectangleRegionOfInterest;

/**
 * TODO
 * 
 * @author Grant Harris
 */
@Plugin(menuPath = "Plugins>Debug>Watch Overlays")
public class WatchOverlays implements ImageJPlugin {

	@Parameter(persist = false)
	private EventService eventService;

	@Parameter(persist = false)
	private ImageDisplayService imageDisplayService;

	@Parameter(persist = false)
	private ObjectService objectService;

	private static SwingOutputWindow window;

	/** Maintains the list of event subscribers, to avoid garbage collection. */
	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	@Override
	public void run() {
		window = new SwingOutputWindow("Overlays in Current Display");
		updateOverlaysShown();
		subscribers = eventService.subscribe(this);
	}

	@EventHandler
	protected void onEvent(
		@SuppressWarnings("unused") final ObjectsListEvent event)
	{
		updateOverlaysShown();
	}

	@EventHandler
	protected void onEvent(
		@SuppressWarnings("unused") final DisplayActivatedEvent event)
	{
		updateOverlaysShown();
	}

	private void updateOverlaysShown() {
		window.clear();
		final List<Overlay> overlays = objectService.getObjects(Overlay.class);

		window.append("all --------------------\n");
		for (final Overlay overlay : overlays) {
			window.append(overlay.getRegionOfInterest().toString() + "\n");
		}

		final ImageDisplay display = getCurrentImageDisplay();
		if (display == null) return;
		window.append("For display " + display.getName() +
			" --------------------\n");
		final List<Overlay> overlays2 = getOverlaysFromDisplay(display);
		for (final Overlay overlay : overlays2) {
			window.append(overlay.getRegionOfInterest().toString() + "\n");
			if (overlay instanceof RectangleOverlay) {
				final Dataset currDataset =
					imageDisplayService.getActiveDataset(display);
				final double[] origin =
					new double[currDataset.getImgPlus().numDimensions()];
				final double[] extent =
					new double[currDataset.getImgPlus().numDimensions()];
				((RectangleRegionOfInterest) overlay.getRegionOfInterest())
					.getExtent(extent);
				((RectangleRegionOfInterest) overlay.getRegionOfInterest())
					.getOrigin(origin);
				final int minX = (int) origin[0];
				final int minY = (int) origin[1];
				final int maxX = (int) extent[0];
				final int maxY = (int) extent[1];
				window.append("   Rect: " + minX + "," + minY + "," + maxX + "," +
					maxY + "\n");
			}
		}
	}

	private ImageDisplay getCurrentImageDisplay() {
		final ImageDisplay display = imageDisplayService.getActiveImageDisplay();
		if (display == null) return null; // headless UI or no open images
		return display;
	}

	public List<Overlay> getOverlaysFromDisplay(final ImageDisplay display) {
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		if (display != null) {
			for (final DataView view : display) {
				final Data data = view.getData();
				if (!(data instanceof Overlay)) continue;
				final Overlay overlay = (Overlay) data;
				overlays.add(overlay);
			}
		}
		return overlays;
	}

	public List<Overlay> getOverlaysForCurrentSlice(final ImageDisplay display) {
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		for (final DataView view : display) {
			final Data data = view.getData();
			if (data instanceof Overlay && display.isVisible(view)) {
				overlays.add((Overlay) data);
			}
		}
		return overlays;
	}

}
