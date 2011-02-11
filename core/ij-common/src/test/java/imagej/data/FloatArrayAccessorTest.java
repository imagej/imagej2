package imagej.data;

import static org.junit.Assert.*;
import imagej.types.FloatArrayAccessor;

import org.junit.Test;

public class FloatArrayAccessorTest {

	private static final double TOL = 0.00001;
	
	private FloatArrayAccessor accessor;
	
	@Test
	public void testByteArrayAccessor() {
		float[] data = new float[]{1.1f,2.2f,3.3f,4.4f,5.5f};
		this.accessor = new FloatArrayAccessor(data);
		assertNotNull(this.accessor);
	}

	@Test
	public void testGetReal() {
		float[] data = new float[]{1.1f,2.2f,3.3f,4.4f,5.5f};
		this.accessor = new FloatArrayAccessor(data);
		assertNotNull(this.accessor);
		assertEquals(1.1, this.accessor.getReal(0), TOL);
		assertEquals(2.2, this.accessor.getReal(1), TOL);
		assertEquals(3.3, this.accessor.getReal(2), TOL);
		assertEquals(4.4, this.accessor.getReal(3), TOL);
	}

	@Test
	public void testGetIntegral() {
		float[] data = new float[]{1.1f,2.2f,3.3f,4.4f,5.5f};
		this.accessor = new FloatArrayAccessor(data);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getIntegral(0));
		assertEquals(2, this.accessor.getIntegral(1));
		assertEquals(3, this.accessor.getIntegral(2));
		assertEquals(4, this.accessor.getIntegral(3));
	}

	@Test
	public void testSetReal() {
		float[] data = new float[]{1.1f,2.2f,3.3f,4.4f,5.5f};
		this.accessor = new FloatArrayAccessor(data);
		assertNotNull(this.accessor);
		
		this.accessor.setReal(0, -Double.MAX_VALUE);
		assertEquals(-Float.MAX_VALUE, this.accessor.getReal(0), TOL);
		
		this.accessor.setReal(1, -1.93);
		assertEquals(-1.93, this.accessor.getReal(1), TOL);
		
		this.accessor.setReal(2, 0);
		assertEquals(0, this.accessor.getReal(2), TOL);
		
		this.accessor.setReal(3, 14.445);
		assertEquals(14.445, this.accessor.getReal(3), TOL);
		
		this.accessor.setReal(4, Double.MAX_VALUE);
		assertEquals(Float.MAX_VALUE, this.accessor.getReal(4), TOL);
	}

	@Test
	public void testSetIntegral() {
		float[] data = new float[]{1.1f,2.2f,3.3f,4.4f,5.5f};
		this.accessor = new FloatArrayAccessor(data);
		assertNotNull(this.accessor);
		
		this.accessor.setIntegral(0, (long)-Double.MAX_VALUE);
		assertEquals((long)-Float.MAX_VALUE, this.accessor.getIntegral(0));
		
		this.accessor.setIntegral(1, -1);
		assertEquals(-1, this.accessor.getIntegral(1));
		
		this.accessor.setIntegral(2, 0);
		assertEquals(0, this.accessor.getIntegral(2));
		
		this.accessor.setIntegral(3, 14);
		assertEquals(14, this.accessor.getIntegral(3));
		
		this.accessor.setIntegral(4, (long)Double.MAX_VALUE);
		assertEquals((long)Float.MAX_VALUE, this.accessor.getIntegral(4));
	}

}
