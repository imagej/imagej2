package imagej.dataset;

import imagej.EncodingManager;
import imagej.MetaData;
import imagej.UserType;
import imagej.Utils;
import imagej.primitive.DataAccessFactory;
import imagej.primitive.DataReader;
import imagej.primitive.DataWriter;

import java.lang.reflect.Array;

// TODO - must decide if indexing subsets right to left or left to right

public class PlanarDataset implements Dataset, RecursiveDataset
{
	private Object arrayOfData;
	private int[] dimensions;
	private UserType type;
	private DataReader dataReader;
	private DataWriter dataWriter;
	private Dataset parent;

	private void verifyInputOkay(int[] dimensions, UserType type, Object arrayOfData)
	{
		// TODO - modify imglib so we can relax this constraint???
		if (dimensions.length != 2)
			throw new IllegalArgumentException("PlanarDataset requires dimensionality of 2 rather than "+dimensions.length);
			
		if (!arrayOfData.getClass().isArray())
			throw new IllegalArgumentException("expected an array as input");
		
		if (Array.getLength(arrayOfData) != Utils.getTotalSamples(dimensions))
			throw new IllegalArgumentException("array input array length does not match total sample count of given input dimensions");
	
		EncodingManager.verifyTypeCompatibility(arrayOfData, type);
	}

	public PlanarDataset(int[] dimensions, UserType type, Object arrayOfData)
	{
		verifyInputOkay(dimensions, type, arrayOfData);
		
		this.dimensions = dimensions;
		this.type = type;
		this.arrayOfData = arrayOfData;
		this.dataReader = DataAccessFactory.getReader(type, arrayOfData);
		this.dataWriter = DataAccessFactory.getWriter(type, arrayOfData);
		this.parent = null;
	}

	@Override
	public int[] getDimensions()
	{
		return this.dimensions;
	}
	
	@Override
	public UserType getType()
	{
		return this.type;
	}
	
	@Override
	public MetaData getMetaData()
	{
		// TODO - do something
		return null;
	}
	
	@Override
	public void setMetaData(MetaData metadata)
	{
		// TODO - do something
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
		return getSubset(position,0);
	}

	@Override
	public Dataset getSubset(int[] position, int axis)
	{
		if (axis == position.length)
			return this;

		if ((axis < 0) || (axis > position.length))
			throw new IllegalArgumentException("axis index ("+axis+") out of bounds (0-"+position.length+")");
		
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
		return this.dataReader.getValue(sampleNum);
	}

	@Override
	public void setDouble(int[] index, int axis, double value)
	{
		int x = index[0];
		int y = index[1];
		int sampleNum = y*this.dimensions[0] + x;
		this.dataWriter.setValue(sampleNum, value);
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

