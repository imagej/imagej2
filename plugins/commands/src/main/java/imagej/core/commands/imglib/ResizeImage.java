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

package imagej.core.commands.imglib;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.command.DynamicCommandInfo;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.types.DataType;
import imagej.data.types.DataTypeService;
import imagej.menu.MenuConstants;
import imagej.module.ModuleItem;
import imagej.module.MutableModuleItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.iterableinterval.unary.Resample;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Resamples a {@link Dataset} and changes it in place.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC), @Menu(label = "Adjust"),
	@Menu(label = "Resize") }, initializer = "init", headless = true)
public class ResizeImage extends DynamicCommand {

	// -- constants --

	private static final String CONSTRAIN = "Constrain";
	private static final String DIMENSION = "Dimension";
	private static final String INTERPOLATION = "Interpolation";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter
	private DataTypeService dataTypeService;

	@Parameter
	private DatasetService datasetService;

	// -- non-parameter fields --

	private List<String> modes = new ArrayList<String>();
	private List<Long> dimensions = new ArrayList<Long>();
	private double aspectRatio = Double.NaN;
	private int xIndex = -1;
	private int yIndex = -1;

	// -- accessors --

	/**
	 * Returns the name of the current interpolation mode.
	 */
	public String getMode() {
		return (String) getInfo().getInput(INTERPOLATION).getValue(this);
	}

	/**
	 * Sets the name of the current interpolation mode. Given mode name must be
	 * one of the values listed by getModes().
	 */
	public void setMode(String mode) {
		boolean found = false;
		for (String s : modes) {
			if (s.equals(mode)) found = true;
		}
		if (!found) {
			throw new IllegalArgumentException("Unknown interpolation mode: " + mode);
		}
		DynamicCommandInfo info = getInfo();
		((ModuleItem<String>) info.getInput(INTERPOLATION)).setValue(this, mode);
	}

	/**
	 * A list of the names of all the possible interpolation methods.
	 */
	public List<String> getModes() {
		return Collections.unmodifiableList(modes);
	}

	/**
	 * Returns the number of dimensions in the resampled image.
	 */
	public int numDimensions() {
		return dataset.numDimensions();
	}

	/**
	 * Returns the size of a dimension for resampling purposes.
	 * 
	 * @param d The dimension number.
	 * @return The size of the dimension in the resampled image.
	 */
	public long dimension(int d) {
		if (d < dimensions.size()) return dimensions.get(d);
		return dataset.dimension(d);
	}

	/**
	 * Sets the size of a dimension for resampling purposes.
	 * 
	 * @param d The dimension number.
	 * @param size The size of the dimension for the resampled image.
	 */
	public void setDimension(int d, long size) {
		if (size <= 0) {
			throw new IllegalArgumentException("Dimension size must be positive.");
		}
		for (int i = 0; i <= d; i++) {
			if (i >= dimensions.size()) {
				dimensions.add(dataset.dimension(i));
			}
		}
		dimensions.set(d, size);
	}

	// -- Command methods --

	@Override
	public void run() {
		ImgPlus<? extends RealType<?>> origImgPlus = dataset.getImgPlus();
		int numDims = origImgPlus.numDimensions();
		String name = dataset.getName();
		CalibratedAxis[] axes = new CalibratedAxis[numDims];
		dataset.axes(axes);
		AxisType[] axisTypes = new AxisType[numDims];
		for (int i = 0; i < numDims; i++) {
			axisTypes[i] = axes[i].type();
		}
		RealType<?> example = origImgPlus.firstElement();
		DataType<?> type = dataTypeService.getTypeByClass(example.getClass());
		Dataset newDs =
			datasetService.create(newDims(), name, axisTypes, type.bitCount(), type
				.isSigned(), type.isFloat());
		newDs.setAxes(axes);
		if (dataset.getCompositeChannelCount() == numChannels(dataset)) {
			newDs.setCompositeChannelCount(numChannels(newDs));
		}
		Resample<?, Img<? extends RealType<?>>> resampleOp =
			new Resample(resampleMode());
		resampleOp.compute(origImgPlus, newDs.getImgPlus());
		dataset.setImgPlus(newDs.getImgPlus());
	}

