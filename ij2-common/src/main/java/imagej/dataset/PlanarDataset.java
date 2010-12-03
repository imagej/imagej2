package imagej.dataset;

import imagej.DataEncoding;
import imagej.EncodingManager;
import imagej.MetaData;
import imagej.DataType;
import imagej.Utils;
import imagej.primitive.DataAccessFactory;
import imagej.primitive.DataReader;
import imagej.primitive.DataWriter;

import java.lang.reflect.Array;

// TODO - our convention is that indexing subsets moves right to left

public class PlanarDataset implements Dataset, RecursiveDataset
{
	private Object arrayOfData;
	private int[] dimensions;
	private DataType type;
	private DataReader dataReader;
	private DataWriter dataWriter;
	private Dataset parent;

	private void verifyInputOkay(int[] dimensions, DataType type, Object arrayOfData)
	{
		// TODO - modify imglib so we can relax this constraint???
		if (dimensions.length != 2)
			throw new IllegalArgumentException("PlanarDataset requires dimensionality of 2 rather than "+dimensions.length);
			
		if (arrayOfData == null)
			throw new IllegalArgumentException("PlanarDataset needs a non null data array to use as storage");
		
		if (!arrayOfData.getClass().isArray())
			throw new IllegalArgumentException("expected an array as input");
		
		DataEncoding encoding = EncodingManager.getEncoding(type);
		
		int expectedArrayLength = EncodingManager.calcStorageUnitsRequired(encoding, (int) Utils.getTotalSamples(dimensions));
		
		if (Array.getLength(arrayOfData) != expectedArrayLength)
			throw new IllegalArgumentException("array input array length does not match total sample count of given input dimensions");
	
		EncodingManager.verifyTypeCompatibility(arrayOfData, type);
	}

	public PlanarDataset(int[] dimensions, DataType type, Object arrayOfData)
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
	public DataType getType()
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
		
		return this.dataReader.getValue(sampleNum);
	}

	@Override
	public void setDouble(int[] index, int axis, double value)
	{
		if (axis != 1)
			throw new IllegalArgumentException();

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

