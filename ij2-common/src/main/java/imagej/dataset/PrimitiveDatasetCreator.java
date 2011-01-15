package imagej.dataset;

import java.lang.reflect.Array;

import imagej.Dimensions;
import imagej.data.DataAccessor;
import imagej.data.Type;
import imagej.data.Types;
import imagej.process.Index;

/** creates a Dataset from an input primitive array of values. */
public class PrimitiveDatasetCreator
{
	//************* instance variables ********************************************
 
	private DatasetFactory factory;

	//************* constructor ********************************************
	 
	/** creates a PrimitiveDatasetCreator using user specified DatasetFactory. */
	public PrimitiveDatasetCreator(DatasetFactory factory)
	{
		this.factory = factory;
	}
	
	//************* public interface ********************************************
	 
	/** creates a Dataset given input values
	 * 
	 * @param dimensions - the dimensions of the desired Dataset.
	 * @param unsigned - boolean specifying whether input array is filled unsigned or signed data.
	 * @param arrayOfData - the 1-D array of input sample values. length must match number of samples
	 * contained by dimensions.
	 * @return a Dataset matching criteria. Input array reference is NOT permanently used by the Dataset.
	 */
	public Dataset createDataset(int[] dimensions, boolean unsigned, Object arrayOfData)
	{
		Class<?> objectType = arrayOfData.getClass();
		
		if (!objectType.isArray())
			throw new IllegalArgumentException("input data must be an array");
		
		long totalSamples = Dimensions.getTotalSamples(dimensions);
		
		if (Array.getLength(arrayOfData) != totalSamples)
			throw new IllegalArgumentException("input data array size is not compatible with specified dimensions");
		
		Type dataType = identifyType(unsigned, arrayOfData);
		
		if (dataType == null)
			throw new IllegalArgumentException("unsupported array type: "+objectType+" (unsigned = "+unsigned+")");
		
		Dataset dataset = this.factory.createDataset(dataType, dimensions);
		
		int[] index = Index.create(dimensions.length);
		int[] origin = Index.create(dimensions.length);
		int[] span = dimensions;

		boolean floatData = dataType.isFloat();
		
		DataAccessor accessor = dataType.allocateArrayAccessor(arrayOfData);
		
		int i = 0;
		while (Index.isValid(index, origin, span))
		{
			if (floatData)
				dataset.setDouble(index, accessor.getReal(i++));
			else  // integral data
				dataset.setLong(index, accessor.getIntegral(i++));
			
			Index.increment(index, origin, span);
		}
		
		return dataset;
	}
	
	//************* private interface ********************************************
	
	/** maps input data to an ImageJ 2.x Type */
	private Type identifyType(boolean unsigned, Object arrayOfData)
	{
		Type dataType = null;
		
		if (arrayOfData instanceof byte[])
		{
			if (unsigned)
				dataType = Types.findType("8-bit unsigned)");
			else
				dataType = Types.findType("8-bit signed)");
		}
		else if (arrayOfData instanceof short[])
		{
			if (unsigned)
				dataType = Types.findType("16-bit unsigned)");
			else
				dataType = Types.findType("16-bit signed)");
		}
		else if (arrayOfData instanceof int[])
		{
			if (unsigned)
				dataType = Types.findType("32-bit unsigned)");
			else
				dataType = Types.findType("32-bit signed)");
		}
		else if (arrayOfData instanceof float[])
		{
			dataType = Types.findType("32-bit float)");
		}
		else if (arrayOfData instanceof double[])
		{
			dataType = Types.findType("64-bit float)");
		}
		else if (arrayOfData instanceof long[])
		{
			if (!unsigned)
				dataType = Types.findType("64-bit signed)");
		}

		// TODO
		// it would be nice to support boolean[] but DataAccessor for 1-bit type handles an int[]
		
		return dataType;
	}
}
