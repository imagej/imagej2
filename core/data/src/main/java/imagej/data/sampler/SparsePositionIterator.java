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

package imagej.data.sampler;

import java.util.List;

/**
 * This class is a PositionIterator that iterates the potentially noncontiguous
 * region of space present in the input of a sampling of an image. It has
 * package level sharing and access.
 * 
 * @author Barry DeZonia
 */
class SparsePositionIterator implements PositionIterator {

	// -- instance variables --

	private final int[] maxIndexes;
	private final int[] indexes;
	private final List<List<Long>> actualValues;
	private final long[] currPos;

	// -- constructor --

	/**
	 * Creates a SparsePositionIterator from a SamplingDefinition. The space to be
	 * iterated is the input space of a sampling. It may not be contiguous.
	 */
	SparsePositionIterator(final SamplingDefinition def) {
		actualValues = def.getInputRanges();
		maxIndexes = calcMaxes(def);
		currPos = new long[maxIndexes.length];
		for (int i = 0; i < currPos.length; i++)
			currPos[i] = actualValues.get(i).get(0);
		indexes = new int[maxIndexes.length];
		indexes[0] = -1;
	}

	// -- public interface --

	/** Returns true if the iterator has a next position in the input space. */
	@Override
	public boolean hasNext() {
		for (int i = 0; i < currPos.length; i++) {
			if (indexes[i] < maxIndexes[i]) return true;
		}
		return false;
	}

	/** Returns the next position of the input space. */
	@Override
	public long[] next() {
		for (int i = 0; i < indexes.length; i++) {
			final int nextPos = indexes[i] + 1;
			if (nextPos <= maxIndexes[i]) {
				indexes[i] = nextPos;
				currPos[i] = actualValues.get(i).get(nextPos);
				return currPos;
			}
			indexes[i] = 0;
			currPos[i] = actualValues.get(i).get(0);
		}
		throw new IllegalArgumentException("Can't position iterator beyond end");
	}

	// -- private helpers --

	/** Determines the maximum values that each axis can take. */
	private int[] calcMaxes(
		@SuppressWarnings("unused") final SamplingDefinition def)
	{
		final int[] mx = new int[actualValues.size()];
		for (int i = 0; i < mx.length; i++) {
			mx[i] = actualValues.get(i).size() - 1;
		}
		return mx;
	}
}
