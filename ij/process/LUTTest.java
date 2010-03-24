package ij.process;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.Assert;

import java.awt.image.IndexColorModel;

import ij.IJInfo;

public class LUTTest {

	LUT lut;
	byte[] reds, blues, greens;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private byte[] bytes(int number, int value)
	{
		byte[] bytes = new byte[number];
		
		for (int i = 0; i < number; i++)
			bytes[i] = (byte)value;
		
		return bytes;
	}
	
	@Test
	public void testLUTByteArrayByteArrayByteArray() {

		byte[] temp = new byte[256];
		
		reds = bytes(256,23);
		greens = bytes(256,47);
		blues = bytes(256,88);
		
		lut = new LUT(reds,greens,blues);
		assertNotNull(lut);
		
		lut.getReds(temp);
		assertArrayEquals(reds,temp);
		
		lut.getBlues(temp);
		assertArrayEquals(blues,temp);
		
		lut.getGreens(temp);
		assertArrayEquals(greens,temp);

		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// in existing IJ code this constructor crashes when passed less than 256 entries for r,g,b 
			reds = bytes(255,23);
			greens = bytes(255,47);
			blues = bytes(255,88);
			lut = new LUT(reds,greens,blues);
			assertNotNull(lut);
		}
		
		// try passing too many lut entries
		
		reds = bytes(257,23);
		greens = bytes(257,47);
		blues = bytes(257,88);
		lut = new LUT(reds,greens,blues);
		assertNotNull(lut);
		
		lut.getReds(temp);
		assertArrayEquals(bytes(256,23),temp);
		
		lut.getGreens(temp);
		assertArrayEquals(bytes(256,47),temp);
		
