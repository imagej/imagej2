package imagej.dataset;

import imagej.MetaData;
import imagej.data.Type;

public class ReadOnlyDataset implements Dataset
{
	private Dataset dataset;
	
	public ReadOnlyDataset(Dataset dataset)
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
		return this.dataset.getMetaData(); // TODO - could user make changes here and cause problems?
	}

	@Override
	public void setMetaData(MetaData metadata)
	{
		throw new UnsupportedOperationException("readonly dataset - cannot set metadata");
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
		throw new UnsupportedOperationException("readonly dataset - cannot set parent");
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
		throw new UnsupportedOperationException("readonly dataset - cannot set data plane");
	}

	@Override
	public Dataset insertNewSubset(int position)
	{
		throw new UnsupportedOperationException("readonly dataset - cannot insert new subset");
	}

	@Override
	public Dataset removeSubset(int position)
	{
		throw new UnsupportedOperationException("readonly dataset - cannot remove subset");
	}

	@Override
	public Dataset getSubset(int position)
	{
		return new ReadOnlyDataset(this.dataset.getSubset(position));
	}

	@Override
	public Dataset getSubset(int[] index)
	{
		return new ReadOnlyDataset(this.dataset.getSubset(index));
	}

	@Override
	public double getDouble(int[] position)
	{
		return this.dataset.getDouble(position);
	}

	@Override
	public void setDouble(int[] position, double value) {
		throw new UnsupportedOperationException("readonly dataset - cannot set sample values");
	}

	@Override
	public long getLong(int[] position) {
		return this.dataset.getLong(position);
	}

	@Override
	public void setLong(int[] position, long value) {
		throw new UnsupportedOperationException("readonly dataset - cannot set sample values");
	}

}
