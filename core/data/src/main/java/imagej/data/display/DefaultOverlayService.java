/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.data.display;

import imagej.data.ChannelCollection;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.DrawingTool;
import imagej.data.Position;
import imagej.data.options.OptionsOverlay;
import imagej.data.overlay.CompositeOverlay;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.OverlaySettings;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.options.OptionsService;
import imagej.render.RenderingService;

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
import org.scijava.util.RealRect;

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

	@Override
	public List<Overlay> getOverlays() {
		return objectService.getObjects(Overlay.class);
	}

	@Override
	public List<Overlay> getOverlays(final ImageDisplay display,
		final boolean selectedOnly)
	{
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		for (final DataView view : display) {
			if (selectedOnly && !view.isSelected()) {
				// ignore non-selected objects
				continue;
			}
			final Data data = view.getData();
			if (!(data instanceof Overlay)) continue; // ignore non-overlays
			final Overlay overlay = (Overlay) data;
			overlays.add(overlay);
		}
		return overlays;
	}

	@Override
	public List<Overlay> getOverlays(final ImageDisplay display) {
		return getOverlays(display, false);
	}

	@Override
	public void addOverlays(final ImageDisplay display,
		final List<Overlay> overlays)
	{
		for (final Overlay overlay : overlays) {
			display.display(overlay);
		}
	}

	@Override
	public void removeOverlay(final ImageDisplay display, final Overlay overlay) {
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
		final List<ImageDisplay> imgDisps =
			objectService.getObjects(ImageDisplay.class);
		for (final ImageDisplay disp : imgDisps)
			removeOverlay(disp, overlay);
	}

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
			final double min0 = data.realMin(0);
			final double max0 = data.realMax(0);
			final double min1 = data.realMin(1);
			final double max1 = data.realMax(1);
			if (min0 < xMin) xMin = min0;
			if (max0 > xMax) xMax = max0;
			if (min1 < yMin) yMin = min1;
			if (max1 > yMax) yMax = max1;
		}

		// get total XY extents of the display
		final double totalMinX = display.realMin(0);
		final double totalMaxX = display.realMax(0);
		final double totalMinY = display.realMin(1);
		final double totalMaxY = display.realMax(1);

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
		if (defaultSettings == null) {
			defaultSettings = new OverlaySettings();
			final OptionsOverlay overlayOptions =
				optionsService.getOptions(OptionsOverlay.class);
			overlayOptions.updateSettings(defaultSettings);
		}
		return defaultSettings;
	}

	@Override
	public void drawOverlay(final Overlay o, final ImageDisplay display,
		final ChannelCollection channels)
	{
		draw(o, imageDisplayService.getActiveDataset(display), imageDisplayService
			.getActivePosition(display), channels, new OverlayOutliner());
	}

	@Override
	public void drawOverlay(final Overlay o, final Dataset ds,
		final Position position, final ChannelCollection channels)
	{
		draw(o, ds, position, channels, new OverlayOutliner());
	}

	@Override
	public void fillOverlay(final Overlay o, final ImageDisplay display,
		final ChannelCollection channels)
	{
		draw(o, imageDisplayService.getActiveDataset(display), imageDisplayService
			.getActivePosition(display), channels, new OverlayFiller());
	}

	@Override
	public void fillOverlay(final Overlay o, final Dataset ds,
		final Position position, final ChannelCollection channels)
	{
		draw(o, ds, position, channels, new OverlayFiller());
	}

	@Override
	public ImageDisplay getFirstDisplay(final Overlay o) {
		final List<Display<?>> displays = displayService.getDisplays();
		for (final Display<?> display : displays) {
			if (display instanceof ImageDisplay) {
				final List<Overlay> displayOverlays =
					getOverlays((ImageDisplay) display);
				if (displayOverlays.contains(o)) return (ImageDisplay) display;
			}
		}
		return null;
	}

	@Override
	public List<ImageDisplay> getDisplays(final Overlay o) {
		final ArrayList<ImageDisplay> containers = new ArrayList<ImageDisplay>();
		final List<Display<?>> displays = displayService.getDisplays();
		for (final Display<?> display : displays) {
			if (!(display instanceof ImageDisplay)) continue;
			final ImageDisplay imageDisplay = (ImageDisplay) display;
			for (final DataView view : imageDisplay) {
				final Data data = view.getData();
				if (!(data instanceof Overlay)) continue;
				final Overlay overlay = (Overlay) data;
				if (overlay == o) containers.add(imageDisplay);
			}
		}
		return containers;
	}

	// TODO - assumes first selected overlay view is the only one. bad?
	@Override
	public Overlay getActiveOverlay(final ImageDisplay disp) {
		for (final DataView view : disp) {
			if (view.isSelected() && view instanceof OverlayView) {
				return ((OverlayView) view).getData();
			}
		}
		return null;
	}

	@Override
	public OverlayInfoList getOverlayInfo() {
		if (overlayInfo == null) {
			overlayInfo = new OverlayInfoList();
		}
		return overlayInfo;
	}

	@Override
	public void divideCompositeOverlay(final CompositeOverlay overlay) {
		final List<Overlay> subcomponents = overlay.getSubcomponents();

		// to each display that owns the composite
		// reference the original overlays (if not already)

		final List<ImageDisplay> owners = getDisplays(overlay);
		for (final ImageDisplay owner : owners) {
			boolean changes = false;
			final List<Overlay> displayOverlays = getOverlays(owner);
			for (final Overlay subcomponent : subcomponents) {
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

	// -- Helper methods --

	private void draw(final Overlay o, final Dataset ds, final Position position,
		final ChannelCollection channels, final Drawer drawer)
	{
		// TODO What null items should be checked here? Return silently if any are
		// null? Currently check only for null Dataset? Others likely result in
		// NullPointerExceptions. OK?
		if (ds == null) return;
		final DrawingTool tool = new DrawingTool(ds, renderingService);
		final long[] pp = new long[position.numDimensions()];
		position.localize(pp);
		final long[] fullPos = new long[pp.length + 2];
		for (int i = 2; i < fullPos.length; i++)
			fullPos[i] = pp[i - 2];
		tool.setPosition(fullPos);
		tool.setChannels(channels);
		drawer.draw(o, tool);
		ds.update();
	}

	// -- Helper classes --

	private interface Drawer {

		void draw(Overlay o, DrawingTool tool);
	}

	private static class OverlayOutliner implements Drawer {

		@Override
		public void draw(final Overlay o, final DrawingTool tool) {
			final RegionOfInterest region = o.getRegionOfInterest();
			final PointSet pointSet = new RoiPointSet(region);
			// TODO - rather than a pointSet use an IterableInterval? Investigate.
			final RealRandomAccess<BitType> accessor = region.realRandomAccess();
			final PointSetIterator iter = pointSet.iterator();
			final long[] max = new long[pointSet.numDimensions()];
			pointSet.max(max);
			long[] pos;
			while (iter.hasNext()) {
				pos = iter.next();
				accessor.setPosition(pos);
				if (accessor.get().get() &&
					isBorderPixel(accessor, pos, max[0], max[1]))
				{
					tool.drawPixel(pos[0], pos[1]);
				}
			}
		}

		private boolean isBorderPixel(final RealRandomAccess<BitType> accessor,
			final long[] pos, final long maxX, final long maxY)
		{
			if (pos[0] == 0) return true;
			if (pos[0] == maxX) return true;
			if (pos[1] == 0) return true;
			if (pos[1] == maxY) return true;
			accessor.setPosition(pos[0] - 1, 0);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[0] + 1, 0);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[0], 0);
			accessor.setPosition(pos[1] - 1, 1);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[1] + 1, 1);
			if (!accessor.get().get()) return true;
			return false;
		}

	}

	private static class OverlayFiller implements Drawer {

		@Override
		public void draw(final Overlay o, final DrawingTool tool) {
			final RegionOfInterest region = o.getRegionOfInterest();
			final RoiPointSet pointSet = new RoiPointSet(region);
			final RealRandomAccess<BitType> accessor = region.realRandomAccess();
			final PointSetIterator iter = pointSet.iterator();
			long[] pos;
			while (iter.hasNext()) {
				pos = iter.next();
				accessor.setPosition(pos);
				if (accessor.get().get()) tool.drawPixel(pos[0], pos[1]);
			}

		}
	}

}
