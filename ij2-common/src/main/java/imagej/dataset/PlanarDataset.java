package imagej.dataset;

import imagej.MetaData;
import imagej.Utils;
import imagej.data.DataAccessor;
import imagej.data.Type;
import imagej.data.Types;

import java.lang.reflect.Array;

// TODO - our convention is that indexing subsets moves right to left. seems to match imglib

// TODO - I'm not checking that user provided axis values are in a valid range in all the methods below

// TODO - note that cannot add subsets/remove subsets. It would be great if we could add a row or a column and have it fixup.
//   The arrayOfData reference would then be wrong. But otherwise it seems supportable;

// TODO - metadata support is nearly nonexistent. May need global metadata (num dims in primitive access) and then per subset metadata (labels)

public class PlanarDataset implements Dataset, RecursiveDataset
{
	private Object arrayOfData;
	private int[] dimensions;
	private Type type;
	private DataAccessor dataAccessor;
	private Dataset parent;
	private MetaData metadata;

	private void verifyInputOkay(int[] dimensions, Type type, Object arrayOfData)
	{
		// TODO - modify imglib so we can relax this constraint???
		if (dimensions.length != 2)
			throw new IllegalArgumentException("PlanarDataset requires dimensionality of 2 rather than "+dimensions.length);
			
		if (arrayOfData == null)
			throw new IllegalArgumentException("PlanarDataset needs a non null data array to use as storage");
		
		if (!arrayOfData.getClass().isArray())
			throw new IllegalArgumentException("expected an array as input");
		
		long numPixels = Utils.getTotalSamples(dimensions);
		
		long expectedArrayLength = type.calcNumStorageUnitsFromPixelCount(numPixels);
		
		if (Array.getLength(arrayOfData) != expectedArrayLength)
			throw new IllegalArgumentException("input array length does not match total sample count of given input dimensions");
	
		Types.verifyCompatibility(type, arrayOfData);
	}
	
	public PlanarDataset(int[] dimensions, Type type, Object arrayOfData)
	{
		verifyInputOkay(dimensions, type, arrayOfData);
		
		this.dimensions = dimensions;
		this.type = type;
		this.arrayOfData = arrayOfData;
		this.dataAccessor = type.allocateArrayAccessor(arrayOfData);
		this.parent = null;
		this.metadata = null;
	}

	@Override
	public int[] getDimensions()
	{
		return this.dimensions;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	@Override
	public MetaData getMetaData()
	{
		return this.metadata;
	}
	
	@Override
	public void setMetaData(MetaData metadata)
	{
		this.metadata = metadata;
	}
	
	@Override
	public Object getData()
	{
		return this.arrayOfData;
	}

	@Override
	public void setData(Object arrayOfData)
	{
		verifyInputOkay(this.dimensions, this.type, arrayOfData);

		this.arrayOfData = arrayOfData;
		this.dataAccessor = this.type.allocateArrayAccessor(arrayOfData);
	}
	
	@Override
	public void releaseData()
	{
		// not necessary to do anything
	}
	
	@Override
	public boolean isComposite()
	{
		return false;
	}

	@Override
	public Dataset insertNewSubset(int position)
	{
		throw new UnsupportedOperationException("PlanarDataset dimensions are unmodifiable");
	}

	@Override
	public Dataset removeSubset(int position)
	{
		throw new UnsupportedOperationException("PlanarDataset dimensions are unmodifiable");
	}

	@Override
	public Dataset getSubset(int position)
	{
		// TODO - hatch some kind of ReferenceDataset that stores this dataset and does appropriate coord transforms
		throw new UnsupportedOperationException("Cannot get a subset of a PlanarDataset");
	}

	@Override
	public Dataset getSubset(int[] position)
	{
		return getSubset(position, 1);
	}

	@Override
	public Dataset getSubset(int[] position, int axis)
	{
		// TODO - hatch some kind of ReferenceDataset that stores this dataset and does appropriate coord transforms
		throw new UnsupportedOperationException("Cannot get a subset of a PlanarDataset");
	}

	@Override
	public double getDouble(int[] position)
	{
		return getDouble(position, 1);
	}

	@Override
	public void setDouble(int[] position, double value)
	{
		setDouble(position, 1, value);
	}

	@Override
	public double getDouble(int[] index, int axis)
	{
		if (axis != 1)
			throw new IllegalArgumentException();
		
		int x = index[0];
		int y = index[1];
		
		int sampleNum = y*this.dimensions[0] + x;
		
		return this.dataAccessor.getReal(sampleNum);
	}

	@Override
	public void setDouble(int[] index, int axis, double value)
	{
		if (axis != 1)
			throw new IllegalArgumentException();

		int x = index[0];
		int y = index[1];
		
		int sampleNum = y*this.dimensions[0] + x;
		
		this.dataAccessor.setReal(sampleNum, value);
	}
	
	@Override
	public Dataset getParent()
	{
		return this.parent;
	}
	
	@Override
	public void setParent(Dataset dataset)
	{
		this.parent = dataset;
	}
}