	// -- initializers --

	protected void init() {

		// fields

		xIndex = dataset.dimensionIndex(Axes.X);
		yIndex = dataset.dimensionIndex(Axes.Y);
		if (xIndex >= 0 && yIndex >= 0) {
			aspectRatio =
				((double) dataset.dimension(yIndex)) / dataset.dimension(xIndex);
		}
		else {
			aspectRatio = Double.NaN;
		}

		// axis dimensions

		for (int i = 0; i < numDimensions(); i++) {
			final MutableModuleItem<Long> dimensionItem =
				addInput(DIMENSION + i, Long.class);
			dimensionItem.setValue(this, dimension(i));
			dimensionItem.setLabel(dataset.axis(i).type().toString());
			String callback;
			if (i == xIndex) {
				callback = "updateHeight";
			}
			else if (i == yIndex) {
				callback = "updateWidth";
			}
			else {
				callback = "updateOther";
			}
			dimensionItem.setCallback(callback);
			dimensionItem.setPersisted(false);
		}

		// constrain aspect ratio : XY only

		final MutableModuleItem<Boolean> constrainItem =
			addInput(CONSTRAIN, Boolean.class);
		constrainItem.setLabel("Maintain XY aspect ratio");
		constrainItem.setPersisted(false);

		// interpolation modes

		for (Resample.Mode mode : Resample.Mode.values()) {
			modes.add(mode.toString());
		}
		final MutableModuleItem<String> interpolationItem =
			addInput(INTERPOLATION, String.class);
		interpolationItem.setPersisted(false);
		interpolationItem.setLabel(INTERPOLATION);
		interpolationItem.setChoices(modes);
		interpolationItem.setValue(this, modes.get(0));
	}

	// -- callbacks --

	protected void updateWidth() {
		updateConstrainedDim(xIndex);
		setAllDimensions();
	}

	protected void updateHeight() {
		updateConstrainedDim(yIndex);
		setAllDimensions();
	}

	protected void updateOther() {
		setAllDimensions();
	}

	// -- helpers --

	private long[] newDims() {
		long[] dims = new long[numDimensions()];
		for (int i = 0; i < dims.length; i++) {
			dims[i] = dimension(i);
		}
		return dims;
	}

	private Resample.Mode resampleMode() {
		return Resample.Mode.valueOf(Resample.Mode.class, getMode());
	}

	private void updateConstrainedDim(int index) {
		if (constrain()) {
			int otherIndex = (index == xIndex) ? yIndex : xIndex;
			long currVal = getDimValue(otherIndex);
			long newVal;
			if (index == xIndex) newVal = (long) Math.ceil(currVal / aspectRatio);
			else newVal = (long) Math.ceil(currVal * aspectRatio);
			setDimValue(index, newVal);
		}
	}

	private void setDimValue(int index, long value) {
		ModuleItem<Long> item = getItem(index);
		item.setValue(this, value);
	}

	private long getDimValue(int index) {
		ModuleItem<Long> item = getItem(index);
		return item.getValue(this);
	}

	private ModuleItem<Long> getItem(int index) {
		String name = DIMENSION + index;
		DynamicCommandInfo info = getInfo();
		return ((ModuleItem<Long>) info.getInput(name));
	}

	private boolean constrain() {
		if (xIndex < 0) return false;
		if (yIndex < 0) return false;
		if (Double.isNaN(aspectRatio)) return false;
		return ((ModuleItem<Boolean>) getInfo().getInput(CONSTRAIN)).getValue(this);
	}

	private void setAllDimensions() {
		for (int d = 0; d < numDimensions(); d++) {
			setDimension(d, getDimValue(d));
		}
	}

	private int numChannels(Dataset ds) {
		int index = ds.dimensionIndex(Axes.CHANNEL);
		if (index < 0) return 1;
		return (int) ds.dimension(index);
	}
}
