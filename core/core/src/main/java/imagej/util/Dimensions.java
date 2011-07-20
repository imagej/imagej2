//
// Dimensions.java
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
 * General purpose methods.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public final class Dimensions {

	private Dimensions() {
		// prevent instantiation of utility class
	}

	/** Gets the last n-2 dimensions of a n-dimensional long array. */
	public static long[] getDims3AndGreater(final long[] lengths) {
		if (lengths.length < 2) throw new IllegalArgumentException(
			"Image must be at least 2-D");

		final long[] extraDims = new long[lengths.length - 2];

		for (int i = 0; i < extraDims.length; i++)
			extraDims[i] = lengths[i + 2];

		return extraDims;
	}

	/**
	 * Throws an exception if the combination of origins and spans is outside an
	 * image's dimensions.
	 */
	public static void verifyDimensions(final long[] lengths,
		final long[] origin, final long[] span)
	{
		// span dims should match origin dims
		if (origin.length != span.length) {
			throw new IllegalArgumentException("origin (dim=" + origin.length +
				") and span (dim=" + span.length + ") arrays are of differing sizes");
		}

		// origin/span dimensions should match image dimensions
		if (origin.length != lengths.length) {
			throw new IllegalArgumentException("origin/span (dim=" + origin.length +
				") different size than input image (dim=" + lengths.length + ")");
		}

		// make sure origin in a valid range, within bounds of source image
		for (int i = 0; i < origin.length; i++) {
			if ((origin[i] < 0) || (origin[i] >= lengths[i])) {
				throw new IllegalArgumentException(
					"origin outside bounds of input image at index " + i);
			}
		}

		// make sure span in a valid range, >= 1
		for (int i = 0; i < span.length; i++) {
			if (span[i] < 1) {
				throw new IllegalArgumentException("span size < 1 at index " + i);
			}
		}

		// make sure origin + span within the bounds of the input image
		for (int i = 0; i < span.length; i++) {
			if ((origin[i] + span[i]) > lengths[i]) {
				throw new IllegalArgumentException("span range " +
					"(origin+span) beyond input image boundaries at index " + i);
			}
		}
	}

	/**
	 * Given an array of dimensions, this gets the number of axes whose dimension
	 * is > 1
	 */
	public static long countNontrivialDimensions(final long[] dimensions) {
		int numNonTrivial = 0;

		for (final long dim : dimensions)
			if (dim > 1) numNonTrivial++;

		return numNonTrivial;
	}

}
