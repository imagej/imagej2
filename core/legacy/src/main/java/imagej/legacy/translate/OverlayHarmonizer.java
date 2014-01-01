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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.legacy.translate;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.overlay.AngleOverlay;
import imagej.data.overlay.BinaryMaskOverlay;
import imagej.data.overlay.EllipseOverlay;
import imagej.data.overlay.GeneralPathOverlay;
import imagej.data.overlay.LineOverlay;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.PointOverlay;
import imagej.data.overlay.PolygonOverlay;
import imagej.data.overlay.RectangleOverlay;
import imagej.data.overlay.TextOverlay;
import imagej.data.overlay.TextOverlay.Justification;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.threshold.ThresholdService;
import imagej.legacy.LegacyService;
import imagej.util.awt.AWTColors;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.BitArray;
import net.imglib2.img.transform.ImgTranslationAdapter;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.roi.EllipseRegionOfInterest;
import net.imglib2.roi.GeneralPathRegionOfInterest;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.roi.RectangleRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;

import org.scijava.AbstractContextual;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * OverlayTranslator translates regions of interest back and forth between
 * {@link Overlay}s and {@link ImagePlus} {@link Roi}s.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class OverlayHarmonizer extends AbstractContextual implements
	DisplayHarmonizer
{

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ThresholdService thresholdService;

	@Parameter
	private LogService log;

	public OverlayHarmonizer(final LegacyService legacyService) {
		setContext(legacyService.getContext());
	}

	/**
	 * Updates the given {@link ImageDisplay} to contain {@link Overlay}s
	 * corresponding to all the given {@link ImagePlus}'s Rois (both the active
	 * Roi and the Rois stored in ImageJ 1.x's current Overlay).
	 */
	@Override
	public void updateDisplay(final ImageDisplay display, final ImagePlus imp) {
		final List<Overlay> overlaysToRemove = overlayService.getOverlays(display);
		for (final Overlay overlay : overlaysToRemove) {
			overlayService.removeOverlay(display, overlay);
		}
		/*
		if (fullySelected(display, imp)) {
			for (DataView view : display)
				view.setSelected(true);
		}
		else {
		*/
		final List<Overlay> overlays = getOverlays(imp);
		overlayService.addOverlays(display, overlays);
		// }
		setModernThreshold(display, imp);
	}

	/**
	 * Updates the given {@link ImagePlus}'s Roi and Overlay to match the modern
	 * ImageJ {@link Overlay}s being visualized in the given {@link ImageDisplay}.
	 */
	@Override
	public void
		updateLegacyImage(final ImageDisplay display, final ImagePlus imp)
	{
		final List<Overlay> overlays = overlayService.getOverlays(display);
		setOverlays(overlays, overlayService.getActiveOverlay(display), imp);
		setLegacyThreshold(display, imp);
	}

	/**
	 * Extracts a list of {@link Overlay}s from the given {@link ImagePlus}. The
	 * Overlays are created from the {@link ImagePlus}' Roi and ImageJ 1.x
	 * Overlay.
	 */
	public List<Overlay> getOverlays(final ImagePlus imp) {
		Roi roi = imp.getRoi();
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		createOverlays(roi, overlays);
		final ij.gui.Overlay overlay = imp.getOverlay();
		if (overlay != null) {
			final ArrayList<Overlay> list = new ArrayList<Overlay>();
			for (int i = 0; i < overlay.size(); i++) {
				list.clear();
				roi = overlay.get(i);
				createOverlays(roi, list);
				overlays.addAll(list);
			}
		}
		return overlays;
	}

	/**
	 * Assigns a list of {@link Overlay}s to the given {@link ImagePlus}. The
	 * active overlay becomes the {@link Roi} of the ImagePlus. The other overlays
	 * become Roi's in the Overlay of the ImagePlus. Also populates legacy
	 * ImageJ's RoiManager.
	 */
	public void setOverlays(List<Overlay> overlays, Overlay activeOverlay,
		final ImagePlus imp)
	{
		final Roi roi = createRoi(activeOverlay);
		final ij.gui.Overlay o = createIJ1Overlay(overlays, activeOverlay);
		imp.setRoi(roi);
		imp.setOverlay(o);
		/*
		 * BDZ 4-18-13 disabling for now. Especially because implementation is
		 * broken since called muiltple times per plugin run and thus reset is
		 * called too often and results unpredictable.
		 * 
		// now make sure the correct rois end up in the RoiManager
		RoiManager mgr = RoiManager.getInstance();
		if (mgr == null) mgr = new RoiManager();
		mgr.runCommand("reset");
		if (roi != null) mgr.addRoi(roi);
		*/

		// TODO: do the rois in the IJ1 Overlay belong in the manager? In IJ1 if you
		// load a file that has a saved overlay it does not auto fill into Roi Mgr.
		// However when you select ROIs in Mgr and run Overlay > From ROI Manager
		// the rois go into the overlay and stay in the Roi Mgr. Decide later.

		// NB 4-18-13 - BDZ and CTR discussed and we'd eventually like to translate
		// all overlays into the ROI Manager and not translate any ROIs into the
		// Overlay of any ImagePlus. Haven't yet thought this through.
	}

	// -- Helper methods - legacy Roi creation --

	private void setModernThreshold(ImageDisplay display, ImagePlus imp) {
		ImageProcessor proc = imp.getProcessor();
		double threshMin = proc.getMinThreshold();
		double threshMax = proc.getMaxThreshold();
		if (threshMin == ImageProcessor.NO_THRESHOLD) {
			if (thresholdService.hasThreshold(display)) {
				thresholdService.removeThreshold(display);
			}
		}
		else { // an IJ1 thresh exists
			ThresholdOverlay thresh = thresholdService.getThreshold(display);
			thresh.setRange(threshMin, threshMax);
		}
	}

	private void setLegacyThreshold(ImageDisplay display, ImagePlus imp) {
		ImageProcessor proc = imp.getProcessor();
		if (thresholdService.hasThreshold(display)) {
			ThresholdOverlay thresh = thresholdService.getThreshold(display);
			double min = thresh.getRangeMin();
			double max = thresh.getRangeMax();
			proc.setThreshold(min, max, ImageProcessor.NO_LUT_UPDATE);
		}
		else {
			proc.resetThreshold();
		}
	}

	private ij.gui.Overlay createIJ1Overlay(final List<Overlay> overlays,
		Overlay activeOverlay)
	{
		List<Roi> rois = new ArrayList<Roi>();
		for (Overlay o : overlays) {
			if (o != activeOverlay) {
				Roi roi = createRoi(o);
				if (roi != null) rois.add(roi);
			}
		}
		if (rois.size() == 0) return null;
		ij.gui.Overlay overlay = new ij.gui.Overlay();
		for (Roi roi : rois) {
			overlay.add(roi);
		}
		return overlay;
	}

	private Roi createRoi(final Overlay overlay) {
		Roi roi = null;

		if (overlay instanceof RectangleOverlay) {
			roi = createRectangleRoi((RectangleOverlay) overlay);
		}
		if (overlay instanceof EllipseOverlay) {
			roi = createEllipseRoi((EllipseOverlay) overlay);
		}
		if (overlay instanceof PolygonOverlay) {
			roi = createPolygonRoi((PolygonOverlay) overlay);
		}
		if (overlay instanceof GeneralPathOverlay) {
			roi = createGeneralPathRoi((GeneralPathOverlay) overlay);
		}
		if (overlay instanceof BinaryMaskOverlay) {
			roi = createBinaryMaskRoi((BinaryMaskOverlay<?, ?>) overlay);
		}
		if (overlay instanceof LineOverlay) {
			roi = createLineRoi((LineOverlay) overlay);
		}
		if (overlay instanceof PointOverlay) {
			roi = createPointRoi((PointOverlay) overlay);
		}
		if (overlay instanceof AngleOverlay) {
			roi = createAngleRoi((AngleOverlay) overlay);
		}
		if (overlay instanceof TextOverlay) {
			roi = createTextRoi((TextOverlay) overlay);
		}
		// TODO: arrows, freehand, text
//		throw new UnsupportedOperationException("Translation of " +
//			overlay.getClass().getName() + " is unimplemented");

		return roi;
	}

	// NB - there is some overloading here with createLineRoi.
	
	// From a LineOverlay
	private Roi createLineRoi(final LineOverlay overlay) {
		double[] p1 = new double[overlay.numDimensions()];
		double[] p2 = new double[overlay.numDimensions()];
		overlay.getLineStart(p1);
		overlay.getLineEnd(p2);
		return createLineRoi(overlay, p1, p2);
	}

	// From a PolygonOverlay that has two points
	private Roi createLineRoi(final PolygonOverlay overlay) {
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		double[] p1 = new double[overlay.numDimensions()];
		double[] p2 = new double[overlay.numDimensions()];
		final RealLocalizable vp1 = region.getVertex(0);
		final RealLocalizable vp2 = region.getVertex(1);
		vp1.localize(p1);
		vp2.localize(p2);
		return createLineRoi(overlay, p1, p2);
	}

	// helper to support other createLineRoi() methods
	private Roi createLineRoi(Overlay overlay, double[] p1, double[] p2) {
		final double x1 = p1[0];
		final double y1 = p1[1];
		final double x2 = p2[0];
		final double y2 = p2[1];
		final Line line = new Line(x1, y1, x2, y2);
		assignPropertiesToRoi(line, overlay);
		return line;
	}
	
	private Roi createRectangleRoi(final RectangleOverlay overlay) {
		final RectangleRegionOfInterest region = overlay.getRegionOfInterest();
		final int dims = region.numDimensions();
		final double[] origin = new double[dims];
		final double[] extent = new double[dims];
		region.getOrigin(origin);
		region.getExtent(extent);
		final double x = origin[0], y = origin[1];
		final double w = extent[0], h = extent[1];
		final Roi roi = new Roi(x, y, w, h);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private Roi createEllipseRoi(final EllipseOverlay overlay) {
		final EllipseRegionOfInterest region = overlay.getRegionOfInterest();
		final int dims = region.numDimensions();
		final double[] origin = new double[dims];
		final double[] radii = new double[dims];
		region.getOrigin(origin);
		region.getRadii(radii);
		final double x = origin[0] - radii[0];
		final double y = origin[1] - radii[1];
		final double w = radii[0] * 2, h = radii[1] * 2;
		final Roi roi = new OvalRoi(x, y, w, h);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private Roi createPolygonRoi(final PolygonOverlay overlay) {
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final int vertexCount = region.getVertexCount();
		if (vertexCount == 1) return createPointRoi(overlay);
		if (vertexCount == 2) return createLineRoi(overlay);
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

	private Roi createGeneralPathRoi(final GeneralPathOverlay overlay) {
		final GeneralPathRegionOfInterest region = overlay.getRegionOfInterest();
		Roi roi = new ShapeRoi(region.getGeneralPath());
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	// NB - there is some overloading here with createPointRoi.
	
	// From a PolygonOverlay
	private Roi createPointRoi(final PolygonOverlay overlay) {
		PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		int nPoints = region.getVertexCount();
		float[] xPts = new float[nPoints];
		float[] yPts = new float[nPoints];
		for (int i = 0; i < nPoints; i++) {
			RealLocalizable vertex = region.getVertex(i);
			xPts[i] = vertex.getFloatPosition(0);
			yPts[i] = vertex.getFloatPosition(1);
		}
		PointRoi roi = new PointRoi(xPts, yPts, nPoints);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	// From a PointOverlay
	private Roi createPointRoi(final PointOverlay overlay) {
		List<double[]> points = overlay.getPoints();
		float[] xPts = new float[points.size()];
		float[] yPts = new float[points.size()];
		for (int i = 0; i < points.size(); i++) {
			double[] pt = points.get(i);
			xPts[i] = (float) pt[0];
			yPts[i] = (float) pt[1];
		}
		PointRoi roi = new PointRoi(xPts, yPts, points.size());
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private Roi createAngleRoi(final AngleOverlay overlay) {
		double[] pt = new double[overlay.numDimensions()];
		overlay.getPoint1(pt);
		float xb = (float) pt[0];
		float yb = (float) pt[1];
		overlay.getCenter(pt);
		float xc = (float) pt[0];
		float yc = (float) pt[1];
		overlay.getPoint2(pt);
		float xe = (float) pt[0];
		float ye = (float) pt[1];
		float[] xpoints = new float[]{xb,xc,xe};
		float[] ypoints = new float[]{yb,yc,ye};
		Roi roi = new PolygonRoi(xpoints, ypoints, 3, Roi.ANGLE);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}
	
	private Roi createTextRoi(TextOverlay overlay) {
		RectangleRegionOfInterest region = overlay.getRegionOfInterest();
		double x = region.getOrigin(0);
		double y = region.getOrigin(1);
		TextRoi roi = new TextRoi(x, y, overlay.getText());
		switch (overlay.getJustification()) {
			case LEFT:
				roi.setJustification(TextRoi.LEFT);
				break;
			case CENTER:
				roi.setJustification(TextRoi.CENTER);
				break;
			case RIGHT:
				roi.setJustification(TextRoi.RIGHT);
				break;
			default:
				break;
		}
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private ShapeRoi createBinaryMaskRoi(final BinaryMaskOverlay<?, ?> overlay) {
		final RegionOfInterest region = overlay.getRegionOfInterest();
		final double[] min = new double[region.numDimensions()];
		region.realMin(min);
		final double[] max = new double[region.numDimensions()];
		region.realMax(max);
		// TODO - is there some way to have subpixel resolution with mask rois?
		final int x = (int) Math.ceil(min[0]);
		final int y = (int) Math.ceil(min[1]);
		final int width = (int) Math.ceil(max[0]) - x + 1;
		final int height = (int) Math.ceil(max[1]) - y + 1;

		// TODO Readjust to account for 3+D binary masks.
		// Assume for now that the Roi is 2-d or that the desired plane is 0 for all
		// accessory dimensions.
		// Later we will have axes for overlays and we can pick the X and Y axes.
		// Later still, we will work out some mechanism for how all the planes are
		// sent to the legacy layer.
		//
		// We only want to return one Roi, so we have a single stack image.
		final ByteProcessor ip = new ByteProcessor(width, height);

		// set things so that true is between 1 and 3 and false is below 1
		ip.setThreshold(1, 3, ImageProcessor.NO_LUT_UPDATE);
		final RealRandomAccess<BitType> ra = region.realRandomAccess();

		// this picks a plane at the minimum Z, T, etc within the Roi
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
		ShapeRoi roi = new ShapeRoi(imagejroi);
		assignPropertiesToRoi(roi, overlay);
		return roi;
	}

	private void assignPropertiesToRoi(final Roi roi, final Overlay overlay) {
		roi.setName(overlay.getName());
		roi.setStrokeWidth((float) overlay.getLineWidth());
		roi.setStrokeColor(AWTColors.getColor(overlay.getLineColor()));
		final Color fillColor = AWTColors.getColor(overlay.getFillColor());
		final Color colorWithAlpha =
			new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(),
				overlay.getAlpha());
		roi.setFillColor(colorWithAlpha);
	}

	// -- Helper methods - modern ImageJ overlay creation --

	/*
	private boolean fullySelected(ImageDisplay display, ImagePlus imp) {
		Roi roi = imp.getRoi();
		if (roi != null) {
			if (roi.getType() == Roi.RECTANGLE) {
				ImageDisplayService dispServ = ImageJ.get(ImageDisplayService.class);
				Dataset ds = dispServ.getActiveDataset(display);
				long[] dims = ds.getDims();
				// TODO - FIXME - assumes X and Y are at 0 & 1
				long imageWidth = dims[0];
				long imageHeight = dims[1];
				Rectangle rect = roi.getBounds();
				if ((rect.x == 0) && (rect.y == 0) &&
						(rect.width == imageWidth) && (rect.height == imageHeight))
					return true;
			}
		}
		return false;
	}
	*/

	private void createOverlays(final Roi roi, final ArrayList<Overlay> overlays)
	{
		if (roi == null) return;

		log.warn("====> Roi class = " + roi.getClass().getName());
		if (roi instanceof TextRoi) {
			log.warn("====> TEXT: " + roi);
			overlays.add(createTextOverlay(roi));
			return;
		}
		switch (roi.getType()) {
			case Roi.RECTANGLE:
				log.warn("====> RECTANGLE: " + roi);
				overlays.add(createRectangleOverlay(roi));
				break;
			case Roi.OVAL:
				log.warn("====> OVAL: " + roi);
				overlays.add(createEllipseOverlay(roi));
				break;
			case Roi.POLYGON:
				log.warn("====> POLYGON: " + roi);
				overlays.add(createPolygonOverlay(roi));
				break;
			case Roi.FREEROI:
				log.warn("====> FREEROI: " + roi);
				overlays.add(createPolygonOverlay(roi));
				break;
			case Roi.TRACED_ROI:
				log.warn("====> TRACED_ROI: " + roi);
				overlays.add(createPolygonOverlay(roi));
				break;
			case Roi.LINE:
				log.warn("====> LINE: " + roi);
				overlays.add(createLineOverlay(roi));
				break;
			case Roi.POLYLINE:
				log.warn("====> POLYLINE: " + roi);
				// TODO - implement this
				// throw new UnsupportedOperationException("POLYLINE unimplemented");
				break;
			case Roi.FREELINE:
				log.warn("====> FREELINE: " + roi);
				// TODO - implement this
				// throw new UnsupportedOperationException("FREELINE unimplemented");
				break;
			case Roi.ANGLE:
				log.warn("====> ANGLE: " + roi);
				overlays.add(createAngleOverlay(roi));
				break;
			case Roi.POINT:
				log.warn("====> POINT: " + roi);
				overlays.add(createPointOverlay(roi));
				break;
			case Roi.COMPOSITE:
				log.warn("====> COMPOSITE: " + roi);
				final ShapeRoi shapeRoi = (ShapeRoi) roi;
				overlays.add(createGeneralPathOverlay(shapeRoi));
				break;
			default:
				log.warn("====> OTHER (" + roi.getType() + ", " + "): " + roi);
				throw new UnsupportedOperationException("OTHER unimplemented");
		}
	}

	private Overlay createAngleOverlay(final Roi roi)
	{
		assert roi instanceof PolygonRoi;
		final PolygonRoi pRoi = (PolygonRoi) roi;
		final FloatPolygon poly = pRoi.getFloatPolygon();
		final double[] end1 = new double[] { poly.xpoints[0], poly.ypoints[0] };
		final double[] ctr = new double[] { poly.xpoints[1], poly.ypoints[1] };
		final double[] end2 = new double[] { poly.xpoints[2], poly.ypoints[2] };
		final AngleOverlay angleOverlay =
			new AngleOverlay(getContext(), ctr, end1, end2);
		assignPropertiesToOverlay(angleOverlay, roi);
		return angleOverlay;
	}
	
	private Overlay createLineOverlay(final Roi roi)
	{
		assert roi instanceof Line;
		final Line line = (Line) roi;
		final LineOverlay lineOverlay =
			new LineOverlay(getContext(), new double[] { line.x1d, line.y1d },
				new double[] { line.x2d, line.y2d });
		assignPropertiesToOverlay(lineOverlay, roi);
		return lineOverlay;
	}

	private RectangleOverlay createRectangleOverlay(final Roi roi)
	{
		final RectangleOverlay overlay = new RectangleOverlay(getContext());
		final RectangleRegionOfInterest region = overlay.getRegionOfInterest();
		final FloatPolygon poly = roi.getFloatPolygon();
		final Double bounds = poly.getFloatBounds();
		region.setOrigin(bounds.x, 0);
		region.setOrigin(bounds.y, 1);
		region.setExtent(bounds.width, 0);
		region.setExtent(bounds.height, 1);
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private EllipseOverlay createEllipseOverlay(final Roi roi)
	{
		final EllipseOverlay overlay = new EllipseOverlay(getContext());
		final EllipseRegionOfInterest region = overlay.getRegionOfInterest();
		final FloatPolygon poly = roi.getFloatPolygon();
		final Double bounds = poly.getFloatBounds();
		final double radiusX = bounds.width / 2.0;
		final double radiusY = bounds.height / 2.0;
		region.setOrigin(bounds.x + radiusX, 0);
		region.setOrigin(bounds.y + radiusY, 1);
		region.setRadius(radiusX, 0);
		region.setRadius(radiusY, 1);
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private PolygonOverlay createPolygonOverlay(final Roi roi)
	{
		assert roi instanceof PolygonRoi;
		final PolygonRoi polygonRoi = (PolygonRoi) roi;
		final PolygonOverlay overlay = new PolygonOverlay(getContext());
		final PolygonRegionOfInterest region = overlay.getRegionOfInterest();
		final FloatPolygon poly = polygonRoi.getFloatPolygon();
		final float[] xCoords = poly.xpoints;
		final float[] yCoords = poly.ypoints;
		for (int i = 0; i < xCoords.length; i++) {
			final double x = xCoords[i];
			final double y = yCoords[i];
			region.addVertex(i, new RealPoint(x, y));
		}
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private GeneralPathOverlay createGeneralPathOverlay(final Roi roi)
	{
		assert roi instanceof ShapeRoi;
		final ShapeRoi polygonRoi = (ShapeRoi) roi;
		final Rectangle bounds = polygonRoi.getBounds();
		final GeneralPathOverlay overlay = new GeneralPathOverlay(getContext());
		final GeneralPathRegionOfInterest region = overlay.getRegionOfInterest();
		region.reset();
		final double[] coords = new double[6];
		for (final PathIterator iterator =
			polygonRoi.getShape().getPathIterator(null); !iterator.isDone(); iterator
			.next())
		{
			int type = iterator.currentSegment(coords);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					region.moveTo(coords[0] + bounds.x, coords[1] + bounds.y);
					break;
				case PathIterator.SEG_LINETO:
					region.lineTo(coords[0] + bounds.x, coords[1] + bounds.y);
					break;
				case PathIterator.SEG_QUADTO:
					region.quadTo(coords[0] + bounds.x, coords[1] + bounds.y, coords[2] +
						bounds.x, coords[3] + bounds.y);
					break;
				case PathIterator.SEG_CUBICTO:
					region.cubicTo(coords[0] + bounds.x, coords[1] + bounds.y, coords[2] +
						bounds.x, coords[3] + bounds.y, coords[4] + bounds.x, coords[5] +
						bounds.y);
					break;
				case PathIterator.SEG_CLOSE:
					region.close();
					break;
				default:
					throw new RuntimeException("Unsupported segment type: " + type);
			}
		}
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private PointOverlay createPointOverlay(final Roi roi)
	{
		assert roi instanceof PointRoi;
		final PointRoi ptRoi = (PointRoi) roi;
		final FloatPolygon poly = ptRoi.getFloatPolygon();
		final List<double[]> points = new ArrayList<double[]>();
		for (int i = 0; i < poly.npoints; i++) {
			final double x = poly.xpoints[i];
			final double y = poly.ypoints[i];
			final double[] pt = new double[]{x,y};
			points.add(pt);
		}
		final PointOverlay overlay = new PointOverlay(getContext(), points);
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	@SuppressWarnings("unused")
	private Overlay createDefaultOverlay(final Roi roi)
	{
		final Rectangle bounds = roi.getBounds();
		final ArrayImg<BitType, BitArray> arrayImg =
			new ArrayImgFactory<BitType>().createBitInstance(new long[] {
				bounds.width, bounds.height }, 1);
		final BitType t = new BitType(arrayImg);
		arrayImg.setLinkedType(t);
		final int xOff = bounds.x;
		final int yOff = bounds.y;
		final Img<BitType> img =
			new ImgTranslationAdapter<BitType, Img<BitType>>(arrayImg, new long[] {
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
		final Overlay overlay =
			new BinaryMaskOverlay<BitType, Img<BitType>>(getContext(), broi);
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private Overlay createTextOverlay(final Roi roi)
	{
		assert roi instanceof TextRoi;
		final TextRoi tRoi = (TextRoi) roi;
		final Double bounds = tRoi.getFloatBounds();
		final double x = bounds.x;
		final double y = bounds.y;
		final TextOverlay overlay =
			new TextOverlay(getContext(), x, y, tRoi.getText());
		switch (tRoi.getJustification()) {
			case TextRoi.LEFT:
				overlay.setJustification(Justification.LEFT);
				break;
			case TextRoi.CENTER:
				overlay.setJustification(Justification.CENTER);
				break;
			case TextRoi.RIGHT:
				overlay.setJustification(Justification.RIGHT);
				break;
			default:
				break;
		}
		assignPropertiesToOverlay(overlay, roi);
		return overlay;
	}

	private void assignPropertiesToOverlay(final Overlay overlay, final Roi roi)
	{
		overlay.setName(roi.getName());
		overlay.setLineWidth(roi.getStrokeWidth());
		final Color strokeColor = roi.getStrokeColor();
		final Color fillColor = roi.getFillColor();
		if (strokeColor != null) {
			overlay.setLineColor(AWTColors.getColorRGB(strokeColor));
		}
		if (fillColor != null) {
			overlay.setFillColor(AWTColors.getColorRGBA(fillColor));
			overlay.setAlpha(fillColor.getAlpha());
		}
	}

}
