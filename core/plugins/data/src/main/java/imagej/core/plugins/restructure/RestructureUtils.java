//
// RestructureUtils.java
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

package imagej.core.plugins.restructure;

import imagej.data.Dataset;
import imagej.data.Extents;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.RegionIndexIterator;
import net.imglib2.type.numeric.RealType;

/**
 * Utility class used by the restructure plugins
 * 
 * @author Barry DeZonia
 */
public class RestructureUtils {

	private RestructureUtils() {
		// utility class : uninstantiable
	}

	/**
	 * Gets the dimensions of the output data
	 */
	public static long[] getDimensions(final Dataset ds, final Axis oneToModify,
		final long delta)
	{
		final long[] dimensions = ds.getDims();
		final int axisIndex = ds.getAxisIndex(oneToModify);
		dimensions[axisIndex] += delta;
		return dimensions;
	}

	/**
	 * Creates a new ImgPlus with specified dimensions and axes. Uses same factory
	 * as input Dataset. Maintains type, name, and calibration values. All data
	 * values are initialized to 0.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ImgPlus<? extends RealType<?>> createNewImgPlus(
		final Dataset ds, final long[] dimensions, final Axis[] axes)
	{
		final ImgFactory factory = ds.getImgPlus().getImg().factory();
		final Img<? extends RealType<?>> img =
			factory.create(dimensions, ds.getType());
		final String name = ds.getName();
		final double[] calibration = new double[axes.length];
		for (int i = 0; i < axes.length; i++) {
			final int index = ds.getAxisIndex(axes[i]);
			if (index >= 0) calibration[i] = ds.getImgPlus().calibration(index);
			else calibration[i] = Double.NaN;
		}
		return new ImgPlus(img, name, axes, calibration);
	}

	/**
	 * Copies a region of data from a srcImgPlus to a dstImgPlus. region is
	 * defined by a number of planes along an axis that is present in both input
	 * ImgPluses
	 */
	public static void copyData(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus, final Axis axis,
		final long srcStartPos, final long dstStartPos, final long numHyperplanes)
	{
		if (numHyperplanes == 0) return;
		final long[] srcOrigin = calcOrigin(srcImgPlus, axis, srcStartPos);
		final long[] dstOrigin = calcOrigin(dstImgPlus, axis, dstStartPos);

		final long[] srcSpan = calcSpan(srcImgPlus, axis, numHyperplanes);
		final long[] dstSpan = calcSpan(dstImgPlus, axis, numHyperplanes);

		copyHyperVolume(srcImgPlus, srcOrigin, srcSpan, dstImgPlus, dstOrigin,
			dstSpan);
	}

	/**
	 * Copies a hypervolume from a source {@link ImgPlus} to a destination
	 * {@link ImgPlus}. Spans may have different number of dimensions but must be
	 * shape compatible with axes in same relative order.
	 */
	public static void copyHyperVolume(
		final ImgPlus<? extends RealType<?>> srcImgPlus, final long[] srcOrigin,
		final long[] srcSpan, final ImgPlus<? extends RealType<?>> dstImgPlus,
		final long[] dstOrigin, final long[] dstSpan)
	{
		checkSpanShapes(srcSpan, dstSpan);
		final RandomAccess<? extends RealType<?>> srcAccessor =
			srcImgPlus.getImg().randomAccess();
		final RandomAccess<? extends RealType<?>> dstAccessor =
			dstImgPlus.getImg().randomAccess();
		final long[] srcOffsets = new long[srcOrigin.length];
		for (int i = 0; i < srcOffsets.length; i++)
			srcOffsets[i] = srcSpan[i] - 1;
		final long[] dstOffsets = new long[dstOrigin.length];
		for (int i = 0; i < dstOffsets.length; i++)
			dstOffsets[i] = dstSpan[i] - 1;
		final RegionIndexIterator iterS =
			new RegionIndexIterator(srcOrigin, new long[srcOrigin.length], srcOffsets);
		final RegionIndexIterator iterD =
			new RegionIndexIterator(dstOrigin, new long[dstOrigin.length], dstOffsets);
		while (iterS.hasNext() && iterD.hasNext()) {
			iterS.fwd();
			iterD.fwd();
			srcAccessor.setPosition(iterS.getPosition());
			dstAccessor.setPosition(iterD.getPosition());
			final double value = srcAccessor.get().getRealDouble();
			dstAccessor.get().setReal(value);
		}
	}

	/**
	 * Returns a span array covering the specified hyperplanes. Only the axis
	 * along which the cut is being made has nonmaximal dimension. That dimension
	 * is set to the passed in number of elements to be preserved.
	 */
	private static long[] calcSpan(final ImgPlus<?> imgPlus, final Axis axis,
		final long numElements)
	{
		final long[] span = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(span);
		final int axisIndex = imgPlus.getAxisIndex(axis);
		span[axisIndex] = numElements;
		return span;
	}

	/**
	 * Returns an origin array locating the first hyperplane to keep. Only the
	 * axis along which the cut is being made has nonzero dimension. That
	 * dimension is set to the passed in start position of the hyperplane along
	 * the axis.
	 */
	private static long[] calcOrigin(final ImgPlus<?> imgPlus, final Axis axis,
		final long startPos)
	{
		final long[] origin = new long[imgPlus.numDimensions()];
		final int axisIndex = imgPlus.getAxisIndex(axis);
		origin[axisIndex] = startPos;
		return origin;
	}

	private static void
		checkSpanShapes(final long[] srcSpan, final long[] dstSpan)
	{
		final Extents srcExtents = new Extents(srcSpan);
		final Extents dstExtents = new Extents(dstSpan);
		if (srcExtents.numElements() != dstExtents.numElements()) throw new IllegalArgumentException(
			"hypervolume regions not shape compatible");
		// TODO
		// we could do a lot more checking but won't for now
		// checks would be that all axes are the same ones and any missing ones
		// in span I have size==1 in other span J. and test the axes are in the
		// same relative order.
	}
}
