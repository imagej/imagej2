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
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Utility methods for transforming {@link Dataset}s and {@link Img}s between
 * types.
 * <p>
 * Note that ImgLib does not do any range clamping while moving between image
 * types. So translations may not always be nice (i.e. narrowing cases) but best
 * behavior may be undefined.
 * </p>
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public final class TypeChanger {

	private TypeChanger() {
		// prevent instantiation of utility class
	}

	/**
	 * Changes the given {@link Dataset}'s underlying {@link Img} data to the
	 * specified type.
	 */
	public static <T extends RealType<T> & NativeType<T>> void changeType(
		final Dataset dataset, final T newType)
	{
		final Img<? extends RealType<?>> inputImg = dataset.getImage();
		dataset.setImage(copyToType(inputImg, newType));
	}

	/**
	 * Creates a planar ImgLib {@link Img} of the given type. It populates the
	 * output {@link Img}'s data from the input {@link Img} (which is likely of a
	 * different data type). No range clamping of data is done.
	 */
	public static <T extends RealType<T> & NativeType<T>> Img<T> copyToType(
		final Img<? extends RealType<?>> inputImg, final T newType)
	{
		return copyToType(inputImg, newType, new PlanarImgFactory<T>());
	}

	/**
	 * Creates an ImgLib {@link Img} of the given type using the specified
	 * {@link ImgFactory}. It populates the output {@link Img}'s data from the
	 * input {@link Img} (which is likely of a different data type). No range
	 * clamping of data is done.
	 */
	public static <T extends RealType<T> & NativeType<T>> Img<T> copyToType(
		final Img<? extends RealType<?>> inputImg, final T newType,
		final ImgFactory<T> imgFactory)
	{
		final long[] dims = new long[inputImg.numDimensions()];
		inputImg.dimensions(dims);
		final Img<T> outputImg = imgFactory.create(dims, newType);

		final Cursor<? extends RealType<?>> in = inputImg.localizingCursor();
		final RandomAccess<T> out = outputImg.randomAccess();

		final long[] pos = new long[dims.length];
		while (in.hasNext()) {
			in.fwd();
			in.localize(pos);
			out.setPosition(pos);
			final double value = in.get().getRealDouble();
			out.get().setReal(value);
		}

		return outputImg;
	}

}
