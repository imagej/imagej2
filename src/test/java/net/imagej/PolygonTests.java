
package net.imagej;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.roi.geometric.GeomRegions;
import net.imglib2.roi.geometric.Polygon2D;
import net.imglib2.type.logic.BoolType;

import org.junit.Before;
import org.junit.Test;

public class PolygonTests {

	// -- Hexagon --

	public static Polygon2D createHexagon() {
		final List<RealLocalizable> points = new ArrayList<>();
		points.add(new RealPoint(new double[] { 0, 80 }));
		points.add(new RealPoint(new double[] { 40, 110 }));
		points.add(new RealPoint(new double[] { 80, 80 }));
		points.add(new RealPoint(new double[] { 80, 30 }));
		points.add(new RealPoint(new double[] { 40, 0 }));
		points.add(new RealPoint(new double[] { 0, 30 }));

		return GeomRegions.polygon2D(points);
	}

	@Test
	public void testHexagon() {
		final Polygon2D hexagon = createHexagon();
		final RealRandomAccess<BoolType> ra = hexagon.realRandomAccess();

		// Vertices
		ra.setPosition(new double[] { 0, 80 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 40, 110 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 80, 80 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 80, 30 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 40, 0 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 0, 30 });
		assertTrue(ra.get().get());
	}

	// -- Self intersecting polygon --

	public static Polygon2D createSelfIntersectingPolygon() {
		final List<RealLocalizable> points = new ArrayList<>();
		points.add(new RealPoint(new double[] { 70, 100 }));
		points.add(new RealPoint(new double[] { 30, 0 }));
		points.add(new RealPoint(new double[] { 15, 80 }));
		points.add(new RealPoint(new double[] { 120, 80 }));
		points.add(new RealPoint(new double[] { 120, 10 }));
		points.add(new RealPoint(new double[] { 0, 10 }));
		points.add(new RealPoint(new double[] { 100, 50 }));

		return GeomRegions.polygon2D(points);
	}

	@Test
	public void testSelfIntersect() {
		final Polygon2D selfIntersect = createSelfIntersectingPolygon();
		final RealRandomAccess<BoolType> ra = selfIntersect.realRandomAccess();

		// Vertices
		ra.setPosition(new double[] { 70, 100 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 30, 0 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 15, 80 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 120, 80 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 120, 10 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 0, 10 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 100, 50 });
		assertTrue(ra.get().get());
	}

	// -- Combined polygons --

	public static Polygon2D createCombinedPolygons() {
		final RealPoint origin = new RealPoint(new double[] { 0, 0 });

		final List<RealLocalizable> points = new ArrayList<>();

		// Create square
		points.add(origin);
		points.add(new RealPoint(new double[] { 1, 1 }));
		points.add(new RealPoint(new double[] { 1, 100 }));
		points.add(new RealPoint(new double[] { 100, 100 }));
		points.add(new RealPoint(new double[] { 100, 1 }));
		points.add(new RealPoint(new double[] { 1, 1 }));

		// Create triangle
		points.add(origin);
		points.add(new RealPoint(new double[] { 20, 60 }));
		points.add(new RealPoint(new double[] { 80, 60 }));
		points.add(new RealPoint(new double[] { 50, 20 }));
		points.add(new RealPoint(new double[] { 20, 60 }));

		// Create pentagon
		points.add(origin);
		points.add(new RealPoint(new double[] { 150, 20 }));
		points.add(new RealPoint(new double[] { 150, 80 }));
		points.add(new RealPoint(new double[] { 200, 120 }));
		points.add(new RealPoint(new double[] { 250, 80 }));
		points.add(new RealPoint(new double[] { 250, 20 }));
		points.add(new RealPoint(new double[] { 150, 20 }));

		// Create rectangle
		points.add(origin);
		points.add(new RealPoint(new double[] { 90, 40 }));
		points.add(new RealPoint(new double[] { 90, 60 }));
		points.add(new RealPoint(new double[] { 220, 60 }));
		points.add(new RealPoint(new double[] { 220, 40 }));
		points.add(new RealPoint(new double[] { 90, 40 }));
		points.add(origin);

		return GeomRegions.polygon2D(points);
	}

	@Test
	public void testCombined() {
		final Polygon2D combined = createCombinedPolygons();
		final RealRandomAccess<BoolType> ra = combined.realRandomAccess();

		// Vertices
		// Square
		ra.setPosition(new double[] { 1, 1 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 1, 100 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 100, 100 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 100, 1 });
		assertFalse(ra.get().get());

		// Triangle
		ra.setPosition(new double[] { 20, 60 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 80, 60 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 50, 20 });
		assertTrue(ra.get().get());

		// Pentagon
		ra.setPosition(new double[] { 150, 20 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 150, 80 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 200, 120 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 250, 80 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 250, 20 });
		assertFalse(ra.get().get());

		// Rectangle
		ra.setPosition(new double[] { 90, 40 });
		assertFalse(ra.get().get());

		ra.setPosition(new double[] { 90, 60 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 220, 60 });
		assertTrue(ra.get().get());

		ra.setPosition(new double[] { 220, 40 });
		assertTrue(ra.get().get());
	}
}
