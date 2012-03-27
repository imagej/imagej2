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
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
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
 * Although ImgLib does not do any range clamping of values we will do so here.
 * </p>
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public abstract class TypeChanger implements ImageJPlugin {

	@Parameter(persist = false)
	private MenuService menuService;

	@Parameter(persist = false)
	protected Dataset input;

	protected <T extends RealType<T> & NativeType<T>> void changeType(
		final T newType)
	{
		changeType(input, newType);
		menuService.setSelected(this, true);
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
			final boolean wasRGBMerged = dataset.isRGBMerged();
			dataset.setImgPlus(copyToType(inputImg, newType));
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
	public static <T extends RealType<T> & NativeType<T>>
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

}
