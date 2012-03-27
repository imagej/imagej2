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
import imagej.data.display.ImageDisplay;
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
 * Deletes an axis from an input Dataset.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Delete Axis...") },
	headless = true, initializer = "initAll")
public class DeleteAxis extends DynamicPlugin {

	// -- Constants --

	private static final String AXIS_NAME = "axisName";
	private static final String POSITION = "position";

	// -- Parameters --

	@Parameter(persist = false)
	private UIService uiService;

	@Parameter(persist = false)
	private ImageDisplay display;

	@Parameter(persist = false)
	private Dataset dataset;

	@Parameter(label = "Axis to delete", persist = false,
		callback = "axisChanged")
	private String axisName;

	@Parameter(label = "Index of hyperplane to keep", persist = false,
		callback = "positionChanged")
	private long position;

	// -- private members --

	private int axisIndex;

	// -- DeleteAxis methods --

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	public AxisType getAxis() {
		return Axes.get(axisName);
	}

	public void setAxis(final AxisType axis) {
		axisName = axis.toString();
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(final long position) {
		this.position = position;
	}

	// -- Runnable methods --

	/**
	 * Creates new ImgPlus data with one less axis. Sets pixels of ImgPlus to user
	 * specified hyperplane within original ImgPlus data. Assigns the new ImgPlus
	 * to the input Dataset.
	 */
	@Override
	public void run() {
		final AxisType axis = getAxis();
		if (inputBad(axis)) {
			informUser();
			return;
		}
		final AxisType[] newAxes = getNewAxes(dataset, axis);
		final long[] newDimensions = getNewDimensions(dataset, axis);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, newAxes);
		final int compositeCount =
			compositeStatus(dataset.getCompositeChannelCount(), dstImgPlus);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus);
		dstImgPlus.setCompositeChannelCount(compositeCount);
		RestructureUtils.allocateColorTables(dstImgPlus);
		if (axis.isXY()) {
			RestructureUtils.copyColorTables(dataset.getImgPlus(), dstImgPlus);
		}
		else {
			final long[] origDims = dataset.getDims();
			final long[] origPlaneDims = new long[origDims.length - 2];
			for (int i = 0; i < origPlaneDims.length; i++)
				origPlaneDims[i] = origDims[i + 2];
			final ColorTableRemapper remapper =
				new ColorTableRemapper(new RemapAlgorithm(origPlaneDims));
			remapper.remapColorTables(dataset.getImgPlus(), dstImgPlus);
		}
		// TODO - metadata, etc.?
		dataset.setImgPlus(dstImgPlus);
	}

	// -- Initializers --

	protected void initAll() {
		initAxisName();
		initPosition();
	}

	// -- Callbacks --

	/** Updates the last value when the axis changes. */
	protected void axisChanged() {
		final AxisType axis = Axes.get(axisName);
		final long value = display.getLongPosition(axis) + 1;
		final long max = currDimLen();
		initPositionRange(1, max);
		setPosition(value);
		clampPosition();
	}

	// TODO - temporary workaround to allow parameter max to be enforced. Should
	// be removed when ticket #886 addressed.

	protected void positionChanged() {
		clampPosition();
	}

	// -- Helper methods --

	/**
	 * Detects if user specified data is invalid
	 */
	private boolean inputBad(final AxisType axis) {
		// axis not determined by dialog
		if (axis == null) return true;

		// axis not already present in Dataset
		axisIndex = dataset.getAxisIndex(axis);
		if (axisIndex < 0) return true;

		// hyperplane index out of range
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);
		if ((position < 1) || (position > axisSize)) return true;

		return false;
	}

	/**
	 * Creates an Axis[] that consists of all the axes from a Dataset minus a user
	 * specified axis
	 */
	private AxisType[] getNewAxes(final Dataset ds, final AxisType axis) {
		final AxisType[] origAxes = ds.getAxes();
		if (axis.isXY()) return origAxes;
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
		if (axis.isXY()) {
			final long[] newDims = origDims;
			newDims[ds.getAxisIndex(axis)] = 1;
			return newDims;
		}
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

		srcOrigin[axisIndex] = position - 1;
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

	private class RemapAlgorithm implements ColorTableRemapper.RemapAlgorithm {

		private final long[] origPlaneDims;
		private final long[] srcPlanePos;

		public RemapAlgorithm(final long[] origPlaneDims) {
			this.origPlaneDims = origPlaneDims;
			this.srcPlanePos = new long[origPlaneDims.length];
		}

		@Override
		public boolean isValidSourcePlane(final long i) {
			ColorTableRemapper.toND(origPlaneDims, i, srcPlanePos);
			return srcPlanePos[axisIndex - 2] == position - 1;
		}

		@Override
		public void remapPlanePosition(final long[] origPlaneDims,
			final long[] origPlanePos, final long[] newPlanePos)
		{
			int curr = 0;
			for (int i = 0; i < origPlaneDims.length; i++) {
				if (i == axisIndex - 2) continue;
				newPlanePos[curr++] = origPlanePos[i];
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
			// if (Axes.isXY(a)) continue;
			choices.add(a.getLabel());
		}
		axisNameItem.setChoices(choices);
	}

	private void initPosition() {
		final long max = getDataset().dimension(0);
		final AxisType axis = getDataset().axis(0);
		final long value = display.getLongPosition(axis) + 1;
		initPositionRange(1, max);
		setPosition(value);
	}

	private void initPositionRange(final long min, final long max) {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> positionItem =
			(DefaultModuleItem<Long>) getInfo().getInput(POSITION);
		positionItem.setMinimumValue(min); // works the first time
		// TODO - temporarily disabled since parameter mins and maxes cannot be
		// changed on the fly. Should be enabled when ticket #886 addressed.
		// positionItem.setMaximumValue(max);
	}

	/** Ensures the first and last values fall within the allowed range. */
	private void clampPosition() {
		final long max = currDimLen();
		final long pos = getPosition();
		if (pos < 1) setPosition(1);
		else if (pos > max) setPosition(max);
	}

	private long currDimLen() {
		final AxisType axis = getAxis();
		final int index = getDataset().getAxisIndex(axis);
		return getDataset().getImgPlus().dimension(index);
	}

	private void informUser() {
		uiService.showDialog("Data unchanged: bad combination of input parameters",
			"Invalid parameter combination");
	}

}
