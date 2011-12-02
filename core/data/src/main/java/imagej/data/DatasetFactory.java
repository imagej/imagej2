//
// DatasetFactory.java
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

package imagej.data;

import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.Unsigned12BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * A utility class with {@link Dataset}-related methods.
 * 
 * @author Curtis Rueden
 */
public final class DatasetFactory {
	
	// TODO - Decide whether to convert this from a utility class into a truly
	// extensible factory implementation.

	private DatasetFactory() {
		// prevent instantiation of utility class
	}

	/**
	 * Creates a new dataset.
	 * 
	 * @param dims The dataset's dimensional extents.
	 * @param name The dataset's name.
	 * @param axes The dataset's dimensional axis labels.
	 * @param bitsPerPixel The dataset's bit depth. Currently supported bit depths
	 *          include 1, 8, 12, 16, 32 and 64.
	 * @param signed Whether the dataset's pixels can have negative values.
	 * @param floating Whether the dataset's pixels can have non-integer values.
	 * @return The newly created dataset.
	 * @throws IllegalArgumentException If the combination of bitsPerPixel, signed
	 *           and floating parameters do not form a valid data type.
	 */
	public static Dataset create(final long[] dims, final String name,
		final AxisType[] axes, final int bitsPerPixel, final boolean signed,
		final boolean floating)
	{
		if (bitsPerPixel == 1) {
			if (signed || floating) invalidParams(bitsPerPixel, signed, floating);
			return create(new BitType(), dims, name, axes);
		}
		if (bitsPerPixel == 8) {
			if (floating) invalidParams(bitsPerPixel, signed, floating);
			if (signed) return create(new ByteType(), dims, name, axes);
			return create(new UnsignedByteType(), dims, name, axes);
		}
		if (bitsPerPixel == 12) {
			if (signed || floating) invalidParams(bitsPerPixel, signed, floating);
			return create(new Unsigned12BitType(), dims, name, axes);
		}
		if (bitsPerPixel == 16) {
			if (floating) invalidParams(bitsPerPixel, signed, floating);
			if (signed) return create(new ShortType(), dims, name, axes);
			return create(new UnsignedShortType(), dims, name, axes);
		}
		if (bitsPerPixel == 32) {
			if (floating) {
				if (!signed) invalidParams(bitsPerPixel, signed, floating);
				return create(new FloatType(), dims, name, axes);
			}
			if (signed) return create(new IntType(), dims, name, axes);
			return create(new UnsignedIntType(), dims, name, axes);
		}
		if (bitsPerPixel == 64) {
			if (!signed) invalidParams(bitsPerPixel, signed, floating);
			if (floating) return create(new DoubleType(), dims, name, axes);
			return create(new LongType(), dims, name, axes);
		}
		invalidParams(bitsPerPixel, signed, floating);
		return null;
	}

	/**
	 * Creates a new dataset.
	 * 
	 * @param <T> The type of the dataset.
	 * @param type The type of the dataset.
	 * @param dims The dataset's dimensional extents.
	 * @param name The dataset's name.
	 * @param axes The dataset's dimensional axis labels.
	 * @return The newly created dataset.
	 */
	public static <T extends RealType<T> & NativeType<T>> Dataset create(
		final T type, final long[] dims, final String name, final AxisType[] axes)
	{
		final PlanarImgFactory<T> imgFactory = new PlanarImgFactory<T>();
		return create(imgFactory, type, dims, name, axes);
	}

	/**
	 * Creates a new dataset using provided ImgFactory
	 * 
	 * @param <T> The type of the dataset.
	 * @param factory The ImgFactory to use to create the data.
	 * @param type The type of the dataset.
	 * @param dims The dataset's dimensional extents.
	 * @param name The dataset's name.
	 * @param axes The dataset's dimensional axis labels.
	 * @return The newly created dataset.
	 */
	public static <T extends RealType<T> & NativeType<T>> Dataset create(
		final ImgFactory<T> factory, final T type, final long[] dims,
		final String name, final AxisType[] axes)
	{
		final Img<T> img = factory.create(dims, type);
		final ImgPlus<T> imgPlus = new ImgPlus<T>(img, name, axes, null);
		return new DefaultDataset(imgPlus);
	}

	// -- Helper methods --
	
	private static void invalidParams(final int bitsPerPixel,
		final boolean signed, final boolean floating)
	{
		throw new IllegalArgumentException("Invalid parameters: bitsPerPixel=" +
			bitsPerPixel + ", signed=" + signed + ", floating=" + floating);
	}

}
