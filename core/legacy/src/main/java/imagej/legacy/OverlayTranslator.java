//
// OverlayTranslator.java
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

package imagej.legacy;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.roi.EllipseOverlay;
import imagej.data.roi.Overlay;
import imagej.data.roi.PolygonOverlay;
import imagej.data.roi.RectangleOverlay;
import imagej.display.OverlayManager;
import imagej.util.Log;
import imagej.util.awt.AWTColors;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.EllipseRegionOfInterest;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.roi.RectangleRegionOfInterest;

/**
 * OverlayTranslator moves regions of interest back and forth between
 * {@link Overlay}s and {@link ImagePlus} {@link Roi}s.
 * 
 * @author Curtis Rueden
 */
public class OverlayTranslator {

	/**
	 * Updates the given {@link Dataset} to be visualized along with
	 * {@link Overlay}s corresponding to the given {@link ImagePlus}'s ROI.
	 * <p>
	 * Note that this method is a big HACK to handle legacy IJ1 support. The
	 * longterm solution will be to create one ImagePlus per display, rather than
	 * one per dataset, with orphan datasets also getting one ImagePlus in the
	 * map. Then we can assign the ImagePlus's ROI according to the overlays
	 * present in only its associated display.
	 * </p>
	 */
	public void setDatasetOverlays(final Dataset ds, final ImagePlus imp) {
		final OverlayManager overlayManager = ImageJ.get(OverlayManager.class);
		final List<Overlay> overlays = getOverlays(imp);
		overlayManager.setOverlays(ds, overlays);
	}

	/**
	 * Updates the given {@link ImagePlus}'s ROI to match the {@link Overlay}s
	 * being visualized along with the given {@link Dataset}.
	 * <p>
	 * Note that this method is a big HACK to handle legacy IJ1 support. The
	 * longterm solution will be to create one ImagePlus per display, rather than
	 * one per dataset, with orphan datasets also getting one ImagePlus in the
	 * map. Then we can assign the ImagePlus's ROI according to the overlays
	 * present in only its associated display.
	 * </p>
	 */
	public void setImagePlusOverlays(final Dataset ds, final ImagePlus imp) {
		final OverlayManager overlayManager = ImageJ.get(OverlayManager.class);
		final List<Overlay> overlays = overlayManager.getOverlays(ds);
		setOverlays(overlays, imp);
	}

	/** Extracts a list of {@link Overlay}s from the given {@link ImagePlus}. */
	public List<Overlay> getOverlays(final ImagePlus imp) {
		final Roi roi = imp.getRoi();
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		createOverlays(roi, overlays);
		return overlays;
	}

	/** Assigns a list of {@link Overlay}s to the given {@link ImagePlus}. */
	public void setOverlays(final List<Overlay> overlays, final ImagePlus imp) {
		ShapeRoi roi = null;
		for (final Overlay overlay : overlays) {
			final ShapeRoi overlayROI = createROI(overlay);
			if (roi == null) roi = overlayROI;
			else if (overlayROI != null) roi = roi.or(overlayROI);
		}
		imp.setRoi(roi);
	}

	// -- Helper methods - legacy ROI creation --

	private ShapeRoi createROI(final Overlay overlay) {
		if (overlay instanceof RectangleOverlay) {
			return createRectangleROI((RectangleOverlay) overlay);
		}
		if (overlay instanceof EllipseOverlay) {
			return createEllipseROI((EllipseOverlay) overlay);
		}
		if (overlay instanceof PolygonOverlay) {
			return createPolygonROI((PolygonOverlay) overlay);
		}
		// TODO: lines, arrows, freehand, text, arbitrary masks
/*		throw new UnsupportedOperationException("Translation of " +
			overlay.getClass().getName() + " is unimplemented");
*/
		return null;
	}

