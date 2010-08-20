package ij.process;

import static org.junit.Assert.*;
import ij.Assert;

import mpicbg.imglib.type.numeric.integer.ByteType;

import org.junit.Test;

public class ImagePropertiesTest {

	@Test
	public void testGetAndSetBackgroundValue()
	{
		ImageProperties props = new ImageProperties();
		assertEquals(0,props.getBackgroundValue(),Assert.DOUBLE_TOL);
		
		props.setBackgroundValue(403);
		assertEquals(403,props.getBackgroundValue(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetAndSetPlanePosition() {
		ImageProperties props = new ImageProperties();
		assertEquals(null,props.getPlanePosition());
		
		int[] plane = new int[]{1,4,0,0,3,5,2,17};
		props.setPlanePosition(plane);
		assertArrayEquals(plane,props.getPlanePosition());
	}
}
