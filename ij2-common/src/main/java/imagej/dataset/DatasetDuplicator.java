package imagej.dataset;

import imagej.data.Type;
import imagej.process.Index;

public class DatasetDuplicator
{
	/** public constructor that allows subclass/override mechanism if needed */
	public DatasetDuplicator()
	{
	}
	
	/** creates a Dataset according to given factory's style but whose shape and data values are copied from a given Dataset */ 
	public Dataset duplicateDataset(DatasetFactory factory, Dataset dataset)
	{
		Type type = dataset.getType();
		
		int[] dimensions = dataset.getDimensions();
		
		Dataset newDataset = factory.createDataset(type, dimensions);
		
		// choose the best way to copy to assure no precision loss
		if (type.isFloat())
			copyRealData(dataset, newDataset);
		else
			copyIntegralData(dataset, newDataset);
		
		newDataset.setMetaData(dataset.getMetaData());  // TODO - PROBABLY NEED TO CLONE THE METADATA HERE!!!!!!!!!!!
		
		return newDataset;
	}
	
	// TODO - copying in the easiest and slowest way possible - do some speed up by indexing on planes to minimize Dataset subset lookup times
	/** copy data as doubles */
	private void copyRealData(Dataset fromDataset, Dataset toDataset)
	{
		int[] dimensions = fromDataset.getDimensions();
		
		int[] position = Index.create(dimensions.length);
		int[] origin = Index.create(dimensions.length);
		int[] span = dimensions;

		while (Index.isValid(position, origin, span))
		{
			double value = fromDataset.getDouble(position);
			
			toDataset.setDouble(position, value);
			
			Index.increment(position, origin, span);
		}
	}

	// TODO - copying in the easiest and slowest way possible - do some speed up by indexing on planes to minimize Dataset subset lookup times
	/** copy data as longs */
	private void copyIntegralData(Dataset fromDataset, Dataset toDataset)
	{
		int[] dimensions = fromDataset.getDimensions();
		
		int[] position = Index.create(dimensions.length);
		int[] origin = Index.create(dimensions.length);
		int[] span = dimensions;

		while (Index.isValid(position, origin, span))
		{
			long value = fromDataset.getLong(position);
			
			toDataset.setLong(position, value);
			
			Index.increment(position, origin, span);
		}
	}
}
