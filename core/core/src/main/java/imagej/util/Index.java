//
// Index.java
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
 * TODO
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public final class Index {

	private Index() {
		// prevent instantiation of utility class
	}

	/** create an index array of length numDims initialized to zeroes */
	public static long[] create(final int numDims) {
		return new long[numDims];
	}

	/** create an index array initialized to passed in values */
	public static long[] create(final long[] initialValues) {
		return initialValues.clone();
	}

	/**
	 * create an index array setting the first 2 dims to x & y and the remaining
	 * dims populated with passed in values
	 */
	public static long[] create(final long x, final long y,
		final long[] planePosition)
	{
		if (x < 0) throw new IllegalArgumentException("x value must be >= 0");

		if (y < 0) throw new IllegalArgumentException("y value must be >= 0");

		final long[] values = new long[planePosition.length + 2];

		values[0] = x;
		values[1] = y;

		for (int i = 2; i < values.length; i++)
			values[i] = planePosition[i - 2];

		return values;
	}

	/**
	 * tells whether a position array is within the bounds specified via origin
	 * and span arrays
	 */
	public static boolean isValid(final long[] position, final long[] origin,
		final long[] span)
	{
		for (int i = 0; i < position.length; i++) {
			if (position[i] < origin[i]) return false;

			if (position[i] >= (origin[i] + span[i])) return false;
		}

		return true;
	}

	/**
	 * increment a position index within a range bounded via origin and spans.
	 * Increments indices from left to right
	 */
	// incrementing from left to right : not textbook but hacky way to get
	// ImgLibProcessor::duplicate() working
	public static void increment(final long[] position, final long[] origin,
		final long[] span)
	{
		// if (position.length == 0) // allow degenerate case to pass through this :
		// TODO - bad idea???
		// return;

		int i = 0;

		position[i]++;

		// if we're beyond end of this dimension
		while (position[i] >= (origin[i] + span[i])) {
			// if this dim is the last then we've gone as far as we can go
			if (i == position.length - 1) {
				// return a value that isValid() will complain about
				for (int j = 0; j < position.length; j++)
					position[j] = origin[j] + span[j];
				return;
			}

			// otherwise set our dim to its origin value and increment the dimension
			// to our right
			position[i] = origin[i];
			position[i + 1]++;
			i++;
		}
	}

	/*
	// incrementing from right to left
	public static void increment(long[] position, long[] origin, long[] span)
	{
		int i = position.length - 1;

		position[i]++;
			
		// if we're beyond end of this dimension
		while (position[i] >= (origin[i] + span[i]))
		{
			// if this dim is the first then we've gone as far as we can go
			if (i == 0)
			{
				// return a value that isValid() will complain about
				for (int j = 0; j < position.length; j++)
					position[j] = origin[j] + span[j];
				return;
			}
			
			// otherwise set our dim to its origin value and increment the dimension to our left
			position[i] = origin[i];
			position[i-1]++;
			i--;
		}		
	}
	*/

	/**
	 * Computes the 1-dimensional index corresponding to the given
	 * multidimensional position.
	 * 
	 * @param lengths the maximum value for each positional dimension
	 * @param indexND position along each dimensional axis
	 * @return 1-dimensional index value
	 */
	public static long indexNDto1D(final long[] lengths, final long[] indexND) {
		long offset = 1;
		long index1D = 0;
		for (int i = 0; i < indexND.length; i++) {
			index1D += offset * indexND[i];
			offset *= lengths[i];
		}
		return index1D;
	}

	/**
	 * Computes the multidimensional position corresponding to the given
	 * 1-dimensional index.
	 * 
	 * @param lengths the maximum value at each positional dimension
	 * @param index1D 1-dimensional index value
	 * @return position along each dimensional axis
	 */
	public static long[] index1DtoND(final long[] lengths, final long index1D) {
		return index1DtoND(lengths, index1D, new long[lengths.length]);
	}

	/**
	 * Computes the multidimensional position corresponding to the given
	 * 1-dimensional index.
	 * 
	 * @param lengths the maximum value at each positional dimension
	 * @param index1D rasterized index value
	 * @param indexND preallocated position array to populate with the result
	 * @return position along each dimensional axis
	 */
	public static long[] index1DtoND(final long[] lengths, final long index1D,
		final long[] indexND)
	{
		long offset = 1;
		long r = index1D;
		for (int i = 0; i < indexND.length; i++) {
			final long offset1 = offset * lengths[i];
			final long q = i < indexND.length - 1 ? r % offset1 : r;
			indexND[i] = q / offset;
			r -= q;
			offset = offset1;
		}
		return indexND;
	}

	/**
	 * Computes the total number of positions for a positional array with the
	 * given lengths.
	 */
	public static long getTotalLength(final long[] lengths) {
		return getTotalLength(lengths, 0);
	}

	/**
	 * Computes the total number of planes for a positional array with the
	 * given lengths. Essentially, this ignores the first two dimensions,
	 * because they are assumed to be width and height of a plane.
	 */
	public static long getPlaneCount(final long[] lengths) {
		return getTotalLength(lengths, 2);
	}

	/**
	 * Gets the sample index of a plane number within a data set of specified
	 * dimensions.
	 */
	public static long[] getPlanePosition(final long[] lengths,
		final long planeNumber)
	{
		if (planeNumber < 0 || planeNumber >= getPlaneCount(lengths)) {
			throw new IllegalArgumentException("invalid plane number given");
		}

		final int numDims = lengths.length;

		if (numDims < 2) {
			throw new IllegalArgumentException("requires at least a 2-D image");
		}

		if (numDims == 2) {
			if (planeNumber != 0) {
				throw new IllegalArgumentException("2-D image can only have 1 plane");
			}

			// TODO - this next step is a little scary to do. might need to throw
			// exception and have other places fix the fact
			// that we have a rows x cols x 1 image

			return new long[0];
		}

		final long[] planeDim = new long[lengths.length - 2];

		for (int i = 0; i < planeDim.length; i++)
			planeDim[i] = lengths[i + 2];

		final long[] position = new long[planeDim.length];

		Index.index1DtoND(planeDim, planeNumber, position);

		return position;
	}

	// -- Helper methods --

	/**
	 * Computes the total number of positions for a positional array with the
	 * given lengths.
	 */
	private static long getTotalLength(final long[] lengths, int startIndex) {
		long len = 1;
		for (int i = startIndex; i < lengths.length; i++) len *= lengths[i];
		return len;
	}


}
