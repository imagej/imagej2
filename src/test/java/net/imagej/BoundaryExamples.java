
package net.imagej;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRealRandomAccessible;
import net.imglib2.roi.Regions;
import net.imglib2.roi.boundary.Boundary;
import net.imglib2.roi.geometric.GeomRegions;
import net.imglib2.roi.geometric.HyperRectangle;
import net.imglib2.roi.geometric.Line;
import net.imglib2.roi.geometric.Polygon2D;
import net.imglib2.roi.geometric.RasterizedRegion;
import net.imglib2.roi.operators.RealBinaryOperator;
import net.imglib2.type.logic.BoolType;

public class BoundaryExamples {

	private BoundaryExamples() {
		// prevent instantiation of utility class
	}

	public static ImageJ launch(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		return ij;
	}

	public static void main(final String... args) {
		final ImageJ ij = launch(args);

		boundaryCombined(ij);
		boundaryPolygon(ij);
		boundaryRectangle(ij);
		boundaryPointCollection(ij);
		boundaryLine(ij);
	}

	private static void boundaryCombined(final ImageJ ij) {
		final double angle = 45.0 * (Math.PI / 180.0);
		final double[][] matrix = new double[][] { { Math.cos(angle), -Math.sin(
			angle) }, { Math.sin(angle), Math.cos(angle) } };
		final AffineRealRandomAccessible<BoolType, AffineGet> rotatedHE =
			GeomRegions.hyperEllipsoid(new RealPoint(new double[] { 150, 100 }),
				new double[] { 50, 100 }, matrix);

		// Still need to add an interval to rotated shapes because the combination
		// methods only work on RealRandomAccessibleRealIntervals
		final RealRandomAccessibleRealInterval<BoolType> rotatedHEInterval = Regions
			.interval(rotatedHE, new FinalRealInterval(new double[] { 0, 0 },
				new double[] { 250, 200 }));

		final HyperRectangle hr = GeomRegions.hyperRectangle(new RealPoint(
			new double[] { 210, 50 }), new double[] { 100, 45 });

		final RealBinaryOperator<BoolType> or = Regions.union(rotatedHEInterval,
			hr);
		final RasterizedRegion<RealBinaryOperator<BoolType>, BoolType> ror = Regions
			.rasterize(or);

		final Boundary<BoolType> boundaryCombine = new Boundary<>(ror,
			Boundary.StructuringElement.EIGHT_CONNECTED);

		ij.ui().show(ror);
		ij.ui().show(boundaryCombine);
	}

	private static void boundaryPolygon(final ImageJ ij) {
		final RasterizedRegion<Polygon2D, BoolType> p = Regions.rasterize(
			PolygonTests.createSelfIntersectingPolygon());

		final Boundary<BoolType> b4 = new Boundary<>(p,
			Boundary.StructuringElement.FOUR_CONNECTED);
		final Boundary<BoolType> b8 = new Boundary<>(p,
			Boundary.StructuringElement.EIGHT_CONNECTED);

		ij.ui().show(p);
		ij.ui().show(b4);
		ij.ui().show(b8);
	}

	private static void boundaryRectangle(final ImageJ ij) {
		final RasterizedRegion<HyperRectangle, BoolType> r = Regions.rasterize(
			GeomRegions.hyperRectangle(new RealPoint(new double[] { 100, 50 }),
				new double[] { 100, 50 }));

		final Boundary<BoolType> b = new Boundary<>(r,
			Boundary.StructuringElement.FOUR_CONNECTED);

		ij.ui().show(r);
		ij.ui().show(b);

		/*
		 * The boundary does not contain all the corners of the rectangle. This is
		 * due to the contains method and also the RandomAccessibleRegionCursor.
		 * 
		 * Could we create a version of Contains which includes the boundary?
		 * 
		 * public interface BoundaryInclusion {
		 * 	 boolean containsInclusive(final RealLocalizable l);
		 * 	 boolean containsExclusive(final RealLocalizable l);
		 * }
		 * 
		 * public class HyperRectangle implements BoundaryInclusion {
		 * 
		 * 	 private boolean inclusive;
		 * 
		 * 	 public boolean contains(final RealLocalizable l) {
		 * 	 	 if(inclusive) return containsInclusive(l);
		 * 	 	 return containsExclusive(l);
		 * 	 }
		 * 
		 * 	 public boolean containsInclusive(final RealLocalizable l){ ... }
		 * 
		 * 	 public boolean containsExclusive(final RealLocalizable l){ ... }
		 * 
		 * }
		 * 
		 * Where inclusive means including the boundaries, and exclusive means
		 * excluding boundary points.
		 * 
		 * Then the boundary would be correct as is, provided that the shape had
		 * inclusive = true. Or we could try and have Boundary check if the base
		 * shape implements BoundaryInclusion and then have it get a RandomAccess
		 * based on containsInclusive. This would be more difficult though ...
		 */
	}

	/*
	 * Calling Boundary on a PointCollection returns a Boundary with the same
	 * points as those in the PointCollection, and calling Boundary on Line
	 * returns the points on the line. Is this the desired effect? Or should
	 * boundary not work on 1D or 0D shapes?
	 */
	private static void boundaryPointCollection(final ImageJ ij) {
		final Boundary<BoolType> bPts = new Boundary<>(OtherGeomTests
				.createPointCollection());

			ij.ui().show(OtherGeomTests.createPointCollection());
			ij.ui().show(bPts);
	}

	private static void boundaryLine(final ImageJ ij) {
		final Line line = OtherGeomTests.createLine();
		final RasterizedRegion<Line, BoolType> rline = Regions.rasterize(line);

		final Boundary<BoolType> bLine = new Boundary<>(rline,
			Boundary.StructuringElement.FOUR_CONNECTED);

		ij.ui().show(rline);
		ij.ui().show(bLine);
	}
}
