package imagej2;

import static org.junit.Assert.*;
import org.junit.Test;

import imagej2.SampleInfo;
import imagej2.SampleManager;
import imagej2.SampleInfo.ValueType;

public class SampleManagerTest {

	@Test
	public void testGetSampleInfo() 
	{
		SampleInfo info;
		
		info = SampleManager.getSampleInfo(ValueType.BIT);
		assertEquals("1-bit unsigned",info.getName());
		assertEquals(1,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(1,info.getNumBits());
		assertEquals(ValueType.BIT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());
		
		info = SampleManager.getSampleInfo(ValueType.BYTE);
		assertEquals("8-bit signed",info.getName());
		assertEquals(8,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(8,info.getNumBits());
		assertEquals(ValueType.BYTE,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.UBYTE);
		assertEquals("8-bit unsigned",info.getName());
		assertEquals(8,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(8,info.getNumBits());
		assertEquals(ValueType.UBYTE,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.UINT12);
		assertEquals("12-bit unsigned",info.getName());
		assertEquals(12,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(12,info.getNumBits());
		assertEquals(ValueType.UINT12,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.SHORT);
		assertEquals("16-bit signed",info.getName());
		assertEquals(16,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(16,info.getNumBits());
		assertEquals(ValueType.SHORT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.USHORT);
		assertEquals("16-bit unsigned",info.getName());
		assertEquals(16,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(16,info.getNumBits());
		assertEquals(ValueType.USHORT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.INT);
		assertEquals("32-bit signed",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(ValueType.INT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.UINT);
		assertEquals("32-bit unsigned",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(ValueType.UINT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.FLOAT);
		assertEquals("32-bit float",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(ValueType.FLOAT,info.getValueType());
		assertTrue(info.isFloat());
		assertFalse(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.DOUBLE);
		assertEquals("64-bit float",info.getName());
		assertEquals(64,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(64,info.getNumBits());
		assertEquals(ValueType.DOUBLE,info.getValueType());
		assertTrue(info.isFloat());
		assertFalse(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.LONG);
		assertEquals("64-bit signed",info.getName());
		assertEquals(64,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(64,info.getNumBits());
		assertEquals(ValueType.LONG,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());
	}

	@Test
	public void testFindSampleInfo()
	{
		assertNull(SampleManager.findSampleInfo(null));
		assertNull(SampleManager.findSampleInfo("ShakyJones"));
		assertEquals(SampleManager.getSampleInfo(ValueType.BIT), SampleManager.findSampleInfo("1-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.BYTE), SampleManager.findSampleInfo("8-bit signed"));
		assertEquals(SampleManager.getSampleInfo(ValueType.UBYTE), SampleManager.findSampleInfo("8-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.UINT12), SampleManager.findSampleInfo("12-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.SHORT), SampleManager.findSampleInfo("16-bit signed"));
		assertEquals(SampleManager.getSampleInfo(ValueType.USHORT), SampleManager.findSampleInfo("16-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.INT), SampleManager.findSampleInfo("32-bit signed"));
		assertEquals(SampleManager.getSampleInfo(ValueType.UINT), SampleManager.findSampleInfo("32-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.FLOAT), SampleManager.findSampleInfo("32-bit float"));
		assertEquals(SampleManager.getSampleInfo(ValueType.DOUBLE), SampleManager.findSampleInfo("64-bit float"));
		assertEquals(SampleManager.getSampleInfo(ValueType.LONG), SampleManager.findSampleInfo("64-bit signed"));
	}

	private void compatible(ValueType type, Object data)
	{
		try {
			SampleManager.verifyTypeCompatibility(data, type);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
	
	private void incompatible(ValueType type, Object data)
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
		compatible(ValueType.BYTE, new byte[0]);
		compatible(ValueType.UBYTE, new byte[0]);
		compatible(ValueType.SHORT, new short[0]);
		compatible(ValueType.USHORT, new short[0]);
		compatible(ValueType.INT, new int[0]);
		compatible(ValueType.UINT, new int[0]);
		compatible(ValueType.LONG, new long[0]);
		compatible(ValueType.FLOAT, new float[0]);
		compatible(ValueType.DOUBLE, new double[0]);
		compatible(ValueType.UINT12, new int[0]);
		
		// some failure values
		incompatible(ValueType.BYTE, "A String");
		incompatible(ValueType.UBYTE, new short[0]);
		incompatible(ValueType.SHORT, new int[0]);
		incompatible(ValueType.USHORT, new long[0]);
		incompatible(ValueType.INT, new double[0]);
		incompatible(ValueType.UINT, new float[0]);
		incompatible(ValueType.LONG, new byte[0]);
		incompatible(ValueType.FLOAT, new double[0]);
		incompatible(ValueType.DOUBLE, new float[0]);
		incompatible(ValueType.UINT12, new short[0]);
	}
}
