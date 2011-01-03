package imagej.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class BitArrayAccessorTest {

	private BitArrayAccessor accessor;
	
	@Test
	public void testBitArrayAccessor() {
		this.accessor = new BitArrayAccessor(new int[]{});
		assertNotNull(this.accessor);
	}

	@Test
	public void testGetReal() {
		int[] values = new int[]{1,2,3};
		this.accessor = new BitArrayAccessor(values);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getReal(0), 0);
		for (int i = 1; i < 33; i++)
			assertEquals(0, this.accessor.getReal(i), 0);
		assertEquals(1, this.accessor.getReal(33), 0);
		for (int i = 34; i < 64; i++)
			assertEquals(0, this.accessor.getReal(i), 0);
		assertEquals(1, this.accessor.getReal(64), 0);
		assertEquals(1, this.accessor.getReal(65), 0);
		for (int i = 66; i < 96; i++)
			assertEquals(0, this.accessor.getReal(i), 0);
	}

	@Test
	public void testSetReal() {
		int[] values = new int[]{0,0,0};
		this.accessor = new BitArrayAccessor(values);
		
		// try bit 0
		assertEquals(0, this.accessor.getReal(0),  0);
		this.accessor.setReal(0, 1);
		assertEquals(1, this.accessor.getReal(0),  0);  // set it to 1
		this.accessor.setReal(0, -1);
		assertEquals(0, this.accessor.getReal(0),  0);  // set it to value out of lower range
		this.accessor.setReal(0, 2);
		assertEquals(1, this.accessor.getReal(0),  0);  // set it to value out of upper range

		// try bit 6
		assertEquals(0, this.accessor.getReal(6),  0);
		this.accessor.setReal(6, 1);
		assertEquals(1, this.accessor.getReal(6),  0);  // set it to 1
		this.accessor.setReal(6, -1);
		assertEquals(0, this.accessor.getReal(6),  0);  // set it to value out of lower range
		this.accessor.setReal(6, 2);
		assertEquals(1, this.accessor.getReal(6),  0);  // set it to value out of upper range

		// try bit 15
		assertEquals(0, this.accessor.getReal(15),  0);
		this.accessor.setReal(15, 1);
		assertEquals(1, this.accessor.getReal(15),  0);  // set it to 1
		this.accessor.setReal(15, -1);
		assertEquals(0, this.accessor.getReal(15),  0);  // set it to value out of lower range
		this.accessor.setReal(15, 2);
		assertEquals(1, this.accessor.getReal(15),  0);  // set it to value out of upper range
		
		// try bit 95
		assertEquals(0, this.accessor.getReal(95),  0);
		this.accessor.setReal(95, 1);
		assertEquals(1, this.accessor.getReal(95),  0);  // set it to 1
		this.accessor.setReal(95, -1);
		assertEquals(0, this.accessor.getReal(95),  0);  // set it to value out of lower range
		this.accessor.setReal(95, 2);
		assertEquals(1, this.accessor.getReal(95),  0);  // set it to value out of upper range
	}

	@Test
	public void testGetIntegral() {
		int[] values = new int[]{1,2,3};
		this.accessor = new BitArrayAccessor(values);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getIntegral(0), 0);
		for (int i = 1; i < 33; i++)
			assertEquals(0, this.accessor.getIntegral(i), 0);
		assertEquals(1, this.accessor.getIntegral(33), 0);
		for (int i = 34; i < 64; i++)
			assertEquals(0, this.accessor.getIntegral(i), 0);
		assertEquals(1, this.accessor.getIntegral(64), 0);
		assertEquals(1, this.accessor.getIntegral(65), 0);
		for (int i = 66; i < 96; i++)
			assertEquals(0, this.accessor.getIntegral(i), 0);
	}

	@Test
	public void testSetIntegral() {
		int[] values = new int[]{0,0,0};
		this.accessor = new BitArrayAccessor(values);
		
		// try bit 0
		assertEquals(0, this.accessor.getIntegral(0),  0);
		this.accessor.setIntegral(0, 1);
		assertEquals(1, this.accessor.getIntegral(0),  0);  // set it to 1
		this.accessor.setIntegral(0, -1);
		assertEquals(0, this.accessor.getIntegral(0),  0);  // set it to value out of lower range
		this.accessor.setIntegral(0, 2);
		assertEquals(1, this.accessor.getIntegral(0),  0);  // set it to value out of upper range

		// try bit 6
		assertEquals(0, this.accessor.getIntegral(6),  0);
		this.accessor.setIntegral(6, 1);
		assertEquals(1, this.accessor.getIntegral(6),  0);  // set it to 1
		this.accessor.setIntegral(6, -1);
		assertEquals(0, this.accessor.getIntegral(6),  0);  // set it to value out of lower range
		this.accessor.setIntegral(6, 2);
		assertEquals(1, this.accessor.getIntegral(6),  0);  // set it to value out of upper range

		// try bit 15
		assertEquals(0, this.accessor.getIntegral(15),  0);
		this.accessor.setIntegral(15, 1);
		assertEquals(1, this.accessor.getIntegral(15),  0);  // set it to 1
		this.accessor.setIntegral(15, -1);
		assertEquals(0, this.accessor.getIntegral(15),  0);  // set it to value out of lower range
		this.accessor.setIntegral(15, 2);
		assertEquals(1, this.accessor.getIntegral(15),  0);  // set it to value out of upper range
		
		// try bit 95
		assertEquals(0, this.accessor.getIntegral(95),  0);
		this.accessor.setIntegral(95, 1);
		assertEquals(1, this.accessor.getIntegral(95),  0);  // set it to 1
		this.accessor.setIntegral(95, -1);
		assertEquals(0, this.accessor.getIntegral(95),  0);  // set it to value out of lower range
		this.accessor.setIntegral(95, 2);
		assertEquals(1, this.accessor.getIntegral(95),  0);  // set it to value out of upper range
	}

}
