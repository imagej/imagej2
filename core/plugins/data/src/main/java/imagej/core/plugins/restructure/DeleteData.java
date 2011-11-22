//
// DeleteData.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.core.plugins.restructure;

import imagej.data.Dataset;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

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
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Delete Data...") })
public class DeleteData extends DynamicPlugin {

	// -- Constants --

	private static final String AXIS_NAME = "axisToModify";
	private static final String POSITION = "oneBasedDelPos";
	private static final String QUANTITY = "numDeleting";

	// -- Parameters --

	@Parameter(required = true, persist = false)
	private Dataset dataset;

	@Parameter(label = "Axis to modify", persist = false,
		initializer = "initAll", callback = "parameterChanged")
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
		final AxisType[] axes = dataset.getAxes();
		final long[] newDimensions =
			RestructureUtils.getDimensions(dataset, axis, -quantity);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, axes);
		final int compositeChannelCount =
			compositeStatus(dataset.getCompositeChannelCount(), dstImgPlus, axis);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus, axis);
		// TODO - colorTables, metadata, etc.?
		dstImgPlus.setCompositeChannelCount(compositeChannelCount);
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
		initPositionRange();
		initQuantityRange();
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
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> positionItem =
			(DefaultModuleItem<Long>) getInfo().getInput(POSITION);
		positionItem.setMinimumValue(1L);
		positionItem.setMaximumValue(max);
	}

	private void initQuantity() {
		final long max = getDataset().getImgPlus().dimension(0);
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> quantityItem =
			(DefaultModuleItem<Long>) getInfo().getInput(QUANTITY);
		quantityItem.setMinimumValue(1L);
		quantityItem.setMaximumValue(max);
	}

	private void initPositionRange() {
		final long dimLen = currDimLen();
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> positionItem =
			(DefaultModuleItem<Long>) getInfo().getInput(POSITION);
		positionItem.setMinimumValue(1L);
		positionItem.setMaximumValue(dimLen);
	}

	private void initQuantityRange() {
		final long max = currDimLen();
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> quantityItem =
			(DefaultModuleItem<Long>) getInfo().getInput(QUANTITY);
		quantityItem.setMinimumValue(1L);
		quantityItem.setMaximumValue(max - getPosition() + 1);
	}

	private void clampPosition() {
		final long max = currDimLen();
		long pos = getPosition();
		if (pos < 1) pos = 1;
		if (pos > max) pos = max;
		setPosition(pos);
	}

	private void clampQuantity() {
		final long max = currDimLen() - getPosition() + 1;
		long total = getQuantity();
		if (total < 1) total = 1;
		if (total > max) total = max;
		setQuantity(total);
	}

	private long currDimLen() {
		final AxisType axis = Axes.get(getAxisName());
		final int axisIndex = getDataset().getAxisIndex(axis);
		return getDataset().getImgPlus().dimension(axisIndex);
	}

}
