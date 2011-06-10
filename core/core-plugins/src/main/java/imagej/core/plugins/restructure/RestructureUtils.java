package imagej.core.plugins.restructure;

import imagej.data.Dataset;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.operation.MultiImageIterator;
import net.imglib2.ops.operation.RegionIterator;
import net.imglib2.type.numeric.RealType;


public class RestructureUtils {
	
	public static final String
  	X="X", Y="Y", C="Channel", Z="Z", T="Time", F="Frequency", S="Spectra",
  		P="Phase", L="Lifetime";
	
	/** maps an axis name String into an Axis value.
	 * returns null if some unknown axis specified */
	public static Axis getAxis(String axisName) {
		Axis axis = null;
			
		if (axisName.equals(C)) axis = Axes.CHANNEL;
		else if (axisName.equals(F)) axis = Axes.FREQUENCY;
		else if (axisName.equals(L)) axis = Axes.LIFETIME;
		else if (axisName.equals(P)) axis = Axes.PHASE;
		else if (axisName.equals(S)) axis = Axes.SPECTRA;
		else if (axisName.equals(T)) axis = Axes.TIME;
		else if (axisName.equals(X)) axis = Axes.X;
		else if (axisName.equals(Y)) axis = Axes.Y;
		else if (axisName.equals(Z)) axis = Axes.Z;
	
		// NB : axis could still be null here : Axes.UNKNOWN
		
		return axis;
	}
	
	/** gets the dimensions of the output data */
	public static long[] getDimensions(Dataset ds, Axis oneToModify, long delta) {
		long[] dimensions = ds.getDims();
		int axisIndex = ds.getAxisIndex(oneToModify);
		dimensions[axisIndex] += delta;
		return dimensions;
	}

	/** creates a new ImgPlus with specified dimensions and axes. Uses same
	 * factory as input Dataset. Maintains type, name, and calibration values.
	 * All data values are initialized to 0. 
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public static ImgPlus<? extends RealType<?>>
		createNewImgPlus(Dataset ds, long[] dimensions, Axis[] axes)
	{
		ImgFactory factory = ds.getImgPlus().getImg().factory();
		Img<? extends RealType<?>> img =
			factory.create(dimensions, ds.getType());
		String name = ds.getName();
		double[] calibration = new double[axes.length];
		for (int i = 0; i < axes.length; i++) {
			int index = ds.getAxisIndex(axes[i]);
			calibration[i] = ds.getImgPlus().calibration(index);
		}
		return new ImgPlus(img, name, axes, calibration); 
	}

	/** copies a region of data from a srcImgPlus to a dstImgPlus */
	@SuppressWarnings({"rawtypes","unchecked"})
	public static void copyData(ImgPlus<? extends RealType<?>> srcImgPlus,
		ImgPlus<? extends RealType<?>> dstImgPlus, Axis axis,
		long srcStartPos, long dstStartPos, long numHyperplanes)
	{
		if (numHyperplanes == 0) return;
		Img[] images = new Img[]{srcImgPlus.getImg(), dstImgPlus.getImg()};
		MultiImageIterator<? extends RealType<?>> iter =
			new MultiImageIterator(images);
		long[] origin0 = calcOrigin(srcImgPlus, axis, srcStartPos);
		long[] origin1 = calcOrigin(dstImgPlus, axis, dstStartPos);
		long[] span = calcSpan(dstImgPlus, axis, numHyperplanes);
		iter.setRegion(0, origin0, span);
		iter.setRegion(1, origin1, span);
		iter.initialize();
		RegionIterator<? extends RealType<?>>[] subIters = iter.getIterators();
		while (iter.hasNext()) {
			iter.next();
			double value = subIters[0].getValue().getRealDouble();
			subIters[1].getValue().setReal(value);
		}
	}

	/** returns a span array covering the specified hyperplanes. Only the axis
	 * along which the cut is being made has nonmaximal dimension. That
	 * dimension is set to the passed in number of elements to be preserved.
	 */
	private static long[] calcSpan(ImgPlus<?> imgPlus, Axis axis, long numElements) {
		long[] span = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(span);
		int axisIndex = imgPlus.getAxisIndex(axis);
		span[axisIndex] = numElements;
		return span;
	}

	/** returns an origin array locating the first hyperplane to keep. Only the
	 * axis along which the cut is being made has nonzero dimension. That
	 * dimension is set to the passed in start position of the hyperplane along
	 * the axis.
	 */
	private static long[] calcOrigin(ImgPlus<?> imgPlus, Axis axis, long startPos) {
		long[] origin = new long[imgPlus.numDimensions()];
		int axisIndex = imgPlus.getAxisIndex(axis);
		origin[axisIndex] = startPos;
		return origin;
	}

}
