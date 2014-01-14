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
 * Represents a plane of ARGB pixel data. Useful for passing around data during
 * copy/paste operations to external programs.
 * 
 * @author Barry DeZonia
 */
public class ARGBPlane {

	// -- instance variables --

	private final int width;
	private final int height;
	private final int[] data;

	// -- constructors --

	public ARGBPlane(final int width, final int height, final int[] data) {
		this.width = width;
		this.height = height;
		this.data = data;
		if (width < 1 || height < 1) {
			throw new IllegalArgumentException(
				"Both width and height must be greater than zero");
		}
		if ((long) width * height > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Plane is too large");
		}
		if (data.length != width * height) {
			throw new IllegalArgumentException("Data size mismatch");
		}
	}

	public ARGBPlane(final int width, final int height) {
		this(width, height, new int[width * height]);
	}

	// -- public interface --

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int[] getData() {
		return data;
	}

	public int getARGB(final int x, final int y) {
		return data[index(x, y)];
	}

	public int getAlpha(final int x, final int y) {
		return alpha(data[index(x, y)]);
	}

	public int getRed(final int x, final int y) {
		return red(data[index(x, y)]);
	}

	public int getGreen(final int x, final int y) {
		return green(data[index(x, y)]);
	}

	public int getBlue(final int x, final int y) {
		return blue(data[index(x, y)]);
	}

	public void setARGB(final int x, final int y, final int argb) {
		data[index(x, y)] = argb;
	}

	public void setAlpha(final int x, final int y, final int v) {
		int argb = data[index(x, y)];
		final int component = v & 0xff;
		argb &= ~0xff000000;
		argb |= (component << 24);
		data[index(x, y)] = argb;
	}

	public void setRed(final int x, final int y, final int v) {
		int argb = data[index(x, y)];
		final int component = v & 0xff;
		argb &= ~0xff0000;
		argb |= (component << 16);
		data[index(x, y)] = argb;
	}

	public void setGreen(final int x, final int y, final int v) {
		int argb = data[index(x, y)];
		final int component = v & 0xff;
		argb &= ~0xff00;
		argb |= (component << 8);
		data[index(x, y)] = argb;
	}

	public void setBlue(final int x, final int y, final int v) {
		int argb = data[index(x, y)];
		final int component = v & 0xff;
		argb &= ~0xff;
		argb |= (component << 0);
		data[index(x, y)] = argb;
	}

	// -- helpers --

	private int index(final int x, final int y) {
		return y * width + x;
	}

	private int alpha(final int argb) {
		return (argb >> 24) & 0xff;
	}

	private int red(final int argb) {
		return (argb >> 16) & 0xff;
	}

	private int green(final int argb) {
		return (argb >> 8) & 0xff;
	}

	private int blue(final int argb) {
		return (argb >> 0) & 0xff;
	}

}
