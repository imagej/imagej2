//
// RealLUTConverter.java
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

package imagej.display.view;

import imagej.display.lut.ColorTable8;
import net.imglib2.converter.Converter;
import net.imglib2.display.AbstractLinearRange;
import net.imglib2.display.RealARGBConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * TODO
 * 
 * @author Grant Harris
 * @see RealARGBConverter for the code upon which this class was based.
 */
public class RealLUTConverter<R extends RealType<R>> extends
	AbstractLinearRange implements Converter<R, ARGBType>
{

	private ColorTable8 lut = null;

	public RealLUTConverter() {
		super();
	}

	public RealLUTConverter(final double min, final double max,
		final ColorTable8 lut)
	{
		super(min, max);
		this.lut = lut;
	}

	@Override
	public void convert(final R input, final ARGBType output) {
		final double a = input.getRealDouble();
		final int b =
			Math.min(255, roundPositive(Math.max(0, ((a - min) / scale * 255.0))));
		final int argb = lut.argb(b);
		output.set(argb);
	}

}
