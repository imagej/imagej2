/**
 * Copyright (c) 2011, Stephan Saalfeld
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.  Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.  Neither the name of the imglib project nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package imagej.display.view;

import imagej.display.lut.Lut;
import net.imglib2.converter.Converter;
import net.imglib2.display.AbstractLinearRange;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * CompositeLUTConverter contains a lookup table, Lut,  for a channel.
 * Used to create a composite multi-channel image (used by CompositeXYProjector)
 * Accumulates the values from the lookup tables to the RGB values in the output/target image. 
 * 
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * @author Grant B. Harris
 */
public class CompositeLUTConverter< R extends RealType< R>> extends AbstractLinearRange implements Converter< R, ARGBType> {

	Lut lut = null;

	public CompositeLUTConverter() {
		super();
	}

	public CompositeLUTConverter(final double min, final double max, final Lut colors) {
		super(min, max);
		this.lut = colors;
	}

	@Override
	public void convert(final R input, final ARGBType output) {
		final double a = input.getRealDouble();
		final int b = Math.min(255, roundPositive(Math.max(0, ((a - min) / scale * 255.0))));
		final int argb = ARGBType.rgba(lut.reds[b], lut.greens[b], lut.blues[b], 0xff);
		output.add(new ARGBType(argb));
	}

}
