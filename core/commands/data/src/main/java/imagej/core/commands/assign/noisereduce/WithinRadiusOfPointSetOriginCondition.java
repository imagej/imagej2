/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.commands.assign.noisereduce;

import net.imglib2.ops.condition.Condition;
import net.imglib2.ops.pointset.PointSet;

// TODO - move to Imglib

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
public class WithinRadiusOfPointSetOriginCondition implements Condition<long[]>
{

	private final PointSet pointSet;
	private final long radius;
	private final long radiusSquared;

	public WithinRadiusOfPointSetOriginCondition(final long radius,
		final PointSet pointSet)
	{
		this.pointSet = pointSet;
		this.radius = radius;
		this.radiusSquared = radius * radius;
	}

	@Override
	public boolean isTrue(final long[] val) {
		final long[] origin = pointSet.getOrigin();
		long sumSq = 0;
		for (int i = 0; i < val.length; i++) {
			final long delta = val[i] - origin[i];
			sumSq += delta * delta;
		}
		return sumSq <= radiusSquared;
	}

	@Override
	public WithinRadiusOfPointSetOriginCondition copy() {
		// TODO - there is some question as to whether this should copy space or not
		// As it is I think this class only works with SerialImageAssignments. It
		// may misbehave when used with regular parallel ImageAssignments. However
		// it can also be used outside of assignment code as it is a valid condition
		return new WithinRadiusOfPointSetOriginCondition(radius, pointSet);
	}

}
