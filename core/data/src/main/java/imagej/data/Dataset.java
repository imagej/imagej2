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
import imagej.util.Rect;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
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
 * TODO
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class Dataset implements Comparable<Dataset> {

	private Img<? extends RealType<?>> img;
	private final Metadata metadata;
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

	public Dataset(final Img<? extends RealType<?>> image) {
		this(image, Metadata.createMetadata(image));
	}

	public Dataset(final Img<? extends RealType<?>> img, final Metadata metadata)
	{
		if (metadata == null) {
			throw new IllegalArgumentException("Metadata must not be null");
		}
		this.img = img;
		this.metadata = metadata;
		this.isRgbMerged = false;
		this.selection = new Rect();
		Events.publish(new DatasetCreatedEvent(this));
	}

	/**
	 * to be used in legacy layer only. allows the various legacy layer image
	 * translators to support color images correctly.
	 */
	public void setIsRgbMerged(final boolean value) {
		this.isRgbMerged = value;
	}

	/**
	 * to be used in legacy layer only. allows the various legacy layer image
	 * translators to support color images correctly.
	 */
	public boolean isRgbMerged() {
		return isRgbMerged;
	}

	public Img<? extends RealType<?>> getImage() {
		return img;
	}

	public void setImage(final Img<? extends RealType<?>> newImageData) {
		if (img.numDimensions() != newImageData.numDimensions()) {
			throw new IllegalArgumentException("Invalid dimensionality: expected " +
				img.numDimensions() + " but was " + newImageData.numDimensions());
		}
		this.img = newImageData;
		// NB - keeping all the old metadata for now. TODO - revisit this?
		// NB - keeping isRgbMerged status for now. TODO - revisit this?
		this.selection = new Rect();

		Events.publish(new DatasetChangedEvent(this));
	}

	public Metadata getMetadata() {
		return metadata;
	}

	/** Gets a string description of the dataset's pixel type. */
	public String getPixelType() {
		// HACK: Since type.toString() isn't nice, we use crazy logic here.
		// TODO: Eliminate this, by improving type.toString() in ImgLib2.
		final Object type = getType();
		String pixelType = type.getClass().getSimpleName();
		pixelType = pixelType.replaceAll("Type", "");
		pixelType = pixelType.replaceAll("Byte", "8-bit");
		pixelType = pixelType.replaceAll("([0-9]+)Bit", "$1-bit"); // e.g., 12Bit
		pixelType = pixelType.replaceAll("Bit", "1-bit (unsigned)");
		pixelType = pixelType.replaceAll("Short", "16-bit");
		pixelType = pixelType.replaceAll("Int", "32-bit");
		pixelType = pixelType.replaceAll("Long", "64-bit");
		pixelType = pixelType.replaceAll("Float", "32-bit (real)");
		pixelType = pixelType.replaceAll("Double", "64-bit (real)");
		pixelType = pixelType.replaceAll("Unsigned(.*)", "$1 (unsigned)");
		if (pixelType.indexOf("(") < 0) pixelType = pixelType + " (signed)";
		return pixelType;
	}

	private RealType<?> getType() {
		return img.cursor().get();
	}

	/** Gets the dimensional extents of the dataset. */
	public long[] getDims() {
		final long[] dims = new long[img.numDimensions()];
		img.dimensions(dims);
		return dims;
	}

	public Object getPlane(final int no) {
		if (!(img instanceof PlanarAccess)) return null;
		// TODO - extract a copy the plane if it cannot be obtained by reference
		final PlanarAccess<?> planarAccess = (PlanarAccess<?>) img;
		final Object plane = planarAccess.getPlane(no);
		if (!(plane instanceof ArrayDataAccess)) return null;
		return ((ArrayDataAccess<?>) plane).getCurrentStorageArray();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setPlane(final int no, final Object plane) {
		if (!(img instanceof PlanarAccess)) return; // cannot set by reference
		// TODO - copy the plane if it cannot be set by reference
		final PlanarAccess planarAccess = (PlanarAccess) img;
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
		final RandomAccess<? extends RealType<?>> cursor = img.randomAccess();
		cursor.setPosition(pos);
		final double value = cursor.get().getRealDouble();
		return value;
	}

	// TEMP
	public boolean isSigned() {
		// HACK - imglib needs a way to query RealTypes for signedness
		final String typeName = getType().getClass().getName();
		return !typeName.startsWith("net.imglib2.type.numeric.integer.Unsigned");
	}

	// TEMP
	public boolean isFloat() {
		// HACK - imglib needs a way to query RealTypes for integer vs. float
		final String typeName = getType().getClass().getName();
		return typeName.equals("net.imglib2.type.numeric.real.FloatType") ||
			typeName.equals("net.imglib2.type.numeric.real.DoubleType");
	}

	/**
	 * Deletes the given dataset, cleaning up resources and removing it from the
	 * object manager.
	 */
	public void delete() {
		Events.publish(new DatasetDeletedEvent(this));
	}

	/** Displays the Dataset as a string (for now simply its name). */
	@Override
	public String toString() {
		return this.metadata.getName();
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final Dataset dataset) {
		return getMetadata().getName().compareTo(dataset.getMetadata().getName());
	}

	// -- Static utility methods --

	// TODO - relocate this when it's clear where it should go
	public static <T extends RealType<T> & NativeType<T>> Img<T>
		createPlanarImage(final T type, final long[] dims)
	{
		final PlanarImgFactory<T> imgFactory = new PlanarImgFactory<T>();
		final PlanarImg<T, ?> planarImg = imgFactory.create(dims, type);
		return planarImg;
	}

	public static Dataset create(final String name, final long[] dims,
		final AxisLabel[] axes, final int bitsPerPixel, final boolean signed,
		final boolean floating)
	{
		if (bitsPerPixel == 1) {
			if (signed || floating) invalidParams(bitsPerPixel, signed, floating);
			return create(name, dims, axes, new BitType());
		}
		if (bitsPerPixel == 8) {
			if (floating) invalidParams(bitsPerPixel, signed, floating);
			if (signed) return create(name, dims, axes, new ByteType());
			return create(name, dims, axes, new UnsignedByteType());
		}
		if (bitsPerPixel == 12) {
			if (signed || floating) invalidParams(bitsPerPixel, signed, floating);
			return create(name, dims, axes, new Unsigned12BitType());
		}
		if (bitsPerPixel == 16) {
			if (floating) invalidParams(bitsPerPixel, signed, floating);
			if (signed) return create(name, dims, axes, new ShortType());
			return create(name, dims, axes, new UnsignedShortType());
		}
		if (bitsPerPixel == 32) {
			if (floating) {
				if (!signed) invalidParams(bitsPerPixel, signed, floating);
				return create(name, dims, axes, new FloatType());
			}
			if (signed) return create(name, dims, axes, new IntType());
			return create(name, dims, axes, new UnsignedIntType());
		}
		if (bitsPerPixel == 64) {
			if (!signed) invalidParams(bitsPerPixel, signed, floating);
			if (floating) return create(name, dims, axes, new DoubleType());
			return create(name, dims, axes, new LongType());
		}
		invalidParams(bitsPerPixel, signed, floating);
		return null;
	}

	public static <T extends RealType<T> & NativeType<T>> Dataset create(
		final String name, final long[] dims, final AxisLabel[] axes, final T type)
	{
		final Img<T> planarImg = createPlanarImage(type, dims);
		final Metadata metadata = new Metadata();
		metadata.setName(name);
		metadata.setAxes(axes);
		return new Dataset(planarImg, metadata);
	}

	private static void invalidParams(final int bitsPerPixel,
		final boolean signed, final boolean floating)
	{
		throw new IllegalStateException("Invalid parameters: bitsPerPixel=" +
			bitsPerPixel + ", signed=" + signed + ", floating=" + floating);
	}

}
