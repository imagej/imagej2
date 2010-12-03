package imagej.dataset;

import imagej.DataType;
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
		DataType type = dataset.getType();
		
		int[] dimensions = dataset.getDimensions();
		
		Dataset newDataset = factory.createDataset(type, dimensions);
		
		int[] position = Index.create(dimensions.length);
		int[] origin = Index.create(dimensions.length);
		int[] span = dimensions;

		// TODO - there are should be faster ways of doing this. do this most general way for now
		
		while (Index.isValid(position, origin, span))
		{
			double value = dataset.getDouble(position);
			
			newDataset.setDouble(position, value);
			
			Index.increment(position, origin, span);
		}
		
		newDataset.setMetaData(dataset.getMetaData());  // TODO - PROBABLY NEED TO CLONE THE METADATA HERE!!!!!!!!!!!
		
		return newDataset;
	}
}
