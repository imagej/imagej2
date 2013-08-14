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

package imagej.core.commands.assign;

import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.Overlay;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.ops.condition.Condition;
import net.imglib2.ops.condition.UVInsideRoiCondition;
import net.imglib2.ops.function.complex.ComplexImageFunction;
import net.imglib2.ops.function.general.GeneralUnaryFunction;
import net.imglib2.ops.img.ImageAssignment;
import net.imglib2.ops.input.InputIteratorFactory;
import net.imglib2.ops.input.PointInputIteratorFactory;
import net.imglib2.ops.operation.complex.unary.ComplexUnaryOperation;
import net.imglib2.type.numeric.ComplexType;

/**
 * Helper class for use by many plugins that apply a {@link
 * ComplexUnaryOperation} to some input image. The run() method modifies the
 * current selection of the active {@link Dataset} of the given {@link
 * ImageDisplay}. The given {@link ComplexUnaryOperation} is applied on a pixel
 * by pixel basis.
 * 
 * @author Barry DeZonia
 */
public class InplaceUnaryTransform<I extends ComplexType<I>, O extends ComplexType<O>> {

	// -- instance variables --

	private final ImageAssignment<I,O,long[]> assigner1;
	private final ImageAssignment<I,O,long[]> assigner2;
	private final ImageAssignment<I,O,long[]> assigner3;
	private final Dataset dataset;
	private long[] origin;
	private long[] span;
	private Condition<long[]> condition;

	// -- constructor --

	/** All planes version */
	public InplaceUnaryTransform(
			final ComplexUnaryOperation<O,O> operation,
			O outType,
			Dataset dataset,
			Overlay overlay)
	{
		this.dataset = dataset;
		setRegion(dataset, overlay);
		@SuppressWarnings("unchecked")
		final Img<I> img = (Img<I>)dataset.getImgPlus();
		final ComplexImageFunction<I,O> f1 =
				new ComplexImageFunction<I,O>(img, outType.createVariable());
		final GeneralUnaryFunction<long[],O,O> function = new
				GeneralUnaryFunction<long[],O,O>(
					f1, operation, outType.createVariable());
		final InputIteratorFactory<long[]> factory =
				new PointInputIteratorFactory();
		assigner1 =
			new ImageAssignment<I,O, long[]>(img, origin, span, function,
					condition, factory);
		assigner2 = null;
		assigner3 = null;
	}

	/** Single plane versions */
	public InplaceUnaryTransform(
			final ComplexUnaryOperation<O,O> operation,
			O outType,
			Dataset dataset,
			Overlay overlay,
			Position planePos)
	{
		this.dataset = dataset;
		setRegion(dataset, overlay, planePos);
		@SuppressWarnings("unchecked")
		final Img<I> img = (Img<I>)dataset.getImgPlus();
		final ComplexImageFunction<I,O> f1 =
				new ComplexImageFunction<I,O>(img, outType.createVariable());
		final GeneralUnaryFunction<long[],O,O> function = new
				GeneralUnaryFunction<long[],O,O>(
					f1, operation, outType.createVariable());
		final InputIteratorFactory<long[]> factory =
				new PointInputIteratorFactory();
		boolean rgb = dataset.isRGBMerged();
		int chIndex = dataset.dimensionIndex(Axes.CHANNEL);
		if (rgb) {
			origin[chIndex] = 0;
		}
		assigner1 =
			new ImageAssignment<I,O, long[]>(img, origin, span, function,
					condition, factory);
		if (rgb) {
			origin[chIndex] = 1;
			assigner2 =
				new ImageAssignment<I,O, long[]>(img, origin, span, function,
					condition, factory);
			origin[chIndex] = 2;
			assigner3 =
				new ImageAssignment<I,O, long[]>(img, origin, span, function,
					condition, factory);
		}
		else {
			assigner2 = null;
			assigner3 = null;
		}
	}

	
	// -- public interface --

	public void run() {
		if (assigner1 != null) assigner1.assign();
		if (assigner2 != null) assigner2.assign();
		if (assigner3 != null) assigner3.assign();
		dataset.update();
	}

	public long[] getRegionOrigin() { return origin; }
	
	public long[] getRegionSpan() { return span; }

	// -- private helpers --

	/** All planes version */
	private void setRegion(Dataset ds, Overlay overlay) {

		// check dimensions of Dataset
		final int xIndex = ds.dimensionIndex(Axes.X);
		final int yIndex = ds.dimensionIndex(Axes.Y);
		if ((xIndex < 0) || (yIndex < 0))
			throw new IllegalArgumentException(
				"display does not have XY planes");
		
		LongRect rect = findXYRegion(ds, overlay, xIndex, yIndex);
		
		// calc origin and span values
		long[] dims = ds.getDims();
		origin = new long[ds.numDimensions()];
		span = new long[ds.numDimensions()];
		for (int i = 0; i < ds.numDimensions(); i++) {
			if (i == xIndex) {
				origin[xIndex] = rect.x;
				span[xIndex] = rect.w;
			}
			else if (i == yIndex) {
				origin[yIndex] = rect.y;
				span[yIndex] = rect.h;
			}
			else {
				origin[i] = 0;
				span[i] = dims[i];
			}
		}
		
		condition = null;
		if (overlay != null)
			condition = new UVInsideRoiCondition(overlay.getRegionOfInterest());
	}

	/** Single plane version */
	private void setRegion(Dataset ds, Overlay overlay, Position planePos) {

		// check dimensions of Dataset
		final int xIndex = ds.dimensionIndex(Axes.X);
		final int yIndex = ds.dimensionIndex(Axes.Y);
		if ((xIndex < 0) || (yIndex < 0))
			throw new IllegalArgumentException(
				"display does not have XY planes");
		
		LongRect rect = findXYRegion(ds, overlay, xIndex, yIndex);
		
		// calc origin and span values
		origin = new long[ds.numDimensions()];
		span = new long[ds.numDimensions()];
		int p = 0;
		for (int i = 0; i < ds.numDimensions(); i++) {
			if (i == xIndex) {
				origin[xIndex] = rect.x;
				span[xIndex] = rect.w;
			}
			else if (i == yIndex) {
				origin[yIndex] = rect.y;
				span[yIndex] = rect.h;
			}
			else {
				origin[i] = planePos.getLongPosition(p++);
				span[i] = 1;
			}
		}
		
		condition = null;
		if (overlay != null)
			condition = new UVInsideRoiCondition(overlay.getRegionOfInterest());
	}

	private LongRect findXYRegion(Dataset ds, Overlay overlay, int xIndex, int yIndex) {

		// calc XY outline boundary
		final long[] dims = ds.getDims();
		final LongRect rect = new LongRect();
		if (overlay == null) {
			rect.x = 0;
			rect.y = 0;
			rect.w = dims[xIndex];
			rect.h = dims[yIndex];
		}
		else {
			rect.x = (long) overlay.realMin(0);
			rect.y = (long) overlay.realMin(1);
			rect.w = Math.round(overlay.realMax(0) - rect.x);
			rect.h = Math.round(overlay.realMax(1) - rect.y);
		}
		return rect;
	}
	
	private class LongRect {
		public long x, y, w, h;
	}
}
