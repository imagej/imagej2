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
		CopyFunction copier;
		if (type.isFloat())
			copier = new DoubleCopyFunction(dataset, newDataset);
		else
			copier = new LongCopyFunction(dataset, newDataset);
		
		copyData(dimensions, copier);
		
		newDataset.setMetaData(dataset.getMetaData());  // TODO - PROBABLY NEED TO CLONE THE METADATA HERE!!!!!!!!!!!
		
		return newDataset;
	}
	
	/** copy data */
	private void copyData(int[] dimensions, CopyFunction copier)
	{
		int[] position = Index.create(dimensions.length);
		int[] origin = Index.create(dimensions.length);
		int[] span = dimensions;

		// TODO - copying in the easiest and slowest way possible - do some speed up by indexing on planes to minimize Dataset subset lookup times
		while (Index.isValid(position, origin, span))
		{
			copier.copyValueFrom(position);
			Index.increment(position, origin, span);
		}
	}
	
	private interface CopyFunction
	{
		void copyValueFrom(int[] position);
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
		
		public void copyValueFrom(int[] position)
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
		
		public void copyValueFrom(int[] position)
		{
			double value = this.fromDataset.getDouble(position);
			this.toDataset.setDouble(position, value);
		}
	}
}
