package imagej.dataset;

import static org.junit.Assert.*;
import imagej.data.Type;
import imagej.data.Types;

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
		Type type;
		
		type = Types.findType("8-bit unsigned");
		
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
		assertEquals(0, ds.getDouble(new int[]{0,0,0,0}), 0);
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
		assertEquals(0, ds.getDouble(new int[]{0,0,0,0}), 0);
		assertNull(ds.getParent());
		assertEquals(ds, ds.getSubset(0).getParent());
		assertNotNull(ds.getSubset(new int[]{0}));
		assertNotNull(ds.getSubset(new int[]{0,0}));
	}

	@Test
	public void testGetData()
	{
		PlanarDatasetFactory factory = new PlanarDatasetFactory();
		
		Dataset ds;
		int[] dimensions;
		Type type;
		
		type = Types.findType("64-bit signed");
		
		dimensions = new int[]{1,2};
		ds = factory.createDataset(type, dimensions);

		assertFalse(ds.isComposite());
		assertNotNull(ds.getData());
		
		dimensions = new int[]{1,2,3};
		ds = factory.createDataset(type, dimensions);

		assertTrue(ds.isComposite());
		assertNull(ds.getData());
	}
	
	@Test
	public void testSetData()
	{
		PlanarDatasetFactory factory = new PlanarDatasetFactory();
		
		Dataset ds;
		int[] dimensions;
		Type type;
		Object array;
		
		type = Types.findType("16-bit unsigned");
		
		// should not be able to create a dataset of dim < 2
		dimensions = new int[]{1,2};
		
		ds = factory.createDataset(type, dimensions);
		
		// try a null array
		try {
			ds.setData(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try an array of the wrong type
		try {
			array = new int[2];
			ds.setData(array);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try an array that is too big
		try {
			array = type.allocateStorageArray(3);
			ds.setData(array);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try an array that is too small
		try {
			array = type.allocateStorageArray(1);
			ds.setData(array);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// try an array that is right size
		try {
			array = type.allocateStorageArray(2);
			ds.setData(array);
			assertTrue(true);
		} catch (Exception e) {
			fail();
		}
		
		// now try a CompositeDataset
		try {
			dimensions = new int[]{1,2,3};
			ds = factory.createDataset(type, dimensions);
			array = type.allocateStorageArray(6);
			ds.setData(array);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testSetAndGetDouble()
	{
		PlanarDatasetFactory factory = new PlanarDatasetFactory();
		
		Dataset ds;
		int[] dimensions;
		Type type;
		
		type = Types.findType("12-bit unsigned");
		dimensions = new int[]{2,2,3,5}; 
			
		ds = factory.createDataset(type, dimensions);
		
		assertEquals(0, ds.getDouble(new int[]{0,0,0,0}), 0);
		ds.setDouble(new int[]{0,0,0,0}, 14);
		assertEquals(14, ds.getDouble(new int[]{0,0,0,0}), 0);

		assertEquals(0, ds.getDouble(new int[]{1,1,1,1}), 0);
		ds.setDouble(new int[]{1,1,1,1}, 8);
		assertEquals(8, ds.getDouble(new int[]{1,1,1,1}), 0);

		assertEquals(0, ds.getDouble(new int[]{1,1,2,4}), 0);
		ds.setDouble(new int[]{1,1,2,4}, 99);
		assertEquals(99, ds.getDouble(new int[]{1,1,2,4}), 0);
	}	
	
	@Test
	public void testDuplicateDataset()
	{
		System.out.println("PlanarDatasetFactoryTest::testDuplicateDataset() has not been implemented.");
	}

}
