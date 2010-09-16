package imagej.process;

import static org.junit.Assert.*;
import imagej.process.TypeManager;

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
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(Byte.MIN_VALUE,TypeManager.boundValueToType(type, Integer.MIN_VALUE),0);
		assertEquals(Byte.MAX_VALUE,TypeManager.boundValueToType(type, Integer.MAX_VALUE),0);
		
		type = new UnsignedByteType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, Integer.MIN_VALUE),0);
		assertEquals(255,TypeManager.boundValueToType(type, Integer.MAX_VALUE),0);

		type = new ShortType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(-32768,TypeManager.boundValueToType(type, Integer.MIN_VALUE),0);
		assertEquals(32767,TypeManager.boundValueToType(type, Integer.MAX_VALUE),0);
		
		type = new UnsignedShortType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, Integer.MIN_VALUE),0);
		assertEquals(65535,TypeManager.boundValueToType(type, Integer.MAX_VALUE),0);
		
		type = new IntType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(Integer.MIN_VALUE,TypeManager.boundValueToType(type, Integer.MIN_VALUE),0);
		assertEquals(Integer.MAX_VALUE,TypeManager.boundValueToType(type, Integer.MAX_VALUE),0);
		
		type = new UnsignedIntType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, Integer.MIN_VALUE),0);
		assertEquals(Integer.MAX_VALUE,TypeManager.boundValueToType(type, Integer.MAX_VALUE),0);
	}

}
