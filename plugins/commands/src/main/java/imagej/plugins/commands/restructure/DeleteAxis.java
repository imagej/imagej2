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
import imagej.data.display.ImageDisplay;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;

import java.util.ArrayList;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.meta.SpaceUtils;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Deletes an axis from an input Dataset.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Axes", mnemonic = 'a'), @Menu(label = "Delete Axis...") },
	headless = true, initializer = "initAll")
public class DeleteAxis extends DynamicCommand {

	// -- Constants --

	private static final String AXIS_NAME = "axisName";
	private static final String POSITION = "position";

	// -- Parameters --

	@Parameter
	private ImageDisplay display;

	@Parameter(type = ItemIO.BOTH)
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
		if (inputBad(axis)) return;
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
			final long[] origPlaneDims = new long[dataset.numDimensions() - 2];
			for (int d = 0; d < origPlaneDims.length; d++)
				origPlaneDims[d] = dataset.dimension(d + 2);
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
		if (axis == null) {
			cancel("Axis must not be null.");
			return true;
		}

		// axis not already present in Dataset
		axisIndex = dataset.dimensionIndex(axis);
		if (axisIndex < 0) {
			cancel("Axis " + axis.getLabel()+" is not present in input dataset.");
			return true;
		}

		// hyperplane index out of range
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);
		if ((position < 1) || (position > axisSize)) {
			cancel("Position of plane to keep is out of bounds.");
			return true;
		}

		return false;
	}

	/**
	 * Creates an Axis[] that consists of all the axes from a Dataset minus a user
	 * specified axis
	 */
	private AxisType[] getNewAxes(final Dataset ds, final AxisType axis) {
		final AxisType[] origAxes = SpaceUtils.getAxisTypes(ds);
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
		if (axis.isXY()) {
			final long[] newDims = new long[ds.numDimensions()];
			final int dimIndex = ds.dimensionIndex(axis);
			for (int d = 0; d < newDims.length; d++) {
				newDims[d] = d == dimIndex ? 1 : ds.dimension(d);
			}
			return newDims;
		}
		final long[] newDims = new long[ds.numDimensions() - 1];
		int index = 0;
		for (int d = 0; d < ds.numDimensions(); d++) {
			final AxisType a = ds.axis(d).type();
			if (a != axis) newDims[index++] = ds.dimension(d);
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
		if (output.dimensionIndex(Axes.CHANNEL) < 0) return 1;
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
		public void remapPlanePosition(final long[] planeDims,
			final long[] origPlanePos, final long[] newPlanePos)
		{
			int curr = 0;
			for (int i = 0; i < planeDims.length; i++) {
				if (i == axisIndex - 2) continue;
				newPlanePos[curr++] = origPlanePos[i];
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
			// if (axisType.isXY()) continue;
			choices.add(axisType.getLabel());
		}
		axisNameItem.setChoices(choices);
	}

	private void initPosition() {
		final long max = getDataset().dimension(0);
		final AxisType axis = getDataset().axis(0).type();
		final long value = display.getLongPosition(axis) + 1;
		initPositionRange(1, max);
		setPosition(value);
	}

	private void initPositionRange(final long min,
		@SuppressWarnings("unused") final long max)
	{
		final MutableModuleItem<Long> positionItem =
			getInfo().getMutableInput(POSITION, Long.class);
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
		final int index = getDataset().dimensionIndex(axis);
		return getDataset().getImgPlus().dimension(index);
	}

}
