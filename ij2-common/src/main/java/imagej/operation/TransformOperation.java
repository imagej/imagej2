package imagej.operation;

import imagej.Dimensions;
import imagej.dataset.Dataset;
import imagej.function.DoubleFunction;
import imagej.function.LongFunction;
import imagej.function.NAryFunction;
import imagej.iterator.SynchronizedIterator;

public class TransformOperation
{
	private final LongFunction longFunction;
	private final DoubleFunction doubleFunction;
	private final int datasetToChange;
	private final SynchronizedIterator iter;
	
	public TransformOperation(LongFunction function, Dataset[] datasets, int[][] origins, int[] span, int datasetToChange)
	{
		for (int i = 0; i < datasets.length; i++)
			Dimensions.verifyDimensions(datasets[i].getDimensions(), origins[i], span);
		
		if (function.getValueCount() != datasets.length)
			throw new IllegalArgumentException("MultiDatasetTransformOperation - passed a function whose number of inputs ("+function.getValueCount()+
					") does not match number of datasets given ("+datasets.length+")");
		
		this.doubleFunction = null;
		this.longFunction = function;
		this.datasetToChange = datasetToChange;
		this.iter = new SynchronizedIterator(datasets, origins, span, false);
	}
	
	public TransformOperation(DoubleFunction function, Dataset[] datasets, int[][] origins, int[] span, int datasetToChange)
	{
		for (int i = 0; i < datasets.length; i++)
			Dimensions.verifyDimensions(datasets[i].getDimensions(), origins[i], span);
		
		if (function.getValueCount() != datasets.length)
			throw new IllegalArgumentException("MultiDatasetTransformOperation - passed a function whose number of inputs ("+function.getValueCount()+
					") does not match number of datasets given ("+datasets.length+")");
		
		this.doubleFunction = function;
		this.longFunction = null;
		this.datasetToChange = datasetToChange;
		this.iter = new SynchronizedIterator(datasets, origins, span, true);
	}
	
	public void execute()
	{
		double[] doubleWorkspace = iter.getDoubleWorkspace();
		long[] longWorkspace = iter.getLongWorkspace();
		while (this.iter.positionValid())
		{
			this.iter.loadWorkspace();
			if (this.doubleFunction != null)
			{
				double value = this.doubleFunction.compute(doubleWorkspace);
				this.iter.setDouble(this.datasetToChange, value);
			}
			else  // longFunction != null
			{
				long value = this.longFunction.compute(longWorkspace);
				this.iter.setLong(this.datasetToChange, value);
			}
			this.iter.incrementPosition();
		}
	}
}
