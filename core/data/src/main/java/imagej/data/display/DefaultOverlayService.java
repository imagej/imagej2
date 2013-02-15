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

package imagej.data.display;

import imagej.data.ChannelCollection;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.DrawingTool;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.options.OptionsOverlay;
import imagej.data.overlay.CompositeOverlay;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.OverlaySettings;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.options.OptionsService;
import imagej.render.RenderingService;
import imagej.util.RealRect;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealRandomAccess;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.ops.pointset.RoiPointSet;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;

import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for working with {@link Overlay}s.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public final class DefaultOverlayService extends AbstractService implements
	OverlayService
{

	@Parameter
	private ObjectService objectService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private OptionsService optionsService;

	@Parameter
	private RenderingService renderingService;

	private OverlaySettings defaultSettings;
	private OverlayInfoList overlayInfo;

	// -- OverlayService methods --

	@Override
	public ObjectService getObjectService() {
		return objectService;
	}

	/**
	 * Gets a list of all {@link Overlay}s. This method is a shortcut that
	 * delegates to {@link ObjectService}.
	 */
	@Override
	public List<Overlay> getOverlays() {
		return objectService.getObjects(Overlay.class);
	}

	/**
	 * Gets a list of {@link Overlay}s linked to the given {@link ImageDisplay}.
	 * If selectedOnly is true then it will gather overlays from the selected
	 * views only. Otherwise it will gather overlays from all the views.
	 */
	@Override
	public List<Overlay> getOverlays(ImageDisplay display, boolean selectedOnly) {
		ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		for (final DataView view : display) {
			if (selectedOnly)
				if (!view.isSelected()) continue; // ignore non-selected objects
			final Data data = view.getData();
			if (!(data instanceof Overlay)) continue; // ignore non-overlays
			final Overlay overlay = (Overlay) data;
			overlays.add(overlay);
		}
		return overlays;
	}
	
	/**
	 * Gets a list of {@link Overlay}s linked to the given {@link ImageDisplay}.
	 * A shortcut for getOverlays(display,false).
	 */
	@Override
	public List<Overlay> getOverlays(final ImageDisplay display) {
		return getOverlays(display, false);
	}

	/** Adds the list of {@link Overlay}s to the given {@link ImageDisplay}. */
	@Override
	public void addOverlays(final ImageDisplay display,
		final List<Overlay> overlays)
	{
		for (final Overlay overlay : overlays) {
			display.display(overlay);
		}
	}

	/**
	 * Removes an {@link Overlay} from the given {@link ImageDisplay}.
	 * 
	 * @param display the {@link ImageDisplay} from which the overlay should be
	 *          removed
	 * @param overlay the {@link Overlay} to remove
	 */
	@Override
	public void removeOverlay(final ImageDisplay display, final Overlay overlay)
	{
		final ArrayList<DataView> overlayViews = new ArrayList<DataView>();
		final List<DataView> views = display;
		for (final DataView view : views) {
			final Data data = view.getData();
			if (data == overlay) overlayViews.add(view);
		}
		for (final DataView view : overlayViews) {
			display.remove(view);
			view.dispose();
		}
		display.update();
	}

	@Override
	public void removeOverlay(final Overlay overlay) {
		List<ImageDisplay> imgDisps = objectService.getObjects(ImageDisplay.class);
		for (ImageDisplay disp : imgDisps)
			removeOverlay(disp, overlay);
	}
	
	/**
	 * Gets the bounding box for the selected data objects in the given
	 * {@link ImageDisplay}.
	 * 
	 * @param display the {@link ImageDisplay} from which the bounding box should
	 *          be computed
	 * @return the smallest bounding box encompassing all selected data objects
	 */
	@Override
	public RealRect getSelectionBounds(final ImageDisplay display) {
		// TODO - Compute bounds over N dimensions, not just two.

		// determine XY bounding box by checking all data objects
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		for (final DataView view : display) {
			if (!view.isSelected()) continue;
			final Data data = view.getData();
			final Extents e = data.getExtents();
			final double min0 = e.realMin(0);
			final double max0 = e.realMax(0);
			final double min1 = e.realMin(1);
			final double max1 = e.realMax(1);
			if (min0 < xMin) xMin = min0;
			if (max0 > xMax) xMax = max0;
			if (min1 < yMin) yMin = min1;
			if (max1 > yMax) yMax = max1;
		}

		// get total XY extents of the display
		final Extents totalExtents = display.getExtents();
		final double totalMinX = totalExtents.min(0);
		final double totalMaxX = totalExtents.max(0);
		final double totalMinY = totalExtents.min(1);
		final double totalMaxY = totalExtents.max(1);

		// use entire XY extents if values are out of bounds
		if (xMin < totalMinX || xMin > totalMaxX) xMin = totalMinX;
		if (xMax < totalMinX || xMax > totalMaxX) xMax = totalMaxX;
		if (yMin < totalMinY || yMin > totalMaxY) yMin = totalMinY;
		if (yMax < totalMinY || yMax > totalMaxY) yMax = totalMaxY;

		// swap reversed bounds
		if (xMin > xMax) {
			final double temp = xMin;
			xMin = xMax;
			xMax = temp;
		}
		if (yMin > yMax) {
			final double temp = yMin;
			yMin = yMax;
			yMax = temp;
		}

		return new RealRect(xMin, yMin, xMax - xMin, yMax - yMin);
	}

	@Override
	public OverlaySettings getDefaultSettings() {
		return defaultSettings;
	}

	@Override
	public void drawOverlay(Overlay o, ImageDisplay display, ChannelCollection channels) {
		draw(o, display, channels, new OverlayOutliner());
	}

	@Override
	public void fillOverlay(Overlay o, ImageDisplay display, ChannelCollection channels) {
		draw(o, display, channels, new OverlayFiller());
	}

	@Override
	public ImageDisplay getFirstDisplay(Overlay o) {
		final List<Display<?>> displays = displayService.getDisplays();
		for (Display<?> display : displays) {
			if (display instanceof ImageDisplay) {
				final List<Overlay> displayOverlays = getOverlays((ImageDisplay)display);
				if (displayOverlays.contains(o))
					return (ImageDisplay) display;
			}
		}
		return null;
	}

	@Override
	public List<ImageDisplay> getDisplays(Overlay o) {
		final ArrayList<ImageDisplay> containers = new ArrayList<ImageDisplay>();
		final List<Display<?>> displays = displayService.getDisplays();
		for (Display<?> display : displays) {
			if ( ! (display instanceof ImageDisplay) ) continue;
			final ImageDisplay imageDisplay = (ImageDisplay) display;
			for (final DataView view : imageDisplay) {
				final Data data = view.getData();
				if ( ! (data instanceof Overlay) ) continue;
				final Overlay overlay = (Overlay) data;
				if (overlay == o)
					containers.add(imageDisplay);
			}
		}
		return containers;
	}
	
	// TODO - assumes first selected overlay view is the only one. bad?
	@Override
	public Overlay getActiveOverlay(ImageDisplay disp) {
		for (DataView view : disp) {
			if (view.isSelected() && (view instanceof OverlayView))
				return ((OverlayView) view).getData();
		}
		return null;
	}
	
	@Override
	public OverlayInfoList getOverlayInfo() {
		return overlayInfo;
	}

	@Override
	public void divideCompositeOverlay(CompositeOverlay overlay) {
		List<Overlay> subcomponents = overlay.getSubcomponents();
		
		// to each display that owns the composite
		//   reference the original overlays (if not already)
		
		List<ImageDisplay> owners = getDisplays(overlay);
		for (ImageDisplay owner : owners) {
			boolean changes = false;
			List<Overlay> displayOverlays = getOverlays(owner);
			for (Overlay subcomponent : subcomponents) {
				if (!displayOverlays.contains(subcomponent)) {
					owner.display(subcomponent);
					changes = true;
				}
			}
			if (changes) owner.update();
		}
		
		// delete the composite overlay
		removeOverlay(overlay);
	}

	// -- Service methods --

	@Override
	public void initialize() {
		defaultSettings = new OverlaySettings();
		final OptionsOverlay overlayOptions =
			optionsService.getOptions(OptionsOverlay.class);
		overlayOptions.updateSettings(defaultSettings);
		overlayInfo = new OverlayInfoList();
	}

	// -- helpers --

	private interface Drawer {
		void draw(Overlay o, DrawingTool tool);
	}

	private static class OverlayOutliner implements Drawer {
		@Override
		public void draw(Overlay o, DrawingTool tool) {
			final RegionOfInterest region = o.getRegionOfInterest();
			PointSet pointSet = new RoiPointSet(region);
			// TODO - rather than a pointSet use an IterableInterval? Investigate.
			final RealRandomAccess<BitType> accessor = region.realRandomAccess();
			final PointSetIterator iter = pointSet.iterator();
			final long[] max = new long[pointSet.numDimensions()];
			pointSet.max(max);
			long[] pos;
			while (iter.hasNext()) {
				pos = iter.next();
				accessor.setPosition(pos);
				if (accessor.get().get())
					if (isBorderPixel(accessor, pos, max[0], max[1]))
						tool.drawPixel(pos[0], pos[1]);
			}
		}
		
		private boolean isBorderPixel(RealRandomAccess<BitType> accessor, long[] pos,
			long maxX, long maxY)
		{
			if (pos[0] == 0) return true;
			if (pos[0] == maxX) return true;
			if (pos[1] == 0) return true;
			if (pos[1] == maxY) return true;
			accessor.setPosition(pos[0]-1,0);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[0]+1,0);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[0],0);
			accessor.setPosition(pos[1]-1,1);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[1]+1,1);
			if (!accessor.get().get()) return true;
			return false;
		}

	}

	private static class OverlayFiller implements Drawer {
		@Override
		public void draw(Overlay o, DrawingTool tool) {
			final RegionOfInterest region = o.getRegionOfInterest();
			final RoiPointSet pointSet = new RoiPointSet(region);
			final RealRandomAccess<BitType> accessor = region.realRandomAccess();
			final PointSetIterator iter = pointSet.iterator();
			long[] pos;
			while (iter.hasNext()) {
				pos = iter.next();
				accessor.setPosition(pos);
				if (accessor.get().get())
					tool.drawPixel(pos[0], pos[1]);
			}
			
		}
	}

	private void draw(Overlay o, ImageDisplay display, ChannelCollection channels, Drawer drawer)
	{
		final Dataset ds = getDataset(display);
		if (ds == null) return;
		DrawingTool tool = new DrawingTool(ds, renderingService);
		final Position position = display.getActiveView().getPlanePosition();
		final long[] pp = new long[position.numDimensions()];
		position.localize(pp);
		final long[] fullPos = new long[pp.length + 2];
		for (int i = 2; i < fullPos.length; i++)
			fullPos[i] = pp[i-2];
		tool.setPosition(fullPos);
		tool.setChannels(channels);
		drawer.draw(o, tool);
		ds.update();
	}
	
	private Dataset getDataset(ImageDisplay display) {
		return imageDisplayService.getActiveDataset(display);
	}
}
