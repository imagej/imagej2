package imagej;

import static org.junit.Assert.*;

import java.lang.reflect.Array;

import org.junit.Test;

public class EncodingManagerTest
{
	@Test
	public void testGetEncodingDataType()
	{
		assertNotNull(EncodingManager.getEncoding(DataType.BIT));
		assertNotNull(EncodingManager.getEncoding(DataType.BYTE));
		assertNotNull(EncodingManager.getEncoding(DataType.UBYTE));
		assertNotNull(EncodingManager.getEncoding(DataType.UINT12));
		assertNotNull(EncodingManager.getEncoding(DataType.SHORT));
		assertNotNull(EncodingManager.getEncoding(DataType.USHORT));
		assertNotNull(EncodingManager.getEncoding(DataType.INT));
		assertNotNull(EncodingManager.getEncoding(DataType.UINT));
		assertNotNull(EncodingManager.getEncoding(DataType.FLOAT));
		assertNotNull(EncodingManager.getEncoding(DataType.LONG));
		assertNotNull(EncodingManager.getEncoding(DataType.DOUBLE));
	}
	
	@Test
	public void testCalcStorageUnitsRequired()
	{
		DataEncoding encoding;
		
		encoding = EncodingManager.getEncoding(DataType.BYTE);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.UBYTE);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.SHORT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.USHORT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.INT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.UINT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.FLOAT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.LONG);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.DOUBLE);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(DataType.BIT);
		assertEquals(0, EncodingManager.calcStorageUnitsRequired(encoding, 0));
		assertEquals(1, EncodingManager.calcStorageUnitsRequired(encoding, 1));
		assertEquals(1, EncodingManager.calcStorageUnitsRequired(encoding, 32));
		assertEquals(2, EncodingManager.calcStorageUnitsRequired(encoding, 33));
		assertEquals(2, EncodingManager.calcStorageUnitsRequired(encoding, 64));
		assertEquals(3, EncodingManager.calcStorageUnitsRequired(encoding, 65));
		assertEquals(3, EncodingManager.calcStorageUnitsRequired(encoding, 96));
		assertEquals(4, EncodingManager.calcStorageUnitsRequired(encoding, 97));
		assertEquals(4, EncodingManager.calcStorageUnitsRequired(encoding, 128));

		encoding = EncodingManager.getEncoding(DataType.UINT12);
		assertEquals(0, EncodingManager.calcStorageUnitsRequired(encoding, 0));
		assertEquals(1, EncodingManager.calcStorageUnitsRequired(encoding, 1));
		assertEquals(1, EncodingManager.calcStorageUnitsRequired(encoding, 2));
		assertEquals(2, EncodingManager.calcStorageUnitsRequired(encoding, 3));
		assertEquals(2, EncodingManager.calcStorageUnitsRequired(encoding, 4));
		assertEquals(2, EncodingManager.calcStorageUnitsRequired(encoding, 5));
		assertEquals(3, EncodingManager.calcStorageUnitsRequired(encoding, 6));
		assertEquals(3, EncodingManager.calcStorageUnitsRequired(encoding, 7));
		assertEquals(3, EncodingManager.calcStorageUnitsRequired(encoding, 8));
		assertEquals(4, EncodingManager.calcStorageUnitsRequired(encoding, 9));
		assertEquals(4, EncodingManager.calcStorageUnitsRequired(encoding, 10));
		assertEquals(5, EncodingManager.calcStorageUnitsRequired(encoding, 11));
		assertEquals(5, EncodingManager.calcStorageUnitsRequired(encoding, 12));
	}
	
	@Test
	public void testCalcMaxPixelsStorable()
	{
		DataEncoding encoding;
		
		encoding = EncodingManager.getEncoding(DataType.BIT);
		assertEquals(320, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.BYTE);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.UBYTE);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.UINT12);
		assertEquals(26, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.SHORT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.USHORT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.INT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.UINT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.FLOAT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.LONG);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(DataType.DOUBLE);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
	}

	private void compatible(StorageType type, Object data)
	{
		try {
			EncodingManager.verifyTypeCompatibility(data, type);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}

	private void incompatible(StorageType type, Object data)
	{
		try {
			EncodingManager.verifyTypeCompatibility(data, type);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	private void compatible(DataEncoding encoding, Object data)
	{
		compatible(encoding.getBackingType(), data);
	}
	
	private void incompatible(DataEncoding encoding, Object data)
	{
		incompatible(encoding.getBackingType(), data);
	}
	
	@Test
	public void testVerifyTypeCompatibilityObjectDataEncoding()
	{
		DataEncoding encoding;
		
		encoding = EncodingManager.getEncoding(DataType.BIT);
		compatible(encoding, new int[0]);
		incompatible(encoding, new byte[0]);
		
		encoding = EncodingManager.getEncoding(DataType.BYTE);
		compatible(encoding, new byte[0]);
		incompatible(encoding, new short[0]);
		
		encoding = EncodingManager.getEncoding(DataType.UBYTE);
		compatible(encoding, new byte[0]);
		incompatible(encoding, new short[0]);
		
		encoding = EncodingManager.getEncoding(DataType.SHORT);
		compatible(encoding, new short[0]);
		incompatible(encoding, new int[0]);
		
		encoding = EncodingManager.getEncoding(DataType.USHORT);
		compatible(encoding, new short[0]);
		incompatible(encoding, new int[0]);
		
		encoding = EncodingManager.getEncoding(DataType.INT);
		compatible(encoding, new int[0]);
		incompatible(encoding, new long[0]);
		
		encoding = EncodingManager.getEncoding(DataType.UINT);
		compatible(encoding, new int[0]);
		incompatible(encoding, new long[0]);
		
		encoding = EncodingManager.getEncoding(DataType.FLOAT);
		compatible(encoding, new float[0]);
		incompatible(encoding, new double[0]);
		
		encoding = EncodingManager.getEncoding(DataType.LONG);
		compatible(encoding, new long[0]);
		incompatible(encoding, new short[0]);
		
		encoding = EncodingManager.getEncoding(DataType.DOUBLE);
		compatible(encoding, new double[0]);
		incompatible(encoding, new float[0]);
	}
	
	private void compatible(DataType type, Object data)
	{
		try {
			EncodingManager.verifyTypeCompatibility(data, type);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
	
	private void incompatible(DataType type, Object data)
	{
		try {
			EncodingManager.verifyTypeCompatibility(data, type);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testVerifyTypeCompatiblilityDataType()
	{
		// valid values
		compatible(DataType.BYTE, new byte[0]);
		compatible(DataType.UBYTE, new byte[0]);
		compatible(DataType.SHORT, new short[0]);
		compatible(DataType.USHORT, new short[0]);
		compatible(DataType.INT, new int[0]);
		compatible(DataType.UINT, new int[0]);
		compatible(DataType.LONG, new long[0]);
		compatible(DataType.FLOAT, new float[0]);
		compatible(DataType.DOUBLE, new double[0]);
		compatible(DataType.UINT12, new int[0]);
		
		// some failure values
		incompatible(DataType.BYTE, "A String");
		incompatible(DataType.UBYTE, new short[0]);
		incompatible(DataType.SHORT, new int[0]);
		incompatible(DataType.USHORT, new long[0]);
		incompatible(DataType.INT, new double[0]);
		incompatible(DataType.UINT, new float[0]);
		incompatible(DataType.LONG, new byte[0]);
		incompatible(DataType.FLOAT, new double[0]);
		incompatible(DataType.DOUBLE, new float[0]);
		incompatible(DataType.UINT12, new byte[0]);
	}

	private void correctlyAllocates(int size, StorageType type)
	{
		Object array = EncodingManager.allocateCompatibleArray(type, size);
		assertNotNull(array);
		assertEquals(size, Array.getLength(array));
		EncodingManager.verifyTypeCompatibility(array, type);
	}
	
	@Test
	public void testAllocateCompatibleArrayStorageTypeInt()
	{
		correctlyAllocates(1, StorageType.INT8);
		correctlyAllocates(23, StorageType.UINT8);
		correctlyAllocates(45, StorageType.INT16);
		correctlyAllocates(67, StorageType.UINT16);
		correctlyAllocates(89, StorageType.INT32);
		correctlyAllocates(2, StorageType.UINT32);
		correctlyAllocates(34, StorageType.FLOAT32);
		correctlyAllocates(56, StorageType.INT64);
		correctlyAllocates(78, StorageType.FLOAT64);

		correctlyAllocates(1024, StorageType.INT8);
	}
	
	private void correctlyAllocates(int numElements, DataType type)
	{
		Object array = EncodingManager.allocateCompatibleArray(type, numElements);
		assertNotNull(array);
		DataEncoding encoding = EncodingManager.getEncoding(type);
		int storageUnitCount = EncodingManager.calcStorageUnitsRequired(encoding, numElements);
		assertEquals(storageUnitCount, Array.getLength(array));
		EncodingManager.verifyTypeCompatibility(array, type);
	}

	@Test
	public void testAllocateCompatibleArrayDataTypeInt()
	{
		correctlyAllocates(1, DataType.BIT);
		correctlyAllocates(23, DataType.BYTE);
		correctlyAllocates(45, DataType.UBYTE);
		correctlyAllocates(100, DataType.UINT12);
		correctlyAllocates(67, DataType.SHORT);
		correctlyAllocates(89, DataType.USHORT);
		correctlyAllocates(2, DataType.INT);
		correctlyAllocates(34, DataType.UINT);
		correctlyAllocates(56, DataType.FLOAT);
		correctlyAllocates(78, DataType.LONG);
		correctlyAllocates(90, DataType.DOUBLE);

		correctlyAllocates(1024, DataType.USHORT);
	}
}
