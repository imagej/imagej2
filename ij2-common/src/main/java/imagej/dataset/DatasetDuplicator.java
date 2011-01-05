package imagej.dataset;

import imagej.data.Type;
import imagej.process.Index;

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
	
	/** create a Dataset according to given factory's style and a specified type but whose shape and data values are copied from a given Dataset */ 
	public Dataset createTypeConvertedDataset(DatasetFactory factory, Type type, Dataset inputDataset)
	{
		int[] dimensions = inputDataset.getDimensions();
		
		Dataset newDataset = factory.createDataset(type, dimensions);
		
		// choose the best way to copy to assure no precision loss
		// TODO - could test here vs. output dataset. I'm not sure it matters. But this way input dataset values are fully preserved before conversion
		CopyFunction copier;
		if (inputDataset.getType().isFloat())
			copier = new DoubleCopyFunction(inputDataset, newDataset);
		else
			copier = new LongCopyFunction(inputDataset, newDataset);
		
		copyData(dimensions, copier);
		
		// TODO - SOMETHING NEEDS TO BE DONE HERE ABOUT PRESERVING METADATA
		// newDataset.setMetaData(inputDataset.getMetaData().clone());  // something like this???
		
		return newDataset;
	}
	
	// *************** private interface ****************************************************
	
	/** copy data */
	private void copyData(int[] dimensions, CopyFunction copier)
	{
		int[] position = Index.create(dimensions.length);
		int[] origin = Index.create(dimensions.length);
		int[] span = dimensions;

		// TODO - copying in the easiest but slowest way possible - do some speed up by indexing on planes to minimize Dataset subset lookup times
		while (Index.isValid(position, origin, span))
		{
			copier.copyValue(position);
			Index.increment(position, origin, span);
		}
	}
	
	private interface CopyFunction
	{
		void copyValue(int[] position);
	}
	
	private class LongCopyFunction implements CopyFunction
	{
		private Dataset fromDataset;
		private Dataset toDataset;
		
		public LongCopyFunction(Dataset from, Dataset to)
		{
			this.fromDataset = from;
			this.toDataset = to;
		}
		
		public void copyValue(int[] position)
		{
			long value = this.fromDataset.getLong(position);
			
			this.toDataset.setLong(position, value);
		}
	}
	
	private class DoubleCopyFunction implements CopyFunction
	{
		private Dataset fromDataset;
		private Dataset toDataset;
		
		public DoubleCopyFunction(Dataset from, Dataset to)
		{
			this.fromDataset = from;
			this.toDataset = to;
		}
		
		public void copyValue(int[] position)
		{
			double value = this.fromDataset.getDouble(position);
			
			this.toDataset.setDouble(position, value);
		}
	}
}
