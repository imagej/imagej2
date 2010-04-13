//
// DirectNIOTest.java
//

package src.test;

import java.nio.*;

/**
 * Tests using NIO data buffers versus regular Java arrays.
 *
 * Looks like NIO buffers are not part of Java's heap...
 */
public class DirectNIOTest {

	// -- Constants --

	public static final int ITERATIONS = 3;
	public static final int DEFAULT_SIZE = 10000000;
	public static final int BPP = 4;

	// -- Fields --

	private int size;

	// -- Constructor --

	public DirectNIOTest(int elements) {
		size = elements;
	}

	// -- DirectNIOTest methods --

	/** Tests space and time performance of direct NIO buffers vs Java arrays. */
	public void testPerformance() {
		for (int i=0; i<ITERATIONS; i++) {
			testArray();
			testNIO();
		}
	}

	/** Runs some space, time and correctness tests with a direct NIO buffer. */
	public void testNIO() {
		// allocation
		long mem = getMem();
		long time = getTime();
		FloatBuffer data = ByteBuffer.allocateDirect(size * BPP).order(
			ByteOrder.nativeOrder()).asFloatBuffer();
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testNIO: allocation", mem, time);

		// assignment (Fibonacci)
		mem = getMem();
		time = getTime();
		int v1 = 1, v2 = 0;
		for (int i=0; i<size; i++) {
			int value = v1 + v2;
			data.put(i, value);
			v2 = value;
			v1 = v2;
		}
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testNIO: assignment", mem, time);

		// access
		mem = getMem();
		time = getTime();
		v1 = 1;
		v2 = 0;
		for (int i=0; i<size; i++) {
			int expected = v1 + v2;
			float actual = data.get(i);
			if (actual != expected) {
				throw new IllegalStateException("Element #" + i + ": " +
					"expected = " + expected + " but actual = " + actual);
			}
			v2 = expected;
			v1 = v2;
		}
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testNIO: access", mem, time);

		boolean hasArray = data.hasArray();
		boolean arrayOK = true;
		try {
			data.array();
		}
		catch (UnsupportedOperationException exc) {
			arrayOK = false;
		}
		System.out.println("testNIO: " +
			"hasArray? " + hasArray + ", arrayOK? " + arrayOK);

		// garbage collect
		mem = getMem();
		time = getTime();
		data.put(0, 0); // fool JIT a little
		data = null;
		System.gc();
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testArray: garbage collect", mem, time);

		System.out.println();
	}

	/** Runs some space, time and correctness tests with a Java array. */
	public void testArray() {
		// allocation
		long mem = getMem();
		long time = getTime();
		float[] data = new float[size];
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testArray: allocation", mem, time);

		// assignment (Fibonacci)
		mem = getMem();
		time = getTime();
		int v1 = 1, v2 = 0;
		for (int i=0; i<size; i++) {
			int value = v1 + v2;
			data[i] = value;
			v2 = value;
			v1 = v2;
		}
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testArray: assignment", mem, time);

		// access
		mem = getMem();
		time = getTime();
		v1 = 1;
		v2 = 0;
		for (int i=0; i<size; i++) {
			int expected = v1 + v2;
			float actual = data[i];
			if (actual != expected) {
				throw new IllegalStateException("Element #" + i + ": " +
					"expected = " + expected + " but actual = " + actual);
			}
			v2 = expected;
			v1 = v2;
		}
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testArray: access", mem, time);

		// garbage collect
		mem = getMem();
		time = getTime();
		data[0] = 0; // fool JIT a little
		data = null;
		System.gc();
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testArray: garbage collect", mem, time);

		System.out.println();
	}

	/**
	 * Tests some characteristics of NIO buffer conversion
	 * between ByteBuffer and FloatBuffer.
	 */
	public void testConversion() {
		// allocate
		long mem = getMem();
		long time = getTime();
		ByteBuffer data =
			ByteBuffer.allocate(size * BPP).order(ByteOrder.BIG_ENDIAN);
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testConversion: allocation", mem, time);

		// assignment (Fibonacci)
		mem = getMem();
		time = getTime();
		int v1 = 1, v2 = 0;
		for (int i=0; i<size/4; i++) {
			int i0 = 4 * i;
			int i1 = i0 + 1;
			int i2 = i0 + 2;
			int i3 = i0 + 3;
			int value = v1 + v2;
			float fval = (float) value;
			int bits = Float.floatToRawIntBits(fval);
			int b0 = (bits >> 24) & 0xff;
			int b1 = (bits >> 16) & 0xff;
			int b2 = (bits >> 8) & 0xff;
			int b3 = bits & 0xff;
			data.put(i0, (byte) b0);
			data.put(i1, (byte) b1);
			data.put(i2, (byte) b2);
			data.put(i3, (byte) b3);
			v1 = value;
			v2 = v1;
		}
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testConversion: assignment", mem, time);

		// conversion
		mem = getMem();
		time = getTime();
		FloatBuffer floatData = data.asFloatBuffer();
		time = getTime(time);
		mem = getMem(mem);
		reportStats("testConversion: conversion", mem, time);

		// read back float data
		boolean hasByteArray = data.hasArray();
		boolean hasFloatArray = floatData.hasArray();
		System.out.println("testConversion: byteArray? " +
			hasByteArray + ", floatArray? " + hasFloatArray);

		System.out.println();
	}

	// -- Helper methods --

	private void reportStats(String prefix, long mem, long time) {
		float bpp = (float) mem / size;
		System.out.println(prefix + ": " +
			bpp + " bytes per element, " + time + " ms");
	}

	private long getMem() {
		Runtime r = Runtime.getRuntime();
		long total = r.totalMemory();
		long free = r.freeMemory();
		return total - free;
	}

	private long getMem(long before) {
		return getMem() - before;
	}

	private long getTime() {
		return System.currentTimeMillis();
	}

	private long getTime(long before) {
		return getTime() - before;
	}

	// -- Main method --

	public static void main(String[] args) {
		int size = DEFAULT_SIZE;
		if (args.length > 0) size = Integer.parseInt(args[0]);

		DirectNIOTest test = new DirectNIOTest(size);
		test.testPerformance();

		test.testConversion();
	}

}
