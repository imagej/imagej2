
package net.imagej;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRealRandomAccessible;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geometric.GeomRegions;
import net.imglib2.roi.geometric.HyperEllipsoid;
import net.imglib2.roi.geometric.HyperRectangle;
import net.imglib2.roi.geometric.Line;
import net.imglib2.roi.geometric.Polygon2D;
import net.imglib2.roi.geometric.RasterizedRegion;
import net.imglib2.roi.operators.RealBinaryOperator;
import net.imglib2.roi.operators.RealUnaryNot;
import net.imglib2.roi.operators.RealUnaryOperator;
import net.imglib2.type.logic.BoolType;

public class CombinedRegionExamples {

	private CombinedRegionExamples() {
		// prevent instantiation of utility class
	}

	public static ImageJ launch(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		return ij;
	}

	public static void main(final String... args) {
		final ImageJ ij = launch(args);

		not(ij);
		xor(ij);
		and(ij);
		or(ij);
		subtract(ij);
		combineFour(ij);
		combineLine(ij);
	}

	private static void not(final ImageJ ij) {
		// Create circle
		final HyperEllipsoid circle = GeomRegions.hyperEllipsoid(new RealPoint(
			new double[] { 20, 20 }), new double[] { 20, 20 });

		// Not
		final RealUnaryOperator<BoolType> not = Regions.not(circle);

		// Can retrieve the input
		final HyperEllipsoid circle2 = (HyperEllipsoid) not.getSource();

		// Rasterize and display
		final RasterizedRegion<RealUnaryOperator<BoolType>, BoolType> rnot =
			Regions.rasterize(not);
		ij.ui().show(rnot);
	}

	private static void xor(final ImageJ ij) {
		// Create 3D ellipse and 3D rectangle
		final HyperRectangle hr = GeomRegions.hyperRectangle(new RealPoint(
			new double[] { 100, 150, 50 }), new double[] { 100, 150, 50 });
		final HyperEllipsoid he = GeomRegions.hyperEllipsoid(new RealPoint(
			new double[] { 100, 150, 50 }), new double[] { 80, 100, 48 });

		// XOr ellipse and rectangle
		final RealBinaryOperator<BoolType> xor = Regions.xor(hr, he);

		// Can retrieve both inputs to xor
		final HyperRectangle hr2 = (HyperRectangle) xor.getA();
		final HyperEllipsoid he2 = (HyperEllipsoid) xor.getB();

		// Rasterize and display
		final RasterizedRegion<RealBinaryOperator<BoolType>, BoolType> rxor =
			Regions.rasterize(xor);
		ij.ui().show(rxor);
	}

	private static void and(final ImageJ ij) {
		// Create hexagon (PolygonTests) and square
		final Polygon2D hexagon = PolygonTests.createHexagon();
		final HyperRectangle square = GeomRegions.hyperRectangle(new RealPoint(
			new double[] { 75, 50 }), new double[] { 40, 40 });

		// Intersection (and) of square and hexagon
		final RealBinaryOperator<BoolType> and = Regions.intersect(
			hexagon, square);

		// Rasterize and display
		final RasterizedRegion<RealBinaryOperator<BoolType>, BoolType> rand =
			Regions.rasterize(and);
		ij.ui().show(rand);
	}

	private static void or(final ImageJ ij) {
		// Create rotated ellipse
		final double angle = 45.0 * (Math.PI / 180.0);
		final double[][] matrix = new double[][] { { Math.cos(angle), -Math.sin(
			angle) }, { Math.sin(angle), Math.cos(angle) } };
		final AffineRealRandomAccessible<BoolType, AffineGet> rotatedHE =
			GeomRegions.hyperEllipsoid(new RealPoint(new double[] { 150, 100 }),
				new double[] { 50, 100 }, matrix);
		// Need to add an interval to rotated shapes because the combination
		// methods only work on RealRandomAccessibleRealIntervals
		final RealRandomAccessibleRealInterval<BoolType> rotatedHEInterval = Regions
			.interval(rotatedHE, new FinalRealInterval(new double[] { 0, 0 },
				new double[] { 250, 200 }));

		// Create rectangle
		final HyperRectangle hr = GeomRegions.hyperRectangle(new RealPoint(
			new double[] { 210, 50 }), new double[] { 100, 45 });

		// Union (or) of rectangle and rotated ellipse
		final RealBinaryOperator<BoolType> or = Regions.union(
			rotatedHEInterval, hr);

		// Rasterize and display
		final RasterizedRegion<RealBinaryOperator<BoolType>, BoolType> ror =
			Regions.rasterize(or);
		ij.ui().show(ror);
	}

