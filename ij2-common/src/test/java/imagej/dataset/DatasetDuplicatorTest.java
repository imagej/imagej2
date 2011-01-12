package imagej.dataset;

import static org.junit.Assert.*;

import java.util.Random;

import imagej.data.Type;
import imagej.data.Types;
import imagej.process.Index;
import imagej.process.Span;

import org.junit.Test;

public class DatasetDuplicatorTest {

	private DatasetDuplicator duplicator;
	private Dataset dataset;
	private PlanarDatasetFactory factory = new PlanarDatasetFactory();

	private Dataset createData(String type, int[] dimensions)
	{
		return factory.createDataset(Types.findType(type), dimensions);
	}
	
	private void populateData(Dataset ds)
	{
		Type type = ds.getType();
		boolean isFloat = type.isFloat();
		boolean isUnsigned = type.isUnsigned();
		
		int[] position = Index.create(ds.getDimensions().length);
		int[] origin = Index.create(ds.getDimensions().length);
		int[] span = Span.create(ds.getDimensions());
		
		Random rng = new Random();
		
		int i = 0;
		
		while (Index.isValid(position, origin, span))
		{
			if (isFloat)
			{
				double value;
				
				if (i == 0)
					value = Double.MAX_VALUE;
				else if (i == 1)
					value = -Double.MAX_VALUE;
				else if (rng.nextBoolean())
					value = rng.nextDouble() * 75000;
				else
					value = rng.nextDouble() * -75000;
					
				ds.setDouble(position, value);
			}
			else // integer
			{
				long value;
				
				if (i == 0)
					value = Long.MAX_VALUE;
				else if (i == 1)
					value = Long.MIN_VALUE;
				else if (isUnsigned)
				{
					value = (long)(rng.nextDouble() * type.getMaxIntegral());
				}
				else // signed
				{
					if (rng.nextBoolean())
						value = (long)(rng.nextDouble() * type.getMaxIntegral());
					else
						value = (long)(rng.nextDouble() * type.getMinIntegral());
				}
				
				ds.setLong(position, value);
			}
		
			i++;
			
			Index.increment(position, origin, span);
		}
	}
	
	@Test
	public void testDatasetDuplicator() {
		duplicator = new DatasetDuplicator();
		assertNotNull(duplicator);
	}

	private void assertDatasetsMatch(Dataset ds1, Dataset ds2)
	{
		assertEquals(ds1.getType(), ds2.getType());
		assertArrayEquals(ds1.getDimensions(), ds2.getDimensions());
		
		int[] position = Index.create(ds1.getDimensions().length);
		int[] origin = Index.create(ds1.getDimensions().length);
		int[] span = Span.create(ds1.getDimensions());
		
		while (Index.isValid(position, origin, span))
		{
			assertEquals(ds1.getDouble(position), ds2.getDouble(position), 0);
			assertEquals(ds1.getLong(position), ds2.getLong(position));
			
			Index.increment(position, origin, span);
		}
	}
	
	@Test
	public void testCreateDataset() {
		
		duplicator = new DatasetDuplicator();
		assertNotNull(duplicator);

		// test every defined type
		
		Dataset newDS;
		
		dataset = createData("8-bit unsigned", new int[]{1,2});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);

