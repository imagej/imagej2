package ij.process;

import static org.junit.Assert.*;

import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

public class TypeManagerTest {

	@Test
	public void testIsUnsignedType() {
		
		assertFalse(TypeManager.isUnsignedType(new ByteType()));
		assertFalse(TypeManager.isUnsignedType(new ShortType()));
		assertFalse(TypeManager.isUnsignedType(new IntType()));
		assertFalse(TypeManager.isUnsignedType(new LongType()));
		assertFalse(TypeManager.isUnsignedType(new FloatType()));
		assertFalse(TypeManager.isUnsignedType(new DoubleType()));
		
		assertTrue(TypeManager.isUnsignedType(new UnsignedByteType()));
		assertTrue(TypeManager.isUnsignedType(new UnsignedShortType()));
		assertTrue(TypeManager.isUnsignedType(new UnsignedIntType()));
	}

	@Test
	public void testIsIntegralType() {
		assertTrue(TypeManager.isIntegralType(new ByteType()));
		assertTrue(TypeManager.isIntegralType(new ShortType()));
		assertTrue(TypeManager.isIntegralType(new IntType()));
		assertTrue(TypeManager.isIntegralType(new LongType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedByteType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedShortType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedIntType()));

		assertFalse(TypeManager.isIntegralType(new FloatType()));
		assertFalse(TypeManager.isIntegralType(new DoubleType()));
	}

	@Test
	public void testBoundIntValueToType() {
		
		RealType type;
		
		type = new ByteType();
		assertEquals(0,TypeManager.boundIntValueToType(type, 0));
		assertEquals(Byte.MIN_VALUE,TypeManager.boundIntValueToType(type, Integer.MIN_VALUE));
		assertEquals(Byte.MAX_VALUE,TypeManager.boundIntValueToType(type, Integer.MAX_VALUE));
		
		type = new UnsignedByteType();
		assertEquals(0,TypeManager.boundIntValueToType(type, 0));
		assertEquals(0,TypeManager.boundIntValueToType(type, Integer.MIN_VALUE));
		assertEquals(255,TypeManager.boundIntValueToType(type, Integer.MAX_VALUE));

		type = new ShortType();
		assertEquals(0,TypeManager.boundIntValueToType(type, 0));
		assertEquals(-32768,TypeManager.boundIntValueToType(type, Integer.MIN_VALUE));
		assertEquals(32767,TypeManager.boundIntValueToType(type, Integer.MAX_VALUE));
		
		type = new UnsignedShortType();
		assertEquals(0,TypeManager.boundIntValueToType(type, 0));
		assertEquals(0,TypeManager.boundIntValueToType(type, Integer.MIN_VALUE));
		assertEquals(65535,TypeManager.boundIntValueToType(type, Integer.MAX_VALUE));
		
		type = new IntType();
		assertEquals(0,TypeManager.boundIntValueToType(type, 0));
		assertEquals(Integer.MIN_VALUE,TypeManager.boundIntValueToType(type, Integer.MIN_VALUE));
		assertEquals(Integer.MAX_VALUE,TypeManager.boundIntValueToType(type, Integer.MAX_VALUE));
		
		type = new UnsignedIntType();
		assertEquals(0,TypeManager.boundIntValueToType(type, 0));
		assertEquals(0,TypeManager.boundIntValueToType(type, Integer.MIN_VALUE));
		assertEquals(Integer.MAX_VALUE,TypeManager.boundIntValueToType(type, Integer.MAX_VALUE));
	}

}
