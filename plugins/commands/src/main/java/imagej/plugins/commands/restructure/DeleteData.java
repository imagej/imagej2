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

package imagej.plugins.commands.restructure;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;

import java.util.ArrayList;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.meta.IntervalUtils;
import net.imglib2.meta.SpaceUtils;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Deletes hyperplanes of data from an input Dataset along a user specified
 * axis.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL,
		weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Data", mnemonic = 'd'), @Menu(label = "Delete Data...") },
	headless = true, initializer = "initAll")
public class DeleteData extends DynamicCommand {

	// -- Constants --

	private static final String AXIS_NAME = "axisName";
	private static final String POSITION = "position";
	private static final String QUANTITY = "quantity";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter(label = "Axis to modify", persist = false,
		callback = "parameterChanged")
	private String axisName;

	@Parameter(label = "Deletion position", persist = false,
		callback = "parameterChanged")
	private long position = 1;

	@Parameter(label = "Deletion quantity", persist = false,
		callback = "parameterChanged")
	private long quantity = 1;

	// -- DeleteData methods --

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	public String getAxisName() {
		return axisName;
	}

	public void setAxisName(final String axisName) {
		this.axisName = axisName;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(final long position) {
		this.position = position;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(final long quantity) {
		this.quantity = quantity;
	}

	// -- Runnable methods --

	/**
	 * Creates new ImgPlus data copying pixel values as needed from an input
	 * Dataset. Assigns the ImgPlus to the input Dataset.
	 */
	@Override
	public void run() {
		final AxisType axis = Axes.get(axisName);
		if (inputBad(axis)) return;
		final AxisType[] axes = SpaceUtils.getAxisTypes(dataset);
		final long[] newDimensions =
			RestructureUtils.getDimensions(dataset, axis, -quantity);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, axes);
		final int compositeChannelCount =
			compositeStatus(dataset.getCompositeChannelCount(), dstImgPlus, axis);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus, axis);
		dstImgPlus.setCompositeChannelCount(compositeChannelCount);
		RestructureUtils.allocateColorTables(dstImgPlus);
		if (axis.isXY()) {
			RestructureUtils.copyColorTables(dataset.getImgPlus(), dstImgPlus);
		}
		else {
			int d = dataset.dimensionIndex(axis);
			final ColorTableRemapper remapper =
				new ColorTableRemapper(new RemapAlgorithm(IntervalUtils
					.getDims(dataset), d));
			remapper.remapColorTables(dataset.getImgPlus(), dstImgPlus);
		}
		// TODO - metadata, etc.?
		dataset.setImgPlus(dstImgPlus);
	}

	// -- Initializers --

	protected void initAll() {
		initAxisName();
		initPosition();
		initQuantity();
	}

	// -- Callbacks --

	protected void parameterChanged() {
		setPositionRange();
		setQuantityRange();
		clampPosition();
		clampQuantity();
	}

	// -- Helper methods --

	/** Detects if user-specified data is invalid. */
	private boolean inputBad(final AxisType axis) {
		// axis not determined by dialog
		if (axis == null) {
			cancel("Axis must not be null.");
			return true;
		}

		// setup some working variables
		final int axisIndex = dataset.dimensionIndex(axis);
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);

		// axis not present in Dataset
		if (axisIndex < 0) {
			cancel("Axis " + axis.getLabel() + " is not present in input dataset.");
			return true;
		}

		// bad value for startPosition
		if (position < 1 || position > axisSize) {
			cancel("Start position is out of bounds.");
			return true;
		}

		// bad value for numDeleting
		if (quantity <= 0) {
			cancel("Quantity to delete must be a positive number.");
			return true;
		}

		// trying to delete all hyperplanes along axis
		if (quantity >= axisSize) {
			cancel("Cannot delete all entries along axis " + axis.getLabel());
			return true;
		}

