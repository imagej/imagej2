package imagej.dataset;

import imagej.MetaData;
import imagej.data.Type;

public class FixedDimensionDataset implements Dataset
{
	private Dataset dataset;
	
	public FixedDimensionDataset(Dataset dataset)
	{
		this.dataset = dataset;
	}
	
	@Override
	public int[] getDimensions()
	{
		return this.dataset.getDimensions();
	}

	@Override
	public Type getType()
	{
		return this.dataset.getType();
	}

	@Override
	public MetaData getMetaData()
	{
		return this.dataset.getMetaData();
	}

	@Override
	public void setMetaData(MetaData metadata)
	{
		this.dataset.setMetaData(metadata);
	}

	@Override
	public boolean isComposite()
	{
		return this.dataset.isComposite();
	}

	@Override
	public Dataset getParent()
	{
		return this.dataset.getParent();
	}

	@Override
	public void setParent(Dataset dataset)
	{
		this.dataset.setParent(dataset);
	}

	@Override
	public Object getData()
	{
		return this.dataset.getData();
	}

	@Override
	public void releaseData()
	{
		this.dataset.releaseData();
	}

	@Override
	public void setData(Object data)
	{
		this.dataset.setData(data);
	}

	@Override
	public Dataset insertNewSubset(int position)
	{
		throw new UnsupportedOperationException("fixed dimension dataset - cannot insert new subset");
	}

	@Override
	public Dataset removeSubset(int position)
	{
		throw new UnsupportedOperationException("fixed dimension dataset - cannot remove subset");
	}

	@Override
	public Dataset getSubset(int position)
	{
		return this.dataset.getSubset(position);
	}

	@Override
	public Dataset getSubset(int[] index)
	{
		return this.dataset.getSubset(index);
	}

	@Override
	public double getDouble(int[] position)
	{
		return this.dataset.getDouble(position);
	}

	@Override
	public void setDouble(int[] position, double value)
	{
		this.dataset.setDouble(position, value);
	}

	@Override
	public long getLong(int[] position)
	{
		return this.dataset.getLong(position);
	}

	@Override
	public void setLong(int[] position, long value)
	{
		this.dataset.setLong(position, value);
	}

}
