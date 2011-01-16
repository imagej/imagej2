package imagej.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class BooleanArrayAccessorTest
{
	private BooleanArrayAccessor accessor;
	
	@Test
	public void testBooleanArrayAccessor()
	{
		boolean[] bools = new boolean[]{};
		this.accessor = new BooleanArrayAccessor(bools);
		assertNotNull(this.accessor);
	}

	@Test
	public void testGetReal() {
		boolean[] bools = new boolean[]{true, true, false, false, true};
		this.accessor = new BooleanArrayAccessor(bools);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getReal(0), 0);
		assertEquals(1, this.accessor.getReal(1), 0);
		assertEquals(0, this.accessor.getReal(2), 0);
		assertEquals(0, this.accessor.getReal(3), 0);
		assertEquals(1, this.accessor.getReal(4), 0);
	}

	@Test
	public void testSetReal()
	{
		boolean[] bools = new boolean[]{true, false, false, false, false};
		this.accessor = new BooleanArrayAccessor(bools);
		assertNotNull(this.accessor);
		
		assertEquals(1, this.accessor.getReal(0), 0);
		this.accessor.setReal(0, 0);
		assertEquals(0, this.accessor.getReal(0), 0);
		
		assertEquals(0, this.accessor.getReal(1), 0);
		this.accessor.setReal(1, 1);
		assertEquals(1, this.accessor.getReal(1), 0);
		
		assertEquals(0, this.accessor.getReal(2), 0);
		this.accessor.setReal(2, 0.00001);
		assertEquals(1, this.accessor.getReal(2), 0);
		
		assertEquals(0, this.accessor.getReal(3), 0);
		this.accessor.setReal(3, Integer.MIN_VALUE);
		assertEquals(1, this.accessor.getReal(3), 0);
	}

	@Test
	public void testGetIntegral()
	{
		boolean[] bools = new boolean[]{false, false, true, true, false};
		this.accessor = new BooleanArrayAccessor(bools);
		assertNotNull(this.accessor);
		assertEquals(0, this.accessor.getIntegral(0));
		assertEquals(0, this.accessor.getIntegral(1));
		assertEquals(1, this.accessor.getIntegral(2));
		assertEquals(1, this.accessor.getIntegral(3));
		assertEquals(0, this.accessor.getIntegral(4));
	}

	@Test
	public void testSetIntegral()
	{
		boolean[] bools = new boolean[]{true, false, false, false, false};
		this.accessor = new BooleanArrayAccessor(bools);
		assertNotNull(this.accessor);
		
		assertEquals(1, this.accessor.getIntegral(0));
		this.accessor.setIntegral(0, 0);
		assertEquals(0, this.accessor.getIntegral(0));
		
		assertEquals(0, this.accessor.getIntegral(1));
		this.accessor.setIntegral(1, -1);
		assertEquals(1, this.accessor.getIntegral(1));
		
		assertEquals(0, this.accessor.getIntegral(2));
		this.accessor.setIntegral(2, Long.MAX_VALUE);
		assertEquals(1, this.accessor.getIntegral(2));
		
		assertEquals(0, this.accessor.getIntegral(3));
		this.accessor.setIntegral(3, 1);
		assertEquals(1, this.accessor.getIntegral(3));
	}

}
