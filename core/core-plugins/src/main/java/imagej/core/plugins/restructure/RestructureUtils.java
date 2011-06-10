package imagej.core.plugins.restructure;

//
//RestructureUtils.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
 * Neither the names of the ImageJDev.org developers nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

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
  	X="X", Y="Y", CH="Channel", Z="Z", TI="Time", FR="Frequency", SP="Spectra",
  		PH="Phase", PO="Polarization", LI="Lifetime";

	public static final String[] AXES = {X,Y,Z,CH,TI,FR,SP,PH,PO,LI};
	
	/** maps an axis name String into an Axis value.
	 * returns null if some unknown axis specified */
	public static Axis getAxis(String axisName) {
		Axis axis = null;
			
		if (axisName.equals(CH)) axis = Axes.CHANNEL;
		else if (axisName.equals(FR)) axis = Axes.FREQUENCY;
		else if (axisName.equals(LI)) axis = Axes.LIFETIME;
		else if (axisName.equals(PH)) axis = Axes.PHASE;
		else if (axisName.equals(PO)) axis = Axes.POLARIZATION;
		else if (axisName.equals(SP)) axis = Axes.SPECTRA;
		else if (axisName.equals(TI)) axis = Axes.TIME;
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
			if (index >= 0)
				calibration[i] = ds.getImgPlus().calibration(index);
			else
				calibration[i] = Double.NaN;
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
