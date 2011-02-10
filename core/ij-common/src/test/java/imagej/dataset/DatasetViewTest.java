package imagej.dataset;

import static org.junit.Assert.*;
import imagej.MetaData;
import imagej.data.Types;

import org.junit.Test;

public class DatasetViewTest {

	private DatasetView view;
	private Dataset parentDS;
	private PlanarDatasetFactory factory = new PlanarDatasetFactory();

	private void createData(String type, int[] dimensions, int[] viewAxesValues)
	{
		parentDS = factory.createDataset(Types.findType(type), dimensions);
		view = new DatasetView(parentDS, viewAxesValues);
	}
	
	@Test
	public void testDatasetView()
	{
		// one with mismatched parent dimensions/axes
		try {
			createData("8-bit unsigned", new int[]{20,50}, new int[]{5});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// one with no unfixed axes
		try {
			createData("8-bit unsigned", new int[]{20,50}, new int[]{5,10});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// one that is correctly defined : 2d data, 1d view
		createData("8-bit unsigned", new int[]{20,50}, new int[]{5,-1});
		assertNotNull(view);

		// one that is correctly defined : 2d data, 1d view
		createData("8-bit signed", new int[]{20,50}, new int[]{-1,7});
		assertNotNull(view);

		// one that is correctly defined : 3d data, 2d view
		createData("12-bit unsigned", new int[]{10,20,30}, new int[]{-1,-1,7});
		assertNotNull(view);

		// one that is correctly defined : 3d data, 2d view
		createData("16-bit unsigned", new int[]{10,20,30}, new int[]{-1,7,-1});
		assertNotNull(view);

		// one that is correctly defined : 3d data, 2d view
		createData("16-bit signed", new int[]{10,20,30}, new int[]{7,-1,-1});
		assertNotNull(view);

		// one that is correctly defined : 3d data, 1d view
		createData("32-bit signed", new int[]{10,20,30}, new int[]{7,7,-1});
		assertNotNull(view);

		// one that is correctly defined : 3d data, 1d view
		createData("32-bit float", new int[]{10,20,30}, new int[]{7,-1,7});
		assertNotNull(view);

		// one that is correctly defined : 3d data, 1d view
		createData("64-bit float", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
	}

	@Test
	public void testGetDimensions() {
		// one that is correctly defined : 2d data, 1d view
		createData("8-bit unsigned", new int[]{20,50}, new int[]{5,-1});
		assertNotNull(view);
		assertArrayEquals(new int[]{50}, view.getDimensions());

		// one that is correctly defined : 2d data, 1d view
		createData("8-bit signed", new int[]{20,50}, new int[]{-1,7});
		assertNotNull(view);
		assertArrayEquals(new int[]{20}, view.getDimensions());

		// one that is correctly defined : 3d data, 2d view
		createData("12-bit unsigned", new int[]{10,20,30}, new int[]{-1,-1,7});
		assertNotNull(view);
		assertArrayEquals(new int[]{10,20}, view.getDimensions());

		// one that is correctly defined : 3d data, 2d view
		createData("16-bit unsigned", new int[]{10,20,30}, new int[]{-1,7,-1});
		assertNotNull(view);
		assertArrayEquals(new int[]{10,30}, view.getDimensions());

		// one that is correctly defined : 3d data, 2d view
		createData("16-bit signed", new int[]{10,20,30}, new int[]{7,-1,-1});
		assertNotNull(view);
		assertArrayEquals(new int[]{20,30}, view.getDimensions());

		// one that is correctly defined : 3d data, 1d view
		createData("32-bit signed", new int[]{10,20,30}, new int[]{7,7,-1});
		assertNotNull(view);
		assertArrayEquals(new int[]{30}, view.getDimensions());

		// one that is correctly defined : 3d data, 1d view
		createData("32-bit float", new int[]{10,20,30}, new int[]{7,-1,7});
		assertNotNull(view);
		assertArrayEquals(new int[]{20}, view.getDimensions());

		// one that is correctly defined : 3d data, 1d view
		createData("64-bit float", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		assertArrayEquals(new int[]{10}, view.getDimensions());
	}

	@Test
	public void testGetType() {
		// one that is correctly defined : 2d data, 1d view
		createData("8-bit unsigned", new int[]{20,50}, new int[]{5,-1});
		assertNotNull(view);
		assertEquals(Types.findType("8-bit unsigned"), view.getType());

		// one that is correctly defined : 2d data, 1d view
		createData("8-bit signed", new int[]{20,50}, new int[]{-1,7});
		assertNotNull(view);
		assertEquals(Types.findType("8-bit signed"), view.getType());

		// one that is correctly defined : 3d data, 2d view
		createData("12-bit unsigned", new int[]{10,20,30}, new int[]{-1,-1,7});
		assertNotNull(view);
		assertEquals(Types.findType("12-bit unsigned"), view.getType());

		// one that is correctly defined : 3d data, 2d view
		createData("16-bit unsigned", new int[]{10,20,30}, new int[]{-1,7,-1});
		assertNotNull(view);
		assertEquals(Types.findType("16-bit unsigned"), view.getType());

		// one that is correctly defined : 3d data, 2d view
		createData("16-bit signed", new int[]{10,20,30}, new int[]{7,-1,-1});
		assertNotNull(view);
		assertEquals(Types.findType("16-bit signed"), view.getType());

		// one that is correctly defined : 3d data, 1d view
		createData("32-bit signed", new int[]{10,20,30}, new int[]{7,7,-1});
		assertNotNull(view);
		assertEquals(Types.findType("32-bit signed"), view.getType());

		// one that is correctly defined : 3d data, 1d view
		createData("32-bit float", new int[]{10,20,30}, new int[]{7,-1,7});
		assertNotNull(view);
		assertEquals(Types.findType("32-bit float"), view.getType());

		// one that is correctly defined : 3d data, 1d view
		createData("64-bit float", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		assertEquals(Types.findType("64-bit float"), view.getType());
	}

	@Test
	public void testGetMetaData() {
		// one that is correctly defined : 3d data, 1d view
		createData("64-bit signed", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		assertNotNull(view.getMetaData());
	}

	@Test
	public void testSetMetaData() {
		// one that is correctly defined : 3d data, 1d view
		createData("64-bit signed", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		assertNotNull(view.getMetaData());
		MetaData metadata = new MetaData();
		view.setMetaData(metadata);
		assertEquals(metadata, view.getMetaData());
	}

	@Test
	public void testIsComposite() {
		// one that is correctly defined : 3d data, 1d view
		createData("32-bit unsigned", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		assertTrue(view.isComposite());
	}

	@Test
	public void testGetParent() {
		// one that is correctly defined : 3d data, 1d view
		createData("32-bit unsigned", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		assertNull(view.getParent());
	}

	@Test
	public void testSetParent() {
		// one that is correctly defined : 3d data, 1d view
		createData("32-bit unsigned", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		assertNull(view.getParent());
		Dataset ds = this.factory.createDataset(Types.findType("32-bit unsigned"), new int[]{1,2});
		view.setParent(ds);  // NOTE - this construction is somewhat nonsense. we're just testing that we can set the reference.
		assertEquals(ds, view.getParent());
	}

	@Test
	public void testGetData() {
		// one that is correctly defined : 3d data, 1d view
		createData("32-bit unsigned", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		assertNull(view.getData());
	}

	@Test
	public void testReleaseData() {
		// one that is correctly defined : 3d data, 1d view
		createData("32-bit unsigned", new int[]{10,20,30}, new int[]{-1,7,7});
		assertNotNull(view);
		view.releaseData();
		assertTrue(true);
	}

	@Test
	public void testSetData() {
		// one that is correctly defined : 3d data, 1d view
		createData("32-bit unsigned", new int[]{5,5,5}, new int[]{-1,2,2});
		assertNotNull(view);
		try {
			int[] ints = new int[4];
			view.setData(ints);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testInsertNewSubset() {
		// one that is correctly defined : 3d data, 1d view
		createData("32-bit unsigned", new int[]{5,5,5}, new int[]{-1,2,2});
		assertNotNull(view);
		try {
			view.insertNewSubset(2);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testRemoveSubset() {
		// one that is correctly defined : 3d data, 1d view
		createData("32-bit unsigned", new int[]{5,5,5}, new int[]{-1,2,2});
		assertNotNull(view);
		try {
			view.removeSubset(2);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetSubsetInt() {
		// one that is correctly defined : 4d data, 3d view
		createData("32-bit unsigned", new int[]{5,5,5,5}, new int[]{-1,-1,-1,2});
		assertNotNull(view);
		assertEquals(parentDS.getSubset(new int[]{0,2}), view.getSubset(0));
		assertEquals(parentDS.getSubset(new int[]{1,2}), view.getSubset(1));
		assertEquals(parentDS.getSubset(new int[]{2,2}), view.getSubset(2));
		assertEquals(parentDS.getSubset(new int[]{3,2}), view.getSubset(3));
		assertEquals(parentDS.getSubset(new int[]{4,2}), view.getSubset(4));

		// one that is correctly defined : 4d data, 3d view
		createData("32-bit unsigned", new int[]{5,5,5,5}, new int[]{-1,-1,2,-1});
		assertNotNull(view);
		assertEquals(parentDS.getSubset(new int[]{2,0}), view.getSubset(0));
		assertEquals(parentDS.getSubset(new int[]{2,1}), view.getSubset(1));
		assertEquals(parentDS.getSubset(new int[]{2,2}), view.getSubset(2));
		assertEquals(parentDS.getSubset(new int[]{2,3}), view.getSubset(3));
		assertEquals(parentDS.getSubset(new int[]{2,4}), view.getSubset(4));

		// one that is correctly defined : 4d data, 3d view but too many fixed indices
		createData("32-bit unsigned", new int[]{5,5,5,5}, new int[]{-1,2,-1,-1});
		assertNotNull(view);
		try {
			view.getSubset(0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// another one that is correctly defined : 4d data, 3d view but too many fixed indices
		createData("32-bit unsigned", new int[]{5,5,5,5}, new int[]{2,-1,-1,-1});
		assertNotNull(view);
		try {
			view.getSubset(0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetSubsetIntArray() {
		createData("64-bit float", new int[]{3,3,3,3,3}, new int[]{-1,-1,-1,-1,2});
		assertNotNull(view);

		assertEquals(parentDS.getSubset(new int[]{0,2}), view.getSubset(new int[]{0}));
		assertEquals(parentDS.getSubset(new int[]{1,2}), view.getSubset(new int[]{1}));
		assertEquals(parentDS.getSubset(new int[]{2,2}), view.getSubset(new int[]{2}));
		assertEquals(parentDS.getSubset(new int[]{0,0,2}), view.getSubset(new int[]{0,0}));
		assertEquals(parentDS.getSubset(new int[]{1,1,2}), view.getSubset(new int[]{1,1}));
		assertEquals(parentDS.getSubset(new int[]{2,2,2}), view.getSubset(new int[]{2,2}));

		createData("64-bit float", new int[]{3,3,3,3,3}, new int[]{-1,-1,-1,2,-1});
		assertNotNull(view);

		assertEquals(parentDS.getSubset(new int[]{2,0}), view.getSubset(new int[]{0}));
		assertEquals(parentDS.getSubset(new int[]{2,1}), view.getSubset(new int[]{1}));
		assertEquals(parentDS.getSubset(new int[]{2,2}), view.getSubset(new int[]{2}));
		assertEquals(parentDS.getSubset(new int[]{0,2,0}), view.getSubset(new int[]{0,0}));
		assertEquals(parentDS.getSubset(new int[]{1,2,1}), view.getSubset(new int[]{1,1}));
		assertEquals(parentDS.getSubset(new int[]{2,2,2}), view.getSubset(new int[]{2,2}));

		createData("64-bit float", new int[]{3,3,3,3,3}, new int[]{-1,-1,2,-1,-1});
		assertNotNull(view);

		try {
			view.getSubset(new int[]{0});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		assertEquals(parentDS.getSubset(new int[]{2,0,0}), view.getSubset(new int[]{0,0}));
		assertEquals(parentDS.getSubset(new int[]{2,1,1}), view.getSubset(new int[]{1,1}));
		assertEquals(parentDS.getSubset(new int[]{2,2,2}), view.getSubset(new int[]{2,2}));
	}

	@Test
	public void testGetDouble() {
		// one that is correctly defined : 3d data, 1d view
		createData("64-bit float", new int[]{5,5,5}, new int[]{-1,2,2});
		assertNotNull(view);
		parentDS.setDouble(new int[]{0,2,2}, 0.22);
		parentDS.setDouble(new int[]{1,2,2}, 1.22);
		parentDS.setDouble(new int[]{2,2,2}, 2.22);
		parentDS.setDouble(new int[]{3,2,2}, 3.22);
		parentDS.setDouble(new int[]{4,2,2}, 4.22);
		assertEquals(0.22, view.getDouble(new int[]{0}), 0);
		assertEquals(1.22, view.getDouble(new int[]{1}), 0);
		assertEquals(2.22, view.getDouble(new int[]{2}), 0);
		assertEquals(3.22, view.getDouble(new int[]{3}), 0);
		assertEquals(4.22, view.getDouble(new int[]{4}), 0);

		// one that is correctly defined : 3d data, 2d view
		createData("64-bit float", new int[]{5,5,5}, new int[]{-1,2,-1});
		assertNotNull(view);
		parentDS.setDouble(new int[]{0,2,0}, 0.20);
		parentDS.setDouble(new int[]{0,2,1}, 0.21);
		parentDS.setDouble(new int[]{1,2,2}, 1.22);
		parentDS.setDouble(new int[]{1,2,3}, 1.23);
		parentDS.setDouble(new int[]{3,2,4}, 3.24);
		parentDS.setDouble(new int[]{3,2,0}, 3.20);
		assertEquals(0.20, view.getDouble(new int[]{0,0}), 0);
		assertEquals(0.21, view.getDouble(new int[]{0,1}), 0);
		assertEquals(1.22, view.getDouble(new int[]{1,2}), 0);
		assertEquals(1.23, view.getDouble(new int[]{1,3}), 0);
		assertEquals(3.24, view.getDouble(new int[]{3,4}), 0);
		assertEquals(3.20, view.getDouble(new int[]{3,0}), 0);
	}

	@Test
	public void testSetDouble() {
		// one that is correctly defined : 3d data, 1d view
		createData("64-bit float", new int[]{5,5,5}, new int[]{-1,2,2});
		assertNotNull(view);
		view.setDouble(new int[]{0}, 0.22);
		view.setDouble(new int[]{1}, 1.22);
		view.setDouble(new int[]{2}, 2.22);
		view.setDouble(new int[]{3}, 3.22);
		view.setDouble(new int[]{4}, 4.22);
		assertEquals(0.22, parentDS.getDouble(new int[]{0,2,2}), 0);
		assertEquals(1.22, parentDS.getDouble(new int[]{1,2,2}), 0);
		assertEquals(2.22, parentDS.getDouble(new int[]{2,2,2}), 0);
		assertEquals(3.22, parentDS.getDouble(new int[]{3,2,2}), 0);
		assertEquals(4.22, parentDS.getDouble(new int[]{4,2,2}), 0);

		// one that is correctly defined : 3d data, 2d view
		createData("64-bit float", new int[]{5,5,5}, new int[]{-1,2,-1});
		assertNotNull(view);
		view.setDouble(new int[]{0,0}, 0.20);
		view.setDouble(new int[]{0,1}, 0.21);
		view.setDouble(new int[]{1,2}, 1.22);
		view.setDouble(new int[]{1,3}, 1.23);
		view.setDouble(new int[]{3,4}, 3.24);
		view.setDouble(new int[]{3,0}, 3.20);
		assertEquals(0.20, parentDS.getDouble(new int[]{0,2,0}), 0);
		assertEquals(0.21, parentDS.getDouble(new int[]{0,2,1}), 0);
		assertEquals(1.22, parentDS.getDouble(new int[]{1,2,2}), 0);
		assertEquals(1.23, parentDS.getDouble(new int[]{1,2,3}), 0);
		assertEquals(3.24, parentDS.getDouble(new int[]{3,2,4}), 0);
		assertEquals(3.20, parentDS.getDouble(new int[]{3,2,0}), 0);
	}

	@Test
	public void testGetLong() {
		// one that is correctly defined : 3d data, 1d view
		createData("64-bit signed", new int[]{5,5,5}, new int[]{-1,2,2});
		assertNotNull(view);
		parentDS.setLong(new int[]{0,2,2}, 022);
		parentDS.setLong(new int[]{1,2,2}, 122);
		parentDS.setLong(new int[]{2,2,2}, 222);
		parentDS.setLong(new int[]{3,2,2}, 322);
		parentDS.setLong(new int[]{4,2,2}, 422);
		assertEquals(022, view.getLong(new int[]{0}));
		assertEquals(122, view.getLong(new int[]{1}));
		assertEquals(222, view.getLong(new int[]{2}));
		assertEquals(322, view.getLong(new int[]{3}));
		assertEquals(422, view.getLong(new int[]{4}));

		// one that is correctly defined : 3d data, 2d view
		createData("64-bit signed", new int[]{5,5,5}, new int[]{-1,2,-1});
		assertNotNull(view);
		parentDS.setLong(new int[]{0,2,0}, 020);
		parentDS.setLong(new int[]{0,2,1}, 021);
		parentDS.setLong(new int[]{1,2,2}, 122);
		parentDS.setLong(new int[]{1,2,3}, 123);
		parentDS.setLong(new int[]{3,2,4}, 324);
		parentDS.setLong(new int[]{3,2,0}, 320);
		assertEquals(020, view.getLong(new int[]{0,0}));
		assertEquals(021, view.getLong(new int[]{0,1}));
		assertEquals(122, view.getLong(new int[]{1,2}));
		assertEquals(123, view.getLong(new int[]{1,3}));
		assertEquals(324, view.getLong(new int[]{3,4}));
		assertEquals(320, view.getLong(new int[]{3,0}));
	}

	@Test
	public void testSetLong() {
		// one that is correctly defined : 3d data, 1d view
		createData("64-bit signed", new int[]{5,5,5}, new int[]{-1,2,2});
		assertNotNull(view);
		view.setLong(new int[]{0}, 022);
		view.setLong(new int[]{1}, 122);
		view.setLong(new int[]{2}, 222);
		view.setLong(new int[]{3}, 322);
		view.setLong(new int[]{4}, 422);
		assertEquals(022, parentDS.getLong(new int[]{0,2,2}));
		assertEquals(122, parentDS.getLong(new int[]{1,2,2}));
		assertEquals(222, parentDS.getLong(new int[]{2,2,2}));
		assertEquals(322, parentDS.getLong(new int[]{3,2,2}));
		assertEquals(422, parentDS.getLong(new int[]{4,2,2}));

		// one that is correctly defined : 3d data, 2d view
		createData("64-bit signed", new int[]{5,5,5}, new int[]{-1,2,-1});
		assertNotNull(view);
		view.setLong(new int[]{0,0}, 020);
		view.setLong(new int[]{0,1}, 021);
		view.setLong(new int[]{1,2}, 122);
		view.setLong(new int[]{1,3}, 123);
		view.setLong(new int[]{3,4}, 324);
		view.setLong(new int[]{3,0}, 320);
		assertEquals(020, parentDS.getLong(new int[]{0,2,0}));
		assertEquals(021, parentDS.getLong(new int[]{0,2,1}));
		assertEquals(122, parentDS.getLong(new int[]{1,2,2}));
		assertEquals(123, parentDS.getLong(new int[]{1,2,3}));
		assertEquals(324, parentDS.getLong(new int[]{3,2,4}));
		assertEquals(320, parentDS.getLong(new int[]{3,2,0}));
	}

}
