/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.data;

import imagej.data.event.DatasetCreatedEvent;
import imagej.data.event.DatasetDeletedEvent;
import imagej.data.event.DatasetRGBChangedEvent;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.event.DatasetTypeChangedEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.data.types.DataTypeService;

import java.util.Iterator;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableRealInterval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.display.ColorTable;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
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
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.meta.IntervalUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * Default implementation of {@link Dataset}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class DefaultDataset extends AbstractData implements Dataset {

	@Parameter
	private LogService log;

	@Parameter
	private DataTypeService dataTypeService;

	private ImgPlus<? extends RealType<?>> imgPlus;
	private boolean rgbMerged;
	private boolean isDirty;

	public DefaultDataset(final Context context,
		final ImgPlus<? extends RealType<?>> imgPlus)
	{
		super(context, imgPlus);
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

		// set rgb merged status
		if (wasRgbMerged) {
			if (!mergedColorCompatible())
			{
				setRGBMerged(false);
			}
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean setPlaneSilently(final int planeNum, final Object newPlane) {
		final Img<? extends RealType<?>> img = imgPlus.getImg();
		if (!(img instanceof PlanarAccess)) {
			// cannot set by reference
			log.error("Cannot set plane for non-planar image");
			return false;
		}
		// TODO - copy the plane if it cannot be set by reference
		@SuppressWarnings("rawtypes")
		final PlanarAccess planarAccess = (PlanarAccess) img;
		final ArrayDataAccess<?> arrayAccess =
			(ArrayDataAccess<?>) planarAccess.getPlane(planeNum);
		final Object currPlane = arrayAccess.getCurrentStorageArray();
		if (newPlane == currPlane) return false;
		final ArrayDataAccess<?> array = createArrayDataAccess(newPlane);
		planarAccess.setPlane(planeNum, array);
		return true;
	}

	@Override
	public boolean setPlane(final int planeNum, final Object newPlane) {
		if (setPlaneSilently(planeNum, newPlane)) {
			update(false);
			return true;
		}
		return false;
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
		NumericType<?> type = getImgPlus().firstElement();
		return dataTypeService.getTypeByClass(type.getClass()).shortName();
	}

	@Override
	public String getTypeLabelLong() {
		if (isRGBMerged()) return "RGB color";
		NumericType<?> type = getImgPlus().firstElement();
		return dataTypeService.getTypeByClass(type.getClass()).longName();
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
		if (rgbMerged == this.rgbMerged) return;
		if (rgbMerged && !mergedColorCompatible()) {
			throw new IllegalArgumentException("This dataset is not color compatible");
		}
		this.rgbMerged = rgbMerged;
		rgbChange();
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
			factory.create(IntervalUtils.getDims(other), other.getType());

		// copy the data into the new img
		copyDataValues(other.getImgPlus(), newImg);

		// create new imgplus to contain data using the current name
		CalibratedAxis[] calibAxes = new CalibratedAxis[other.numDimensions()];
		other.axes(calibAxes);
		CalibratedAxis[] calibAxesCopy = new CalibratedAxis[calibAxes.length];
		for (int i = 0; i < calibAxes.length; i++) {
			calibAxesCopy[i] = calibAxes[i].copy();
		}
		final ImgPlus<? extends RealType<?>> newImgPlus =
			wrapAsImgPlus(newImg, calibAxesCopy);

		// make sure we grab color tables too
		// TODO - disable this option? It's a question of what we think data is.
		int count = other.getColorTableCount();
		newImgPlus.initializeColorTables(count);
		for (int i = 0; i < count; i++) {
			newImgPlus.setColorTable(other.getColorTable(i), i);
		}

		// set my instance vars to the new values
		setRGBMerged(other.isRGBMerged());
		setImgPlus(newImgPlus);
	}

	// -- Data methods --

	@Override
	public void update() {
		update(false);
	}

	@Override
	public void rebuild() {
		setDirty(true);
		publish(new DatasetRestructuredEvent(this));
	}

	// -- CalibratedSpace methods --

	@Override
	public int dimensionIndex(final AxisType axis) {
		return imgPlus.dimensionIndex(axis);
	}

	@Override
	public CalibratedAxis axis(final int d) {
		return imgPlus.axis(d);
	}

	@Override
	public void axes(final CalibratedAxis[] axes) {
		imgPlus.axes(axes);
	}

	@Override
	public void setAxes(final CalibratedAxis[] axes) {
		if (axes.length != numDimensions())
			throw new IllegalArgumentException(
				"number of axes must match dimensionality of dataset");
		boolean changes = false;
		for (int i = 0; i < axes.length; i++) {
			if (!imgPlus.axis(i).equals(axes[i])) {
				changes = true;
				imgPlus.setAxis(axes[i], i);
			}
		}
		if (changes) rebuild();
	}

	@Override
	public void setAxis(final CalibratedAxis axis, final int d) {
		if (axis.equals(imgPlus.axis(d))) return;
		imgPlus.setAxis(axis, d);
		update(true); // TODO : false instead of true?
		// Maybe we need more levels of discrimination with update(bool)
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
		update(true);
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
	public ColorTable getColorTable(final int no) {
		return imgPlus.getColorTable(no);
	}

	@Override
	public void setColorTable(final ColorTable lut, final int no) {
		imgPlus.setColorTable(lut, no);
		// TODO - ???
		// update(false);
	}

	@Override
	public void initializeColorTables(final int count) {
		imgPlus.initializeColorTables(count);
		// TODO - ???
		// update(false);
	}

	@Override
	public int getColorTableCount() {
		return imgPlus.getColorTableCount();
	}

	@Override
	public double getBytesOfInfo() {
		final double bitsPerPix = getType().getBitsPerPixel();
		long pixCount = 1;
		for (int d = 0; d < numDimensions(); d++) {
			pixCount *= dimension(d);
		}
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
			throw new IllegalArgumentException(
				"Can't create an in memory plane of " + (w * h) +
					" entities (MAX = " + Integer.MAX_VALUE + ")");
		}
		final Type<?> type = getType();
		// might not be able to get a copy of native data
		if (!(type instanceof NativeType<?>)) return null;
		final NativeType<?> nativeType = (NativeType<?>) type;
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

	private <T extends RealType<?>> ImgPlus<T> wrapAsImgPlus(final Img<T> newImg,
		final CalibratedAxis... calibAxes)
	{
		return new ImgPlus<T>(newImg, getName(), calibAxes);
	}

	private void update(boolean metadataOnly) {
		setDirty(true);
		publish(new DatasetUpdatedEvent(this, metadataOnly));
	}

	private boolean mergedColorCompatible() {
		if (isSigned()) return false;
		if (!isInteger()) return false;
		if (getType().getBitsPerPixel() != 8) return false;
		if (imgPlus.dimensionIndex(Axes.CHANNEL) < 0) return false;
		if (imgPlus.dimension(dimensionIndex(Axes.CHANNEL)) != 3) return false;
		return true;
	}

	@Override
	public Img<RealType<?>> copy() {
		final ImgPlus<? extends RealType<?>> copy = getImgPlus().copy();
		return new DefaultDataset(getContext(), copy);
	}

	@Override
	public ImgFactory<RealType<?>> factory() {
		throw new UnsupportedOperationException("TODO");
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccess<RealType<?>> randomAccess() {
		return (RandomAccess<RealType<?>>) getImgPlus().randomAccess();
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccess<RealType<?>> randomAccess(Interval interval) {
		return (RandomAccess<RealType<?>>) getImgPlus().randomAccess(interval);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Cursor<RealType<?>> cursor() {
		return (Cursor<RealType<?>>) getImgPlus().cursor();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Cursor<RealType<?>> localizingCursor() {
		return (Cursor<RealType<?>>) getImgPlus().localizingCursor();
	}

	@Override
	public boolean equalIterationOrder(IterableRealInterval<?> interval) {
		return getImgPlus().equalIterationOrder(interval);
	}

	@Override
	public RealType<?> firstElement() {
		return getImgPlus().firstElement();
	}

	@Override
	public Object iterationOrder() {
		return getImgPlus().iterationOrder();
	}

	@Override
	public long size() {
		return getImgPlus().size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<RealType<?>> iterator() {
		return (Iterator<RealType<?>>) getImgPlus().iterator();
	}

	@Override
	public Map<String, Object> getProperties() {
		return imgPlus.getProperties();
	}
}