		lut.getBlues(temp);
		assertArrayEquals(bytes(256,88),temp);
	}

	private void shouldBeIllegalArgument(int bits, int size)
	{
		try{
			lut = new LUT(bits,size,bytes(size,31),bytes(size,88),bytes(size,104));
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}

	private void shouldBeOkay(int bits, int size)
	{
		byte[] reds = bytes(size,31);
		byte[] blues = bytes(size,88);
		byte[] greens = bytes(size,104);
		byte[] temp = new byte[size];
		LUT lut = new LUT(bits,size,reds,greens,blues);
		assertNotNull(lut);
		lut.getReds(temp);
		assertArrayEquals(reds,temp);
		lut.getGreens(temp);
		assertArrayEquals(greens,temp);
		lut.getBlues(temp);
		assertArrayEquals(blues,temp);
	}
	
	@Test
	public void testLUTIntIntByteArrayByteArrayByteArray() {
		// constructor(bits, size, r, g, b)

		// negative num bits
		shouldBeIllegalArgument(-1,1);
		shouldBeIllegalArgument(-1,2);
		shouldBeIllegalArgument(-1,4);
		shouldBeIllegalArgument(-1,8);
		
		// zero num bits
		shouldBeIllegalArgument(0,1);
		shouldBeIllegalArgument(0,2);
		shouldBeIllegalArgument(0,4);
		shouldBeIllegalArgument(0,8);
		
		// 0 label lengths
		shouldBeIllegalArgument(1,0);

		// edges cases that should work
		shouldBeOkay(1,1);
		shouldBeOkay(1,2);
		shouldBeOkay(1,4);
		shouldBeOkay(1,8);
		shouldBeOkay(2,1); // surprise it works
		shouldBeOkay(2,2); // surprise it works
		shouldBeOkay(2,4);
		shouldBeOkay(2,8);
		
		// what if r,g,b's do not match size? done below
		
		// more r,g,b's than 2^numbits
		
		reds = bytes(17,45);
		blues = bytes(17,58);
		greens = bytes(17,99);
		lut = new LUT(4,17,reds,blues,greens);
		assertNotNull(lut);
		
		// less r,g,b's than 2^numbits
		
		reds = bytes(15,45);
		blues = bytes(15,58);
		greens = bytes(15,99);
		lut = new LUT(4,15,reds,blues,greens);
		assertNotNull(lut);
		
		// what if r,g,b's are all different sizes?
		
		reds = bytes(12,45);
		blues = bytes(14,58);
		greens = bytes(16,99);

		// this one works - underlying code only uses the 12 that all define
		lut = new LUT(4,12,reds,blues,greens);
		assertNotNull(lut);
		
		// this one fails - underlying code expects 14 of them
		try {
			lut = new LUT(4,14,reds,blues,greens);
			fail();
		} catch (ArrayIndexOutOfBoundsException e)
		{
			assertTrue(true);
		}

		// this one fails - underlying code expects 16 of them
		try {
			lut = new LUT(4,16,reds,blues,greens);
			fail();
		} catch (ArrayIndexOutOfBoundsException e)
		{
			assertTrue(true);
		}
	}

	@Test
	public void testLUTIndexColorModelDoubleDouble() {

		// need to assume IndexColorModel correctly constructed as that's Java's job to verify
		// therefore there is really nothing to test except that it works
		// and that min and max are correct

		int size = 5;
		byte[] temp = new byte[size];
		reds = bytes(size,101);
		blues = bytes(size,44);
		greens = bytes(size,86);

		IndexColorModel cm = new IndexColorModel(2,size,reds,greens,blues);
		
		lut = new LUT(cm,1.0,1000.0);
		assertNotNull(lut);

		lut.getReds(temp);
		assertArrayEquals(reds,temp);
		
		lut.getGreens(temp);
		assertArrayEquals(greens,temp);
		
		lut.getBlues(temp);
		assertArrayEquals(blues,temp);
		
		assertEquals(1.0,lut.min,Assert.DOUBLE_TOL);
		assertEquals(1000.0,lut.max,Assert.DOUBLE_TOL);

		// min and max reversed
		lut = new LUT(cm,1000.0,1.0);
		assertEquals(1000.0,lut.min,Assert.DOUBLE_TOL);
		assertEquals(1.0,lut.max,Assert.DOUBLE_TOL);
		
		// should throw an exception if passed a null colormodel
		try {
			lut = new LUT(null,1,1000);
			fail();
		} catch(NullPointerException e)
		{
			assertTrue(true);
		}
	}

	@Test
	public void testGetBytes() {

		byte[] results;
		IndexColorModel cm;
		
		reds = bytes(256,12);
		greens = bytes(256,33);
		blues = bytes(256,50);

		// null if size != 256:
		cm = new IndexColorModel(8,255,reds,greens,blues);
		lut = new LUT(cm,0,5);
		results = lut.getBytes();
		assertNull(results);
		
		// 768 r's,g's,b's if size == 256
		cm = new IndexColorModel(8,256,reds,greens,blues);
		lut = new LUT(cm,0,5);
		results = lut.getBytes();
		assertNotNull(results);
		
		byte[] temp = new byte[256];
		
		System.arraycopy(results, 0, temp, 0, 256);
		assertArrayEquals(reds,temp);
		
		System.arraycopy(results, 256, temp, 0, 256);
		assertArrayEquals(greens,temp);
		
		System.arraycopy(results, 512, temp, 0, 256);
		assertArrayEquals(blues,temp);
	}

	private byte[] bytesAscending(int number)
	{
		byte[] results = new byte[number];
		
		for (int i = 0; i < number; i++)
			results[i] = (byte) i;
		return results;
	}
	
	private byte[] bytesDescending(int number)
	{
		byte[] results = new byte[number];
		
		for (int i = 0; i < number; i++)
			results[i] = (byte) (number-1-i);
		return results;
	}
	
	@Test
	public void testCreateInvertedLut() {
		
		reds = bytesAscending(256);
		blues = bytesAscending(256);
		greens = bytesAscending(256);
		
		lut = new LUT(reds,greens,blues);
		LUT invLut = lut.createInvertedLut();
		
		byte[] temp = new byte[256];
		byte[] invBytes = bytesDescending(256);
		
		invLut.getReds(temp);
		assertArrayEquals(invBytes,temp);

		invLut.getGreens(temp);
		assertArrayEquals(invBytes,temp);

		invLut.getBlues(temp);
		assertArrayEquals(invBytes,temp);
	}

	@Test
	public void testClone() {
		
		reds = bytesAscending(256);
		blues = bytesDescending(256);  // descend
		greens = bytesAscending(256);
		
		lut = new LUT(reds,greens,blues);
		
		LUT newLut = (LUT) lut.clone();
		
		assertEquals(lut,newLut);
	}

	@Test
	public void testMinAndMax() {
		reds = bytes(12,45);
		blues = bytes(12,58);
		greens = bytes(12,99);
		lut = new LUT(4,12,reds,blues,greens);
		assertEquals(0.0,lut.min,Assert.DOUBLE_TOL);
		assertEquals(0.0,lut.max,Assert.DOUBLE_TOL);
		
		// check their values after they are set by other constructors
		IndexColorModel cm = new IndexColorModel(4,12,reds,greens,blues);
		
		lut = new LUT(cm,66.34,Double.MAX_VALUE);
		assertNotNull(lut);
		assertEquals(66.34,lut.min,Assert.DOUBLE_TOL);
		assertEquals(Double.MAX_VALUE,lut.max,Assert.DOUBLE_TOL);
	}

}
