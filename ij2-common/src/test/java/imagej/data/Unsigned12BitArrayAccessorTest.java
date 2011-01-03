package imagej.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class Unsigned12BitArrayAccessorTest {

	private Unsigned12BitArrayAccessor accessor;
	
	@Test
	public void testUnsigned12BitArrayAccessor() {
		this.accessor = new Unsigned12BitArrayAccessor(new int[]{});
		assertNotNull(this.accessor);
	}

	@Test
	public void testGetReal() {
		int[] values = new int[]{1,2,3};
		this.accessor = new Unsigned12BitArrayAccessor(values);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getReal(0), 0);
		assertEquals(0, this.accessor.getReal(1), 0);
		assertEquals(512, this.accessor.getReal(2), 0);
		assertEquals(0, this.accessor.getReal(3), 0);
		assertEquals(0, this.accessor.getReal(4), 0);
		assertEquals(48, this.accessor.getReal(5), 0);
		assertEquals(0, this.accessor.getReal(6), 0);
		assertEquals(0, this.accessor.getReal(7), 0);
	}

	@Test
	public void testSetReal() {
		int[] values = new int[]{0,0,0};
		this.accessor = new Unsigned12BitArrayAccessor(values);
		
		// try entry 0
		assertEquals(0, this.accessor.getReal(0),  0);
		this.accessor.setReal(0, 23);
		assertEquals(23, this.accessor.getReal(0),  0);  // set it to 1
		this.accessor.setReal(0, -1);
		assertEquals(0, this.accessor.getReal(0),  0);  // set it to value out of lower range
		this.accessor.setReal(0, 4096);
		assertEquals(4095, this.accessor.getReal(0),  0);  // set it to value out of upper range

		// try entry 3
		assertEquals(0, this.accessor.getReal(3),  0);
		this.accessor.setReal(3, 145);
		assertEquals(145, this.accessor.getReal(3),  0);  // set it to 1
		this.accessor.setReal(3, -1);
		assertEquals(0, this.accessor.getReal(3),  0);  // set it to value out of lower range
		this.accessor.setReal(3, 4096);
		assertEquals(4095, this.accessor.getReal(3),  0);  // set it to value out of upper range

		// try entry 5
		assertEquals(0, this.accessor.getReal(5),  0);
		this.accessor.setReal(5, 2010);
		assertEquals(2010, this.accessor.getReal(5),  0);  // set it to 1
		this.accessor.setReal(5, -1);
		assertEquals(0, this.accessor.getReal(5),  0);  // set it to value out of lower range
		this.accessor.setReal(5, 4096);
		assertEquals(4095, this.accessor.getReal(5),  0);  // set it to value out of upper range
		
		// try entry 7
		assertEquals(0, this.accessor.getReal(7),  0);
		this.accessor.setReal(7, 3886);
		assertEquals(3886, this.accessor.getReal(7),  0);  // set it to 1
		this.accessor.setReal(7, -1);
		assertEquals(0, this.accessor.getReal(7),  0);  // set it to value out of lower range
		this.accessor.setReal(7, 4096);
		assertEquals(4095, this.accessor.getReal(7),  0);  // set it to value out of upper range
	}

	@Test
	public void testGetIntegral() {
		int[] values = new int[]{1,2,3};
		this.accessor = new Unsigned12BitArrayAccessor(values);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getIntegral(0), 0);
		assertEquals(0, this.accessor.getIntegral(1), 0);
		assertEquals(512, this.accessor.getIntegral(2), 0);
		assertEquals(0, this.accessor.getIntegral(3), 0);
		assertEquals(0, this.accessor.getIntegral(4), 0);
		assertEquals(48, this.accessor.getIntegral(5), 0);
		assertEquals(0, this.accessor.getIntegral(6), 0);
		assertEquals(0, this.accessor.getIntegral(7), 0);
	}

	@Test
	public void testSetIntegral() {
		int[] values = new int[]{0,0,0};
		this.accessor = new Unsigned12BitArrayAccessor(values);
		
		// try entry 0
		assertEquals(0, this.accessor.getReal(0),  0);
		this.accessor.setReal(0, 999);
		assertEquals(999, this.accessor.getReal(0),  0);  // set it to 1
		this.accessor.setReal(0, -1);
		assertEquals(0, this.accessor.getReal(0),  0);  // set it to value out of lower range
		this.accessor.setReal(0, 4096);
		assertEquals(4095, this.accessor.getReal(0),  0);  // set it to value out of upper range

		// try entry 1
		assertEquals(0, this.accessor.getReal(1),  0);
		this.accessor.setReal(1, 4040);
		assertEquals(4040, this.accessor.getReal(1),  0);  // set it to 1
		this.accessor.setReal(1, -1);
		assertEquals(0, this.accessor.getReal(1),  0);  // set it to value out of lower range
		this.accessor.setReal(1, 4096);
		assertEquals(4095, this.accessor.getReal(1),  0);  // set it to value out of upper range

		// try entry 4
		assertEquals(0, this.accessor.getReal(4),  0);
		this.accessor.setReal(4, 3333);
		assertEquals(3333, this.accessor.getReal(4),  0);  // set it to 1
		this.accessor.setReal(4, -1);
		assertEquals(0, this.accessor.getReal(4),  0);  // set it to value out of lower range
		this.accessor.setReal(4, 4096);
		assertEquals(4095, this.accessor.getReal(4),  0);  // set it to value out of upper range
		
		// try entry 6
		assertEquals(0, this.accessor.getReal(6),  0);
		this.accessor.setReal(6, 2422);
		assertEquals(2422, this.accessor.getReal(6),  0);  // set it to 1
		this.accessor.setReal(6, -1);
		assertEquals(0, this.accessor.getReal(6),  0);  // set it to value out of lower range
		this.accessor.setReal(6, 4096);
		assertEquals(4095, this.accessor.getReal(6),  0);  // set it to value out of upper range
	}

}
