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

package imagej.io.plugins;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import net.imglib2.Cursor;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

/**
 * Creates a new {@link Dataset}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(label = "New Image...", iconPath = "/icons/plugins/picture.png",
	menu = {
		@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
			mnemonic = MenuConstants.FILE_MNEMONIC),
		@Menu(label = "New", mnemonic = 'n'),
		@Menu(label = "Image...", weight = 0, mnemonic = 'i',
			accelerator = "control N") })
public class NewImage implements ImageJPlugin {

	public static final String DEPTH1 = "1-bit";
	public static final String DEPTH8 = "8-bit";
	public static final String DEPTH12 = "12-bit";
	public static final String DEPTH16 = "16-bit";
	public static final String DEPTH32 = "32-bit";
	public static final String DEPTH64 = "64-bit";

	public static final String WHITE = "White";
	public static final String BLACK = "Black";
	public static final String RAMP = "Ramp";
	public static final String ZERO = "Zero";

	@Parameter(persist = false)
	private DatasetService datasetService;

	@Parameter
	private String name = "Untitled";

	@Parameter(label = "Bit Depth", callback = "bitDepthChanged", choices = {
		DEPTH1, DEPTH8, DEPTH12, DEPTH16, DEPTH32, DEPTH64 })
	private String bitDepth = DEPTH8;

	@Parameter(callback = "signedChanged")
	private boolean signed = false;

	@Parameter(callback = "floatingChanged")
	private boolean floating = false;

	@Parameter(label = "Fill With", choices = { WHITE, BLACK, RAMP, ZERO })
	private String fillType = WHITE;

	@Parameter(min = "1")
	private long width = 512;

	@Parameter(min = "1")
	private long height = 512;

	// TODO: allow creation of multidimensional datasets

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset dataset;

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
		this.fillType = fillType;
	}

	public long getWidth() {
		return width;
	}

	public void setWidth(final long width) {
		this.width = width;
	}

	public long getHeight() {
		return height;
	}

	public void setHeight(final long height) {
		this.height = height;
	}

	// -- RunnablePlugin methods --

	@Override
	public void run() {
		// create the dataset
		final int bitsPerPixel = getBitsPerPixel();
		final long[] dims = { width, height };
		final AxisType[] axes = { Axes.X, Axes.Y };
		dataset =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);

		final boolean isWhite = fillType.equals(WHITE);
		final boolean isBlack = fillType.equals(BLACK);
		final boolean isZero = fillType.equals(ZERO);

		// fill in the diagonal gradient
		final long[] pos = new long[2];
		final Cursor<? extends RealType<?>> cursor =
			dataset.getImgPlus().localizingCursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			pos[0] = cursor.getLongPosition(0);
			pos[1] = cursor.getLongPosition(1);
			final RealType<?> type = cursor.get();
			final double value;
			if (isWhite) value = type.getMaxValue();
			else if (isBlack) value = type.getMinValue();
			else if (isZero) value = 0;
			else value = rampedValue(pos, dims, type); // fillWith == RAMP
			type.setReal(value);
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
}
