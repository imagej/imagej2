package imagej2.imglib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import imagej2.UserType;
import imagej2.imglib.TypeManager;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;


public class TypeManagerTest {

	@Test
	public void testGetRealType()
	{
		assertTrue(TypeManager.getRealType(UserType.BIT) instanceof BitType);
		assertTrue(TypeManager.getRealType(UserType.BYTE) instanceof ByteType);
		assertTrue(TypeManager.getRealType(UserType.UBYTE) instanceof UnsignedByteType);
		assertTrue(TypeManager.getRealType(UserType.UINT12) instanceof Unsigned12BitType);
		assertTrue(TypeManager.getRealType(UserType.SHORT) instanceof ShortType);
		assertTrue(TypeManager.getRealType(UserType.USHORT) instanceof UnsignedShortType);
		assertTrue(TypeManager.getRealType(UserType.INT) instanceof IntType);
		assertTrue(TypeManager.getRealType(UserType.UINT) instanceof UnsignedIntType);
		assertTrue(TypeManager.getRealType(UserType.FLOAT) instanceof FloatType);
		assertTrue(TypeManager.getRealType(UserType.LONG) instanceof LongType);
		assertTrue(TypeManager.getRealType(UserType.DOUBLE) instanceof DoubleType);
	}

	@Test
	public void testGetUserTypeRealType()
	{
		assertEquals(UserType.BIT, TypeManager.getUserType(new BitType()));
		assertEquals(UserType.BYTE, TypeManager.getUserType(new ByteType()));
		assertEquals(UserType.UBYTE, TypeManager.getUserType(new UnsignedByteType()));
		assertEquals(UserType.UINT12, TypeManager.getUserType(new Unsigned12BitType()));
		assertEquals(UserType.SHORT, TypeManager.getUserType(new ShortType()));
		assertEquals(UserType.USHORT, TypeManager.getUserType(new UnsignedShortType()));
		assertEquals(UserType.INT, TypeManager.getUserType(new IntType()));
		assertEquals(UserType.UINT, TypeManager.getUserType(new UnsignedIntType()));
		assertEquals(UserType.FLOAT, TypeManager.getUserType(new FloatType()));
		assertEquals(UserType.LONG, TypeManager.getUserType(new LongType()));
		assertEquals(UserType.DOUBLE, TypeManager.getUserType(new DoubleType()));
	}
	@Test
	public void testIsUnsignedType() {
		
		assertFalse(TypeManager.isUnsignedType(new ByteType()));
		assertFalse(TypeManager.isUnsignedType(new ShortType()));
		assertFalse(TypeManager.isUnsignedType(new IntType()));
		assertFalse(TypeManager.isUnsignedType(new LongType()));
		assertFalse(TypeManager.isUnsignedType(new FloatType()));
		assertFalse(TypeManager.isUnsignedType(new DoubleType()));
		
		assertTrue(TypeManager.isUnsignedType(new BitType()));
		assertTrue(TypeManager.isUnsignedType(new UnsignedByteType()));
		assertTrue(TypeManager.isUnsignedType(new UnsignedShortType()));
		assertTrue(TypeManager.isUnsignedType(new UnsignedIntType()));
		assertTrue(TypeManager.isUnsignedType(new Unsigned12BitType()));
	}

	@Test
	public void testIsIntegralType() {
		assertTrue(TypeManager.isIntegralType(new BitType()));
		assertTrue(TypeManager.isIntegralType(new ByteType()));
		assertTrue(TypeManager.isIntegralType(new ShortType()));
		assertTrue(TypeManager.isIntegralType(new IntType()));
		assertTrue(TypeManager.isIntegralType(new LongType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedByteType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedShortType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedIntType()));
		assertTrue(TypeManager.isIntegralType(new Unsigned12BitType()));

		assertFalse(TypeManager.isIntegralType(new FloatType()));
		assertFalse(TypeManager.isIntegralType(new DoubleType()));
	}

	@Test
	public void testBoundValueToType()
	{
		RealType<?> type;
		
		type = new BitType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(1,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new ByteType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(Byte.MIN_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Byte.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new UnsignedByteType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(255,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);

		type = new ShortType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(-32768,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(32767,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new UnsignedShortType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(65535,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new IntType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(Integer.MIN_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Integer.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new UnsignedIntType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals((1L<<32)-1,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new FloatType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(-Float.MAX_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Float.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);

		type = new DoubleType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(-Double.MAX_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Double.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new LongType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(Long.MIN_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Long.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new Unsigned12BitType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(4095,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
	}
	
	@Test
	public void testSameKind()
	{
		RealType<?> type1, type2;

		type1 = new BitType();
		
		type2 = new BitType();
		assertTrue(TypeManager.sameKind(type1,type2));
		
		type2 = new ByteType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new UnsignedByteType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new ShortType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new UnsignedShortType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new IntType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new UnsignedIntType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new FloatType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new DoubleType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new LongType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new Unsigned12BitType();
		assertFalse(TypeManager.sameKind(type1,type2));
	}

	@Test
	public void testValidValue()
	{
		RealType<?> type;
		
		type = new BitType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, 1));
		assertFalse(TypeManager.validValue(type, -1));
		assertFalse(TypeManager.validValue(type, 2));
		
		type = new ByteType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Byte.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, Byte.MIN_VALUE));
		assertFalse(TypeManager.validValue(type, Byte.MAX_VALUE+1));
		assertFalse(TypeManager.validValue(type, Byte.MIN_VALUE-1));
		
		type = new UnsignedByteType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, 255));
		assertTrue(TypeManager.validValue(type, 0));
		assertFalse(TypeManager.validValue(type, 256));
		assertFalse(TypeManager.validValue(type, -1));

		type = new ShortType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Short.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, Short.MIN_VALUE));
		assertFalse(TypeManager.validValue(type, Short.MAX_VALUE+1));
		assertFalse(TypeManager.validValue(type, Short.MIN_VALUE-1));
		
		type = new UnsignedShortType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, 65535));
		assertTrue(TypeManager.validValue(type, 0));
		assertFalse(TypeManager.validValue(type, 65536));
		assertFalse(TypeManager.validValue(type, -1));
		
		type = new IntType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Integer.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, Integer.MIN_VALUE));
		assertFalse(TypeManager.validValue(type, Integer.MAX_VALUE+1.0));
		assertFalse(TypeManager.validValue(type, Integer.MIN_VALUE-1.0));
		
		type = new UnsignedIntType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, (1L<<32)-1));
		assertTrue(TypeManager.validValue(type, 0));
		assertFalse(TypeManager.validValue(type, Math.pow(2, 32)+1));
		assertFalse(TypeManager.validValue(type, -1));
		
		type = new DoubleType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Double.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, -Double.MAX_VALUE));
		
		type = new FloatType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Float.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, -Float.MAX_VALUE));
		assertFalse(TypeManager.validValue(type, Double.MAX_VALUE));
		assertFalse(TypeManager.validValue(type, -Double.MAX_VALUE));

		type = new LongType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Long.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, Long.MIN_VALUE));
		assertFalse(TypeManager.validValue(type, Double.MAX_VALUE));
		assertFalse(TypeManager.validValue(type, -Double.MAX_VALUE));

		type = new Unsigned12BitType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, 4095));
		assertFalse(TypeManager.validValue(type, -1));
		assertFalse(TypeManager.validValue(type, 4096));
		assertFalse(TypeManager.validValue(type, -Double.MAX_VALUE));
		assertFalse(TypeManager.validValue(type, Double.MAX_VALUE));
	}
}
