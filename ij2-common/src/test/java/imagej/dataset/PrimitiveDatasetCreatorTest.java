package imagej.dataset;

import static org.junit.Assert.*;
import imagej.data.Types;

import org.junit.Test;

public class PrimitiveDatasetCreatorTest
{
	private PrimitiveDatasetCreator creator;
	private DatasetFactory factory;
	
	@Test
	public void testPrimitiveDatasetCreator()
	{
		this.factory = new PlanarDatasetFactory();
		this.creator = new PrimitiveDatasetCreator(factory);
		assertNotNull(this.creator);
	}

	@Test
	public void testCreateDataset() {
		this.factory = new PlanarDatasetFactory();
		this.creator = new PrimitiveDatasetCreator(factory);
		assertNotNull(this.creator);
		
		Dataset ds;
		int[] dimensions;
		
		/* 
		 * 1-bit from boolean[] will not work since boolean[] does not match the Type's internal storage (which matches Imglib)
		 * 
		ds = this.creator.createDataset(dimensions, false, new boolean[]{false, true, true});
		assertEquals(Types.findType("1-bit signed"), ds.getType());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(0, ds.getLong(new int[]{0,0}));
		assertEquals(1, ds.getLong(new int[]{0,1}));
		assertEquals(1, ds.getLong(new int[]{0,2}));
		*/
		
		// signed 8-bit
		dimensions = new int[]{1,3};
		ds = this.creator.createDataset(dimensions, false, new byte[]{-5, 0, 5});
		assertFalse(ds.getType().isFloat());
		assertFalse(ds.getType().isUnsigned());
		assertEquals(Byte.MIN_VALUE, ds.getType().getMinIntegral());
		assertEquals(Byte.MAX_VALUE, ds.getType().getMaxIntegral());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(-5, ds.getLong(new int[]{0,0}));
		assertEquals(0, ds.getLong(new int[]{0,1}));
		assertEquals(5, ds.getLong(new int[]{0,2}));
		
		// unsigned 8-bit
		dimensions = new int[]{1,4};
		ds = this.creator.createDataset(dimensions, true, new byte[]{0, 0x7f, (byte)0xf0, (byte)0xff});
		assertFalse(ds.getType().isFloat());
		assertTrue(ds.getType().isUnsigned());
		assertEquals(0, ds.getType().getMinIntegral());
		assertEquals(255, ds.getType().getMaxIntegral());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(0, ds.getLong(new int[]{0,0}));
		assertEquals(0x7f, ds.getLong(new int[]{0,1}));
		assertEquals(0xf0, ds.getLong(new int[]{0,2}));
		assertEquals(0xff, ds.getLong(new int[]{0,3}));

		// signed 16-bit
		dimensions = new int[]{1,5};
		ds = this.creator.createDataset(dimensions, false, new short[]{Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE});
		assertFalse(ds.getType().isFloat());
		assertFalse(ds.getType().isUnsigned());
		assertEquals(Short.MIN_VALUE, ds.getType().getMinIntegral());
		assertEquals(Short.MAX_VALUE, ds.getType().getMaxIntegral());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(Short.MIN_VALUE, ds.getLong(new int[]{0,0}));
		assertEquals(-1, ds.getLong(new int[]{0,1}));
		assertEquals(0, ds.getLong(new int[]{0,2}));
		assertEquals(1, ds.getLong(new int[]{0,3}));
		assertEquals(Short.MAX_VALUE, ds.getLong(new int[]{0,4}));

		// unsigned 16-bit
		dimensions = new int[]{1,5};
		ds = this.creator.createDataset(dimensions, true, new short[]{0, 1, Short.MAX_VALUE, (short)(Short.MAX_VALUE+1), (short)0xffff});
		assertFalse(ds.getType().isFloat());
		assertTrue(ds.getType().isUnsigned());
		assertEquals(0, ds.getType().getMinIntegral());
		assertEquals(65535, ds.getType().getMaxIntegral());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(0, ds.getLong(new int[]{0,0}));
		assertEquals(1, ds.getLong(new int[]{0,1}));
		assertEquals(Short.MAX_VALUE, ds.getLong(new int[]{0,2}));
		assertEquals(Short.MAX_VALUE+1, ds.getLong(new int[]{0,3}));
		assertEquals(65535, ds.getLong(new int[]{0,4}));

		// signed 32-bit
		dimensions = new int[]{1,5};
		ds = this.creator.createDataset(dimensions, false, new int[]{Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE});
		assertFalse(ds.getType().isFloat());
		assertFalse(ds.getType().isUnsigned());
		assertEquals(Integer.MIN_VALUE, ds.getType().getMinIntegral());
		assertEquals(Integer.MAX_VALUE, ds.getType().getMaxIntegral());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(Integer.MIN_VALUE, ds.getLong(new int[]{0,0}));
		assertEquals(-1, ds.getLong(new int[]{0,1}));
		assertEquals(0, ds.getLong(new int[]{0,2}));
		assertEquals(1, ds.getLong(new int[]{0,3}));
		assertEquals(Integer.MAX_VALUE, ds.getLong(new int[]{0,4}));

		// unsigned 32-bit
		dimensions = new int[]{1,5};
		ds = this.creator.createDataset(dimensions, true, new int[]{0, 1, Integer.MAX_VALUE, (int)(1L + Integer.MAX_VALUE), (int)0xffffffffL});
		assertFalse(ds.getType().isFloat());
		assertTrue(ds.getType().isUnsigned());
		assertEquals(0, ds.getType().getMinIntegral());
		assertEquals(0xffffffffL, ds.getType().getMaxIntegral());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(0, ds.getLong(new int[]{0,0}));
		assertEquals(1, ds.getLong(new int[]{0,1}));
		assertEquals(Integer.MAX_VALUE, ds.getLong(new int[]{0,2}));
		assertEquals(1L+Integer.MAX_VALUE, ds.getLong(new int[]{0,3}));
		assertEquals(0xffffffffL, ds.getLong(new int[]{0,4}));

		// signed 64-bit
		dimensions = new int[]{1,5};
		ds = this.creator.createDataset(dimensions, false, new long[]{Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE});
		assertFalse(ds.getType().isFloat());
		assertFalse(ds.getType().isUnsigned());
		assertEquals(Long.MIN_VALUE, ds.getType().getMinIntegral());
		assertEquals(Long.MAX_VALUE, ds.getType().getMaxIntegral());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(Long.MIN_VALUE, ds.getLong(new int[]{0,0}));
		assertEquals(-1, ds.getLong(new int[]{0,1}));
		assertEquals(0, ds.getLong(new int[]{0,2}));
		assertEquals(1, ds.getLong(new int[]{0,3}));
		assertEquals(Long.MAX_VALUE, ds.getLong(new int[]{0,4}));

		// float 32-bit
		dimensions = new int[]{1,5};
		ds = this.creator.createDataset(dimensions, false, new float[]{-Float.MAX_VALUE, -1.5f, 0, 1.5f, Float.MAX_VALUE});
		assertTrue(ds.getType().isFloat());
		assertFalse(ds.getType().isUnsigned());
		assertEquals(-Float.MAX_VALUE, ds.getType().getMinReal(), 0);
		assertEquals(Float.MAX_VALUE, ds.getType().getMaxReal(), 0);
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(-Float.MAX_VALUE, ds.getDouble(new int[]{0,0}), 0);
		assertEquals(-1.5f, ds.getDouble(new int[]{0,1}), 0);
		assertEquals(0, ds.getDouble(new int[]{0,2}), 0);
		assertEquals(1.5f, ds.getDouble(new int[]{0,3}), 0);
		assertEquals(Float.MAX_VALUE, ds.getDouble(new int[]{0,4}), 0);

		// float 64-bit
		dimensions = new int[]{1,5};
		ds = this.creator.createDataset(dimensions, false, new double[]{-Double.MAX_VALUE, -1.5, 0, 1.5, Double.MAX_VALUE});
		assertTrue(ds.getType().isFloat());
		assertFalse(ds.getType().isUnsigned());
		assertEquals(-Double.MAX_VALUE, ds.getType().getMinReal(), 0);
		assertEquals(Double.MAX_VALUE, ds.getType().getMaxReal(), 0);
		assertArrayEquals(dimensions, ds.getDimensions());
		assertEquals(-Double.MAX_VALUE, ds.getDouble(new int[]{0,0}), 0);
		assertEquals(-1.5, ds.getDouble(new int[]{0,1}), 0);
		assertEquals(0, ds.getDouble(new int[]{0,2}), 0);
		assertEquals(1.5, ds.getDouble(new int[]{0,3}), 0);
		assertEquals(Double.MAX_VALUE, ds.getDouble(new int[]{0,4}), 0);
		
		// more than 1 row of values
		dimensions = new int[]{2,2};
		ds = this.creator.createDataset(dimensions, false, new double[]{-Double.MAX_VALUE, -1.5, 0, 1.5});
		assertEquals(-Double.MAX_VALUE, ds.getDouble(new int[]{0,0}), 0);
		assertEquals(-1.5, ds.getDouble(new int[]{1,0}), 0);
		assertEquals(0, ds.getDouble(new int[]{0,1}), 0);
		assertEquals(1.5, ds.getDouble(new int[]{1,1}), 0);
		
		dimensions = new int[]{2,2};

		// choose bad unsigned combo : unsigned long - throws exception
		try {
			ds = this.creator.createDataset(dimensions, true, new long[]{0,0,0,0});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// choose bad unsigned combo : unsigned float - throws exception
		try {
			ds = this.creator.createDataset(dimensions, true, new float[]{0,0,0,0});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// choose bad unsigned combo : unsigned double - throws exception
		try {
			ds = this.creator.createDataset(dimensions, true, new double[]{0,0,0,0});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// pass an array whose length does not match input data length
		try {
			ds = this.creator.createDataset(dimensions, true, new byte[]{0,0,0,0,0}); // 2x2 != 5
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// pass something that is not an Array
		try {
			ds = this.creator.createDataset(dimensions, false, "Not An Array");
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

}