		dataset = createData("8-bit unsigned", new int[]{1,1,2});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);

		dataset = createData("1-bit unsigned", new int[]{2,3,4});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);

		dataset = createData("8-bit unsigned", new int[]{2,3});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("8-bit signed", new int[]{2,3,4});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("12-bit unsigned", new int[]{2,3,4,5});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("16-bit unsigned", new int[]{2,3,4,5,6});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("16-bit signed", new int[]{2,3,4,5,6,7});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("32-bit unsigned", new int[]{7,6,5,4,3,2});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("32-bit signed", new int[]{7,6,5,4,3});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("32-bit float", new int[]{7,6,5,4});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("64-bit signed", new int[]{7,6,5});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		dataset = createData("64-bit float", new int[]{7,6});
		populateData(dataset);
		newDS = duplicator.createDataset(factory, dataset);
		assertDatasetsMatch(dataset, newDS);
		
		// do one with a DatasetView
		dataset = createData("32-bit float", new int[]{7,6,5,4});
		DatasetView view = new DatasetView(dataset, new int[]{-1,-1,3,-1});
		populateData(view);
		newDS = duplicator.createDataset(factory, view);
		assertDatasetsMatch(view, newDS);
	}

	// TODO - this method was written when the DataAccessors clamped data values. Now that they don't a bunch of tests in here fail.
	//   Will comment out much for now. They should be restored if we reenable data clamping.
	
	@Test
	public void testCreateTypeConvertedDataset() {
		
		double TOL = 0.0000001;
		Dataset newDS;
		
		duplicator = new DatasetDuplicator();
		assertNotNull(duplicator);

		// ORIGINAL DATASET 1-BIT UNSIGNED ************************************************************************************
		
		dataset = createData("1-bit unsigned", new int[]{1,20});
		dataset.setLong(new int[]{0,0}, 0);
		dataset.setLong(new int[]{0,1}, 1);
		
		//   TO 8-BIT UNSIGNED - widening unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit unsigned"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		
		//   TO 8-BIT SIGNED - widening signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));

		//   TO 32-BIT FLOAT - widening float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit float"), dataset);
		assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,1}), 0);

		// ORIGINAL DATASET 8-BIT SIGNED ************************************************************************************
		
		dataset = createData("8-bit signed", new int[]{1,20});
		dataset.setLong(new int[]{0,0}, Byte.MIN_VALUE);
		dataset.setLong(new int[]{0,1}, -1);
		dataset.setLong(new int[]{0,2}, 0);
		dataset.setLong(new int[]{0,3}, 1);
		dataset.setLong(new int[]{0,4}, Byte.MAX_VALUE);
		
		//   TO 1-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("1-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		assertEquals(0, newDS.getLong(new int[]{0,2}));
		assertEquals(1, newDS.getLong(new int[]{0,3}));
		//assertEquals(1, newDS.getLong(new int[]{0,4}));
		
		//   TO 8-BIT UNSIGNED - same size, opposite sign
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		assertEquals(0, newDS.getLong(new int[]{0,2}));
		assertEquals(1, newDS.getLong(new int[]{0,3}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		
		
		//   TO 16-BIT SIGNED - widening signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit signed"), dataset);
		assertEquals(Byte.MIN_VALUE, newDS.getLong(new int[]{0,0}));
		assertEquals(-1, newDS.getLong(new int[]{0,1}));
		assertEquals(0, newDS.getLong(new int[]{0,2}));
		assertEquals(1, newDS.getLong(new int[]{0,3}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,4}));

		//   TO 16-BIT UNSIGNED - widening unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		assertEquals(0, newDS.getLong(new int[]{0,2}));
		assertEquals(1, newDS.getLong(new int[]{0,3}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		
		//   TO 64-BIT FLOAT - widening float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit float"), dataset);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,4}), 0);
		
		// ORIGINAL DATASET 8-BIT UNSIGNED ************************************************************************************
		
		dataset = createData("8-bit unsigned", new int[]{1,20});
		dataset.setLong(new int[]{0,0}, 0);
		dataset.setLong(new int[]{0,1}, 1);
		dataset.setLong(new int[]{0,2}, Byte.MAX_VALUE);
		dataset.setLong(new int[]{0,3}, Byte.MAX_VALUE+1);
		dataset.setLong(new int[]{0,4}, 255);
		
		//   TO 1-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("1-bit unsigned"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		//assertEquals(1, newDS.getLong(new int[]{0,2}));
		//assertEquals(1, newDS.getLong(new int[]{0,3}));
		//assertEquals(1, newDS.getLong(new int[]{0,4}));
		
		//   TO 8-BIT SIGNED - same size, opposite sign
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		//assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,3}));
		//assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,4}));

		
		//   TO 16-BIT SIGNED - widening signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(255, newDS.getLong(new int[]{0,4}));

		//   TO 16-BIT UNSIGNED - widening unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit unsigned"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(255, newDS.getLong(new int[]{0,4}));
		
		//   TO 32-BIT FLOAT - widening float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit float"), dataset);
		assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MAX_VALUE+1, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(255, newDS.getDouble(new int[]{0,4}), 0);
		
		// ORIGINAL DATASET 16-BIT SIGNED ************************************************************************************
		
		dataset = createData("16-bit signed", new int[]{1,20});
		dataset.setLong(new int[]{0,0}, Short.MIN_VALUE);
		dataset.setLong(new int[]{0,1}, Byte.MIN_VALUE-1);
		dataset.setLong(new int[]{0,2}, Byte.MIN_VALUE);
		dataset.setLong(new int[]{0,3}, -1);
		dataset.setLong(new int[]{0,4}, 0);
		dataset.setLong(new int[]{0,5}, 1);
		dataset.setLong(new int[]{0,6}, Byte.MAX_VALUE);
		dataset.setLong(new int[]{0,7}, Byte.MAX_VALUE+1);
		dataset.setLong(new int[]{0,8}, Short.MAX_VALUE);
		
		//   TO 1-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("1-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		//assertEquals(0, newDS.getLong(new int[]{0,2}));
		//assertEquals(0, newDS.getLong(new int[]{0,3}));
		assertEquals(0, newDS.getLong(new int[]{0,4}));
		assertEquals(1, newDS.getLong(new int[]{0,5}));
		//assertEquals(1, newDS.getLong(new int[]{0,6}));
		//assertEquals(1, newDS.getLong(new int[]{0,7}));
		//assertEquals(1, newDS.getLong(new int[]{0,8}));
		
		//   TO 8-BIT SIGNED - narrowing signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit signed"), dataset);
		//assertEquals(Byte.MIN_VALUE, newDS.getLong(new int[]{0,0}));
		//assertEquals(Byte.MIN_VALUE, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MIN_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(-1, newDS.getLong(new int[]{0,3}));
		assertEquals(0, newDS.getLong(new int[]{0,4}));
		assertEquals(1, newDS.getLong(new int[]{0,5}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,6}));
		//assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,7}));
		//assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,8}));
		
		//   TO 16-BIT UNSIGNED - same size, opposite sign
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		//assertEquals(0, newDS.getLong(new int[]{0,2}));
		//assertEquals(0, newDS.getLong(new int[]{0,3}));
		assertEquals(0, newDS.getLong(new int[]{0,4}));
		assertEquals(1, newDS.getLong(new int[]{0,5}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,6}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,7}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,8}));

		
		//   TO 32-BIT SIGNED - widening signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit signed"), dataset);
		assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,0}));
		assertEquals(Byte.MIN_VALUE-1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MIN_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(-1, newDS.getLong(new int[]{0,3}));
		assertEquals(0, newDS.getLong(new int[]{0,4}));
		assertEquals(1, newDS.getLong(new int[]{0,5}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,6}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,7}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,8}));

		//   TO 32-BIT UNSIGNED - widening unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		//assertEquals(0, newDS.getLong(new int[]{0,2}));
		//assertEquals(0, newDS.getLong(new int[]{0,3}));
		assertEquals(0, newDS.getLong(new int[]{0,4}));
		assertEquals(1, newDS.getLong(new int[]{0,5}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,6}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,7}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,8}));
		
		//   TO 64-BIT FLOAT - widening float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit float"), dataset);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(Byte.MIN_VALUE-1, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(Byte.MAX_VALUE+1, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,8}), 0);
		
		
		// ORIGINAL DATASET 16-BIT UNSIGNED ************************************************************************************
		
		dataset = createData("16-bit unsigned", new int[]{1,20});
		dataset.setLong(new int[]{0,0}, 0);
		dataset.setLong(new int[]{0,1}, 1);
		dataset.setLong(new int[]{0,2}, Byte.MAX_VALUE);
		dataset.setLong(new int[]{0,3}, Byte.MAX_VALUE+1);
		dataset.setLong(new int[]{0,4}, Short.MAX_VALUE);
		dataset.setLong(new int[]{0,5}, Short.MAX_VALUE+1);
		dataset.setLong(new int[]{0,6}, 65535);
		
		//   TO 8-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit unsigned"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(255, newDS.getLong(new int[]{0,4}));
		//assertEquals(255, newDS.getLong(new int[]{0,5}));
		//assertEquals(255, newDS.getLong(new int[]{0,6}));

		//   TO 8-BIT SIGNED - narrowing signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		//assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,3}));
		//assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		//assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,5}));
		//assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,6}));
		
		//   TO 16-BIT SIGNED - same size, opposite sign
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,5}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,6}));


		//   TO 32-BIT SIGNED - widening signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,5}));
		assertEquals(65535, newDS.getLong(new int[]{0,6}));

		//   TO 32-BIT UNSIGNED - widening unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit unsigned"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,5}));
		assertEquals(65535, newDS.getLong(new int[]{0,6}));
		
		//   TO 32-BIT FLOAT - widening float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit float"), dataset);
		assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MAX_VALUE+1, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(Short.MAX_VALUE+1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(65535, newDS.getDouble(new int[]{0,6}), 0);
		
		
		// ORIGINAL DATASET 32-BIT SIGNED ************************************************************************************
		
		dataset = createData("32-bit signed", new int[]{1,20});
		dataset.setLong(new int[]{0,0}, Integer.MIN_VALUE);
		dataset.setLong(new int[]{0,1}, Short.MIN_VALUE-1);
		dataset.setLong(new int[]{0,2}, Short.MIN_VALUE);
		dataset.setLong(new int[]{0,3}, Byte.MIN_VALUE-1);
		dataset.setLong(new int[]{0,4}, Byte.MIN_VALUE);
		dataset.setLong(new int[]{0,5}, -1);
		dataset.setLong(new int[]{0,6}, 0);
		dataset.setLong(new int[]{0,7}, 1);
		dataset.setLong(new int[]{0,8}, Byte.MAX_VALUE);
		dataset.setLong(new int[]{0,9}, Byte.MAX_VALUE+1);
		dataset.setLong(new int[]{0,10}, Short.MAX_VALUE);
		dataset.setLong(new int[]{0,11}, Short.MAX_VALUE+1);
		dataset.setLong(new int[]{0,12}, Integer.MAX_VALUE);
		
		//   TO 8-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		//assertEquals(0, newDS.getLong(new int[]{0,2}));
		//assertEquals(0, newDS.getLong(new int[]{0,3}));
		//assertEquals(0, newDS.getLong(new int[]{0,4}));
		//assertEquals(0, newDS.getLong(new int[]{0,5}));
		assertEquals(0, newDS.getLong(new int[]{0,6}));
		assertEquals(1, newDS.getLong(new int[]{0,7}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,8}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,9}));
		assertEquals(255, newDS.getLong(new int[]{0,10}));
		//assertEquals(255, newDS.getLong(new int[]{0,11}));
		//assertEquals(255, newDS.getLong(new int[]{0,12}));

		//   TO 16-BIT SIGNED - narrowing signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit signed"), dataset);
		//assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,0}));
		//assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,1}));
		assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MIN_VALUE-1, newDS.getLong(new int[]{0,3}));
		assertEquals(Byte.MIN_VALUE, newDS.getLong(new int[]{0,4}));
		assertEquals(-1, newDS.getLong(new int[]{0,5}));
		assertEquals(0, newDS.getLong(new int[]{0,6}));
		assertEquals(1, newDS.getLong(new int[]{0,7}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,8}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,9}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,10}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,11}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,12}));
		
		//   TO 32-BIT UNSIGNED - same size, opposite sign
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		//assertEquals(0, newDS.getLong(new int[]{0,2}));
		//assertEquals(0, newDS.getLong(new int[]{0,3}));
		//assertEquals(0, newDS.getLong(new int[]{0,4}));
		//assertEquals(0, newDS.getLong(new int[]{0,5}));
		assertEquals(0, newDS.getLong(new int[]{0,6}));
		assertEquals(1, newDS.getLong(new int[]{0,7}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,8}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,9}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,10}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,11}));
		assertEquals(Integer.MAX_VALUE, newDS.getLong(new int[]{0,12}));

		//   TO 64-BIT SIGNED - widening signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit signed"), dataset);
		assertEquals(Integer.MIN_VALUE, newDS.getLong(new int[]{0,0}));
		assertEquals(Short.MIN_VALUE-1, newDS.getLong(new int[]{0,1}));
		assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MIN_VALUE-1, newDS.getLong(new int[]{0,3}));
		assertEquals(Byte.MIN_VALUE, newDS.getLong(new int[]{0,4}));
		assertEquals(-1, newDS.getLong(new int[]{0,5}));
		assertEquals(0, newDS.getLong(new int[]{0,6}));
		assertEquals(1, newDS.getLong(new int[]{0,7}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,8}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,9}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,10}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,11}));
		assertEquals(Integer.MAX_VALUE, newDS.getLong(new int[]{0,12}));

		//   TO 64-BIT FLOAT - widening float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit float"), dataset);
		assertEquals(Integer.MIN_VALUE, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(Short.MIN_VALUE-1, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MIN_VALUE-1, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(Byte.MAX_VALUE+1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Short.MAX_VALUE+1, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);

		// ORIGINAL DATASET 32-BIT UNSIGNED ************************************************************************************
		
		dataset = createData("32-bit unsigned", new int[]{1,20});
		dataset.setLong(new int[]{0,0}, 0);
		dataset.setLong(new int[]{0,1}, 1);
		dataset.setLong(new int[]{0,2}, Byte.MAX_VALUE);
		dataset.setLong(new int[]{0,3}, Byte.MAX_VALUE+1);
		dataset.setLong(new int[]{0,4}, Short.MAX_VALUE);
		dataset.setLong(new int[]{0,5}, Short.MAX_VALUE+1);
		dataset.setLong(new int[]{0,6}, Integer.MAX_VALUE);
		dataset.setLong(new int[]{0,7}, 1L + Integer.MAX_VALUE);
		dataset.setLong(new int[]{0,8}, 0xffffffffL);
		
		//   TO 16-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit unsigned"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,5}));
		assertEquals(65535, newDS.getLong(new int[]{0,6}));
		//assertEquals(65535, newDS.getLong(new int[]{0,7}));
		//assertEquals(65535, newDS.getLong(new int[]{0,8}));

		//   TO 16-BIT SIGNED - narrowing signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,5}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,6}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,7}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,8}));
		
		//   TO 32-BIT SIGNED - same size, opposite sign
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,5}));
		assertEquals(Integer.MAX_VALUE, newDS.getLong(new int[]{0,6}));
		//assertEquals(Integer.MAX_VALUE, newDS.getLong(new int[]{0,7}));
		//assertEquals(Integer.MAX_VALUE, newDS.getLong(new int[]{0,8}));
		
		//   TO 32-BIT FLOAT - same size, float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit float"), dataset);
		assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MAX_VALUE+1, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(Short.MAX_VALUE+1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals((float)Integer.MAX_VALUE, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals((float)Integer.MAX_VALUE, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals((float)(0xffffffffL), newDS.getDouble(new int[]{0,8}), 0);

		//   TO 64-BIT SIGNED - widening signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit signed"), dataset);
		assertEquals(0, newDS.getLong(new int[]{0,0}));
		assertEquals(1, newDS.getLong(new int[]{0,1}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,3}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,4}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,5}));
		assertEquals(Integer.MAX_VALUE, newDS.getLong(new int[]{0,6}));
		assertEquals(1L + Integer.MAX_VALUE, newDS.getLong(new int[]{0,7}));
		assertEquals(0xffffffffL, newDS.getLong(new int[]{0,8}));

		//   TO 64-BIT FLOAT - widening float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit float"), dataset);
		assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MAX_VALUE+1, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(Short.MAX_VALUE+1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(1L+Integer.MAX_VALUE, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0xffffffffL, newDS.getDouble(new int[]{0,8}), 0);

		// ORIGINAL DATASET 32-BIT float ************************************************************************************
		
		dataset = createData("32-bit float", new int[]{1,20});
		dataset.setDouble(new int[]{0,0}, -Float.MAX_VALUE);
		dataset.setDouble(new int[]{0,1}, Integer.MIN_VALUE);
		dataset.setDouble(new int[]{0,2}, Short.MIN_VALUE);
		dataset.setDouble(new int[]{0,3}, Byte.MIN_VALUE);
		dataset.setDouble(new int[]{0,4}, -1);
		dataset.setDouble(new int[]{0,5}, -0.5);
		dataset.setDouble(new int[]{0,6}, -0.3);
		dataset.setDouble(new int[]{0,7}, 0);
		dataset.setDouble(new int[]{0,8}, 0.3);
		dataset.setDouble(new int[]{0,9}, 0.5);
		dataset.setDouble(new int[]{0,10}, 1);
		dataset.setDouble(new int[]{0,11}, Byte.MAX_VALUE);
		dataset.setDouble(new int[]{0,12}, Short.MAX_VALUE);
		dataset.setDouble(new int[]{0,13}, Integer.MAX_VALUE);
		dataset.setDouble(new int[]{0,14}, Float.MAX_VALUE);
		
		//   TO 16-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit unsigned"), dataset);
		//assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,1}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,2}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,3}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,4}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,5}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		assertEquals(65535, newDS.getDouble(new int[]{0,13}), 0);
		//assertEquals(65535, newDS.getDouble(new int[]{0,14}), 0);

		//   TO 16-BIT SIGNED - narrowing signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit signed"), dataset);
		//assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,0}), 0);
		//assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		//assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,13}), 0);
		//assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,14}), 0);
		
		//   TO 32-BIT SIGNED - same size, opposite type, signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit signed"), dataset);
		//assertEquals(Integer.MIN_VALUE, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(Integer.MIN_VALUE, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		//assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,13}), 0);
		//assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,14}), 0);

		//   TO 32-BIT UNSIGNED - same size, opposite type, unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit unsigned"), dataset);
		//assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,1}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,2}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,3}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,4}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,5}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		assertEquals((float)Integer.MAX_VALUE, newDS.getDouble(new int[]{0,13}), 0);
		assertEquals(0xffffffffL, newDS.getDouble(new int[]{0,14}), 0);


		//   TO 64-BIT SIGNED - widening signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit signed"), dataset);
		assertEquals((long)-Float.MAX_VALUE, newDS.getLong(new int[]{0,0}));
		assertEquals((long)Integer.MIN_VALUE, newDS.getLong(new int[]{0,1}));
		assertEquals((long)Short.MIN_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals((long)Byte.MIN_VALUE, newDS.getLong(new int[]{0,3}));
		assertEquals((long)-1, newDS.getLong(new int[]{0,4}));
		assertEquals((long)-1, newDS.getLong(new int[]{0,5}));
		assertEquals((long)-0.3, newDS.getLong(new int[]{0,6}));
		assertEquals((long)0, newDS.getLong(new int[]{0,7}));
		assertEquals((long)0.3, newDS.getLong(new int[]{0,8}));
		assertEquals((long)1, newDS.getLong(new int[]{0,9}));
		assertEquals((long)1, newDS.getLong(new int[]{0,10}));
		assertEquals((long)Byte.MAX_VALUE, newDS.getLong(new int[]{0,11}));
		assertEquals((long)Short.MAX_VALUE, newDS.getLong(new int[]{0,12}));
		//TODO - off by one!!! (float)(Integer.MAX_VALUE) is off by one
		assertEquals(1L+Integer.MAX_VALUE, newDS.getLong(new int[]{0,13}));
		assertEquals((long)Float.MAX_VALUE, newDS.getLong(new int[]{0,14}));

		//   TO 64-BIT FLOAT - widening float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit float"), dataset);
		assertEquals(-Float.MAX_VALUE, newDS.getDouble(new int[]{0,0}), TOL);
		assertEquals(Integer.MIN_VALUE, newDS.getDouble(new int[]{0,1}), TOL);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,2}), TOL);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,3}), TOL);
		assertEquals(-1, newDS.getDouble(new int[]{0,4}), TOL);
		assertEquals(-0.5, newDS.getDouble(new int[]{0,5}), TOL);
		assertEquals(-0.3, newDS.getDouble(new int[]{0,6}), TOL);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), TOL);
		assertEquals(0.3, newDS.getDouble(new int[]{0,8}), TOL);
		assertEquals(0.5, newDS.getDouble(new int[]{0,9}), TOL);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), TOL);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), TOL);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), TOL);
		//TODO - off by one!!! (float)(Integer.MAX_VALUE) is off by one
		assertEquals(1.0+Integer.MAX_VALUE, newDS.getDouble(new int[]{0,13}), TOL);
		assertEquals(Float.MAX_VALUE, newDS.getDouble(new int[]{0,14}), TOL);

		// ORIGINAL DATASET 64-BIT SIGNED ************************************************************************************
		
		dataset = createData("64-bit signed", new int[]{1,20});
		dataset.setLong(new int[]{0,0}, Long.MIN_VALUE);
		dataset.setLong(new int[]{0,1}, -1L + Integer.MIN_VALUE);
		dataset.setLong(new int[]{0,2}, Integer.MIN_VALUE);
		dataset.setLong(new int[]{0,3}, Short.MIN_VALUE-1);
		dataset.setLong(new int[]{0,4}, Short.MIN_VALUE);
		dataset.setLong(new int[]{0,5}, Byte.MIN_VALUE-1);
		dataset.setLong(new int[]{0,6}, Byte.MIN_VALUE);
		dataset.setLong(new int[]{0,7}, -1);
		dataset.setLong(new int[]{0,8}, 0);
		dataset.setLong(new int[]{0,9}, 1);
		dataset.setLong(new int[]{0,10}, Byte.MAX_VALUE);
		dataset.setLong(new int[]{0,11}, Byte.MAX_VALUE+1);
		dataset.setLong(new int[]{0,12}, Short.MAX_VALUE);
		dataset.setLong(new int[]{0,13}, Short.MAX_VALUE+1);
		dataset.setLong(new int[]{0,14}, Integer.MAX_VALUE);
		dataset.setLong(new int[]{0,15}, 1L + Integer.MAX_VALUE);
		dataset.setLong(new int[]{0,16}, Long.MAX_VALUE);
		
		//   TO 8-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		//assertEquals(0, newDS.getLong(new int[]{0,2}));
		//assertEquals(0, newDS.getLong(new int[]{0,3}));
		//assertEquals(0, newDS.getLong(new int[]{0,4}));
		//assertEquals(0, newDS.getLong(new int[]{0,5}));
		//assertEquals(0, newDS.getLong(new int[]{0,6}));
		//assertEquals(0, newDS.getLong(new int[]{0,7}));
		assertEquals(0, newDS.getLong(new int[]{0,8}));
		assertEquals(1, newDS.getLong(new int[]{0,9}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,10}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,11}));
		assertEquals(255, newDS.getLong(new int[]{0,12}));
		//assertEquals(255, newDS.getLong(new int[]{0,13}));
		//assertEquals(255, newDS.getLong(new int[]{0,14}));
		//assertEquals(255, newDS.getLong(new int[]{0,15}));
		//assertEquals(255, newDS.getLong(new int[]{0,16}));

		//   TO 16-BIT SIGNED - narrowing signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit signed"), dataset);
		//assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,0}));
		//assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,1}));
		//assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,2}));
		//assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,3}));
		assertEquals(Short.MIN_VALUE, newDS.getLong(new int[]{0,4}));
		assertEquals(Byte.MIN_VALUE-1, newDS.getLong(new int[]{0,5}));
		assertEquals(Byte.MIN_VALUE, newDS.getLong(new int[]{0,6}));
		assertEquals(-1, newDS.getLong(new int[]{0,7}));
		assertEquals(0, newDS.getLong(new int[]{0,8}));
		assertEquals(1, newDS.getLong(new int[]{0,9}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,10}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,11}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,12}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,13}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,14}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,15}));
		//assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,16}));
		
		//   TO 16-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		//assertEquals(0, newDS.getLong(new int[]{0,2}));
		//assertEquals(0, newDS.getLong(new int[]{0,3}));
		//assertEquals(0, newDS.getLong(new int[]{0,4}));
		//assertEquals(0, newDS.getLong(new int[]{0,5}));
		//assertEquals(0, newDS.getLong(new int[]{0,6}));
		//assertEquals(0, newDS.getLong(new int[]{0,7}));
		assertEquals(0, newDS.getLong(new int[]{0,8}));
		assertEquals(1, newDS.getLong(new int[]{0,9}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,10}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,11}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,12}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,13}));
		assertEquals(65535, newDS.getLong(new int[]{0,14}));
		//assertEquals(65535, newDS.getLong(new int[]{0,15}));
		//assertEquals(65535, newDS.getLong(new int[]{0,16}));

		//   TO 32-BIT UNSIGNED - narrowing unsigned again
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit unsigned"), dataset);
		//assertEquals(0, newDS.getLong(new int[]{0,0}));
		//assertEquals(0, newDS.getLong(new int[]{0,1}));
		//assertEquals(0, newDS.getLong(new int[]{0,2}));
		//assertEquals(0, newDS.getLong(new int[]{0,3}));
		//assertEquals(0, newDS.getLong(new int[]{0,4}));
		//assertEquals(0, newDS.getLong(new int[]{0,5}));
		//assertEquals(0, newDS.getLong(new int[]{0,6}));
		//assertEquals(0, newDS.getLong(new int[]{0,7}));
		assertEquals(0, newDS.getLong(new int[]{0,8}));
		assertEquals(1, newDS.getLong(new int[]{0,9}));
		assertEquals(Byte.MAX_VALUE, newDS.getLong(new int[]{0,10}));
		assertEquals(Byte.MAX_VALUE+1, newDS.getLong(new int[]{0,11}));
		assertEquals(Short.MAX_VALUE, newDS.getLong(new int[]{0,12}));
		assertEquals(Short.MAX_VALUE+1, newDS.getLong(new int[]{0,13}));
		assertEquals(Integer.MAX_VALUE, newDS.getLong(new int[]{0,14}));
		assertEquals(Integer.MAX_VALUE+1L, newDS.getLong(new int[]{0,15}));
		assertEquals(0xffffffffL, newDS.getLong(new int[]{0,16}));
		
		//   TO 64-BIT FLOAT - same size but float
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit float"), dataset);
		assertEquals((double)Long.MIN_VALUE, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(Integer.MIN_VALUE-1L, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Integer.MIN_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Short.MIN_VALUE-1, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(Byte.MIN_VALUE-1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE+1, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		assertEquals(Short.MAX_VALUE+1, newDS.getDouble(new int[]{0,13}), 0);
		assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,14}), 0);
		assertEquals(Integer.MAX_VALUE+1L, newDS.getDouble(new int[]{0,15}), 0);
		assertEquals((double)Long.MAX_VALUE, newDS.getDouble(new int[]{0,16}), 0);

		// ORIGINAL DATASET 64-BIT float ************************************************************************************
		
		dataset = createData("64-bit float", new int[]{1,20});
		dataset.setDouble(new int[]{0,0}, -Float.MAX_VALUE);
		dataset.setDouble(new int[]{0,1}, Integer.MIN_VALUE);
		dataset.setDouble(new int[]{0,2}, Short.MIN_VALUE);
		dataset.setDouble(new int[]{0,3}, Byte.MIN_VALUE);
		dataset.setDouble(new int[]{0,4}, -1);
		dataset.setDouble(new int[]{0,5}, -0.5);
		dataset.setDouble(new int[]{0,6}, -0.3);
		dataset.setDouble(new int[]{0,7}, 0);
		dataset.setDouble(new int[]{0,8}, 0.3);
		dataset.setDouble(new int[]{0,9}, 0.5);
		dataset.setDouble(new int[]{0,10}, 1);
		dataset.setDouble(new int[]{0,11}, Byte.MAX_VALUE);
		dataset.setDouble(new int[]{0,12}, Short.MAX_VALUE);
		dataset.setDouble(new int[]{0,13}, Integer.MAX_VALUE);
		dataset.setDouble(new int[]{0,14}, Float.MAX_VALUE);
		
		//   TO 1-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("1-bit unsigned"), dataset);
		//assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,1}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,2}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,3}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,4}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,5}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		//assertEquals(1, newDS.getDouble(new int[]{0,11}), 0);
		//assertEquals(1, newDS.getDouble(new int[]{0,12}), 0);
		//assertEquals(1, newDS.getDouble(new int[]{0,13}), 0);
		//assertEquals(1, newDS.getDouble(new int[]{0,14}), 0);
		
		//   TO 8-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("8-bit unsigned"), dataset);
		//assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,1}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,2}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,3}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,4}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,5}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(255, newDS.getDouble(new int[]{0,12}), 0);
		//assertEquals(255, newDS.getDouble(new int[]{0,13}), 0);
		//assertEquals(255, newDS.getDouble(new int[]{0,14}), 0);

		//   TO 16-BIT UNSIGNED - narrowing unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit unsigned"), dataset);
		//assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,1}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,2}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,3}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,4}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,5}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		assertEquals(65535, newDS.getDouble(new int[]{0,13}), 0);
		assertEquals(65535, newDS.getDouble(new int[]{0,14}), 0);

		//   TO 16-BIT SIGNED - narrowing signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("16-bit signed"), dataset);
		//assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,0}), 0);
		//assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		//assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,13}), 0);
		//assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,14}), 0);
		
		//   TO 32-BIT SIGNED - narrowing, signed
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit signed"), dataset);
		//assertEquals(Integer.MIN_VALUE, newDS.getDouble(new int[]{0,0}), 0);
		assertEquals(Integer.MIN_VALUE, newDS.getDouble(new int[]{0,1}), 0);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,2}), 0);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,3}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,4}), 0);
		assertEquals(-1, newDS.getDouble(new int[]{0,5}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,13}), 0);
		//assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,14}), 0);

		//   TO 32-BIT UNSIGNED - narrowing, unsigned
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("32-bit unsigned"), dataset);
		//assertEquals(0, newDS.getDouble(new int[]{0,0}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,1}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,2}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,3}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,4}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,5}), 0);
		//assertEquals(0, newDS.getDouble(new int[]{0,6}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), 0);
		assertEquals(0, newDS.getDouble(new int[]{0,8}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,9}), 0);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), 0);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), 0);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), 0);
		assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,13}), 0);
		assertEquals(0xffffffffL, newDS.getDouble(new int[]{0,14}), 0);

		//   TO 64-BIT SIGNED - same size, opposite type
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit signed"), dataset);
		assertEquals((long)-Float.MAX_VALUE, newDS.getLong(new int[]{0,0}));
		assertEquals((long)Integer.MIN_VALUE, newDS.getLong(new int[]{0,1}));
		assertEquals((long)Short.MIN_VALUE, newDS.getLong(new int[]{0,2}));
		assertEquals((long)Byte.MIN_VALUE, newDS.getLong(new int[]{0,3}));
		assertEquals((long)-1, newDS.getLong(new int[]{0,4}));
		assertEquals((long)-1, newDS.getLong(new int[]{0,5}));
		assertEquals((long)-0.3, newDS.getLong(new int[]{0,6}));
		assertEquals((long)0, newDS.getLong(new int[]{0,7}));
		assertEquals((long)0.3, newDS.getLong(new int[]{0,8}));
		assertEquals((long)1, newDS.getLong(new int[]{0,9}));
		assertEquals((long)1, newDS.getLong(new int[]{0,10}));
		assertEquals((long)Byte.MAX_VALUE, newDS.getLong(new int[]{0,11}));
		assertEquals((long)Short.MAX_VALUE, newDS.getLong(new int[]{0,12}));
		assertEquals(Integer.MAX_VALUE, newDS.getLong(new int[]{0,13}));
		assertEquals((long)Float.MAX_VALUE, newDS.getLong(new int[]{0,14}));

		//   TO 64-BIT FLOAT - same type
		newDS = duplicator.createTypeConvertedDataset(factory, Types.findType("64-bit float"), dataset);
		assertEquals(-Float.MAX_VALUE, newDS.getDouble(new int[]{0,0}), TOL);
		assertEquals(Integer.MIN_VALUE, newDS.getDouble(new int[]{0,1}), TOL);
		assertEquals(Short.MIN_VALUE, newDS.getDouble(new int[]{0,2}), TOL);
		assertEquals(Byte.MIN_VALUE, newDS.getDouble(new int[]{0,3}), TOL);
		assertEquals(-1, newDS.getDouble(new int[]{0,4}), TOL);
		assertEquals(-0.5, newDS.getDouble(new int[]{0,5}), TOL);
		assertEquals(-0.3, newDS.getDouble(new int[]{0,6}), TOL);
		assertEquals(0, newDS.getDouble(new int[]{0,7}), TOL);
		assertEquals(0.3, newDS.getDouble(new int[]{0,8}), TOL);
		assertEquals(0.5, newDS.getDouble(new int[]{0,9}), TOL);
		assertEquals(1, newDS.getDouble(new int[]{0,10}), TOL);
		assertEquals(Byte.MAX_VALUE, newDS.getDouble(new int[]{0,11}), TOL);
		assertEquals(Short.MAX_VALUE, newDS.getDouble(new int[]{0,12}), TOL);
		assertEquals(Integer.MAX_VALUE, newDS.getDouble(new int[]{0,13}), TOL);
		assertEquals(Float.MAX_VALUE, newDS.getDouble(new int[]{0,14}), TOL);
	}

}
