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

package imagej.util;

/**
 * This class represents an (X, Y) coordinate pair in real coordinates. It is
 * required for high precision image coordinates translation.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class RealCoords {

	public double x;
	public double y;

	public RealCoords(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	// -- RealCoords methods --

	public int getIntX() {
		return (int) (x + 0.5);
	}

	public int getIntY() {
		return (int) (y + 0.5);
	}

	public long getLongX() {
		return (long) (x + 0.5);
	}

	public long getLongY() {
		return (long) (y + 0.5);
	}

	// -- Object methods --

	@Override
	public String toString() {
		return "[Coords: x=" + x + ",y=" + y + "]";
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof RealCoords)) return false;
		final RealCoords that = (RealCoords) o;
		return x == that.x && y == that.y;
	}

	@Override
	public int hashCode() {
		// combine 16 least significant bits of x and y
		final int b1 = lsb16(x);
		final int b2 = lsb16(y);
		return b1 | (b2 << 16);
	}

	// -- Helper methods --

	private int lsb16(final double d) {
		return (int) Double.doubleToLongBits(d) & 0xffff;
	}

}
