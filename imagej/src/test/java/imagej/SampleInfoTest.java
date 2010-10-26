package imagej;

import static org.junit.Assert.*;

import org.junit.Test;

import imagej.SampleInfo.ValueType;

public class SampleInfoTest
{

	@Test
	public void testExistence()  // compile time check
	{
		assertEquals(ValueType.BIT, ValueType.BIT);
		assertEquals(ValueType.BYTE, ValueType.BYTE);
		assertEquals(ValueType.UBYTE, ValueType.UBYTE);
		assertEquals(ValueType.UINT12, ValueType.UINT12);
		assertEquals(ValueType.SHORT, ValueType.SHORT);
		assertEquals(ValueType.USHORT, ValueType.USHORT);
		assertEquals(ValueType.INT, ValueType.INT);
		assertEquals(ValueType.UINT, ValueType.UINT);
		assertEquals(ValueType.FLOAT, ValueType.FLOAT);
		assertEquals(ValueType.DOUBLE, ValueType.DOUBLE);
		assertEquals(ValueType.LONG, ValueType.LONG);
	}
}