	private static void subtract(final ImageJ ij) {
		// Create self intersecting polygon and rectangle
		final Polygon2D selfIntersect = PolygonTests
			.createSelfIntersectingPolygon();
		final HyperRectangle hr = GeomRegions.hyperRectangle(new RealPoint(
			new double[] { 50, 60 }), new double[] { 30, 70 });

		// Subtract rectangle from self intersecting polygon
		final RealBinaryOperator<BoolType> subtract = Regions
			.subtract(selfIntersect, hr);

		// Rasterize and display
		final RasterizedRegion<RealBinaryOperator<BoolType>, BoolType> rsubtract =
			Regions.rasterize(subtract);
		ij.ui().show(rsubtract);
	}

	private static void combineFour(final ImageJ ij) {
		// Create rotated ellipse
		final double angle = 45.0 * (Math.PI / 180.0);
		final double[][] matrix = new double[][] { { Math.cos(angle), -Math.sin(
			angle) }, { Math.sin(angle), Math.cos(angle) } };
		final AffineRealRandomAccessible<BoolType, AffineGet> rotatedHE =
			GeomRegions.hyperEllipsoid(new RealPoint(new double[] { 150, 100 }),
				new double[] { 50, 100 }, matrix);
		// Need to add an interval to rotated shapes
		final RealRandomAccessibleRealInterval<BoolType> rotatedHEInterval = Regions
			.interval(rotatedHE, new FinalRealInterval(new double[] { 0, 0 },
				new double[] { 250, 200 }));

		// Create rectangle, square, and hexagon
		final HyperRectangle hr = GeomRegions.hyperRectangle(new RealPoint(
			new double[] { 100, 100 }), new double[] { 10, 50 });
		final HyperRectangle square = GeomRegions.hyperRectangle(new RealPoint(
			new double[] { 70, 70 }), new double[] { 30, 30 });
		final Polygon2D hexagon = PolygonTests.createHexagon();

		// Combine
		// XOr rectangle with rotated ellipse
		final RealBinaryOperator<BoolType> xor = Regions.xor(hr,
			rotatedHEInterval);
		// Subtract square from XOr of rectangle and rotated ellipse
		final RealBinaryOperator<BoolType> subtract = Regions
			.subtract(xor, square);
		// Union (or) of subtraction result with hexagon
		final RealBinaryOperator<BoolType> or = Regions.union(hexagon,
			subtract);
		// Final region is ((rotated ellipse XOR rectangle) - square) OR hexagon

		// Rasterize and display
		final RasterizedRegion<RealBinaryOperator<BoolType>, BoolType> rCombine =
			Regions.rasterize(or);
		ij.ui().show(rCombine);
	}

	private static void combineLine(final ImageJ ij) {
		// Create line and ellipse
		final Line line = OtherGeomTests.createLine();
		final HyperEllipsoid circle = GeomRegions.hyperEllipsoid(new RealPoint(
			new double[] { 30, 30 }), new double[] { 20, 20 });

		// Union (or)
		final RealBinaryOperator<BoolType> combine = Regions.union(
			line, circle);

		// Rasterize and display
		final RasterizedRegion<RealBinaryOperator<BoolType>, BoolType> rCombine =
			Regions.rasterize(combine);
		ij.ui().show(rCombine);
	}

	/*
	 * Regions.xor(), union(), intersect(), subtract(), and not() will not work
	 * with RealPoints and PointCollections. 
	 * 
	 * It doesn't work with PointCollections because PointCollection is integer
	 * space and all the operators are for real space. Should we create integer
	 * space combination methods? i.e.
	 * 
	 * BinaryOperator xor(RandomAccessibleInterval<B> A,
	 * RandomAccessibleInterval<B> C) {
	 * 		return new BinaryXOr(A, C);
	 * }
	 * 
	 * You can't combine RealPoints using these methods because they're not
	 * Intervals. Should we create combine methods which operate on RealPoints,
	 * transform a RealPoint into a zero width/height interval, or just not allow
	 * points to be combined?
	 */

	/*
	 * The below doesn't work because hexagon does not have the same
	 * dimensionality as the other two. In order for this to work, we'd need to
	 * be able to embed the 2D hexagon in 3D.
	 */

/*
 * private static void combineMultiple(final ImageJ ij) {
 * 	 final HyperRectangle hr = GeomRegions.hyperRectangle(new RealPoint(
 * 	 	 new double[] { 100, 150, 50 }), new double[] { 100, 150, 50 });
 * 	 final HyperEllipsoid he = GeomRegions.hyperEllipsoid(new RealPoint(
 * 	 	 new double[] { 100, 150, 50 }), new double[] { 80, 100, 48 });
 * 	 final Polygon2D hexagon = PolygonTests.createHexagon();
 * 
 * 	 final RealRandomAccessibleRealInterval<BoolType> xor = Regions.xor(hr, he);
 * 	 final RealRandomAccessibleRealInterval<BoolType> combineThree = Regions
 * 	 	 .subtract(xor, hexagon);
 * 	 final RasterizedRegion<RealRandomAccessibleRealInterval<BoolType>, BoolType> rCombineThree =
 * 	 Regions.rasterize(combineThree);
 * 	 ij.ui().show(rCombineThree);
 * }
*/
}
