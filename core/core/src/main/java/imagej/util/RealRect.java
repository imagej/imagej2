//
// RealRect.java
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

package imagej.util;

/**
 * A class for representing a rectangular region, in real coordinates.
 *
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class RealRect {

	// -- Fields --

	public double x;
	public double y;
	public double width;
	public double height;

	// -- Constructor --

	public RealRect() {
		// default constructor - allow all instance vars to be initialized to 0
	}

	public RealRect(final double x, final double y, final double width,
		final double height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	// -- RealRect methods --

	/** Returns true if this rect intersects the given rect. */
	public boolean intersects(final RealRect r) {
		double tw = this.width;
		double th = this.height;
		double rw = r.width;
		double rh = r.height;
		if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
			return false;
		}
		final double tx = this.x;
		final double ty = this.y;
		final double rx = r.x;
		final double ry = r.y;
		rw += rx;
		rh += ry;
		tw += tx;
		th += ty;
		final boolean rtn =
			(rw < rx || rw > tx) && (rh < ry || rh > ty) &&
			(tw < tx || tw > rx) && (th < ty || th > ry);
		return rtn;
	}

	/**
	 * Returns a Rect representing the doubleersection of this Rect with the given
	 * Rect. If the two Rects do not doubleersect, the result is an empty Rect.
	 */
	public RealRect doubleersection(final RealRect r) {
		final double newX = Math.max(this.x, r.x);
		final double newY = Math.max(this.y, r.y);
		double newW = Math.min(this.x + this.width, r.x + r.width) - x;
		double newH = Math.min(this.y + this.height, r.y + r.height) - y;

		if (newW < 0) newW = 0;
		if (newH < 0) newH = 0;

		return new RealRect(newX, newY, newW, newH);
	}

	@Override
	public String toString() {
		return "x=" + x + ", y=" + y + ", w=" + width + ", h=" + height;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof RealRect)) return false;
		final RealRect rect = (RealRect) o;
		return x == rect.x && y == rect.y && width == rect.width &&
			height == rect.height;
	}

	@Override
	public int hashCode() {
		// combine 8 least significant bits of x, y, width and height
		final int b1 = lsb(x);
		final int b2 = lsb(y);
		final int b3 = lsb(width);
		final int b4 = lsb(height);
		return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
	}

	// -- Helper methods --

	private int lsb(final double d) {
		return (int) Double.doubleToLongBits(d) & 0xff;
	}

}
