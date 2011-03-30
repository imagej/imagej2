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
import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.container.basictypecontainer.array.ByteArray;
import mpicbg.imglib.container.basictypecontainer.array.DoubleArray;
import mpicbg.imglib.container.basictypecontainer.array.FloatArray;
import mpicbg.imglib.container.basictypecontainer.array.IntArray;
import mpicbg.imglib.container.basictypecontainer.array.LongArray;
import mpicbg.imglib.container.basictypecontainer.array.ShortArray;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.Type;
import mpicbg.imglib.type.numeric.RealType;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class Dataset implements Comparable<Dataset> {

	private Image<?> image;
	private final Metadata metadata;
	private boolean isRgbMerged;
	
	// FIXME TEMP - the current selection for this Dataset. Temporarily located
	//   here for plugin testing purposes. Really should be viewcentric.
	private Rect selection;
	public void setSelection(int minX, int minY, int maxX, int maxY)
	{
		selection.x = minX;
		selection.y = minY;
		selection.width = maxX-minX+1;
		selection.height = maxY-minY+1;
	}
	public Rect getSelection()
	{
		return selection;
	}
	// END FIXME TEMP

	public Dataset(final Image<?> image) {
		this(image, Metadata.createMetadata(image));
	}

	public Dataset(final Image<?> image, final Metadata metadata) {
		if (metadata == null) {
			throw new IllegalArgumentException("Metadata must not be null");
		}
		this.image = image;
		this.metadata = metadata;
		this.isRgbMerged = false;
		this.selection = new Rect();
		Events.publish(new DatasetCreatedEvent(this));
	}

	/** to be used in legacy layer only. allows the various legacy layer image translators to support color images correctly. */
	public void setIsRgbMerged(boolean value) {
		this.isRgbMerged = value;
	}
	
	/** to be used in legacy layer only. allows the various legacy layer image translators to support color images correctly. */
	public boolean isRgbMerged() {
		return isRgbMerged;
	}
	
	public Image<?> getImage() {
		return image;
	}

	public void setImage(Image<?> newImageData) {
		
		if (image.getNumDimensions() != newImageData.getNumDimensions())
			throw new IllegalArgumentException();
		
		this.image = newImageData;
		
		// NB - keeping all the old metadata for now. TODO - revisit this?
		// NB - keeping isRgnMerged status for now. TODO - revisit this?
		
		Events.publish(new DatasetChangedEvent(this));
	}

	public Metadata getMetadata() {
		return metadata;
	}

	/** Gets a string description of the dataset's pixel type. */
	public String getPixelType() {
		// HACK: Since type.toString() isn't nice, we use crazy logic here.
		// TODO: Eliminate this, by improving type.toString() in ImgLib2.
		final Type<?> type = image.createType();
		String pixelType = type.getClass().getSimpleName();
		pixelType = pixelType.replaceAll("Type", "");
		pixelType = pixelType.replaceAll("Bit", "1-bit (unsigned)");
		pixelType = pixelType.replaceAll("Byte", "8-bit");
		pixelType = pixelType.replaceAll("([0-9]+)Bit", "$1-bit"); // e.g., 12Bit
		pixelType = pixelType.replaceAll("Short", "16-bit");
		pixelType = pixelType.replaceAll("Int", "32-bit");
		pixelType = pixelType.replaceAll("Long", "64-bit");
		pixelType = pixelType.replaceAll("Float", "32-bit (real)");
		pixelType = pixelType.replaceAll("Double", "64-bit (real)");
		pixelType = pixelType.replaceAll("Unsigned(.*)", "$1 (unsigned)");
		if (pixelType.indexOf("(") < 0) pixelType = pixelType + " (signed)";
		return pixelType;
	}

	public Object getPlane(final int no) {
		final Container<?> container = image.getContainer();
		if (!(container instanceof PlanarAccess)) return null;
		final Object plane = ((PlanarAccess<?>) container).getPlane(no);
		if (!(plane instanceof ArrayDataAccess)) return null;
		return ((ArrayDataAccess<?>) plane).getCurrentStorageArray();
	}

	@SuppressWarnings({"rawtypes","unchecked"})
	public void setPlane(final int no, final Object plane) {
		final Container<?> container = image.getContainer();
		if (!(container instanceof PlanarAccess)) return;
		final PlanarAccess planarAccess = (PlanarAccess) container;
		ArrayDataAccess<?> array = null;
		if (plane instanceof byte[]) {
			array = new ByteArray((byte[]) plane);
		}
		else if (plane instanceof short[] ) {
			array = new ShortArray((short[]) plane);
		}
		else if (plane instanceof int[] ) {
			array = new IntArray((int[]) plane);
		}
		else if (plane instanceof float[] ) {
			array = new FloatArray((float[]) plane);
		}
		else if (plane instanceof long[] ) {
			array = new LongArray((long[]) plane);
		}
		else if (plane instanceof double[] ) {
			array = new DoubleArray((double[]) plane);
		}
		planarAccess.setPlane(no, array);
	}

	public double getDoubleValue(final int[] pos) {
		// NB: This method is slow... will change anyway with ImgLib2.
		@SuppressWarnings("unchecked")
		final LocalizableByDimCursor<? extends RealType<?>> cursor =
			(LocalizableByDimCursor<? extends RealType<?>>)
			image.createLocalizableByDimCursor();
		cursor.setPosition(pos);
		final double value = cursor.getType().getRealDouble();
		cursor.close();
		return value;
	}

	// TEMP
	public boolean isSigned() {
		// HACK - imglib needs a way to query RealTypes for signedness
		final String typeName = image.createType().getClass().getName();
		return !typeName.startsWith("mpicbg.imglib.type.numeric.integer.Unsigned");
	}

	// TEMP
	public boolean isFloat() {
		// HACK - imglib needs a way to query RealTypes for integer vs. float
		final String typeName = image.createType().getClass().getName();
		return typeName.equals("mpicbg.imglib.type.numeric.real.FloatType")
			|| typeName.equals("mpicbg.imglib.type.numeric.real.DoubleType");
	}

	/**
	 * Deletes the given dataset, cleaning up resources
	 * and removing it from the object manager.
	 */
	public void delete() {
		Events.publish(new DatasetDeletedEvent(this));
	}

	/**
	 * displays the Dataset as a string (for now simply its name)
	 */
	@Override
	public String toString() {
		return this.metadata.getName();
	}
	// -- Comparable methods --

	@Override
	public int compareTo(Dataset dataset) {
		return getMetadata().getName().compareTo(dataset.getMetadata().getName());
	}

	// -- Static utility methods --

	// TODO - relocate this when its clear where it should go
	public static <T extends RealType<T>> Image<T> createPlanarImage(final String name, final T type, final int[] dims)
	{
		final PlanarContainerFactory pcf = new PlanarContainerFactory();
		final ImageFactory<T> imageFactory = new ImageFactory<T>(type, pcf);
		return imageFactory.createImage(dims, name);
	}
	
	public static <T extends RealType<T>> Dataset create(final String name,
		final T type, final int[] dims, final AxisLabel[] axes)
	{
		Image<T> image = createPlanarImage(name, type, dims);
		final Metadata metadata = new Metadata();
		metadata.setName(name);
		metadata.setAxes(axes);
		return new Dataset(image, metadata);
	}

}
