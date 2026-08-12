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

import net.imagej.ops.Op;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * ImageJ Op that audits a 2D differentiable transformation across a spatial grid.
 *
 * @author ImageJ2 Developers
 */
@Plugin(type = Op.class, name = "transform.audit")
public class AuditTransformOp extends AbstractUnaryFunctionOp<DifferentiableRealTransform, TransformAuditField2D> {

	@Parameter(required = false)
	private int width = 11;

	@Parameter(required = false)
	private int height = 11;

	@Parameter(required = false)
	private double minX = 0.0, maxX = 1.0;

	@Parameter(required = false)
	private double minY = 0.0, maxY = 1.0;

	@Parameter(required = false)
	private double kappaMax = AuditableTransformField2D.DEFAULT_KAPPA_MAX;

	@Override
	public TransformAuditField2D calculate(final DifferentiableRealTransform transform) {
		return new AuditableTransformField2D(transform, kappaMax).sample(width, height, minX, maxX, minY, maxY);
	}
}
