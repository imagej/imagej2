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

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
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

	// -- constants --
	
	private static final String DATASET = "dataset";
	private static final String AXIS_NAME = "axisToModify";
	private static final String POSITION = "oneBasedDelPos";
	private static final String QUANTITY = "numDeleting";

	// -- instance variables --
	
	@Parameter(required = true, persist = false)
	private Dataset dataset;
	
	@Parameter(label = "Axis to modify", persist = false,
			initializer = "initAll", callback = "somethingChanged")
	private String axisToModify;
	
	@Parameter(label = "Deletion position", persist = false,
			callback = "somethingChanged")
	private long oneBasedDelPos;
	
	@Parameter(label = "Deletion quantity", persist = false,
			callback = "somethingChanged")
	private long numDeleting;

	private long deletePosition;

	// -- public interface --
	
	public DeleteData() {
	}

	/**
	 * Creates new ImgPlus data copying pixel values as needed from an input
	 * Dataset. Assigns the ImgPlus to the input Dataset.
	 */
	@Override
	public void run() {
		dataset = getDataset();
		axisToModify = getAxisName();
		oneBasedDelPos = getPosition();
		numDeleting = getQuantity();
		deletePosition = oneBasedDelPos - 1;
		final Axis axis = Axes.get(axisToModify);
		if (inputBad(axis)) return;
		final Axis[] axes = dataset.getAxes();
		final long[] newDimensions =
			RestructureUtils.getDimensions(dataset, axis, -numDeleting);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, axes);
		final int compositeChannelCount =
			compositeStatus(dataset.getCompositeChannelCount(), dstImgPlus, axis);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus, axis);
		// TODO - colorTables, metadata, etc.?
		dstImgPlus.setCompositeChannelCount(compositeChannelCount);
		dataset.setImgPlus(dstImgPlus);
	}

	// -- protected interface --

	protected void initAll() {
		initAxisName();
		initPosition();
		initQuantity();
	}

	protected void somethingChanged() {
		initPositionRange();
		initQuantityRange();
		clampPosition();
		clampQuantity();
	}

	// -- private helpers --
	
	/**
	 * Detects if user specified data is invalid
	 */
	private boolean inputBad(final Axis axis) {
		// axis not determined by dialog
		if (axis == null) return true;

		// setup some working variables
		final int axisIndex = dataset.getAxisIndex(axis);
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);

		// axis not present in Dataset
		if (axisIndex < 0) return true;

		// bad value for startPosition
		if ((deletePosition < 0) || (deletePosition >= axisSize)) return true;

		// bad value for numDeleting
		if (numDeleting <= 0) return true;

		// trying to delete all hyperplanes along axis
		if (numDeleting >= axisSize) return true;

		// if here everything is okay
		return false;
	}

	/**
	 * Fills the newly created ImgPlus with data values from a larger source
	 * image. Copies data from those hyperplanes not being cut.
	 */
	private void fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus, final Axis modifiedAxis)
	{
		final long[] dimensions = dataset.getDims();
		final int axisIndex = dataset.getAxisIndex(modifiedAxis);
		final long axisSize = dimensions[axisIndex];
		final long numBeforeCut = deletePosition;
		long numInCut = numDeleting;
		if (numBeforeCut + numInCut > axisSize) numInCut = axisSize - numBeforeCut;
		final long numAfterCut = axisSize - (numBeforeCut + numInCut);

		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis, 0, 0,
			numBeforeCut);
		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			numBeforeCut + numInCut, numBeforeCut, numAfterCut);
	}

	private int compositeStatus(final int compositeCount,
		final ImgPlus<?> output, final Axis axis)
	{
		if (axis == Axes.CHANNEL) {
			final int axisIndex = output.getAxisIndex(Axes.CHANNEL);
			final long numChannels = output.dimension(axisIndex);
			if (numChannels < compositeCount) return (int) numChannels;
		}
		return compositeCount;
	}
	
	private Dataset getDataset() {
		return (Dataset) getInput(DATASET);
	}
	
	private String getAxisName() {
		return (String) getInput(AXIS_NAME);
	}
	
	private long getPosition() {
		return (Long) getInput(POSITION);
	}
	
	private void setPosition(long pos) {
		setInput(POSITION, pos);
	}
	
	private long getQuantity() {
		return (Long) getInput(QUANTITY);
	}
	
	private void setQuantity(long val) {
		setInput(QUANTITY, val);
	}
	
	private void initAxisName() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> axisNameItem =
			(DefaultModuleItem<String>) getInfo().getInput(AXIS_NAME);
		final Axis[] axes = getDataset().getAxes();
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis a : axes) {
			choices.add(a.getLabel());
		}
		axisNameItem.setChoices(choices);
	}
	
	private void initPosition() {
		long max = getDataset().getImgPlus().dimension(0);
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> positionItem =
				(DefaultModuleItem<Long>) getInfo().getInput(POSITION);
		positionItem.setMinimumValue(1L);
		positionItem.setMaximumValue(max);
		setPosition(1);
	}
	
	private void initQuantity() {
		long max = getDataset().getImgPlus().dimension(0);
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> quantityItem =
			(DefaultModuleItem<Long>) getInfo().getInput(QUANTITY);
		quantityItem.setMinimumValue(1L);
		quantityItem.setMaximumValue(max);
		setQuantity(1);
	}

	private void initPositionRange() {
		long dimLen = currDimLen();
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> positionItem =
			(DefaultModuleItem<Long>) getInfo().getInput(POSITION);
		positionItem.setMinimumValue(1L);
		positionItem.setMaximumValue(dimLen);
	}

	private void initQuantityRange() {
		long max = currDimLen();
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> quantityItem =
			(DefaultModuleItem<Long>) getInfo().getInput(QUANTITY);
		quantityItem.setMinimumValue(1L);
		quantityItem.setMaximumValue(max-getPosition()+1);
	}
	
	private void clampPosition() {
		long max = currDimLen();
		long pos = getPosition();
		if (pos < 1) pos = 1;
		if (pos > max) pos = max;
		setPosition(pos);
	}
	
	private void clampQuantity() {
		long max = currDimLen() - getPosition() + 1;
		long total = getQuantity();
		if (total < 1) total = 1;
		if (total > max) total = max;
		setQuantity(total);
	}
	
	private long currDimLen() {
		Axis axis = Axes.get(getAxisName());
		int axisIndex = getDataset().getAxisIndex(axis);
		return getDataset().getImgPlus().dimension(axisIndex);
	}
}
