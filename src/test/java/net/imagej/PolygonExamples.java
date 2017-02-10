
package net.imagej;

import net.imglib2.roi.Regions;
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

		// doesn't work because can't display real space object
		ij.ui().show(hexagon);

		// rasterize
		ij.ui().show(Regions.rasterize(hexagon));
		ij.ui().show(Regions.rasterize(selfIntersect));
		ij.ui().show(Regions.rasterize(combined));
	}
}