	private ShapeRoi createRectangleROI(final RectangleOverlay overlay) {
		final RectangleRegionOfInterest region = overlay.getRegionOfInterest();
		final int dims = region.numDimensions();
		final double[] origin = new double[dims];
		final double[] extent = new double[dims];
		region.getOrigin(origin);
		region.getExtent(extent);
		final int x = (int) origin[0], y = (int) origin[1];
		final int w = (int) extent[0], h = (int) extent[1];
		final ShapeRoi roi = new ShapeRoi(new Roi(x, y, w, h));
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private ShapeRoi createEllipseROI(final EllipseOverlay overlay) {
		final EllipseRegionOfInterest region = overlay.getRegionOfInterest();
		final int dims = region.numDimensions();
		final double[] origin = new double[dims];
		final double[] radii = new double[dims];
		region.getOrigin(origin);
		region.getRadii(radii);
		final int x = (int) origin[0], y = (int) origin[1];
		final int w = (int) radii[0], h = (int) radii[1];
		final ShapeRoi roi = new ShapeRoi(new OvalRoi(x, y, w, h));
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private ShapeRoi createPolygonROI(final PolygonOverlay overlay) {
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final int vertexCount = region.getVertexCount();
		if (vertexCount == 1) return createPointROI(overlay);
		if (vertexCount == 2) return createLineROI(overlay);
		final float[] x = new float[vertexCount];
		final float[] y = new float[vertexCount];
		for (int v = 0; v < vertexCount; v++) {
			final RealLocalizable vertex = region.getVertex(v);
			x[v] = vertex.getFloatPosition(0);
			y[v] = vertex.getFloatPosition(1);
		}
		final ShapeRoi roi =
			new ShapeRoi(new PolygonRoi(x, y, vertexCount, Roi.POLYGON));
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private ShapeRoi createPointROI(final PolygonOverlay overlay) {
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final RealLocalizable point = region.getVertex(0);
		final int x = (int) point.getFloatPosition(0);
		final int y = (int) point.getFloatPosition(1);
		final ShapeRoi roi = new ShapeRoi(new PointRoi(x, y));
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private ShapeRoi createLineROI(final PolygonOverlay overlay) {
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final RealLocalizable p1 = region.getVertex(0);
		final RealLocalizable p2 = region.getVertex(1);
		final double x1 = p1.getDoublePosition(0);
		final double y1 = p1.getDoublePosition(1);
		final double x2 = p2.getDoublePosition(0);
		final double y2 = p2.getDoublePosition(1);
		final ShapeRoi roi = new ShapeRoi(new Line(x1, y1, x2, y2));
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private void
		assignPropertiesToRoi(final ShapeRoi roi, final Overlay overlay)
	{
		roi.setStrokeWidth((float) overlay.getLineWidth());
		roi.setStrokeColor(AWTColors.getColor(overlay.getLineColor()));
		roi.setFillColor(AWTColors.getColor(overlay.getFillColor()));
	}

	// -- Helper methods - IJ2 overlay creation --

	private void
		createOverlays(final Roi roi, final ArrayList<Overlay> overlays)
	{
		if (roi == null) return;

		Log.warn("====> Roi class = " + roi.getClass().getName());
		switch (roi.getType()) {
			case Roi.RECTANGLE:
				Log.warn("====> RECTANGLE: " + roi);
				overlays.add(createRectangleOverlay(roi));
				break;
			case Roi.OVAL:
				Log.warn("====> OVAL: " + roi);
				overlays.add(createEllipseOverlay(roi));
				break;
			case Roi.POLYGON:
				Log.warn("====> POLYGON: " + roi);
				overlays.add(createPolygonOverlay(roi));
				break;
			case Roi.FREEROI:
				Log.warn("====> FREEROI: " + roi);
				throw new UnsupportedOperationException("FREEROI unimplemented");
//				break;
			case Roi.TRACED_ROI:
				Log.warn("====> TRACED_ROI: " + roi);
				throw new UnsupportedOperationException("TRACED_ROI unimplemented");
//				break;
			case Roi.LINE:
				Log.warn("====> LINE: " + roi);
				throw new UnsupportedOperationException("LINE unimplemented");
//				break;
			case Roi.POLYLINE:
				Log.warn("====> POLYLINE: " + roi);
				throw new UnsupportedOperationException("POLYLINE unimplemented");
//				break;
			case Roi.FREELINE:
				Log.warn("====> FREELINE: " + roi);
				throw new UnsupportedOperationException("FREELINE unimplemented");
//				break;
			case Roi.ANGLE:
				Log.warn("====> ANGLE: " + roi);
				throw new UnsupportedOperationException("ANGLE unimplemented");
//				break;
			case Roi.COMPOSITE:
				Log.warn("====> COMPOSITE: " + roi);
				final ShapeRoi shapeRoi = (ShapeRoi) roi;
				final Roi[] rois = shapeRoi.getRois();
				for (final Roi r : rois)
					createOverlays(r, overlays);
				break;
			case Roi.POINT:
				Log.warn("====> POINT: " + roi);
				throw new UnsupportedOperationException("POINT unimplemented");
//				break;
			default:
				Log.warn("====> OTHER (" + roi.getType() + ", " + "): " + roi);
				throw new UnsupportedOperationException("OTHER unimplemented");
		}
	}

	private RectangleOverlay createRectangleOverlay(final Roi roi) {
		final RectangleOverlay overlay = new RectangleOverlay();
		final RectangleRegionOfInterest region = overlay.getRegionOfInterest();
		final Rectangle bounds = roi.getBounds();
		region.setOrigin(bounds.x, 0);
		region.setOrigin(bounds.y, 1);
		region.setExtent(bounds.width, 0);
		region.setExtent(bounds.height, 1);
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private EllipseOverlay createEllipseOverlay(final Roi roi) {
		final EllipseOverlay overlay = new EllipseOverlay();
		final EllipseRegionOfInterest region = overlay.getRegionOfInterest();
		final Rectangle bounds = roi.getBounds();
		region.setOrigin(bounds.x, 0);
		region.setOrigin(bounds.y, 1);
		region.setRadius(bounds.width, 0);
		region.setRadius(bounds.height, 1);
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private PolygonOverlay createPolygonOverlay(final Roi roi) {
		final PolygonRoi polygonRoi = (PolygonRoi) roi;

		final PolygonOverlay overlay = new PolygonOverlay();
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final int[] xCoords = polygonRoi.getXCoordinates();
		final int[] yCoords = polygonRoi.getYCoordinates();
		for (int i = 0; i < xCoords.length; i++) {
			final double x = xCoords[i], y = yCoords[i];
			region.addVertex(i, new RealPoint(x, y));
		}
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private void assignPropertiesToOverlay(final Overlay overlay, final Roi roi)
	{
		overlay.setLineWidth(roi.getStrokeWidth());
		overlay.setLineColor(AWTColors.getColorRGB(roi.getStrokeColor()));
		overlay.setFillColor(AWTColors.getColorRGBA(roi.getFillColor()));
	}

}
