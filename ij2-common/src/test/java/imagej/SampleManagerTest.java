package imagej;

import static org.junit.Assert.*;
import org.junit.Test;

import imagej.SampleInfo;
import imagej.SampleManager;
import imagej.DataType;

public class SampleManagerTest {

	@Test
	public void testGetSampleInfo() 
	{
		SampleInfo info;
		
		info = SampleManager.getSampleInfo(DataType.BIT);
		assertEquals("1-bit unsigned",info.getName());
		assertEquals(1,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(1,info.getNumBits());
		assertEquals(DataType.BIT,info.getDataType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());
		
		info = SampleManager.getSampleInfo(DataType.BYTE);
		assertEquals("8-bit signed",info.getName());
		assertEquals(8,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(8,info.getNumBits());
		assertEquals(DataType.BYTE,info.getDataType());
		assertFalse(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.UBYTE);
		assertEquals("8-bit unsigned",info.getName());
		assertEquals(8,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(8,info.getNumBits());
		assertEquals(DataType.UBYTE,info.getDataType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.UINT12);
		assertEquals("12-bit unsigned",info.getName());
		assertEquals(12,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(12,info.getNumBits());
		assertEquals(DataType.UINT12,info.getDataType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.SHORT);
		assertEquals("16-bit signed",info.getName());
		assertEquals(16,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(16,info.getNumBits());
		assertEquals(DataType.SHORT,info.getDataType());
		assertFalse(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.USHORT);
		assertEquals("16-bit unsigned",info.getName());
		assertEquals(16,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(16,info.getNumBits());
		assertEquals(DataType.USHORT,info.getDataType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.INT);
		assertEquals("32-bit signed",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(DataType.INT,info.getDataType());
		assertFalse(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.UINT);
		assertEquals("32-bit unsigned",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(DataType.UINT,info.getDataType());
		assertFalse(info.isFloat());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.FLOAT);
		assertEquals("32-bit float",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(DataType.FLOAT,info.getDataType());
		assertTrue(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.DOUBLE);
		assertEquals("64-bit float",info.getName());
		assertEquals(64,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(64,info.getNumBits());
		assertEquals(DataType.DOUBLE,info.getDataType());
		assertTrue(info.isFloat());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(DataType.LONG);
		assertEquals("64-bit signed",info.getName());
		assertEquals(64,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(64,info.getNumBits());
		assertEquals(DataType.LONG,info.getDataType());
		assertFalse(info.isFloat());
		assertFalse(info.isUnsigned());
	}

	@Test
	public void testFindSampleInfo()
	{
		assertNull(SampleManager.findSampleInfo(null));
		assertNull(SampleManager.findSampleInfo("ShakyJones"));
		assertEquals(SampleManager.getSampleInfo(DataType.BIT), SampleManager.findSampleInfo("1-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(DataType.BYTE), SampleManager.findSampleInfo("8-bit signed"));
		assertEquals(SampleManager.getSampleInfo(DataType.UBYTE), SampleManager.findSampleInfo("8-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(DataType.UINT12), SampleManager.findSampleInfo("12-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(DataType.SHORT), SampleManager.findSampleInfo("16-bit signed"));
		assertEquals(SampleManager.getSampleInfo(DataType.USHORT), SampleManager.findSampleInfo("16-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(DataType.INT), SampleManager.findSampleInfo("32-bit signed"));
		assertEquals(SampleManager.getSampleInfo(DataType.UINT), SampleManager.findSampleInfo("32-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(DataType.FLOAT), SampleManager.findSampleInfo("32-bit float"));
		assertEquals(SampleManager.getSampleInfo(DataType.DOUBLE), SampleManager.findSampleInfo("64-bit float"));
		assertEquals(SampleManager.getSampleInfo(DataType.LONG), SampleManager.findSampleInfo("64-bit signed"));
	}
}
