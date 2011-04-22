//
// GradientImage.java
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

package imagej.core.plugins;

import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import net.imglib2.Cursor;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.type.numeric.RealType;

/**
 * A demonstration plugin that generates a gradient image.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(menuPath = "Process>Gradient")
public class GradientImage implements ImageJPlugin {

	public static final String DEPTH1 = "1-bit";
	public static final String DEPTH8 = "8-bit";
	public static final String DEPTH12 = "12-bit";
	public static final String DEPTH16 = "16-bit";
	public static final String DEPTH32 = "32-bit";
	public static final String DEPTH64 = "64-bit";

	@Parameter(min = "1")
	private final int width = 512;

	@Parameter(min = "1")
	private final int height = 512;

	@Parameter(callback = "bitDepthChanged", choices = { DEPTH1, DEPTH8,
		DEPTH12, DEPTH16, DEPTH32, DEPTH64 })
	private String bitDepth = DEPTH8;

	@Parameter(callback = "signedChanged")
	private boolean signed = false;

	@Parameter(callback = "floatingChanged")
	private boolean floating = false;

	@Parameter(output = true)
	private Dataset dataset;

	// -- RunnablePlugin methods --

	@Override
	public void run() {
		// create the dataset
		final String name = "Gradient Image";
		final int bitsPerPixel = getBitsPerPixel();
		final long[] dims = { width, height };
		final Axis[] axes = { Axes.X, Axes.Y };
		dataset = Dataset.create(name, dims, axes, bitsPerPixel, signed, floating);

		// fill in the diagonal gradient
		final Cursor<? extends RealType<?>> cursor = dataset.getImage().cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			final long x = cursor.getLongPosition(0);
			final long y = cursor.getLongPosition(1);
			final RealType<?> type = cursor.get();
			final double value = calcRangedValue(x + y, type);
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

	private int getBitsPerPixel() {
		final int dash = bitDepth.indexOf("-");
		return Integer.parseInt(bitDepth.substring(0, dash));
	}

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
