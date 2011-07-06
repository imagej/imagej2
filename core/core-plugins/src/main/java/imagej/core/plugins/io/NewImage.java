//
// NewImage.java
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

package imagej.core.plugins.io;

import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import net.imglib2.Cursor;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.type.numeric.RealType;

/**
 * Creates a new {@link Dataset}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(label = "New Image...", iconPath = "/icons/plugins/picture.png",
	menu = {
		@Menu(label = "File", mnemonic = 'f'),
		@Menu(label = "New", mnemonic = 'n'),
		@Menu(label = "Image [IJ2]...", weight = 0, mnemonic = 'i',
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

	@Parameter
	private String name = "Untitled";

	@Parameter(label = "Bit Depth", callback = "bitDepthChanged", choices = {
		DEPTH1, DEPTH8, DEPTH12, DEPTH16, DEPTH32, DEPTH64 })
	private String bitDepth = DEPTH8;

	@Parameter(callback = "signedChanged")
	private boolean signed = false;

	@Parameter(callback = "floatingChanged")
	private boolean floating = false;

	@Parameter(label = "Fill With", choices = { WHITE, BLACK, RAMP })
	private String fillType = WHITE;

	@Parameter(min = "1")
	private int width = 512;

	@Parameter(min = "1")
	private int height = 512;

	// TODO: allow creation of multidimensional datasets

	@Parameter(output = true)
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

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	// -- RunnablePlugin methods --

	@Override
	public void run() {
		// create the dataset
		final int bitsPerPixel = getBitsPerPixel();
		final long[] dims = { width, height };
		final Axis[] axes = { Axes.X, Axes.Y };
		dataset = Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);
		
		final boolean isWhite = fillType.equals(WHITE);
		final boolean isBlack = fillType.equals(BLACK);

		// fill in the diagonal gradient
		final Cursor<? extends RealType<?>> cursor =
			dataset.getImgPlus().localizingCursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			final long x = cursor.getLongPosition(0);
			final long y = cursor.getLongPosition(1);
			final RealType<?> type = cursor.get();
			final double value;
			if (isWhite) value = type.getMaxValue();
			else if (isBlack) value = type.getMinValue();
			else value = calcRangedValue(x + y, type); // fillWith == RAMP
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

	// NOTE
	// input "value" assumed to be >= 0.
	// if not float data we modulate to fit into type's [range min, range, max]
	// note that this behavior is different than before in that it starts from
	// the type's minimum value. the difference is apparent for signed data.

	private double calcRangedValue(final double value, final RealType<?> type) {
		if (floating) return value;

		final double rangeMin = type.getMinValue();
		final double rangeMax = type.getMaxValue();
		final double totalRange = rangeMax - rangeMin + 1;

		double newValue = value;
		while (newValue >= totalRange) {
			newValue -= totalRange;
		}

		return rangeMin + newValue;
	}

}
