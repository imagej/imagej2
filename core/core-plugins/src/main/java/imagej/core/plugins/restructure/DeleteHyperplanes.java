package imagej.core.plugins.restructure;

//
//DeleteHyperplanes.java
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

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.operation.MultiImageIterator;
import net.imglib2.ops.operation.RegionIterator;
import net.imglib2.type.numeric.RealType;
import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;

/**
* Deletes hyperplanes of data from an input Dataset along a user specified axis
* 
* @author Barry DeZonia
*/
@Plugin(menu = {
@Menu(label = "Image", mnemonic = 'i'),
@Menu(label = "Stacks", mnemonic = 's'),
@Menu(label = "Delete Data") })
public class DeleteHyperplanes implements ImageJPlugin {

	private static final String
	  X="X", Y="Y", C="Channel", Z="Z", T="Time", F="Frequency", S="Spectra",
	  	P="Phase", L="Lifetime";
	
	@Parameter(required=true)
	private Dataset input;
	
	// TODO - populate choices from Dataset somehow
	@Parameter(label="Axis to modify",choices = {X,Y,Z,C,T,F,S,P,L})
	private String axisToModify;
	
	// TODO - populate max from Dataset somehow
	@Parameter(label="Deletion position",min="0")
	private long deletePosition;
	
	// TODO - populate max from Dataset somehow
	@Parameter(label="Deletion quantity",min="1")
	private long numDeleting;

	/** creates new ImgPlus data copying pixel values as needed from an input
	 * Dataset. Assigns the ImgPlus to the input Dataset.
	 */
	@Override
	public void run() {
		Axis axis = getAxis();
		if (inputBad(axis)) return;
		Axis[] axes = input.getAxes();
		long[] newDimensions = getDimensions(axis);
		ImgPlus<? extends RealType<?>> dstImgPlus =
			createNewImgPlus(newDimensions, axes);
		fillNewImgPlus(input.getImgPlus(), dstImgPlus, axis);
		// TODO - colorTables, metadata, etc.?
		input.setImgPlus(dstImgPlus);
	}

	/** maps the user specified choice in the dailog to an Axis value.
	 * returns null if some unknown axis specified */
	private Axis getAxis() {
		Axis axis = null;
			
		if (axisToModify.equals(C)) axis = Axes.CHANNEL;
		else if (axisToModify.equals(F)) axis = Axes.FREQUENCY;
		else if (axisToModify.equals(L)) axis = Axes.LIFETIME;
		else if (axisToModify.equals(P)) axis = Axes.PHASE;
		else if (axisToModify.equals(S)) axis = Axes.SPECTRA;
		else if (axisToModify.equals(T)) axis = Axes.TIME;
		else if (axisToModify.equals(X)) axis = Axes.X;
		else if (axisToModify.equals(Y)) axis = Axes.Y;
		else if (axisToModify.equals(Z)) axis = Axes.Z;
	
		// NB : axis could still be null here : Axes.UNKNOWN
		
		return axis;
	}

	/** detects if user specified data is invalid */
	private boolean inputBad(Axis axis) {
		// axis not determined by dialog
		if (axis == null)
			return true;
		
		// setup some working variables
		int axisIndex = input.getAxisIndex(axis);
		long axisSize = input.getImgPlus().dimension(axisIndex);

	  // axis not present in Dataset
		if (axisIndex < 0)
			return true;
		
		// bad value for startPosition
		if ((deletePosition < 0)  || (deletePosition >= axisSize))
			return true;
		
		// bad value for numDeleting
		if (numDeleting <= 0)
			return true;
		
		// trying to delete all hyperplanes along axis
		if ((deletePosition+numDeleting) >= axisSize)
			return true;
		
		// if here everything is okay
		return false;
	}

	/** gets the dimensions of the output data */
	private long[] getDimensions(Axis oneToModify) {
		long[] dimensions = input.getDims();
		int axisIndex = input.getAxisIndex(oneToModify);
		dimensions[axisIndex] -= numDeleting;
		return dimensions;
	}

	/** creates a new ImgPlus with specified dimensions and axes. Uses same
	 * factory as input Dataset. Maintains type, name, and calibration values.
	 * All data values are initialized to 0. 
	 */
	private ImgPlus<? extends RealType<?>>
		createNewImgPlus(long[] dimensions, Axis[] axes)
	{
		ImgFactory factory = input.getImgPlus().getImg().factory();
		Img<? extends RealType<?>> img = (Img<? extends RealType<?>>)
			factory.create(dimensions, input.getType());
		String name = input.getName();
		double[] calibration = new double[axes.length];
		for (int i = 0; i < axes.length; i++) {
			int index = input.getAxisIndex(axes[i]);
			calibration[i] = input.getImgPlus().calibration(index);
		}
		return new ImgPlus(img, name, axes, calibration); 
	}

	/** fills the newly created ImgPlus with data values from a larger source
	 * image. Copies data from those hyperplanes not being cut.
	 */
	private void fillNewImgPlus(ImgPlus<? extends RealType<?>> srcImgPlus,
		ImgPlus<? extends RealType<?>> dstImgPlus, Axis modifiedAxis)
	{
		long[] dimensions = input.getDims();
		int axisIndex = input.getAxisIndex(modifiedAxis);
		long axisSize = dimensions[axisIndex];
		long numBeforeCut = deletePosition;
		long numInCut = numDeleting;
		if (numBeforeCut + numInCut > axisSize)
			numInCut = axisSize - numBeforeCut;
		long numAfterCut = axisSize -	(numBeforeCut + numInCut);
		
		copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			0, 0, numBeforeCut);
		copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			numBeforeCut+numInCut, numBeforeCut, numAfterCut);
	}

	/** copies a region of data from a srcImgPlus to a dstImgPlus */
	private void copyData(ImgPlus<? extends RealType<?>> srcImgPlus,
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
	private long[] calcSpan(ImgPlus imgPlus, Axis axis, long numElements) {
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
	private long[] calcOrigin(ImgPlus imgPlus, Axis axis, long startPos) {
		long[] origin = new long[imgPlus.numDimensions()];
		int axisIndex = imgPlus.getAxisIndex(axis);
		origin[axisIndex] = startPos;
		return origin;
	}
}
