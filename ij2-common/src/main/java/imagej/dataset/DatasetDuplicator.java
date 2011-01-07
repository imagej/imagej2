package imagej.dataset;

import imagej.Dimensions;
import imagej.data.Type;
import imagej.function.NAryFunction;
import imagej.process.Index;
import imagej.process.Span;

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
	/*
	public Dataset createTypeConvertedDataset(DatasetFactory factory, Type type, Dataset inputDataset)
	{
		int[] dimensions = inputDataset.getDimensions();
		
		Dataset newDataset = factory.createDataset(type, dimensions);

		NAryFunction copyFunc = new CopyRightmostNaryFunction(2);
		
		MultiDatasetTransformOperation copier = new MultiDatasetTransformOperation(copyFunc, new Dataset[]{newDataset, inputDataset});
		
		copier.execute();
		
		// TODO - SOMETHING NEEDS TO BE DONE HERE ABOUT PRESERVING METADATA
		// newDataset.setMetaData(inputDataset.getMetaData().clone());  // something like this???
		
		return newDataset;
	}
	
	*/
	
	// original way - works but may be very slow
	
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
	
	private class NestedIterator
	{
	}
	
	private class SynchronizedIterator
	{
		private Dataset[] datasets;
		private Dataset[] directAccessDatasets;
		private int[][] outerPositions;
		private int[][] innerPositions;
		private int[][] outerOrigins;
		private int[][] innerOrigins;
		private int[][] outerSpans;
		private int[][] innerSpans;
		private double[] workspace;
		private int datasetCount;
		
		public SynchronizedIterator(Dataset[] datasets, double[] workspace)
		{
			if (datasets.length != workspace.length)
				throw new IllegalArgumentException("parameter count mismatch");
		
			for (int i = 0; i < datasets.length; i++)
			{
				if (Dimensions.getTotalSamples(datasets[0].getDimensions()) !=
					Dimensions.getTotalSamples(datasets[i].getDimensions()))
					throw new IllegalArgumentException("datasets are not compatible in size");
			}
			
			this.datasets = datasets;
			this.workspace = workspace;
			this.datasetCount = datasets.length;
			this.directAccessDatasets = new Dataset[this.datasetCount];
			this.outerPositions = new int[this.datasetCount][];
			this.innerPositions = new int[this.datasetCount][];
			this.outerOrigins = new int[this.datasetCount][];
			this.innerOrigins = new int[this.datasetCount][];
			this.outerSpans = new int[this.datasetCount][];
			this.innerSpans = new int[this.datasetCount][];
			for (int i = 0; i < this.datasetCount; i++)
			{
				int[] dimensions = this.datasets[i].getDimensions();
				int directAxisCount = this.datasets[i].getMetaData().getDirectAccessDimensionCount();
				int outerSize = dimensions.length - directAxisCount;
				int innerSize = directAxisCount;
				this.outerPositions[i] = Index.create(outerSize);
				this.innerPositions[i] = Index.create(innerSize);
				this.outerOrigins[i] = Index.create(outerSize);
				this.innerOrigins[i] = Index.create(innerSize);
				int[] outerSpan = new int[outerSize];
				for (int j = 0; j < outerSize; j++)
					outerSpan[j] = dimensions[innerSize+j];
				int[] innerSpan = new int[innerSize];
				for (int j = 0; j < innerSize; j++)
					innerSpan[j] = dimensions[j];
				this.outerSpans[i] = Span.create(outerSpan);
				this.innerSpans[i] = Span.create(innerSpan);
			}
		}
		
		private boolean valuesAtMax(int[] position, int[] bounds)
		{
			int positionLen = position.length;
			for (int i = 0; i < positionLen; i++)
				if (position[i] >= (bounds[i]-1))
					return false;
			
			return true;
		}
		
		public boolean hasNext()
		{
			for (int i = 0; i < this.datasetCount; i++)
			{
				if (valuesAtMax(this.outerPositions[i],this.outerSpans[i]) &&
						valuesAtMax(this.innerPositions[i],this.innerSpans[i]))
					return false;
			}
			return true;
		}
		
		public void next()
		{
			int[] innerPosition;
			int[] innerOrigin;
			int[] innerSpan;
			int[] outerPosition;

			for (int i = 0; i < this.datasetCount; i++)
			{
				innerPosition = this.innerPositions[i];

				if (this.directAccessDatasets[i] == null) // first pass
				{
					this.directAccessDatasets[i] = this.datasets[i].getSubset(this.outerPositions[i]);
				}
				else  // pointing at an inner dataset already
				{
					innerOrigin = this.innerOrigins[i];
					innerSpan = this.innerSpans[i];
					
					Index.increment(innerPosition, innerOrigin, innerSpan);
					
					if (!Index.isValid(innerPosition, innerOrigin, innerSpan))
					{
						outerPosition = this.outerPositions[i];
						
						Index.increment(outerPosition, this.outerOrigins[i], this.outerSpans[i]);
						
						this.directAccessDatasets[i] = this.datasets[i].getSubset(outerPosition);
						
						for (int j = 0; j < innerPosition.length; j++)
							innerPosition[j] = 0;
					}
				}
				
				this.workspace[i] = this.directAccessDatasets[i].getDouble(innerPosition);
			}
		}
		
		public void setDouble(int iterNumber, double value)
		{
			int[] subPosition = this.innerPositions[iterNumber];
			this.directAccessDatasets[iterNumber].setDouble(subPosition, value);
		}
	}
	
	private class MultiDatasetTransformOperation
	{
		private NAryFunction function;
		private SynchronizedIterator iter;
		private double[] workspace;
		
		MultiDatasetTransformOperation(NAryFunction function, Dataset[] datasets)
		{
			this.function = function;
			this.workspace = new double [function.getValueCount()];
			this.iter = new SynchronizedIterator(datasets, this.workspace);
		}
		
		void execute()
		{
			while (this.iter.hasNext())
			{
				this.iter.next();
				this.iter.setDouble(0, this.function.compute(this.workspace));
			}
		}
	}
	
	private class CopyRightmostNaryFunction implements NAryFunction
	{
		private int numValues;
		private boolean firstTime;
		
		public CopyRightmostNaryFunction(int numValues)
		{
			this.numValues = numValues;
			this.firstTime = false;
		}

		
		@Override
		public int getValueCount()
		{
			return this.numValues;
		}

		@Override
		public double compute(double[] inputs)
		{
			if (this.firstTime)
			{
				if (inputs.length != this.numValues)
					throw new IllegalArgumentException("nary function not given correct number of inputs");
			}
			return inputs[this.numValues-1];
		}
		
	}
}