		// if here everything is okay
		return false;
	}

	/**
	 * Fills the newly created ImgPlus with data values from a larger source
	 * image. Copies data from those hyperplanes not being cut.
	 */
	private void fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus,
		final AxisType modifiedAxis)
	{
		final int axisIndex = dataset.dimensionIndex(modifiedAxis);
		final long axisSize = dataset.dimension(axisIndex);
		final long numBeforeCut = position - 1; // one based position
		long numInCut = quantity;
		if (numBeforeCut + numInCut > axisSize) numInCut = axisSize - numBeforeCut;
		final long numAfterCut = axisSize - (numBeforeCut + numInCut);

		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis, 0, 0,
			numBeforeCut);
		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			numBeforeCut + numInCut, numBeforeCut, numAfterCut);
	}

	private int compositeStatus(final int compositeCount,
		final ImgPlus<?> output, final AxisType axis)
	{
		if (axis == Axes.CHANNEL) {
			final int axisIndex = output.dimensionIndex(Axes.CHANNEL);
			final long numChannels = output.dimension(axisIndex);
			if (numChannels < compositeCount) return (int) numChannels;
		}
		return compositeCount;
	}

	@SuppressWarnings("synthetic-access")
	private class RemapAlgorithm implements ColorTableRemapper.RemapAlgorithm {

		private final long[] origPlaneDims;
		private final long[] srcPlanePos;
		private final int axisIndex;

		public RemapAlgorithm(final long[] origDims, int axisIndex) {
			this.origPlaneDims = new long[origDims.length - 2];
			for (int i = 0; i < origPlaneDims.length; i++) {
				origPlaneDims[i] = origDims[i + 2];
			}
			this.srcPlanePos = new long[origPlaneDims.length];
			this.axisIndex = axisIndex;
		}

		@Override
		public boolean isValidSourcePlane(final long i) {
			ColorTableRemapper.toND(origPlaneDims, i, srcPlanePos);
			if (srcPlanePos[axisIndex - 2] < position - 1) return true;
			if (srcPlanePos[axisIndex - 2] >= position - 1 + quantity) return true;
			return false;
		}

		@Override
		public void remapPlanePosition(final long[] origPlaneDims,
			final long[] origPlanePos, final long[] newPlanePos)
		{
			for (int i = 0; i < origPlanePos.length; i++) {
				if (i != axisIndex - 2) {
					newPlanePos[i] = origPlanePos[i];
				}
				else {
					if (origPlanePos[i] < position - 1) {
						newPlanePos[i] = origPlanePos[i];
					}
					else if (origPlanePos[i] >= position - 1 + quantity) {
						newPlanePos[i] = origPlanePos[i] - quantity;
					}
					else {
						// System.out.println("orig dims = "+Arrays.toString(origPlaneDims));
						// System.out.println("orig pos  = "+Arrays.toString(origPlanePos));
						// System.out.println("pos       = "+position);
						// System.out.println("quantity  = "+quantity);
						System.out.println("val = " + origPlanePos[i] + " pos-1 = " +
							(position - 1) + " quantity = " + quantity);
						throw new IllegalArgumentException(
							"position remap should not be happening here!");
					}
				}
			}
		}
	}

	private void initAxisName() {
		final MutableModuleItem<String> axisNameItem =
			getInfo().getMutableInput(AXIS_NAME, String.class);
		Dataset ds = getDataset();
		final ArrayList<String> choices = new ArrayList<String>();
		for (int i = 0; i < ds.numDimensions(); i++) {
			AxisType axisType = ds.axis(i).type();
			choices.add(axisType.getLabel());
		}
		axisNameItem.setChoices(choices);
	}

	private void initPosition() {
		final long max = getDataset().getImgPlus().dimension(0);
		setItemRange(POSITION, 1, max);
		setPosition(1);
	}

	private void initQuantity() {
		final long max = getDataset().getImgPlus().dimension(0);
		setItemRange(QUANTITY, 1, max);
		setQuantity(1);
	}

	private void setPositionRange() {
		final long max = currDimLen();
		setItemRange(POSITION, 1, max);
	}

	private void setQuantityRange() {
		final long max = currDimLen() - getPosition() + 1;
		setItemRange(QUANTITY, 1, max);
	}

	private void clampPosition() {
		final long max = currDimLen();
		final long pos = getPosition();
		if (pos < 1) setPosition(1);
		else if (pos > max) setPosition(max);
	}

	private void clampQuantity() {
		final long max = currDimLen() - getPosition() + 1;
		final long total = getQuantity();
		if (total < 1) setQuantity(1);
		else if (total > max) setQuantity(max);
	}

	private long currDimLen() {
		final AxisType axis = Axes.get(getAxisName());
		final int axisIndex = getDataset().dimensionIndex(axis);
		return getDataset().getImgPlus().dimension(axisIndex);
	}

	private void setItemRange(final String fieldName, final long min,
		@SuppressWarnings("unused") final long max)
	{
		final MutableModuleItem<Long> item =
			getInfo().getMutableInput(fieldName, Long.class);
		item.setMinimumValue(min);
		// TODO - disable until we fix ticket #886
		// item.setMaximumValue(max);
	}

}
