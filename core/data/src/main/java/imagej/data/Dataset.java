//
// Dataset.java
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

import imagej.data.event.DatasetChangedEvent;
import imagej.data.event.DatasetCreatedEvent;
import imagej.data.event.DatasetDeletedEvent;
import imagej.event.Events;
import imagej.util.Log;
import imagej.util.Rect;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.Metadata;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
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
 * Dataset is the primary image data structure in ImageJ. A Dataset wraps an
 * ImgLib {@link ImgPlus}. It also provides a number of convenience methods,
 * such as the ability to access pixels on a plane-by-plane basis, and create
 * new Datasets of various types easily.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class Dataset implements Comparable<Dataset>, Metadata {

	private ImgPlus<? extends RealType<?>> imgPlus;
	private boolean isRgbMerged;

	// FIXME TEMP - the current selection for this Dataset. Temporarily located
	// here for plugin testing purposes. Really should be viewcentric.
	private Rect selection;

	public void setSelection(final int minX, final int minY, final int maxX,
		final int maxY)
	{
		selection.x = minX;
		selection.y = minY;
		selection.width = maxX - minX + 1;
		selection.height = maxY - minY + 1;
	}

	public Rect getSelection() {
		return selection;
	}

	// END FIXME TEMP

	public Dataset(final ImgPlus<? extends RealType<?>> imgPlus) {
		this.imgPlus = imgPlus;
		this.isRgbMerged = false;
		this.selection = new Rect();
		Events.publish(new DatasetCreatedEvent(this));
	}

	/**
	 * For use in legacy layer only, this flag allows the various legacy layer
	 * image translators to support color images correctly.
	 */
	public void setIsRgbMerged(final boolean value) {
		this.isRgbMerged = value;
	}

	/**
	 * For use in legacy layer only, this flag allows the various legacy layer
	 * image translators to support color images correctly.
	 */
	public boolean isRgbMerged() {
		return isRgbMerged;
	}

	public ImgPlus<? extends RealType<?>> getImgPlus() {
		return imgPlus;
	}

	public void setImgPlus(final ImgPlus<? extends RealType<?>> imgPlus) {
		if (this.imgPlus.numDimensions() != imgPlus.numDimensions()) {
			throw new IllegalArgumentException("Invalid dimensionality: expected " +
				this.imgPlus.numDimensions() + " but was " + imgPlus.numDimensions());
		}
		this.imgPlus = imgPlus;
		// NB - keeping all the old metadata for now. TODO - revisit this?
		// NB - keeping isRgbMerged status for now. TODO - revisit this?
		this.selection = new Rect();

		update();
	}

	/** Gets the dimensional extents of the dataset. */
	public long[] getDims() {
		final long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		return dims;
	}

	public Object getPlane(final int no) {
		if (!(imgPlus instanceof PlanarAccess)) return null;
		// TODO - extract a copy the plane if it cannot be obtained by reference
		final PlanarAccess<?> planarAccess = (PlanarAccess<?>) imgPlus;
		final Object plane = planarAccess.getPlane(no);
		if (!(plane instanceof ArrayDataAccess)) return null;
		return ((ArrayDataAccess<?>) plane).getCurrentStorageArray();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setPlane(final int no, final Object plane) {
		final Img<? extends RealType<?>> wrappedImg = imgPlus.getImg();
		if (!(wrappedImg instanceof PlanarAccess)) {
			// cannot set by reference
			Log.error("Cannot set plane for non-planar image");
			return;
		}
		// TODO - copy the plane if it cannot be set by reference
		final PlanarAccess planarAccess = (PlanarAccess) wrappedImg;
		ArrayDataAccess<?> array = null;
		if (plane instanceof byte[]) {
			array = new ByteArray((byte[]) plane);
		}
		else if (plane instanceof short[]) {
			array = new ShortArray((short[]) plane);
		}
		else if (plane instanceof int[]) {
			array = new IntArray((int[]) plane);
		}
		else if (plane instanceof float[]) {
			array = new FloatArray((float[]) plane);
		}
		else if (plane instanceof long[]) {
			array = new LongArray((long[]) plane);
		}
		else if (plane instanceof double[]) {
			array = new DoubleArray((double[]) plane);
		}
		planarAccess.setPlane(no, array);
	}

	public double getDoubleValue(final long[] pos) {
		// NB: This method is slow... will change anyway with ImgLib2.
		final RandomAccess<? extends RealType<?>> cursor = imgPlus.randomAccess();
		cursor.setPosition(pos);
		final double value = cursor.get().getRealDouble();
		return value;
	}

	public RealType<?> getType() {
		return imgPlus.firstElement();
	}

	public boolean isSigned() {
		return getType().getMinValue() < 0;
	}

	public boolean isInteger() {
		return getType() instanceof IntegerType;
	}

	/** Gets a string description of the dataset's pixel type. */
	public String getTypeLabel() {
		if (isRgbMerged()) return "RGB";
		final int bitsPerPixel = getType().getBitsPerPixel();
		final String category = isInteger() ?
			isSigned() ? "signed" : "unsigned" : "real";
		return bitsPerPixel + "-bit (" + category + ")";
	}

	/** Creates a copy of the dataset. */
	public Dataset duplicate() {
		final Dataset d = duplicateBlank();
		copyInto(d);
		return d;
	}

	/** Creates a copy of the dataset, but without copying any pixel values. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Dataset duplicateBlank() {
		final ImgPlus untypedImg = imgPlus;
		return new Dataset(createBlankCopy(untypedImg));
	}

	/** Copies the dataset's pixels into the given target dataset. */
	public void copyInto(final Dataset target) {
		final Cursor<? extends RealType<?>> in = imgPlus.localizingCursor();
		final RandomAccess<? extends RealType<?>> out =
			target.getImgPlus().randomAccess();
		final long[] position = new long[imgPlus.numDimensions()];

		while (in.hasNext()) {
			in.next();
			final double value = in.get().getRealDouble();
			in.localize(position);
			out.setPosition(position);
			out.get().setReal(value);
		}
	}

	/** Informs interested parties that the dataset has changed somehow. */
	public void update() {		
		Events.publish(new DatasetChangedEvent(this));
	}

	/**
	 * Deletes the given dataset, cleaning up resources and removing it from the
	 * object manager.
	 */
	public void delete() {
		Events.publish(new DatasetDeletedEvent(this));
	}

	@Override
	public String toString() {
		return imgPlus.getName();
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final Dataset dataset) {
		return imgPlus.getName().compareTo(dataset.imgPlus.getName());
	}

	// -- Metadata methods --

	@Override
	public String getName() {
		return imgPlus.getName();
	}

	@Override
	public Axis[] getAxes() {
		return imgPlus.getAxes();
	}

	@Override
	public int getAxisIndex(final Axis axis) {
		return imgPlus.getAxisIndex(axis);
	}

	@Override
	public float[] getCalibration() {
		return imgPlus.getCalibration();
	}

	// -- Utility methods --

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
		final Axis[] axes, final int bitsPerPixel, final boolean signed,
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
		final T type, final long[] dims, final String name, final Axis[] axes)
	{
		final PlanarImgFactory<T> imgFactory = new PlanarImgFactory<T>();
		final PlanarImg<T, ?> planarImg = imgFactory.create(dims, type);
		final ImgPlus<T> imgPlus = new ImgPlus<T>(planarImg, name, axes, null);
		return new Dataset(imgPlus);
	}

	// -- Helper methods --

	private static void invalidParams(final int bitsPerPixel,
		final boolean signed, final boolean floating)
	{
		throw new IllegalArgumentException("Invalid parameters: bitsPerPixel=" +
			bitsPerPixel + ", signed=" + signed + ", floating=" + floating);
	}

	/** Makes an image that has same type, container, and dimensions as refImage. */
	private static <T extends RealType<T>> ImgPlus<T> createBlankCopy(
		final ImgPlus<T> img)
	{
		final long[] dimensions = new long[img.numDimensions()];
		img.dimensions(dimensions);
		final Img<T> blankImg = img.factory().create(dimensions,
			img.firstElement());
		return new ImgPlus<T>(blankImg, img);
	}

}
