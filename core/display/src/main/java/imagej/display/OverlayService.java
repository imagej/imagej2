//
// OverlayService.java
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

package imagej.display;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.roi.Overlay;
import imagej.object.ObjectService;
import imagej.util.RealRect;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.roi.RegionOfInterest;

/**
 * Service for working with {@link Overlay}s.
 * 
 * @author Curtis Rueden
 */
@Service
public final class OverlayService extends AbstractService {

	private final ObjectService objectService;

	// -- Constructors --

	public OverlayService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public OverlayService(final ImageJ context, final ObjectService objectService)
	{
		super(context);
		this.objectService = objectService;
	}

	// -- OverlayService methods --

	/**
	 * Gets a list of all {@link Overlay}s. This method is a shortcut that
	 * delegates to {@link ObjectService}.
	 */
	public List<Overlay> getOverlays() {
		return objectService.getObjects(Overlay.class);
	}

	/** Gets a list of {@link Overlay}s linked to the given {@link Display}. */
	public List<Overlay> getOverlays(final Display display) {
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		if (display != null) {
			for (final DisplayView view : display.getViews()) {
				final DataObject dataObject = view.getDataObject();
				if (!(dataObject instanceof Overlay)) continue;
				final Overlay overlay = (Overlay) dataObject;
				overlays.add(overlay);
			}
		}
		return overlays;
	}

	/** Adds the list of {@link Overlay}s to the given {@link Display}. */
	public void addOverlays(final Display display, final List<Overlay> overlays)
	{
		for (final Overlay overlay : overlays) {
			display.display(overlay);
		}
	}

	/**
	 * Removes an {@link Overlay} from the given {@link Display}.
	 * 
	 * @param display the {@link Display} from which the overlay should be removed
	 * @param overlay the {@link Overlay} to remove
	 */
	public void removeOverlay(final Display display, final Overlay overlay) {
		final ArrayList<DisplayView> overlayViews = new ArrayList<DisplayView>();
		final List<DisplayView> views = display.getViews();
		for (final DisplayView view : views) {
			final DataObject dataObject = view.getDataObject();
			if (dataObject == overlay) overlayViews.add(view);
		}
		for (final DisplayView view : overlayViews) {
			display.removeView(view);
		}
	}

	/**
	 * Gets the bounding box for the selected overlays in the given
	 * {@link Display}.
	 * 
	 * @param display the {@link Display} from which the bounding box should be
	 *          computed
	 * @return the smallest bounding box encompassing all selected overlays
	 */
	public RealRect getSelectionBounds(final Display display) {
		// get total XY extents of the display by checking all datasets
		double width = 0, height = 0;
		for (final DisplayView view : display.getViews()) {
			final DataObject dataObject = view.getDataObject();
			if (!(dataObject instanceof Dataset)) continue;
			final Dataset dataset = (Dataset) dataObject;
			final Extents extents = dataset.getExtents();
			final double w = extents.dimension(0);
			final double h = extents.dimension(1);
			if (w > width) width = w;
			if (h > height) height = h;
		}

		// TODO - Compute bounds over N dimensions, not just two.
		// TODO - Update this method when ticket #660 is done.
		// For example, why don't all DataObjects have Extents?

		// determine XY bounding box by checking all overlays
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		for (final DisplayView view : display.getViews()) {
			if (!view.isSelected()) continue; // ignore non-selected objects
			final DataObject dataObject = view.getDataObject();
			if (!(dataObject instanceof Overlay)) continue; // ignore non-overlays

			final Overlay overlay = (Overlay) dataObject;
			final RegionOfInterest roi = overlay.getRegionOfInterest();
			final double min0 = roi.realMin(0);
			final double max0 = roi.realMax(0);
			final double min1 = roi.realMin(1);
			final double max1 = roi.realMax(1);
			if (min0 < xMin) xMin = min0;
			if (max0 > xMax) xMax = max0;
			if (min1 < yMin) yMin = min1;
			if (max1 < yMax) yMax = max1;
		}
		if (xMin > xMax || yMin > yMax) return null;
		if (xMin < 0) xMin = 0;
		if (yMin < 0) yMin = 0;
		if (xMax > width) xMax = width;
		if (yMax > height) yMax = height;
		return new RealRect(xMin, yMin, xMax - xMin, yMax - yMin);
	}

	// -- IService methods --

	@Override
	public void initialize() {
		// NB: no action needed
	}

}
