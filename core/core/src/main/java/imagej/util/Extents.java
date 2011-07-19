//
// Extents.java
//

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
 * This class wraps a set of dimensions encoded as a long[]. It facilitates
 * the creation of {@link Position} indexes for iterating within the Extents.
 * 
 * @author Barry DeZonia
 *
 */
public class Extents {

	private final long[] extents;
	private final long numElements;

	/**
	 * Constructor - takes a given set of dimensions. Clones them on input.
	 */
	public Extents(long[] dims) {
		extents = dims.clone();
		long elements;
		if (dims.length == 0)
			elements = 0;
		else {
			elements = 1;
			for (long dimSize : extents)
				elements *= dimSize;
		}
		numElements = elements;
	}

	/**
	 * Returns a {@link Position} object that can be used to iterate these
	 * Extents.
	 */
	public Position createPosition() {
		return new Position(this);
	}

	/**
	 * Reports the number of dimensions spanned by parent {@link Extents}.
	 */
	public int numDimensions() {
		return extents.length;
	}

	/**
	 * Returns the size of dimension number i within the parent {@link Extents}.
	 */
	public long dimension(int i) {
		return extents[i];
	}

	/**
	 * Fills a given dimension array with the parent {@link Extents} values.
	 */
	public void dimensions(long[] outputExts) {
		for (int i = 0; i < extents.length; i++)
			outputExts[i] = extents[i];
	}

	/**
	 * Returns the total number of elements spanned by the parent {@link Extents}.
	 */
	public long numElements() {
		return numElements;
	}
}
