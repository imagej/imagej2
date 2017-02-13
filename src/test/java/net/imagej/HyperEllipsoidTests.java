
package net.imagej;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.RealInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geometric.GeomRegions;
import net.imglib2.roi.geometric.HyperEllipsoid;
import net.imglib2.roi.geometric.RasterizedRegion;
import net.imglib2.type.logic.BoolType;

import org.junit.Test;

public class HyperEllipsoidTests {

	// -- 2D HyperEllipsoid --

	public static HyperEllipsoid createHyperEllipsoid() {
		return GeomRegions.hyperEllipsoid(new RealPoint(new double[] { 20, 50 }),
			new double[] { 20, 50 });
	}

	@Test
	public void testCreateHyperEllipsoid() {
		final HyperEllipsoid he = createHyperEllipsoid();
		final RealRandomAccess<BoolType> rra = he.realRandomAccess();

		// ellipses contain all vertices
		rra.setPosition(new double[] { 0, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 40, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, 0 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, 100 });
		assertTrue(rra.get().get());

		// outside
		rra.setPosition(new double[] { 23, 100 });
		assertFalse(rra.get().get());
	}

	@Test
	public void testRasterHyperEllipsoid() {
		final HyperEllipsoid he = createHyperEllipsoid();
		final RasterizedRegion<HyperEllipsoid, BoolType> rhe = Regions.rasterize(
			he);
		final RandomAccess<BoolType> ra = rhe.randomAccess();

		ra.setPosition(new long[] { 0, 50 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 40, 50 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, 0 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, 100 });
		assertTrue(ra.get().get());
	}

	// -- 3D HyperEllipsoid --

	public static HyperEllipsoid create3DHyperEllipsoid() {
		return GeomRegions.hyperEllipsoid(new RealPoint(new double[] { 20, 40,
			50 }), new double[] { 20, 40, 50 });
	}

	@Test
	public void testCreate3DHyperEllipsoid() {
		final HyperEllipsoid he3 = create3DHyperEllipsoid();
		final RealRandomAccess<BoolType> rra = he3.realRandomAccess();

		// all vertices still contained in 3D
		rra.setPosition(new double[] { 0, 40, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 40, 40, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, 0, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, 80, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, 40, 0 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, 40, 100 });
		assertTrue(rra.get().get());

		// inside
		rra.setPosition(new double[] { 20, 40.1, 99.6 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 19, 58, 94 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 21, 45, 1 });
		assertTrue(rra.get().get());

		// outside
		rra.setPosition(new double[] { 20, -3, 50 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 11, 75.75, 51.5 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 30, 70, 25 });
		assertFalse(rra.get().get());
	}

	@Test
	public void testRaster3DHyperEllipsoid() {
		final HyperEllipsoid he3 = create3DHyperEllipsoid();
		final RasterizedRegion<HyperEllipsoid, BoolType> rhe3 = Regions.rasterize(
			he3);
		final RandomAccess<BoolType> ra = rhe3.randomAccess();

		ra.setPosition(new long[] { 0, 40, 50 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 40, 40, 50 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, 0, 50 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, 80, 50 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, 40, 0 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, 40, 100 });
		assertTrue(ra.get().get());
	}

	// -- 3D Rotated HyperEllipsoid --

	public static AffineRealRandomAccessible<BoolType, AffineGet>
		createRotated3DHyperEllipsoid()
	{
		final double angle = 70.0 * (Math.PI / 180.0);
		final double[][] matrix = new double[][] {
			{ 1, 0, 0 },
			{ 0, Math.cos(angle), -Math.sin(angle) },
			{ 0, Math.sin(angle), Math.cos(angle) }
		};
		return GeomRegions.hyperEllipsoid(new RealPoint(new double[] { 20, 40,
			50 }), new double[] { 20, 40, 50 }, matrix);
	}

