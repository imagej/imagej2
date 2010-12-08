package imagej.data;

public class LongArrayAccessor implements DataAccessor
{
	private long[] longs;
	
	public LongArrayAccessor(Object data)
	{
		this.longs = (long[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		return this.longs[(int)index];
	}

	@Override
	public void setReal(long index, double value)
	{
		this.longs[(int)index] = (long)value;
	}

	@Override
	public long getIntegral(long index)
	{
		return this.longs[(int)index];
	}

	@Override
	public void setIntegral(long index, long value)
	{
		this.longs[(int)index] = value;
	}

}
