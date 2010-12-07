package imagej.data;

public class LongAccessor implements DataAccessor
{
	private long[] longs;
	
	public LongAccessor(Object data)
	{
		this.longs = (long[]) data;
	}
	
	@Override
	public double getReal(int index)
	{
		return this.longs[index];
	}

	@Override
	public void setReal(int index, double value)
	{
		this.longs[index] = (long)value;
	}

	@Override
	public long getIntegral(int index)
	{
		return this.longs[index];
	}

	@Override
	public void setIntegral(int index, long value)
	{
		this.longs[index] = value;
	}

}
