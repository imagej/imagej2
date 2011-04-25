//
// ColorTable8.java
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

package imagej.display.lut;

import net.imglib2.type.numeric.ARGBType;

/**
 * 8-bit color lookup table.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class ColorTable8 {

	private final byte[][] values;

	public ColorTable8(final byte[]... values) {
		this.values = values;
	}

	public int getComponentCount() {
		return values.length;
	}

	public int getLength() {
		return values[0].length;
	}

	public int get(final int c, final int i) {
		return values[c][i] & 0xff;
	}

	public int argb(final int i) {
		final int r = values[0][i];
		final int g = values[1][i];
		final int b = values[2][i];
		final int a = values.length > 3 ? values[3][i] : 0xff;
		return ARGBType.rgba(r, g, b, a);
	}

	public byte[][] getValues() {
		return values.clone();
	}

	public byte[] getReds() {
		return values.length > 0 ? values[0].clone() : null;
	}

	public byte[] getGreens() {
		return values.length > 1 ? values[1].clone() : null;
	}

	public byte[] getBlues() {
		return values.length > 2 ? values[2].clone() : null;
	}

	public byte[] getAlphas() {
		return values.length > 3 ? values[3].clone() : null;
	}

}
