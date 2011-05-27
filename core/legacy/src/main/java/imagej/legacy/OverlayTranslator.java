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
import ij.gui.Arrow;
import ij.gui.EllipseRoi;
import ij.gui.FreehandRoi;
import ij.gui.ImageRoi;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.roi.EllipseOverlay;
import imagej.data.roi.Overlay;
import imagej.data.roi.PolygonOverlay;
import imagej.data.roi.RectangleOverlay;
import imagej.display.OverlayManager;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.EllipseRegionOfInterest;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.roi.RectangleRegionOfInterest;

/**
 * OverlayTranslator moves regions of interest back and forth between
 * {@link Overlay}s and {@link ImagePlus}es.
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
			else roi = roi.or(overlayROI);
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
		return null;
	}

	private ShapeRoi createRectangleROI(final RectangleOverlay overlay) {
		final RectangleRegionOfInterest roi = overlay.getRegionOfInterest();
		final int dims = roi.numDimensions();
		final double[] origin = new double[dims];
		final double[] extent = new double[dims];
		roi.getOrigin(origin);
		roi.getExtent(extent);
		final int x = (int) origin[0], y = (int) origin[1];
		final int w = (int) extent[0], h = (int) extent[1];
		return new ShapeRoi(new Roi(x, y, w, h));
	}

	private ShapeRoi createEllipseROI(final EllipseOverlay overlay) {
		final EllipseRegionOfInterest roi = overlay.getRegionOfInterest();
		final int dims = roi.numDimensions();
		final double[] origin = new double[dims];
		final double[] radii = new double[dims];
		roi.getOrigin(origin);
		roi.getRadii(radii);
		final int x = (int) origin[0], y = (int) origin[1];
		final int w = (int) radii[0], h = (int) radii[1];
		return new ShapeRoi(new OvalRoi(x, y, w, h));
	}

	private ShapeRoi createPolygonROI(final PolygonOverlay overlay) {
		final PolygonRegionOfInterest roi = overlay.getRegionOfInterest();
		final int vertexCount = roi.getVertexCount();
		if (vertexCount == 1) return createPointROI(roi);
		if (vertexCount == 2) return createLineROI(roi);
		final float[] x = new float[vertexCount];
		final float[] y = new float[vertexCount];
		for (int v = 0; v < vertexCount; v++) {
			final RealLocalizable vertex = roi.getVertex(v);
			x[v] = vertex.getFloatPosition(0);
			y[v] = vertex.getFloatPosition(1);
		}
		return new ShapeRoi(new PolygonRoi(x, y, vertexCount, Roi.POLYGON));
	}

	private ShapeRoi createPointROI(final PolygonRegionOfInterest roi) {
		final RealLocalizable point = roi.getVertex(0);
		final int x = (int) point.getFloatPosition(0);
		final int y = (int) point.getFloatPosition(1);
		return new ShapeRoi(new PointRoi(x, y));
	}

	private ShapeRoi createLineROI(final PolygonRegionOfInterest roi) {
		final RealLocalizable p1 = roi.getVertex(0);
		final RealLocalizable p2 = roi.getVertex(1);
		final double x1 = p1.getDoublePosition(0);
		final double y1 = p1.getDoublePosition(1);
		final double x2 = p2.getDoublePosition(0);
		final double y2 = p2.getDoublePosition(1);
		return new ShapeRoi(new Line(x1, y1, x2, y2));
	}

	// -- Helper methods - IJ2 overlay creation --

	private void
		createOverlays(final Roi roi, final ArrayList<Overlay> overlays)
	{
		// TODO
		if (roi instanceof ImageRoi) {
			Log.warn("Ignoring unsupported ImageRoi: " + roi);
		}
		else if (roi instanceof Arrow) {
			Log.warn("Ignoring unsupported Arrow: " + roi);
		}
		else if (roi instanceof Line) {
			Log.warn("Ignoring unsupported Line: " + roi);
		}
		else if (roi instanceof OvalRoi) {
			Log.warn("Ignoring unsupported OvalRoi: " + roi);
		}
		else if (roi instanceof EllipseRoi) {
			Log.warn("Ignoring unsupported EllipseRoi: " + roi);
		}
		else if (roi instanceof FreehandRoi) {
			Log.warn("Ignoring unsupported FreehandRoi: " + roi);
		}
		else if (roi instanceof PointRoi) {
			Log.warn("Ignoring unsupported PointRoi: " + roi);
		}
		else if (roi instanceof PolygonRoi) {
			final PolygonRoi polygonRoi = (PolygonRoi) roi;
			final PolygonOverlay overlay = new PolygonOverlay();
			final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
			final int[] xCoords = polygonRoi.getXCoordinates();
			final int[] yCoords = polygonRoi.getYCoordinates();
			for (int i = 0; i < xCoords.length; i++) {
				final double x = xCoords[i], y = yCoords[i];
				region.addVertex(i, new RealPoint(x, y));
			}
			Log.debug("====> Adding polygon overlay: " + overlay);// TEMP
			overlays.add(overlay);
		}
		else if (roi instanceof ShapeRoi) {
			final ShapeRoi shapeRoi = (ShapeRoi) roi;
			final Roi[] rois = shapeRoi.getRois();
			for (final Roi r : rois) {
				createOverlays(r, overlays);
			}
 		}
		else if (roi instanceof TextRoi) {
			Log.warn("Ignoring unsupported TextRoi: " + roi);
		}
		else if (roi != null) { // Roi
			switch (roi.getType()) {
				case Roi.ANGLE:
					Log.warn("Ignoring unsupported ANGLE: " + roi);
					break;
				case Roi.COMPOSITE:
					Log.warn("Ignoring unsupported COMPOSITE: " + roi);
					break;
				case Roi.FREELINE:
					Log.warn("Ignoring unsupported FREELINE: " + roi);
					break;
				case Roi.FREEROI:
					Log.warn("Ignoring unsupported FREEROI: " + roi);
					break;
				case Roi.LINE:
					Log.warn("Ignoring unsupported LINE: " + roi);
					break;
				case Roi.RECTANGLE:
					Log.warn("Ignoring unsupported RECTANGLE: " + roi);
					break;
				case Roi.OVAL:
					Log.warn("Ignoring unsupported OVAL: " + roi);
					break;
				case Roi.POINT:
					Log.warn("Ignoring unsupported POINT: " + roi);
					break;
				case Roi.POLYGON:
					Log.warn("Ignoring unsupported POLYGON: " + roi);
					break;
				case Roi.POLYLINE:
					Log.warn("Ignoring unsupported POLYLINE: " + roi);
					break;
				case Roi.TRACED_ROI:
					Log.warn("Ignoring unsupported TRACE_ROI: " + roi);
					break;
				default:
					Log.warn("Ignoring unsupported Roi: " + roi);						
					break;
			}
		}
	}

}
