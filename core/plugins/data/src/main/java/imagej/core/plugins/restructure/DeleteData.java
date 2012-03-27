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
import imagej.ui.UIService;

import java.util.ArrayList;

import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

/**
 * Deletes hyperplanes of data from an input Dataset along a user specified
 * axis.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Delete Data...") },
	headless = true, initializer = "initAll")
public class DeleteData extends DynamicPlugin {

	// -- Constants --

	private static final String AXIS_NAME = "axisName";
	private static final String POSITION = "position";
	private static final String QUANTITY = "quantity";

	// -- Parameters --

	@Parameter(persist = false)
	private UIService uiService;

	@Parameter(persist = false)
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
		if (inputBad(axis)) {
			informUser();
			return;
		}
		final AxisType[] axes = dataset.getAxes();
		final long[] newDimensions =
			RestructureUtils.getDimensions(dataset, axis, -quantity);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, axes);
		final int compositeChannelCount =
			compositeStatus(dataset.getCompositeChannelCount(), dstImgPlus, axis);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus, axis);
		dstImgPlus.setCompositeChannelCount(compositeChannelCount);
		RestructureUtils.allocateColorTables(dstImgPlus);
		if (Axes.isXY(axis)) {
			RestructureUtils.copyColorTables(dataset.getImgPlus(), dstImgPlus);
		}
		else {
			final ColorTableRemapper remapper =
				new ColorTableRemapper(new RemapAlgorithm());
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
		if (axis == null) return true;

		// setup some working variables
		final int axisIndex = dataset.getAxisIndex(axis);
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);

		// axis not present in Dataset
		if (axisIndex < 0) return true;

		// bad value for startPosition
		if (position < 1 || position > axisSize) return true;

		// bad value for numDeleting
		if (quantity <= 0) return true;

		// trying to delete all hyperplanes along axis
		if (quantity >= axisSize) return true;

		// if here everything is okay
		return false;
	}

	/**
	 * Fills the newly created ImgPlus with data values from a larger source
	 * image. Copies data from those hyperplanes not being cut.
	 */
	private void
		fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
			final ImgPlus<? extends RealType<?>> dstImgPlus,
			final AxisType modifiedAxis)
	{
		final long[] dimensions = dataset.getDims();
		final int axisIndex = dataset.getAxisIndex(modifiedAxis);
		final long axisSize = dimensions[axisIndex];
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
			final int axisIndex = output.getAxisIndex(Axes.CHANNEL);
			final long numChannels = output.dimension(axisIndex);
			if (numChannels < compositeCount) return (int) numChannels;
		}
		return compositeCount;
	}

	private class RemapAlgorithm implements ColorTableRemapper.RemapAlgorithm {

		@Override
		public boolean isValidSourcePlane(final long i) {
			if (i < position - 1) return true;
			if (i >= position - 1 + quantity) return true;
			return false;
		}

		@Override
		public void remapPlanePosition(final long[] origPlaneDims,
			final long[] origPlanePos, final long[] newPlanePos)
		{
			final AxisType axis = Axes.get(axisName);
			final int axisIndex = dataset.getAxisIndex(axis);
			for (int i = 0; i < origPlanePos.length; i++) {
				if (i != axisIndex - 2) {
					newPlanePos[i] = origPlanePos[i];
				}
				else {
					if (origPlanePos[i] < position - 1) newPlanePos[i] = origPlanePos[i];
					else if (origPlanePos[i] >= position - 1 + quantity) newPlanePos[i] =
						origPlanePos[i] - quantity;
					else {
						// System.out.println("orig dims = "+Arrays.toString(origPlaneDims));
						// System.out.println("orig pos  = "+Arrays.toString(origPlanePos));
						// System.out.println("pos       = "+position);
						// System.out.println("quantity  = "+quantity);
						throw new IllegalArgumentException(
							"position remap should not be happening here!");
					}
				}
			}
		}
	}

	private void initAxisName() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> axisNameItem =
			(DefaultModuleItem<String>) getInfo().getInput(AXIS_NAME);
		final AxisType[] axes = getDataset().getAxes();
		final ArrayList<String> choices = new ArrayList<String>();
		for (final AxisType a : axes) {
			choices.add(a.getLabel());
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
		final int axisIndex = getDataset().getAxisIndex(axis);
		return getDataset().getImgPlus().dimension(axisIndex);
	}

	private void setItemRange(final String fieldName, final long min,
		final long max)
	{
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> item =
			(DefaultModuleItem<Long>) getInfo().getInput(fieldName);
		item.setMinimumValue(min);
		// TODO - disable until we fix ticket #886
		// item.setMaximumValue(max);
	}

	private void informUser() {
		uiService.showDialog("Data unchanged: bad combination of input parameters",
			"Invalid parameter combination");
	}

}
