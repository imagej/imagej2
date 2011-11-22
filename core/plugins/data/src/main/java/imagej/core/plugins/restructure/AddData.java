//
// AddData.java
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
 * Adds hyperplanes of data to an input Dataset along a user specified axis.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Add Data...") })
public class AddData extends DynamicPlugin {

	// -- constants --
	
	private static final String DATASET = "dataset";
	private static final String AXIS_NAME = "axisToModify";
	private static final String POSITION = "oneBasedInsPos";
	private static final String QUANTITY = "numAdding";

	// -- instance variables --
	
	@Parameter(required = true, persist = false)
	private Dataset dataset;
	
	@Parameter(label = "Axis to modify", persist = false,
			initializer = "initAll", callback = "somethingChanged")
	private String axisToModify;
	
	@Parameter(label = "Insertion position", persist = false,
			callback = "somethingChanged")
	private long oneBasedInsPos;
	
	@Parameter(label = "Insertion quantity", persist = false,
			callback = "somethingChanged")
	private long numAdding;

	private long insertPosition;

	// -- public interface --

	/**
	 * Creates new ImgPlus data copying pixel values as needed from an input
	 * Dataset. Assigns the ImgPlus to the input Dataset.
	 */
	@Override
	public void run() {
		dataset = getDataset();
		axisToModify = getAxisName();
		oneBasedInsPos = getPosition();
		numAdding = getQuantity();
		insertPosition = oneBasedInsPos - 1;
		final AxisType axis = Axes.get(axisToModify);
		if (inputBad(axis)) return;
		final AxisType[] axes = dataset.getAxes();
		final long[] newDimensions =
			RestructureUtils.getDimensions(dataset, axis, numAdding);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, axes);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus, axis);
		final int compositeChannelCount =
			compositeStatus(dataset, dstImgPlus, axis);
		dstImgPlus.setCompositeChannelCount(compositeChannelCount);
		// TODO - colorTables, metadata, etc.?
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
	private boolean inputBad(final AxisType axis) {
		// axis not determined by dialog
		if (axis == null) return true;

		// setup some working variables
		final int axisIndex = dataset.getAxisIndex(axis);
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);

		// axis not present in Dataset
		if (axisIndex < 0) return true;

		// bad value for startPosition
		if ((insertPosition < 0) || (insertPosition > axisSize)) return true;

		// bad value for numAdding
		if ((numAdding <= 0) || ((Long.MAX_VALUE - numAdding) < axisSize)) return true;

		// if here everything is okay
		return false;
	}

	/**
	 * Fills the newly created ImgPlus with data values from a smaller source
	 * image. Copies data from existing hyperplanes.
	 */
	private void fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus, final AxisType modifiedAxis)
	{
		final long[] dimensions = dataset.getDims();
		final int axisIndex = dataset.getAxisIndex(modifiedAxis);
		final long axisSize = dimensions[axisIndex];
		final long numBeforeInsert = insertPosition;
		final long numInInsertion = numAdding;
		final long numAfterInsertion = axisSize - numBeforeInsert;

		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis, 0, 0,
			numBeforeInsert);
		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			numBeforeInsert, numBeforeInsert + numInInsertion, numAfterInsertion);
	}

	private int compositeStatus(final Dataset origData,
		final ImgPlus<?> dstImgPlus, final AxisType axis)
	{

		// adding along non-channel axis
		if (axis != Axes.CHANNEL) {
			return origData.getCompositeChannelCount();
		}

		// else adding hyperplanes along channel axis

		// calc working data
		final int currComposCount = dataset.getCompositeChannelCount();
		final int origAxisPos = origData.getAxisIndex(Axes.CHANNEL);
		final long numOrigChannels = origData.getImgPlus().dimension(origAxisPos);
		final long numNewChannels = dstImgPlus.dimension(origAxisPos);

		// was "composite" on 1 channel
		if (currComposCount == 1) {
			return 1;
		}

		// was composite on all channels
		if (numOrigChannels == currComposCount) {
			return (int) numNewChannels; // in future be composite on all channels
		}

		// was composite on a subset of channels that divides channels evenly
		if (((numOrigChannels % currComposCount) == 0) &&
			((numNewChannels % currComposCount) == 0))
		{
			return currComposCount;
		}

		// cannot figure out a good count - no longer composite
		return 1;
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
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> quantityItem =
			(DefaultModuleItem<Long>) getInfo().getInput(QUANTITY);
		quantityItem.setMinimumValue(1L);
		setQuantity(1);
	}

	private void initPositionRange() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> positionItem =
			(DefaultModuleItem<Long>) getInfo().getInput(POSITION);
		long dimLen = currDimLen();
		positionItem.setMinimumValue(1L);
		positionItem.setMaximumValue(dimLen+1);
	}
	
	private void initQuantityRange() {
		long max = Long.MAX_VALUE;
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> quantityItem =
			(DefaultModuleItem<Long>) getInfo().getInput(QUANTITY);
		quantityItem.setMinimumValue(1L);
		quantityItem.setMaximumValue(max-getPosition()+1);
	}
	
	private void clampPosition() {
		long max = currDimLen() + 1;
		long pos = getPosition();
		if (pos < 1) pos = 1;
		if (pos > max) pos = max;
		setPosition(pos);
	}
	
	private void clampQuantity() {
		long max = Long.MAX_VALUE - getPosition() + 1;
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
