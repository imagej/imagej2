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

package imagej.core.plugins.restructure;

import imagej.data.Dataset;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Map;

import net.imglib2.RandomAccess;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.RegionIndexIterator;
import net.imglib2.type.numeric.RealType;

// TODO
// - can reorder X & Y out of 1st two positions. This could be useful in future
//     but might need to block right now.

// TODO: add callbacks as appropriate to keep input valid

/**
 * Changes the internal ImgPlus of a Dataset so that its data values stay the
 * same but the order of the axes is changed.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Reorder Axes...") },
	headless = true, initializer = "initAxes")
public class ReorderAxes extends DynamicPlugin {

	// -- Parameters --

	@Parameter(persist = false)
	private Dataset dataset;

	// -- Fields --

	private String[] axisNames;
	private int[] permutationAxisIndices;
	private AxisType[] desiredAxisOrder;

	// -- ReorderAxes methods --

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	// -- Runnable methods --

	/** Runs the plugin and reorders axes as specified by user. */
	@Override
	public void run() {
		if (dataset == null) return;
		getAxisNamesInOrder();
		setupDesiredAxisOrder();
		if (inputBad()) return;
		setupPermutationVars();
		final ImgPlus<? extends RealType<?>> newImgPlus = getReorganizedData();
		// reportDims(dataset.getImgPlus());
		// reportDims(newImgPlus);
		RestructureUtils.allocateColorTables(newImgPlus);
		final ColorTableRemapper remapper =
			new ColorTableRemapper(new RemapAlgorithm());
		remapper.remapColorTables(dataset.getImgPlus(), newImgPlus);
		final int count = dataset.getCompositeChannelCount();
		dataset.setImgPlus(newImgPlus);
		dataset.setCompositeChannelCount(count);
	}

	// -- Initializers --

	protected void initAxes() {
		final AxisType[] axes = dataset.getAxes();

		final ArrayList<String> choices = new ArrayList<String>();
		for (int i = 0; i < axes.length; i++) {
			choices.add(axes[i].getLabel());
		}
		for (int i = 0; i < axes.length; i++) {
			final DefaultModuleItem<String> axisItem =
				new DefaultModuleItem<String>(this, name(i), String.class);
			axisItem.setChoices(choices);
			axisItem.setPersisted(false);
			axisItem.setValue(this, axes[i].getLabel());
			addInput(axisItem);
		}
	}

	// -- Helper methods --

	private String name(final int i) {
		return "Axis #" + i;
	}

	private void getAxisNamesInOrder() {
		final Map<String, Object> inputs = getInputs();
		axisNames = new String[dataset.getImgPlus().numDimensions()];
		for (int i = 0; i < axisNames.length; i++)
			axisNames[i] = (String) inputs.get(name(i));
	}

	/**
	 * Fills the internal variable "desiredAxisOrder" with the order of axes that
	 * the user specified in the dialog. all axes are present rather than just
	 * those present in the input Dataset.
	 */
	private void setupDesiredAxisOrder() {
		desiredAxisOrder = new AxisType[axisNames.length];
		for (int i = 0; i < axisNames.length; i++)
			desiredAxisOrder[i] = Axes.get(axisNames[i]);
	}

	/**
	 * Returns true if user input is invalid. Basically this is a test that the
	 * user did not repeat any axis when specifying the axis ordering.
	 */
	private boolean inputBad() {
		for (int i = 0; i < desiredAxisOrder.length; i++)
			for (int j = i + 1; j < desiredAxisOrder.length; j++)
				if (desiredAxisOrder[i] == desiredAxisOrder[j]) {
					Log.error("at least one axis preference is repeated:"
						+ " axis preferences must be mututally exclusive");
					return true;
				}
		return false;
	}

	/**
	 * Takes a given set of axes (usually a subset of all possible axes) and
	 * returns a permuted set of axes that reflect the user specified axis order
	 */
	private AxisType[] getPermutedAxes(final AxisType[] currAxes) {
		final AxisType[] permuted = new AxisType[currAxes.length];
		int index = 0;
		for (int i = 0; i < desiredAxisOrder.length; i++)
			for (int j = 0; j < currAxes.length; j++) {
				if (currAxes[j] == desiredAxisOrder[i]) {
					permuted[index++] = currAxes[j];
					break;
				}
			}
		return permuted;
	}

	/**
	 * Sets up the working variable "permutationAxisIndices" which is used to
	 * actually permute positions.
	 */
	private void setupPermutationVars() {
		final AxisType[] currAxes = dataset.getAxes();
		final AxisType[] permutedAxes = getPermutedAxes(currAxes);
		permutationAxisIndices = new int[currAxes.length];
		for (int i = 0; i < currAxes.length; i++) {
			final AxisType axis = currAxes[i];
			final int newIndex = getNewAxisIndex(permutedAxes, axis);
			permutationAxisIndices[i] = newIndex;
		}
	}

	/**
	 * Returns an ImgPlus that has same data values as the input Dataset but which
	 * has them stored in a different axis order
	 */
	private ImgPlus<? extends RealType<?>> getReorganizedData() {
		final RandomAccess<? extends RealType<?>> inputAccessor =
			dataset.getImgPlus().randomAccess();
		final long[] inputOrigin = new long[dataset.getImgPlus().numDimensions()];
		final long[] inputOffsets = new long[inputOrigin.length];
		dataset.getImgPlus().dimensions(inputOffsets);
		for (int i = 0; i < inputOffsets.length; i++)
			inputOffsets[i]--;
		final RegionIndexIterator iter =
			new RegionIndexIterator(inputOrigin, new long[inputOrigin.length],
				inputOffsets);
		final long[] origDims = dataset.getDims();
		final AxisType[] origAxes = dataset.getAxes();
		final long[] newDims = getNewDims(origDims);
		final AxisType[] newAxes = getNewAxes(origAxes);
		final ImgPlus<? extends RealType<?>> newImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDims, newAxes);
		newImgPlus.setCompositeChannelCount(dataset.getCompositeChannelCount());
		final RandomAccess<? extends RealType<?>> outputAccessor =
			newImgPlus.randomAccess();
		final long[] permutedPos = new long[inputOrigin.length];
		long[] currPos;
		while (iter.hasNext()) {
			iter.fwd();
			currPos = iter.getPosition();
			inputAccessor.setPosition(currPos);
			final double value = inputAccessor.get().getRealDouble();
			permute(currPos, permutedPos);
			outputAccessor.setPosition(permutedPos);
			outputAccessor.get().setReal(value);
		}
		return newImgPlus;
	}

	/**
	 * Returns the axis index of an Axis given a permuted set of axes.
	 */
	private int getNewAxisIndex(final AxisType[] permutedAxes,
		final AxisType originalAxis)
	{
		for (int i = 0; i < permutedAxes.length; i++) {
			if (permutedAxes[i] == originalAxis) return i;
		}
		throw new IllegalArgumentException("axis not found!");
	}

	/**
	 * Taking the original dims this method returns the new dimensions of the
	 * permuted space.
	 */
	private long[] getNewDims(final long[] origDims) {
		final long[] newDims = new long[origDims.length];
		permute(origDims, newDims);
		return newDims;
	}

	/**
	 * Taking the original axes order this method returns the new axes in the
	 * order of the permuted space.
	 */
	private AxisType[] getNewAxes(final AxisType[] origAxes) {
		final AxisType[] newAxes = new AxisType[origAxes.length];
		permute(origAxes, newAxes);
		return newAxes;
	}

	/**
	 * Permutes from a position in the original space into a position in the
	 * permuted space
	 */
	private void permute(final long[] origPos, final long[] permutedPos) {
		for (int i = 0; i < origPos.length; i++)
			permutedPos[permutationAxisIndices[i]] = origPos[i];
	}

	/**
	 * Permutes from an axis order in the original space into an axis order in the
	 * permuted space
	 */
	private void
		permute(final AxisType[] origAxes, final AxisType[] permutedAxes)
	{
		for (int i = 0; i < origAxes.length; i++)
			permutedAxes[permutationAxisIndices[i]] = origAxes[i];
	}

	private class RemapAlgorithm implements ColorTableRemapper.RemapAlgorithm {

		private final long[] inputPos =
			new long[ReorderAxes.this.permutationAxisIndices.length];
		private final long[] outputPos =
			new long[ReorderAxes.this.permutationAxisIndices.length];

		@Override
		public boolean isValidSourcePlane(final long i) {
			return true;
		}

		@Override
		public void remapPlanePosition(final long[] origPlaneDims,
			final long[] origPlanePos, final long[] newPlanePos)
		{
			inputPos[0] = 0;
			inputPos[1] = 0;
			for (int i = 0; i < origPlanePos.length; i++)
				inputPos[2 + i] = origPlanePos[i];

			permute(inputPos, outputPos);

			for (int i = 0; i < newPlanePos.length; i++)
				newPlanePos[i] = outputPos[i + 2];
		}

	}
}
