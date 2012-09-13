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

// TODO - move to Imglib

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
public class WithinRadiusOfPointCondition implements Condition<long[]> {

	private final long[] point;
	private final long radius;
	private final long radiusSquared;
	
	public WithinRadiusOfPointCondition(long radius, long[] point) {
		this.point = point;
		this.radius = radius;
		this.radiusSquared = radius * radius;
	}
	
	@Override
	public boolean isTrue(long[] val) {
		/* old debugging code
		if (!Arrays.equals(point,lastPt)) {
			for (int i = 0; i < point.length; i++) {
				lastPt[i] = point[i];
			}
		}
		*/
		long sumSq = 0;
		for (int i = 0; i < val.length; i++) {
			long delta = val[i] - point[i];
			sumSq += delta * delta;
		}
		return sumSq <= radiusSquared;
	}

	@Override
	public WithinRadiusOfPointCondition copy() {
		// NOTE - purposely not cloning point here. Not sure what is best. But this
		// allows one to have numerous conditions all comparing to a single point
		// stored elsewhere. That could or could not be bad depending upon the
		// circumstance. For instance you may have one single point that you want to
		// update in one place and have all conditions respect it. But you also may
		// have the case where you want one point per subspace in which the condition
		// applies (like the center of a neighborhood). There could be multiple
		// subspaces because of copy()'s called by parallelization code. This idea
		// needs to be thought out more carefully. Maybe use a nonparallel assigner
		// in cases like this.
		return new WithinRadiusOfPointCondition(radius, point);
	}
	
}
