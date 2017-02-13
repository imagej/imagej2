
package net.imagej;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geometric.HyperRectangle;
import net.imglib2.roi.geometric.RasterizedRegion;
import net.imglib2.type.logic.BoolType;

public class HyperRectangleExamples {

	private HyperRectangleExamples() {
		// prevent instantiation of utility class
	}

	public static ImageJ launch(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		return ij;
	}

	public static void main(final String... args) {
		final ImageJ ij = launch(args);

		// Create HyperRectangles(see HyperRectangleTests for details)
		final HyperRectangle square = HyperRectangleTests.createSquare();
		final HyperRectangle rectangle = HyperRectangleTests.createRectangle();
		final HyperRectangle subpixel = HyperRectangleTests
			.createSubPixelRectangle();
		final AffineRealRandomAccessible<BoolType, AffineGet> rotated =
			HyperRectangleTests.createRotatedRectangle();

		// Rasterize HyperRectangles so they can be displayed
		final RasterizedRegion<HyperRectangle, BoolType> rasterSquare = Regions
			.rasterize(square);
		final RasterizedRegion<HyperRectangle, BoolType> rasterRectangle = Regions
				.rasterize(rectangle);
		final RasterizedRegion<HyperRectangle, BoolType> rasterSubpixel = Regions
			.rasterize(subpixel);

		// final RasterizedRegion<AffineRealRandomAccessible<BoolType, AffineGet>,
		// BoolType> rasterRotated =
		// Regions.rasterize(rotated);
		// Doesn't work because Regions.rasterize(...) works on
		// RealRandomAccessibleRealIntervals, not RealRandomAccessibles

		// Instead apply interval
		final HyperRectangle sourceRotated = (HyperRectangle) rotated.getSource();
		final double[] center = new double[sourceRotated.numDimensions()];
		sourceRotated.localize(center);
		final double[] semiaxes = new double[] { sourceRotated.getSemiAxisLength(0),
			sourceRotated.getSemiAxisLength(1) };
		final RealInterval interval = getCorners(semiaxes, center,
			(AffineTransform) rotated.getTransformToSource().inverse());

		// user specifies the interval
		final RealRandomAccessibleRealInterval<BoolType> riv = Regions.interval(
			rotated, interval);

		// now it can be rasterized
		final RasterizedRegion<RealRandomAccessibleRealInterval<BoolType>, BoolType> rasterRotated =
			Regions.rasterize(riv);

		// display
		ij.ui().show(rasterSquare);
		ij.ui().show(rasterRectangle);
		ij.ui().show(rasterSubpixel);
		ij.ui().show(rasterRotated);
	}

	// -- Helper methods --

	private static RealInterval getCorners(final double[] semiAxes,
		final double[] center, final AffineTransform transform)
	{
		final int dim = center.length;
		final List<RealLocalizable> corners = new ArrayList<>();
		final double numCorners = Math.pow(2, dim);

		for (int p = 0; p < numCorners; p++) {
			corners.add(new RealPoint(dim));
		}

		double change = numCorners / 2;
		for (int n = 0; n < dim; n++) {
			double axis = semiAxes[n];
			for (int p = 0; p < numCorners; p++) {
				if (p % change == 0) axis = -axis;
				final RealPoint vert = (RealPoint) corners.get(p);
				vert.setPosition(center[n] + axis, n);
			}
			change = change / 2;
		}

		for (int p = 0; p < numCorners; p++) {
			final RealPoint vert = (RealPoint) corners.get(p);
			final double[] source = new double[dim];
			final double[] target = new double[dim];

			vert.localize(source);
			transform.apply(source, target);
			vert.setPosition(target);
		}

		return Regions.getBoundsReal(corners);
	}
}
