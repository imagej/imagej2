package imagej;

import static org.junit.Assert.*;

import java.lang.reflect.Array;

import org.junit.Test;

public class EncodingManagerTest
{
	@Test
	public void testGetEncodingUserType()
	{
		assertNotNull(EncodingManager.getEncoding(UserType.BIT));
		assertNotNull(EncodingManager.getEncoding(UserType.BYTE));
		assertNotNull(EncodingManager.getEncoding(UserType.UBYTE));
		assertNotNull(EncodingManager.getEncoding(UserType.UINT12));
		assertNotNull(EncodingManager.getEncoding(UserType.SHORT));
		assertNotNull(EncodingManager.getEncoding(UserType.USHORT));
		assertNotNull(EncodingManager.getEncoding(UserType.INT));
		assertNotNull(EncodingManager.getEncoding(UserType.UINT));
		assertNotNull(EncodingManager.getEncoding(UserType.FLOAT));
		assertNotNull(EncodingManager.getEncoding(UserType.LONG));
		assertNotNull(EncodingManager.getEncoding(UserType.DOUBLE));
	}
	
	@Test
	public void testCalcStorageUnitsRequired()
	{
		DataEncoding encoding;
		
		encoding = EncodingManager.getEncoding(UserType.BYTE);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.UBYTE);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.SHORT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.USHORT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.INT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.UINT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.FLOAT);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.LONG);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.DOUBLE);
		for (int i = 0; i < 10; i++)
			assertEquals(i, EncodingManager.calcStorageUnitsRequired(encoding, i));

		encoding = EncodingManager.getEncoding(UserType.BIT);
		assertEquals(0, EncodingManager.calcStorageUnitsRequired(encoding, 0));
		assertEquals(1, EncodingManager.calcStorageUnitsRequired(encoding, 1));
		assertEquals(1, EncodingManager.calcStorageUnitsRequired(encoding, 32));
		assertEquals(2, EncodingManager.calcStorageUnitsRequired(encoding, 33));
		assertEquals(2, EncodingManager.calcStorageUnitsRequired(encoding, 64));
		assertEquals(3, EncodingManager.calcStorageUnitsRequired(encoding, 65));
		assertEquals(3, EncodingManager.calcStorageUnitsRequired(encoding, 96));
		assertEquals(4, EncodingManager.calcStorageUnitsRequired(encoding, 97));
		assertEquals(4, EncodingManager.calcStorageUnitsRequired(encoding, 128));

		encoding = EncodingManager.getEncoding(UserType.UINT12);
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
		
		encoding = EncodingManager.getEncoding(UserType.BIT);
		assertEquals(320, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.BYTE);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.UBYTE);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.UINT12);
		assertEquals(26, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.SHORT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.USHORT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.INT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.UINT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.FLOAT);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.LONG);
		assertEquals(10, EncodingManager.calcMaxPixelsStorable(encoding, 10));
		
		encoding = EncodingManager.getEncoding(UserType.DOUBLE);
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
		
		encoding = EncodingManager.getEncoding(UserType.BIT);
		compatible(encoding, new int[0]);
		incompatible(encoding, new byte[0]);
		
		encoding = EncodingManager.getEncoding(UserType.BYTE);
		compatible(encoding, new byte[0]);
		incompatible(encoding, new short[0]);
		
		encoding = EncodingManager.getEncoding(UserType.UBYTE);
		compatible(encoding, new byte[0]);
		incompatible(encoding, new short[0]);
		
		encoding = EncodingManager.getEncoding(UserType.SHORT);
		compatible(encoding, new short[0]);
		incompatible(encoding, new int[0]);
		
		encoding = EncodingManager.getEncoding(UserType.USHORT);
		compatible(encoding, new short[0]);
		incompatible(encoding, new int[0]);
		
		encoding = EncodingManager.getEncoding(UserType.INT);
		compatible(encoding, new int[0]);
		incompatible(encoding, new long[0]);
		
		encoding = EncodingManager.getEncoding(UserType.UINT);
		compatible(encoding, new int[0]);
		incompatible(encoding, new long[0]);
		
		encoding = EncodingManager.getEncoding(UserType.FLOAT);
		compatible(encoding, new float[0]);
		incompatible(encoding, new double[0]);
		
		encoding = EncodingManager.getEncoding(UserType.LONG);
		compatible(encoding, new long[0]);
		incompatible(encoding, new short[0]);
		
		encoding = EncodingManager.getEncoding(UserType.DOUBLE);
		compatible(encoding, new double[0]);
		incompatible(encoding, new float[0]);
	}
	
	private void compatible(UserType type, Object data)
	{
		try {
			EncodingManager.verifyTypeCompatibility(data, type);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
	
	private void incompatible(UserType type, Object data)
	{
		try {
			EncodingManager.verifyTypeCompatibility(data, type);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testVerifyTypeCompatiblilityUserType()
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
	
	private void correctlyAllocates(int numElements, UserType type)
	{
		Object array = EncodingManager.allocateCompatibleArray(type, numElements);
		assertNotNull(array);
		DataEncoding encoding = EncodingManager.getEncoding(type);
		int storageUnitCount = EncodingManager.calcStorageUnitsRequired(encoding, numElements);
		assertEquals(storageUnitCount, Array.getLength(array));
		EncodingManager.verifyTypeCompatibility(array, type);
	}

	@Test
	public void testAllocateCompatibleArrayUserTypeInt()
	{
		correctlyAllocates(1, UserType.BIT);
		correctlyAllocates(23, UserType.BYTE);
		correctlyAllocates(45, UserType.UBYTE);
		correctlyAllocates(100, UserType.UINT12);
		correctlyAllocates(67, UserType.SHORT);
		correctlyAllocates(89, UserType.USHORT);
		correctlyAllocates(2, UserType.INT);
		correctlyAllocates(34, UserType.UINT);
		correctlyAllocates(56, UserType.FLOAT);
		correctlyAllocates(78, UserType.LONG);
		correctlyAllocates(90, UserType.DOUBLE);

		correctlyAllocates(1024, UserType.USHORT);
	}
}
