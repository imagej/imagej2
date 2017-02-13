
package net.imagej;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RealInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geometric.GeomRegions;
import net.imglib2.roi.geometric.HyperRectangle;
import net.imglib2.roi.geometric.RasterizedRegion;
import net.imglib2.type.logic.BoolType;

import org.junit.Test;

public class HyperRectangleTests {

	// -- Square --

	public static HyperRectangle createSquare() {
		return GeomRegions.hyperRectangle(new RealPoint(new double[] { 75, 75 }),
			new double[] { 75, 75 });
	}

	@Test
	public void testSquare() {
		final HyperRectangle square = createSquare();
		final RealRandomAccess<BoolType> rra = square.realRandomAccess();

		// corners
		rra.setPosition(new double[] { 0, 0 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 0, 150 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 150, 150 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 150, 0 });
		assertFalse(rra.get().get());

		// inside
		rra.setPosition(new double[] { 100, 30 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 0, 93 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 12, 0 });
		assertTrue(rra.get().get());

		// outside
		rra.setPosition(new double[] { 150, 126 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 8, 150 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 200, 3 });
		assertFalse(rra.get().get());
	}

	// -- Rectangle --

	public static HyperRectangle createRectangle() {
		return GeomRegions.hyperRectangle(new RealPoint(new double[] { 17.5,
			30.125 }), new double[] { 17.5, 30.125 });
	}

	@Test
	public void testRectangle() {
		final HyperRectangle rectangle = createRectangle();
		final RealRandomAccess<BoolType> rra = rectangle.realRandomAccess();

		// corners
		rra.setPosition(new double[] { 0, 0 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 0, 60.25 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 35, 60.25 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 35, 0 });
		assertFalse(rra.get().get());

		// inside
		rra.setPosition(new double[] { 12, 0.01 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 0, 30 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 15.0625, 0 });
		assertTrue(rra.get().get());

		// outside
		rra.setPosition(new double[] { 35, 11 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 1.25, 60.25 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 32, 70 });
		assertFalse(rra.get().get());
	}

