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

package imagej.core.plugins.typechange;

import imagej.data.Dataset;
import imagej.ext.menu.MenuService;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.RunnablePlugin;
import imagej.ext.plugin.Parameter;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.Function;
import net.imglib2.ops.InputIterator;
import net.imglib2.ops.PointSet;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.input.PointSetInputIteratorFactory;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Transforms a {@link Dataset} (and linked {@link ImgPlus}) between types.
 * <p>
 * Although ImgLib does not do any range clamping of values we will do so here.
 * </p>
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public abstract class TypeChanger implements RunnablePlugin {

	@Parameter
	protected MenuService menuService;

	@Parameter(type = ItemIO.BOTH)
	protected Dataset input;

	protected <T extends RealType<T>> void changeType(
		final T newType)
	{
		changeType(input, newType);
		menuService.setSelected(this, true);
	}

	/**
	 * Changes the given {@link Dataset}'s underlying {@link Img} data to the
	 * specified type.
	 */
	public static <T extends RealType<T>> void changeType(
		final Dataset dataset, final T newType)
	{
		final ImgPlus<? extends RealType<?>> inputImg = dataset.getImgPlus();
		final Class<?> currTypeClass = dataset.getType().getClass();
		final Class<?> newTypeClass = newType.getClass();
		int chanIndex = dataset.getAxisIndex(Axes.CHANNEL);
		long numChannels = (chanIndex < 0) ? 1 : dataset.dimension(chanIndex);
		final boolean isColor = dataset.getCompositeChannelCount() == numChannels;
		if ((currTypeClass != newTypeClass) || isColor) {
			ImgPlus<? extends RealType<?>> imgPlus;
			if (isColor)
				imgPlus = colorToGrayscale(inputImg, newType);
			else
				imgPlus = copyToType(inputImg, newType);
			dataset.setImgPlus(imgPlus);
			dataset.setRGBMerged(false);
			dataset.setCompositeChannelCount(1);
		}
	}

	/**
	 * Creates a planar ImgLib {@link Img} of the given type. It populates the
	 * output {@link Img}'s data from the input {@link Img} (which is likely of a
	 * different data type). Output data is range clamped.
	 */
	public static <T extends RealType<T>>
		ImgPlus<? extends RealType<?>> copyToType(
			final ImgPlus<? extends RealType<?>> inputImg, final T newType)
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

	// TODO - make public?
	
	/** Creates an output ImgPlus of specified type by averaging the channel
	 * values of an input ImgPlus */
	private static
		<I extends RealType<I>, O extends RealType<O>>
		ImgPlus<O> colorToGrayscale(
				ImgPlus<? extends RealType<?>> inputImg, final O newType)
	{
		// determine the attributes of the output image
		String name = inputImg.getName();
		long[] dims = outputDims(inputImg);
		AxisType[] axes = outputAxes(inputImg);
		double[] cal = outputCalibration(inputImg);

		// create the output image
		ImgFactory<? extends RealType<?>> factory = inputImg.factory();
		@SuppressWarnings("unchecked")
		ImgFactory<O> typedFactory = (ImgFactory<O>) factory;
		Img<O> outputImg = typedFactory.create(dims, newType);
		
		// for each set of channels in the image
		//   figure out what color it is
		//   turn that into an intensity value from 0 to 255
		//   set the output pixel to that value

		// problems though: we don't have color table info except for view
		
		// so instead of color just average the channel intensities
		//   and could do a special case for rgb that uses formula

		// determine channel space
		int chIndex = inputImg.getAxisIndex(Axes.CHANNEL);
		int numInputDims = inputImg.numDimensions();
		long[] minChanPt = new long[numInputDims];
		long[] maxChanPt = new long[numInputDims];
		if (chIndex >= 0) maxChanPt[chIndex] = inputImg.dimension(chIndex) - 1;
		PointSet channelColumn = new HyperVolumePointSet(minChanPt, maxChanPt);
		
		// determine data space without channels
		long[] minDimPt = new long[numInputDims];
		long[] maxDimPt = new long[numInputDims];
		for (int i = 0; i < numInputDims; i++) {
			if (i != chIndex) maxDimPt[i] = inputImg.dimension(i)-1;
		}
		PointSet allOtherDims = new HyperVolumePointSet(minDimPt, maxDimPt);
		
		// setup neighborhood iterator
		PointSetInputIteratorFactory inputFactory =
				new PointSetInputIteratorFactory(channelColumn);
		InputIterator<PointSet> iter =
				inputFactory.createInputIterator(allOtherDims);

		// build averaging function
		@SuppressWarnings("unchecked")
		Function<long[], DoubleType> valFunc =
				new RealImageFunction<I, DoubleType>(
						(ImgPlus<I>)inputImg, new DoubleType());
		Function<PointSet, DoubleType> avgFunc =
				new RealArithmeticMeanFunction<DoubleType>(valFunc);
		
		// do the iteration and copy the data
		RandomAccess<? extends RealType<?>> outputAccessor =
				outputImg.randomAccess();
		PointSet inputPointset = null;
		long[] outputPt = new long[outputImg.numDimensions()];
		DoubleType avg = new DoubleType();
		while (iter.hasNext()) {
			inputPointset = iter.next(inputPointset);
			avgFunc.compute(inputPointset, avg);
			computeOutputPoint(chIndex, inputPointset.getAnchor(), outputPt);
			outputAccessor.setPosition(outputPt);
			outputAccessor.get().setReal(avg.getRealDouble());
		}
		
		// return the result
		return new ImgPlus<O>(outputImg, name, axes, cal);
	}
	
	/** determines the axes of the output image ignoring channels if necessary */
	private static AxisType[] outputAxes(ImgPlus<?> inputImg) {
		int inputAxisCount = inputImg.numDimensions();
		int chanIndex = inputImg.getAxisIndex(Axes.CHANNEL);
		int outputAxisCount = (chanIndex < 0) ? inputAxisCount : inputAxisCount - 1;
		AxisType[] inputAxes = new AxisType[inputAxisCount];
		inputImg.axes(inputAxes);
		AxisType[] outputAxes = new AxisType[outputAxisCount];
		int o = 0;
		for (int i = 0; i < inputAxisCount; i++) {
			if (i != chanIndex) outputAxes[o++] = inputAxes[i];
		}
		return outputAxes;
	}

	/** determines the dims of the output image ignoring channels if necessary */
	private static long[] outputDims(ImgPlus<?> inputImg) {
		int inputDimCount = inputImg.numDimensions();
		int chanIndex = inputImg.getAxisIndex(Axes.CHANNEL);
		int outputDimCount = (chanIndex < 0) ? inputDimCount : inputDimCount - 1;
		long[] inputDims = new long[inputDimCount];
		inputImg.dimensions(inputDims);
		long[] outputDims = new long[outputDimCount];
		int o = 0;
		for (int i = 0; i < inputDimCount; i++) {
			if (i != chanIndex) outputDims[o++] = inputDims[i];
		}
		return outputDims;
	}

	/** determines the calibration of the output image ignoring channels if
	 * necessary */
	private static double[] outputCalibration(ImgPlus<?> inputImg) {
		int inputDimCount = inputImg.numDimensions();
		int chanIndex = inputImg.getAxisIndex(Axes.CHANNEL);
		int outputDimCount = (chanIndex < 0) ? inputDimCount : inputDimCount - 1;
		double[] inputCal = new double[inputDimCount];
		inputImg.calibration(inputCal);
		double[] outputCal = new double[outputDimCount];
		int o = 0;
		for (int i = 0; i < inputDimCount; i++) {
			if (i != chanIndex) outputCal[o++] = inputCal[i];
		}
		return outputCal;
	}
	
	/** computes an output point from an input point ignoring the channel axis
	 * if necessary. */
	private static void computeOutputPoint(
		int chIndex, long[] inputPt, long[] outputPt)
	{
		int p = 0;
		for (int i = 0; i < inputPt.length; i++) {
			if (i != chIndex) outputPt[p++] = inputPt[i];
		}
	}

}
