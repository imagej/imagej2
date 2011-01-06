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
		
		while (Index.isValid(position, origin, span))
		{
			if (isFloat)
			{
				double value;
				
				if (rng.nextBoolean())
					value = rng.nextDouble() * 75000;
				else
					value = rng.nextDouble() * -75000;
					
				ds.setDouble(position, value);
			}
			else // integer
			{
				long value;
				
				if (isUnsigned)
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

	@Test
	public void testCreateTypeConvertedDataset() {
		//fail("Not yet implemented");
	}

}
