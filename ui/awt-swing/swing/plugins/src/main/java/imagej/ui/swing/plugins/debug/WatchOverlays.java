//
// WatchOverlays.java
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

package imagej.ui.swing.plugins.debug;

import imagej.ImageJ;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.roi.Overlay;
import imagej.data.roi.RectangleOverlay;
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

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.roi.RectangleRegionOfInterest;

/**
 * TODO
 * 
 * @author Grant Harris
 */
@Plugin(menuPath = "Plugins>Debug>Watch Overlays")
public class WatchOverlays implements ImageJPlugin {

	@Parameter(required = true, persist = false)
	private EventService eventService;

	private static SwingOutputWindow window;

	/** Maintains the list of event subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	@Override
	public void run() {
		window = new SwingOutputWindow("Overlays in Current Display");
		updateOverlaysShown();
		subscribeToEvents();
	}

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<ObjectsListEvent> objectsUpdatedSubscriber =
			new EventSubscriber<ObjectsListEvent>() {

				@Override
				public void onEvent(final ObjectsListEvent event) {
					updateOverlaysShown();
				}
			};
		subscribers.add(objectsUpdatedSubscriber);
		eventService.subscribe(objectsUpdatedSubscriber);
		//
//		final EventSubscriber<WinActivatedEvent> WinActivatedSubscriber =
//				new EventSubscriber<WinActivatedEvent>() {
//					@Override
//					public void onEvent(final WinActivatedEvent event) {
//						updateOverlaysShown();
//					}
//				};
//		subscribers.add(WinActivatedSubscriber);
//		Events.subscribe(WinActivatedEvent.class, WinActivatedSubscriber);

		final EventSubscriber<DisplayActivatedEvent> DisplaySelectedSubscriber =
			new EventSubscriber<DisplayActivatedEvent>() {

				@Override
				public void onEvent(final DisplayActivatedEvent event) {
					updateOverlaysShown();
				}
			};
		subscribers.add(DisplaySelectedSubscriber);
		eventService.subscribe(DisplaySelectedSubscriber);
	}

	private void updateOverlaysShown() {
		window.clear();
		final ObjectService objectService = ImageJ.get(ObjectService.class);
		final List<Overlay> overlays = objectService.getObjects(Overlay.class);

		window.append("all --------------------\n");
		for (final Overlay overlay : overlays) {
			window.append(overlay.getRegionOfInterest().toString() + ": " +
				overlay.getPosition(Axes.Z) + "\n");
		}

		final ImageDisplay display = getCurrentImageDisplay();
		if (display == null) return;
		window.append("For display " + display.getName() +
			" --------------------\n");
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
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
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final ImageDisplay display = imageDisplayService.getActiveImageDisplay();
		if (display == null) return null; // headless UI or no open images
		return display;
	}

//		List<Overlay> overlays;
//		final OverlayService overlayService = ImageJ.get(OverlayService.class);
//		if (overlayService == null) {
//			window.append("** overlayService==null");
//			return;
//		}
//		AbstractDatasetView dsView = (AbstractDatasetView) display.getActiveView();
//		if (dsView != null) {
//			overlays = overlayService.getOverlays(dsView.getData());
//		} else {
//			window.append("** dsView==null");
//			overlays = overlayService.getOverlays();
//		}
//		window.clear();
//		for (Overlay overlay : overlays) {
//			window.append(overlay.getRegionOfInterest().toString() + "\n");
//			//Inspector.inspect(overlay);
//		}

	public List<Overlay> getOverlaysFromDisplay(final ImageDisplay display) {
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		if (display != null) {
			for (final DataView view : display) {
				// SwingOverlayView sov = (SwingOverlayView) view;
				final Data dataObject = view.getData();
				dataObject.getClass().getSimpleName();
				if (!(dataObject instanceof Overlay)) {
					continue;
				}
				final Overlay overlay = (Overlay) dataObject;
				overlays.add(overlay);
			}
		}
		return overlays;
	}

	public List<Overlay> getOverlaysForCurrentSlice(final ImageDisplay display) {
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		for (final DataView view : display) {
			final Position planePosition = view.getPlanePosition();
			final Data dataObject = view.getData();
			if (dataObject instanceof Overlay) {
				isVisible((Overlay) dataObject, planePosition);
			}

		}
		return overlays;
	}

	public boolean isVisible(final Overlay overlay, final Position planePosition)
	{
		for (int i = 2; i < overlay.numDimensions(); i++) {
			final Axis axis = overlay.axis(i);
			final Long pos = overlay.getPosition(axis);
			if (pos != null && !pos.equals(planePosition.getLongPosition(i - 2))) {
				return false;
			}
		}
		return true;
	}

}
