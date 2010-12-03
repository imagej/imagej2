package imagej.dataset;

import static org.junit.Assert.*;
import imagej.UserType;

import org.junit.Test;

public class PlanarDatasetFactoryTest
{
	@Test
	public void testPlanarDatasetFactory()
	{
		PlanarDatasetFactory factory = new PlanarDatasetFactory();
		assertNotNull(factory);
	}

	@Test
	public void testCreateDataset()
	{
		PlanarDatasetFactory factory = new PlanarDatasetFactory();
		
		Dataset ds;
		int[] dimensions;
		UserType type;
		
		type = UserType.UBYTE;
		
		// should not be able to create a dataset of dim < 2
		dimensions = new int[]{};
		try {
			ds = factory.createDataset(type, dimensions);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// should not be able to create a dataset of dim < 2
		dimensions = new int[]{1};
		try {
			ds = factory.createDataset(type, dimensions);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		dimensions = new int[]{1,2};
		ds = factory.createDataset(type, dimensions);
		assertNotNull(ds);
		assertEquals(type, ds.getType());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertNotNull(ds.getData());
		assertEquals(0, ds.getDouble(new int[]{0,0}), 0);
		assertNull(ds.getParent());
		try {
			ds.getSubset(0);
			fail();
		} catch (UnsupportedOperationException e) {
			assertTrue(true);
		}
		try {
			ds.getSubset(new int[]{0});
			fail();
		} catch (UnsupportedOperationException e) {
			assertTrue(true);
		}
		
		dimensions = new int[]{1,2,3};
		ds = factory.createDataset(type, dimensions);
		assertNotNull(ds);
		assertEquals(type, ds.getType());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertNull(ds.getData());
		assertNotNull(ds.getSubset(new int[]{0}).getData());
		assertEquals(0, ds.getDouble(new int[]{0,0,0}), 0);
		assertNull(ds.getParent());
		assertEquals(ds, ds.getSubset(0).getParent());
		assertNotNull(ds.getSubset(new int[]{0}));
		
		dimensions = new int[]{1,2,3,4};
		ds = factory.createDataset(type, dimensions);
		assertNotNull(ds);
		assertEquals(type, ds.getType());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertNull(ds.getData());
		assertNotNull(ds.getSubset(new int[]{0,0}).getData());
		// TODO - reenable
		//assertEquals(0, ds.getDouble(new int[]{0,0,0,0}), 0);
		assertNull(ds.getParent());
		assertEquals(ds, ds.getSubset(0).getParent());
		assertNotNull(ds.getSubset(new int[]{0}));
		assertNotNull(ds.getSubset(new int[]{0,0}));
		
		dimensions = new int[]{4,3,2,1};
		ds = factory.createDataset(type, dimensions);
		assertNotNull(ds);
		assertEquals(type, ds.getType());
		assertArrayEquals(dimensions, ds.getDimensions());
		assertNull(ds.getData());
		assertNotNull(ds.getSubset(new int[]{0,0}).getData());
		// TODO - reenable
		// assertEquals(0, ds.getDouble(new int[]{0,0,0,0}), 0);
		assertNull(ds.getParent());
		assertEquals(ds, ds.getSubset(0).getParent());
		assertNotNull(ds.getSubset(new int[]{0}));
		assertNotNull(ds.getSubset(new int[]{0,0}));
	}

	@Test
	public void testDuplicateDataset()
	{
		// TODO - once method implemented add tests here
	}

}
