package imagej2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import imagej2.Utils;

import org.junit.Test;

public class UtilsTest {

	// *************  instance vars ********************************************

	int width = 224, height = 403;

	// *************  private helpers ********************************************


	private void getDims3AndGreaterShouldFail(int[] dims)
	{
		try {
			Utils.getDims3AndGreater(dims);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	private void verifyDims(boolean shouldFail, int[] dims, int[] origin, int[] span)
	{
		try {
			Utils.verifyDimensions(dims, origin, span);
			if (shouldFail)
				fail();
			else
				assertTrue(true);
		} catch (IllegalArgumentException e) {
			if (shouldFail)
				assertTrue(true);
			else
				fail();
		}
	}

	// *************  public tests ********************************************

	@Test
	public void testgetDims3AndGreater() {
		getDims3AndGreaterShouldFail(new int[]{});
		getDims3AndGreaterShouldFail(new int[]{1});
		assertArrayEquals(new int[]{}, Utils.getDims3AndGreater(new int[]{1,2}));
		assertArrayEquals(new int[]{3}, Utils.getDims3AndGreater(new int[]{1,2,3}));
		assertArrayEquals(new int[]{3,4}, Utils.getDims3AndGreater(new int[]{1,2,3,4}));
		assertArrayEquals(new int[]{3,4,5}, Utils.getDims3AndGreater(new int[]{1,2,3,4,5}));
	}

	@Test
	public void testGetTotalSamples() {
		assertEquals(0,Utils.getTotalSamples(new int[]{}));
		assertEquals(0,Utils.getTotalSamples(new int[]{0}));
		assertEquals(1,Utils.getTotalSamples(new int[]{1}));
		assertEquals(8,Utils.getTotalSamples(new int[]{8}));
		assertEquals(1,Utils.getTotalSamples(new int[]{1,1}));
		assertEquals(10,Utils.getTotalSamples(new int[]{2,5}));
		assertEquals(24,Utils.getTotalSamples(new int[]{2,3,4}));
		assertEquals(720,Utils.getTotalSamples(new int[]{1,2,3,4,5,6}));
	}

	@Test
	public void testGetTotalPlanes() {
		assertEquals(0,Utils.getTotalPlanes(new int[]{}));
		assertEquals(0,Utils.getTotalPlanes(new int[]{0}));
		assertEquals(0,Utils.getTotalPlanes(new int[]{1}));
		assertEquals(0,Utils.getTotalPlanes(new int[]{8}));
		assertEquals(1,Utils.getTotalPlanes(new int[]{1,1}));
		assertEquals(1,Utils.getTotalPlanes(new int[]{2,5}));
		assertEquals(4,Utils.getTotalPlanes(new int[]{2,3,4}));
		assertEquals(360,Utils.getTotalPlanes(new int[]{1,2,3,4,5,6}));
	}

	@Test
	public void testVerifyDimensions()
	{
		final boolean FAIL = true;

		// origin len != span len
		verifyDims(FAIL, new int[]{1}, new int[]{0}, new int[]{1,1});

		// origin len != dim len
		verifyDims(FAIL, new int[]{1}, new int[]{0,0}, new int[]{1,1});

		// dim len != span len
		verifyDims(FAIL, new int[]{1,2}, new int[]{0,0}, new int[]{1});

		// origin outside image in some dim
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,-1}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,3}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,-1,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,2,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{-1,0,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{1,0,0}, new int[]{1,1,1});

		// span <= 0 in some dim
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,-1}, new int[]{0,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,3}, new int[]{1,0,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,-1,0}, new int[]{1,1,0});

		// origin + span outside image in some dim
		verifyDims(FAIL, new int[]{1}, new int[]{0}, new int[]{2});
		verifyDims(FAIL, new int[]{2,2}, new int[]{0,1}, new int[]{2,2});
		verifyDims(FAIL, new int[]{2,2}, new int[]{1,0}, new int[]{2,2});

		// all other cases should succeed

		final boolean SUCCEED = false;

		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{2,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{1,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{2,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,0}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,0}, new int[]{1,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,1}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,1}, new int[]{2,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,1}, new int[]{1,1});
	}


}