	@Test
	public void testCreateRotated3DHyperEllipsoid() {
		final double angle = 70.0 * (Math.PI / 180.0);
		final AffineRealRandomAccessible<BoolType, AffineGet> rotated =
			createRotated3DHyperEllipsoid();
		final RealRandomAccess<BoolType> rra = rotated.realRandomAccess();

		rra.setPosition(new double[] { 0, 40, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 40, 40, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, -40 * Math.cos(angle) + 40, -40 * Math
			.sin(angle) + 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, 40 * Math.cos(angle) + 40, 40 * Math.sin(
			angle) + 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 20, -50 * -Math.sin(angle) + 40, -50 * Math
			.cos(angle) + 50 });
		assertTrue(rra.get().get());

		final double y = Math.round((-50 * Math.sin(angle) + 40) * 10.0e13) /
			10.0e13; // rounding error
		rra.setPosition(new double[] { 20, y, 50 * Math.cos(angle) + 50 });
		assertTrue(rra.get().get());

		// inside
		rra.setPosition(new double[] { 20, -3, 50 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 11, 75.75, 51.5 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 30, 70, 25 });
		assertTrue(rra.get().get());

		// outside
		rra.setPosition(new double[] { 20, 40.1, 99.6 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 19, 58, 94 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 21, 45, 1 });
		assertFalse(rra.get().get());
	}

	@Test
	public void testRasterRotated3DHyperEllipsoid() {
		final double angle = 70.0 * (Math.PI / 180.0);
		final AffineRealRandomAccessible<BoolType, AffineGet> rotated =
			createRotated3DHyperEllipsoid();

		// Add interval to rotated ellipse then rasterize
		final HyperEllipsoid sourceRotated = (HyperEllipsoid) rotated.getSource();
		final double[] center = new double[sourceRotated.numDimensions()];
		sourceRotated.localize(center);
		final double[] semiaxes = new double[] { sourceRotated.getSemiAxisLength(0),
			sourceRotated.getSemiAxisLength(1), sourceRotated.getSemiAxisLength(2) };
		final RealInterval interval = getCorners(semiaxes, center,
			(AffineTransform) rotated.getTransformToSource().inverse());

		final RealRandomAccessibleRealInterval<BoolType> rotatedInterval = Regions
			.interval(rotated, interval);
		final RasterizedRegion<RealRandomAccessibleRealInterval<BoolType>, BoolType> rasterRotated =
			Regions.rasterize(rotatedInterval);

		final RandomAccess<BoolType> ra = rasterRotated.randomAccess();

		ra.setPosition(new long[] { 0, 40, 50 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 40, 40, 50 });
		assertTrue(ra.get().get());

		// need to take ceiling of second term for this vertex
		ra.setPosition(new long[] { 20, (long) Math.floor(-40 * Math.cos(angle) +
			40), (long) Math.ceil(-40 * Math.sin(angle) + 50) });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, (long) Math.floor(40 * Math.cos(angle) +
			40), (long) Math.floor(40 * Math.sin(angle) + 50) });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, (long) Math.floor(-50 * -Math.sin(angle) +
			40), (long) Math.floor(-50 * Math.cos(angle) + 50) });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 20, (long) Math.floor(-50 * Math.sin(angle) +
			40), (long) Math.floor(50 * Math.cos(angle) + 50) });
		assertTrue(ra.get().get());
	}

	// -- Helper methods --

	// Display because the center is different, such that when displayed its
	// lower bound is (0, 0, 0) ensuring nothing gets cut off
	public static AffineRealRandomAccessible<BoolType, AffineGet>
		createRotated3DHyperEllipsoidDisplay()
	{
		final double angle = 70.0 * (Math.PI / 180.0);
		final double[][] matrix = new double[][] { { 1, 0, 0 }, { 0, Math.cos(
			angle), -Math.sin(angle) }, { 0, Math.sin(angle), Math.cos(angle) } };
		return GeomRegions.hyperEllipsoid(new RealPoint(new double[] { 20, 61,
			55 }), new double[] { 20, 40, 50 }, matrix);
	}

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
