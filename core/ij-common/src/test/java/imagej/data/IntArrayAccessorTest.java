package imagej.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntArrayAccessorTest {

	private IntArrayAccessor accessor;
	
	@Test
	public void testByteArrayAccessor() {
		int[] data = new int[]{1,2,3,4};
		this.accessor = new IntArrayAccessor(data);
		assertNotNull(this.accessor);
	}

	@Test
	public void testGetReal() {
		int[] data = new int[]{1,2,3,4};
		this.accessor = new IntArrayAccessor(data);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getReal(0), 0);
		assertEquals(2, this.accessor.getReal(1), 0);
		assertEquals(3, this.accessor.getReal(2), 0);
		assertEquals(4, this.accessor.getReal(3), 0);
	}

	@Test
	public void testGetIntegral() {
		int[] data = new int[]{1,2,3,4};
		this.accessor = new IntArrayAccessor(data);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getIntegral(0));
		assertEquals(2, this.accessor.getIntegral(1));
		assertEquals(3, this.accessor.getIntegral(2));
		assertEquals(4, this.accessor.getIntegral(3));
	}

	@Test
	public void testSetReal() {
		int[] data = new int[]{1,2,3,4,5};
		this.accessor = new IntArrayAccessor(data);
		assertNotNull(this.accessor);
		
		this.accessor.setReal(0, Integer.MIN_VALUE);
		assertEquals(Integer.MIN_VALUE, this.accessor.getReal(0), 0);
		
		this.accessor.setReal(1, Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, this.accessor.getReal(1), 0);
		
		this.accessor.setReal(2, -13.4);
		assertEquals(-13, this.accessor.getReal(2), 0);
		
		this.accessor.setReal(3, 68.6);
		assertEquals(69, this.accessor.getReal(3), 0);
		
		this.accessor.setReal(4, 0);
		assertEquals(0, this.accessor.getReal(4), 0);
	}

	@Test
	public void testSetIntegral() {
		int[] data = new int[]{1,2,3,4,5};
		this.accessor = new IntArrayAccessor(data);
		assertNotNull(this.accessor);
		
		this.accessor.setIntegral(0, Integer.MIN_VALUE);
		assertEquals(Integer.MIN_VALUE, this.accessor.getIntegral(0));
		
		this.accessor.setIntegral(1, Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, this.accessor.getIntegral(1));
		
		this.accessor.setIntegral(2, -13);
		assertEquals(-13, this.accessor.getIntegral(2));
		
		this.accessor.setIntegral(3, 68);
		assertEquals(68, this.accessor.getIntegral(3));
		
		this.accessor.setIntegral(4, 0);
		assertEquals(0, this.accessor.getIntegral(4));
	}

}