	@Test
	public void testRasterizedRectangle() {
		final HyperRectangle rectangle = createRectangle();
		final RasterizedRegion<HyperRectangle, BoolType> rasterRectangle = Regions
			.rasterize(rectangle);
		final RandomAccess<BoolType> ra = rasterRectangle.randomAccess();

		// corners
		ra.setPosition(new long[] { 0, 0 });
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 0, 60 }); // included since it was truncated
		assertTrue(ra.get().get());

		ra.setPosition(new long[] { 35, 60 }); // still not in the region
		assertFalse(ra.get().get());

		ra.setPosition(new long[] { 35, 0 });
		assertFalse(ra.get().get());
	}

	// -- Sub-pixel rectangle --

	public static HyperRectangle createSubPixelRectangle() {
		return GeomRegions.hyperRectangle(new RealPoint(new double[] { 0.25,
			0.25 }), new double[] { 0.125, 0.0625 });
	}

	@Test
	public void testSubPixelRectangle() {
		final HyperRectangle subpixel = createSubPixelRectangle();
		final RealRandomAccess<BoolType> rra = subpixel.realRandomAccess();

		// corners
		rra.setPosition(new double[] { 0.125, 0.1875 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 0.125, 0.3125 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 0.375, 0.3125 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 0.375, 0.1875 });
		assertFalse(rra.get().get());

		// inside
		rra.setPosition(new double[] { 0.15, 0.2 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 0.125, 0.3 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 0.25, 0.1875 });
		assertTrue(rra.get().get());

		// outside
		rra.setPosition(new double[] { 0.375, 0.25 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 0.25, 0.3125 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 0.01, 0.026 });
		assertFalse(rra.get().get());
	}

	@Test
	public void testRasterizedSubPixelRectangle() {
		final HyperRectangle subpixel = createSubPixelRectangle();
		final RasterizedRegion<HyperRectangle, BoolType> rasterSubPixel = Regions
			.rasterize(subpixel);
		final RandomAccess<BoolType> ra = rasterSubPixel.randomAccess();

		// region has non-zero width/height
		assertEquals(rasterSubPixel.max(0), 1);
		assertEquals(rasterSubPixel.max(1), 1);
		assertEquals(rasterSubPixel.min(0), 0);
		assertEquals(rasterSubPixel.min(1), 0);

		// but contains no points
		ra.setPosition(new long[] { 0, 0 });
		assertFalse(ra.get().get());

		ra.setPosition(new long[] { 0, 1 });
		assertFalse(ra.get().get());

		ra.setPosition(new long[] { 1, 0 });
		assertFalse(ra.get().get());

		ra.setPosition(new long[] { 1, 1 });
		assertFalse(ra.get().get());
	}

	// -- Rotated rectangle --

	public static AffineRealRandomAccessible<BoolType, AffineGet>
		createRotatedRectangle()
	{
		final double angle = 30.0 * (Math.PI / 180.0);
		final double[][] rotationMatrix = new double[][] {
				{ Math.cos(angle), -Math.sin(angle) },
				{ Math.sin(angle), Math.cos(angle) }
			};
		return GeomRegions.hyperRectangle(new RealPoint(new double[] { 19, 23 }),
		new double[] { 10, 20 }, rotationMatrix);
	}

	@Test
	public void testRotatedRectangle() {
		final AffineRealRandomAccessible<BoolType, AffineGet> rotatedRectangle =
			createRotatedRectangle();
		final RealRandomAccess<BoolType> rra = rotatedRectangle.realRandomAccess();

		final double angle = 30.0 * (Math.PI / 180.0);

		// corners
		rra.setPosition(new double[] { 9 * Math.cos(angle) - 3 * Math.sin(angle) +
			19, 9 * Math.sin(angle) + 3 * Math.cos(angle) + 23 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 29 * Math.cos(angle) - 3 * Math.sin(angle) +
			19, 29 * Math.sin(angle) + 3 * Math.cos(angle) + 23 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 29 * Math.cos(angle) - 43 * Math.sin(angle) +
			19, 9 * Math.sin(angle) + 43 * Math.cos(angle) + 23 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 9 * Math.cos(angle) - 43 * Math.sin(angle) +
			19, 9 * Math.sin(angle) + 43 * Math.cos(angle) + 23 });
		assertFalse(rra.get().get());

		// inside
		rra.setPosition(new double[] { 36, 11 });
		assertTrue(rra.get().get());

		// outside
		rra.setPosition(new double[] { 9, 3 });
		assertFalse(rra.get().get());

		rra.setPosition(new double[] { 50, 3 });
		assertFalse(rra.get().get());
	}

	@Test
	public void testRasterizedRotatedRectangle() {
		final AffineRealRandomAccessible<BoolType, AffineGet> rotatedRectangle =
			createRotatedRectangle();

		// Cannot use Regions.raster on a RealRandomAccessible, so transform
		// into RealRandomAccessibleRealInterval
		final HyperRectangle sourceRotated = (HyperRectangle) rotatedRectangle
			.getSource();
		final double[] center = new double[sourceRotated.numDimensions()];
		sourceRotated.localize(center);
		final double[] semiaxes = new double[] { sourceRotated.getSemiAxisLength(0),
			sourceRotated.getSemiAxisLength(1) };
		final RealInterval interval = getCorners(semiaxes, center,
			(AffineTransform) rotatedRectangle.getTransformToSource().inverse());

		final RealRandomAccessibleRealInterval<BoolType> rriv =
				Regions.interval(rotatedRectangle, interval);

		// Now rasterize
		final RasterizedRegion<RealRandomAccessibleRealInterval<BoolType>, BoolType> rasterRotatedRectangle =
				Regions.rasterize(rriv);
		final RandomAccess<BoolType> ra = rasterRotatedRectangle.randomAccess();

		// inside
		ra.setPosition(new int[] {37, 11});
		assertTrue(ra.get().get());

		ra.setPosition(new int[] {18, 44});
		assertTrue(ra.get().get());

		// outside
		ra.setPosition(new int[] {5, 2});
		assertFalse(ra.get().get());

		ra.setPosition(new int[] {22, 38});
		assertFalse(ra.get().get());
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
