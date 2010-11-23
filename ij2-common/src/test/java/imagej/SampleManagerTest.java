package imagej;

import static org.junit.Assert.*;
import org.junit.Test;

import imagej.SampleInfo;
import imagej.SampleManager;
import imagej.UserType;

public class SampleManagerTest {

	@Test
	public void testGetSampleInfo() 
	{
		SampleInfo info;
		
		info = SampleManager.getSampleInfo(UserType.BIT);
		assertEquals("1-bit unsigned",info.getName());
		assertEquals(1,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(1,info.getNumBits());
		assertEquals(UserType.BIT,info.getUserType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());
		
		info = SampleManager.getSampleInfo(UserType.BYTE);
		assertEquals("8-bit signed",info.getName());
		assertEquals(8,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(8,info.getNumBits());
		assertEquals(UserType.BYTE,info.getUserType());
		assertFalse(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.UBYTE);
		assertEquals("8-bit unsigned",info.getName());
		assertEquals(8,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(8,info.getNumBits());
		assertEquals(UserType.UBYTE,info.getUserType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.UINT12);
		assertEquals("12-bit unsigned",info.getName());
		assertEquals(12,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(12,info.getNumBits());
		assertEquals(UserType.UINT12,info.getUserType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.SHORT);
		assertEquals("16-bit signed",info.getName());
		assertEquals(16,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(16,info.getNumBits());
		assertEquals(UserType.SHORT,info.getUserType());
		assertFalse(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.USHORT);
		assertEquals("16-bit unsigned",info.getName());
		assertEquals(16,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(16,info.getNumBits());
		assertEquals(UserType.USHORT,info.getUserType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.INT);
		assertEquals("32-bit signed",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(UserType.INT,info.getUserType());
		assertFalse(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.UINT);
		assertEquals("32-bit unsigned",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(UserType.UINT,info.getUserType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.FLOAT);
		assertEquals("32-bit float",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(UserType.FLOAT,info.getUserType());
		assertTrue(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.DOUBLE);
		assertEquals("64-bit float",info.getName());
		assertEquals(64,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(64,info.getNumBits());
		assertEquals(UserType.DOUBLE,info.getUserType());
		assertTrue(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(UserType.LONG);
		assertEquals("64-bit signed",info.getName());
		assertEquals(64,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(64,info.getNumBits());
		assertEquals(UserType.LONG,info.getUserType());
		assertFalse(info.isFloat());
		assertFalse(info.isUnsigned());
	}

	@Test
	public void testFindSampleInfo()
	{
		assertNull(SampleManager.findSampleInfo(null));
		assertNull(SampleManager.findSampleInfo("ShakyJones"));
		assertEquals(SampleManager.getSampleInfo(UserType.BIT), SampleManager.findSampleInfo("1-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(UserType.BYTE), SampleManager.findSampleInfo("8-bit signed"));
		assertEquals(SampleManager.getSampleInfo(UserType.UBYTE), SampleManager.findSampleInfo("8-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(UserType.UINT12), SampleManager.findSampleInfo("12-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(UserType.SHORT), SampleManager.findSampleInfo("16-bit signed"));
		assertEquals(SampleManager.getSampleInfo(UserType.USHORT), SampleManager.findSampleInfo("16-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(UserType.INT), SampleManager.findSampleInfo("32-bit signed"));
		assertEquals(SampleManager.getSampleInfo(UserType.UINT), SampleManager.findSampleInfo("32-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(UserType.FLOAT), SampleManager.findSampleInfo("32-bit float"));
		assertEquals(SampleManager.getSampleInfo(UserType.DOUBLE), SampleManager.findSampleInfo("64-bit float"));
		assertEquals(SampleManager.getSampleInfo(UserType.LONG), SampleManager.findSampleInfo("64-bit signed"));
	}

	private void compatible(UserType type, Object data)
	{
		try {
			SampleManager.verifyTypeCompatibility(data, type);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
	
	private void incompatible(UserType type, Object data)
	{
		try {
			SampleManager.verifyTypeCompatibility(data, type);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testTypeCompatiblility()
	{
		// valid values
		compatible(UserType.BYTE, new byte[0]);
		compatible(UserType.UBYTE, new byte[0]);
		compatible(UserType.SHORT, new short[0]);
		compatible(UserType.USHORT, new short[0]);
		compatible(UserType.INT, new int[0]);
		compatible(UserType.UINT, new int[0]);
		compatible(UserType.LONG, new long[0]);
		compatible(UserType.FLOAT, new float[0]);
		compatible(UserType.DOUBLE, new double[0]);
		compatible(UserType.UINT12, new int[0]);
		
		// some failure values
		incompatible(UserType.BYTE, "A String");
		incompatible(UserType.UBYTE, new short[0]);
		incompatible(UserType.SHORT, new int[0]);
		incompatible(UserType.USHORT, new long[0]);
		incompatible(UserType.INT, new double[0]);
		incompatible(UserType.UINT, new float[0]);
		incompatible(UserType.LONG, new byte[0]);
		incompatible(UserType.FLOAT, new double[0]);
		incompatible(UserType.DOUBLE, new float[0]);
		incompatible(UserType.UINT12, new byte[0]);
	}
}
