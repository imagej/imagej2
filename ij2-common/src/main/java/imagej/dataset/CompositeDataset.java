package imagej.dataset;

import imagej.MetaData;
import imagej.DataType;

import java.util.ArrayList;

//TODO - our convention is that indexing subsets moves right to left

public class CompositeDataset implements Dataset, RecursiveDataset
{
	private Dataset parent;
	private ArrayList<Dataset> subsets;
	private DataType type;
	private int[] dimensions;

	public CompositeDataset(DataType type, int[] dimensions, ArrayList<Dataset> subsets)
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
	public double getDouble(int[] index, int axis)
	{
		RecursiveDataset subset = (RecursiveDataset) getSubset(index[axis]); 
		return subset.getDouble(index, axis-1);
	}

	@Override
	public void setDouble(int[] index, int axis, double value)
	{
		RecursiveDataset subset = (RecursiveDataset) getSubset(index[axis]); 
		subset.setDouble(index, axis-1, value);
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
		return null;
		// throw new IllegalArgumentException("CompositeDatasets do not support getting data array");
	}

	@Override
	public void setData(Object data)
	{
		throw new IllegalArgumentException("CompositeDatasets do not support setting data array");
	}

	@Override
	public void releaseData()
	{
		// not necessary to do anything
	}
	
	@Override
	public Dataset insertNewSubset(int position)
	{
		if (this.parent != null)
			throw new IllegalArgumentException("can only add subsets to outermost Dataset");

		int[] subsetDimensions = this.dimensions.clone();
		
		subsetDimensions[position] = 1;
		
		Dataset ds = new PlanarDatasetFactory().createDataset(this.type, subsetDimensions);
		
		ds.setParent(this);
		
		this.subsets.add(position, ds);
		
		int outermostAxis = this.dimensions.length - 1;
		
		this.dimensions[outermostAxis]++;
		
		return ds;
	}

	@Override
	public Dataset removeSubset(int position)
	{
		if (this.parent != null)
			throw new IllegalArgumentException();
		
		Dataset ds = this.subsets.remove(position);
		
		ds.setParent(null);
		
		int outermostAxis = this.dimensions.length - 1;
		
		this.dimensions[outermostAxis]--;

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

		int outermostAxis = this.dimensions.length - 1;
		
		return getDouble(position, outermostAxis);
	}

	@Override
	public void setDouble(int[] position, double value)
	{
		if (this.parent != null)
			throw new IllegalArgumentException();
		
		int outermostAxis = this.dimensions.length - 1;

		setDouble(position, outermostAxis, value);
	}

	@Override
	public Dataset getSubset(int[] partialIndex, int axis)
	{
		if (axis < 0)
			throw new IllegalArgumentException("axis index ("+axis+") less than 0");

		RecursiveDataset nextSubset = (RecursiveDataset)getSubset(partialIndex[axis]);
		
		if (axis == 0)
			return (Dataset) nextSubset;
		
		return nextSubset.getSubset(partialIndex, axis-1);
	}

	@Override
	public Dataset getSubset(int[] index)
	{
		int outermostAxis = index.length - 1;

		return getSubset(index, outermostAxis);
	}
	
}
