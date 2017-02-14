
package net.imagej;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geometric.GeomRegions;
import net.imglib2.roi.geometric.Line;
import net.imglib2.roi.geometric.PointCollection;
import net.imglib2.roi.geometric.RasterizedRegion;
import net.imglib2.type.logic.BoolType;

import org.junit.Test;

public class OtherGeomTests {

	// -- Line slope = 1 --

	public static Line createLine() {
		return GeomRegions.line(new double[] { 0, 0 }, new double[] { 100, 100 });
	}

	@Test
	public void testCreateLine() {
		final Line line = createLine();
		final RealRandomAccess<BoolType> rra = line.realRandomAccess();

		// contains endpoints
		rra.setPosition(new double[] { 0, 0 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 100, 100 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 4, 4 });
		assertTrue(rra.get().get());

		// doesn't contain points outside of the endpoints, even though they fall
		// on the same infinite length line
		rra.setPosition(new double[] { 100.01, 100.01 });
		assertFalse(rra.get().get());
	}

	@Test
	public void testRasterLine() {
		final Line line = OtherGeomTests.createLine();
		final RasterizedRegion<Line, BoolType> rline = Regions.rasterize(line);

		// make rasterized region iterable
		final IterableRegion<BoolType> iline = Regions.iterable(rline);
		final Cursor<Void> c = iline.cursor();

		int count = 0;
		while (c.hasNext()) {
			c.next();
			count++;
		}

		assertEquals((count - 1) * Math.sqrt(2), line.getLength(), 0);
	}

	// -- Line slope = 1 in 3D --

	public static Line createLineIn3D() {
		return GeomRegions.line(new double[] { 0, 0, 0 }, new double[] { 100, 100,
			100 });
	}

	@Test
	public void testCreateLineIn3D() {
		final Line line3D = createLineIn3D();
		final RealRandomAccess<BoolType> rra = line3D.realRandomAccess();

		// endpoints
		rra.setPosition(new double[] { 0, 0, 0 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 100, 100, 100 });
		assertTrue(rra.get().get());

		// on line
		rra.setPosition(new double[] { 71, 71, 71 });
		assertTrue(rra.get().get());

		// off line
		rra.setPosition(new double[] { 71, 71.08, 71 });
		assertFalse(rra.get().get());
	}

	// -- Line slope = 3 (1/3) --

	public static Line createLineSlopeNot1() {
		return GeomRegions.line(new RealPoint(new double[] { 30, 100 }),
			new RealPoint(new double[] { 0, 0 }));
	}

	@Test
	public void testCreateLineSlopeNot1() {
		final Line lineSlopeNot1 = createLineSlopeNot1();
		final RealRandomAccess<BoolType> rra = lineSlopeNot1.realRandomAccess();

		// endpoints
		rra.setPosition(new double[] { 0, 0 });
		assertTrue(rra.get().get());

		rra.setPosition(new double[] { 30, 100 });
		assertTrue(rra.get().get());

		// on line
		rra.setPosition(new double[] { 14, (100.0 / 30.0) * 14.0 });
		assertTrue(rra.get().get());

		// off line
		rra.setPosition(new double[] { 22, 60 });
		assertFalse(rra.get().get());
	}

	@Test
	public void testRasterLineSlopeNot1() {
		final Line lineSlopeNot1 = OtherGeomTests.createLineSlopeNot1();
		final RasterizedRegion<Line, BoolType> rLineSlopeNot1 = Regions.rasterize(
			lineSlopeNot1);

		// make rasterized region iterable
		final IterableRegion<BoolType> iLineSlopeNot1 = Regions.iterable(
			rLineSlopeNot1);
		final Cursor<Void> c = iLineSlopeNot1.cursor();

		int count = 0;
		while (c.hasNext()) {
			c.next();
			count++;
		}

		assertTrue((count - 1) * Math.sqrt(2) != lineSlopeNot1.getLength());
		// 30/3 + 1 = 11
		assertEquals(count, 11);

		/*
		 * Regions.raster() creates a RasterizedRegion by sampling the given real
		 * region at integer coordinates. This creates an issue with lines, which
		 * may only have values at a few integer coordinates. Should we raster lines
		 * differently (bresenham maybe)?
		 *
		 * public static RasterizedLine raster(Line l) { ... }
		 * RasterizedLine rline = Regions.raster(line);
		 *
		 * Or raster them the same as everything else, but just find
		 * a way of displaying lines so they don't disappear at certain points
		 * (possibly with a level set)?
		 */
	}

	// -- Point --

	public static RealPoint createPoint() {
		return GeomRegions.point(new double[] { 12.125, 77.03125 });
	}

	@Test
	public void testCreatePoint() {
		final RealPoint p = createPoint();
		final double[] pos = new double[p.numDimensions()];
		p.localize(pos);

		assertEquals(p.getDoublePosition(0), pos[0], 0);
		assertEquals(p.getDoublePosition(1), pos[1], 0);
		/*
		 * RealPoints can't be rasterized. They could be converted to Points. Even
		 * then, RealPoints are not RealRandomAccessibleRealIntervals and are thus
		 * isolated from a lot of the API (i.e. iterating, combining, etc.).
		 */
	}

	// -- PointCollection --

	public static PointCollection createPointCollection() {
		final List<Localizable> points = new ArrayList<>();
		points.add(new Point(new int[] { 0, 0 }));
		points.add(new Point(new int[] { 4, 11 }));
		points.add(new Point(new int[] { 103, 40 }));
		points.add(new Point(new int[] { 44, 44 }));
		points.add(new Point(new int[] { 27, 100 }));
		points.add(new Point(new int[] { 53, 86 }));
		points.add(new Point(new int[] { 89, 146 }));
		points.add(new Point(new int[] { 74, 201 }));
		return GeomRegions.pointCollection(points);
	}

	@Test
	public void testCreatePointCollection() {
		final PointCollection pc = OtherGeomTests.createPointCollection();
		final RandomAccess<BoolType> ra = pc.randomAccess();
		final Cursor<Void> c = pc.cursor(); // PointCollections are IterableRegions
		final List<Localizable> points = new ArrayList<>();
		points.add(new Point(new int[] { 0, 0 }));
		points.add(new Point(new int[] { 4, 11 }));
		points.add(new Point(new int[] { 103, 40 }));
		points.add(new Point(new int[] { 44, 44 }));
		points.add(new Point(new int[] { 27, 100 }));
		points.add(new Point(new int[] { 53, 86 }));
		points.add(new Point(new int[] { 89, 146 }));
		points.add(new Point(new int[] { 74, 201 }));

		// region contains all points
		for (int i = 0; i < points.size(); i++) {
			ra.setPosition(points.get(i));
			assertTrue(ra.get().get());
		}

		// can iterate through all points contained in region
		int pt = 0;
		while (c.hasNext()) {
			c.next();
			pt++;
		}

		assertEquals(points.size(), pt);

		// not in region
		ra.setPosition(new int[] { 12, 12 });
		assertFalse(ra.get().get());

		ra.setPosition(new int[] { 100, 105 });
		assertFalse(ra.get().get());
		/*
		 * There's not real space equivalent for PointCollection. Would it make
		 * sense to create a RealPointCollection, and then create a method in
		 * Regions which converts a RealPointCollection to a PointCollection?
		 *
		 * public static PointCollection raster( RealPointCollection rpc ){ ... }
		 *
		 * RealPointCollection rpc = GeomRegions.realPointCollection(Collection<? extends RealLocalizable> points);
		 * PointCollection pc = Regions.raster(rpc);
		 *
		 * This way GeomRegions is only for getting real space geometric objects.
		 */
	}
}
