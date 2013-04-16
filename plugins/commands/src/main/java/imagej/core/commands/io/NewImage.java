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

package imagej.core.commands.io;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.menu.MenuConstants;
import imagej.module.DefaultModuleItem;
import net.imglib2.Cursor;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
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
	iconPath = "/icons/commands/picture.png",
	initializer = "init",
	menu = {
		@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
			mnemonic = MenuConstants.FILE_MNEMONIC),
		@Menu(label = "New", mnemonic = 'n'),
		@Menu(label = "Image...", weight = 0, mnemonic = 'i',
			accelerator = "control N") })
public class NewImage extends DynamicCommand {

	// -- private constants --

	// TODO - this enables all axes
	// private static final AxisType[] defaultAxes = Axes.values();

	// TODO - this just enables axes we choose
	private static final AxisType[] defaultAxes = new AxisType[] { Axes.X,
		Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };

	// -- public constants --

	public static final String DEPTH1 = "1-bit";
	public static final String DEPTH8 = "8-bit";
	public static final String DEPTH12 = "12-bit";
	public static final String DEPTH16 = "16-bit";
	public static final String DEPTH32 = "32-bit";
	public static final String DEPTH64 = "64-bit";

	public static final String MAX = "Max";
	public static final String MIN = "Min";
	public static final String ZERO = "Zero";
	public static final String RAMP = "Ramp";

	private static final String DEFAULT_NAME = "Untitled";
	
	// -- Parameters --

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private String name = DEFAULT_NAME;

	@Parameter(label = "Bit Depth", callback = "bitDepthChanged", choices = {
		DEPTH1, DEPTH8, DEPTH12, DEPTH16, DEPTH32, DEPTH64 })
	private String bitDepth = DEPTH8;

	@Parameter(callback = "signedChanged")
	private boolean signed = false;

	@Parameter(callback = "floatingChanged")
	private boolean floating = false;

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

	public int getBitsPerPixel() {
		final int dash = bitDepth.indexOf("-");
		return Integer.parseInt(bitDepth.substring(0, dash));
	}

	public void setBitsPerPixel(final int bitsPerPixel) {
		bitDepth = bitsPerPixel + "-bit";
	}

	public boolean isSigned() {
		return signed;
	}

	public void setSigned(final boolean signed) {
		this.signed = signed;
	}

	public boolean isFloating() {
		return floating;
	}

	public void setFloating(final boolean floating) {
		this.floating = floating;
	}

	public String getFillType() {
		return fillType;
	}

	public void setFillType(final String fillType) {
		if (MIN.toString().equalsIgnoreCase(fillType)) this.fillType = MIN;
		else if (MAX.toString().equalsIgnoreCase(fillType)) this.fillType = MAX;
		else if (RAMP.toString().equalsIgnoreCase(fillType)) this.fillType = RAMP;
		else this.fillType = ZERO;
	}

	public long getDimension(final AxisType axisType) {
		for (int i = 0; i < defaultAxes.length; i++) {
			if (defaultAxes[i].equals(axisType)) return dimensions[i];
		}
		return 0; // could be 1 instead but 0 is more informative
	}

	public void setDimension(final AxisType axisType, final long size) {
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
		setDimensions();
		if ((name == null) || (name.trim().length() == 0)) name = DEFAULT_NAME;
		// create the dataset
		final int bitsPerPixel = getBitsPerPixel();
		final long[] dims = getDims();
		final AxisType[] axes = getAxes();
		if (badSpecification(dims, axes)) {
			dataset = null;
			return;
		}
		dataset =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);

		// fill in the diagonal gradient
		final long[] pos = new long[2];
		final Cursor<? extends RealType<?>> cursor =
			dataset.getImgPlus().localizingCursor();
		final RealType<?> type = cursor.get();

		final boolean isMax = fillType.equals(MAX);
		final boolean isMin = fillType.equals(MIN);
		final boolean isZero = fillType.equals(ZERO);
		final boolean isRamp = fillType.equals(RAMP);

		double v = Double.NaN;
		if (isMax) v = type.getMaxValue();
		else if (isMin) v = type.getMinValue();
		else if (isZero) v = 0;
		// else fill type == ramp and v still NaN

		while (cursor.hasNext()) {
			cursor.fwd();
			final double value;
			if (!isRamp) {
				value = v;
			}
			else { // isRamp
				pos[0] = cursor.getLongPosition(0);
				pos[1] = cursor.getLongPosition(1);
				value = rampedValue(pos, dims, type);
			}
			type.setReal(value);
		}
	}

	// -- initializer --

	protected void init() {
		for (AxisType axisType : defaultAxes) {
			final DefaultModuleItem<Long> axisItem =
				new DefaultModuleItem<Long>(this, axisType.getLabel(), Long.class);
			// NB - persist all values for now
			//if (!axisType.isXY()) axisItem.setPersisted(false);
			axisItem.setValue(this, 0L);
			axisItem.setMinimumValue(0L);
			addInput(axisItem);
		}
	}

	// -- Parameter callback methods --

	protected void bitDepthChanged() {
		if (signedForbidden()) signed = false;
		if (signedRequired()) signed = true;
		if (floatingForbidden()) floating = false;
	}

	protected void signedChanged() {
		if (signed && signedForbidden() || !signed && signedRequired()) {
			bitDepth = DEPTH8;
		}
		if (!signed) floating = false; // no unsigned floating types
	}

	protected void floatingChanged() {
		if (floating && floatingForbidden()) bitDepth = DEPTH32;
		if (floating) signed = true; // no unsigned floating types
	}

	// -- Helper methods --

	private boolean signedForbidden() {
		// 1-bit and 12-bit signed data are not supported
		return bitDepth.equals(DEPTH1) || bitDepth.equals(DEPTH12);
	}

	private boolean signedRequired() {
		// 64-bit unsigned data is not supported
		return bitDepth.equals(DEPTH64);
	}

	private boolean floatingForbidden() {
		// only 32-bit and 64-bit floating point are supported
		return !bitDepth.equals(DEPTH32) && !bitDepth.equals(DEPTH64);
	}

	private double rampedValue(final long[] pos, final long[] dims,
		final RealType<?> type)
	{
		double origin = type.getMinValue();
		double range = type.getMaxValue() - type.getMinValue();
		if (floating) {
			origin = 0;
			range = 1;
		}

		double numerator = 0;
		double denominator = 0;
		for (int i = 0; i < pos.length; i++) {
			numerator += pos[i];
			denominator += dims[i] - 1;
		}

		if (denominator == 0) return origin;

		final double percent = numerator / denominator;

		return origin + percent * range;
	}

	private long[] getDims() {
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

	private AxisType[] getAxes() {
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
	// no X dim, no Y dim, any dim size <= 1 (unless X or Y)
	
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

	private void setDimensions() {
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
