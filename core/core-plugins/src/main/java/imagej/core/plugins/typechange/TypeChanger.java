//
// TypeChanger.java
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

package imagej.core.plugins.typechange;

import imagej.data.Dataset;
import imagej.plugin.Parameter;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Transforms a {@link Dataset} (and linked {@link ImgPlus}) between types.
 * <p>
 * Although Imglib does not do any range clamping of values we will do so here.
 * </p>
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class TypeChanger {

	@Parameter(required = true)
	protected Dataset input;

	protected <T extends RealType<T> & NativeType<T>> void changeType(
		final T newType)
	{
		changeType(input, newType);
	}

	/**
	 * Changes the given {@link Dataset}'s underlying {@link Img} data to the
	 * specified type.
	 */
	public static <T extends RealType<T> & NativeType<T>> void changeType(
		final Dataset dataset, final T newType)
	{
		final ImgPlus<? extends RealType<?>> inputImg = dataset.getImgPlus();
		final Class<?> currTypeClass = dataset.getType().getClass();
		final Class<?> newTypeClass = newType.getClass();
		if ((currTypeClass != newTypeClass) || (dataset.isRGBMerged())) {
			boolean wasRGBMerged = dataset.isRGBMerged();
			dataset.setImgPlus(copyToType(inputImg,newType));
			if (wasRGBMerged) {
				dataset.setRGBMerged(false);
				dataset.setCompositeChannelCount(1);
			}
		}
	}

	/**
	 * Creates a planar ImgLib {@link Img} of the given type. It populates the
	 * output {@link Img}'s data from the input {@link Img} (which is likely of a
	 * different data type). Output data is range clamped.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends RealType<T> & NativeType<T>>
	ImgPlus<? extends RealType<?>> copyToType(
		final ImgPlus<? extends RealType<?>> inputImg, final T newType)
	{
		ImgFactory<? extends RealType<?>> factory = inputImg.factory();
		return copyToType(inputImg, newType, (ImgFactory<T>)factory);
	}

	/**
	 * Creates an ImgLib {@link Img} of the given type using the specified
	 * {@link ImgFactory}. It populates the output {@link Img}'s data from the
	 * input {@link Img} (which is likely of a different data type). Output
	 * data is range clamped.
	 */
	public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> copyToType(
		final ImgPlus<? extends RealType<?>> inputImg, final T newType,
		final ImgFactory<T> imgFactory)
	{
		final long[] dims = new long[inputImg.numDimensions()];
		inputImg.dimensions(dims);
		final Img<T> outputImg = imgFactory.create(dims, newType);

		final Cursor<? extends RealType<?>> in = inputImg.localizingCursor();
		final RandomAccess<T> out = outputImg.randomAccess();

		final double outTypeMin = out.get().getMinValue();
		final double outTypeMax = out.get().getMaxValue();
		
		final boolean inputIs1bit = in.get().getBitsPerPixel() == 1;
		
		final long[] pos = new long[dims.length];
		while (in.hasNext()) {
			in.fwd();
			in.localize(pos);
			out.setPosition(pos);
			double value = in.get().getRealDouble();
			if (value < outTypeMin) value = outTypeMin;
			if (value > outTypeMax) value = outTypeMax;
			if ((inputIs1bit) && (value > 0))
				value = outTypeMax;
			out.get().setReal(value);
		}

		return new ImgPlus<T>(outputImg, inputImg);
	}

}
