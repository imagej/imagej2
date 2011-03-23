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

import imagej.model.AxisLabel;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import mpicbg.imglib.cursor.LocalizableCursor;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * A demonstration plugin that generates a gradient image.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Process>Gradient")
public class GradientImage implements ImageJPlugin {

	public static final String DEPTH1 = "1-bit";
	public static final String DEPTH8 = "8-bit";
	public static final String DEPTH12 = "12-bit";
	public static final String DEPTH16 = "16-bit";
	public static final String DEPTH32 = "32-bit";
	public static final String DEPTH64 = "64-bit";

	@Parameter(callback = "bitDepthChanged",
		choices = { DEPTH1, DEPTH8, DEPTH12, DEPTH16, DEPTH32, DEPTH64 })
	private String bitDepth = DEPTH8;

	@Parameter(callback = "signedChanged")
	private boolean signed = false;

	@Parameter(callback = "floatingChanged")
	private boolean floating = false;

	@Parameter(min = "1")
	private int width = 512;

	@Parameter(min = "1")
	private int height = 512;

	@Parameter(output = true)
	private Dataset dataset;

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() {
		// create the dataset
		final String name = "Gradient Image";
		final RealType type = makeType();
		final int[] dims = { width, height };
		final AxisLabel[] axes = { AxisLabel.X, AxisLabel.Y };
		dataset = Dataset.create(name, type, dims, axes);

		// fill in the diagonal gradient
		final LocalizableCursor<? extends RealType<?>> cursor =
			(LocalizableCursor<? extends RealType<?>>)
			dataset.getImage().createLocalizableCursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			final int x = cursor.getPosition(0);
			final int y = cursor.getPosition(1);
			cursor.getType().setReal(x + y);
		}
		cursor.close();
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

	private RealType<? extends RealType<?>> makeType() {
		if (bitDepth.equals(DEPTH1)) {
			assert !signed && !floating;
			return new BitType();
		}
		if (bitDepth.equals(DEPTH8)) {
			assert !floating;
			if (signed) return new ByteType();
			return new UnsignedByteType();
		}
		if (bitDepth.equals(DEPTH12)) {
			assert !signed && !floating;
			return new Unsigned12BitType();
		}
		if (bitDepth.equals(DEPTH16)) {
			assert !floating;
			if (signed) return new ShortType();
			return new UnsignedShortType();
		}
		if (bitDepth.equals(DEPTH32)) {
			if (floating) {
				assert signed;
				return new FloatType();
			}
			if (signed) return new IntType();
			return new UnsignedIntType();
		}
		if (bitDepth.equals(DEPTH64)) {
			assert signed;
			if (floating) return new DoubleType();
			return new LongType();
		}
		throw new IllegalStateException("Invalid parameters: bitDepth=" +
			bitDepth + ", signed=" + signed + ", floating=" + floating);
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

}
