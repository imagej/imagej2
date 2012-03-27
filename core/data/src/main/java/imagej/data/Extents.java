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

package imagej.data;

import net.imglib2.AbstractInterval;

/**
 * This class wraps a pair of min and max dimensions encoded as long[]'s. It
 * facilitates the creation of {@link Position} indexes for iterating within the
 * Extents.
 * 
 * @author Barry DeZonia
 */
public class Extents extends AbstractInterval {

	private final long numElements;

	/**
	 * Constructor that takes a dimensional extent and sets min to a zero origin
	 * and each max dim i to dims[i]-1.
	 */
	public Extents(final long[] dims) {
		super(dims);
		numElements = calcNumElements();
	}

	/**
	 * Constructor that takes min and max extents. No checking is done that min <=
	 * max for all dim i.
	 */
	public Extents(final long[] min, final long[] max) {
		super(min, max);
		numElements = calcNumElements();
	}

	/**
	 * Returns a {@link Position} object that can be used to iterate these
	 * Extents.
	 */
	public Position createPosition() {
		return new Position(this);
	}

	/**
	 * Returns the total number of elements spanned by the parent {@link Extents}.
	 */
	public long numElements() {
		return numElements;
	}

//	/**
//	 * Returns a long[] containing a subset of the dimensions. The dimensions to
//	 * choose are identified by offset and length. Throws exceptions when offset
//	 * or length are invalid.
//	 */
//	public long[] subdimensions(final int offset, final int length) {
//		if ((offset < 0) || (offset >= numDimensions())) {
//			throw new IllegalArgumentException("bad offset");
//		}
//		if ((length < 0) || (offset + length > numDimensions())) {
//			throw new IllegalArgumentException("bad length");
//		}
//		final long[] subDims = new long[length];
//		for (int i = 0; i < length; i++)
//			subDims[i] = dimension(i + offset);
//		return subDims;
//	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		for (int d = 0; d < numDimensions(); d++) {
			sb.append(" " + dimension(d));
		}
		sb.append(" }");
		return sb.toString();
	}

	// -- private helpers --

	private long calcNumElements() {
		if (min.length == 0) return 0;
		long elements = 1;
		for (int i = 0; i < min.length; i++)
			elements *= dimension(i);
		return elements;
	}
}
