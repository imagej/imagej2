/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
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

package net.imagej.ops.transform.auditable;

/**
 * Immutable result field for a two-dimensional spatial transform audit.
 *
 * @author ImageJ2 Developers
 */
public final class TransformAuditField2D {

	private final int width, height;
	private final double minX, maxX, minY, maxY, kappaMax;
	private final double[] areaDistortion, anisotropy;
	private final boolean[] validityMask;

	public TransformAuditField2D(
		final int width, final int height,
		final double minX, final double maxX, final double minY, final double maxY,
		final double kappaMax,
		final double[] areaDistortion, final double[] anisotropy, final boolean[] validityMask)
	{
		if (width <= 0 || height <= 0) throw new IllegalArgumentException("Grid dimensions must be positive");
		if (maxX < minX || maxY < minY) throw new IllegalArgumentException("Invalid domain bounds");
		final int size = width * height;
		if (areaDistortion == null || anisotropy == null || validityMask == null ||
			areaDistortion.length != size || anisotropy.length != size || validityMask.length != size)
		{
			throw new IllegalArgumentException("Audit arrays must match grid size");
		}
		this.width = width; this.height = height;
		this.minX = minX; this.maxX = maxX; this.minY = minY; this.maxY = maxY;
		this.kappaMax = kappaMax;
		this.areaDistortion = areaDistortion.clone();
		this.anisotropy = anisotropy.clone();
		this.validityMask = validityMask.clone();
	}

	public int width() { return width; }
	public int height() { return height; }
	public double minX() { return minX; }
	public double maxX() { return maxX; }
	public double minY() { return minY; }
	public double maxY() { return maxY; }
	public double kappaMax() { return kappaMax; }

	public double areaDistortion(final int x, final int y) { return areaDistortion[index(x, y)]; }
	public double anisotropy(final int x, final int y) { return anisotropy[index(x, y)]; }
	public boolean isValid(final int x, final int y) { return validityMask[index(x, y)]; }

	public double[] areaDistortion() { return areaDistortion.clone(); }
	public double[] anisotropy() { return anisotropy.clone(); }
	public boolean[] validityMask() { return validityMask.clone(); }

	public double validFraction() {
		int count = 0;
		for (final boolean v : validityMask) if (v) count++;
		return (double) count / validityMask.length;
	}

	public double meanAreaDistortion() {
		double sum = 0.0;
		for (final double v : areaDistortion) sum += v;
		return sum / areaDistortion.length;
	}

	public double minAreaDistortion() {
		double min = Double.POSITIVE_INFINITY;
		for (final double v : areaDistortion) min = Math.min(min, v);
		return min;
	}

	public double maxAreaDistortion() {
		double max = Double.NEGATIVE_INFINITY;
		for (final double v : areaDistortion) max = Math.max(max, v);
		return max;
	}

	public double maxAnisotropy() {
		double max = 0.0;
		for (final double v : anisotropy) max = Math.max(max, v);
		return max;
	}

	private int index(final int x, final int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			throw new IndexOutOfBoundsException("Grid coordinate (" + x + ", " + y + ") outside bounds");
		}
		return y * width + x;
	}
}
