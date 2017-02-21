
package net.imagej;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geometric.GeomRegions;
import net.imglib2.roi.geometric.Polygon2D;

public class PolygonExamples {

	private PolygonExamples() {
		// prevent instantiation of utility class
	}

	public static ImageJ launch(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		return ij;
	}

	public static void main(final String... args) {
		final ImageJ ij = launch(args);

		final Polygon2D hexagon = PolygonTests.createHexagon();
		final Polygon2D selfIntersect = PolygonTests
			.createSelfIntersectingPolygon();
		final Polygon2D combined = PolygonTests.createCombinedPolygons();
		
		final List<RealLocalizable> pt = new ArrayList<>();
		pt.add(new RealPoint(new double[] {0, 0, 0}));
		pt.add(new RealPoint(new double[] {20, 20, 20}));
		pt.add(new RealPoint(new double[] {30, 30, 30}));
		
		final Polygon2D p = polygon3D();

		// doesn't work because can't display real space object
		ij.ui().show(hexagon);

		// rasterize
		ij.ui().show(Regions.rasterize(hexagon));
		ij.ui().show(Regions.rasterize(selfIntersect));
		ij.ui().show(Regions.rasterize(combined));
		// empty region, because Polygon2D is only intended for 2D polygons
		ij.ui().show(Regions.rasterize(p));
	}

	// Can't be created in PolygonTests because of assert(...) in Polygon2D
	// constructor, but can be created here.
	public static Polygon2D polygon3D() {
		final List<RealLocalizable> pt = new ArrayList<>();
		pt.add(new RealPoint(new double[] { 0, 0, 0 }));
		pt.add(new RealPoint(new double[] { 20, 20, 20 }));
		pt.add(new RealPoint(new double[] { 30, 30, 30 }));

		return GeomRegions.polygon2D(pt);
	}
}
