//
// Assert.java
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

package ij;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * Helper methods for unit testing assertions.
 *
 * @author Barry DeZonia
 */
public class Assert {
	
	public static final float FLOAT_TOL = 0.00001f;
	public static final double DOUBLE_TOL = 0.00001;
	
	public static void assertFloatArraysEqual(float[] expected, float[] actual, float tolerance)
	{
		if (expected == actual)
			return;

		if (expected == null)
			fail("Assert.assertFloatArraysEqual(float[],float[]) passed in null data for first parameter");

		if (actual == null)
			fail("Assert.assertFloatArraysEqual(float[],float[]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("Assert.assertFloatArraysEqual(float[],float[]) array lengths differ: expected "+expected.length + " and got " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			if (Math.abs(expected[i]-actual[i]) > tolerance)
				fail("Assert.assertFloatArraysEqual(float[],float[]) items differ at index " + i + ": expected "+ expected[i] + " and got " + actual[i]);
	}
	
	public static void assertDoubleArraysEqual(double[] expected, double[] actual, double tolerance)
	{
		if (expected == actual)
			return;

		if (expected == null)
			fail("Assert.assertDoubleArraysEqual(double[],double[]) passed in null data for first parameter");

		if (actual == null)
			fail("Assert.assertDoubleArraysEqual(double[],double[]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("Assert.assertDoubleArraysEqual(double[],double[]) array lengths differ: expected "+expected.length + " and got " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			if (Math.abs(expected[i]-actual[i]) > tolerance)
				fail("Assert.assertDoubleArraysEqual(double[],double[]) items differ at index " + i + ": expected "+ expected[i] + " and got " + actual[i]);
	}
	
	public static void assertArraysSame(Object expected, Object actual)
	{
		if (expected == actual)
			return;
		
		if ((expected == null) || (actual == null))
			fail("Assert.assertArraysSame() one argument is null : (expcted=" + expected + ",actual=" + actual + ")");
		
		Class<?> aClass = expected.getClass();
		Class<?> bClass = actual.getClass();
		
		if (aClass != bClass)
			fail("Assert.assertArraysSame() passed incompatible Objects : (" + aClass.getName() + "," + bClass.getName() + ")");
		
		if (expected instanceof byte[])
			assertArrayEquals((byte[])expected,(byte[])actual);
		else if (expected instanceof short[])
			assertArrayEquals((short[])expected,(short[])actual);
		else if (expected instanceof int[])
			assertArrayEquals((int[])expected,(int[])actual);
		else if (expected instanceof long[])
			assertArrayEquals((long[])expected,(long[])actual);
		else if (expected instanceof short[][])
			assertArrayEquals((short[][])expected,(short[][])actual);
		else if (expected instanceof Object[])
			assertArrayEquals((Object[])expected,(Object[])actual);
		else if (expected instanceof float[])
			assertFloatArraysEqual((float[])expected,(float[])actual,FLOAT_TOL);  // JUnit does not have a float[] method
		else
			fail("Assert.assertArraysSame() passed unsupported data format type : (" + aClass.getName() + ")");
	}
	
}
