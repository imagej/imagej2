/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data;

import imagej.ImageJ;
import imagej.data.event.DatasetCreatedEvent;
import imagej.data.event.DatasetDeletedEvent;
import imagej.data.event.DatasetRGBChangedEvent;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.event.DatasetTypeChangedEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.util.Log;
import net.imglib2.Cursor;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

/**
 * Default implementation of {@link Dataset}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class DefaultDataset extends AbstractData implements Dataset {

	private ImgPlus<? extends RealType<?>> imgPlus;
	private boolean rgbMerged;
	private boolean isDirty;

	public DefaultDataset(final ImageJ context,
		final ImgPlus<? extends RealType<?>> imgPlus)
	{
		super(context);
		this.imgPlus = imgPlus;
		rgbMerged = false;
		isDirty = false;
	}

	// -- AbstractData methods --

	@Override
	protected void register() {
		publish(new DatasetCreatedEvent(this));
	}

	@Override
	protected void delete() {
		publish(new DatasetDeletedEvent(this));
	}

	// -- Dataset methods --

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public void setDirty(final boolean value) {
		isDirty = value;
	}

	@Override
	public ImgPlus<? extends RealType<?>> getImgPlus() {
		return imgPlus;
	}

	@Override
	public <T extends RealType<T>> ImgPlus<T> typedImg(final T t) {
		final ImgPlus<? extends RealType<?>> img = getImgPlus();
		if (t.getClass().isAssignableFrom(img.firstElement().getClass())) {
			@SuppressWarnings("unchecked")
			final ImgPlus<T> typedImg = (ImgPlus<T>) img;
			return typedImg;
		}
		return null;
	}

	@Override
	public void setImgPlus(final ImgPlus<? extends RealType<?>> imgPlus) {

		final boolean wasRgbMerged = isRGBMerged();

		// are types different
		boolean typeChanged = false;
		if (imgPlus.firstElement().getClass() != getType().getClass()) {
			typeChanged = true;
		}

		this.imgPlus = imgPlus;

		// NB - keeping all the old metadata for now. TODO - revisit this?
		// NB - keeping isRgbMerged status for now. TODO - revisit this?

		if (wasRgbMerged) {
			if ((isSigned()) || (!isInteger()) ||
				(getType().getBitsPerPixel() != 8) ||
				(imgPlus.getAxisIndex(Axes.CHANNEL) < 0) ||
				(imgPlus.dimension(getAxisIndex(Axes.CHANNEL)) != 3)) setRGBMerged(false);
		}

		rebuild();

		if (typeChanged) typeChange();
	}

	@Override
	public Object getPlane(final int planeNumber) {
		return getPlane(planeNumber, true);
	}

	/**
	 * gets a plane of data from the Dataset. The representation of the plane is
	 * determined by the native ImgLib container. The behavior of this method when
	 * a reference to the actual data cannot be obtained depends upon the value of
	 * the input copyOK boolean. If copyOK is true a copy of the data is created
	 * and returned. If copyOK is false null is returned.
	 */
	@Override
	public Object getPlane(final int planeNumber, final boolean copyOK) {
		final Img<? extends RealType<?>> img = imgPlus.getImg();
		if (img instanceof PlanarAccess) {
			final PlanarAccess<?> planarAccess = (PlanarAccess<?>) img;
			final Object plane = planarAccess.getPlane(planeNumber);
			if (plane instanceof ArrayDataAccess) return ((ArrayDataAccess<?>) plane)
				.getCurrentStorageArray();
		}
		if (copyOK) return copyOfPlane(planeNumber);
		return null;
	}

	@Override
	public void setPlane(final int no, final Object newPlane) {
		final Img<? extends RealType<?>> img = imgPlus.getImg();
		if (!(img instanceof PlanarAccess)) {
			// cannot set by reference
			Log.error("Cannot set plane for non-planar image");
			return;
		}
		// TODO - copy the plane if it cannot be set by reference
		@SuppressWarnings("rawtypes")
		final PlanarAccess planarAccess = (PlanarAccess) img;
		final ArrayDataAccess<?> arrayAccess =
			(ArrayDataAccess<?>) planarAccess.getPlane(no);
		final Object currPlane = arrayAccess.getCurrentStorageArray();
		if (newPlane == currPlane) return;
		final ArrayDataAccess<?> array = createArrayDataAccess(newPlane);
		setPlane(no, planarAccess, array);
		update();
	}

	@Override
	public RealType<?> getType() {
		return imgPlus.firstElement();
	}

	@Override
	public boolean isSigned() {
		return getType().getMinValue() < 0;
	}

	@Override
	public boolean isInteger() {
		return getType() instanceof IntegerType;
	}

	@Override
	public String getTypeLabelShort() {
		if (isRGBMerged()) return "RGB";
		final int bitsPerPixel = getType().getBitsPerPixel();
		final String category =
			isInteger() ? isSigned() ? "int" : "uint" : "float";
		return category + bitsPerPixel;
	}

	@Override
	public String getTypeLabelLong() {
		if (isRGBMerged()) return "RGB color";
		final int bitsPerPixel = getType().getBitsPerPixel();
		final String category =
			isInteger() ? isSigned() ? "signed" : "unsigned" : "float";
		return category + " " + bitsPerPixel + "-bit";
	}

	@Override
	public Dataset duplicate() {
		ImgPlus<? extends RealType<?>> newImgPlus = imgPlus.copy();
		return new DefaultDataset(getContext(), newImgPlus);
		// OLD WAY
		//final Dataset d = duplicateBlank();
		//copyInto(d);
		//return d;
	}

	@Override
	public Dataset duplicateBlank() {
		@SuppressWarnings("rawtypes")
		final ImgPlus untypedImg = imgPlus;
		@SuppressWarnings("unchecked")
		final Dataset d =
			new DefaultDataset(getContext(), createBlankCopy(untypedImg));
		d.setRGBMerged(isRGBMerged());
		return d;
	}

	@Override
	public void copyInto(final Dataset target) {
		copyDataValues(imgPlus, target.getImgPlus());
		target.update();
	}

	@Override
	public void setRGBMerged(final boolean rgbMerged) {
		if (rgbMerged != this.rgbMerged) rgbChange();
		this.rgbMerged = rgbMerged;
	}

	@Override
	public boolean isRGBMerged() {
		return rgbMerged;
	}

	@Override
	public void typeChange() {
		setDirty(true);
		publish(new DatasetTypeChangedEvent(this));
	}

	@Override
	public void rgbChange() {
		// TODO - not sure if this needs to be done here
		// setDirty(true);
		publish(new DatasetRGBChangedEvent(this));
	}

	@Override
	public void copyDataFrom(final Dataset other) {
		// create a new img to hold data using our own factory
		@SuppressWarnings("rawtypes")
		final ImgFactory factory = getImgPlus().factory();
		@SuppressWarnings("unchecked")
		final Img<? extends RealType<?>> newImg =
			factory.create(other.getDims(), other.getType());

		// copy the data into the new img
		copyDataValues(other.getImgPlus(), newImg);

		// create new imgplus to contain data using the current name
		final double[] calib = new double[other.getDims().length];
		other.calibration(calib);
		final ImgPlus<? extends RealType<?>> newImgPlus =
			wrapAsImgPlus(newImg, other.getAxes(), calib);

		// set my instance vars to the new values
		setRGBMerged(other.isRGBMerged());
		setImgPlus(newImgPlus);
	}

	// -- Data methods --

	@Override
	public void update() {
		setDirty(true);
		publish(new DatasetUpdatedEvent(this));
	}

	@Override
	public void rebuild() {
		setDirty(true);
		publish(new DatasetRestructuredEvent(this));
	}

	// -- CalibratedInterval methods --

	@Override
	public boolean isDiscrete() {
		return true;
	}

	// -- CalibratedSpace methods --

	@Override
	public int getAxisIndex(final AxisType axis) {
		return imgPlus.getAxisIndex(axis);
	}

	@Override
	public AxisType axis(final int d) {
		return imgPlus.axis(d);
	}

	@Override
	public void axes(final AxisType[] axes) {
		imgPlus.axes(axes);
	}

	@Override
	public void setAxis(final AxisType axis, final int d) {
		imgPlus.setAxis(axis, d);
		update();
	}

	@Override
	public double calibration(final int d) {
		return imgPlus.calibration(d);
	}

	@Override
	public void calibration(final double[] cal) {
		imgPlus.calibration(cal);
	}

	@Override
	public void setCalibration(final double cal, final int d) {
		if (imgPlus.calibration(d) == cal) return;
		imgPlus.setCalibration(cal, d);
		update();
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return imgPlus.numDimensions();
	}

	// -- Interval methods --

	@Override
	public long min(final int d) {
		return imgPlus.min(d);
	}

	@Override
	public void min(final long[] min) {
		imgPlus.min(min);
	}

	@Override
	public void min(final Positionable min) {
		imgPlus.min(min);
	}

	@Override
	public long max(final int d) {
		return imgPlus.max(d);
	}

	@Override
	public void max(final long[] max) {
		imgPlus.max(max);
	}

	@Override
	public void max(final Positionable max) {
		imgPlus.max(max);
	}

	@Override
	public void dimensions(final long[] dimensions) {
		imgPlus.dimensions(dimensions);
	}

	@Override
	public long dimension(final int d) {
		return imgPlus.dimension(d);
	}

	// -- RealInterval methods --

	@Override
	public double realMin(final int d) {
		return imgPlus.realMin(d);
	}

	@Override
	public void realMin(final double[] min) {
		imgPlus.realMin(min);
	}

	@Override
	public void realMin(final RealPositionable min) {
		imgPlus.realMin(min);
	}

	@Override
	public double realMax(final int d) {
		return imgPlus.realMax(d);
	}

	@Override
	public void realMax(final double[] max) {
		imgPlus.realMax(max);
	}

	@Override
	public void realMax(final RealPositionable max) {
		imgPlus.realMax(max);
	}

	// -- Named methods --

	@Override
	public String getName() {
		return imgPlus.getName();
	}

	@Override
	public void setName(final String name) {
		if (imgPlus.getName().equals(name)) return;
		imgPlus.setName(name);
		update();
	}

	// -- Sourced methods --
	
	@Override
	public String getSource() {
		return imgPlus.getSource();
	}
	
	@Override
	public void setSource(String source) {
		imgPlus.setSource(source);
	}
	
	// -- ImageMetadata methods --

	@Override
	public int getValidBits() {
		final int validBits = imgPlus.getValidBits();
		if (validBits > 0) return validBits;
		return getType().getBitsPerPixel();
	}

	@Override
	public void setValidBits(final int bits) {
		imgPlus.setValidBits(bits);
	}

	@Override
	public double getChannelMinimum(final int c) {
		return imgPlus.getChannelMinimum(c);
	}

	@Override
	public void setChannelMinimum(final int c, final double min) {
		imgPlus.setChannelMinimum(c, min);
	}

	@Override
	public double getChannelMaximum(final int c) {
		return imgPlus.getChannelMaximum(c);
	}

	@Override
	public void setChannelMaximum(final int c, final double max) {
		imgPlus.setChannelMaximum(c, max);
	}

	@Override
	public int getCompositeChannelCount() {
		return imgPlus.getCompositeChannelCount();
	}

	@Override
	public void setCompositeChannelCount(final int count) {
		imgPlus.setCompositeChannelCount(count);
	}

	@Override
	public ColorTable8 getColorTable8(final int no) {
		return imgPlus.getColorTable8(no);
	}

	@Override
	public void setColorTable(final ColorTable8 lut, final int no) {
		imgPlus.setColorTable(lut, no);
		// TODO - ???
		// update();
	}

	@Override
	public ColorTable16 getColorTable16(final int no) {
		return imgPlus.getColorTable16(no);
	}

	@Override
	public void setColorTable(final ColorTable16 lut, final int no) {
		imgPlus.setColorTable(lut, no);
		// TODO - ???
		// update();
	}

	@Override
	public void initializeColorTables(final int count) {
		imgPlus.initializeColorTables(count);
		// TODO - ???
		// update();
	}

	@Override
	public int getColorTableCount() {
		return imgPlus.getColorTableCount();
	}

	@Override
	public double getBytesOfInfo() {
		final double bitsPerPix = getType().getBitsPerPixel();
		final long[] dims = getDims();
		long pixCount = 1;
		for (final long dimSize : dims)
			pixCount *= dimSize;
		final double totBits = bitsPerPix * pixCount;
		return totBits / 8;
	}

	// -- Helper methods --

	/**
	 * Wraps the given primitive array in an {@link ArrayDataAccess} object of the
	 * proper type.
	 */
	private ArrayDataAccess<?> createArrayDataAccess(final Object newPlane) {
		if (newPlane instanceof byte[]) {
			return new ByteArray((byte[]) newPlane);
		}
		else if (newPlane instanceof short[]) {
			return new ShortArray((short[]) newPlane);
		}
		else if (newPlane instanceof int[]) {
			return new IntArray((int[]) newPlane);
		}
		else if (newPlane instanceof float[]) {
			return new FloatArray((float[]) newPlane);
		}
		else if (newPlane instanceof long[]) {
			return new LongArray((long[]) newPlane);
		}
		else if (newPlane instanceof double[]) {
			return new DoubleArray((double[]) newPlane);
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setPlane(final int no, final PlanarAccess planarAccess,
		final ArrayDataAccess<?> array)
	{
		planarAccess.setPlane(no, array);
	}

	// NB - assumes the two images are of the exact same dimensions
	private void copyDataValues(final Img<? extends RealType<?>> input,
		final Img<? extends RealType<?>> output)
	{
		final long[] position = new long[output.numDimensions()];
		final Cursor<? extends RealType<?>> outputCursor =
			output.localizingCursor();
		final RandomAccess<? extends RealType<?>> inputAccessor =
			input.randomAccess();
		while (outputCursor.hasNext()) {
			outputCursor.next();
			outputCursor.localize(position);
			inputAccessor.setPosition(position);
			final double value = inputAccessor.get().getRealDouble();
			outputCursor.get().setReal(value);
		}
	}

	private Object copyOfPlane(final int planeNum) {
		final long[] dimensions = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dimensions);
		final long w = dimensions[0];
		final long h = dimensions[1];
		if (w * h > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("cannot create a plane of " +
				(w * h) + " entities (MAX = " + Integer.MAX_VALUE + ")");
		}
		final NativeType<?> nativeType = (NativeType<?>) getType();
		@SuppressWarnings("rawtypes")
		final NativeImgFactory storageFactory = new ArrayImgFactory();
		@SuppressWarnings("unchecked")
		final ArrayImg<?, ?> container =
			(ArrayImg<?, ?>) nativeType.createSuitableNativeImg(storageFactory,
				new long[] { w, h });
		final RandomAccess<? extends RealType<?>> input = imgPlus.randomAccess();
		@SuppressWarnings("unchecked")
		final RandomAccess<? extends RealType<?>> output =
			(RandomAccess<? extends RealType<?>>) container.randomAccess();
		final long[] planeIndexSpans = new long[dimensions.length - 2];
		for (int i = 0; i < planeIndexSpans.length; i++)
			planeIndexSpans[i] = dimensions[i + 2];
		final Extents planeExtents = new Extents(planeIndexSpans);
		final long[] planePos = new long[planeExtents.numDimensions()];
		final Position pos = planeExtents.createPosition();
		pos.setIndex(planeNum);
		pos.localize(planePos);
		final long[] inputPos = new long[dimensions.length];
		for (int i = 2; i < dimensions.length; i++)
			inputPos[i] = planePos[i - 2];
		final long[] outputPos = new long[2];
		input.setPosition(inputPos);
		output.setPosition(outputPos);
		final RealType<?> inputRef = input.get();
		final RealType<?> outputRef = output.get();
		final int maxX = (int) (w - 1);
		final int maxY = (int) (h - 1);
		for (int y = 0; y <= maxY; y++) {
			for (int x = 0; x <= maxX; x++) {
				final double value = inputRef.getRealDouble();
				outputRef.setReal(value);
				if (x != maxX) {
					input.move(1, 0);
					output.move(1, 0);
				}
			}
			if (y != maxY) {
				input.move(-maxX, 0);
				output.move(-maxX, 0);
				input.move(1, 1);
				output.move(1, 1);
			}
		}
		final ArrayDataAccess<?> store =
			(ArrayDataAccess<?>) container.update(null);
		return store.getCurrentStorageArray();
	}

	/** Makes an image that has same type, container, and dimensions as refImage. */
	private static <T extends RealType<T>> ImgPlus<T> createBlankCopy(
		final ImgPlus<T> img)
	{
		final long[] dimensions = new long[img.numDimensions()];
		img.dimensions(dimensions);
		final Img<T> blankImg =
			img.factory().create(dimensions, img.firstElement());
		return new ImgPlus<T>(blankImg, img);
	}

	private <T extends RealType<?>> ImgPlus<T> wrapAsImgPlus(
		final Img<T> newImg, final AxisType[] axes, final double[] calib)
	{
		return new ImgPlus<T>(newImg, getName(), axes, calib);
	}

}
