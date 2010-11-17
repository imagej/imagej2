package ij;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

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
