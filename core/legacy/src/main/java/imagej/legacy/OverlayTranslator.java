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
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import imagej.ImageJ;
import imagej.data.roi.BinaryMaskOverlay;
import imagej.data.roi.CompositeOverlay;
import imagej.data.roi.EllipseOverlay;
import imagej.data.roi.LineOverlay;
import imagej.data.roi.Overlay;
import imagej.data.roi.PolygonOverlay;
import imagej.data.roi.RectangleOverlay;
import imagej.display.Display;
import imagej.display.OverlayService;
import imagej.util.ColorRGB;
import imagej.util.Log;
import imagej.util.awt.AWTColors;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.BitAccess;
import net.imglib2.img.transform.ImgTranslationAdapter;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.roi.CompositeRegionOfInterest;
import net.imglib2.roi.EllipseRegionOfInterest;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.roi.RectangleRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;

/**
 * OverlayTranslator moves regions of interest back and forth between
 * {@link Overlay}s and {@link ImagePlus} {@link Roi}s.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public class OverlayTranslator {

	/**
	 * Updates the given {@link Display} to contain {@link Overlay}s corresponding
	 * to the given {@link ImagePlus}'s ROI.
	 */
	public void setDisplayOverlays(final Display display, final ImagePlus imp) {
		final OverlayService overlayService = ImageJ.get(OverlayService.class);
		final Roi oldROI = createROI(overlayService.getOverlays(display));
		if (oldROI instanceof ShapeRoi) {
			final float[] oldPath = ((ShapeRoi)oldROI).getShapeAsArray();
			final Roi newROI = imp.getRoi();
			if (newROI instanceof ShapeRoi) {
				final float[] newPath = ((ShapeRoi) newROI).getShapeAsArray();
				if (oldPath.length == newPath.length) {
					boolean same = true;
					for (int i = 0; i < oldPath.length; i++) {
						if (oldPath[i] != newPath[i]) {
							same = false;
							break;
						}
					}
					if (same &&
						oldROI.getStrokeWidth() == newROI.getStrokeWidth() &&
						oldROI.getStrokeColor().equals(newROI.getStrokeColor()) &&
						oldROI.getFillColor().equals(newROI.getFillColor())) {
						return;
					}
				}
			}
		}
		final List<Overlay> overlaysToRemove = overlayService.getOverlays(display);
		for (final Overlay overlay : overlaysToRemove) {
			overlayService.removeOverlay(display, overlay);
		}
		final List<Overlay> overlays = getOverlays(imp);
		overlayService.addOverlays(display, overlays);
	}

	/**
	 * Updates the given {@link ImagePlus}'s ROI to match the {@link Overlay}s
	 * being visualized in the given {@link Display}.
	 */
	public void setImagePlusOverlays(final Display display, final ImagePlus imp)
	{
		final OverlayService overlayService = ImageJ.get(OverlayService.class);
		final List<Overlay> overlays = overlayService.getOverlays(display);
		setOverlays(overlays, imp);
	}

	/** Extracts a list of {@link Overlay}s from the given {@link ImagePlus}. */
	public List<Overlay> getOverlays(final ImagePlus imp) {
		final Roi roi = imp.getRoi();
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		createOverlays(roi, overlays, 0, 0);
		return overlays;
	}

	/** Assigns a list of {@link Overlay}s to the given {@link ImagePlus}. */
	public void setOverlays(final List<Overlay> overlays, final ImagePlus imp) {
		final Roi roi = createROI(overlays);
		imp.setRoi(roi);
	}

	private Roi createROI(final List<Overlay> overlays) {
		if (overlays.size() == 0) return null;
		if (overlays.size() == 1) return createROI(overlays.get(0));
		ShapeRoi roi = new ShapeRoi(createROI(overlays.get(0)));
		for (int i = 1; i < overlays.size(); i++) {
			final Roi overlayROI = createROI(overlays.get(i));
			if (overlayROI != null) roi = roi.or(new ShapeRoi(overlayROI));
		}
		return roi;
	}

	// -- Helper methods - legacy ROI creation --

	private Roi createROI(final Overlay overlay) {
		if (overlay instanceof RectangleOverlay) {
			return createRectangleROI((RectangleOverlay) overlay);
		}
		if (overlay instanceof EllipseOverlay) {
			return createEllipseROI((EllipseOverlay) overlay);
		}
		if (overlay instanceof PolygonOverlay) {
			return createPolygonROI((PolygonOverlay) overlay);
		}
		if (overlay instanceof BinaryMaskOverlay) {
			return createBinaryMaskRoi((BinaryMaskOverlay) overlay);
		}
		if (overlay instanceof LineOverlay) {
			return createLineRoi((LineOverlay) overlay);
		}
		// TODO: arrows, freehand, text
//		throw new UnsupportedOperationException("Translation of " +
//			overlay.getClass().getName() + " is unimplemented");
		return null;
	}

	private Roi createLineRoi(final LineOverlay overlay) {
		final RealLocalizable pt0 = overlay.getLineStart();
		final RealLocalizable pt1 = overlay.getLineEnd();
		final Line line =
			new Line(pt0.getDoublePosition(0), pt0.getDoublePosition(1), pt1
				.getDoublePosition(0), pt1.getDoublePosition(1));
		assignPropertiesToRoi(line, overlay);
		return line;
	}

	private Roi createRectangleROI(final RectangleOverlay overlay) {
		final RectangleRegionOfInterest region = overlay.getRegionOfInterest();
		final int dims = region.numDimensions();
		final double[] origin = new double[dims];
		final double[] extent = new double[dims];
		region.getOrigin(origin);
		region.getExtent(extent);
		final int x = (int) origin[0], y = (int) origin[1];
		final int w = (int) extent[0], h = (int) extent[1];
		final Roi roi = new Roi(x, y, w, h);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private Roi createEllipseROI(final EllipseOverlay overlay) {
		final EllipseRegionOfInterest region = overlay.getRegionOfInterest();
		final int dims = region.numDimensions();
		final double[] origin = new double[dims];
		final double[] radii = new double[dims];
		region.getOrigin(origin);
		region.getRadii(radii);
		final int x = (int) (origin[0] - radii[0]);
		final int y = (int) (origin[1] - radii[1]);
		final int w = (int) radii[0] * 2, h = (int) radii[1] * 2;
		final Roi roi = new OvalRoi(x, y, w, h);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private Roi createPolygonROI(final PolygonOverlay overlay) {
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
		final Roi roi = new PolygonRoi(x, y, vertexCount, Roi.POLYGON);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private Roi createPointROI(final PolygonOverlay overlay) {
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final RealLocalizable point = region.getVertex(0);
		final int x = (int) point.getFloatPosition(0);
		final int y = (int) point.getFloatPosition(1);
		final Roi roi = new PointRoi(x, y);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private Roi createLineROI(final PolygonOverlay overlay) {
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final RealLocalizable p1 = region.getVertex(0);
		final RealLocalizable p2 = region.getVertex(1);
		final double x1 = p1.getDoublePosition(0);
		final double y1 = p1.getDoublePosition(1);
		final double x2 = p2.getDoublePosition(0);
		final double y2 = p2.getDoublePosition(1);
		final Line line = new Line(x1, y1, x2, y2);
		assignPropertiesToRoi(line, overlay);
		return line;
	}

	private ShapeRoi createBinaryMaskRoi(final BinaryMaskOverlay overlay) {
		final RegionOfInterest roi = overlay.getRegionOfInterest();
		final double[] min = new double[roi.numDimensions()];
		roi.realMin(min);
		final double[] max = new double[roi.numDimensions()];
		roi.realMax(max);
		final int x = (int) Math.ceil(min[0]);
		final int y = (int) Math.ceil(min[1]);
		final int width = (int) Math.ceil(max[0]) - x + 1;
		final int height = (int) Math.ceil(max[1]) - y + 1;

		// TODO Readjust to account for 3+D binary masks.
		// Assume for now that the ROI is 2-d or that the desired plane is 0 for all
		// accessory dimensions.
		// Later we will have axes for overlays and we can pick the X and Y axes.
		// Later still, we will work out some mechanism for how all the planes are
		// sent to the legacy layer.
		//
		// We only want to return one ROI, so we have a single stack image.
		final ByteProcessor ip = new ByteProcessor(width, height);

		// set things so that true is between 1 and 3 and false is below 1
		ip.setThreshold(1, 3, ImageProcessor.NO_LUT_UPDATE);
		final RealRandomAccess<BitType> ra = roi.realRandomAccess();

		// this picks a plane at the minimum Z, T, etc within the ROI
		ra.setPosition(min);
		for (int i = 0; i < width; i++) {
			ra.setPosition(i + x, 0);
			for (int j = 0; j < height; j++) {
				ra.setPosition(j + y, 1);
				ip.set(i, j, ra.get().get() ? 2 : 0);
			}
		}
		final ThresholdToSelection plugin = new ThresholdToSelection();

		final Roi imagejroi = plugin.convert(ip);
		imagejroi.setLocation(x, y);
		return new ShapeRoi(imagejroi);
	}

	private void
		assignPropertiesToRoi(final Roi roi, final Overlay overlay)
	{
		roi.setStrokeWidth((float) overlay.getLineWidth());
		roi.setStrokeColor(AWTColors.getColor(overlay.getLineColor()));
		roi.setFillColor(AWTColors.getColor(overlay.getFillColor()));
	}

	// -- Helper methods - IJ2 overlay creation --

	private void createOverlays(final Roi roi,
		final ArrayList<Overlay> overlays, int xOff, int yOff)
	{
		if (roi == null) return;

		Log.warn("====> Roi class = " + roi.getClass().getName());
		switch (roi.getType()) {
			case Roi.RECTANGLE:
				Log.warn("====> RECTANGLE: " + roi);
				overlays.add(createRectangleOverlay(roi, xOff, yOff));
				break;
			case Roi.OVAL:
				Log.warn("====> OVAL: " + roi);
				overlays.add(createEllipseOverlay(roi, xOff, yOff));
				break;
			case Roi.POLYGON:
				Log.warn("====> POLYGON: " + roi);
				overlays.add(createPolygonOverlay(roi, xOff, yOff));
				break;
			case Roi.FREEROI:
				Log.warn("====> FREEROI: " + roi);
				overlays.add(createDefaultOverlay(roi, xOff, yOff));
				break;
			case Roi.TRACED_ROI:
				Log.warn("====> TRACED_ROI: " + roi);
				overlays.add(createDefaultOverlay(roi, xOff, yOff));
				break;
			case Roi.LINE:
				Log.warn("====> LINE: " + roi);
				overlays.add(createLineOverlay(roi, xOff, yOff));
				break;
			case Roi.POLYLINE:
				Log.warn("====> POLYLINE: " + roi);
				// throw new UnsupportedOperationException("POLYLINE unimplemented");
				break;
			case Roi.FREELINE:
				Log.warn("====> FREELINE: " + roi);
				// throw new UnsupportedOperationException("FREELINE unimplemented");
				break;
			case Roi.ANGLE:
				Log.warn("====> ANGLE: " + roi);
				// throw new UnsupportedOperationException("ANGLE unimplemented");
				break;
			case Roi.COMPOSITE:
				Log.warn("====> COMPOSITE: " + roi);
				final ShapeRoi shapeRoi = (ShapeRoi) roi;
				final Roi[] rois = shapeRoi.getRois();
				xOff += xOff + shapeRoi.getBounds().x;
				yOff += shapeRoi.getBounds().y;
				final ArrayList<Overlay> subOverlays = new ArrayList<Overlay>();
				for (final Roi r : rois)
					createOverlays(r, subOverlays, xOff, yOff);
				for (final Overlay overlay : subOverlays) {
					assignPropertiesToOverlay(overlay, shapeRoi);
				}
				if (subOverlays.size() == 1) {
					overlays.add(subOverlays.get(0));
					return;
				}
				final CompositeRegionOfInterest croi =
					new CompositeRegionOfInterest(2);
				for (final Overlay overlay : subOverlays) {
					final RegionOfInterest subRoi = overlay.getRegionOfInterest();
					if (subRoi == null) {
						Log.warn(String.format("Can't composite %s", overlay.toString()));
					}
					else {
						croi.xor(subRoi);
					}
				}
				final CompositeOverlay coverlay = new CompositeOverlay(croi);
				/*
				 * An arbitrary guess - set the fill color to red with a 1/3 alpha
				 */
				coverlay.setFillColor(new ColorRGB(255, 0, 0));
				coverlay.setAlpha(80);
				overlays.add(coverlay);
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

	private Overlay createLineOverlay(final Roi roi, final int xOff,
		final int yOff)
	{
		assert roi instanceof Line;
		final Line line = (Line) roi;
		final LineOverlay lineOverlay =
			new LineOverlay(new RealPoint(line.x1d + xOff, line.y1d + yOff),
				new RealPoint(line.x2d + xOff, line.y2d + yOff));
		assignPropertiesToOverlay(lineOverlay, roi);
		return lineOverlay;
	}

	private RectangleOverlay createRectangleOverlay(final Roi roi,
		final int xOff, final int yOff)
	{
		final RectangleOverlay overlay = new RectangleOverlay();
		final RectangleRegionOfInterest region = overlay.getRegionOfInterest();
		final Rectangle bounds = roi.getBounds();
		region.setOrigin(bounds.x + xOff, 0);
		region.setOrigin(bounds.y + yOff, 1);
		region.setExtent(bounds.width, 0);
		region.setExtent(bounds.height, 1);
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private EllipseOverlay createEllipseOverlay(final Roi roi, final int xOff,
		final int yOff)
	{
		final EllipseOverlay overlay = new EllipseOverlay();
		final EllipseRegionOfInterest region = overlay.getRegionOfInterest();
		final Rectangle bounds = roi.getBounds();
		final double radiusX = ((bounds.width)) / 2.0;
		final double radiusY = ((bounds.height)) / 2.0;
		region.setOrigin(bounds.x + radiusX + xOff, 0);
		region.setOrigin(bounds.y + radiusY + yOff, 1);
		region.setRadius(radiusX, 0);
		region.setRadius(radiusY, 1);
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private PolygonOverlay createPolygonOverlay(final Roi roi, final int xOff,
		final int yOff)
	{
		final PolygonRoi polygonRoi = (PolygonRoi) roi;

		final PolygonOverlay overlay = new PolygonOverlay();
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final int[] xCoords = polygonRoi.getXCoordinates();
		final int[] yCoords = polygonRoi.getYCoordinates();
		final int x0 = polygonRoi.getBounds().x;
		final int y0 = polygonRoi.getBounds().y;
		for (int i = 0; i < xCoords.length; i++) {
			final double x = xCoords[i] + x0, y = yCoords[i] + y0;
			region.addVertex(i, new RealPoint(x, y));
		}
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private Overlay createDefaultOverlay(final Roi roi, int xOff, int yOff) {
		final Rectangle bounds = roi.getBounds();
		final NativeImg<BitType, BitAccess> nativeImg =
			new ArrayImgFactory<BitType>().createBitInstance(new long[] {
				bounds.width, bounds.height }, 1);
		final BitType t = new BitType(nativeImg);
		nativeImg.setLinkedType(t);
		xOff = bounds.x;
		yOff = bounds.y;
		final Img<BitType> img =
			new ImgTranslationAdapter<BitType, Img<BitType>>(nativeImg, new long[] {
				xOff, yOff });
		final RandomAccess<BitType> ra = img.randomAccess();
		final ImageProcessor ip = roi.getMask();
		for (int i = xOff; i < xOff + bounds.width; i++) {
			ra.setPosition(i, 0);
			for (int j = yOff; j < yOff + bounds.height; j++) {
				ra.setPosition(j, 1);
				ra.get().set(ip.get(i - xOff, j - yOff) > 0);
			}
		}
		final BinaryMaskRegionOfInterest<BitType, Img<BitType>> broi =
			new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(img);
		return new BinaryMaskOverlay(broi);
	}

	private void assignPropertiesToOverlay(final Overlay overlay, final Roi roi)
	{
		overlay.setLineWidth(roi.getStrokeWidth());
		final Color strokeColor = roi.getStrokeColor();
		final Color fillColor = roi.getFillColor();
		if (strokeColor != null) {
			overlay.setLineColor(AWTColors.getColorRGB(strokeColor));
		}
		if (fillColor != null) {
			overlay.setFillColor(AWTColors.getColorRGBA(fillColor));
		}
	}

}
