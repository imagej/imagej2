
package net.imagej;

import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geometric.Line;
import net.imglib2.roi.geometric.PointCollection;
import net.imglib2.roi.geometric.RasterizedRegion;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class OtherGeomExamples {

	private OtherGeomExamples() {
		// prevent instantiation of utility class
	}

	public static ImageJ launch(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		return ij;
	}

	public static void main(final String... args) {
		final ImageJ ij = launch(args);

		final Line line = OtherGeomTests.createLine();
		final RasterizedRegion<Line, BoolType> rline = Regions.rasterize(line);

		final Line lineSlopeNot1 = OtherGeomTests.createLineSlopeNot1();
		final RasterizedRegion<Line, BoolType> rLineSlopeNot1 = Regions.rasterize(
			lineSlopeNot1);

		final Line line3D = OtherGeomTests.createLineIn3D();
		final RasterizedRegion<Line, BoolType> rline3D = Regions.rasterize(line3D);

		// No method for rasterizing RealPoints or converting them to Points
		final RealPoint p = OtherGeomTests.createPoint();

		// PointCollections don't need to be rasterized, because they're already
		// in discrete space. They're IterableRegions.
		final PointCollection pc = OtherGeomTests.createPointCollection();

		ij.ui().show(rline);

		// Regions.raster(...) rasterizes by sampling the region at integer
		// coordinates, which causes lines without slope=1,0,or undef. to appear
		// broken
		ij.ui().show(rLineSlopeNot1);

		ij.ui().show(rline3D);

		// Doesn't display:
		// 1) (12.125, 77.03125) doesn't exist in discrete space
		// 2) Not a RandomAccessibleInterval, or an IterableInterval
		ij.ui().show(p);

		ij.ui().show(pc);
	}
}
