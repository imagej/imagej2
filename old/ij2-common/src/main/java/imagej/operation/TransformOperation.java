package imagej.operation;

import imagej.Dimensions;
import imagej.dataset.Dataset;
import imagej.function.DoubleFunction;
import imagej.function.LongFunction;
import imagej.iterator.SynchronizedIterator;

// TODO
//     The best implementation is not to have an TransformOperation. We should have an AssignOperation that takes N input
//   Datasets and assigns values to an output Dataset. A transform is then an AssignOperation where one of the input datasets
//   is passed as an output dataset. The issue is that SynchronizedIterator would need to know about N+1 datasets. Yet it
//   would need a workspace of size N (filling the N values from the correct datasets). Not very elegant and for transforms
//   the iteration is less efficient. Think about this some more.
//     One thing we could do is compose a type of Long/DoubleFunction that takes another Function and wraps it such that it's
//   compute() method ignores one input. Still that might require a temp workspace that copies from the parent workspace
//   before computing values. And also does delegation. Again both of these introduce inefficiencies.
//     What if a SynchronizedIterator could accept functions that took a function that handled N or N-1 inputs. If N-1 then
//   workspace allocated that size. Maybe not. Maybe a SyncIter could determine if a Dataset is repeated in the inputs. Then
//   check that num unique datasets == functions getValueCount(). It would allocate workspace for num unique inputs. And then
//   it would fill them in left to right order and only once for repeated datasets. Though this makes transforms work better
//   kills the assign idea - the output dataset would be different and thus the workspace would be one too big. Also the thought
//   occurred that what if you specify a Dataset twice but with different regions within it and want to do some operation?
//     Note that unlike the imglib Operations these ones have not yet been extended to support Observers and SelectionFunctions.

/** A TransformOperation changes the values of one specified Dataset by applying a function on the values of all input Datasets.
 *  This treats the output Dataset as an input Dataset also. The function is only applied across synchronized user defined
 *  regions. The user specified function can be either a DoubleFunction or a LongFunction. The transform works in doubles or
 *  longs appropriately. Use an AssignOperation to set the value of one specified Dataset to a function of other Datasets
 *  with.
 */
public class TransformOperation
{
	// ************ instance variables *********************************************************************************
	
	private final LongFunction longFunction;
	private final DoubleFunction doubleFunction;
	private final int datasetToChange;
	private final SynchronizedIterator iter;
	
	// ************ constructors *********************************************************************************

	/** constructs a transform that will work with longs internally */
	public TransformOperation(LongFunction function, Dataset[] datasets, int[][] origins, int[] span, int datasetToChange)
	{
		for (int i = 0; i < datasets.length; i++)
			Dimensions.verifyDimensions(datasets[i].getDimensions(), origins[i], span);
		
		if (function.getParameterCount() != datasets.length)
			throw new IllegalArgumentException("TransformOperation - passed a function whose number of inputs ("+function.getParameterCount()+
					") does not match number of datasets given ("+datasets.length+")");
		
		this.doubleFunction = null;
		this.longFunction = function;
		this.datasetToChange = datasetToChange;
		this.iter = new SynchronizedIterator(datasets, origins, span, false);
	}

	/** constructs a transform that will work with doubles internally */
	public TransformOperation(DoubleFunction function, Dataset[] datasets, int[][] origins, int[] span, int datasetToChange)
	{
		for (int i = 0; i < datasets.length; i++)
			Dimensions.verifyDimensions(datasets[i].getDimensions(), origins[i], span);
		
		if (function.getParameterCount() != datasets.length)
			throw new IllegalArgumentException("TransformOperation - passed a function whose number of inputs ("+function.getParameterCount()+
					") does not match number of datasets given ("+datasets.length+")");
		
		this.doubleFunction = function;
		this.longFunction = null;
		this.datasetToChange = datasetToChange;
		this.iter = new SynchronizedIterator(datasets, origins, span, true);
	}

	// ************ public interface *********************************************************************************

	/** executes the transform changing the target dataset as it goes */
	public void execute()
	{
		if (iter.getDoubleWorkspace() != null)
			doubleExecution();
		else
			longExecution();
	}
	
	// ************ private interface *********************************************************************************

	private void longExecution()
	{
		long[] longWorkspace = iter.getLongWorkspace();
		while (this.iter.positionValid())
		{
			this.iter.loadWorkspace();
			long value = this.longFunction.compute(longWorkspace);
			this.iter.setLong(this.datasetToChange, value);
			this.iter.incrementPosition();
		}
	}
	
	private void doubleExecution()
	{
		double[] doubleWorkspace = iter.getDoubleWorkspace();
		while (this.iter.positionValid())
		{
			this.iter.loadWorkspace();
			double value = this.doubleFunction.compute(doubleWorkspace);
			this.iter.setDouble(this.datasetToChange, value);
			this.iter.incrementPosition();
		}
	}
}
