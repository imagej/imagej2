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

package imagej.plugins.commands.io;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.types.DataType;
import imagej.data.types.DataTypeService;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.menu.MenuConstants;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Creates a new {@link Dataset}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, label = "New Image...",
	iconPath = "/icons/commands/picture.png", initializer = "init", menu = {
		@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
			mnemonic = MenuConstants.FILE_MNEMONIC),
		@Menu(label = "New", mnemonic = 'n'),
		@Menu(label = "Image...", weight = 0, mnemonic = 'i', accelerator = "^N") })
public class NewImage<U extends RealType<U> & NativeType<U>> extends
	DynamicCommand
{

	// -- private constants --

	// TODO - this enables all axes
	// private static final AxisType[] defaultAxes = Axes.values();

	// TODO - this just enables axes we choose
	private static final AxisType[] defaultAxes = new AxisType[] { Axes.X,
		Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };

	// -- public constants --

	public static final String MAX = "Max";
	public static final String MIN = "Min";
	public static final String ZERO = "Zero";
	public static final String RAMP = "Ramp";

	private static final String DEFAULT_NAME = "Untitled";
	
	// -- Parameters --

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private DataTypeService dataTypeService;

	@Parameter
	private String name = DEFAULT_NAME;

	@Parameter(label = "Type", initializer = "initType")
	private String typeName;

	@Parameter(label = "Fill With", choices = { MAX, MIN, ZERO, RAMP })
	private String fillType = MAX;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset dataset;

	// -- instance variables --

	private long[] dimensions = new long[defaultAxes.length];

	// -- NewImage methods --

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getFillType() {
		return fillType;
	}

	// TODO - extend to support types unknown to the DataTypeService?

	public void setDataType(DataType<?> dataType) {
		String tname = dataType.longName();
		if (dataTypeService.getTypeByName(tname) == null) {
			throw new IllegalArgumentException("unknown data type [" + tname + "]");
		}
		typeName = tname;
	}

	public DataType<?> getDataType() {
		return dataTypeService.getTypeByName(typeName);
	}

	public void setFillType(final String fillType) {
		if (MIN.toString().equalsIgnoreCase(fillType)) this.fillType = MIN;
		else if (MAX.toString().equalsIgnoreCase(fillType)) this.fillType = MAX;
		else if (RAMP.toString().equalsIgnoreCase(fillType)) this.fillType = RAMP;
		else if (ZERO.toString().equalsIgnoreCase(fillType)) this.fillType = ZERO;
		else {
			throw new IllegalArgumentException("please specify fill type as one of " +
				MIN + ", " + MAX + ", " + RAMP + ", or " + ZERO);
		}
	}

	public long getDimension(final AxisType axisType) {
		for (int i = 0; i < defaultAxes.length; i++) {
			if (defaultAxes[i].equals(axisType)) return dimensions[i];
		}
		return 0; // could be 1 instead but 0 is more informative
	}

	public void setDimension(final AxisType axisType, final long size) {
		final long min;
		if (axisType.isXY()) min = 1;
		else min = 0;
		if (size < min) {
			throw new IllegalArgumentException("axis " + axisType +
				" dimension must be >= " + min);
		}
		for (int i = 0; i < defaultAxes.length; i++) {
			if (defaultAxes[i].equals(axisType)) dimensions[i] = size;
		}
		// NB - ignore axes we don't support in this plugin
	}

	public Dataset getDataset() {
		return dataset;
	}

	// -- Command methods --

	@Override
	public void run() {
		// FIXME: Migrate this logic into a service.
		fillProvidedDimensions();
		if ((name == null) || (name.trim().length() == 0)) name = DEFAULT_NAME;
		// create the dataset
		final long[] dims = getActualDims();
		final AxisType[] axes = getActualAxes();
		if (badSpecification(dims, axes)) {
			dataset = null;
			return;
		}
		@SuppressWarnings("unchecked")
		DataType<U> dataType = (DataType<U>) getDataType();
		U variable = dataType.createVariable();
		dataset = datasetService.create(variable, dims, name, axes);

		// initialize the image data
		
		final long[] pos = new long[2];
		@SuppressWarnings("unchecked")
		final Cursor<U> cursor =
			(Cursor<U>) dataset.getImgPlus().localizingCursor();

		boolean isMax = fillType.equals(MAX) && dataType.isBounded();
		boolean isMin = fillType.equals(MIN) && dataType.isBounded();
		boolean isZero = fillType.equals(ZERO);
		boolean isRamp = fillType.equals(RAMP);
		if (!isMax && !isMin && !isZero && !isRamp) isZero = true;

		U val = dataType.createVariable();
		U min = dataType.createVariable();
		U max = dataType.createVariable();

		if (!dataType.isBounded()) {
			isRamp = false;
			val.setZero();
		}
		else if (isMax) {
			dataType.upperBound(max);
			val.set(max);
		}
		else if (isMin) {
			dataType.lowerBound(min);
			val.set(min);
		}
		else if (isZero) {
			val.setZero();
		}
		else { // else isRamp
			dataType.upperBound(max);
			dataType.lowerBound(min);
		}

		ComplexDoubleType tmp = new ComplexDoubleType();

		while (cursor.hasNext()) {
			cursor.fwd();
			if (!isRamp) {
				cursor.get().set(val);
			}
			else { // isRamp
				// For performance work in real range. This is nearly always safe.
				pos[0] = cursor.getLongPosition(0);
				pos[1] = cursor.getLongPosition(1);
				rampedValue(pos, dims, dataType, min, max, tmp);
				cursor.get().setComplexNumber(tmp.getRealDouble(),
					tmp.getImaginaryDouble());
			}
		}
	}

	// -- initializers --

	protected void init() {
		for (AxisType axisType : defaultAxes) {
			final DefaultMutableModuleItem<Long> axisItem =
				new DefaultMutableModuleItem<Long>(this, axisType.getLabel(), Long.class);
			// NB - persist all values for now
			//if (!axisType.isXY()) axisItem.setPersisted(false);
			final long min;
			if (axisType.isXY()) min = 1;
			else min = 0;
			axisItem.setValue(this, min);
			axisItem.setMinimumValue(min);
			addInput(axisItem);
		}
	}

	protected void initType() {
		MutableModuleItem<String> input =
			getInfo().getMutableInput("typeName", String.class);
		List<String> choices = new ArrayList<String>();
		for (DataType<?> dataType : dataTypeService.getInstances()) {
			choices.add(dataType.longName());
		}
		input.setChoices(choices);
		input.setValue(this, choices.get(0));
	}

	// -- Parameter callback methods --

	private void rampedValue(final long[] pos, final long[] dims,
		final DataType<U> type, U min, U max, ComplexDoubleType outValue)
	{
		double origin = min.getRealDouble();
		double range = max.getRealDouble() - min.getRealDouble();

		if (type.isFloat()) {
			origin = 0;
			range = 1;
		}

		double numerator = 0;
		double denominator = 0;
		for (int i = 0; i < pos.length; i++) {
			numerator += pos[i];
			denominator += dims[i] - 1;
		}

		if (denominator == 0) {
			outValue.setReal(origin);
			outValue.setImaginary(origin);
			return;
		}

		final double percent = numerator / denominator;

		double val = origin + percent * range;

		outValue.setReal(val);
		outValue.setImaginary(val);
	}

	private long[] getActualDims() {
		int numSpecified = 0;
		for (int i = 0; i < dimensions.length; i++) {
			long dim = dimensions[i];
			if (dim > 1 || ((dim == 1) && defaultAxes[i].isXY())) numSpecified++;
		}
		long[] dims = new long[numSpecified];
		int d = 0;
		for (int i = 0; i < dimensions.length; i++) {
			long dim = dimensions[i];
			if (dim > 1 || ((dim == 1) && defaultAxes[i].isXY())) dims[d++] = dim;
		}
		return dims;
	}

	private AxisType[] getActualAxes() {
		int numSpecified = 0;
		for (int i = 0; i < dimensions.length; i++) {
			long dim = dimensions[i];
			if (dim > 1 || ((dim == 1) && defaultAxes[i].isXY())) numSpecified++;
		}
		AxisType[] axes = new AxisType[numSpecified];
		int d = 0;
		for (int i = 0; i < dimensions.length; i++) {
			long dim = dimensions[i];
			if (dim > 1 || ((dim == 1) && defaultAxes[i].isXY())) {
				axes[d++] = defaultAxes[i];
			}
		}
		return axes;
	}

	// error conditions:
	// no X dim, no Y dim, any dim size <= 1 (or <= 0 for X or Y)
	
	private boolean badSpecification(long[] dims, AxisType[] axes) {
		boolean hasX = false;
		boolean hasY = false;
		for (int i = 0; i < dims.length; i++) {
			AxisType axisType = axes[i];
			hasX |= axisType.equals(Axes.X);
			hasY |= axisType.equals(Axes.Y);
			int smallestAllowed = axisType.isXY() ? 1 : 2;
			if (dims[i] < smallestAllowed) {
				cancel("New image: dimension size must be >= " + smallestAllowed +
					" for axis " + axisType);
				return true;
			}
		}
		if (!hasX || !hasY) {
			cancel("New image: images require both X and Y axes to be present.");
			return true;
		}
		return false;
	}

	private void fillProvidedDimensions() {
		for (int i = 0; i < defaultAxes.length; i++) {
			AxisType axisType = defaultAxes[i];
			Object input = getInput(axisType.getLabel());
			// FIXME TODO - when this plugin is called directly from Java the Inputs
			// may not have been initialized because pre/post processors not called.
			// It is the api users job to have specified the dimensions correctly
			// beforehand. So we only gather dims when the input is not null. Its not
			// clear that this code is correct. However without it the
			// InvokeCommandTest plugin barfs with an NPE. See bug #1641.
			if (input != null) {
				long size = (Long) input;
				dimensions[i] = size;
			}
		}
	}


}
