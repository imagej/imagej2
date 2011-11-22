//
// DeleteAxis.java
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
 * Deletes an axis from an input Dataset.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Delete Axis...") })
public class DeleteAxis extends DynamicPlugin {

	// -- constants --
	
	private static final String DATASET = "dataset";
	private static final String AXIS_NAME = "axisName";
	private static final String POSITION = "planePos";
	
	// -- instance variables --
	
	@Parameter(required = true, persist = false)
	private Dataset dataset;

	@Parameter(label = "Axis to delete", persist = false,
			initializer = "initAll", callback = "axisChanged")
	private String axisName;
	
	@Parameter(label = "Index of hyperplane to keep", persist = false,
			callback = "axisChanged")
	private long planePos;

	// -- public interface --
	
	/**
	 * Creates new ImgPlus data with one less axis. sets pixels of ImgPlus to user
	 * specified hyperplane within original ImgPlus data. Assigns the new ImgPlus
	 * to the input Dataset.
	 */
	@Override
	public void run() {

		// I don't think this is needed and it could cause unexpected behavior
		//clampPlanePos();
		
		dataset = getDataset();
		axisName = getAxisName();
		planePos = getPosition();
		final AxisType a = Axes.get(axisName);
		if (inputBad(a)) return;
		final AxisType[] newAxes = getNewAxes(dataset, a);
		final long[] newDimensions = getNewDimensions(dataset, a);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, newAxes);
		final int compositeCount =
			compositeStatus(dataset.getCompositeChannelCount(), dstImgPlus);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus);
		// TODO - colorTables, metadata, etc.?
		dstImgPlus.setCompositeChannelCount(compositeCount);
		dataset.setImgPlus(dstImgPlus);
	}

	// -- protected interface --

	protected void initAll() {
		initAxisName();
		initPosition();
	}
	
	/** Updates the last value when the axis changes. */
	protected void axisChanged() {
		initPositionRange();
		clampPosition();
	}
	
	// -- private helpers --
	
	/**
	 * Detects if user specified data is invalid
	 */
	private boolean inputBad(final AxisType axis) {
		// axis not determined by dialog
		if (axis == null) return true;

		// axis not already present in Dataset
		final int axisIndex = dataset.getAxisIndex(axis);
		if (axisIndex < 0) return true;

		// hyperplane index out of range
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);
		if ((planePos < 1) || (planePos > axisSize)) return true;

		return false;
	}

	/**
	 * Creates an Axis[] that consists of all the axes from a Dataset minus a user
	 * specified axis
	 */
	private AxisType[] getNewAxes(final Dataset ds, final AxisType axis) {
		final AxisType[] origAxes = ds.getAxes();
		final AxisType[] newAxes = new AxisType[origAxes.length - 1];
		int index = 0;
		for (final AxisType a : origAxes)
			if (a != axis) newAxes[index++] = a;
		return newAxes;
	}

	/**
	 * Creates a long[] that consists of all the dimensions from a Dataset minus a
	 * user specified axis.
	 */
	private long[] getNewDimensions(final Dataset ds, final AxisType axis) {
		final long[] origDims = ds.getDims();
		final AxisType[] origAxes = ds.getAxes();
		final long[] newDims = new long[origAxes.length - 1];
		int index = 0;
		for (int i = 0; i < origAxes.length; i++) {
			final AxisType a = origAxes[i];
			if (a != axis) newDims[index++] = origDims[i];
		}
		return newDims;
	}

	/**
	 * Fills the data in the shrunken ImgPlus with the contents of the user
	 * specified hyperplane in the original image
	 */
	private void fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus)
	{
		final long[] srcOrigin = new long[srcImgPlus.numDimensions()];
		final long[] dstOrigin = new long[dstImgPlus.numDimensions()];

		final long[] srcSpan = new long[srcOrigin.length];
		final long[] dstSpan = new long[dstOrigin.length];

		srcImgPlus.dimensions(srcSpan);
		dstImgPlus.dimensions(dstSpan);

		final AxisType axis = Axes.get(axisName);
		final int axisIndex = srcImgPlus.getAxisIndex(axis);
		srcOrigin[axisIndex] = planePos - 1;
		srcSpan[axisIndex] = 1;

		RestructureUtils.copyHyperVolume(srcImgPlus, srcOrigin, srcSpan,
			dstImgPlus, dstOrigin, dstSpan);
	}

	private int
		compositeStatus(final int compositeCount, final ImgPlus<?> output)
	{
		if (output.getAxisIndex(Axes.CHANNEL) < 0) return 1;
		return compositeCount;

	}
	
	private Dataset getDataset() {
		return (Dataset) getInput(DATASET);
	}

	private long getPosition() {
		return (Long) getInput(POSITION);
	}
	
	private void setPosition(long pos) {
		setInput(POSITION, pos);
	}
	
	private String getAxisName() {
		return (String) getInput(AXIS_NAME);
	}

	private void initAxisName() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> axisNameItem =
			(DefaultModuleItem<String>) getInfo().getInput(AXIS_NAME);
		final Axis[] axes = getDataset().getAxes();
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis a : axes) {
			if (Axes.isXY(a)) continue;
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
	
	private void initPositionRange() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> positionItem =
			(DefaultModuleItem<Long>) getInfo().getInput(POSITION);
		long dimLen = currDimLen();
		positionItem.setMinimumValue(1L);
		positionItem.setMaximumValue(dimLen);
	}
	
	/** Ensures the first and last values fall within the allowed range. */
	private void clampPosition() {
		long max = currDimLen();
		long pos = getPosition();
		if (pos < 1) pos = 1;
		if (pos > max) pos = max;
		setPosition(pos);
 	}

	private long currDimLen() {
		Axis axis = Axes.get(getAxisName());
		int axisIndex = getDataset().getAxisIndex(axis);
		return getDataset().getImgPlus().dimension(axisIndex);
	}
}
