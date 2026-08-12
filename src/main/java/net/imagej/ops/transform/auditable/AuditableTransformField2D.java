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

import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;

/**
 * Audits local geometric properties of a 2D differentiable coordinate transformation.
 *
 * <p>Derives area distortion \(D(x, y) = |\det J(x, y)|\), singular-value anisotropy
 * \(\kappa(x, y) = \sigma_{\max}/\sigma_{\min}\), and validity \(\text{valid} = (D &gt; 0 \land \kappa \le \kappa_{\max})\).</p>
 *
 * @author ImageJ2 Developers
 */
public class AuditableTransformField2D {

	public static final double DEFAULT_KAPPA_MAX = 100.0;

	private final DifferentiableRealTransform transform;
	private final double kappaMax;

	public AuditableTransformField2D(final DifferentiableRealTransform transform) {
		this(transform, DEFAULT_KAPPA_MAX);
	}

	public AuditableTransformField2D(final DifferentiableRealTransform transform, final double kappaMax) {
		if (transform == null) throw new IllegalArgumentException("Transform must not be null");
		if (transform.numSourceDimensions() != 2 || transform.numTargetDimensions() != 2) {
			throw new IllegalArgumentException("Only 2D-to-2D transforms are supported");
		}
		if (kappaMax <= 0.0) throw new IllegalArgumentException("Maximum anisotropy threshold must be positive");
		this.transform = transform.copy();
		this.kappaMax = kappaMax;
	}

	public double kappaMax() {
		return kappaMax;
	}

	public double[][] jacobian(final double x, final double y) {
		final AffineTransform j = transform.jacobian(new double[] { x, y });
		return new double[][] { { j.get(0, 0), j.get(0, 1) }, { j.get(1, 0), j.get(1, 1) } };
	}

	public double areaDistortion(final double x, final double y) {
		return areaDistortion(jacobian(x, y));
	}

	public double anisotropy(final double x, final double y) {
		return anisotropy(jacobian(x, y));
	}

	public boolean isValid(final double x, final double y) {
		final double[][] j = jacobian(x, y);
		final double a = areaDistortion(j), k = anisotropy(j);
		return a > 0.0 && Double.isFinite(k) && k <= kappaMax;
	}

	public TransformAuditField2D sample(final int w, final int h, final double minX, final double maxX, final double minY, final double maxY) {
		if (w <= 0 || h <= 0) throw new IllegalArgumentException("Field dimensions must be positive");
		if (maxX < minX || maxY < minY) throw new IllegalArgumentException("Invalid domain bounds");
		final int size = w * h;
		final double[] area = new double[size], kappa = new double[size];
		final boolean[] valid = new boolean[size];
		for (int y = 0; y < h; y++) {
			final double py = (h == 1) ? 0.5 * (minY + maxY) : minY + (maxY - minY) * y / (h - 1);
			for (int x = 0; x < w; x++) {
				final double px = (w == 1) ? 0.5 * (minX + maxX) : minX + (maxX - minX) * x / (w - 1);
				final int idx = y * w + x;
				final double[][] j = jacobian(px, py);
				final double a = areaDistortion(j);
				final double k = anisotropy(j);
				area[idx] = a;
				kappa[idx] = k;
				valid[idx] = a > 0.0 && Double.isFinite(k) && k <= kappaMax;
			}
		}
		return new TransformAuditField2D(w, h, minX, maxX, minY, maxY, kappaMax, area, kappa, valid);
	}

	private double areaDistortion(final double[][] j) {
		return Math.abs(j[0][0] * j[1][1] - j[0][1] * j[1][0]);
	}

	private double anisotropy(final double[][] j) {
		final double a = j[0][0], b = j[0][1], c = j[1][0], d = j[1][1];
		final double s11 = a * a + c * c, s12 = a * b + c * d, s22 = b * b + d * d;
		final double trace = s11 + s22, det = s11 * s22 - s12 * s12;
		if (det <= 0.0) return Double.POSITIVE_INFINITY;
		final double disc = Math.max(0.0, trace * trace - 4.0 * det);
		final double lMax = 0.5 * (trace + Math.sqrt(disc));
		final double lMin = 0.5 * (trace - Math.sqrt(disc));
		return (lMin <= 0.0) ? Double.POSITIVE_INFINITY : Math.max(1.0, Math.sqrt(lMax / lMin));
	}
}
