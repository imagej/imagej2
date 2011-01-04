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
		int[] values = new int[]{-1,-1,-1};
		this.accessor = new Unsigned12BitArrayAccessor(values);
		
		for (int v = 0; v < values.length; v++)
		{
			for (int i = 0; i < 4096; i++)
			{
				this.accessor.setReal(v, i);
				assertEquals(i, this.accessor.getReal(v), 0);
			}
			
			for (int i = -68000; i < 0; i+=1000)
			{
				this.accessor.setReal(v, 99);
				this.accessor.setReal(v, i);
				assertEquals(0, this.accessor.getReal(v), 0);
			}

			for (int i = 4096; i < 68000; i+=1000)
			{
				this.accessor.setReal(v, 99);
				this.accessor.setReal(v, i);
				assertEquals(4095, this.accessor.getReal(v), 0);
			}
		}
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
		int[] values = new int[]{-1,-1,-1};
		this.accessor = new Unsigned12BitArrayAccessor(values);
		
		for (int v = 0; v < values.length; v++)
		{
			for (int i = 0; i < 4096; i++)
			{
				this.accessor.setIntegral(v, i);
				assertEquals(i, this.accessor.getIntegral(v));
			}
			
			for (int i = -68000; i < 0; i+=1000)
			{
				this.accessor.setIntegral(v, 99);
				this.accessor.setIntegral(v, i);
				assertEquals(0, this.accessor.getIntegral(v));
			}

			for (int i = 4096; i < 68000; i+=1000)
			{
				this.accessor.setIntegral(v, 99);
				this.accessor.setIntegral(v, i);
				assertEquals(4095, this.accessor.getIntegral(v));
			}
		}
	}

}
