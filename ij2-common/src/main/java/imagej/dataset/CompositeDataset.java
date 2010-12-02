package imagej.dataset;

import imagej.MetaData;
import imagej.UserType;

import java.util.ArrayList;

public class CompositeDataset implements Dataset, RecursiveDataset
{
	private Dataset parent;
	private ArrayList<Dataset> subsets;
	private UserType type;
	private int[] dimensions;

	public CompositeDataset(UserType type, int[] dimensions, ArrayList<Dataset> subsets)
	{
		this.type = type;
		this.dimensions = dimensions;
		this.subsets = subsets;
		this.parent = null;
		for (Dataset subset : this.subsets)
			subset.setParent(this);
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
	public double getDouble(int[] index, int axis)
	{
		RecursiveDataset subset = (RecursiveDataset) getSubset(index[axis]); 
		return subset.getDouble(index, axis+1);
	}

	@Override
	public void setDouble(int[] index, int axis, double value)
	{
		RecursiveDataset subset = (RecursiveDataset) getSubset(index[axis]); 
		subset.setDouble(index, axis+1, value);
	}

	@Override
	public boolean isComposite()
	{
		return true;
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

	@Override
	public Object getData()
	{
		// TODO - return null?
		throw new IllegalArgumentException("CompositeDatasets do not support getting data array");
	}

	@Override
	public void setData(Object data)
	{
		throw new IllegalArgumentException("CompositeDatasets do not support setting data array");
	}

	@Override
	public Dataset insertNewSubset(int position)
	{
		if (this.parent != null)
			throw new IllegalArgumentException("can only add subsets to outermost Dataset");

		int[] subsetDimensions = this.dimensions.clone();
		
		subsetDimensions[position] = 1;
		
		Dataset ds = new DatasetFactory().createDataset(this.type, subsetDimensions);
		
		ds.setParent(this);
		
		this.subsets.add(position, ds);
		
		return ds;
	}

	@Override
	public Dataset removeSubset(int position)
	{
		if (this.parent != null)
			throw new IllegalArgumentException();
		
		Dataset ds = this.subsets.remove(position);
		
		ds.setParent(null);
		
		return ds;
	}

	@Override
	public Dataset getSubset(int position)
	{
		return this.subsets.get(position);
	}

	@Override
	public double getDouble(int[] position)
	{
		if (this.parent != null)
			throw new IllegalArgumentException();
		
		return getDouble(position, this.dimensions.length-1);
	}

	@Override
	public void setDouble(int[] position, double value)
	{
		if (this.parent != null)
			throw new IllegalArgumentException();
		
		setDouble(position, this.dimensions.length-1, value);
	}

	@Override
	public Dataset getSubset(int[] partialIndex, int axis)
	{
		if (axis == partialIndex.length)
			return this;
		
		if ((axis < 0) || (axis > partialIndex.length))
			throw new IllegalArgumentException("axis index ("+axis+") out of bounds (0-"+partialIndex.length+")");

		return ((RecursiveDataset)getSubset(partialIndex[axis])).getSubset(partialIndex, axis+1);
	}

	@Override
	public Dataset getSubset(int[] index)
	{
		return getSubset(index, 0);
	}
	
}
