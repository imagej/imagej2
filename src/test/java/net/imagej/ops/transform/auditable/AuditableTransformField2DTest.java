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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.ThinplateSplineTransform;
import net.imglib2.realtransform.inverse.AbstractDifferentiableRealTransform;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;
import net.imglib2.realtransform.inverse.RealTransformFiniteDerivatives;

import org.junit.Test;

/**
 * Unit tests for {@link AuditableTransformField2D} and {@link TransformAuditField2D}.
 *
 * @author ImageJ2 Developers
 */
public class AuditableTransformField2DTest {

	private static final double EPS = 1e-8;

	@Test
	public void identityTransformHasUnitMetrics() {
		final AuditableTransformField2D auditor = new AuditableTransformField2D(new RealTransformFiniteDerivatives(new AffineTransform2D()));
		final TransformAuditField2D field = auditor.sample(5, 5, 0.0, 1.0, 0.0, 1.0);
		for (int y = 0; y < field.height(); y++) {
			for (int x = 0; x < field.width(); x++) {
				assertEquals(1.0, field.areaDistortion(x, y), EPS);
				assertEquals(1.0, field.anisotropy(x, y), EPS);
				assertTrue(field.isValid(x, y));
			}
		}
		assertEquals(1.0, field.validFraction(), EPS);
	}

	@Test
	public void affineTransformProducesSpatiallyConstantMetrics() {
		final AffineTransform2D affine = new AffineTransform2D();
		affine.set(2.0, 0.3, 1.0, 0.0, 1.5, -0.5);
		final AuditableTransformField2D auditor = new AuditableTransformField2D(new RealTransformFiniteDerivatives(affine));
		final TransformAuditField2D field = auditor.sample(9, 9, -1.0, 1.0, -1.0, 1.0);
		final double area = field.areaDistortion(0, 0), kappa = field.anisotropy(0, 0);
		for (int y = 0; y < field.height(); y++) {
			for (int x = 0; x < field.width(); x++) {
				assertEquals(area, field.areaDistortion(x, y), EPS);
				assertEquals(kappa, field.anisotropy(x, y), EPS);
				assertTrue(field.isValid(x, y));
			}
		}
		assertEquals(3.0, area, EPS);
		assertTrue(kappa >= 1.0);
	}

	@Test
	public void thinPlateSplineProducesSpatialVariation() {
		final double[][] source = { { 0.0, 1.0, 0.0, 1.0, 0.5 }, { 0.0, 0.0, 1.0, 1.0, 0.5 } };
		final double[][] target = { { 0.0, 1.0, 0.0, 1.0, 0.65 }, { 0.0, 0.0, 1.0, 1.0, 0.5 } };
		final AuditableTransformField2D auditor = new AuditableTransformField2D(new ThinplateSplineTransform(source, target));
		final TransformAuditField2D field = auditor.sample(11, 11, 0.0, 1.0, 0.0, 1.0);
		final double dCenter = auditor.areaDistortion(0.5, 0.5), dCorner = auditor.areaDistortion(0.0, 0.0);
		assertTrue(Double.isFinite(dCenter) && Double.isFinite(dCorner));
		assertTrue(Math.abs(dCenter - dCorner) > EPS);
		assertTrue(field.maxAreaDistortion() - field.minAreaDistortion() > EPS);
	}

	@Test
	public void validityThresholdAltersValidityResult() {
		final AffineTransform2D affine = new AffineTransform2D();
		affine.scale(100.0, 1.0);
		final AuditableTransformField2D strictAuditor = new AuditableTransformField2D(new RealTransformFiniteDerivatives(affine), 50.0);
		final AuditableTransformField2D relaxedAuditor = new AuditableTransformField2D(new RealTransformFiniteDerivatives(affine), 150.0);
		assertEquals(0.0, strictAuditor.sample(3, 3, 0.0, 1.0, 0.0, 1.0).validFraction(), EPS);
		assertEquals(1.0, relaxedAuditor.sample(3, 3, 0.0, 1.0, 0.0, 1.0).validFraction(), EPS);
	}

