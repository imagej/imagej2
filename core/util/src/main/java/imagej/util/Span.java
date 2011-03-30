//
// Span.java
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
 * A helper class that supports getting ranges within n-dimensional data sets.
 *
 * @author Barry DeZonia
 */
public final class Span {
		
	private Span() {
		// prevent instantiation of utility class
	}
	
	/** create a span array of length numDims initialized to zeroes */
	public static int[] create(int numDims)
	{
		return new int[numDims];
	}
	
	/** create a span array initialized to passed in values */
	public static int[] create(int[] initialValues)
	{
		return initialValues.clone();
	}
	
	/** create a span array that encompasses one plane of dimension width by height and all other dimensions at 1 */
	public static int[] singlePlane(int width, int height, int totalDims)
	{
		if (width < 1)
			throw new IllegalArgumentException("plane must have width > 0: passed width of "+width);
			
		if (height < 1)
			throw new IllegalArgumentException("plane must have height > 0: passed width of "+height);

		if (totalDims < 2)
			throw new IllegalArgumentException("plane must have at least 2 dimensions: passed "+totalDims+" dimensions");
		
		int[] values = new int[totalDims];
		
		values[0] = width;
		values[1] = height;
		
		for (int i = 2; i < totalDims; i++)
			values[i] = 1;
		
		return values;
	}
	
	/** create a span array that encompasses the whole range of a set of dimensions */
	public static int[] wholeRange(int[] dimensions)
	{
		return dimensions.clone();
	}
}
