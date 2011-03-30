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
 * A class for representing a rectangular region. This class is very similar to
 * {@link java.awt.Rectangle}; it mainly exists to avoid problems with AWT, JNI
 * and headless operation. Adapted from BioFormats' Region class.
 */
public class Rect {

  // -- Fields --

	public int x;
	public int y;
	public int width;
	public int height;

  // -- Constructor --

	public Rect() {
		// default constructor - allow all instance vars to be initialized to 0
	}

	public Rect(final int x, final int y, final int width, final int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

  // -- Rect API methods --

	/** Returns true if this rect intersects the given rect. */
	public boolean intersects(final Rect r) {
		int tw = this.width;
		int th = this.height;
		int rw = r.width;
		int rh = r.height;
		if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
			return false;
		}
		final int tx = this.x;
		final int ty = this.y;
		final int rx = r.x;
		final int ry = r.y;
		rw += rx;
		rh += ry;
		tw += tx;
		th += ty;
		final boolean rtn =
			((rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry));
		return rtn;
	}

	/**
	 * Returns a Rect representing the intersection of this Rect with the given
	 * Rect. If the two Rects do not intersect, the result is an empty Rect.
	 */
	public Rect intersection(final Rect r) {
		final int newX = Math.max(this.x, r.x);
		final int newY = Math.max(this.y, r.y);
		int newW = Math.min(this.x + this.width, r.x + r.width) - x;
		int newH = Math.min(this.y + this.height, r.y + r.height) - y;

		if (newW < 0) newW = 0;
		if (newH < 0) newH = 0;

		return new Rect(newX, newY, newW, newH);
	}

	@Override
	public String toString() {
		return "x=" + x + ", y=" + y + ", w=" + width + ", h=" + height;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Rect)) return false;

		final Rect that = (Rect) o;
		return this.x == that.x && this.y == that.y && this.width == that.width &&
			this.height == that.height;
	}

}