	@Test
	public void singularJacobianReturnsInfiniteAnisotropyAndInvalid() {
		final DifferentiableRealTransform singularTransform = new AbstractDifferentiableRealTransform() {
			@Override public int numSourceDimensions() { return 2; }
			@Override public int numTargetDimensions() { return 2; }
			@Override public void apply(double[] src, double[] tgt) { tgt[0] = 0.0; tgt[1] = 2.0 * src[1]; }
			@Override public void apply(float[] src, float[] tgt) { tgt[0] = 0.0f; tgt[1] = 2.0f * src[1]; }
			@Override public void apply(RealLocalizable src, RealPositionable tgt) {
				tgt.setPosition(0.0, 0);
				tgt.setPosition(2.0 * src.getDoublePosition(1), 1);
			}
			@Override public AffineTransform jacobian(double[] position) {
				final AffineTransform j = new AffineTransform(2) {
					@Override protected void invert() {}
				};
				j.set(0.0, 0.0, 0.0, 0.0, 2.0, 0.0);
				return j;
			}
			@Override public DifferentiableRealTransform copy() { return this; }
		};
		final TransformAuditField2D field = new AuditableTransformField2D(singularTransform).sample(3, 3, 0.0, 1.0, 0.0, 1.0);
		assertEquals(0.0, field.areaDistortion(0, 0), EPS);
		assertEquals(Double.POSITIVE_INFINITY, field.anisotropy(0, 0), 0.0);
		assertFalse(field.isValid(0, 0));
		assertEquals(0.0, field.validFraction(), EPS);
	}

	@Test
	public void defensiveImmutabilityPreventsExternalMutation() {
		final double[] area = { 1.0, 2.0, 3.0, 4.0 }, kappa = { 1.0, 1.5, 2.0, 2.5 };
		final boolean[] valid = { true, true, true, true };
		final TransformAuditField2D field = new TransformAuditField2D(2, 2, 0.0, 1.0, 0.0, 1.0, 100.0, area, kappa, valid);
		area[0] = 999.0; kappa[0] = 999.0; valid[0] = false;
		assertEquals(1.0, field.areaDistortion(0, 0), EPS);
		assertEquals(1.0, field.anisotropy(0, 0), EPS);
		assertTrue(field.isValid(0, 0));
		field.areaDistortion()[0] = 888.0;
		assertEquals(1.0, field.areaDistortion(0, 0), EPS);
	}

	@Test
	public void samplingSinglePointGridWorksCorrectly() {
		final AffineTransform2D affine = new AffineTransform2D();
		affine.scale(2.0, 3.0);
		final TransformAuditField2D field = new AuditableTransformField2D(new RealTransformFiniteDerivatives(affine)).sample(1, 1, 0.0, 2.0, 0.0, 2.0);
		assertEquals(1, field.width()); assertEquals(1, field.height());
		assertEquals(6.0, field.areaDistortion(0, 0), EPS);
		assertEquals(1.5, field.anisotropy(0, 0), EPS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void samplingInvalidWidthThrowsException() {
		new AuditableTransformField2D(new RealTransformFiniteDerivatives(new AffineTransform2D())).sample(0, 5, 0.0, 1.0, 0.0, 1.0);
	}

	@Test
	public void aggregateStatisticsReturnExpectedValues() {
		final AffineTransform2D affine = new AffineTransform2D();
		affine.scale(2.0, 4.0);
		final TransformAuditField2D field = new AuditableTransformField2D(new RealTransformFiniteDerivatives(affine)).sample(3, 3, 0.0, 1.0, 0.0, 1.0);
		assertEquals(1.0, field.validFraction(), EPS);
		assertEquals(8.0, field.meanAreaDistortion(), EPS);
		assertEquals(8.0, field.minAreaDistortion(), EPS);
		assertEquals(8.0, field.maxAreaDistortion(), EPS);
		assertEquals(2.0, field.maxAnisotropy(), EPS);
	}
}
