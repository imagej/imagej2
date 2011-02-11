package imagej.dataset;

import imagej.operation.RegionCopyOperation;
import imagej.process.Index;
import imagej.types.Type;

public class DatasetDuplicator
{
	// *************** public interface ****************************************************
	
	/** constructor that allows subclass/override mechanism if needed */
	public DatasetDuplicator()
	{
	}
	
	/** creates a Dataset according to given factory's style but whose shape and data values are copied from a given Dataset */ 
	public Dataset createDataset(DatasetFactory factory, Dataset inputDataset)
	{
		Type type = inputDataset.getType();
		
		return createTypeConvertedDataset(factory, type, inputDataset);
	}
	
	// newer way - minimizes subset lookups using primitive access so faster but not yet working
	
	/** create a Dataset according to given factory's style and a specified type but whose shape and data values are copied from a given Dataset */
	public Dataset createTypeConvertedDataset(DatasetFactory factory, Type type, Dataset inputDataset)
	{
		int[] dimensions = inputDataset.getDimensions();
		
		Dataset newDataset = factory.createDataset(type, dimensions);
		
		int[] origin = Index.create(dimensions.length);
		
		RegionCopyOperation copier = new RegionCopyOperation(inputDataset, origin, newDataset, origin, dimensions, inputDataset.getType().isFloat());

		copier.execute();
		
		// TODO - SOMETHING NEEDS TO BE DONE HERE ABOUT PRESERVING METADATA
		// newDataset.setMetaData(inputDataset.getMetaData().clone());  // something like this???
		
		return newDataset;
	}
}
