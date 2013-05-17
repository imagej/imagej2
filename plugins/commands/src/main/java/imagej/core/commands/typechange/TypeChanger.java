/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.core.commands.typechange;

import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.module.DefaultMutableModuleItem;
import imagej.util.Prefs;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.input.InputIterator;
import net.imglib2.ops.input.PointSetInputIteratorFactory;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

/**
 * Transforms a {@link Dataset} (and linked {@link ImgPlus}) between types.
 * <p>
 * Although ImgLib does not do any range clamping of values we will do so here.
 * </p>
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public abstract class TypeChanger extends DynamicCommand {

	// -- constants --
	
	private static final String FIELDNAME = "imagej.type.change.make.composite";
	
	// -- Parameters --
	
	// TODO
	//@Parameter
	//private MenuService menuService;

	@Parameter(type = ItemIO.BOTH)
	private Dataset data;

	// -- protected methods --
	
	protected void maybeAddChannelInput() {
		int axisIndex = data.getAxisIndex(Axes.CHANNEL);
		if (axisIndex < 0) return;
		DefaultMutableModuleItem<Boolean> booleanItem =
				new DefaultMutableModuleItem<Boolean>(this, FIELDNAME, Boolean.class);
		booleanItem.setLabel("Combine channels");
		booleanItem.setDescription(
				"Combine all channels into one channel by averaging");
		booleanItem.setPersisted(false);
		Boolean val = Prefs.getBoolean(FIELDNAME, Boolean.FALSE);
		booleanItem.setValue(this, val);
		addInput(booleanItem);
	}
	
	protected <T extends RealType<T>> void changeType(final T newType) {
		Boolean b = (Boolean) getInput(FIELDNAME);
		if (b != null) Prefs.put(FIELDNAME,b);
		boolean compositeMode = (b == null) ? false : b;
		changeType(data, newType, compositeMode);
		// TODO
		//menuService.setSelected(this, true);
	}

	// -- TypeChanger methods --
	
	public void setDataset(Dataset d) {
		data = d;
	}
	
	public Dataset getDataset() {
		return data;
	}
	
	/**
	 * Changes the given {@link Dataset}'s underlying {@link Img} data to the
	 * specified type.
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public static <T extends RealType<T>> void changeType(final Dataset dataset,
		final T newType, boolean compositeMode)
	{
		// see if input dataset is already typed correctly
		if (dataset.isRGBMerged()) {
			// fall through
		}
		else if (dataset.getType().getClass() == newType.getClass()) {
			if (!compositeMode) return;
			int chanIndex = dataset.getAxisIndex(Axes.CHANNEL);
			if (chanIndex < 0) return;
		}
		// if here then a type change of some sort is needed
		final ImgPlus<? extends RealType<?>> inputImg = dataset.getImgPlus();
		final ImgPlus<? extends RealType<?>> imgPlus;
		if (compositeMode) {
			imgPlus = copyToCompositeGrayscale((ImgPlus) inputImg, newType);
		}
		else {
			imgPlus = copyToType(inputImg, newType);
		}
		dataset.setRGBMerged(false);  // event order requires this before setImgPlus()
		dataset.setImgPlus(imgPlus);
		dataset.setCompositeChannelCount(1);
	}

	/**
	 * Creates a planar ImgLib {@link Img} of the given type. It populates the
	 * output {@link Img}'s data from the input {@link Img} (which is likely of a
	 * different data type). Output data is range clamped.
	 */
	public static <T extends RealType<T>> ImgPlus<? extends RealType<?>>
		copyToType(final ImgPlus<? extends RealType<?>> inputImg, final T newType)
	{
		final ImgFactory<? extends RealType<?>> factory = inputImg.factory();
		@SuppressWarnings("unchecked")
		final ImgFactory<T> typedFactory = (ImgFactory<T>) factory;
		return copyToType(inputImg, newType, typedFactory);
	}

	/**
	 * Creates an ImgLib {@link Img} of the given type using the specified
	 * {@link ImgFactory}. It populates the output {@link Img}'s data from the
	 * input {@link Img} (which is likely of a different data type). Output data
	 * is range clamped.
	 */
	public static <T extends RealType<T>> ImgPlus<T> copyToType(
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

		final boolean inputIs1Bit = in.get().getBitsPerPixel() == 1;

		final long[] pos = new long[dims.length];
		while (in.hasNext()) {
			in.fwd();
			in.localize(pos);
			out.setPosition(pos);
			double value = in.get().getRealDouble();
			if (value < outTypeMin) value = outTypeMin;
			if (value > outTypeMax) value = outTypeMax;
			if (inputIs1Bit && value > 0) value = outTypeMax;
			out.get().setReal(value);
		}

		return new ImgPlus<T>(outputImg, inputImg);
	}

	// -- private helpers --
	
	// TODO - make public?

	/**
	 * Creates an output {@link ImgPlus} of specified type by averaging the
	 * channel values of an input {@link ImgPlus}.
	 */
	private static <I extends RealType<I>, O extends RealType<O>> ImgPlus<O>
		copyToCompositeGrayscale(final ImgPlus<I> inputImg, final O newType)
	{
		// determine the attributes of the output image
		final String name = inputImg.getName();
		final long[] dims = outputDims(inputImg);
		final AxisType[] axes = outputAxes(inputImg);
		final double[] cal = outputCalibration(inputImg);

		// create the output image
		final ImgFactory<? extends RealType<?>> factory = inputImg.factory();
		@SuppressWarnings("unchecked")
		final ImgFactory<O> typedFactory = (ImgFactory<O>) factory;
		final Img<O> outputImg = typedFactory.create(dims, newType);

		// Would be nice
		// for each set of channels in the image
		// figure out what color it is
		// turn that color into an intensity value for the given type
		// set the output pixel to that value
		//
		// problem though: we don't have color table info except for view
		//
		// so instead of color just average the channel intensities
		// and we could do a special case for rgb that uses formula later

		// determine channel space
		final int chIndex = inputImg.getAxisIndex(Axes.CHANNEL);
		final int numInputDims = inputImg.numDimensions();
		final long[] minChanPt = new long[numInputDims];
		final long[] maxChanPt = new long[numInputDims];
		if (chIndex >= 0) maxChanPt[chIndex] = inputImg.dimension(chIndex) - 1;
		final PointSet channelColumn =
			new HyperVolumePointSet(minChanPt, maxChanPt);

		// determine data space without channels
		final long[] minDimPt = new long[numInputDims];
		final long[] maxDimPt = new long[numInputDims];
		for (int i = 0; i < numInputDims; i++) {
			if (i != chIndex) maxDimPt[i] = inputImg.dimension(i) - 1;
		}
		final PointSet allOtherDims = new HyperVolumePointSet(minDimPt, maxDimPt);

		// setup neighborhood iterator
		final PointSetInputIteratorFactory inputFactory =
			new PointSetInputIteratorFactory(channelColumn);
		final InputIterator<PointSet> iter =
			inputFactory.createInputIterator(allOtherDims);

		// build averaging function
		final Function<long[], DoubleType> valFunc =
			new RealImageFunction<I, DoubleType>(inputImg, new DoubleType());
		final Function<PointSet, DoubleType> avgFunc =
			new RealArithmeticMeanFunction<DoubleType>(valFunc);

		// do the iteration and copy the data
		final RandomAccess<? extends RealType<?>> out = outputImg.randomAccess();
		final boolean inputIs1Bit = inputImg.firstElement().getBitsPerPixel() == 1;
		final double outTypeMin = out.get().getMinValue();
		final double outTypeMax = out.get().getMaxValue();
		PointSet inputPointset = null;
		final long[] outputCoord = new long[outputImg.numDimensions()];
		final DoubleType avg = new DoubleType();
		while (iter.hasNext()) {
			inputPointset = iter.next(inputPointset);
			avgFunc.compute(inputPointset, avg);
			double value = avg.getRealDouble();
			if (value < outTypeMin) value = outTypeMin;
			if (value > outTypeMax) value = outTypeMax;
			if (inputIs1Bit && value > 0) value = outTypeMax;
			determineOutputCoordinate(chIndex, inputPointset.getOrigin(), outputCoord);
			out.setPosition(outputCoord);
			out.get().setReal(value);
		}

		// return the result
		return new ImgPlus<O>(outputImg, name, axes, cal);
	}

	/** Determines the axes of the output image ignoring channels if necessary. */
	private static AxisType[] outputAxes(final ImgPlus<?> inputImg) {
		final int inputAxisCount = inputImg.numDimensions();
		final int chanIndex = inputImg.getAxisIndex(Axes.CHANNEL);
		final int outputAxisCount =
			(chanIndex < 0) ? inputAxisCount : inputAxisCount - 1;
		final AxisType[] inputAxes = new AxisType[inputAxisCount];
		inputImg.axes(inputAxes);
		final AxisType[] outputAxes = new AxisType[outputAxisCount];
		int o = 0;
		for (int i = 0; i < inputAxisCount; i++) {
			if (i != chanIndex) outputAxes[o++] = inputAxes[i];
		}
		return outputAxes;
	}

	/** Determines the dims of the output image ignoring channels if necessary. */
	private static long[] outputDims(final ImgPlus<?> inputImg) {
		final int inputDimCount = inputImg.numDimensions();
		final int chanIndex = inputImg.getAxisIndex(Axes.CHANNEL);
		final int outputDimCount =
			(chanIndex < 0) ? inputDimCount : inputDimCount - 1;
		final long[] inputDims = new long[inputDimCount];
		inputImg.dimensions(inputDims);
		final long[] outputDims = new long[outputDimCount];
		int o = 0;
		for (int i = 0; i < inputDimCount; i++) {
			if (i != chanIndex) outputDims[o++] = inputDims[i];
		}
		return outputDims;
	}

	/**
	 * Determines the calibration of the output image ignoring channels if
	 * necessary.
	 */
	private static double[] outputCalibration(final ImgPlus<?> inputImg) {
		final int inputDimCount = inputImg.numDimensions();
		final int chanIndex = inputImg.getAxisIndex(Axes.CHANNEL);
		final int outputDimCount =
			(chanIndex < 0) ? inputDimCount : inputDimCount - 1;
		final double[] inputCal = new double[inputDimCount];
		inputImg.calibration(inputCal);
		final double[] outputCal = new double[outputDimCount];
		int o = 0;
		for (int i = 0; i < inputDimCount; i++) {
			if (i != chanIndex) outputCal[o++] = inputCal[i];
		}
		return outputCal;
	}

	/**
	 * Determines an output coordinate from an input coordinate ignoring the
	 * channel axis if necessary.
	 */
	private static void determineOutputCoordinate(final int chIndex,
		final long[] inputPt, final long[] outputPt)
	{
		int p = 0;
		for (int i = 0; i < inputPt.length; i++) {
			if (i != chIndex) outputPt[p++] = inputPt[i];
		}
	}

}
