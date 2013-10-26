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

package imagej.core.commands.assign.noisereduce;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.menu.MenuConstants;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealAdaptiveMedianFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Reduces noise in an image by applying an adaptive median algorithm.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Noise", mnemonic = 'n'),
	@Menu(label = "Noise Reduction", mnemonic = 'r'),
	@Menu(label = "Adaptive Median") })
public class NoiseReductionAdaptiveMedian<U extends RealType<U>> extends
	ContextCommand
{

	// -- Parameters --

	@Parameter
	private Context context;

	@Parameter
	private Dataset input;

	@Parameter(label = "Neighborhood: negative width", min = "0")
	private int windowNegWidthSpan = 1;

	@Parameter(label = "Neighborhood: negative height", min = "0")
	private int windowNegHeightSpan = 1;

	@Parameter(label = "Neighborhood: positive width", min = "0")
	private int windowPosWidthSpan = 1;

	@Parameter(label = "Neighborhood: positive height", min = "0")
	private int windowPosHeightSpan = 1;

	@Parameter(label = "Number of expansions", min = "1")
	private int windowExpansions = 1;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	// -- NoiseReductionAdaptiveMedian methods --

	@Override
	public void run() {
		@SuppressWarnings("unchecked")
		final ImgPlus<U> inputImg = (ImgPlus<U>) input.getImgPlus();
		final OutOfBoundsMirrorFactory<U, RandomAccessibleInterval<U>> oobFactory =
			new OutOfBoundsMirrorFactory<U, RandomAccessibleInterval<U>>(
				Boundary.DOUBLE);
		final Function<long[], DoubleType> otherFunc =
			new RealImageFunction<U, DoubleType>(inputImg, oobFactory,
				new DoubleType());
		final List<PointSet> pointSets = getNeighborhoods(input.numDimensions());
		final Reducer<U, DoubleType> reducer =
			new Reducer<U, DoubleType>(context, inputImg, getFunction(otherFunc,
				pointSets), pointSets.get(0));
		output = reducer.reduceNoise("Adaptive window neighborhood");
	}

	// -- private helpers --

	private Function<PointSet, DoubleType> getFunction(
		final Function<long[], DoubleType> otherFunc, final List<PointSet> neighs)
	{
		return new RealAdaptiveMedianFunction<DoubleType>(otherFunc, neighs);
	}

	private List<PointSet> getNeighborhoods(final int numDims) {
		final ArrayList<PointSet> pointSets = new ArrayList<PointSet>();
		for (int i = 0; i < windowExpansions; i++) {
			final PointSet rect =
				new HyperVolumePointSet(new long[numDims], offsets(windowNegWidthSpan +
					i, windowNegHeightSpan + i, numDims), offsets(windowPosWidthSpan + i,
					windowPosHeightSpan + i, numDims));
			pointSets.add(rect);
		}
		return pointSets;
	}

	private long[]
		offsets(final int xOffset, final int yOffset, final int numDims)
	{
		final long[] offsets = new long[numDims];
		offsets[0] = xOffset;
		offsets[1] = yOffset;
		return offsets;
	}

}
