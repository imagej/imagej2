package imagej.operation;

import imagej.Dimensions;
import imagej.dataset.Dataset;
import imagej.iterator.SynchronizedIterator;

/** Simple implementation of a data copier. There are more efficient ways to do so. */ 
public class RegionCopyOperation
{
	// ***************** instance variables ***********************************************************
	
	private SynchronizedIterator iter;
	private CopyFunction copier;
	
	// ***************** constructor ***********************************************************
	
	public RegionCopyOperation(Dataset inputDataset, int[] inputOrigin, Dataset outputDataset, int[] outputOrigin, int[] span, boolean floatData)
	{
		Dimensions.verifyDimensions(inputDataset.getDimensions(), inputOrigin, span);
		Dimensions.verifyDimensions(outputDataset.getDimensions(), outputOrigin, span);
		
		this.iter = new SynchronizedIterator(new Dataset[]{inputDataset, outputDataset}, new int[][]{inputOrigin, outputOrigin}, span, floatData);
		if (floatData)
			this.copier = new DoubleCopyFunction(this.iter);
		else
			this.copier = new LongCopyFunction(this.iter);
	}
	
	// ***************** public interface ***********************************************************

	/** run the actual data copy operation */
	public void execute()
	{
		while (this.iter.positionValid())
		{
			this.iter.loadWorkspace();
			this.copier.copyValue();
			this.iter.incrementPosition();
		}
	}
	
	// ***************** private interface ***********************************************************

	private interface CopyFunction
	{
		void copyValue();
	}
	
	private class LongCopyFunction implements CopyFunction
	{
		private long[] longWorkspace;
		private SynchronizedIterator iter;
		
		public LongCopyFunction(SynchronizedIterator iter)
		{
			this.iter = iter;
			this.longWorkspace = iter.getLongWorkspace();
		}
		
		public void copyValue()
		{
			long value = this.longWorkspace[0];;
			
			this.iter.setLong(1, value);
		}
	}
	
	private class DoubleCopyFunction implements CopyFunction
	{
		private double[] doubleWorkspace;
		private SynchronizedIterator iter;
		
		public DoubleCopyFunction(SynchronizedIterator iter)
		{
			this.iter = iter;
			this.doubleWorkspace = iter.getDoubleWorkspace();
		}
		
		public void copyValue()
		{
			double value = this.doubleWorkspace[0];
			
			this.iter.setDouble(1, value);
		}
	}
}
