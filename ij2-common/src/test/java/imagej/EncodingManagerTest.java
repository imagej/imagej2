package imagej;

import static org.junit.Assert.*;
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
		
	}
	
	@Test
	public void testCalcMaxPixelsStorable()
	{

	}

	private void compatible(DataEncoding encoding, Object data)
	{
		try {
			EncodingManager.verifyTypeCompatibility(data, encoding);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
	
	private void incompatible(DataEncoding encoding, Object data)
	{
		try {
			EncodingManager.verifyTypeCompatibility(data, encoding);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
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
		incompatible(encoding, new float[0]);
		
		encoding = EncodingManager.getEncoding(UserType.FLOAT);
		compatible(encoding, new float[0]);
		incompatible(encoding, new long[0]);
		
		encoding = EncodingManager.getEncoding(UserType.LONG);
		compatible(encoding, new long[0]);
		incompatible(encoding, new short[0]);
		
		encoding = EncodingManager.getEncoding(UserType.USHORT);
		compatible(encoding, new short[0]);
		incompatible(encoding, new int[0]);
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
}
